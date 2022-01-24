package backup;

import java.util.ArrayList;
import java.util.Date;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.DetailedOperationDescription;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Timeslot;
import agentPro.onto.TransportResource;
import agentPro.onto.Transport_Operation;
import agentPro_Prototype_Agents.ResourceAgent;
import agentPro_Prototype_Agents.TransportResourceAgent;
import agentPro_Prototype_ResourceAgent.ReceiveOrderBehaviour;
import agentPro_Prototype_ResourceAgent.RequestPerformer_Resource;
import jade.core.behaviours.Behaviour;
import support_classes.Interval;
import support_classes.Run_Configuration;

public class WaitForSharedResourcesBehaviour extends Behaviour{

	private static final long serialVersionUID = 1L;
	private Boolean sharedResourcesStillToBeConsidered = false;
	private TransportResourceAgent myAgent;
	public Boolean [] shared_resource_asked;
	public Boolean [] shared_resource_available_PROPOSAL;
	public Boolean [] shared_resource_available_INFORMSCHED;
	private int step = 0;
	private String conversationID;
	private Proposal proposal;
	private Timeslot timeslot_for_schedule;
	private Timeslot timeslot_for_proposal;
	private float time_increment_or_decrement_to_be_added_for_setup_of_next_task;
	//private Timeslot timeslot_for_proposal;
	private Transport_Operation requested_operation;
	//public long reply_by_time = 150;
	private long blocking_date = 0;
	private ArrayList <String> sender = new ArrayList<String>();
	private CFP cfp;
	
