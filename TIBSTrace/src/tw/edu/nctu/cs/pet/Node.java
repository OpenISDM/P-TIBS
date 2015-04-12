package tw.edu.nctu.cs.pet;

import java.util.ArrayList;

public class Node {
	private String parent = null;
	private String content = null;
	private ArrayList<Node> childs = new ArrayList<Node>();
	
	public void setParent(String s){
		this.parent = s;
	}
	
	public String getParent(String s){
		return this.parent;
	}
	
	public void setContent(String s){
		this.content = s;
	}
	
	public String getContent(){
		return this.content;
	}
	
	public void setChilds(ArrayList<Node> node_list){
		this.childs = node_list;
	}
	
	public ArrayList<Node> getChilds(){
		return this.childs;
	}
	
	public void addChild(Node n){
		childs.add(n);
	}
}
