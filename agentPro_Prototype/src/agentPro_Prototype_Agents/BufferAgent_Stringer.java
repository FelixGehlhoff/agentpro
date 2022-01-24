package agentPro_Prototype_Agents;

import java.util.ArrayList;

import agentPro.onto.Accept_Proposal;
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
	public ArrayList<Proposal> checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp) {
	
		
		//extract CFP Timeslot
		Timeslot cfp_timeslot = cfp.getHasTimeslot();	
		Operation operation = cfp.getHasOperation();
		
		ArrayList<Proposal> proposal_list = new ArrayList<Proposal>();
		Transport_Operation transport_op_to_destination = (Transport_Operation) operation;
		float price = 1;
		this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(String.valueOf(cfp_timeslot.getEndDate())); //time increment is reduced / put to the other busy interval later
		this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(String.valueOf(cfp_timeslot.getStartDate()));	
		this.getReceiveCFPBehav().timeslot_for_schedule.setLength(cfp_timeslot.getLength());
		
	Proposal proposal = createProposal(price, transport_op_to_destination, cfp_timeslot, cfp.getHasSender(), cfp.getID_String());
	proposal_list.add(proposal);
	return proposal_list;
	}
	@Override
	public Boolean bookIntoSchedule(Accept_Proposal accept_proposal) {
		Proposal prop = (Proposal) accept_proposal.getHasProposal().get(0);
		getWorkplan().addConsistsOfAllocatedWorkingSteps((AllocatedWorkingStep) prop.getConsistsOfAllocatedWorkingSteps().get(0));
		
		if(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>1) {
			this.setWorkplan(_Agent_Template.sortWorkplanChronologically(this.getWorkplan()));
		}
		return true;
		
	}
	

}
