package agentPro_Prototype_Agents;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Capability;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Shared_Resource;
import agentPro.onto.Timeslot;
import agentPro.onto.Transport_Operation;
import agentPro_Prototype_ResourceAgent.ReceiveCFPBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import support_classes.Interval;

/*
 * Models a shared resource. 
 */
public class Crane_RailAgent extends SharedResourceAgent{

	private static final long serialVersionUID = 1L;
	//private ReceiveCFPBehaviour ReceiveCFPBehav;
	

	protected void setup (){
		super.setup();
		//this.numberOfResourcesPossibleForCalculationOfSharedResourceProposal = 2; //TBD dynamisch
		logLinePrefix = 	logLinePrefix+"Crane_RailAgent";
		
		
	}


	@Override
	public boolean feasibilityCheckAndDetermineDurationParameters(Operation operation) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Proposal checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp) {
		
		Proposal proposal = new Proposal();
		Timeslot timeslot_for_proposal = new Timeslot();
		
		//extract CFP Timeslot
		Timeslot cfp_timeslot = cfp.getHasTimeslot();	
		long startdate_cfp = Long.parseLong(cfp_timeslot.getStartDate());
		long enddate_cfp = Long.parseLong(cfp_timeslot.getEndDate());
		Transport_Operation operation = (Transport_Operation) cfp.getHasOperation();
		
		//TBD real procedure
		operation.setBuffer_after_operation(3*60*60*1000);
		operation.setBuffer_before_operation(3*60*60*1000);
		
		/*
		long estimated_start_date = 0;
		long estimated_enddate = 0;
		long set_up_time = 0; //no set up time needed at this moment

		
		long enddate_interval = 0;
			//operation avg duration = 0 in case of buffer place
		if(operation.getAvg_Duration() == 0) {
			enddate_interval = enddate_cfp;
		}else {		//shared resource should get an operation with a duration from e.g. a transport resource
			enddate_interval = (long) (startdate_cfp+operation.getAvg_Duration()*60*1000);
			this.getReceiveCFPBehav().duration_for_price = set_up_time + operation.getAvg_Duration(); //in min
			//duration_for_price = set_up_time + operation.getAvg_Duration(); //in min
		}
		
		Interval timeslot_interval_to_be_booked_shared_resource = new Interval( startdate_cfp-set_up_time*60*1000, enddate_cfp, false);	
		
		for(int i = 0;i<getFree_interval_array().size();i++) {	
			if(getFree_interval_array().get(i).contains(timeslot_interval_to_be_booked_shared_resource)){
				//desired slot can be fulfilled
				estimated_start_date = startdate_cfp-set_up_time*60*1000;
				estimated_enddate = enddate_interval;	
				//determine how much time there is between this operation and the one before --> the workpiece can only arrive if the one before is already gone
				//	put that time in the allocated working step as Buffer
				this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier = estimated_start_date-getFree_interval_array().get(i).lowerBound(); 
				break;
			
			}else if(getFree_interval_array().get(i).getSize() >= timeslot_interval_to_be_booked_shared_resource.getSize()){
				//the desired slot is not fully in a free interval
				//check whether the slot can be postponed to a later interval (which has the correct size) but not the earliest start date
				//for(int j = 0;j<getFree_interval_array().size();j++) {
					//if(getFree_interval_array().get(j).getSize() >= timeslot_interval_to_be_booked_production.getSize()) {
					if(getFree_interval_array().get(i).lowerBound() >= timeslot_interval_to_be_booked_shared_resource.lowerBound()) {	
						estimated_start_date = getFree_interval_array().get(i).lowerBound();
						estimated_enddate = (long) (estimated_start_date+operation.getAvg_Duration()*60*1000+set_up_time*60*1000);	//start at the lower bound with the set up + duration = enddate
						this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier = 0; //because the lowerBound of a free Interval is taken --> before that is a busy interval
						break;
					}
					
				//}				
			}
			
		}	
		
		timeslot_for_proposal.setEndDate(Long.toString(estimated_enddate));
		timeslot_for_proposal.setStartDate(Long.toString(estimated_start_date+set_up_time*60*1000));	
		this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(Long.toString(estimated_enddate));
		this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(Long.toString(estimated_start_date));
		*/
		float price = operation.getAvg_Duration();	
		//this.getReceiveCFPBehav().duration_for_price = operation.getAvg_Duration(); //in min
		timeslot_for_proposal.setEndDate(Long.toString(enddate_cfp));
		timeslot_for_proposal.setStartDate(Long.toString(startdate_cfp));	
		
		proposal = createProposal(price, operation, timeslot_for_proposal, cfp.getHasSender(), "");	//cfp.getIDString() is empty in CFPs to production resources
		return proposal;
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
	protected void considerPickup(AllocatedWorkingStep allocatedWorkingStep) {
		// TODO Auto-generated method stub
		
	}


	@Override
	
		protected Resource createResource() {
			Resource res = new Resource();
			return res;
		}
	



} 

