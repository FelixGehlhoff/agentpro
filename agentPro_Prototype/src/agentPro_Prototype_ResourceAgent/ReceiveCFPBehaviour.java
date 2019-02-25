package agentPro_Prototype_ResourceAgent;

import java.util.ArrayList;

/*
 * Checks for CFP messages. Performs calculations for offer.
 * Sends PROPOSAL and starts ReceiveOrderBehaviour.
 */

import java.util.Date;
import java.util.Iterator;
//import java.util.Iterator;
//import java.util.Random;

//import org.jfree.ui.RefineryUtilities;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
//import agentPro.onto.Resource;
import agentPro.onto.Timeslot;
//import agentPro.onto.TransportResource;
import agentPro.onto.Transport_Operation;
import agentPro.onto.WorkPlan;
import agentPro.onto._SendCFP;
import agentPro.onto._SendProposal;
//import agentPro.onto._SendProposal;
import agentPro_Prototype_Agents.ResourceAgent;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.AID;
//import jade.core.AID;
import jade.core.behaviours.Behaviour;
//import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import support_classes.Interval;
//import support_classes.XYTaskDatasetDemo2;

public class ReceiveCFPBehaviour extends Behaviour{

	private static final long serialVersionUID = 1L;
	//private Integer offerNumber = 0;
	private String conversationID;
	private String logLinePrefix = ".ReceiveCFPBehaviour ";
	private ResourceAgent myAgent;
	public float buffer = 5*60*1000;	//5 minutes Buffer in ms
	public long buffer_time_that_production_can_start_earlier = 0;
	public float deadline_not_met = 0; //or 1000	TBD
	public ArrayList<String> sender = new ArrayList<String>();
	public Timeslot timeslot_for_schedule;
	public float duration_for_price = 0;
	Boolean sharedResourcesStillToBeConsidered = false;
	public int step = 0;
	public Timeslot timeslot_for_proposal;
	private Operation requested_operation;
	private Proposal proposal;
	public Boolean [] shared_resource_asked;
	public Boolean [] shared_resource_available;
	public float duration_setup;
	public float time_increment_or_decrement_to_be_added_for_setup_of_next_task;
	public CFP cfp;

