package webservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ActualProcessStep {
	
	//@XmlElement
	public String ActualEnd;
	//@XmlElement
	public String ActualStart;
	//@XmlElement
	public int ID;
	//@XmlElement
	public int Order;
	//@XmlElement
	public String PlannedEndDispatch;
	//@XmlElement
	public Date PlannedStart;
	public Date PlannedEnd;
	//@XmlElement
	public String PlannedStartDispatch;
	
	@XmlElementWrapper(name = "Ressource")
    // XmlElement sets the name of the entities 
    @XmlElement(name = "Resource")
	public List  <Resource> Ressource = new ArrayList<Resource>();
	
	/*
	//@XmlElement(name = "ActualEnd")
	public String getActualEnd() {
		return ActualEnd;
	}
	

	public void setActualEnd(String actualEnd) {
		ActualEnd = actualEnd;
	}
	@XmlElement(name = "ActualStart")
	public String getActualStart() {
		return ActualStart;
	}

	public void setActualStart(String actualStart) {
		ActualStart = actualStart;
	}
	@XmlElement (name = "ID")
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}
	@XmlElement(name = "Order")
	public int getOrder() {
		return Order;
	}

	public void setOrder(int order) {
		Order = order;
	}
	@XmlElement(name = "PlannedEndDispatch")
	public String getPlannedEndDispatch() {
		return PlannedEndDispatch;
	}

	public void setPlannedEndDispatch(String plannedEndDispatch) {
		PlannedEndDispatch = plannedEndDispatch;
	}
	@XmlElement(name = "PlannedStart")
	public String getPlannedStart() {
		return PlannedStart;
	}

	public void setPlannedStart(String plannedStart) {
		PlannedStart = plannedStart;
	}
	@XmlElement(name = "PlannedStartDispatch")
	public String getPlannedStartDispatch() {
		return PlannedStartDispatch;
	}

	public void setPlannedStartDispatch(String plannedStartDispatch) {
		PlannedStartDispatch = plannedStartDispatch;
	}
	@XmlElement(name = "Resource")
	public List<Resource> getRessource() {
		return Ressource;
	}

	public void setRessource(List<Resource> ressource) {
		Ressource = ressource;
	}
	
*/
}
