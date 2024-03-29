package agentPro_Prototype_Agents;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Capability;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Shared_Resource;
import agentPro.onto.State;
import agentPro.onto.Timeslot;
import agentPro.onto.Transport_Operation;
import agentPro_Prototype_ResourceAgent.ReceiveCFPBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import support_classes.Interval;
import support_classes.Storage_element_slot;

/*
 * Models a shared resource. 
 */
public class OperatorAgent extends SharedResourceAgent{

	private static final long serialVersionUID = 1L;
	//private ReceiveCFPBehaviour ReceiveCFPBehav;
	

	protected void setup (){
		super.setup();
		//this.numberOfResourcesPossibleForCalculationOfSharedResourceProposal = 3; //TBD dynamisch
		logLinePrefix = "SharedResource_Operator_Agent.";
		
		
		    //representedResource = new Shared_Resource();
			//representedResource.setName(this.getLocalName());
			//receiveValuesFromDB(representedResource);
			//reply_by_time = 300;
							
		
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
		//ReceiveCFPBehav = new ReceiveCFPBehaviour(this);
        //addBehaviour(ReceiveCFPBehav);
	}

	@Override
	public boolean feasibilityCheckAndDetermineDurationParameters(Operation operation) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public ArrayList<Proposal> checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp) {
		
		ArrayList<Proposal> proposal_list = new ArrayList<Proposal>();
		//Timeslot timeslot_for_proposal = new Timeslot();
		long estimated_start_date = 0;
		long estimated_enddate = 0;
		long set_up_time = 0; //can be time to reach the crane
		float duration_for_price = 0;
		int deadline_not_met = 0;
		
		//extract CFP Timeslot
				Timeslot cfp_timeslot = cfp.getHasTimeslot();	
				Transport_Operation operation = (Transport_Operation) cfp.getHasOperation();
				long startdate_cfp = Long.parseLong(cfp_timeslot.getStartDate());
				long enddate_cfp = Long.parseLong(cfp_timeslot.getEndDate());
				
		long enddate_interval = 0;
		enddate_interval = enddate_cfp;
		duration_for_price = set_up_time + operation.getAvg_Duration();

	Interval timeslot_interval_to_be_booked_production = new Interval( startdate_cfp-(set_up_time*60*1000), enddate_interval, false);	
	this.printoutBusyIntervals();
	this.printoutFreeIntervals();
	
	for(int i = 0;i<getFree_interval_array().size();i++) {	
		if(getFree_interval_array().get(i).contains(timeslot_interval_to_be_booked_production)){
			//desired slot can be fulfilled
			estimated_start_date = startdate_cfp;
			estimated_enddate = enddate_interval;	
			//determine how much time there is between this operation and the one before 
			//this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier = timeslot_interval_to_be_booked_production.lowerBound()-getFree_interval_array().get(i).lowerBound(); 
			operation.setBuffer_before_operation_start(timeslot_interval_to_be_booked_production.lowerBound()-getFree_interval_array().get(i).lowerBound());
			operation.setBuffer_after_operation_end(getFree_interval_array().get(i).upperBound()-timeslot_interval_to_be_booked_production.upperBound());
			break;
		
		}else if(getFree_interval_array().get(i).getSize() >= timeslot_interval_to_be_booked_production.getSize()){
			//the desired slot is not fully in a free interval
			//check whether the slot can be postponed to a later interval (which has the correct size) but not the earliest start date

				if(getFree_interval_array().get(i).lowerBound() >= timeslot_interval_to_be_booked_production.lowerBound()) {	
					
					estimated_start_date = getFree_interval_array().get(i).lowerBound();
					estimated_enddate =  estimated_start_date+(long)(operation.getAvg_Duration()*60*1000+set_up_time*60*1000);	//start at the lower bound with the set up + duration = enddate
					timeslot_interval_to_be_booked_production = new Interval(estimated_start_date, estimated_enddate);
					
					//this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier = 0; //because the lowerBound of a free Interval is taken --> before that is a busy interval
					//System.out.println("DEBUG_______________i "+i+" getFree_interval_array().get(i).lowerBound()"+getFree_interval_array().get(i).lowerBound());
					deadline_not_met = 1000;
					operation.setBuffer_before_operation_start(0);
					operation.setBuffer_after_operation_end(getFree_interval_array().get(i).upperBound()-timeslot_interval_to_be_booked_production.upperBound());
					
					
					float price = duration_for_price + deadline_not_met;		//strafkosten, wenn deadline_not_met
					Timeslot timeslot_for_proposal = new Timeslot();
					timeslot_for_proposal.setEndDate(Long.toString(estimated_enddate));
					timeslot_for_proposal.setStartDate(Long.toString(estimated_start_date));	
					
					Proposal proposal = createProposal(price, operation, timeslot_for_proposal, cfp.getHasSender(), "", 0);	//cfp.getIDString() is empty in CFPs to production resources
				
					proposal_list.add(proposal);
					break;
				}
				
			//}				
		}
		
	}	

		
		return proposal_list;
	}

	@Override
	protected void setStartState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Operation setStateAndOperation(ResultSet rs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void considerPickup(AllocatedWorkingStep allocatedWorkingStep, Storage_element_slot slot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Resource createResource() {
		Resource res = new Resource();
		return res;
	}

	@Override
	public float calculateTimeBetweenStates(State start_next_task, State end_new, long end_of_free_interval) {
		// TODO Auto-generated method stub
		return 0;
	}


} 

