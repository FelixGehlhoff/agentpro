package agentPro_Prototype_Agents;

import agentPro.onto.Accept_Proposal;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Production_Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Timeslot;
import agentPro.onto.Transport_Operation;
import support_classes.Interval;
import support_classes.Storage_element_slot;

public class BufferAgent_new extends ProductionResourceAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Interval range;
	
	protected void setup() {
		super.setup();
        Operation op = (Operation) this.getRepresentedResource().getHasCapability().getEnables().get(0);
        String [] split =		op.getName().split("_");
        String [] coordinates = split[1].split(";");
        
        range = new Interval(coordinates[0], coordinates[1], false);
	}
	
	@Override
	public boolean feasibilityCheckAndDetermineDurationParameters(Operation operation) {
		Location start = (Location) operation.getStartStateNeeded();
		Location end = (Location) operation.getEndState();	
		//System.out.println("DEBUG___"+this.getName()+" range "+range.toString()+" contains "+end.getCoordX()+" and contains "+ start.getCoordX());
		
		if(range.contains((long)start.getCoordX()) && range.contains((long)end.getCoordX())) {
			operation.setName(operation.getAppliedOn().getID_String()+"@"+this.getLocalName()+"_from_"+start.toString()+"_to_"+end.toString());
			return true;
		}else {
			return false;
		}	
	}
	@Override
	public double calculateDurationSetup(Interval free_interval, Operation operation) {
		return 0;
		
	}
	@Override
	public Proposal checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp) {
	
		
		//extract CFP Timeslot
		Timeslot cfp_timeslot = cfp.getHasTimeslot();	
		Operation operation = cfp.getHasOperation();
		operation.setType("buffer");
		Proposal proposal = new Proposal();
		Production_Operation buffer_operation = (Production_Operation) operation;
		float price = 1;
		buffer_operation.setBuffer_after_operation_end(10*60*60*1000);
		buffer_operation.setBuffer_after_operation_start(10*60*60*1000);
		buffer_operation.setBuffer_before_operation_start(10*60*60*1000);
		//buffer_operation.setName("buffer_"+this.getOfferNumber());
		//this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(String.valueOf(cfp_timeslot.getEndDate())); //time increment is reduced / put to the other busy interval later
		//this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(String.valueOf(cfp_timeslot.getStartDate()));	
		//this.getReceiveCFPBehav().timeslot_for_schedule.setLength(cfp_timeslot.getLength());

		Storage_element_slot slot = createStorageElement(operation, cfp_timeslot, (long) cfp.getHasOperation().getAvg_PickupTime()*60*1000, 0F);
		 this.getReceiveCFPBehav().getProposed_slots().add(slot);	
	proposal = createProposal(price, buffer_operation, cfp_timeslot, cfp.getHasSender(), cfp.getID_String());
	return proposal;
	}
	
	
	@Override
	public Boolean bookIntoSchedule(Accept_Proposal accept_proposal) {
		Proposal prop = (Proposal) accept_proposal.getHasProposal().get(0);
		int pick_up = ((AllocatedWorkingStep) prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getAvg_PickupTime();
		Long start_new = Long.parseLong(((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getStartDate())-pick_up*60*1000;
		Long end_new = Long.parseLong(((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getEndDate())+pick_up*60*1000;
		((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().setStartDate(Long.toString(start_new));
		((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().setEndDate(Long.toString(end_new));
		getWorkplan().addConsistsOfAllocatedWorkingSteps((AllocatedWorkingStep) prop.getConsistsOfAllocatedWorkingSteps().get(0));
		
		if(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>1) {
			this.setWorkplan(_Agent_Template.sortWorkplanChronologically(this.getWorkplan()));
		}
		return true;
		
	}
	

}
