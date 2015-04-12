package tw.edu.nctu.cs.pet;

import java.util.ArrayList;

public class DocumentInfo {
	private String document_id = "";
	private String title = "";
	private String senLv = "";
	private String version = "";
	private String domain = "";
	private String author = "";
	private ArrayList<String> tag_list = new ArrayList<String>();
	
	
	public void setDocumentId(String s){
		this.document_id = s;
	}
	
	public String getDocumentId(){
		return this.document_id;
	}
	
	public void setTitle(String s){
		this.title = s;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public void setSenLv(String s){
		this.senLv = s;
	}
	
	public String getSenLv(){
		return this.senLv;
	}
	
	public void setVersion(String s){
		this.version = s;
	}
	
	public String getVersion(){
		return this.version;
	}
	
	public void setDomain(String s){
		this.domain = s;
	}
	
	public String getDomain(){
		return this.domain;
	}
	
	public void setAuthor(String s){
		this.author = s;
	}
	
	public String getAuthor(){
		return this.author;
	}
	
	public void addTag(String s){
		this.tag_list.add(s);
	}
	
	public ArrayList<String> getTagList(){
		return this.tag_list;
	}
}
