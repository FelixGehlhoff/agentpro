package webservice;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class ProcessSchedule {
	//@XmlElement
	public int ID;

	//@XmlElement
	public String Name;
	
	@XmlElementWrapper(name = "ProcessSteps")
    // XmlElement sets the name of the entities
  
    @XmlElement(name = "ProcessStep")
		public List  <ProcessStep> ProcessSteps = new ArrayList<ProcessStep>();
	
	/*
	@XmlElement (name = "ID")
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}
	@XmlElement(name = "Name")
	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}
	@XmlElement(name = "ProcessSteps")
	public List<ProcessStep> getProcessSteps() {
		return ProcessSteps;
	}

	public void setProcessSteps(List<ProcessStep> processSteps) {
		ProcessSteps = processSteps;
	}*/
}
