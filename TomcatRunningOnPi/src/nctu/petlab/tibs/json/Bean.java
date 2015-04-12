package nctu.petlab.tibs.json;

import java.sql.Timestamp;

public class Bean {

	String addr = null;
	String created_at = null;
	String fac_type = null;
	int id = -1;
	String lat = null;
	String lon = null;
	String name = null;
	String tel = null;
	String updated_at = null;
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getFac_type() {
		return fac_type;
	}
	public void setFac_type(String fac_type) {
		this.fac_type = fac_type;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}
	@Override
	public String toString() {
		return "Bean [addr=" + addr + ", created_at=" + created_at
				+ ", fac_type=" + fac_type + ", id=" + id + ", lat=" + lat
				+ ", lon=" + lon + ", name=" + name + ", tel=" + tel
				+ ", updated_at=" + updated_at + "]";
	}
	
	
	
}
