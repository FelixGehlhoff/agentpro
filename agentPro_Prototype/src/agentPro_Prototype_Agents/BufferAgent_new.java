package agentPro_Prototype_Agents;

import java.util.ArrayList;

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
	/*
	@Override
	public ArrayList<Proposal> checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp) {
	
		
		//extract CFP Timeslot
		Timeslot cfp_timeslot = cfp.getHasTimeslot();	
		Operation operation = cfp.getHasOperation();
		operation.setType("buffer");
		ArrayList<Proposal> proposal_list = new ArrayList<Proposal>();
		Production_Operation buffer_operation = (Production_Operation) operation;
		float price = 1;
		//24.01.2022 Puffer werden erstmal nicht gesetzt
		
		//buffer_operation.setBuffer_after_operation_end(10*60*60*1000);
		//buffer_operation.setBuffer_after_operation_start(10*60*60*1000);
		//buffer_operation.setBuffer_before_operation_start(10*60*60*1000);
		//buffer_operation.setName("buffer_"+this.getOfferNumber());
		//this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(String.valueOf(cfp_timeslot.getEndDate())); //time increment is reduced / put to the other busy interval later
		//this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(String.valueOf(cfp_timeslot.getStartDate()));	
		//this.getReceiveCFPBehav().timeslot_for_schedule.setLength(cfp_timeslot.getLength());

		Storage_element_slot slot = createStorageElement(operation, cfp_timeslot, (long) cfp.getHasOperation().getAvg_PickupTime()*60*1000, 0F);		
	Proposal proposal = createProposal(price, buffer_operation, cfp_timeslot, cfp.getHasSender(), cfp.getID_String());
	slot.setProposal(proposal);
	this.getReceiveCFPBehav().getProposed_slots().add(slot);
	proposal_list.add(proposal);
	return proposal_list;
	}*/
	
	@Override
	public ArrayList<Proposal> checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp) {

		//Proposal proposal = new Proposal();
		ArrayList<Proposal> proposal_list = new ArrayList<Proposal>();
		//Timeslot timeslot_for_proposal = new Timeslot();
		long estimated_start_date = 0;


		int deadline_not_met = 0;
		float duration_total_for_price = 0;
		//float duration_for_answering_CFP_so_for_Workpiece_schedule = 0;
		
		//extract CFP Timeslot
				Timeslot cfp_timeslot = cfp.getHasTimeslot();	
				
				
				long startdate_cfp = Long.parseLong(cfp_timeslot.getStartDate());
				long enddate_cfp = Long.parseLong(cfp_timeslot.getEndDate());
				
				
				
				
				float time_increment_or_decrement_to_be_added_for_setup_of_next_task = 0; //TODO integrieren
		//long enddate_interval = 0;			

		//ArrayList<Interval> listOfIntervals = new ArrayList<Interval>();
	//System.out.println("DEBUG_______productionResourceAgent  timeslot_interval_to_be_booked_production "+timeslot_interval_to_be_booked_production.getSize()/(60*1000));
		
		boolean slot_found = false;
		double duration_setup = 0;
		long setup_and_pickup_to_consider = 0;
		//Storage_element_slot slot = null;
		
		for(int i = 0;i<getFree_interval_array().size();i++) {	
			Production_Operation operation = createOperationCopy((Production_Operation)cfp.getHasOperation());
			operation.setType("buffer");
			long buffer_before_operation_end = (long) operation.getBuffer_before_operation_end();
			long buffer_before_operation_start = (long) operation.getBuffer_before_operation_start();
			long buffer_after_operation_end = (long) operation.getBuffer_after_operation_end();
			long buffer_after_operation_start = (long) operation.getBuffer_after_operation_start();
			//long earliest_finish_date_from_arrive_at_resource = enddate_cfp-buffer_before_operation_end;
			Long length = Long.parseLong(cfp_timeslot.getEndDate())-Long.parseLong(cfp_timeslot.getStartDate());
			cfp_timeslot.setLength(length.floatValue());
			float duration_eff = cfp_timeslot.getLength()/(60*1000);
			operation.setAvg_Duration(duration_eff);
			Boolean slot_found_this_FI = false;
			//dependent parameters
			duration_setup = 0;	// in min
			time_increment_or_decrement_to_be_added_for_setup_of_next_task = 0; // in min    null is detailed operation description (ony needed for transport)

			//operation.setSet_up_time((float)setup_and_pickup_to_consider);
			operation.setSet_up_time((float)duration_setup);
			operation.setAvg_PickupTime(avg_pickUp);
			//System.out.println("DEBUG_________duration_setup "+duration_setup+" setup_and_pickup_to_consider "+setup_and_pickup_to_consider);
			float buffer = 0;
			duration_total_for_price = (float) setup_and_pickup_to_consider + duration_eff + buffer + time_increment_or_decrement_to_be_added_for_setup_of_next_task;	// min
			//duration_for_answering_CFP_so_for_Workpiece_schedule 	=  	 duration_eff + buffer;

			
			if(operation.getAvg_Duration() == 0) { //operation avg duration = 0 in case of buffer place
				System.out.println("DEBUG_________________"+this.getLocalName()+" PRODUCTION AGENT check schedule and determine timeslot --> operation == 0 --> should not be needed");
				//enddate_interval = enddate_cfp;
			}else {
				//enddate_interval =  startdate_cfp+(long) (operation.getAvg_Duration()*60*1000);	
				//duration_for_price = (float) duration_setup + operation.getAvg_Duration();
			//this.getReceiveCFPBehav().duration_for_price = set_up_time + operation.getAvg_Duration(); //in min
			}
			
			//Interval timeslot_interval_to_be_booked_production = new Interval( startdate_cfp-(Math.max((long)duration_setup, avg_pickUp))*60*1000, enddate_interval+avg_pickUp*60*1000, false);	//18.06. pick-up added

			//calculate possible slots within this free interval (FI)
			//the enddate of the cfp is quite far in the future. Thus, it does not mark a constrained in the same sense as it does for transports
			// the process can be finised much earlier than the enddate of the CFP, e.g. at the start of the cfp
			ArrayList<Interval> listOfIntervals = calculateIntervals(startdate_cfp, enddate_cfp, (long)(setup_and_pickup_to_consider*60*1000), (long)(duration_eff*60*1000), (long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000), (enddate_cfp-startdate_cfp), i); //0 = buffer that production can start earlier
			//System.out.println(this.printoutArraylistIntervals(listOfIntervals));
			 checkFeasibilityNew(listOfIntervals, startdate_cfp, buffer_before_operation_start, buffer_after_operation_start, "start");
			 checkFeasibilityNew(listOfIntervals, enddate_cfp, buffer_before_operation_end, buffer_after_operation_end, "end");
			 //checkFeasibility(listOfIntervals, startdate_cfp, enddate_cfp, (enddate_cfp-startdate_cfp)); //no buffer that production can start earlier
			 //System.out.println(this.printoutArraylistIntervals(listOfIntervals));
			
			 //checks the schedule --> LB and UB violated?
			 checkSchedule(listOfIntervals, (long)(setup_and_pickup_to_consider*60*1000), (long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000), i);
			// System.out.println(this.printoutArraylistIntervals(listOfIntervals));
			 
			 if(listOfIntervals.size()>0) {
				 slot_found = true;
				 slot_found_this_FI = true;
				 //sort earliest end first
				 this.sortArrayListIntervalsEarliestFirst(listOfIntervals, "end");
				 //now the best slot is found --> calculate buffers
				operation.setBuffer_before_operation_start((listOfIntervals.get(0).lowerBound()-(long)(setup_and_pickup_to_consider*60*1000))-getFree_interval_array().get(i).lowerBound());			 			
				operation.setBuffer_before_operation_end(operation.getBuffer_before_operation_start());
				operation.setBuffer_after_operation_end((getFree_interval_array().get(i).upperBound()-(long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000))-listOfIntervals.get(0).upperBound());
				operation.setBuffer_after_operation_start(operation.getBuffer_after_operation_end());
				
				
				if(listOfIntervals.get(0).upperBound()>enddate_cfp) {
					 deadline_not_met = 10000; 
				 }
				//System.out.println("DEBUG_prod res agent buffer before operation: "+operation.getBuffer_before_operation()+" buffer after operation: "+operation.getBuffer_after_operation()+" free int: "+getFree_interval_array().get(i).toString()+" work int: "+listOfIntervals.get(0).toString()+" setup/pickup: "+setup_and_pickup_to_consider+" time increment "+time_increment_or_decrement_to_be_added_for_setup_of_next_task);
				Timeslot timeslot_for_proposal = new Timeslot();
				timeslot_for_proposal.setEndDate(String.valueOf(listOfIntervals.get(0).upperBound()));
				timeslot_for_proposal.setStartDate(String.valueOf(listOfIntervals.get(0).lowerBound()));	
				timeslot_for_proposal.setLength(listOfIntervals.get(0).upperBound()-listOfIntervals.get(0).lowerBound());
				
				if(timeslot_for_proposal.getLength() == 0) {
					System.out.println("DEBUG_________ERROR___"+this.getLocalName()+" timeslot for proposal length = 0 in ProductionAgent L 450");	
					
					}
				if(slot_found_this_FI) {
					Storage_element_slot slot = new Storage_element_slot();
					slot = createStorageElement(operation, timeslot_for_proposal, setup_and_pickup_to_consider*60*1000, (long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000));
					float price = duration_total_for_price + deadline_not_met;
					Proposal proposal = createProposal(price, operation, timeslot_for_proposal, cfp.getHasSender(), cfp.getID_String(), 0);						
					slot.setProposal(proposal);
					this.getReceiveCFPBehav().getProposed_slots().add(slot);
					proposal_list.add(proposal);	
				}
				
				 //TBD Buffer
				 break;
				 
			 }
		}

				if(!slot_found) {
					//if(startdate_cfp+(long)(duration_for_answering_CFP_so_for_Workpiece_schedule*60*1000)>=earliest_finish_date_from_arrive_at_resource) {
						//System.out.println("DEBUG_______WRONG DATA FROM CFP --> startdate_cfp+(long)(duration_for_answering_CFP_so_for_Workpiece_schedule*60*1000)  "+startdate_cfp+(long)(duration_for_answering_CFP_so_for_Workpiece_schedule*60*1000)+" >= "+earliest_finish_date_from_arrive_at_resource+" earliest_finish_date_from_arrive_at_resource    is violated");
					//}
					System.out.println("DEBUG----------"+"NO FREE SLOT FOUND ---> this should not happen   printout   	"+this.getLocalName());
				}else {					

		}
				return proposal_list;
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
