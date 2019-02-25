package support_classes;

import java.util.Iterator;

import agentPro.onto.Timeslot;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

public class storage_element {
private String ID;
	
	private List hasSlots = new ArrayList();
	
	public storage_element(String workpiece_id) {
		this.setID(workpiece_id);
		
	}
	
	   public void addHasSlots(Timeslot elem) { 
	     List oldList = this.hasSlots;
	     hasSlots.add(elem);
	   }
	   public boolean removeHasSlots(Timeslot elem) {
	     List oldList = this.hasSlots;
	     boolean result = hasSlots.remove(elem);
	     return result;
	   }
	   public void clearAllHasSlots() {
	     List oldList = this.hasSlots;
	     hasSlots.clear();
	   }
	   public Iterator getAllHasSlots() {return hasSlots.iterator(); }
	   public List getConsistsOfAllocatedWorkingSteps() {return hasSlots; }
	   public void setConsistsOfAllocatedWorkingSteps(List l) {hasSlots = l; }
	   

	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
}
