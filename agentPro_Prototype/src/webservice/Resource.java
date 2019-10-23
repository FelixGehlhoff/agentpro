package webservice;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Resource {
	//@XmlElement
		public int ID;
		//@XmlElement
		public String Name;
		//@XmlElement
		public int QuantityOfAllocations;
		//@XmlElement
		public int Status;
		
	public Resource() {
		
	}
	
	public Resource (String name) {
		this.Name = name;
		ID = 1;
		QuantityOfAllocations = 1;
		Status = 1;
	}
	/*
	
	@XmlElement (name = "ID")
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	@XmlElement (name = "Name")
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	@XmlElement (name = "QuantityOfAllocations")
	public int getQuantityOfAllocations() {
		return QuantityOfAllocations;
	}
	public void setQuantityOfAllocations(int quantityOfAllocations) {
		QuantityOfAllocations = quantityOfAllocations;
	}
	@XmlElement (name = "Status")
	public int getStatus() {
		return Status;
	}
	public void setStatus(int status) {
		Status = status;
	}
	*/
}
