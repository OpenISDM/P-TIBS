package nctu.petlab.tibs.ws.bean;

import java.io.Serializable;

public class PersonInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3259052166333491533L;
	
	String name = "";
	int age = 0;
	String phone = null;
	String note = null;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
	@Override
	public String toString() {
		return "PersonInfo [name=" + name + ", age=" + age + ", phone=" + phone
				+ ", note=" + note + "]";
	}	
	
}
