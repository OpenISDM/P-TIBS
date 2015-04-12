package nctu.petlab.tibs.ws.rest;

public class data {
	String name;
	String phone;
	double lon;
	double lat;
	String addr;
	String str_addr;
	
	public data(){
		name = "";
		phone = "";
		lon = 0.0;
		lat = 0.0;
		addr = "";
		str_addr = "";
	}
	
	public void setName(String s){
		this.name = s;
	}
	
	public void setPhone(String s){
		this.phone = s;
	}
	
	public void setLon(double s){
		this.lon = s;
	}
	
	public void setLat(double s){
		this.lat = s;
	}
	
	public void setStr(String s){
		this.str_addr = s;
	}
	
	public void setAddr(String s){
		this.addr = s;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getPhone(){
		return this.phone;
	}
	
	public double getLon(){
		return this.lon;
	}
	
	public double getLat(){
		return this.lat;
	}
	
	public String getStr(){
		return this.str_addr;
	}
	
	public String getAddr(){
		return this.addr;
	}
}
