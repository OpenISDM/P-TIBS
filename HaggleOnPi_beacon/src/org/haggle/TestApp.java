package org.haggle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.System;
import java.lang.Thread;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.haggle.DataObject.DataObjectException;
import org.haggle.Handle;
import org.haggle.EventHandler;
import org.haggle.DataObject;
import org.haggle.LaunchCallback;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class TestApp implements EventHandler {	
    private Handle h = null;
    private String name;
	private long num_dataobjects_received = 0;
	private boolean should_quit = false;
	private int num_dataobjects = 10;
	
	static String url = "jdbc:virtuoso://localhost:1111/";

	public synchronized void onNewDataObject(DataObject dObj) {
		num_dataobjects_received++;
        System.out.println("Got data object " + num_dataobjects_received + " filepath=" + dObj.getFilePath());	
	       
		dObj.dispose();
	}
	
	public synchronized void onNeighborUpdate(Node[] neighbors) {
        System.out.println("Got neighbor update event");

        for (int i = 0; i < neighbors.length; i++) {
                System.out.println("Neighbor: " + neighbors[i].getName());
        }
	}
	
    public synchronized void onInterestListUpdate(Attribute[] interests) {
        System.out.println("Got interest list update event");

        for (int i = 0; i < interests.length; i++) {
                System.out.println("Interest: " + interests[i].toString());
        }
    }
        
	public synchronized void onShutdown(int reason) {
        System.out.println("Got shutdown event, reason=" + reason);
        should_quit = true;
    }
	
	public void onEventLoopStart() {
		System.out.println("Event loop started");
	}
	
	public void onEventLoopStop() {
		System.out.println("Event loop stopped");
	}
	
    public TestApp(String name, int num_dataobjects){
        super();
        this.name = name;
        this.num_dataobjects = num_dataobjects;
    }
    
    public void start()
    {
    	 
        File beacon_file = new File("/root/ShareMAD/Beacon.json");
        
        long pid = Handle.getDaemonPid();

        System.out.println("Pid is " + pid);

        if (pid == 0) {
            // Haggle is not running
            if (Handle.spawnDaemon(new LaunchCallback() { 
                public int callback(long milliseconds) {
                    System.out.println("Launching haggle, milliseconds=" + milliseconds); 
                    return 0;
                }
            }) == false) {
                // Could not spawn daemon
                System.out.println("Could not spawn new Haggle daemon");
                return;
            }
        }
        try {
        	//DataObject[] dobjs = new DataObject[num_dataobjects];
            h = new Handle(name);

            h.registerEventInterest(EVENT_NEW_DATAOBJECT, this);
            h.registerEventInterest(EVENT_NEIGHBOR_UPDATE, this);
            h.registerEventInterest(EVENT_INTEREST_LIST_UPDATE, this);
            h.registerEventInterest(EVENT_HAGGLE_SHUTDOWN, this);
    
            h.eventLoopRunAsync(this);           

            h.getApplicationInterestsAsync();

            Thread.sleep(2000);
		
			System.out.println("Forcing Haggle to send node description");
	
			h.sendNodeDescription();
			
			Thread.sleep(3000);
			
			while(true){
				
				Thread.sleep(1000);
				
				rdf_file = new File("/var/www/IS/Refuge_dahu.rdf");
				json_file = new File("/root/ShareMAD/Refuge_dahu.json");
				
        		
        		long new_rdf_modifiedTime = rdf_file.lastModified();
        		long new_json_modifiedTime = rdf_file.lastModified();
        		
        		/*System.out.println("r: " + new_rdf_modifiedTime + " " + rdf_modifiedTime);
        		System.out.println("j: " + new_json_modifiedTime + " " + json_modifiedTime);*/
        		
        		if(new_rdf_modifiedTime > rdf_modifiedTime){
        			
        			//push rdf to another pi
        			
        			String newpath = "/root/ShareMAD/" + System.currentTimeMillis() + "--Refuge_dahu.rdf";
        			
        			copyFile("/var/www/IS/Refuge_dahu.rdf", newpath);
        			
        			DataObject piObj;
        				
    				piObj = new DataObject();
    				piObj = new DataObject(newpath);
    				piObj.addAttribute("RDF", "PI_RDF", 1);
    				
    				//copyFile("/var/www/IS/Refuge.rdf");
    				
    				h.publishDataObject(piObj);
    				
    				//new File("/var/www/IS/temp.rdf").renameTo(new File("/var/www/IS/Refuge.rdf"));
    				
    				rdf_modifiedTime = new_rdf_modifiedTime;
        			
        		}else if(new_json_modifiedTime > json_modifiedTime){
        			
        			String result = "";
        			
        			try{
        	        	FileReader fr = new FileReader("/root/ShareMAD/Refuge_dahu.json");
        	        	BufferedReader br = new BufferedReader(fr);
        	        	String temp = br.readLine();
        	        	while(temp != null){
        	        		result += temp;
        	        		temp = br.readLine();
        	        	}
        	        }catch(Exception e){
        	        	e.printStackTrace();
        	        }
        	        
        	        String gg = result;
        	        int index = gg.indexOf("document");
        	        String doc_uuid = gg.substring(index+11, index+47);
        	        
        			
        			long time = System.currentTimeMillis();
        			
        			String newpath = "/root/ShareMAD/" + System.currentTimeMillis() + "--Refuge_dahu.json";
            	    
            	    copyFile("/root/ShareMAD/Refuge_dahu.json", newpath);
            	    
            	    DataObject madObj = null;
        			
        			try {
        				madObj = new DataObject();
        				madObj = new DataObject(newpath);
        				madObj.addAttribute("JSON", "Refuge_dahu", 1);
        			} catch (DataObjectException e2) {
        				// TODO Auto-generated catch block
        				e2.printStackTrace();
        			}
        			
        			if(madObj!=null)
        				h.publishDataObject(madObj);
        			
        			json_modifiedTime = new_json_modifiedTime;
        			
        			DataObject infoObj = new DataObject();
        			
        			try{
						FileWriter fw = new FileWriter("/root/ShareMAD/" + "info_" + time + "--Refuge_dahu.json" , false);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write("http://PETLab.nctu.edu.tw/");
						bw.newLine();
						bw.write("Ambor");
						bw.newLine();
						bw.write(getLinuxMACAddress());
						bw.newLine();
						bw.write("Tax ID");
						bw.newLine();
						bw.write("C124365879");
						bw.newLine();
						bw.write("POS-1");
						bw.newLine();
						bw.write(doc_uuid);
						bw.newLine();
						bw.write("Mac address");
						bw.newLine();
						bw.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        			
        			infoObj = new DataObject("/root/ShareMAD/" + "info_" + time + "--Refuge_dahu.json");
        			infoObj.addAttribute("JSON", "Refuge_dahu", 1);
        			
        			if(infoObj!=null)
        				h.publishDataObject(infoObj);
        			
        		}
        		
        		tagList = filePath.listFiles(tagfilter);
                int new_tag_count = tagList.length;
                if(new_tag_count>tag_count){
                	System.out.println("get tag!!!");
                	tag_count = new_tag_count;
                	long newestfiletime = 0;
                	int newestfileindex = -1;
                	for(int i=0;i<tagList.length;i++){
                		File tag = tagList[i];
                		long filetime = new Date(tag.lastModified()).getTime();
                		if(filetime>newestfiletime){
                			newestfiletime = filetime;
                			newestfileindex = i;
                		}
                	}
                	if(newestfileindex != -1){
                		String filepath = tagList[newestfileindex].getPath();
                		FileReader fr = new FileReader(filepath);
        				BufferedReader br = new BufferedReader(fr);
        				
        				String triplet = null;
        				System.out.println("insert");
        				while ((triplet = br.readLine()) != null) {
        					String str2 = "insert into graph <http://pet.cs.nctu.edu.tw/trace> { "+triplet+" }";
	                		VirtuosoUpdateRequest vur2 = VirtuosoUpdateFactory.create(str2, set);
	                		vur2.exec();
        				}
                		
                	}
                }
        		
        	}

        } catch (Handle.RegistrationFailedException e) {
            System.out.println("Could not get handle: " + e.getMessage());
            return;
        } catch (InterruptedException e) {
			h.unregister();
			h.eventLoopStop();
        } catch (Exception e) {
            System.out.println("Got run loop exception\n");
            return;
        }

        //h.dispose();
    }
        
    public static void main(String args[])
    {
	
		System.out.println("===== Haggle Start!!! =====");
		
		int num_dataobjects = 10;
	
		for (int i = 0; i < args.length; i++) {
			if (args[i].compareTo("-h") == 0) {
				System.out.println("usage:");
				System.out.println("\t -h : print this help");
				System.out.println("\t -n NUM_DATAOBJECTS : generate specified number of data objects");
				System.out.println("");
				return;
			} else if (args[i].compareTo("-n") == 0 && (i + 1) != args.length) {
				try {
					num_dataobjects = Integer.parseInt(args[++i]);
					System.out.println("Going to generate " + num_dataobjects + " data objects");
				} catch (NumberFormatException e) {
					System.err.println("Bad number format in argument");
				}
			}
		}
		
        TestApp app = new TestApp("ShareMAD", num_dataobjects);


        app.start();


        System.out.println("Done...\n");
    }
        
    public void copyFile(String oldPath) {
		try {
			File file = new File(oldPath);
			
			String newPath = "/var/www/IS/temp.rdf";
			
			//Log.d("temp", "copy to TIBS: " + newPath);
			
			File file2 = new File(newPath);
			
			if(!file2.exists())
			{
			    try {
			        file2.createNewFile();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }
			}
			
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { //檔存在時
			InputStream inStream = new FileInputStream(oldPath);//讀入原檔
			FileOutputStream fs = new FileOutputStream(newPath);
			byte[] buffer = new byte[1444];
			int length;
			while ( (byteread = inStream.read(buffer)) != -1) {
				bytesum += byteread; //位元組數 檔案大小
				//Log.d("temp", "" + bytesum);
				fs.write(buffer, 0, byteread);
			}
				inStream.close();
				fs.close();
			}
		}
		catch(Exception e) {
			//Log.d("temp", "複製單個檔操作出錯");
			e.printStackTrace();
		}
	}
        
    public void copyFile(String srFile, String dtFile){
    	try{
    		File f1 = new File(srFile);
    		File f2 = new File(dtFile);
    		InputStream in = new FileInputStream(f1);
    		OutputStream out = new FileOutputStream(f2);
    		byte[] buf = new byte[1024];
    		int len;
    		while ((len = in.read(buf)) > 0){
    			out.write(buf, 0, len);
    		}
    		in.close();
    		out.close();
    	}
    	catch(FileNotFoundException ex){
    		ex.printStackTrace();  
    	}
    	catch(IOException e){
    		e.printStackTrace();    
    	}
    }
        
    public static String getLinuxMACAddress() { 
        String address = ""; 
        try { 
         ProcessBuilder pb = new ProcessBuilder("ifconfig", "-a"); 
         Process p = pb.start(); 
         BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream())); 
         String line; 
         while ((line = br.readLine()) != null) { 
            System.out.println(line); 
            if (line.indexOf("Link encap:Ethernet  HWaddr") != -1) { 
             int index = line.indexOf("HWaddr"); 
             address = line.substring(index + 7); 
             break; 
            } 
         } 
         br.close(); 
         return address.trim(); 
        } catch (IOException e) { 
        } 
        System.out.println(address);
        return address; 
    }
}
