package tw.edu.nctu.cs.pet;

public class DeviceInfo {
	private String id_type = "";
	private String device_id = "";
	private String name = "";
	private String domain = "";
	private String owner = "";
	
	public void setIdType(String s){
		this.id_type = s;
	}
	
	public String getIdType(){
		return this.id_type;
	}
	
	public void setDeviceId(String s){
		this.device_id = s;
	}
	
	public String getDeviceId(){
		return this.device_id;
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
	
	public void setOwner(String s){
		this.owner = s;
	}
	
	public String getOwner(){
		return this.owner;
	}
}
