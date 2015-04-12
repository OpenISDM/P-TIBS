package nctu.petlab.tibs.ws.rest;

import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

@Path("place/")
public class Place {

	int mErrCode = 0;
	String mErrMsg = "";
	
	@Context private UriInfo uriInfo;
	@Context private javax.servlet.http.HttpServletRequest hsr;
	
	@GET
	@Path("showList")
	public Response getPlaceList(){
		
		String DB_URL = "jdbc:mysql://localhost:3306/yfc?useUnicode=true&characterEncoding=utf8";
	    String DB_USER = "pi";
	    String DB_PASSWD = "pet97x2z";
	    String DB = "MySQL";
	    String DB_DRIVER = "com.mysql.jdbc.Driver";
	    
	    try {
	        Class.forName("com.mysql.jdbc.Driver");
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    }
	  
	    IDBConnection con = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
	    OntModel model = getModelFromDB(con,"yfc");
	    String rslt = "<html><head><title>Show List</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>";
	    //System.out.println(model.toString()+" ,size="+model.size()+" ,count="+model.countSubModels());
	  //  printModel(model);
	    for (ResIterator i = model.listSubjects(); i.hasNext();) {
	    	Resource n = (Resource) i.next();
	    	if(n.toString().contains("http")){
	    		NodeIterator name = model.listObjectsOfProperty(n, model.getProperty("gr:name"));
                NodeIterator lat = model.listObjectsOfProperty(n, model.getProperty("gr:latitude"));
                NodeIterator lon = model.listObjectsOfProperty(n, model.getProperty("gr:longitude"));
                NodeIterator addr = model.listObjectsOfProperty(n , model.getProperty("gr:address"));
                NodeIterator stAddr = null;
                if(addr.hasNext()){
                        stAddr = model.listObjectsOfProperty(
                                        addr.next().asResource(),
                                        model.getProperty("gr:streetAddress"));
                }else
                        System.out.println("-> get street address error because of null");

                if(stAddr != null){
                        if(name.hasNext() && lat.hasNext() && lon.hasNext()){
                                RDFNode nodeName = (RDFNode) name.next();
                                RDFNode nodeLat = (RDFNode) lat.next();
                                RDFNode nodeLon = (RDFNode) lon.next();
                                rslt += "<li><a href='"+nodeName.toString()+"'>"+nodeName.toString()+"<br>";
                        }
                }
	    	}
	    }
		
			
		
		rslt += "</body></html>";
		System.out.println("rslt = "+rslt);		
		ResponseBuilder builder = Response.ok(rslt);		
		return builder.build();
	}
	
	public static void printModel(OntModel model) {  
	    for (Iterator i = model.listClasses(); i.hasNext();) {  
	        OntClass oc = (OntClass) i.next();  
	        System.out.println(oc.getLocalName());  
	    }  
	} 
	
	@GET
	@Path("{placeName}")
	public Response getPlaceDetail(@PathParam("placeName") String place){
		String DB_URL = "jdbc:mysql://localhost:3306/yfc?useUnicode=true&characterEncoding=utf8";
	    String DB_USER = "pi";
	    String DB_PASSWD = "pet97x2z";
	    String DB = "MySQL";
	    String DB_DRIVER = "com.mysql.jdbc.Driver";
	    System.out.println("place = "+place);
	    try {
	        Class.forName("com.mysql.jdbc.Driver");
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    }
	  
	    IDBConnection con = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
	    OntModel model = getModelFromDB(con,"yfc");
	    String rslt = "<html><head><title>Show Detail</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>";
		
	    for (ResIterator i = model.listSubjects(); i.hasNext();) {
	    	Resource n = (Resource) i.next();
	    	if(n.toString().contains("http")){
	    		NodeIterator name = model.listObjectsOfProperty(n, model.getProperty("gr:name"));
                NodeIterator lat = model.listObjectsOfProperty(n, model.getProperty("gr:latitude"));
                NodeIterator lon = model.listObjectsOfProperty(n, model.getProperty("gr:longitude"));
                NodeIterator addr = model.listObjectsOfProperty(n , model.getProperty("gr:address"));
                NodeIterator stAddr = null;
                if(addr.hasNext()){
                        stAddr = model.listObjectsOfProperty(
                                        addr.next().asResource(),
                                        model.getProperty("gr:streetAddress"));
                }else
                        System.out.println("-> get street address error because of null");

                if(stAddr != null){
                        if(name.hasNext() && lat.hasNext() && lon.hasNext()){
                                RDFNode nodeName = (RDFNode) name.next();
                                RDFNode nodeLat = (RDFNode) lat.next();
                                RDFNode nodeLon = (RDFNode) lon.next();
                                if(nodeName.toString().equals(place)){
                                	rslt += "<b>address info</b><br>lat/lon:"+nodeLat.toString()+"/"+nodeLon.toString()+"<br>address:";
                                	if(stAddr.hasNext()){
                                        RDFNode nodeStAddr = (RDFNode) stAddr.next();
                                       rslt += nodeStAddr.toString();
                                	}
                                }
                        }
                }
	    	}
	    }
		
		rslt += "</body></html>";
		System.out.println("rslt = "+rslt);		
		ResponseBuilder builder = Response.ok(rslt);		
		return builder.build();
	}
	
	/* 連接數據庫 */
	public static IDBConnection connectDB(String DB_URL, String DB_USER, String DB_PASSWD, String DB_NAME) {
	    return new DBConnection(DB_URL, DB_USER, DB_PASSWD, DB_NAME);
	} 
	
	/* 從數據庫中得到已存入本體 */
	public static OntModel getModelFromDB(IDBConnection con, String name) {
	    ModelMaker maker = ModelFactory.createModelRDBMaker(con);
	    Model base = maker.getModel(name);
	    OntModel newmodel = ModelFactory.createOntologyModel(getModelSpec(maker), base);
	    return newmodel;
	}
	
	public static OntModelSpec getModelSpec(ModelMaker maker) {
	    OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
	    spec.setImportModelMaker(maker);
	    return spec;
	}
	
 
	
	
}
