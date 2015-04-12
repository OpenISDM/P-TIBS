package nctu.petlab.tibs.ws.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;
import nctu.petlab.tibs.ws.bean.MadInfo;
import nctu.petlab.tibs.ws.bean.PersonInfo;

import com.google.gson.Gson;
import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

@Path("sub/")
public class Sample {
	
	public ArrayList<MadInfo> dataList = null;
	
	final int datalist_size = 300;
	
	final double pos_longitude = 121.535;
	final double pos_latitude = 25.0178;
	
	int mErrCode = 0;
	String mErrMsg = "";
	
	static String url = "jdbc:virtuoso://localhost:1111/";
	
	@Context private UriInfo uriInfo;
	@Context private javax.servlet.http.HttpServletRequest hsr;
	
	
	@GET
	@Path("callback")
	public Response verify(@QueryParam("hub.challenge") String challenge) throws IOException{
		
		System.out.println("hub.challenge = "+challenge);
		
		ResponseBuilder builder = Response.ok(challenge);
		
		return builder.build();
	}
	
	@POST
	@Path("callback")
	public void receiver(String inform) throws IOException{
		
		System.out.println("inform = "+inform);
		
		if(inform.indexOf("臺灣大學")!=-1){
			System.out.println("Get Refuge_ntu.rdf!!");
			
			byte[] b = inform.getBytes();
			
			File file3 = new File("/var/www/IS/Refuge_ntu.rdf");
			
			if(!file3.exists()){
				file3.createNewFile();
			}
			
			FileOutputStream fileOutputStream = new FileOutputStream(new File("/var/www/IS/Refuge_ntu.rdf"));
			fileOutputStream.write(b);
			
			System.out.println("GET IS data OK!!");
			
			System.out.println("Link to virtuoso");
			
			VirtGraph set = new VirtGraph (url, "dba", "pet97x2z");
			
			Model model = ModelFactory.createDefaultModel();
			
			model.read(new ByteArrayInputStream(inform.getBytes()), null);
			
			StringWriter out = new StringWriter();
			model.write(out, "N-TRIPLE");
			
			File file2 = new File("/root/Temp/temp.txt");
			
			if(file2.createNewFile())
				System.out.println("create temp file");
			
			FileWriter fw = new FileWriter("/root/Temp/temp.txt");
			
			fw.write(out.toString());
			
			fw.flush();
			
			fw.close();
			
			System.out.println("Insert to local virtuoso");
			
			String strcl = "CLEAR GRAPH <http://pet.cs.nctu.edu.tw/refuge_ntu>";
	        VirtuosoUpdateRequest vurcl = VirtuosoUpdateFactory.create(strcl, set);
	        vurcl.exec(); 
			
			FileReader fr = new FileReader("/root/Temp/temp.txt");
			
			BufferedReader br = new BufferedReader(fr);
			String strNum = br.readLine();
			
			while ((strNum=br.readLine())!=null){
				  
				String str = "insert into graph <http://pet.cs.nctu.edu.tw/refuge_ntu> { "+ strNum +" }";
				System.out.println(str);
				
				VirtuosoUpdateRequest vur2 = VirtuosoUpdateFactory.create(str, set);
				vur2.exec();
				
			}
			
			String pre_property = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#";
			
			StmtIterator iter = model.listStatements();
			
			dataList = new ArrayList<MadInfo>();
			
			for(int p=0;p<datalist_size;p++){
				MadInfo dd = new MadInfo();
				dataList.add(dd);
			}
			
			String doc_uuid = null;
			
			int i = 0;
			while (iter.hasNext()) {
			    Statement stmt = iter.nextStatement();// get next statement
			    Property predicate = stmt.getPredicate();
			    
			    System.out.println("===== " + i + " =====");
			    i++;
			    System.out.println(stmt.getSubject().toString());
			    System.out.println(stmt.getPredicate().toString());
			    System.out.println(stmt.getObject());
			    
			    if(stmt.getSubject().toString().startsWith(pre_property + "Document_")){
			    	doc_uuid = "" + stmt.getObject();
			    }
			    
			    if(stmt.getSubject().toString().startsWith("http://140.109.21.188/facilities#store")){
	        		int index = Integer.parseInt(stmt.getSubject().toString().substring("http://140.109.21.188/facilities#store".length()));
	        		
	        		String tmp = stmt.getPredicate().toString();
	        		
	        		if(tmp.compareTo("gr:name") == 0){
	        			MadInfo dd = dataList.get(index);
	        			dd.setName(stmt.getObject().toString());
	        			dataList.set(index, dd);
	        		}else if(tmp.compareTo("gr:telephone") == 0){
	        			MadInfo dd = dataList.get(index);
	        			dd.setPhone(stmt.getObject().toString());
	        			dataList.set(index, dd);
	        		}else if(tmp.compareTo("gr:longitude") == 0){
	        			MadInfo dd = dataList.get(index);
	        			dd.setLon(Double.parseDouble(stmt.getObject().toString()));
	        			dataList.set(index, dd);
	        		}else if(tmp.compareTo("gr:latitude") == 0){
	        			MadInfo dd = dataList.get(index);
	        			dd.setLat(Double.parseDouble(stmt.getObject().toString()));
	        			dataList.set(index, dd);
	        		}else if(tmp.compareTo("gr:address") == 0){
	        			MadInfo dd = dataList.get(index);
	        			dd.setAddr(stmt.getObject().toString());
	        			dataList.set(index, dd);
	        		}
	        		
	        	}
	    
			}
			
			System.out.println(dataList.size());
			
			for(int g=0;g<dataList.size();g++){
				System.out.println(dataList.get(g).getAddr());
			}
			
			iter = model.listStatements();
			while (iter.hasNext()) {
			    Statement stmt = iter.nextStatement();// get next statement
			    
			    if(stmt.getPredicate().toString().compareTo("gr:streetAddress") == 0){
	        		int addr_index = 0;
	        		while(addr_index < datalist_size){
	        			if(stmt.getSubject().toString().compareTo(dataList.get(addr_index).getAddr())==0){
	        				MadInfo dd = dataList.get(addr_index);
	            			dd.setStr(stmt.getObject().toString());
	            			dataList.set(addr_index, dd);
	            			break;
	        			}
	        			addr_index++;
	        		}
	        		addr_index = 0;
	        	}	
	    
			}
			
			Collections.sort(dataList, new Comparator<MadInfo>(){
				 @Override
				 public int compare(MadInfo o1, MadInfo o2) {
					 double lon1 = o1.getLon();
					 double lat1 = o1.getLat();
					 double lon2 = o2.getLon();
					 double lat2 = o2.getLat();
				  return (int)(((Math.pow(lon1-pos_longitude, 2) + Math.pow(lat1-pos_latitude, 2)) - (Math.pow(lon2-pos_longitude, 2) + Math.pow(lat2-pos_latitude, 2)))*1000000);
				 }   
				});
		    
		    JSONArray array = new JSONArray();
		    
		    for(int j=0;j<10;j++){
				
				//單個使用者JSON物件
				JSONObject obj = new JSONObject();
				
				try{
					obj.put("name", dataList.get(j).getName());
					obj.put("phone", dataList.get(j).getPhone());
					obj.put("lon", dataList.get(j).getLon());
					obj.put("lat", dataList.get(j).getLat());
					obj.put("str_addr", dataList.get(j).getStr());
				} catch (Exception e) {}

				array.put(obj);
			}
		    
		    JSONObject obj = new JSONObject();
		    
		    try{
				obj.put("document", doc_uuid);
			} catch (Exception e) {}

			array.put(obj);
		    
		    try {
	        	
	        	File file = new File("/root/Share/");
	            if(!file.exists()){
	    				file.mkdir();
	            }
	        	
				FileWriter fw2 = new FileWriter("/root/Share/Refuge_ntu.json", false);
				BufferedWriter bw = new BufferedWriter(fw2);
				bw.write(array.toString());
				bw.newLine();
				bw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
		if(inform.indexOf("大湖派出所")!=-1){
			System.out.println("Get Refuge_dahu.rdf!!");
			
			byte[] b = inform.getBytes();
			
			File file3 = new File("/var/www/IS/Refuge_dahu.rdf");
			
			if(!file3.exists()){
				file3.createNewFile();
			}
			
			FileOutputStream fileOutputStream = new FileOutputStream(new File("/var/www/IS/Refuge_dahu.rdf"));
			fileOutputStream.write(b);
			
			System.out.println("GET IS data OK!!");
			
			System.out.println("Link to virtuoso");
			
			VirtGraph set = new VirtGraph (url, "dba", "pet97x2z");
			
			Model model = ModelFactory.createDefaultModel();
			
			model.read(new ByteArrayInputStream(inform.getBytes()), null);
			
			StringWriter out = new StringWriter();
			model.write(out, "N-TRIPLE");
			
			File file2 = new File("/root/Temp/temp.txt");
			
			if(file2.createNewFile())
				System.out.println("create temp file");
			
			FileWriter fw = new FileWriter("/root/Temp/temp.txt");
			
			fw.write(out.toString());
			
			fw.flush();
			
			fw.close();
			
			System.out.println("Insert to local virtuoso");
			
			String strcl = "CLEAR GRAPH <http://pet.cs.nctu.edu.tw/refuge_dahu>";
	        VirtuosoUpdateRequest vurcl = VirtuosoUpdateFactory.create(strcl, set);
	        vurcl.exec(); 
			
			FileReader fr = new FileReader("/root/Temp/temp.txt");
			
			BufferedReader br = new BufferedReader(fr);
			String strNum = br.readLine();
			
			while ((strNum=br.readLine())!=null){
				  
				String str = "insert into graph <http://pet.cs.nctu.edu.tw/refuge_dahu> { "+ strNum +" }";
				System.out.println(str);
				
				VirtuosoUpdateRequest vur2 = VirtuosoUpdateFactory.create(str, set);
				vur2.exec();
				
			}
			
			String pre_property = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#";
			
			StmtIterator iter = model.listStatements();
			
			dataList = new ArrayList<MadInfo>();
			
			for(int p=0;p<datalist_size;p++){
				MadInfo dd = new MadInfo();
				dataList.add(dd);
			}
			
			String doc_uuid = null;
			
			int i = 0;
			while (iter.hasNext()) {
			    Statement stmt = iter.nextStatement();// get next statement
			    Property predicate = stmt.getPredicate();
			    
			    System.out.println("===== " + i + " =====");
			    i++;
			    System.out.println(stmt.getSubject().toString());
			    System.out.println(stmt.getPredicate().toString());
			    System.out.println(stmt.getObject());
			    
			    if(stmt.getSubject().toString().startsWith(pre_property + "Document_")){
			    	doc_uuid = "" + stmt.getObject();
			    }
			    
			    if(stmt.getSubject().toString().startsWith("http://140.109.21.188/facilities#store")){
	        		int index = Integer.parseInt(stmt.getSubject().toString().substring("http://140.109.21.188/facilities#store".length()));
	        		
	        		String tmp = stmt.getPredicate().toString();
	        		
	        		if(tmp.compareTo("gr:name") == 0){
	        			MadInfo dd = dataList.get(index);
	        			dd.setName(stmt.getObject().toString());
	        			dataList.set(index, dd);
	        		}else if(tmp.compareTo("gr:telephone") == 0){
	        			MadInfo dd = dataList.get(index);
	        			dd.setPhone(stmt.getObject().toString());
	        			dataList.set(index, dd);
	        		}else if(tmp.compareTo("gr:longitude") == 0){
	        			MadInfo dd = dataList.get(index);
	        			dd.setLon(Double.parseDouble(stmt.getObject().toString()));
	        			dataList.set(index, dd);
	        		}else if(tmp.compareTo("gr:latitude") == 0){
	        			MadInfo dd = dataList.get(index);
	        			dd.setLat(Double.parseDouble(stmt.getObject().toString()));
	        			dataList.set(index, dd);
	        		}else if(tmp.compareTo("gr:address") == 0){
	        			MadInfo dd = dataList.get(index);
	        			dd.setAddr(stmt.getObject().toString());
	        			dataList.set(index, dd);
	        		}
	        		
	        	}
	    
			}
			
			System.out.println(dataList.size());
			
			for(int g=0;g<dataList.size();g++){
				System.out.println(dataList.get(g).getAddr());
			}
			
			iter = model.listStatements();
			while (iter.hasNext()) {
			    Statement stmt = iter.nextStatement();// get next statement
			    
			    if(stmt.getPredicate().toString().compareTo("gr:streetAddress") == 0){
	        		int addr_index = 0;
	        		while(addr_index < datalist_size){
	        			if(stmt.getSubject().toString().compareTo(dataList.get(addr_index).getAddr())==0){
	        				MadInfo dd = dataList.get(addr_index);
	            			dd.setStr(stmt.getObject().toString());
	            			dataList.set(addr_index, dd);
	            			break;
	        			}
	        			addr_index++;
	        		}
	        		addr_index = 0;
	        	}	
	    
			}
			
			Collections.sort(dataList, new Comparator<MadInfo>(){
				 @Override
				 public int compare(MadInfo o1, MadInfo o2) {
					 double lon1 = o1.getLon();
					 double lat1 = o1.getLat();
					 double lon2 = o2.getLon();
					 double lat2 = o2.getLat();
				  return (int)(((Math.pow(lon1-pos_longitude, 2) + Math.pow(lat1-pos_latitude, 2)) - (Math.pow(lon2-pos_longitude, 2) + Math.pow(lat2-pos_latitude, 2)))*1000000);
				 }   
				});
		    
		    JSONArray array = new JSONArray();
		    
		    for(int j=0;j<10;j++){
				
				//單個使用者JSON物件
				JSONObject obj = new JSONObject();
				
				try{
					obj.put("name", dataList.get(j).getName());
					obj.put("phone", dataList.get(j).getPhone());
					obj.put("lon", dataList.get(j).getLon());
					obj.put("lat", dataList.get(j).getLat());
					obj.put("str_addr", dataList.get(j).getStr());
				} catch (Exception e) {}

				array.put(obj);
			}
		    
		    JSONObject obj = new JSONObject();
		    
		    try{
				obj.put("document", doc_uuid);
			} catch (Exception e) {}

			array.put(obj);
		    
		    try {
	        	
	        	File file = new File("/root/Share/");
	            if(!file.exists()){
	    				file.mkdir();
	            }
	        	
				FileWriter fw2 = new FileWriter("/root/Share/Refuge_dahu.json", false);
				BufferedWriter bw = new BufferedWriter(fw2);
				bw.write(array.toString());
				bw.newLine();
				bw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
		
	}
	
	
	private void println(String string) {
		// TODO Auto-generated method stub
		
	}
	
	
	public static String toUtf8(String str) throws UnsupportedEncodingException {
		return new String(str.getBytes("UTF-8"),"UTF-8");
	}
}
