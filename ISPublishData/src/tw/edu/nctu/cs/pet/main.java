package tw.edu.nctu.cs.pet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.UUID;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

enum SensitivityLevel {
    PRIVATE, PUBLIC, SENSITIVE 
}

public class main {
	
	static String url = "jdbc:virtuoso://localhost:1111/";
	
	public static void main(String[] args) {
		
		Model model = ModelFactory.createDefaultModel();
		
		String pre_property = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#";
		String pre_property_person = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Person_";
		String pre_property_device = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Device_";
		String pre_property_domain = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Domain_";
		
		Property has_uuid = model.createProperty(pre_property + "hasUUID");
		Property time = model.createProperty(pre_property + "hasOccurAt");
		
		Property has_title = model.createProperty(pre_property + "hasTitle");
		Property sensitivity_level = model.createProperty(pre_property + "hasSensitivityLevel");
		Property has_version = model.createProperty(pre_property + "hasVersion");
		
		Property has_device_id_type = model.createProperty(pre_property + "hasDeviceIDtype");
		Property has_id = model.createProperty(pre_property + "hasID");
		Property has_alias = model.createProperty(pre_property + "hasAlias");
		
		Property has_domain_name = model.createProperty(pre_property + "hasDomainName");
		
		Property has_person_id_type = model.createProperty(pre_property + "hasPersonIDtype");
		Property has_legal_name = model.createProperty(pre_property + "hasLegalName");
		
		Property doc = model.createProperty(pre_property + "isTransportSessionOf");
		Property doc_re = model.createProperty(pre_property + "hasTransportSession");
		Property from = model.createProperty(pre_property + "hasSource");
		Property to = model.createProperty(pre_property + "hasDestination");

		Property belong_to = model.createProperty(pre_property + "belongsTo");
		Property has_owner = model.createProperty(pre_property + "ownedBy");
		Property write_by = model.createProperty(pre_property + "hasAuthor");
		
		String prefix = "http://PETLab.nctu.edu.tw/";
		String person = "Jack";	//The document author
		
		
		
		String hub = "http://localhost:8080/push/ws/hub";
		
		while(true){
		
			URL ul = null;
			int pub_doc_choice_num = 0;
			String pub_filepath = null;
			String topic = null;
			String title = null;
			String senLv = null;
			String version = null;
			
			SimpleDateFormat nowdate = new java.text.SimpleDateFormat("MM.dd.HH.mm");
			version = nowdate.format(new java.util.Date());
			
			File last_ntu_version_file = new File("/root/version_ntu.txt");
			File last_dahu_version_file = new File("/root/version_dahu.txt");
			
			String last_ntu_version = null;
			String last_dahu_version = null;
			
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
			
			System.out.println("Last Refuge_ntu version: " + last_ntu_version);
			System.out.println("Last Refuge_dahu version: " + last_dahu_version);
			
			int new_version[] = new int[4];
			int old_version[] = new int[4];
			
			String callback = null;
			Scanner input = new Scanner(System.in);
			System.out.println("====Publisher====");
			System.out.println("[1]Refuge_ntu [2]Refuge_dahu [other]Exit");
			System.out.print("Input your select number: ");
			pub_doc_choice_num = input.nextInt();
			System.out.println("Publish file path: ");
			if(pub_doc_choice_num==1){
			
				//The File have to publish
				pub_filepath = "/root/Refuge_ntu.rdf";
				topic = "http://localhost/IS/Refuge_ntu.rdf";
				title = "Refuge_ntu";
				senLv = "PUBLIC";
				
				//File last_ntu_version_file = new File("/root/version_ntu.txt");
				if(!last_ntu_version_file.exists()){
					try {
						last_ntu_version_file.createNewFile();
						FileWriter fw = new FileWriter("/root/version_ntu.txt", false);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(version);
						bw.flush();
						fw.flush();
						bw.close();
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}else{
					
					String[] vv = version.split("\\.");
    		    	for(int i=0;i<vv.length;i++){
    		    		new_version[i] = Integer.parseInt(vv[i]);
    		    	}
    		    	
					String[] vvo = last_ntu_version.split("\\.");
					for(int i=0;i<vv.length;i++){
    		    		old_version[i] = Integer.parseInt(vvo[i]);
    		    	}
						
    	
    		    	
    		    	boolean use_new = true;
    		    	
    		    	for(int i=0;i<4;i++){
    		    		if(old_version[i]>new_version[i]){
    		    			use_new = false;
    		    			break;
    		    		}
    		    	}
    		    	
    		    	if(!use_new){
    		    		version = old_version[0] + "." + old_version[1] + "." + old_version[2] + "." + (old_version[3]+1);
    		    	}
    		    	
    		    	//write the newest version into file
    		    	try {
						FileWriter fw = new FileWriter("/root/version_ntu.txt", false);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(version);
						bw.flush();
						fw.flush();
						bw.close();
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    		    	
				}
				
			}else if(pub_doc_choice_num==2){
			
				//The File have to publish
				pub_filepath = "/root/Refuge_dahu.rdf";
				topic = "http://localhost/IS/Refuge_dahu.rdf";
				title = "Refuge_dahu";
				senLv = "PUBLIC";
				
				//File last_dahu_version_file = new File("/root/version_dahu.txt");
				if(!last_dahu_version_file.exists()){
					try {
						last_dahu_version_file.createNewFile();
						FileWriter fw = new FileWriter("/root/version_dahu.txt", false);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(version);
						bw.flush();
						fw.flush();
						bw.close();
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}else{
					
					String[] vv = version.split("\\.");
    		    	for(int i=0;i<vv.length;i++){
    		    		new_version[i] = Integer.parseInt(vv[i]);
    		    	}
    		    	

					String[] vvo = last_dahu_version.split("\\.");
					for(int i=0;i<vv.length;i++){
    		    		old_version[i] = Integer.parseInt(vvo[i]);
    		    	}
    	
    		    	
    		    	boolean use_new = true;
    		    	
    		    	for(int i=0;i<4;i++){
    		    		//System.out.print(old_version[i] + ">" + new_version[i]);
    		    		if(old_version[i]>new_version[i]){
    		    			//System.out.print(old_version[i] + ">" + new_version[i]);
    		    			use_new = false;
    		    			break;
    		    		}
    		    	}
    		    	
    		    	if(!use_new){
    		    		version = old_version[0] + "." + old_version[1] + "." + old_version[2] + "." + (old_version[3]+1);
    		    	}
    		    	
    		    	//write the newest version into file
    		    	try {
						FileWriter fw = new FileWriter("/root/version_dahu.txt", false);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(version);
						bw.flush();
						fw.flush();
						bw.close();
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    		    	
				}
				
			}else{
				break;
			}
			System.out.println(pub_filepath);
			File file2 = new File(" " + pub_filepath);
			String filename = file2.getName();
			copyFile(pub_filepath, "/var/www/IS/" + filename);
			System.out.print("topic: ");
			System.out.println(" " + topic);
			System.out.print("title: ");
			System.out.println(" " + title);
			System.out.println("sensitivitylevel: " + senLv);
			System.out.print("version: ");
			System.out.println(" " + version);
			
			
			//Add version,doc_uuid information to document & insert all document information to local DB
			
			String uuid = "" + UUID.randomUUID();
			
			model.createResource(pre_property + "Document_" + uuid).addProperty(has_uuid, uuid)
																							 .addProperty(has_title, title)
																							 .addProperty(sensitivity_level, senLv)
																							 .addProperty(has_version, version)
																							 .addProperty(write_by, model.createResource(pre_property_person + prefix + person))
																							 .addProperty(belong_to, model.createResource(pre_property_domain + prefix));
			Resource person_from = model.createResource(pre_property_person + prefix + person);
			
			StringWriter out = new StringWriter();
			model.write(out, "N-TRIPLE");
			
			VirtGraph set = new VirtGraph (url, "dba", "pet97x2z");
			
			/*String strcl = "CLEAR GRAPH <http://pet.cs.nctu.edu.tw/trace>";
	        VirtuosoUpdateRequest vurcl = VirtuosoUpdateFactory.create(strcl, set);
	        vurcl.exec();*/
			
			String str = "insert into graph <http://pet.cs.nctu.edu.tw/trace> { " + out.toString() +" }";
			VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, set);
			vur.exec();
			
			//Add Document UUID into document
			String pubfilename = topic.substring(new String("http://localhost/IS/").length());
			String pubpath = "/var/www/IS/" + pubfilename;
			//String pubpath = "C:\\IS\\" + pubfilename;
			String newpubpath = "/var/www/IStemp/" + pubfilename;
			String newtopic = "http://localhost/IStemp/" + pubfilename;
			
			Model model2 = ModelFactory.createDefaultModel();
			 
			InputStream in = FileManager.get().open(pubpath);
			if (in == null) {
			    throw new IllegalArgumentException(
			                                 "File: " + pubpath + " not found");
			}
			 
			model2.read(in, null);
			
			model2.createResource(pre_property + "Document_" + uuid).addProperty(has_uuid, uuid);
			model2.createResource(pre_property + "Version_" + version).addProperty(has_version, version);
			
			File file = new File(newpubpath);
			
			if(!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file.getAbsolutePath());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			model2.write(fos);
			try {
				fos.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			
			
			
			//Pub the document to POS
			
			try {
				ul = new URL(hub + "/publish");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			URLConnection connection = null;
			try {
				connection = ul.openConnection();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			HttpURLConnection   uc   =   (HttpURLConnection)   connection;
			try {
				uc.setRequestMethod("POST");
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 uc.setRequestProperty ( "Connection", "Keep-Alive" ) ; 
		        uc.setRequestProperty ( "Cache-Control", "no-cache" ) ;    
	        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        uc.setDoOutput(true);  
	        uc.setDoInput(true); 
	        String xml = "topic=" + newtopic ;
	        byte[] bs = new String(xml).getBytes();    
	        try {
				uc.connect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}        
	        OutputStream om = null;
			try {
				om = uc.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				om.write(bs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				om.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				om.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        InputStream im = null;
			try {
				im = uc.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}       
	        try {
				im.read(bs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //System.out.println(new String(bs));        
	        try {
				im.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        uc.disconnect();
			
			System.out.println("Publish Done.");
			System.out.println("");
			
		}
	    
	}
	
	public static void copyFile(String srFile, String dtFile){
    	try{
    		File f1 = new File(srFile);
    		File f2 = new File(dtFile);
    		if(!f2.exists())
    			f2.createNewFile();
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
}