	public ReceiveCFPBehaviour(ResourceAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		switch (step) {
		case 0:
			//receive Message from Inbox
			MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.CFP);

	        ACLMessage msg = myAgent.receive(mt1);				
			
			if ( msg != null ) {			
				Date deadline = msg.getReplyByDate();
				conversationID = msg.getConversationId();
				sender.add(msg.getSender().getLocalName());
				//check if current time < deadline for ANSWERING CFP
					//tbd
				
			//analyze msg.content
				cfp = new CFP();
				try {	

					Action act = (Action) myAgent.getContentManager().extractContent(msg);
					_SendCFP sendcfp_onto = (_SendCFP) act.getAction();
					cfp = sendcfp_onto.getHasCFP();
					Operation operation = cfp.getHasOperation();
					requested_operation = operation;
						
						//check if operation can be fulfilled and add avg duration to the operation
						Boolean feasable = myAgent.feasibilityCheck(operation);
						if(feasable) {
							
							//extract CFP Timeslot
							Timeslot cfp_timeslot = cfp.getHasTimeslot();	
							long startdate_cfp = Long.parseLong(cfp_timeslot.getStartDate());
							long enddate_cfp = Long.parseLong(cfp_timeslot.getEndDate());
							if(myAgent.getLocalName().equals("Kranschiene")) {							
							}else {
								System.out.println(System.currentTimeMillis()+" "+myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+"cfp received from "+msg.getSender().getLocalName()+". Reply by: "+deadline+" order should start at: "+myAgent.SimpleDateFormat.format(startdate_cfp)+" and end at "+myAgent.SimpleDateFormat.format(enddate_cfp));									
							}
							//System.out.println("DBEUG_________sender.size()_"+sender.size());
							
							//for e.g. a Crane RAIL Resource Agent the number is > 0 
							if(myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal > 0 && sender.size() == 1) {	//first msg received
								this.getDataStore().clear();
								//store message
								ArrayList<ACLMessage>listOfMessages = new ArrayList<ACLMessage>();
								listOfMessages.add(msg);
								this.getDataStore().put(0, false);
								this.getDataStore().put(1, listOfMessages);
								//determine time to wait, e.g. 50 ms
								long timeToWait = 50;
								//System.out.println("DEBUG__________________"+myAgent.getLocalName()+"  SharedPhysicalResourceCalculationBehaviour started");
								SharedPhysicalResourceCalculationBehaviour sprcb = new SharedPhysicalResourceCalculationBehaviour(myAgent, timeToWait);
								sprcb.setDataStore(this.getDataStore());
								//myAgent.addBehaviour(myAgent.tbf.wrap(sprcb));	
								myAgent.addBehaviour(sprcb);	
									//Special Case: Only one crane
									if(sender.size() == myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal) {									
										this.getDataStore().put(0, true); //all msg's received --> calculation can start
										sender.clear();
										break;
									}
								break;														
							}else if(myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal > 0 && sender.size() > 1 && sender.size() < myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal){
								//store message
								((ArrayList<ACLMessage>)this.getDataStore().get(1)).add(msg);											
								break;
							}else if(myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal > 0 && sender.size() == myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal) {
								((ArrayList<ACLMessage>)this.getDataStore().get(1)).add(msg);	
								this.getDataStore().put(0, true); //all msg's received --> calculation can start
								sender.clear();
								break;
							}
							timeslot_for_schedule = new Timeslot();									
							this.proposal = myAgent.checkScheduleDetermineTimeslotAndCreateProposal(cfp);								
							
							
							deadline_not_met = 0;
							

							if(this.proposal != null && myAgent.getNeeded_shared_resources().size()>0) {
								step = 2;
								break;
																
							}else if (this.proposal != null){	//no need for shared resources to be considered
								step = 1;
								break;
							}else {	//error handling
								System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+" no proposal can be made. No free slot found --> should not happen.");
								step = 0;
								break;
							}
							
							
							

						}
						else {
							System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+" no proposal can be made.");
							myAgent.sendRefusal(msg);
							sender.clear(); //clears the arraylist of senders for the next message
							break;
						}
								
					
				} catch (UngroundedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
		      
			}
			else {
				block();
				break;
			}	
		case 1:
			//send proposal
			myAgent.sendProposal(proposal, conversationID, sender, 2.5); //2,5 x reply_by_time 
			
			//as a reservation the slot is booked into the schedule
			
			AllocatedWorkingStep for_schedule = (AllocatedWorkingStep) proposal.getConsistsOfAllocatedWorkingSteps().get(0);	
			for_schedule.setHasTimeslot(timeslot_for_schedule);	
			Boolean bool = 	bookIntoSchedule(for_schedule, time_increment_or_decrement_to_be_added_for_setup_of_next_task);
			if(!bool) {
				System.out.println("ERROR______ReceiveCFP__"+myAgent.getLocalName()+"___STEP could not be added");
			}

			// / ADD BEHAVIOURS
	        // /////////////////////////////////////////////////////////
			
			myAgent.addBehaviour(myAgent.tbf.wrap(new ReceiveOrderBehaviour(myAgent, conversationID, (long) (2*myAgent.reply_by_time), System.currentTimeMillis()+(long)(2.5*myAgent.reply_by_time), proposal.getID_Number())));
			//ReceiveOrderBehaviour rob = new ReceiveOrderBehaviour(myAgent, conversationID, (long) 2*myAgent.reply_by_time, System.currentTimeMillis()+(long)2.5*myAgent.reply_by_time, proposal.getID_Number());
			//myAgent.addBehaviour(rob);
			
			
	        //myAgent.addBehaviour(new ReceiveOrderBehaviourTemplate(myAgent, conversationID, reply_by_time, reply_by_date_long, myAgent.getOfferNumber()));	
	        myAgent.setOfferNumber(myAgent.getOfferNumber()+1);
	        step = 0;
	        sender.clear(); //clears the arraylist of senders for the next message
	   
