package tw.edu.nctu.cs.pet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

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

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
 
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openrdf.sail.SailException;

import java.net.URL;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.GridLayout;

public class main {
	
	//To set the IS&POSs IP
	static String ISip = "192.168.1.12";
	static String POSurl1 = "192.168.1.11";
	static String POSurl2 = "192.168.1.11";
	
	static String ISurl = "jdbc:virtuoso://" + ISip + ":1111/";
	static String POSurl1 = "jdbc:virtuoso://" + POSurl1 + ":1111/";
	static String POSurl2 = "jdbc:virtuoso://" + POSurl2 + ":1111/";
	
	public static int rdf_count = 0;
	
	private JEditorPane htmlPane;
    private JTree tree;
    private URL helpURL;
    
    private static boolean DEBUG = false;
 
    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";
     
    //Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = false;
    
    private static String trace_doc = "";
    private static String trace_doc_id = "";
    private static ArrayList<TagInfo> tag_list = new ArrayList<TagInfo>(); 
    private static ArrayList<TagInfo> doc_tag_list = new ArrayList<TagInfo>();
    private static ArrayList<DomainInfo> domain_list = new ArrayList<DomainInfo>();
    private static ArrayList<PersonInfo> person_list = new ArrayList<PersonInfo>();
	
	public static void main(String[] args){
		
		System.out.println("[1]Make The Trace Graph to Gephi.");
		System.out.println("[2]Make The Trace Graph to Gephi(only transfer anthor domain).");
		System.out.println("[3]Clear Database.");
		System.out.print("Input your select: ");
		Scanner selscanner = new Scanner(System.in);
		int select = selscanner.nextInt();
		
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		Model model = ModelFactory.createDefaultModel();
		String pre_property = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#";
		String pre_property_person = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Person_";
		String pre_property_device = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Device_";
		String pre_property_domain = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Domain_";
		
		Property has_uuid = model.createProperty(pre_property + "hasUUID");
		Property time = model.createProperty(pre_property + "hasOccurAt");
		
		Property title = model.createProperty(pre_property + "hasTitle");
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
		
		VirtuosoQueryExecution vqe = null;
		Query sparql = null;
		VirtGraph setIS = new VirtGraph (ISurl, "dba", "pet97x2z");
		VirtGraph setPOS1 = new VirtGraph (POSurl1, "dba", "pet97x2z");
		VirtGraph setPOS2 = new VirtGraph (POSurl2, "dba", "pet97x2z");
		
		if(select == 3){
			String str56 = "CLEAR GRAPH <http://pet.cs.nctu.edu.tw/trace>";
	        VirtuosoUpdateRequest vur56 = VirtuosoUpdateFactory.create(str56, setPOS1);
	        vur56.exec();
		}	
		
		if(select ==1 || select ==2){
		
		//The ArrayList to save Tag information
		ArrayList<DocumentInfo> doc_list = new ArrayList<DocumentInfo>();
		person_list = new ArrayList<PersonInfo>();
		ArrayList<DeviceInfo> device_list = new ArrayList<DeviceInfo>();
		domain_list = new ArrayList<DomainInfo>();
		
		
		//Set DocumentInfo List
		String strselectTag = "prefix infoflow: <" + pre_property + "> select ?doc "
				+ " from <http://pet.cs.nctu.edu.tw/trace> where { ?doc infoflow:hasTransportSession ?o }";
		Query selectTag2 = QueryFactory.create(strselectTag);
		VirtuosoQueryExecution vqeTag2 = VirtuosoQueryExecutionFactory.create(selectTag2, setPOS2);
		ResultSet rsTag2 = vqeTag2.execSelect();
		VirtuosoQueryExecution vqeTag183 = VirtuosoQueryExecutionFactory.create(selectTag2, setPOS1);
		ResultSet rsTag183 = vqeTag183.execSelect();
		
		while(rsTag2.hasNext()) {
			
			QuerySolution qs2 = rsTag2.nextSolution();
			RDFNode rn = qs2.get("doc");
			String UUID = rn.toString();
			String subUUID = UUID.substring(new String(pre_property + "Document_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<doc_list.size();i++){
				
				//System.out.println(subUUID + " <-> " + doc_list.get(i).getDocumentId());
				
				if(doc_list.get(i).getDocumentId().compareTo(subUUID)==0){
					
					in_list = true;
					break;
				}
					
				//System.out.println(in_list);
				
			}
			
			if(!in_list){
			
				DocumentInfo tmp = new DocumentInfo();
				tmp.setDocumentId(UUID.substring(new String(pre_property + "Document_").length()));
				doc_list.add(tmp);
				
				//Set DocumentInfo Title
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?title "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:hasTitle ?title }";
				//System.out.println(strselectTag);
				Query selectTag3 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag3 = VirtuosoQueryExecutionFactory.create(selectTag3, setIS);
				ResultSet rsTag3 = vqeTag3.execSelect();
				
				while(rsTag3.hasNext()) {
					
					QuerySolution qs3 = rsTag3.nextSolution();
					RDFNode rn2 = qs3.get("title");
					String title_s = rn2.toString();
					//System.out.println(title_s);
					
					doc_list.get(doc_list.size()-1).setTitle(title_s);
					
					
				}
				
				//Set DocumentInfo SensitivityLevel
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?senLv "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:hasSensitivityLevel ?senLv }";
				//System.out.println(strselectTag);
				Query selectTag4 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag4 = VirtuosoQueryExecutionFactory.create(selectTag4, setIS);
				ResultSet rsTag4 = vqeTag4.execSelect();
				
				while(rsTag4.hasNext()) {
					
					QuerySolution qs = rsTag4.nextSolution();
					RDFNode rn2 = qs.get("senLv");
					String senLv_s = rn2.toString();
					//System.out.println(senLv_s);
					
					doc_list.get(doc_list.size()-1).setSenLv(senLv_s);
					
					
				}
				
				//Set DocumentInfo Version
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?version "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:hasVersion ?version }";
				//System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setIS);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("version");
					String version_s = rn2.toString();
					//System.out.println(version_s);
					
					doc_list.get(doc_list.size()-1).setVersion(version_s);
					
					
				}
				
				//Set DocumentInfo Domain
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?domain "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:belongsTo ?domain }";
				//System.out.println(strselectTag);
				Query selectTag6 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setIS);
				ResultSet rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("domain");
					String domain_s = rn2.toString().substring(new String(pre_property + "Domain_").length());
					System.out.println(domain_s);
					
					doc_list.get(doc_list.size()-1).setDomain(domain_s);
					
					
				}
				
				//Set DocumentInfo Author
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?author "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:hasAuthor ?author }";
				System.out.println(strselectTag);
				Query selectTag7 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setIS);
				ResultSet rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("author");
					String person_s = rn2.toString().substring(new String(pre_property + "Person_").length());
					System.out.println(person_s);
					
					doc_list.get(doc_list.size()-1).setAuthor(person_s);
					
					
				}
				
