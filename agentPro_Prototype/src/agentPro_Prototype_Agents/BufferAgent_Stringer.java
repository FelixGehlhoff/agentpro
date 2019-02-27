package agentPro_Prototype_Agents;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Timeslot;
import agentPro.onto.Transport_Operation;

public class BufferAgent_Stringer extends TransportResourceAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public Proposal checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp) {
	
		
		//extract CFP Timeslot
		Timeslot cfp_timeslot = cfp.getHasTimeslot();	
		Operation operation = cfp.getHasOperation();
		
		Proposal proposal = new Proposal();
		Transport_Operation transport_op_to_destination = (Transport_Operation) operation;
		float price = 1;
		this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(String.valueOf(cfp_timeslot.getEndDate())); //time increment is reduced / put to the other busy interval later
		this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(String.valueOf(cfp_timeslot.getStartDate()));	
		this.getReceiveCFPBehav().timeslot_for_schedule.setLength(cfp_timeslot.getLength());
		
	proposal = createProposal(price, transport_op_to_destination, cfp_timeslot, cfp.getHasSender());
	return proposal;
	}
	@Override
	public Boolean bookIntoSchedule(AllocatedWorkingStep allocWorkingstep, float time_increment_or_decrement_to_be_added_for_setup_of_next_task) {
		getWorkplan().addConsistsOfAllocatedWorkingSteps(allocWorkingstep);
		
		if(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>1) {
			sortWorkplanChronologically();
		}
		return true;
		
	}
	

}
