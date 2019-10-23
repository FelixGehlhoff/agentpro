package webservice;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
//@XmlAccessorType(XmlAccessType.FIELD)
public class ActualProcess {
	//@XmlElement
	public int ID;
	//@XmlElement
	public String ActualStart;
	//@XmlElement
	public String ActualEnd;
	//@XmlElement
	public String PlannedStartEarliest;
	//@XmlElement
	public String PlannedEndLatest;
	
	@XmlElementWrapper(name = "ActualProcessSteps")
    // XmlElement sets the name of the entities
  
    @XmlElement(name = "ActualProcessStep")
		public List  <ActualProcessStep> ActualProcessSteps = new ArrayList<ActualProcessStep>();
	
	//@XmlElement
	public ProcessSchedule ProcessSchedule;
	/*
	//@XmlElement (name = "ID")
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	@XmlElement (name = "ActualStart")
	public String getActualStart() {
		return ActualStart;
	}
	public void setActualStart(String actualStart) {
		ActualStart = actualStart;
	}
	@XmlElement (name = "ActualEnd")
	public String getActualEnd() {
		return ActualEnd;
	}
	public void setActualEnd(String actualEnd) {
		ActualEnd = actualEnd;
	}
	@XmlElement (name = "PlannedStartEarliest")
	public String getPlannedStartEarliest() {
		return PlannedStartEarliest;
	}
	public void setPlannedStartEarliest(String plannedStart) {
		PlannedStartEarliest = plannedStart;
	}
	@XmlElement (name = "PlannedEndLatest")
	public String getPlannedEndLatest() {
		return PlannedEndLatest;
	}
	public void setPlannedEndLatest(String plannedEnd) {
		PlannedEndLatest = plannedEnd;
	}
	@XmlElement (name = "ProcessSchedule")
	public ProcessSchedule getProcessSchedule() {
		return ProcessSchedule;
	}
	public void setProcessSchedule(ProcessSchedule processSchedule) {
		ProcessSchedule = processSchedule;
	}
	@XmlElement (name = "ActualProcessSteps")
	public List<ActualProcessStep> getActualProcessSteps() {
		return ActualProcessSteps;
	}
	public void setActualProcessSteps(List<ActualProcessStep> actualProcessSteps) {
		ActualProcessSteps = actualProcessSteps;
	}
*/

}
