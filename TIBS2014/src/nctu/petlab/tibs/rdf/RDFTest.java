package nctu.petlab.tibs.rdf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.VCARD;

public class RDFTest {

	public RDFTest() {}
	
	public void test(){
		String personUri = "http://localhost/ivan";
		String fullName = "I-Fan Chou";
		
		Model model = ModelFactory.createDefaultModel();
		Resource resource = model.createResource(personUri);
		
		resource.addProperty(VCARD.FN, fullName).addProperty(VCARD.GROUP, "PET Lab");
			//.addLiteral(VCARD.N, model.createResource().addProperty(VCARD.Given, "I-Fan").addProperty(VCARD.Family, "Chou"));
		/*
		StmtIterator iter = model.listStatements();
		while(iter.hasNext()){
			Statement stmt = iter.nextStatement();
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();
			System.out.println("subject = "+subject.toString());
			System.out.println("predicate = "+predicate.toString());
			if(object instanceof Resource){
				System.out.println("resource: "+object.toString());
			}else{
				System.out.println(object.toString());
			}
			System.out.println("end");
		}
		*/
		model.write(System.out,"RDF/JSON");
		
		try {
			model.write(new FileOutputStream("d:/yfc/Tmp/rtmp.rdf"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void readRdfFile(){
		Model model = ModelFactory.createDefaultModel();
		InputStream in = FileManager.get().open("d:/yfc/Tmp/MAD.rdf");
		if(in!=null){
			model.read(in, null);
			StmtIterator iter = model.listStatements();
			while(iter.hasNext()){
				Statement stmt = iter.nextStatement();
				Resource subject = stmt.getSubject();
				Property predicate = stmt.getPredicate();
				RDFNode object = stmt.getObject();
				System.out.println("subject = "+subject.toString());
				System.out.println("predicate = "+predicate.toString());
				if(object instanceof Resource){
					System.out.println("resource: "+object.toString());
				}else{
					System.out.println(object.toString());
				}
				System.out.println("end");
			}
			//model.write(System.out);
		}
		 
	}

}
