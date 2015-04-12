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

public class main implements EventHandler {	
    private Handle h = null;
    private String name;
	private long num_dataobjects_received = 0;
	private boolean should_quit = false;
	private int num_dataobjects = 10;
	
	//The Log File Path
	private String log_filepath = "/root/haggle_log.txt";
	
	static String url = "jdbc:virtuoso://localhost:1111/";
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

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
        public main(String name, int num_dataobjects)
        {
                super();
                this.name = name;
		this.num_dataobjects = num_dataobjects;
        }
        public void start()
        {
        	
        	DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			//The file path receive from IS
            File dahu_rdf_file = new File("/var/www/IS/Refuge.rdf");
            File ntu_rdf_file = new File("/var/www/IS/Refuge.rdf");
			
			//The file Convert from RDF to json
            File dahu_json_file = new File("/root/Share/Refuge_dahu.json");
            File ntu_json_file = new File("/root/Share/Refuge_ntu.json");
        	
            long dahu_rdf_modifiedTime = dahu_rdf_file.lastModified();
            long ntu_rdf_modifiedTime = ntu_rdf_file.lastModified();
            long dahu_json_modifiedTime = dahu_json_file.lastModified();
            long ntu_json_modifiedTime = ntu_json_file.lastModified();
        	
            Attribute attr = new Attribute("RDF", "TAG", 1);
            Attribute attr2 = new Attribute("RDF", "PI_RDF", 1);
            
            System.out.println("Attribute " + attr.toString());
            
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
                h = new Handle(name);

                h.registerEventInterest(EVENT_NEW_DATAOBJECT, this);
                h.registerEventInterest(EVENT_NEIGHBOR_UPDATE, this);
                h.registerEventInterest(EVENT_INTEREST_LIST_UPDATE, this);
                h.registerEventInterest(EVENT_HAGGLE_SHUTDOWN, this);
                
                h.registerInterest(attr);
                h.registerInterest(attr2);
        
                h.eventLoopRunAsync(this);
                        
                System.out.println("Getting Application interests");

                h.getApplicationInterestsAsync();

                Thread.sleep(2000);
			
				System.out.println("Forcing Haggle to send node description");
		
				h.sendNodeDescription();
				
				attr.dispose();
				attr2.dispose();
				
				Thread.sleep(3000);

				
				FilenameFilter tagfilter = new FilenameFilter(){
	            	@Override
	            	public boolean accept(File dir, String filename){
	            		if(filename.startsWith("tag"))
	            			return true;
	            		return false;
	            	}
	            };
	            
	            FilenameFilter beaconfilter = new FilenameFilter(){
	            	@Override
	            	public boolean accept(File dir, String filename){
	            		if(filename.indexOf("--") == 13 && filename.lastIndexOf("Beacon.json")!= -1)
	            			return true;
	            		return false;
	            	}
	            };
				
				File filePath = new File("/home/root/.Haggle");
				File beaconFilePath = new File("/root/Share");
	            File[] tagList = filePath.listFiles(tagfilter);
	            File[] beaconList = beaconFilePath.listFiles(beaconfilter);
	            int tag_count = tagList.length;
	            int beacon_count = beaconList.length;
	            
	            VirtGraph set = new VirtGraph (url, "dba", "pet97x2z");
	            
	            FileWriter log_fw = new FileWriter(log_filepath);
				BufferedWriter log_bw = new BufferedWriter(log_fw);
				
				while(true){
					
					Thread.sleep(1000);
					
					dahu_rdf_file = new File("/var/www/IS/Refuge_dahu.rdf");
					ntu_rdf_file = new File("/var/www/IS/Refuge_ntu.rdf");
					dahu_json_file = new File("/root/Share/Refuge_dahu.json");
					ntu_json_file = new File("/root/Share/Refuge_ntu.json");
					
					beaconList = beaconFilePath.listFiles(beaconfilter);
					int new_beacon_count = beaconList.length;
	        		
	        		long new_dahu_rdf_modifiedTime = dahu_rdf_file.lastModified();
	        		long new_ntu_rdf_modifiedTime = ntu_rdf_file.lastModified();
	        		long new_dahu_json_modifiedTime = dahu_json_file.lastModified();
	        		long new_ntu_json_modifiedTime = ntu_json_file.lastModified();		
	        		
	        		if(new_dahu_rdf_modifiedTime > dahu_rdf_modifiedTime){
	        			
	        			File last_dahu_version_file = new File("/root/version_dahu.txt");
	        			
	        			String last_dahu_version = null;
	        			
	        			if(last_dahu_version_file.exists()){
	        			
		        			try {
		        				FileReader fr = new FileReader(last_dahu_version_file);
		        				BufferedReader br = new BufferedReader(fr);
		        				last_dahu_version = br.readLine();
		        				br.close();
		        				fr.close();
		        				
		        			} catch (FileNotFoundException e) {
		        				// TODO Auto-generated catch block
		        				e.printStackTrace();
		        			} catch (IOException e) {
		        				// TODO Auto-generated catch block
		        				e.printStackTrace();
		        			}
		        			
	        			}else{
	        				last_dahu_version = "00.00.00.00";
	        			}
	        			
	        			
	        			
	        			String pre_property = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#";
	        			
	        			Model model = ModelFactory.createDefaultModel();
            			
            			InputStream in = FileManager.get().open("/var/www/IS/Refuge_dahu.rdf");
            			if (in == null) {
            			    throw new IllegalArgumentException(
            			                                 "File: " + "/var/www/IS/Refuge_dahu.rdf" + " not found");
            			}
            			 
            			model.read(in, null);
            			
            			StmtIterator iter = model.listStatements();
            			
            			int[] version = new int[4];
            			int[] old_version = new int[4];
            			
            			String new_version = null;
            			
            			boolean update = false;
            			
            			while (iter.hasNext()) {
                		    Statement stmt = iter.nextStatement();// get next statement
                		    Property predicate = stmt.getPredicate();
                		    
                		    if(stmt.getSubject().toString().startsWith(pre_property + "Version_")){
                		    	new_version = "" + stmt.getObject();
                		    	String[] vv = new_version.split("\\.");
                		    	for(int i=0;i<vv.length;i++){
                		    		version[i] = Integer.parseInt(vv[i]);
                		    	}
         
        	    		    	
        						String[] vvo = last_dahu_version.split("\\.");
        						for(int i=0;i<vv.length;i++){
        	    		    		old_version[i] = Integer.parseInt(vvo[i]);
        	    		    	}
                		    }
                		    
            			}
            			
            			System.out.println("old: " + last_dahu_version);
            			System.out.println("new: " + new_version);
            			
            			for(int i=0;i<4;i++){
        		    		if(version[i]>old_version[i]){
        		    			update = true;
        		    			
        		    			try {
        							FileWriter fw = new FileWriter("/root/version_dahu.txt", false);
        							BufferedWriter bw = new BufferedWriter(fw);
        							bw.write(new_version);
        							bw.flush();
        							fw.flush();
        							bw.close();
        							fw.close();
        						} catch (IOException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
        		    			
        		    			break;
        		    		}
        		    	}
	        			
            			
            			if(update){
            				//push rdf to another pi
		        			String newpath = "/root/Share/" + System.currentTimeMillis() + "--Refuge_dahu.rdf";
		        			
		        			copyFile("/var/www/IS/Refuge_dahu.rdf", newpath);
		        			
		        			DataObject piObj;
		        				
		    				piObj = new DataObject();
		    				piObj = new DataObject(newpath);
		    				piObj.addAttribute("RDF", "PI_RDF", 1);
		    				
		    				//copyFile("/var/www/IS/Refuge.rdf");
		    				
		    				h.publishDataObject(piObj);
		    				log_bw.write(ANSI_GREEN + "Receive Refuge_dahu.rdf" + ANSI_RESET);
		    				log_bw.newLine();
		    				log_bw.write(ANSI_YELLOW + "Convert Refuge_dahu.rdf --> Refuge_dahu.json" + ANSI_RESET);
		    				log_bw.newLine();
		    				log_bw.write(ANSI_RED + "Publish Refuge_dahu.json" + ANSI_RESET);
		    				log_bw.newLine();
		    				log_bw.newLine();
		    				
		    				//new File("/var/www/IS/temp.rdf").renameTo(new File("/var/www/IS/Refuge.rdf"));
            			}
	    				
	    				dahu_rdf_modifiedTime = new_dahu_rdf_modifiedTime;
	        			
	        		}else if(new_ntu_rdf_modifiedTime > ntu_rdf_modifiedTime){
	        			
	        			File last_ntu_version_file = new File("/root/version_ntu.txt");
	        			
	        			String last_ntu_version = null;
	        			
	        			if(last_ntu_version_file.exists()){
	        			
		        			try {
		        				FileReader fr = new FileReader(last_ntu_version_file);
		        				BufferedReader br = new BufferedReader(fr);
		        				last_ntu_version = br.readLine();
		        				br.close();
		        				fr.close();
		        				
		        			} catch (FileNotFoundException e) {
		        				// TODO Auto-generated catch block
		        				e.printStackTrace();
		        			} catch (IOException e) {
		        				// TODO Auto-generated catch block
		        				e.printStackTrace();
		        			}
		        			
	        			}else{
	        				last_ntu_version = "00.00.00.00";
	        			}
	        			
	        			
	        			
	        			String pre_property = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#";
	        			
	        			Model model = ModelFactory.createDefaultModel();
            			
            			InputStream in = FileManager.get().open("/var/www/IS/Refuge_ntu.rdf");
            			if (in == null) {
            			    throw new IllegalArgumentException(
            			                                 "File: " + "/var/www/IS/Refuge_ntu.rdf" + " not found");
            			}
            			 
            			model.read(in, null);
            			
            			StmtIterator iter = model.listStatements();
            			
            			int[] version = new int[4];
            			int[] old_version = new int[4];
            			
            			String new_version = null;
            			
            			boolean update = false;
            			
            			while (iter.hasNext()) {
                		    Statement stmt = iter.nextStatement();// get next statement
                		    Property predicate = stmt.getPredicate();
                		    
                		    if(stmt.getSubject().toString().startsWith(pre_property + "Version_")){
                		    	new_version = "" + stmt.getObject();
                		    	String[] vv = new_version.split("\\.");
                		    	for(int i=0;i<vv.length;i++){
                		    		version[i] = Integer.parseInt(vv[i]);
                		    	}
         
        	    		    	
        						String[] vvo = last_ntu_version.split("\\.");
        						for(int i=0;i<vv.length;i++){
        	    		    		old_version[i] = Integer.parseInt(vvo[i]);
        	    		    	}
                		    }
                		    
            			}
            			
            			System.out.println("old: " + last_ntu_version);
            			System.out.println("new: " + new_version);
            			
            			for(int i=0;i<4;i++){
        		    		if(version[i]>old_version[i]){
        		    			update = true;
        		    			
        		    			try {
        							FileWriter fw = new FileWriter("/root/version_ntu.txt", false);
        							BufferedWriter bw = new BufferedWriter(fw);
        							bw.write(new_version);
        							bw.flush();
        							fw.flush();
        							bw.close();
        							fw.close();
        						} catch (IOException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
        		    			
        		    			break;
        		    		}
        		    	}
	        			
	        			
	        			
	        			
            			if(update){
	        			
		        			//push rdf to another pi
		        			
		        			String newpath = "/root/Share/" + System.currentTimeMillis() + "--Refuge_ntu.rdf";
		        			
		        			copyFile("/var/www/IS/Refuge_ntu.rdf", newpath);
		        			
		        			DataObject piObj;
		        				
		    				piObj = new DataObject();
		    				piObj = new DataObject(newpath);
		    				piObj.addAttribute("RDF", "PI_RDF", 1);
		    				
		    				//copyFile("/var/www/IS/Refuge.rdf");
		    				
		    				h.publishDataObject(piObj);
		    				log_bw.write(ANSI_GREEN + "Receive Refuge_ntu.rdf" + ANSI_RESET);
		    				log_bw.newLine();
		    				log_bw.write(ANSI_YELLOW + "Convert Refuge_ntu.rdf --> Refuge_ntu.json" + ANSI_RESET);
		    				log_bw.newLine();
		    				log_bw.write(ANSI_RED + "Publish Refuge_ntu.json" + ANSI_RESET);
		    				log_bw.newLine();
		    				log_bw.newLine();
		    				
		    				//new File("/var/www/IS/temp.rdf").renameTo(new File("/var/www/IS/Refuge.rdf"));
		    				
            			}
	    				
	    				ntu_rdf_modifiedTime = new_ntu_rdf_modifiedTime;
	        			
	        		}
	        		
	        		tagList = filePath.listFiles(tagfilter);
	                int new_tag_count = tagList.length;
	                if(new_tag_count>tag_count){
	                	int get_tag_count = new_tag_count - tag_count;
	                	System.out.println("get " + get_tag_count + " tag!!!");
	                	tag_count = new_tag_count;
	                	long newestfiletime = 0;
	                	int newestfileindex = -1;
	                	
	                	ArrayList<Integer> had_add_index = new ArrayList<Integer>();
	                	
	                	for(int j=0;j<get_tag_count;j++){
		                	for(int i=0;i<tagList.length;i++){
		                		if(!had_add_index.contains(i)){
			                		File tag = tagList[i];
			                		long filetime = new Date(tag.lastModified()).getTime();
			                		if(filetime>newestfiletime){
			                			newestfiletime = filetime;
			                			newestfileindex = i;
			                		}
		                		}
		                	}
		                	had_add_index.add(newestfileindex);
		                	newestfiletime = 0;
		                	newestfileindex = -1;
	                	}
	                	
	                	if(!had_add_index.isEmpty()){
	                		
	                		for(int i=0;i<had_add_index.size();i++){
	                		
		                		String filepath = tagList[had_add_index.get(i)].getPath();
		                		String filename = tagList[had_add_index.get(i)].getName();
		                		System.out.println("Get Tag: " + filename);
		                		/*log_bw.write("Get Tag: " + filename);
			    				log_bw.newLine();*/
			    				
		                		FileReader fr = new FileReader(filepath);
		                		//System.out.println("into buffer " + filename);
	            				BufferedReader br = new BufferedReader(fr);
	            				
	            				String triplet = null;
	            				System.out.println("insert " + filename);
	            				
	            				FileOutputStream fos = null;
	            				try {
	            					fos = new FileOutputStream("/root/recv_tag/" + filename);
	            				} catch (FileNotFoundException e) {
	            					// TODO Auto-generated catch block
	            					e.printStackTrace();
	            				}
	            				
	            			    Model model = ModelFactory.createDefaultModel();
	            			    InputStream is = FileManager.get().open(filepath);
	            			    if (is != null) {
	            			        model.read(is, null, "N-TRIPLE");
	            			        model.write(fos);
	            			    } else {
	            			        System.err.println("cannot read " + filepath);
	            			    }
	            			    
	            			    is.close();
	            			    fos.flush();
	            			    fos.close();
	            				
	            			    int tag_combine_count = 0;
	            			    
	            				while ((triplet = br.readLine()) != null) {
	            					String str2 = "insert into graph <http://pet.cs.nctu.edu.tw/trace> { "+triplet+" }";
	            					if(triplet.contains("TransportSession"))
	            						tag_combine_count++;
	            					//System.out.println(" " + str2);
	    	                		VirtuosoUpdateRequest vur2 = VirtuosoUpdateFactory.create(str2, set);
	    	                		//System.out.println(str2);
	    	                		vur2.exec();
	            				}
	            				
	            				/*log_bw.write("tag_combine_count: " + tag_combine_count);
			    				log_bw.newLine();*/
	            				
	            				System.out.println("tag_combine_count: " + tag_combine_count);
	            				
	            				if(tag_combine_count>=24){
	            					log_bw.write(ANSI_GREEN + "Receive Tag: " + filename + ANSI_RESET);
				    				log_bw.newLine();
				    				log_bw.write(ANSI_YELLOW + "Insert " + filename  + "to Virtuoso DataBase" + ANSI_RESET);
				    				log_bw.newLine();
				    				log_bw.newLine();
	            				}
	            				
	            				System.out.println("insert " + filename + "  done.");
	            				
	            				
	            				fr.close();
	            				br.close();
            				
	                		}
	                		
	                	}
	                }
	                
	                log_bw.flush();
					log_fw.flush();
	        		
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
			
            main app = new main("Share", num_dataobjects);


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
