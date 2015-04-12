package tw.edu.nctu.cs.pet;

public class PersonInfo {
	private String uri = "";
	private String id_type = "";
	private String id = "";
	private String name = "";
	private String domain = "";
	
	public void setUri(String s){
		this.uri = s;
	}
	
	public String getUri(){
		return this.uri;
	}
	
	public void setIdType(String s){
		this.id_type = s;
	}
	
	public String getIdType(){
		return this.id_type;
	}
	
	public void setPersonId(String s){
		this.id = s;
	}
	
	public String getPersonId(){
		return this.id;
	}
	
	public void setName(String s){
		this.name = s;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setDomain(String s){
		this.domain = s;
	}
	
	public String getDomain(){
		return this.domain;
	}
	
}
