package webservice;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class ProcessStep {
	@XmlElement
	public double BufferTimeMinutes;
	//@XmlElement
	public int ID;
	//@XmlElement
	public double JobTimeMinutes;
	//@XmlElement
	public int Order;
	//@XmlElement
	public double SetUpTimeMinutes;
	//@XmlElement
	public String Name;
	
	@XmlElementWrapper(name = "ConsumableMaterials")
    // XmlElement sets the name of the entities
  
    @XmlElement(name = "ConsumableMaterial")
		public List  <ConsumableMaterial> ConsumableMaterial = new ArrayList<ConsumableMaterial>();
	
	/*
	@XmlElement (name = "BufferTimeMinutes")
	public double getBufferTimeMinutes() {
		return BufferTimeMinutes;
	}
	public void setBufferTimeMinutes(double bufferTimeMinutes) {
		BufferTimeMinutes = bufferTimeMinutes;
	}
	@XmlElement (name = "ID")
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	@XmlElement (name = "JobTimeMinutes")
	public double getJobTimeMinutes() {
		return JobTimeMinutes;
	}
	public void setJobTimeMinutes(double jobTimeMinutes) {
		JobTimeMinutes = jobTimeMinutes;
	}
	@XmlElement (name = "Order")
	public int getOrder() {
		return Order;
	}
	public void setOrder(int order) {
		Order = order;
	}
	@XmlElement (name = "SetUpTimeMinutes")
	public double getSetUpTimeMinutes() {
		return SetUpTimeMinutes;
	}
	public void setSetUpTimeMinutes(double setUpTimeMinutes) {
		SetUpTimeMinutes = setUpTimeMinutes;
	}
	@XmlElement (name = "Name")
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	
*/
}