	public WaitForSharedResourcesBehaviour(ResourceAgent myAgent, Operation requested_operation, String conversationID, Proposal proposal, Timeslot timeslot_for_schedule, float time_increment_or_decrement_to_be_added_for_setup_of_next_task, ArrayList<String> sender, CFP cfp) {
		super(myAgent);
		this.myAgent = (TransportResourceAgent ) myAgent;
		this.conversationID = conversationID;
		this.proposal = proposal;
		this.timeslot_for_proposal = ((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot();
		this.timeslot_for_schedule = timeslot_for_schedule;
		this.time_increment_or_decrement_to_be_added_for_setup_of_next_task = time_increment_or_decrement_to_be_added_for_setup_of_next_task;
		//this.timeslot_for_proposal = timeslot_for_proposal;
		this.requested_operation = (Transport_Operation) requested_operation;
		this.sender.add(sender.get(0));
		this.cfp = cfp;
	}
	@Override
	public void action() {
		switch(step) {
		case 0:
			if(!sharedResourcesStillToBeConsidered) {
				shared_resource_asked = new Boolean [myAgent.getNeeded_shared_resources().size()];
				shared_resource_available_INFORMSCHED = new Boolean [myAgent.getNeeded_shared_resources().size()];
					for(int i = 0; i<myAgent.getNeeded_shared_resources().size();i++) {
						shared_resource_asked[i] = false;
						shared_resource_available_INFORMSCHED[i]=false;
					}
				shared_resource_available_PROPOSAL = new Boolean [myAgent.getNeeded_shared_resources().size()];
				Boolean test = null;
				Proposal [] shared_resource__PROPOSALS = new Proposal[myAgent.getNeeded_shared_resources().size()];
				this.getDataStore().put(0, shared_resource_asked);
				this.getDataStore().put(1, shared_resource_available_PROPOSAL);			
				this.getDataStore().put(2, shared_resource_available_INFORMSCHED);
				this.getDataStore().put(3, test);
				this.getDataStore().put(4, shared_resource__PROPOSALS);
				
				int i = 0;
				for(String shared_resource_capability_Name : myAgent.getNeeded_shared_resources()) {
					//shared_resource_asked[i] = false;	
					RequestPerformer_Resource rp = new RequestPerformer_Resource(myAgent, shared_resource_capability_Name, timeslot_for_schedule, false, i, requested_operation, myAgent.reply_by_time_shared_resources);
					rp.setDataStore(this.getDataStore());
					 myAgent.addBehaviour(rp);
					 //System.out.println("DEBUG_____SHARED RESOURCE_______new Req_Performer_Resource added for Resource 	"+shared_resource_capability_Name);
					 i++;
				}
				sharedResourcesStillToBeConsidered = true;
				//blocking_date = System.currentTimeMillis()+(long)0.1*myAgent.reply_by_time;
				block();
				break;
			}
			
			//wait until the shared resources have been booked	
			//System.out.println("DEBUG_____"+myAgent.getLocalName()+".WaitForSharedResources______"+System.currentTimeMillis()+" <= "+blocking_date);
			//if(System.currentTimeMillis()<blocking_date) {
				//this.block(10);

			//}else {
				
				for(int j = 0;j<myAgent.getNeeded_shared_resources().size();j++) {
					//System.out.println("DEBUG___myAgent.getNeeded_shared_resources().size() "+myAgent.getNeeded_shared_resources().size()+" ____shared_resource_asked[j]_____j = _"+j+" "+shared_resource_asked[j]);
					shared_resource_asked = (Boolean[]) this.getDataStore().get(0);
					shared_resource_available_PROPOSAL = (Boolean[]) this.getDataStore().get(1);
					//System.out.println(System.currentTimeMillis()+" DEBUG______shared_resource_asked [j]_"+shared_resource_asked[j]);
					if(shared_resource_asked[j] == false) {	
						this.block(5);			
						break;//leave for loop and start again
					} else if(j == myAgent.getNeeded_shared_resources().size()-1) {
						for(int k = 0;k<myAgent.getNeeded_shared_resources().size();k++) {
							//System.out.println("DEBUG_______shared_resource_asked[j]______j = _"+j+" "+shared_resource_asked[j]+" shared_resource_available[k] "+shared_resource_available_PROPOSAL[k]+" k = "+k);
							if(shared_resource_available_PROPOSAL[k] != null && shared_resource_available_PROPOSAL[k] == false) {
								System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.logLinePrefix+" not all shared Resources available --> make no offer for proposal "+proposal.getID_Number());
								step = 4;	//stop procedure? TBD
								sharedResourcesStillToBeConsidered = false;
								break;
							//}else if(j == myAgent.getNeeded_shared_resources().size()-1) {
								}else if(k == myAgent.getNeeded_shared_resources().size()-1) {
									//all resources made an offer
									Boolean intersection_ok = checkProposalIntersection(); //TODO: 28.12.19: Checks the exact start and end date --> better: check "contains"
								if(intersection_ok) {
									step = 1; //needs booking
									sharedResourcesStillToBeConsidered = false;
									System.out.println(System.currentTimeMillis()+" "+myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.logLinePrefix+" ALL shared Resources available");							

							        break;
								}else {
									System.out.println(System.currentTimeMillis()+" DEBUG______ABCD____________   "+myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.logLinePrefix+" not all resources perfectly available");							

									//can be if possible of all (incl. transport ress.) if not, only shared ress									
									Timeslot best_available_timeslot = determineBestTimeslotFromSharedResources();	
									
									//TBD does that make sense?
									if(Long.parseLong(best_available_timeslot.getEndDate()) <= Long.parseLong(timeslot_for_schedule.getEndDate())+requested_operation.getBuffer_after_operation_end() && Long.parseLong(best_available_timeslot.getStartDate()) >= Long.parseLong(timeslot_for_schedule.getStartDate())+requested_operation.getBuffer_before_operation_start()) {
										System.out.println("DEBUG_________TIMESLOT "+best_available_timeslot.getStartDate()+","+best_available_timeslot.getEndDate()+" is apropriate because end is <= "+(Long.parseLong(timeslot_for_schedule.getEndDate())+requested_operation.getBuffer_before_operation_start()));
										((AllocatedWorkingStep)proposal.getAllConsistsOfAllocatedWorkingSteps().next()).setHasTimeslot(best_available_timeslot);
										timeslot_for_schedule = best_available_timeslot; //new 27.06.: use this timeslot to book intervals
										long startdate = Long.parseLong(timeslot_for_schedule.getStartDate()) - (long) (requested_operation.getSet_up_time()*60*1000);
										long enddate = Long.parseLong(timeslot_for_schedule.getEndDate()) - (long) (time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000);
										timeslot_for_proposal.setStartDate(String.valueOf(startdate));
										timeslot_for_proposal.setEndDate(String.valueOf(enddate));
										
										step = 1; //still needs booking
										break;
									}else {
										AllocatedWorkingStep for_schedule = (AllocatedWorkingStep) proposal.getConsistsOfAllocatedWorkingSteps().get(0);	
										//for_schedule.setHasTimeslot(best_available_timeslot);	
											//check if that slots enables to make an offer --> if a free interval contains the operation duration 
										Boolean suggestion_feasible = checkContainsInSchedule(for_schedule, best_available_timeslot, time_increment_or_decrement_to_be_added_for_setup_of_next_task); //TBD assumed for now that the increment doesnt change
										if(suggestion_feasible) {
											System.out.println(System.currentTimeMillis()+" DEBUG  "+myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.logLinePrefix+"  make a proposal with slot for proposal "+timeslot_for_proposal.getStartDate()+";"+timeslot_for_proposal.getEndDate()+" for schedule "+timeslot_for_schedule.getStartDate()+";"+timeslot_for_schedule.getEndDate());							
												//timeslot_for_schedule & proposal are determined in checkContainsInSchedule
											//timeslot_for_schedule = best_available_timeslot; //new: use this timeslot to book intervals
											//long startdate = Long.parseLong(timeslot_for_schedule.getStartDate()) - (long) (requested_operation.getSet_up_time()*60*1000);
											//long enddate = Long.parseLong(timeslot_for_schedule.getEndDate()) - (long) (time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000);
											//timeslot_for_proposal.setStartDate(String.valueOf(startdate));
											//timeslot_for_proposal.setEndDate(String.valueOf(enddate));
											step = 1;	//needs booking
											break;
										}else {
											System.out.println(System.currentTimeMillis()+" DEBUG "+myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.logLinePrefix+" not all shared Resources available --> make no offer for proposal "+proposal.getID_Number()+" _____Case 2 (all offers are there but do not match critera");							
											myAgent.sendRefusal(conversationID, sender);
											step = 4;
											break;
										}
										
										
										/*
										 * 										//try to find a slot for the new proposal   --> This probably does also not make sense --> a completely new slot will not match 
										//the other shared ressource and probably not  any shared res. proposal
											//maybe its possible just to break the CFP restriction
										cfp.setHasTimeslot(best_available_timeslot);	
										Transport_Operation op = requested_operation;
										op.setBuffer_after_operation(0);
										op.setBuffer_before_operation(0);
										cfp.setHasOperation(op);
										proposal = myAgent.checkScheduleDetermineTimeslotAndCreateProposal(cfp); //tbd if that method can be reused
										
										if(proposal != null) {
											float price = proposal.getPrice()+1000;
											proposal.setPrice(price);
											System.out.println(System.currentTimeMillis()+" DEBUG  "+myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.logLinePrefix+"  make a proposal ");							
											timeslot_for_schedule = best_available_timeslot; //new: use this timeslot to book intervals
											step = 1;
											break;
										}else {
											System.out.println(System.currentTimeMillis()+" DEBUG "+myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.logLinePrefix+" not all shared Resources available --> make no offer for proposal "+proposal.getID_Number()+" _____Case 2 (all offers are there but do not match critera");							
											step = 2;
											break;
										}	*/								
									}
							
									
									
								}
								

							}					
						}					
					}
				}
			//}

			break;
		case 1:
			//Here must be a check if the proposal meets the requirements --> TBD
			
			
			//send proposal to Workpiece Agent
			myAgent.sendProposal(proposal, conversationID, sender, null);
			
			//as a reservation the slot is booked into the schedule
			
			AllocatedWorkingStep for_schedule = (AllocatedWorkingStep) proposal.getConsistsOfAllocatedWorkingSteps().get(0);	
			for_schedule.setHasTimeslot(timeslot_for_schedule);	
		//ausgegraut 27.09.19 um Fehler weg zu machen
		//	Boolean bool = 	myAgent.bookIntoSchedule(for_schedule, time_increment_or_decrement_to_be_added_for_setup_of_next_task);
		//	if(!bool) {
		//		System.out.println("ERROR____________Wait For shared res   "+myAgent.getLocalName()+"   step could not be added");
		//	}
			
			// / ADD BEHAVIOURS
	        // /////////////////////////////////////////////////////////
			
			//ReceiveOrderBehaviour rob = new ReceiveOrderBehaviour(myAgent, conversationID, myAgent.reply_by_time, System.currentTimeMillis()+myAgent.reply_by_time, proposal.getID_Number());
			//rob.setDataStore(this.getDataStore());				
			//myAgent.addBehaviour(rob);		
							 	
	        //myAgent.setOfferNumber(myAgent.getOfferNumber()+1);
	        step = 3;	
	        break;
		case 2:
			//send proposal to Workpiece Agent
			//((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).setHasTimeslot(timeslot_for_proposal);
			myAgent.sendProposal(proposal, conversationID, sender, null);
			
			//as a reservation the slot is booked into the schedule
			
			//AllocatedWorkingStep for_schedule = (AllocatedWorkingStep) proposal.getConsistsOfAllocatedWorkingSteps().get(0);	
			//for_schedule.setHasTimeslot(timeslot_for_schedule);	
		
			//myAgent.getReceiveCFPBehav().bookIntoSchedule(for_schedule, time_increment_or_decrement_to_be_added_for_setup_of_next_task);
			
			// / ADD BEHAVIOURS
	        // /////////////////////////////////////////////////////////
			
			//ReceiveOrderBehaviour rob = new ReceiveOrderBehaviour(myAgent, conversationID, myAgent.reply_by_time, System.currentTimeMillis()+myAgent.reply_by_time, proposal.getID_Number());
			//rob.setDataStore(this.getDataStore());				
			//myAgent.addBehaviour(rob);		
							 	
	       // myAgent.setOfferNumber(myAgent.getOfferNumber()+1);
	        step = 3;	
	        break;
		case 3:
			// / ADD BEHAVIOURS
	        // /////////////////////////////////////////////////////////
			
			ReceiveOrderBehaviour rob = new ReceiveOrderBehaviour(myAgent, conversationID, Run_Configuration.reply_by_time_resource_agent, System.currentTimeMillis()+Run_Configuration.reply_by_time_resource_agent, proposal.getID_Number());
			rob.setDataStore(this.getDataStore());				
			myAgent.addBehaviour(rob);		
							 	
	        myAgent.setOfferNumber(myAgent.getOfferNumber()+1);
	        step = 4;	
			
	        
		}

	
		
	}
	//similar to book checkScheduleDetermineTimeslotAndCreate Proposal
	
	private Boolean checkContainsInSchedule(AllocatedWorkingStep allWS,	Timeslot best_available_timeslot, float time_increment_or_decrement_to_be_added_for_setup_of_next_task2) {
		Boolean successful = false;
		
		//extract CFP Timeslot
		Timeslot cfp_timeslot = allWS.getHasTimeslot();	
		Operation operation = allWS.getHasOperation();
		long startdate_cfp = Long.parseLong(best_available_timeslot.getStartDate());
		long enddate_cfp = Long.parseLong(best_available_timeslot.getEndDate());
		
		long estimated_start_date = 0;
		long estimated_enddate = 0;
		int deadline_not_met = 1000;
		
		float duration_total_for_schedule = 0;
		float duration_for_answering_CFP_so_for_Workpiece_schedule = 0;
		float duration_to_get_to_workpiece = 0;
		float avg_speed = 0;
		//float set_up_time = 0;
		float traveling_time = 0;
		//float avg_Pickup_time = 0;
		float time_increment_or_decrement_to_be_added_for_setup_of_next_task = 0;
		Location start_old_idle = new Location();
		Location start_new = new Location();
		
		TransportResource tR = (TransportResource) myAgent.getRepresentedResource();
		float avg_Pickup_time = tR.getAvg_PickupTime();	//stored in min 
		avg_speed = tR.getAvg_Speed();					//stored in m/s

		Transport_Operation transport_op_to_destination = (Transport_Operation) operation;
		long buffer_time_that_production_can_start_earlier = (long) transport_op_to_destination.getBuffer_before_operation_start();
				
		boolean slot_found = false;
		String printout = "";
		long buffer_after_operation = 0;
		long buffer_before_operation = 0;
	for(int i = 0;i<myAgent.getFree_interval_array().size();i++) {	
		
				start_old_idle = (Location) myAgent.getStateAtTime(myAgent.getFree_interval_array().get(i).lowerBound());			
				start_new = (Location) transport_op_to_destination.getStartState();
			float distance_TransportResource_to_Workpiece = myAgent.calcDistance(start_old_idle, start_new);			
		duration_to_get_to_workpiece = (distance_TransportResource_to_Workpiece/avg_speed) / 60 ;	// in min
		//System.out.println("DEBUG_________________________________duration_to_get_to_workpiece_=  distance_TransportResource_to_Workpiece "+distance_TransportResource_to_Workpiece+"/ avg_speed "+avg_speed);
			
		//set_up_time = distance_TransportResource_to_Workpiece/avg_speed;
					
			float distance_Workpiece_to_ProductionResource = myAgent.calcDistance((Location)transport_op_to_destination.getStartState(), (Location)transport_op_to_destination.getEndState());	//in m
		
		traveling_time = (distance_Workpiece_to_ProductionResource/avg_speed)/60; // in min
				//time_increment_or_decrement_to_be_added_for_setup_of_next_task = calculateTimeIncrement(transport_op_to_destination, avg_speed, i, operation_description); // in min
				//myAgent.getReceiveCFPBehav().time_increment_or_decrement_to_be_added_for_setup_of_next_task = time_increment_or_decrement_to_be_added_for_setup_of_next_task;
				
			duration_total_for_schedule 							= duration_to_get_to_workpiece + 2*avg_Pickup_time + traveling_time + myAgent.buffer + time_increment_or_decrement_to_be_added_for_setup_of_next_task;	// min
			//System.out.println("DEBUG________duration_total_for_schedule = "+duration_total_for_schedule+"_____duration_to_get_to_workpiece  "+duration_to_get_to_workpiece + "   traveling_time   "+traveling_time+ "   2*avg_Pickup_time   "+2*avg_Pickup_time+" time_increment_or_decrement_to_be_added  "+time_increment_or_decrement_to_be_added_for_setup_of_next_task+ " + buffer "+buffer);
			duration_for_answering_CFP_so_for_Workpiece_schedule 	=  								 2*avg_Pickup_time + traveling_time + myAgent.buffer;
			//this.getReceiveCFPBehav().duration_for_price = duration_total_for_schedule; //in min
		
			long latest_start_date = enddate_cfp - (long) (duration_total_for_schedule*60*1000 - time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000);
			Interval timeslot_interval_to_be_booked_transport_desired = new Interval(latest_start_date, enddate_cfp+(long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000), false);
			Interval timeslot_interval_to_be_booked_transport_alternative = new Interval(startdate_cfp, startdate_cfp+ (long) (duration_total_for_schedule*60*1000 - time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000), false);
	
			if(myAgent.getFree_interval_array().get(i).contains(timeslot_interval_to_be_booked_transport_alternative)){
				//estimated_start_date = latest_start_date;
				//estimated_enddate = enddate_cfp+(long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000);	
				//slot_found = true;
				//buffer_after_operation = Math.max(0, Math.min(enddate_cfp, myAgent.getFree_interval_array().get(i).upperBound())-estimated_enddate);
				//buffer_before_operation = Math.min(estimated_start_date + (long)(duration_to_get_to_workpiece*60*1000) - startdate_cfp , estimated_start_date - myAgent.getFree_interval_array().get(i).lowerBound());
				//System.out.println("DEBUG____________buffer_before_operation  "+buffer_before_operation+"  -buffer_after_operation = "+buffer_after_operation+" estimated enddate = "+estimated_enddate+" endate cfp = "+enddate_cfp);
				successful = true;
				
				timeslot_for_proposal.setEndDate(String.valueOf(timeslot_interval_to_be_booked_transport_alternative.upperBound()- (long) (time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000)));	
				timeslot_for_proposal.setStartDate(Long.toString(timeslot_interval_to_be_booked_transport_alternative.lowerBound()+(long)(duration_to_get_to_workpiece*60*1000)));

				timeslot_for_schedule.setEndDate(Long.toString(timeslot_interval_to_be_booked_transport_alternative.upperBound())); //time increment is reduced / put to the other busy interval later
				timeslot_for_schedule.setStartDate(Long.toString(timeslot_interval_to_be_booked_transport_alternative.lowerBound()));		
				System.out.println("DEBUG___________estimated_start_date "+timeslot_interval_to_be_booked_transport_alternative.lowerBound()+" estimated_enddate "+timeslot_interval_to_be_booked_transport_alternative.upperBound()+" duration_total_for_schedule = "+duration_total_for_schedule+" duration_to_get_to_workpiece = "+duration_to_get_to_workpiece);
				proposal.setPrice(duration_total_for_schedule + deadline_not_met);		//strafkosten, wenn deadline_not_met
				
				break; 
			}else if(myAgent.getFree_interval_array().get(i).contains(timeslot_interval_to_be_booked_transport_desired)){
				//estimated_start_date = latest_start_date;
				//estimated_enddate = enddate_cfp+(long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000);	
				//slot_found = true;
				//buffer_after_operation = Math.max(0, Math.min(enddate_cfp, myAgent.getFree_interval_array().get(i).upperBound())-estimated_enddate);
				//buffer_before_operation = Math.min(estimated_start_date + (long)(duration_to_get_to_workpiece*60*1000) - startdate_cfp , estimated_start_date - myAgent.getFree_interval_array().get(i).lowerBound());
				//System.out.println("DEBUG____________buffer_before_operation  "+buffer_before_operation+"  -buffer_after_operation = "+buffer_after_operation+" estimated enddate = "+estimated_enddate+" endate cfp = "+enddate_cfp);
				successful = true;
				
				timeslot_for_proposal.setEndDate(String.valueOf(timeslot_interval_to_be_booked_transport_desired.upperBound()- (long) (time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000)));	
				timeslot_for_proposal.setStartDate(Long.toString(timeslot_interval_to_be_booked_transport_desired.lowerBound()+(long)(duration_to_get_to_workpiece*60*1000)));

				timeslot_for_schedule.setEndDate(Long.toString(timeslot_interval_to_be_booked_transport_desired.upperBound())); //time increment is reduced / put to the other busy interval later
				timeslot_for_schedule.setStartDate(Long.toString(timeslot_interval_to_be_booked_transport_desired.lowerBound()));		
				System.out.println("DEBUG___________estimated_start_date "+timeslot_interval_to_be_booked_transport_desired.lowerBound()+" estimated_enddate "+timeslot_interval_to_be_booked_transport_desired.upperBound()+" duration_total_for_schedule = "+duration_total_for_schedule+" duration_to_get_to_workpiece = "+duration_to_get_to_workpiece);
				proposal.setPrice(duration_total_for_schedule + deadline_not_met);	
				
				break; 
			}	
			else {		
			}	
	}
	if(!successful) {
		//desired slot not possible	
		//TBD start new routine
	}
	return successful;
	}

	private Timeslot determineBestTimeslotFromSharedResources() {
		Timeslot return_timeslot = new Timeslot();
		/*
		//find earliest start that all resources can match
		long latest_start = Long.parseLong(timeslot_for_schedule.getStartDate()) - (long) requested_operation.getBuffer_before_operation();
		long earliest_end = Long.parseLong(timeslot_for_schedule.getEndDate()) +  (long) requested_operation.getBuffer_after_operation();
		
		for(Proposal proposal_from_shared_res : (Proposal []) this.getDataStore().get(4)) {
			long start_timeslot_proposal_from_shared_res = Long.parseLong(((AllocatedWorkingStep)proposal_from_shared_res.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getStartDate());
			long end_timeslot_proposal_from_shared_res = Long.parseLong(((AllocatedWorkingStep)proposal_from_shared_res.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getEndDate());
			long buffer_before_operation 					= (long) ((Transport_Operation) ((AllocatedWorkingStep)proposal_from_shared_res.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation()).getBuffer_before_operation();
			long buffer_after_operation 					= (long) ((Transport_Operation) ((AllocatedWorkingStep)proposal_from_shared_res.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation()).getBuffer_after_operation();
			
			if(start_timeslot_proposal_from_shared_res - buffer_before_operation> latest_start){	
				latest_start = start_timeslot_proposal_from_shared_res - buffer_before_operation;
				//earliest_end = end_timeslot_proposal_from_shared_res;
			}else if(end_timeslot_proposal_from_shared_res + buffer_after_operation< earliest_end){
				earliest_end = end_timeslot_proposal_from_shared_res + buffer_after_operation;
			}
			
		}
		//return_timeslot.setStartDate(String.valueOf(latest_start));
		//return_timeslot.setEndDate(String.valueOf(earliest_end));
		System.out.println("DEBUG______WAIT FOR SHARED RES latest_start "+latest_start+" earliest_end "+earliest_end);
		*/

		//alternative
		//create all intervals [start - buffer , end + buffer] and add them to a list
		
		ArrayList <Interval> list_of_intervals_with_transport_res = new ArrayList<Interval>();	
		ArrayList <Interval> list_of_intervals_shared_res = new ArrayList<Interval>();	
		
		Interval transport_resource = new Interval(Long.parseLong(timeslot_for_schedule.getStartDate()) - (long) requested_operation.getBuffer_before_operation_start(), Long.parseLong(timeslot_for_schedule.getEndDate()) + (long) requested_operation.getBuffer_after_operation_end());
		list_of_intervals_with_transport_res.add(transport_resource);

		
		for(Proposal proposal_from_shared_res : (Proposal []) this.getDataStore().get(4)) {
			long start_timeslot_proposal_from_shared_res 	= Long.parseLong(((AllocatedWorkingStep)proposal_from_shared_res.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getStartDate());
			long buffer_before_operation 					= (long) ((Transport_Operation) ((AllocatedWorkingStep)proposal_from_shared_res.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation()).getBuffer_before_operation_start();
			long buffer_after_operation 					= (long) ((Transport_Operation) ((AllocatedWorkingStep)proposal_from_shared_res.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation()).getBuffer_after_operation_end();
			long end_timeslot_proposal_from_shared_res 		= Long.parseLong(((AllocatedWorkingStep)proposal_from_shared_res.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getEndDate());
			Interval interval = new Interval(start_timeslot_proposal_from_shared_res - buffer_before_operation, end_timeslot_proposal_from_shared_res + buffer_after_operation);
			list_of_intervals_with_transport_res.add(interval);	
			list_of_intervals_shared_res.add(interval);
		}
		
		Interval intersection_with_transport_res = new Interval();
		
		
		String printout = "DEBUG   intervals: ";
		for (int i = 0;i<list_of_intervals_with_transport_res.size();i++) {
			if(i == 0 && i+1 < list_of_intervals_with_transport_res.size()) {
				intersection_with_transport_res = list_of_intervals_with_transport_res.get(i).intersection(list_of_intervals_with_transport_res.get(i+1));
				printout = printout + list_of_intervals_with_transport_res.get(i).toString()+"    "+list_of_intervals_with_transport_res.get(i+1)+"   ";
				i++;
			}else {
				intersection_with_transport_res = intersection_with_transport_res.intersection(list_of_intervals_with_transport_res.get(i));
				printout = printout + list_of_intervals_with_transport_res.get(i).toString() + "     ";
			}
		}
				
		
		System.out.println("DEBUG_____intervals:    "+printout+"      interseciton result = "+intersection_with_transport_res.toString());
	
		
		
		if(intersection_with_transport_res.getSize() >= requested_operation.getAvg_Duration()*60*1000) { //than we can use this intersection
			return_timeslot.setStartDate(String.valueOf(intersection_with_transport_res.lowerBound()));
			return_timeslot.setEndDate(String.valueOf(intersection_with_transport_res.upperBound()));
			return_timeslot.setLength(intersection_with_transport_res.getSize());
		}else {
			//determine the bottleneck (e.g. the operator that has time only later) and check availability of the other shared resource
			Interval intersection_shared_res = new Interval();
			for (int i = 0;i<list_of_intervals_shared_res.size();i++) {
				if(i == 0 && i+1 < list_of_intervals_shared_res.size()) {
					intersection_shared_res = list_of_intervals_shared_res.get(i).intersection(list_of_intervals_shared_res.get(i+1));
					printout = list_of_intervals_shared_res.get(i).toString()+"    "+list_of_intervals_shared_res.get(i+1)+"   ";
					i++;
				}else {
					intersection_shared_res = intersection_shared_res.intersection(list_of_intervals_shared_res.get(i));
					printout = printout + list_of_intervals_with_transport_res.get(i).toString() + "     ";
				}
			}
			System.out.println("DEBUG___2__intervals:    "+printout+"      interseciton result = "+intersection_shared_res.toString());
			
			return_timeslot.setStartDate(String.valueOf(intersection_shared_res.lowerBound()));
			return_timeslot.setEndDate(String.valueOf(intersection_shared_res.upperBound()));
			return_timeslot.setLength(intersection_shared_res.getSize());
		}
		
		return return_timeslot;
	}
	private Boolean checkProposalIntersection() {
			Boolean all_availabe_as_requested = true;
				for(Proposal proposal : (Proposal []) this.getDataStore().get(4)) {
					String start_timeslot_proposal = ((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getStartDate();
					String end_timeslot_proposal = ((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getEndDate();
					if(timeslot_for_schedule.getStartDate().equals(start_timeslot_proposal) && timeslot_for_schedule.getEndDate().equals(end_timeslot_proposal)){						
					}else {
						all_availabe_as_requested = false;
					}
				}				
		return all_availabe_as_requested;
	}
	
	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return step == 4;
	}

}
