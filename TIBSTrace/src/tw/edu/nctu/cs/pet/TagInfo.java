package tw.edu.nctu.cs.pet;

import java.util.ArrayList;
import java.util.Date;

public class TagInfo {
	private String tag_id = "";
	private Date transTime = new Date();
	private DeviceInfo trans_from = new DeviceInfo();
	private ArrayList<DeviceInfo> trans_to = new ArrayList<DeviceInfo>();
	private DocumentInfo doc = new DocumentInfo();
	
	public void setTagID(String s){
		this.tag_id = s;
	}
	
	public String getTagID(){
		return this.tag_id;
	}
	
	public void setTransTime(Date d){
		this.transTime = d;
	}
	
	public Date getTransTime(){
		return this.transTime;
	}
	
	public void setTransFrom(DeviceInfo di){
		this.trans_from = di;
	}
	
	public DeviceInfo getTransFrom(){
		return this.trans_from;
	}
	
	public void addTransTo(DeviceInfo di){
		this.trans_to.add(di);
	}
	
	public ArrayList<DeviceInfo> getTransTo(){
		return this.trans_to;
	}
	
	public void setDoc(DocumentInfo s){
		this.doc = s;
	}
	
	public DocumentInfo getDoc(){
		return this.doc;
	}
	
}
