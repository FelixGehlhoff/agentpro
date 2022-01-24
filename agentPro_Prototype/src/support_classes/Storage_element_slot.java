package support_classes;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.State;
import agentPro.onto.Timeslot;
import agentPro_Prototype_Agents.ResourceAgent;
public class Storage_element_slot {
	
private String ID;
private Timeslot timeslot;
private long time_increment;
private float buffer_before;
private float duration_setup;
private float buffer_after;
private Proposal proposal;
private String type;
private State endState;
private String allWS_id;
private int allWS_quantity;
private Resource res;

public Storage_element_slot() {
	
};

	public Storage_element_slot(Operation operation, Timeslot slot, float duration_to_get_to_workpiece, long increment, 
			String all_WS_id, int allWS_quantity, Resource resource) {
		Operation op = ResourceAgent.createOperationCopy(operation);
		this.setID(operation.getName());
		this.setTimeslot(slot);
		this.setTime_increment(increment);
		this.setBuffer_after(operation.getBuffer_after_operation_end());
		this.setBuffer_before(operation.getBuffer_before_operation_start());
		this.setDuration_to_get_to_workpiece(duration_to_get_to_workpiece);
		this.setType(operation.getType());
		this.setEndState(operation.getEndState());
		this.setOperation(op);
		this.setAllWS_id(all_WS_id);
		this.setAllWS_quantity(allWS_quantity);
		
		this.res = ResourceAgent.createResourceCopy(resource);
		
	}
	public Storage_element_slot(Operation operation2, Timeslot timeslot2, float duration_setup_time,
			long time_increment2) {
		
		Operation op = ResourceAgent.createOperationCopy(operation2);
		this.setOperation(op);
		this.setID(operation2.getName());
		this.setBuffer_after(operation2.getBuffer_after_operation_end());
		this.setBuffer_before(operation2.getBuffer_before_operation_start());
		this.setType(operation2.getType());
		this.setEndState(op.getEndState());
		Timeslot ts = new Timeslot();
		ts.setStartDate(timeslot2.getStartDate());
		ts.setEndDate(timeslot2.getEndDate());
		ts.setLength(timeslot2.getLength());
		this.setTimeslot(ts);
		this.setDuration_to_get_to_workpiece(duration_setup_time);
		this.setTime_increment(time_increment2);
		// TODO Auto-generated constructor stub
	}
	public State getEndState() {
	return endState;
}

private Operation operation;
	



	public Operation getOperation() {
	return operation;
}
public void setOperation(Operation operation) {
	this.operation = operation;
}
public void setEndState(State endState) {
	this.endState = endState;
}
	
	public float getDuration_to_get_to_workpiece() {
		return duration_setup;
	}
	

	public void setDuration_to_get_to_workpiece(float duration_to_get_to_workpiece) {
		this.duration_setup = duration_to_get_to_workpiece;
	}

	public Boolean checkNewTimeslot(AllocatedWorkingStep allWS) {																		// new end = 12, old end = 11 + 1 (buffer)
		Timeslot timeslot_new = allWS.getHasTimeslot();
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
	public String getAllWS_id() {
		return allWS_id;
	}
	public void setAllWS_id(String allWS_id) {
		this.allWS_id = allWS_id;
	}
	public int getAllWS_quantity() {
		return allWS_quantity;
	}
	public void setAllWS_quantity(int allWS_quantity) {
		this.allWS_quantity = allWS_quantity;
	}
	public Resource getRes() {
		return res;
	}
	public void setRes(Resource res) {
		this.res = res;
	}


}