				//Set DocumentInfo TagList
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?tag "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:hasTransportSession ?tag }";
				System.out.println(strselectTag);
				Query selectTag8 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag8 = VirtuosoQueryExecutionFactory.create(selectTag8, setPOS2);
				ResultSet rsTag8 = vqeTag8.execSelect();
				vqeTag183 = VirtuosoQueryExecutionFactory.create(selectTag8, setPOS1);
				rsTag183 = vqeTag8.execSelect();
				
				while(rsTag8.hasNext()) {
					
					QuerySolution qs = rsTag8.nextSolution();
					RDFNode rn2 = qs.get("tag");
					String tag_s = rn2.toString().substring(new String(pre_property + "TransportSession_").length());
					System.out.println(tag_s);
					
					doc_list.get(doc_list.size()-1).addTag(tag_s);
					
					
				}
				
				while(rsTag183.hasNext()) {
					
					QuerySolution qs = rsTag183.nextSolution();
					RDFNode rn2 = qs.get("tag");
					String tag_s = rn2.toString().substring(new String(pre_property + "TransportSession_").length());
					System.out.println(tag_s);
					
					doc_list.get(doc_list.size()-1).addTag(tag_s);
					
					
				}
				
			}
			
			
		}
		
		vqeTag183 = VirtuosoQueryExecutionFactory.create(selectTag2, setPOS1);
		rsTag183 = vqeTag183.execSelect();
		
		
		while(rsTag183.hasNext()) {
			
			QuerySolution qs2 = rsTag183.nextSolution();
			RDFNode rn = qs2.get("doc");
			String UUID = rn.toString();
			String subUUID = UUID.substring(new String(pre_property + "Document_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<doc_list.size();i++){
				
				//System.out.println(subUUID + " <-> " + doc_list.get(i).getDocumentId());
				
				if(doc_list.get(i).getDocumentId().compareTo(subUUID)==0){
					
					in_list = true;
					break;
				}
					
				//System.out.println(in_list);
				
				
			}
			
			if(!in_list){
			
				DocumentInfo tmp = new DocumentInfo();
				tmp.setDocumentId(UUID.substring(new String(pre_property + "Document_").length()));
				doc_list.add(tmp);
				
				//Set DocumentInfo Title
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?title "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:hasTitle ?title }";
				//System.out.println(strselectTag);
				Query selectTag3 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag3 = VirtuosoQueryExecutionFactory.create(selectTag3, setIS);
				ResultSet rsTag3 = vqeTag3.execSelect();
				
				while(rsTag3.hasNext()) {
					
					QuerySolution qs3 = rsTag3.nextSolution();
					RDFNode rn2 = qs3.get("title");
					String title_s = rn2.toString();
					//System.out.println(title_s);
					
					doc_list.get(doc_list.size()-1).setTitle(title_s);
					
					
				}
				
				//Set DocumentInfo SensitivityLevel
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?senLv "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:hasSensitivityLevel ?senLv }";
				//System.out.println(strselectTag);
				Query selectTag4 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag4 = VirtuosoQueryExecutionFactory.create(selectTag4, setIS);
				ResultSet rsTag4 = vqeTag4.execSelect();
				
				while(rsTag4.hasNext()) {
					
					QuerySolution qs = rsTag4.nextSolution();
					RDFNode rn2 = qs.get("senLv");
					String senLv_s = rn2.toString();
					//System.out.println(senLv_s);
					
					doc_list.get(doc_list.size()-1).setSenLv(senLv_s);
					
					
				}
				
				//Set DocumentInfo Version
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?version "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:hasVersion ?version }";
				//System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setIS);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("version");
					String version_s = rn2.toString();
					//System.out.println(version_s);
					
					doc_list.get(doc_list.size()-1).setVersion(version_s);
					
					
				}
				
				//Set DocumentInfo Domain
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?domain "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:belongsTo ?domain }";
				//System.out.println(strselectTag);
				Query selectTag6 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setIS);
				ResultSet rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("domain");
					String domain_s = rn2.toString().substring(new String(pre_property + "Domain_").length());
					System.out.println(domain_s);
					
					doc_list.get(doc_list.size()-1).setDomain(domain_s);
					
					
				}
				
				//Set DocumentInfo Author
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?author "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:hasAuthor ?author }";
				System.out.println(strselectTag);
				Query selectTag7 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setIS);
				ResultSet rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("author");
					String person_s = rn2.toString().substring(new String(pre_property + "Person_").length());
					System.out.println(person_s);
					
					doc_list.get(doc_list.size()-1).setAuthor(person_s);
					
					
				}
				
				//Set DocumentInfo TagList
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?tag "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + UUID + "> infoflow:hasTransportSession ?tag }";
				System.out.println(strselectTag);
				Query selectTag8 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag8 = VirtuosoQueryExecutionFactory.create(selectTag8, setPOS1);
				ResultSet rsTag8 = vqeTag8.execSelect();
				
				while(rsTag8.hasNext()) {
					
					QuerySolution qs = rsTag8.nextSolution();
					RDFNode rn2 = qs.get("tag");
					String tag_s = rn2.toString().substring(new String(pre_property + "TransportSession_").length());
					
					System.out.println(tag_s);
					
					doc_list.get(doc_list.size()-1).addTag(tag_s);
					
					
				}
				
			}
			
			
		}
		
		
		
		//Set DeviceInfo List(from Source)
		strselectTag = "prefix infoflow: <" + pre_property + "> select ?device "
				+ " from <http://pet.cs.nctu.edu.tw/trace> where { ?s infoflow:hasSource ?device }";
		System.out.println(strselectTag);
		Query selectTag3 = QueryFactory.create(strselectTag);
		VirtuosoQueryExecution vqeTag3 = VirtuosoQueryExecutionFactory.create(selectTag3, setPOS2);
		ResultSet rsTag3 = vqeTag3.execSelect();
		
		vqeTag183 = VirtuosoQueryExecutionFactory.create(selectTag3, setPOS1);
		rsTag183 = vqeTag183.execSelect();
		
		while(rsTag3.hasNext()) {
			
			QuerySolution qs2 = rsTag3.nextSolution();
			RDFNode rn = qs2.get("device");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "Device_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<device_list.size();i++){
				
				if(device_list.get(i).getDeviceId().compareTo(subID)==0){
					
					in_list = true;
					break;
				}
					
				
			}
			
			if(!in_list){
			
				DeviceInfo tmp = new DeviceInfo();
				tmp.setDeviceId(id.substring(new String(pre_property + "Device_").length()));
				device_list.add(tmp);
				
				//Set DeviceInfo IDType
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id_type "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasDeviceIDtype ?id_type }";
				System.out.println(strselectTag);
				Query selectTag4 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag4 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS2);
				ResultSet rsTag4 = vqeTag4.execSelect();
				
				while(rsTag4.hasNext()) {
					
					QuerySolution qs = rsTag4.nextSolution();
					RDFNode rn2 = qs.get("id_type");
					String id_type_s = rn2.toString();
					System.out.println(id_type_s);
					
					device_list.get(device_list.size()-1).setIdType(id_type_s);
					
					
				}
				
				//Set DeviceInfo Name
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?name "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasAlias ?name }";
				System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS2);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("name");
					String name_s = rn2.toString();
					System.out.println(name_s);
					
					device_list.get(device_list.size()-1).setName(name_s);
					
					
				}
				
				//Set DeviceInfo Domain
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?domain "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:belongsTo ?domain }";
				System.out.println(strselectTag);
				Query selectTag6 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS2);
				ResultSet rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("domain");
					String domain_s = rn2.toString().substring(new String(pre_property + "Domain_").length());
					System.out.println(domain_s);
					
					device_list.get(device_list.size()-1).setDomain(domain_s);
					
					
				}
				
				//Set DeviceInfo Owner
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?owner "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:ownedBy ?owner }";
				System.out.println(strselectTag);
				Query selectTag7 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS2);
				ResultSet rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("owner");
					String person_s = rn2.toString().substring(new String(pre_property + "Person_").length());
					System.out.println(person_s);
					
					device_list.get(device_list.size()-1).setOwner(person_s);
					
					
				}
				
			}
			
		}
		
		
		
		while(rsTag183.hasNext()) {
			
			QuerySolution qs2 = rsTag183.nextSolution();
			RDFNode rn = qs2.get("device");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "Device_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<device_list.size();i++){
				
				if(device_list.get(i).getDeviceId().compareTo(subID)==0){
					
					in_list = true;
					break;
				}
					
				
			}
			
			if(!in_list){
			
				DeviceInfo tmp = new DeviceInfo();
				tmp.setDeviceId(id.substring(new String(pre_property + "Device_").length()));
				device_list.add(tmp);
				
				//Set DeviceInfo IDType
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id_type "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasDeviceIDtype ?id_type }";
				System.out.println(strselectTag);
				Query selectTag4 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag4 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS1);
				ResultSet rsTag4 = vqeTag4.execSelect();
				
				while(rsTag4.hasNext()) {
					
					QuerySolution qs = rsTag4.nextSolution();
					RDFNode rn2 = qs.get("id_type");
					String id_type_s = rn2.toString();
					System.out.println(id_type_s);
					
					device_list.get(device_list.size()-1).setIdType(id_type_s);
					
					
				}
				
				//Set DeviceInfo Name
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?name "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasAlias ?name }";
				System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS1);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("name");
					String name_s = rn2.toString();
					System.out.println(name_s);
					
					device_list.get(device_list.size()-1).setName(name_s);
					
					
				}
				
				//Set DeviceInfo Domain
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?domain "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:belongsTo ?domain }";
				System.out.println(strselectTag);
				Query selectTag6 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS1);
				ResultSet rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("domain");
					String domain_s = rn2.toString().substring(new String(pre_property + "Domain_").length());
					System.out.println(domain_s);
					
					device_list.get(device_list.size()-1).setDomain(domain_s);
					
					
				}
				
				//Set DeviceInfo Owner
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?owner "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:ownedBy ?owner }";
				System.out.println(strselectTag);
				Query selectTag7 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS1);
				ResultSet rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("owner");
					String person_s = rn2.toString().substring(new String(pre_property + "Person_").length());
					System.out.println(person_s);
					
					device_list.get(device_list.size()-1).setOwner(person_s);
					
					
				}
				
			}
			
		}
		
		
		
		
		
		
		
		//Set DeviceInfo List(from Destination)
		strselectTag = "prefix infoflow: <" + pre_property + "> select ?device "
				+ " from <http://pet.cs.nctu.edu.tw/trace> where { ?s infoflow:hasDestination ?device }";
		System.out.println(strselectTag);
		Query selectTag4 = QueryFactory.create(strselectTag);
		VirtuosoQueryExecution vqeTag4 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS2);
		ResultSet rsTag4 = vqeTag4.execSelect();
		vqeTag183 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS1);
		rsTag183 = vqeTag183.execSelect();
		
		while(rsTag4.hasNext()) {
			
			QuerySolution qs2 = rsTag4.nextSolution();
			RDFNode rn = qs2.get("device");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "Device_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<device_list.size();i++){
				
				if(device_list.get(i).getDeviceId().compareTo(subID)==0){
					
					in_list = true;
					break;
				}
					
				
			}
			
			if(!in_list){
			
				DeviceInfo tmp = new DeviceInfo();
				tmp.setDeviceId(id.substring(new String(pre_property + "Device_").length()));
				device_list.add(tmp);
				
				//Set DeviceInfo IDType
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id_type "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasDeviceIDtype ?id_type }";
				System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS2);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("id_type");
					String id_type_s = rn2.toString();
					System.out.println(id_type_s);
					
					device_list.get(device_list.size()-1).setIdType(id_type_s);
					
					
				}
				
				//Set DeviceInfo Name
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?name "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasAlias ?name }";
				System.out.println(strselectTag);
				Query selectTag6 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS2);
				ResultSet rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("name");
					String name_s = rn2.toString();
					System.out.println(name_s);
					
					device_list.get(device_list.size()-1).setName(name_s);
					
					
				}
				
				//Set DeviceInfo Domain
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?domain "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:belongsTo ?domain }";
				System.out.println(strselectTag);
				Query selectTag7 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS2);
				ResultSet rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("domain");
					String domain_s = rn2.toString().substring(new String(pre_property + "Domain_").length());
					System.out.println(domain_s);
					
					device_list.get(device_list.size()-1).setDomain(domain_s);
					
					
				}
				
				//Set DeviceInfo Owner
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?owner "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:ownedBy ?owner }";
				System.out.println(strselectTag);
				selectTag7 = QueryFactory.create(strselectTag);
				vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS2);
				rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("owner");
					String person_s = rn2.toString().substring(new String(pre_property + "Person_").length());
					System.out.println(person_s);
					
					device_list.get(device_list.size()-1).setOwner(person_s);
					
					
				}
				
			}
			
		}
		
		while(rsTag183.hasNext()) {
			
			QuerySolution qs2 = rsTag183.nextSolution();
			RDFNode rn = qs2.get("device");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "Device_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<device_list.size();i++){
				
				if(device_list.get(i).getDeviceId().compareTo(subID)==0){
					
					in_list = true;
					break;
				}
					
				
			}
			
			if(!in_list){
			
				DeviceInfo tmp = new DeviceInfo();
				tmp.setDeviceId(id.substring(new String(pre_property + "Device_").length()));
				device_list.add(tmp);
				
				//Set DeviceInfo IDType
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id_type "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasDeviceIDtype ?id_type }";
				System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS1);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("id_type");
					String id_type_s = rn2.toString();
					System.out.println(id_type_s);
					
					device_list.get(device_list.size()-1).setIdType(id_type_s);
					
					
				}
				
				//Set DeviceInfo Name
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?name "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasAlias ?name }";
				System.out.println(strselectTag);
				Query selectTag6 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS1);
				ResultSet rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("name");
					String name_s = rn2.toString();
					System.out.println(name_s);
					
					device_list.get(device_list.size()-1).setName(name_s);
					
					
				}
				
				//Set DeviceInfo Domain
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?domain "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:belongsTo ?domain }";
				System.out.println(strselectTag);
				Query selectTag7 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS1);
				ResultSet rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("domain");
					String domain_s = rn2.toString().substring(new String(pre_property + "Domain_").length());
					System.out.println(domain_s);
					
					device_list.get(device_list.size()-1).setDomain(domain_s);
					
					
				}
				
				//Set DeviceInfo Owner
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?owner "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:ownedBy ?owner }";
				System.out.println(strselectTag);
				selectTag7 = QueryFactory.create(strselectTag);
				vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS1);
				rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("owner");
					String person_s = rn2.toString().substring(new String(pre_property + "Person_").length());
					System.out.println(person_s);
					
					device_list.get(device_list.size()-1).setOwner(person_s);
					
					
				}
				
			}
			
		}
		
		
		
		
		
		//Set DomainInfo List
		strselectTag = "prefix infoflow: <" + pre_property + "> select ?domain "
				+ " from <http://pet.cs.nctu.edu.tw/trace> where { ?s infoflow:belongsTo ?domain }";
		System.out.println(strselectTag);
		selectTag4 = QueryFactory.create(strselectTag);
		vqeTag4 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS2);
		rsTag4 = vqeTag4.execSelect();
		vqeTag183 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS1);
		rsTag183 = vqeTag183.execSelect();
		
		while(rsTag4.hasNext()) {
			
			QuerySolution qs2 = rsTag4.nextSolution();
			RDFNode rn = qs2.get("domain");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "Domain_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<domain_list.size();i++){
				
				if(domain_list.get(i).getUri().compareTo(subID)==0){
					
					in_list = true;
					break;
				}
					
				
			}
			
			if(!in_list){
			
				DomainInfo tmp = new DomainInfo();
				tmp.setUri(id.substring(new String(pre_property + "Domain_").length()));
				domain_list.add(tmp);
				
				//Set DomainInfo Name
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?name "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasDomainName ?name }";
				System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS2);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("name");
					String name_s = rn2.toString();
					System.out.println(name_s);
					
					domain_list.get(domain_list.size()-1).setName(name_s);
					
					
				}
				
			}
			
		}
		
		
		
		while(rsTag183.hasNext()) {
			
			QuerySolution qs2 = rsTag183.nextSolution();
			RDFNode rn = qs2.get("domain");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "Domain_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<domain_list.size();i++){
				
				if(domain_list.get(i).getUri().compareTo(subID)==0){
					
					in_list = true;
					break;
				}
					
				
			}
			
			if(!in_list){
			
				DomainInfo tmp = new DomainInfo();
				tmp.setUri(id.substring(new String(pre_property + "Domain_").length()));
				domain_list.add(tmp);
				
				//Set DomainInfo Name
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?name "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasDomainName ?name }";
				System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS1);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("name");
					String name_s = rn2.toString();
					System.out.println(name_s);
					
					domain_list.get(domain_list.size()-1).setName(name_s);
					
					
				}
				
			}
			
		}
		
		
		
		//Set PersonInfo List(from Author)
		strselectTag = "prefix infoflow: <" + pre_property + "> select ?person "
				+ " from <http://pet.cs.nctu.edu.tw/trace> where { ?s infoflow:hasAuthor ?person }";
		System.out.println(strselectTag);
		selectTag4 = QueryFactory.create(strselectTag);
		vqeTag4 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS2);
		rsTag4 = vqeTag4.execSelect();
		vqeTag183 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS1);
		rsTag183 = vqeTag183.execSelect();
		
		while(rsTag4.hasNext()) {
			
			QuerySolution qs2 = rsTag4.nextSolution();
			RDFNode rn = qs2.get("person");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "Person_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<person_list.size();i++){
				
				if(person_list.get(i).getUri().compareTo(subID)==0){
					
					in_list = true;
					break;
				}
					
				
			}
			
			if(!in_list){
			
				PersonInfo tmp = new PersonInfo();
				tmp.setUri(id.substring(new String(pre_property + "Person_").length()));
				person_list.add(tmp);
				
				//Set PersonInfo IDType
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id_type "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasPersonIDtype ?id_type }";
				System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS2);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("id_type");
					String id_type_s = rn2.toString();
					System.out.println(id_type_s);
					
					person_list.get(person_list.size()-1).setIdType(id_type_s);
					
					
				}
				
				//Set PersonInfo ID
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasID ?id }";
				System.out.println(strselectTag);
				Query selectTag6 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS2);
				ResultSet rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("id");
					String id_s = rn2.toString();
					System.out.println(id_s);
					
					person_list.get(person_list.size()-1).setPersonId(id_s);
					
					
				}
				
				//Set PersonInfo Name
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?name "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasLegalName ?name }";
				System.out.println(strselectTag);
				selectTag6 = QueryFactory.create(strselectTag);
				vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS2);
				rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("name");
					String name_s = rn2.toString();
					System.out.println(name_s);
					
					person_list.get(person_list.size()-1).setName(name_s);
					
					
				}
				
				//Set DeviceInfo Domain
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?domain "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:belongsTo ?domain }";
				System.out.println(strselectTag);
				Query selectTag7 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS2);
				ResultSet rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("domain");
					String domain_s = rn2.toString().substring(new String(pre_property + "Domain_").length());
					System.out.println(domain_s);
					
					person_list.get(person_list.size()-1).setDomain(domain_s);
					
					
				}
				
			}
			
		}
		
		
		
		while(rsTag183.hasNext()) {
			
			QuerySolution qs2 = rsTag183.nextSolution();
			RDFNode rn = qs2.get("person");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "Person_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<person_list.size();i++){
				
				if(person_list.get(i).getUri().compareTo(subID)==0){
					
					in_list = true;
					break;
				}
					
				
			}
			
			if(!in_list){
			
				PersonInfo tmp = new PersonInfo();
				tmp.setUri(id.substring(new String(pre_property + "Person_").length()));
				person_list.add(tmp);
				
				//Set PersonInfo IDType
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id_type "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasPersonIDtype ?id_type }";
				System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS1);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("id_type");
					String id_type_s = rn2.toString();
					System.out.println(id_type_s);
					
					person_list.get(person_list.size()-1).setIdType(id_type_s);
					
					
				}
				
				//Set PersonInfo ID
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasID ?id }";
				System.out.println(strselectTag);
				Query selectTag6 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS1);
				ResultSet rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("id");
					String id_s = rn2.toString();
					System.out.println(id_s);
					
					person_list.get(person_list.size()-1).setPersonId(id_s);
					
					
				}
				
				//Set PersonInfo Name
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?name "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasLegalName ?name }";
				System.out.println(strselectTag);
				selectTag6 = QueryFactory.create(strselectTag);
				vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS1);
				rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("name");
					String name_s = rn2.toString();
					System.out.println(name_s);
					
					person_list.get(person_list.size()-1).setName(name_s);
					
					
				}
				
				//Set DeviceInfo Domain
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?domain "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:belongsTo ?domain }";
				System.out.println(strselectTag);
				Query selectTag7 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS1);
				ResultSet rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("domain");
					String domain_s = rn2.toString().substring(new String(pre_property + "Domain_").length());
					System.out.println(domain_s);
					
					person_list.get(person_list.size()-1).setDomain(domain_s);
					
					
				}
				
			}
			
		}
		
		
		//Set PersonInfo List(from Owner)
		strselectTag = "prefix infoflow: <" + pre_property + "> select ?person "
				+ " from <http://pet.cs.nctu.edu.tw/trace> where { ?s infoflow:ownedBy ?person }";
		System.out.println(strselectTag);
		selectTag4 = QueryFactory.create(strselectTag);
		vqeTag4 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS2);
		rsTag4 = vqeTag4.execSelect();
		vqeTag183 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS1);
		rsTag183 = vqeTag183.execSelect();
		
		while(rsTag4.hasNext()) {
			
			QuerySolution qs2 = rsTag4.nextSolution();
			RDFNode rn = qs2.get("person");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "Person_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<person_list.size();i++){
				
				if(person_list.get(i).getUri().compareTo(subID)==0){
					
					in_list = true;
					break;
				}
					
				
			}
			
			if(!in_list){
			
				PersonInfo tmp = new PersonInfo();
				tmp.setUri(id.substring(new String(pre_property + "Person_").length()));
				person_list.add(tmp);
				
				//Set PersonInfo IDType
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id_type "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasPersonIDtype ?id_type }";
				System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS2);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("id_type");
					String id_type_s = rn2.toString();
					System.out.println(id_type_s);
					
					person_list.get(person_list.size()-1).setIdType(id_type_s);
					
					
				}
				
				//Set PersonInfo ID
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasID ?id }";
				System.out.println(strselectTag);
				Query selectTag6 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS2);
				ResultSet rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("id");
					String id_s = rn2.toString();
					System.out.println(id_s);
					
					person_list.get(person_list.size()-1).setPersonId(id_s);
					
					
				}
				
				//Set PersonInfo Name
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?name "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasLegalName ?name }";
				System.out.println(strselectTag);
				selectTag6 = QueryFactory.create(strselectTag);
				vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS2);
				rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("name");
					String name_s = rn2.toString();
					System.out.println(name_s);
					
					person_list.get(person_list.size()-1).setName(name_s);
					
					
				}
				
				//Set DeviceInfo Domain
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?domain "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:belongsTo ?domain }";
				System.out.println(strselectTag);
				Query selectTag7 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS2);
				ResultSet rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("domain");
					String domain_s = rn2.toString().substring(new String(pre_property + "Domain_").length());
					System.out.println(domain_s);
					
					person_list.get(person_list.size()-1).setDomain(domain_s);
					
					
				}
				
			}
			
		}
		
		
		
		
		while(rsTag183.hasNext()) {
			
			QuerySolution qs2 = rsTag183.nextSolution();
			RDFNode rn = qs2.get("person");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "Person_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<person_list.size();i++){
				
				if(person_list.get(i).getUri().compareTo(subID)==0){
					
					in_list = true;
					break;
				}
					
				
			}
			
			if(!in_list){
			
				PersonInfo tmp = new PersonInfo();
				tmp.setUri(id.substring(new String(pre_property + "Person_").length()));
				person_list.add(tmp);
				
				//Set PersonInfo IDType
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id_type "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasPersonIDtype ?id_type }";
				System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS1);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("id_type");
					String id_type_s = rn2.toString();
					System.out.println(id_type_s);
					
					person_list.get(person_list.size()-1).setIdType(id_type_s);
					
					
				}
				
				//Set PersonInfo ID
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?id "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasID ?id }";
				System.out.println(strselectTag);
				Query selectTag6 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS1);
				ResultSet rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("id");
					String id_s = rn2.toString();
					System.out.println(id_s);
					
					person_list.get(person_list.size()-1).setPersonId(id_s);
					
					
				}
				
				//Set PersonInfo Name
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?name "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasLegalName ?name }";
				System.out.println(strselectTag);
				selectTag6 = QueryFactory.create(strselectTag);
				vqeTag6 = VirtuosoQueryExecutionFactory.create(selectTag6, setPOS1);
				rsTag6 = vqeTag6.execSelect();
				
				while(rsTag6.hasNext()) {
					
					QuerySolution qs = rsTag6.nextSolution();
					RDFNode rn2 = qs.get("name");
					String name_s = rn2.toString();
					System.out.println(name_s);
					
					person_list.get(person_list.size()-1).setName(name_s);
					
					
				}
				
				//Set DeviceInfo Domain
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?domain "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:belongsTo ?domain }";
				System.out.println(strselectTag);
				Query selectTag7 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS1);
				ResultSet rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("domain");
					String domain_s = rn2.toString().substring(new String(pre_property + "Domain_").length());
					System.out.println(domain_s);
					
					person_list.get(person_list.size()-1).setDomain(domain_s);
					
					
				}
				
			}
			
		}
		
		
		
		//Set TransportSession List
		strselectTag = "prefix infoflow: <" + pre_property + "> select ?tag "
				+ " from <http://pet.cs.nctu.edu.tw/trace> where { ?s infoflow:hasTransportSession ?tag }";
		System.out.println(strselectTag);
		selectTag4 = QueryFactory.create(strselectTag);
		vqeTag4 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS2);
		rsTag4 = vqeTag4.execSelect();
		vqeTag183 = VirtuosoQueryExecutionFactory.create(selectTag4, setPOS1);
		rsTag183 = vqeTag183.execSelect();
		
		while(rsTag4.hasNext()) {
			
			QuerySolution qs2 = rsTag4.nextSolution();
			RDFNode rn = qs2.get("tag");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "TransportSession_").length());
			
			TagInfo tmp = new TagInfo();
			tmp.setTagID(id.substring(new String(pre_property + "TransportSession_").length()));
			tag_list.add(tmp);
			
			//Set TagInfo Transfer Time
			strselectTag = "prefix infoflow: <" + pre_property + "> select ?time "
					+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasOccurAt ?time }";
			System.out.println(strselectTag);
			Query selectTag5 = QueryFactory.create(strselectTag);
			VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS2);
			ResultSet rsTag5 = vqeTag5.execSelect();
			
			while(rsTag5.hasNext()) {
				
				QuerySolution qs = rsTag5.nextSolution();
				RDFNode rn2 = qs.get("time");
				String time_s = rn2.toString();
				System.out.println(time_s);
				
				long time_l = Long.parseLong(time_s);
				Date dtmp = new Date(time_l);
				
				tag_list.get(tag_list.size()-1).setTransTime(dtmp);
				
				
			}
			
			//Set TagInfo Document
			strselectTag = "prefix infoflow: <" + pre_property + "> select ?doc "
					+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:isTransportSessionOf ?doc }";
			System.out.println(strselectTag);
			Query selectTag7 = QueryFactory.create(strselectTag);
			VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS2);
			ResultSet rsTag7 = vqeTag7.execSelect();
			
			while(rsTag7.hasNext()) {
				
				QuerySolution qs = rsTag7.nextSolution();
				RDFNode rn2 = qs.get("doc");
				String doc_s = rn2.toString().substring(new String(pre_property + "Document_").length());
				System.out.println(doc_s);
				
				DocumentInfo dtmp = new DocumentInfo();
				
				for(int i=0;i<doc_list.size();i++){
					
					if(doc_list.get(i).getDocumentId().compareTo(doc_s)==0){
						
						dtmp = doc_list.get(i);
						break;
						
					}
					
				}
				
				tag_list.get(tag_list.size()-1).setDoc(dtmp);
				
				
			}
			
			//Set TagInfo Source
			strselectTag = "prefix infoflow: <" + pre_property + "> select ?source "
					+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasSource ?source }";
			System.out.println(strselectTag);
			selectTag7 = QueryFactory.create(strselectTag);
			vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS2);
			rsTag7 = vqeTag7.execSelect();
			
			while(rsTag7.hasNext()) {
				
				QuerySolution qs = rsTag7.nextSolution();
				RDFNode rn2 = qs.get("source");
				String source_s = rn2.toString().substring(new String(pre_property + "Device_").length());
				System.out.println(source_s);
				
				DeviceInfo dtmp = new DeviceInfo();
				
				for(int i=0;i<device_list.size();i++){
					
					if(device_list.get(i).getDeviceId().compareTo(source_s)==0){
						
						dtmp = device_list.get(i);
						break;
						
					}
					
				}
				
				tag_list.get(tag_list.size()-1).setTransFrom(dtmp);
				
				
			}
			
			//Set TagInfo Destination
			strselectTag = "prefix infoflow: <" + pre_property + "> select ?des "
					+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasDestination ?des }";
			System.out.println(strselectTag);
			selectTag7 = QueryFactory.create(strselectTag);
			vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS2);
			rsTag7 = vqeTag7.execSelect();
			
			while(rsTag7.hasNext()) {
				
				QuerySolution qs = rsTag7.nextSolution();
				RDFNode rn2 = qs.get("des");
				String des_s = rn2.toString().substring(new String(pre_property + "Device_").length());
				System.out.println(des_s);
				
				DeviceInfo dtmp = new DeviceInfo();
				
				for(int i=0;i<device_list.size();i++){
					
					if(device_list.get(i).getDeviceId().compareTo(des_s)==0){
						
						dtmp = device_list.get(i);
						break;
						
					}
					
				}
				
				tag_list.get(tag_list.size()-1).addTransTo(dtmp);
				
				
			}
			
		}
		
		
		
		
		while(rsTag183.hasNext()) {
			
			QuerySolution qs2 = rsTag183.nextSolution();
			RDFNode rn = qs2.get("tag");
			String id = rn.toString();
			String subID = id.substring(new String(pre_property + "TransportSession_").length());
			
			boolean in_list = false;
			
			for(int i=0;i<tag_list.size();i++){
				
				//System.out.println(subUUID + " <-> " + doc_list.get(i).getDocumentId());
				
				if(tag_list.get(i).getTagID().compareTo(subID)==0){
					
					in_list = true;
					break;
				}
					
				//System.out.println(in_list);
				
			}
			
			if(!in_list){
			
				TagInfo tmp = new TagInfo();
				tmp.setTagID(id.substring(new String(pre_property + "TransportSession_").length()));
				tag_list.add(tmp);
				
				//Set TagInfo Transfer Time
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?time "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasOccurAt ?time }";
				System.out.println(strselectTag);
				Query selectTag5 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag5 = VirtuosoQueryExecutionFactory.create(selectTag5, setPOS1);
				ResultSet rsTag5 = vqeTag5.execSelect();
				
				while(rsTag5.hasNext()) {
					
					QuerySolution qs = rsTag5.nextSolution();
					RDFNode rn2 = qs.get("time");
					String time_s = rn2.toString();
					System.out.println(time_s);
					
					long time_l = Long.parseLong(time_s);
					Date dtmp = new Date(time_l);
					
					tag_list.get(tag_list.size()-1).setTransTime(dtmp);
					
					
				}
				
				//Set TagInfo Document
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?doc "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:isTransportSessionOf ?doc }";
				System.out.println(strselectTag);
				Query selectTag7 = QueryFactory.create(strselectTag);
				VirtuosoQueryExecution vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS1);
				ResultSet rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("doc");
					String doc_s = rn2.toString().substring(new String(pre_property + "Document_").length());
					System.out.println(doc_s);
					
					DocumentInfo dtmp = new DocumentInfo();
					
					for(int i=0;i<doc_list.size();i++){
						
						if(doc_list.get(i).getDocumentId().compareTo(doc_s)==0){
							
							dtmp = doc_list.get(i);
							break;
							
						}
						
					}
					
					tag_list.get(tag_list.size()-1).setDoc(dtmp);
					
					
				}
				
				//Set TagInfo Source
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?source "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasSource ?source }";
				System.out.println(strselectTag);
				selectTag7 = QueryFactory.create(strselectTag);
				vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS1);
				rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("source");
					String source_s = rn2.toString().substring(new String(pre_property + "Device_").length());
					System.out.println(source_s);
					
					DeviceInfo dtmp = new DeviceInfo();
					
					for(int i=0;i<device_list.size();i++){
						
						if(device_list.get(i).getDeviceId().compareTo(source_s)==0){
							
							dtmp = device_list.get(i);
							break;
							
						}
						
					}
					
					tag_list.get(tag_list.size()-1).setTransFrom(dtmp);
					
					
				}
				
				//Set TagInfo Destination
				strselectTag = "prefix infoflow: <" + pre_property + "> select ?des "
						+ " from <http://pet.cs.nctu.edu.tw/trace> where { <" + id + "> infoflow:hasDestination ?des }";
				System.out.println(strselectTag);
				selectTag7 = QueryFactory.create(strselectTag);
				vqeTag7 = VirtuosoQueryExecutionFactory.create(selectTag7, setPOS1);
				rsTag7 = vqeTag7.execSelect();
				
				while(rsTag7.hasNext()) {
					
					QuerySolution qs = rsTag7.nextSolution();
					RDFNode rn2 = qs.get("des");
					String des_s = rn2.toString().substring(new String(pre_property + "Device_").length());
					System.out.println(des_s);
					
					DeviceInfo dtmp = new DeviceInfo();
					
					for(int i=0;i<device_list.size();i++){
						
						if(device_list.get(i).getDeviceId().compareTo(des_s)==0){
							
							dtmp = device_list.get(i);
							break;
							
						}
						
					}
					
					tag_list.get(tag_list.size()-1).addTransTo(dtmp);
					
					
				}
				
			}
			
		}
		
		
		
		
		//Sorting tag_list by recevice time
		Collections.sort(tag_list, new Comparator<TagInfo>(){
			@Override
			public int compare(TagInfo o1, TagInfo o2){
				//System.out.println((int)o1.getTransTime().getTime() + " - " + (int)o2.getTransTime().getTime());
				if(o1.getTransTime().getTime() - o2.getTransTime().getTime() <0)
					return -1;
				else if(o1.getTransTime().getTime() - o2.getTransTime().getTime() >0)
					return 1;
				else return 0;
			}
		});
		
		
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		
		for(int i=0;i<tag_list.size();i++){
			
			System.out.println("UUID = " + tag_list.get(i).getTagID() + ", Document = " + tag_list.get(i).getDoc().getTitle() + ", Time = " + sdFormat.format(tag_list.get(i).getTransTime()));
			System.out.println("Transmit from = " + tag_list.get(i).getTransFrom().getDeviceId() + ", " + tag_list.get(i).getTransFrom().getDomain() 
					+ ", " + tag_list.get(i).getTransFrom().getOwner()); 
			for(int j=0;j<tag_list.get(i).getTransTo().size();j++){
				System.out.println("Transmit to = " + tag_list.get(i).getTransTo().get(j).getDeviceId() + ", " + tag_list.get(i).getTransTo().get(j).getDomain() 
						+ ", " + tag_list.get(i).getTransTo().get(j).getOwner()); 
			}
			
		}
		
		int trace_num = 0;
		
		
			System.out.println("The document you can trace:");
			
			for(int i=0;i<doc_list.size();i++){
				
				System.out.println("[" + (i+1) + "] name: " + doc_list.get(i).getTitle() + ", sensitivity level: " + doc_list.get(i).getSenLv() + ", version: " + doc_list.get(i).getVersion() + ", domain: " + doc_list.get(i).getDomain() + ", author: " + doc_list.get(i).getAuthor());
				
			}
			
			Scanner scanner = new Scanner(System.in);
			System.out.print("Please input the document number you want to trace: ");
			trace_num = scanner.nextInt() - 1;
			trace_doc = doc_list.get(trace_num).getTitle();
			trace_doc_id = doc_list.get(trace_num).getDocumentId();
			
			
			//Get all tag about choice document
			doc_tag_list = new ArrayList<TagInfo>();
			for(int i=0;i<tag_list.size();i++){
				
				if(tag_list.get(i).getDoc().getDocumentId().compareTo(trace_doc_id)==0){
					doc_tag_list.add(tag_list.get(i));
				}
				
			}
			
			if(select==2){
				
				for(int i=doc_tag_list.size()-1;i>=0;i--){
					
					String from_domain = doc_tag_list.get(i).getTransFrom().getDomain();
					
					for(int j=doc_tag_list.get(i).getTransTo().size()-1;j>=0;j--){
						
						String to_domain = doc_tag_list.get(i).getTransTo().get(j).getDomain();
						
						if(from_domain.compareTo(to_domain)==0){
							
							doc_tag_list.get(i).getTransTo().remove(j);
							
						}
						
					}
					
					if(doc_tag_list.get(i).getTransTo().isEmpty()){
						
						doc_tag_list.remove(i);
					}
					
				}
				
			}
			
			//Get all machine transfer this document
			ArrayList<String> trans_mac = new ArrayList<String>();
			for(int i=0;i<doc_tag_list.size();i++){

				if(!trans_mac.contains(doc_tag_list.get(i).getTransFrom().getDeviceId())){
					trans_mac.add(doc_tag_list.get(i).getTransFrom().getDeviceId());
				}
				for(int j=0;j<doc_tag_list.get(i).getTransTo().size();j++){
					if(!trans_mac.contains(doc_tag_list.get(i).getTransTo().get(j).getDeviceId())){
						trans_mac.add(doc_tag_list.get(i).getTransTo().get(j).getDeviceId());
					}
				}
			}
			
			//Write the trace device information to excel
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Device information");
			Map<String, Object[]> data = new TreeMap<String, Object[]>();
			data.put("1", new Object[] {"Domain&Device Alias", "Device ID Type", "Device ID", "Device Owner", "Owner ID Type", "Owner ID"});
			ArrayList<String> in_excel = new ArrayList<String>();
			int index = 2;
			for(int i=0;i<doc_tag_list.size();i++){
				DeviceInfo dfrom = doc_tag_list.get(i).getTransFrom();
				String tmp = dfrom.getDomain() + dfrom.getName();
				if(!in_excel.contains(tmp)){
					in_excel.add(tmp);
					String device_owner = dfrom.getOwner();
					PersonInfo ptmp = null;
					for(int j=0;j<person_list.size();j++){
						if(person_list.get(j).getUri().compareTo(device_owner)==0){
							ptmp = person_list.get(j);
							break;
						}
					}
					data.put("" + index++, new Object[] {tmp, dfrom.getIdType(), dfrom.getDeviceId(), ptmp.getName(), ptmp.getIdType(), ptmp.getPersonId()});
				}
				ArrayList<DeviceInfo> dto = doc_tag_list.get(i).getTransTo();
				for(int j=0;j<dto.size();j++){
					tmp = dto.get(j).getDomain() + dto.get(j).getName();
					if(!in_excel.contains(tmp)){
						in_excel.add(tmp);
						String device_owner = dto.get(j).getOwner();
						PersonInfo ptmp = null;
						for(int k=0;k<person_list.size();k++){
							if(person_list.get(k).getUri().compareTo(device_owner)==0){
								ptmp = person_list.get(k);
								break;
							}
						}
						data.put("" + index++, new Object[] {tmp, dto.get(j).getIdType(), dto.get(j).getDeviceId(), ptmp.getName(), ptmp.getIdType(), ptmp.getPersonId()});
					}
				}
			}
			
			Set<String> keyset = data.keySet();
			int rownum = 0;
			for (String key : keyset)
			{
			    Row row = sheet.createRow(rownum++);
			    Object [] objArr = data.get(key);
			    int cellnum = 0;
			    for (Object obj : objArr)
			    {
			       Cell cell = row.createCell(cellnum++);
			       if(obj instanceof String)
			            cell.setCellValue((String)obj);
			        else if(obj instanceof Integer)
			            cell.setCellValue((Integer)obj);
			    }
			}
			try 
			{
				//Write the workbook in file system
			    FileOutputStream out = new FileOutputStream(new File("Transmit Device Information.xlsx"));
			    workbook.write(out);
			    out.close();
			    
			    System.out.println("Transmit Device Information.xlsx written successfully on disk.");
			     
			} 
			catch (Exception e) 
			{
			    e.printStackTrace();
			}
			
			

	        //Draw the data flow
			try {
				rdf_count++;
				be.datablend.streaming.sail.inferencing.InferenceLoop.addToGraph(doc_tag_list);
			} catch (SailException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}

}
