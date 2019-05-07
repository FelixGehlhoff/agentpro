package support_classes;

import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Timeslot;
public class Storage_element_slot {
	
private String ID;
private Timeslot timeslot;
private long time_increment;
private float buffer_before;
private float duration_to_get_to_workpiece;
private float buffer_after;
private Proposal proposal;
private String type;
	
	public Storage_element_slot(Operation operation, Timeslot slot, float duration_to_get_to_workpiece, long increment) {
		this.setID(operation.getName());
		this.setTimeslot(slot);
		this.setTime_increment(increment);
		this.setBuffer_after(operation.getBuffer_after_operation());
		this.setBuffer_before(operation.getBuffer_before_operation());
		this.setDuration_to_get_to_workpiece(duration_to_get_to_workpiece);
		this.setType(operation.getType());
	}
	
	
	public float getDuration_to_get_to_workpiece() {
		return duration_to_get_to_workpiece;
	}
	

	public void setDuration_to_get_to_workpiece(float duration_to_get_to_workpiece) {
		this.duration_to_get_to_workpiece = duration_to_get_to_workpiece;
	}

	public Boolean checkNewTimeslot(Timeslot timeslot_new) {																		// new end = 12, old end = 11 + 1 (buffer)
		if(Long.parseLong(timeslot_new.getStartDate())>=Long.parseLong(timeslot.getStartDate())-buffer_before && Long.parseLong(timeslot_new.getEndDate())<=Long.parseLong(timeslot.getEndDate())+buffer_after){	//eg new start = 10 vorher 11-1(buffer before)
			return true;
		}else {
			return false;
		}
		
	}
	  
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}


	public Timeslot getTimeslot() {
		return timeslot;
	}


	public void setTimeslot(Timeslot timeslot) {
		this.timeslot = timeslot;
	}


	public long getTime_increment() {
		return time_increment;
	}


	public void setTime_increment(long time_increment) {
		this.time_increment = time_increment;
	}


	public Proposal getProposal() {
		return proposal;
	}


	public void setProposal(Proposal proposal) {
		this.proposal = proposal;
	}
	public float getBuffer_before() {
		return buffer_before;
	}


	public void setBuffer_before(float buffer_before) {
		this.buffer_before = buffer_before;
	}


	public float getBuffer_after() {
		return buffer_after;
	}


	public void setBuffer_after(float buffer_after) {
		this.buffer_after = buffer_after;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


}
