package nctu.petlab.tibs.ws.bean;

import java.io.Serializable;

public class MadInfo implements Serializable{
	
	private static final long serialVersionUID = 3922424505668001324L;
	
	String name = "";
	String phone = null;
	double lon = 0.0;
	double lat = 0.0;
	String addr = "";
	String str_addr = null;
	
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
	
	@Override
	public String toString() {
		return "PersonInfo [name=" + name + ", phone=" + phone + ", longitude=" + lon + ", latitude=" + lat
				+ ", address=" + addr + "]";
	}	
}
