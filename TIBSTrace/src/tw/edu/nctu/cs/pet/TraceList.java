package tw.edu.nctu.cs.pet;

import java.util.ArrayList;

public class TraceList {
	private DeviceInfo start_point = new DeviceInfo();
	private ArrayList<TraceList> tracelist = new ArrayList<TraceList>();
	
	public boolean isEmpty(){
		return this.tracelist.isEmpty();
	}
	
	public void addNode(TraceList tl){
		
		this.tracelist.add(tl);
		
	}
	
	public ArrayList<TraceList> getNode(){
		
		return this.tracelist;
		
	}
	
	public void setStartPoint(DeviceInfo di){
		this.start_point = di;
	}
	
	public DeviceInfo getStartPoint(){
		return this.start_point;
	}
}