	        break;
		case 2:
			//System.out.println("DEBUG_____________timeslot_for_schedule "+timeslot_for_schedule.getStartDate()+", "+timeslot_for_schedule.getEndDate());
			//myAgent.addBehaviour(myAgent.tbf.wrap(new WaitForSharedResourcesBehaviour(myAgent, requested_operation, conversationID, proposal, timeslot_for_proposal, timeslot_for_schedule, time_increment_or_decrement_to_be_added_for_setup_of_next_task, sender)));
			myAgent.addBehaviour(new WaitForSharedResourcesBehaviour(myAgent, requested_operation, conversationID, proposal, timeslot_for_schedule, time_increment_or_decrement_to_be_added_for_setup_of_next_task, sender, cfp));
			sender.clear(); //clears the arraylist of senders for the next message
			step = 0;
			break;		
		}
	}
	

	public Boolean bookIntoSchedule(AllocatedWorkingStep allocWorkingstep, float time_increment_or_decrement_to_be_added_for_setup_of_next_task) {		
		/*
		 * add interval (busy) and new resulting free intervals
		 * 
		 * 
		 */
		Boolean booking_successful = false;
		
		long long_time_increment_or_decrement_to_be_added_for_setup_of_next_task = (long) (time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000);
		long startdate_busy_interval_new = Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate());
		long enddate_busy_interval_new = Long.parseLong(allocWorkingstep.getHasTimeslot().getEndDate());
		Location new_endLocation = null;
		if(allocWorkingstep.getHasOperation().getType().equals("transport")) {
			new_endLocation = ((Transport_Operation)allocWorkingstep.getHasOperation()).getHasEndLocation();				
		}
		
		Interval timeslot_interval_busy = new Interval(startdate_busy_interval_new, enddate_busy_interval_new, false);
		timeslot_interval_busy.setId(allocWorkingstep.getID_String());
		//System.out.println("DEBUG_                  REceiveCFPBookintoSchedule  allocWorkingstep.getHasTimeslot().getStartDate() "+allocWorkingstep.getHasTimeslot().getStartDate()+" allocWorkingstep.getHasTimeslot().getEndDate() "+allocWorkingstep.getHasTimeslot().getEndDate()+" Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate()  "+Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate())+" Long.parseLong(allocWorkingstep.getHasTimeslot().getEndDate())  "+Long.parseLong(allocWorkingstep.getHasTimeslot().getEndDate())+" time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000+ "+(long) time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000+" timeslot_interval_busy "+timeslot_interval_busy.toString());
		
		for(int i = 0;i<myAgent.getFree_interval_array().size();i++) {		//check the free intervals and find the one that fits		
			if(myAgent.getFree_interval_array().get(i).contains(timeslot_interval_busy)) {
				booking_successful = true;
				//eg from 0 - 10 contains 5-10
				//store the free interval
				Interval free_interval_that_existed_before = myAgent.getFree_interval_array().get(i);
				
				//remove the free interval that contains the new busy interval (new ones are created later)
				myAgent.getFree_interval_array().remove(i);
				//check which new intervals are needed
					//long enddate_busy_interval_before = 0;
					//long startdate_busy_interval_after = 0;
					boolean busy_interval_before_contains_startdate = false;
					boolean busy_interval_after_contains_enddate = false;
					
					//as there can be no (real) overlap between busy intervals --> contains means start & enddate match (or vice versa)		
					if(myAgent.getBusyInterval_array().size()>0) {
						//check for every interval in busy intervals
						for(int j = 0;j < myAgent.getBusyInterval_array().size();j++) {
							
							//if this busy interval contains the start date of the new busy interval --> enddate before and start new are equal
							if(myAgent.getBusyInterval_array().get(j).contains(startdate_busy_interval_new)) {	
								busy_interval_before_contains_startdate = true;
								//if there is a busy interval after
								if(j+1<myAgent.getBusyInterval_array().size()) {
									if(myAgent.getBusyInterval_array().get(j).contains(enddate_busy_interval_new)) {	//new 12.02.19
										busy_interval_after_contains_enddate = true;
									}
									
									long old_start = myAgent.getBusyInterval_array().get(j+1).lowerBound();
									Interval new_busy_interval_AFTER = new Interval(old_start - long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, myAgent.getBusyInterval_array().get(j+1).upperBound());
									new_busy_interval_AFTER.setId(myAgent.getBusyInterval_array().get(j+1).getId());
									myAgent.getBusyInterval_array().remove(j+1);
									myAgent.getBusyInterval_array().add(j+1, new_busy_interval_AFTER);	
									setStartOfAllocatedWorkingStepThatStartsAtTimeXToYandChangeStartLocation(old_start, old_start - long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, new_endLocation);
									
								}						
								break;
							//if this busy interval contains the end date of the new busy interval --> startdate after and end new are equal
							}else if(myAgent.getBusyInterval_array().get(j).contains(enddate_busy_interval_new)) {								
								busy_interval_after_contains_enddate = true;
							//startdate of the new free interval BEFORE must be the old start date of the free interval
							//enddate of the new free interval BEFORE must be the start date of the new busy interval
							//startdate of the new free interval AFTER must be the enddate of the new busy interval
							//enddate of the new free interval AFTER must be the enddate of the old free intervall
								//04.04.18 time increment has to be considered! The old busy interval AFTER must be increased (or decreased in time 
								//(or vice versa)) because the setup now takes longer (or shorter)
							long old_start = myAgent.getBusyInterval_array().get(j).lowerBound(); //new 12.02.19
							Interval new_busy_interval_AFTER = new Interval(myAgent.getBusyInterval_array().get(j).lowerBound() - long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, myAgent.getBusyInterval_array().get(j).upperBound());
							new_busy_interval_AFTER.setId(myAgent.getBusyInterval_array().get(j).getId()); //new 12.02.19
							myAgent.getBusyInterval_array().remove(j);
							myAgent.getBusyInterval_array().add(j, new_busy_interval_AFTER);
							setStartOfAllocatedWorkingStepThatStartsAtTimeXToYandChangeStartLocation(old_start, old_start - long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, new_endLocation); //new 12.02.19
							
							break;
							}
						}					
					}

					
					//if only the startdate is element of a busy interval
					// --> one new free interval AFTER the new busy interval
					if(busy_interval_before_contains_startdate && !busy_interval_after_contains_enddate) {
						Interval new_free_intervall_after = new Interval(enddate_busy_interval_new, free_interval_that_existed_before.upperBound()- long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, false);
						myAgent.getFree_interval_array().add(i, new_free_intervall_after);
					}
					
					//if the enddate is element of a busy interval
					// --> one new free interval BEFORE the new busy interval
					else if(!busy_interval_before_contains_startdate && busy_interval_after_contains_enddate) {
						Interval new_free_intervall_before = new Interval(free_interval_that_existed_before.lowerBound(), startdate_busy_interval_new, false);
						myAgent.getFree_interval_array().add(i, new_free_intervall_before);	
						
					}											
					//if neither the startdate nor the enddate is element of any busy interval --> two new free intervals are needed
					// --> two new free intervals are needed (BEFORE and AFTER)
					else if(!busy_interval_before_contains_startdate && !busy_interval_after_contains_enddate) {
						Interval new_free_intervall_before = new Interval(free_interval_that_existed_before.lowerBound(), startdate_busy_interval_new, false);
						Interval new_free_intervall_after = new Interval(enddate_busy_interval_new, free_interval_that_existed_before.upperBound(), false);						
						myAgent.getFree_interval_array().add(i, new_free_intervall_after);
						myAgent.getFree_interval_array().add(i, new_free_intervall_before);
						
					}
						//if both are elements of two different busy intervals
						// --> no new free interval is needed because it is "replaced" by a busy interval	
					else {
						
					}							
			}
			//break;
			
		}
		if(booking_successful) {
			myAgent.getBusyInterval_array().add(timeslot_interval_busy);	
			myAgent.sortArrayListIntervalsEarliestFirst(myAgent.getBusyInterval_array(), "start");
		    
			myAgent.getWorkplan().addConsistsOfAllocatedWorkingSteps(allocWorkingstep);
			
			if(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>1) {
				myAgent.sortWorkplanChronologically();
			}
		}
		
		myAgent.printoutFreeIntervals();
		myAgent.printoutBusyIntervals();
		return booking_successful;		
		//create GANTT chart
		/*
			 XYTaskDatasetDemo2 demo = new XYTaskDatasetDemo2(
		                "JFreeChart : XYTaskDatasetDemo2.java", myAgent.getWorkplan(), myAgent.getLocalName());
		        demo.pack();
		        RefineryUtilities.centerFrameOnScreen(demo);
		        demo.setVisible(false);	*/
	}
	
	private void setStartOfAllocatedWorkingStepThatStartsAtTimeXToYandChangeStartLocation(long old_start_date, long new_start_date, Location new_endLocation) {
		for(int i = 0; i<myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size();i++) {   	   	
				AllocatedWorkingStep a = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(i);				
				if(Long.parseLong(a.getHasTimeslot().getStartDate()) == old_start_date) {	    		
		    		a.getHasTimeslot().setStartDate(String.valueOf(new_start_date));	
		    		if(new_endLocation != null) {
		    			((Transport_Operation)a.getHasOperation()).setHasStartLocation(new_endLocation);
		    		}
		    		
		    		
		    		break;
		    	}
		    }
		
	}

	/*
	public void sortWorkplanChronologically() {

			WorkPlan wP_toBeSorted = new WorkPlan();
			
		    @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
		   
		    while(it.hasNext()) {		//checks for every allWS in Workplan
		    	AllocatedWorkingStep allocWorkingstep = it.next();	  
		    	long startdate_of_current_step = Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate());	//get the startdate of the step
		    	
		    	if(wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().size()>0) {	//not first element
			    	int position_to_be_added = 0;
			    	
			    	for(int i = 0;i<wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().size();i++) {	//check for every element already in the new list if the current one from WP has an earlier startdate
			    		//startdate of the element in the new list
			    		long startdate_step_in_to_be_sorted = Long.parseLong(((AllocatedWorkingStep) wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().get(i)).getHasTimeslot().getStartDate());
			    		//if the startdate of the element in the new list is smaller than the startdate of the current step, 
			    		//the current step has to be added afterwards    			
			    		if(startdate_step_in_to_be_sorted < startdate_of_current_step) {	
			    			position_to_be_added++; //so the position must be increased by one
			    		}else {
			    			//if not it can be added on that position and all behind are moved one position "to the right"
			    		}
			    	}
			    	wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().add(position_to_be_added, allocWorkingstep);
			    	
		    	}else {	//first element can just be added (list was still null)
		    		wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().add(allocWorkingstep);
		    		}

		    }
		    myAgent.setWorkplan(wP_toBeSorted);

	}
*/
	public float calcDistance(Location location_1, Location location_2) {
		float x1 = location_1.getCoordX();
		float y1 = location_1.getCoordY();
		
		float x2 = location_2.getCoordX();
		float y2 = location_2.getCoordY();
		
		float distance = 0;
	    distance = (float) Math.hypot(x2 - x1, y2 - y1);
	    return distance;
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
