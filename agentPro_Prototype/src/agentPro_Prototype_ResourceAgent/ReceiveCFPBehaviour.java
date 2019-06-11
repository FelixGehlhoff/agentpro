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
import agentPro_Prototype_Agents._Agent_Template;
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
import support_classes.Storage_element;
import support_classes.Storage_element_slot;
//import support_classes.XYTaskDatasetDemo2;

public class ReceiveCFPBehaviour extends Behaviour{

	private static final long serialVersionUID = 1L;
	//private Integer offerNumber = 0;
	private String conversationID;
	private String logLinePrefix = ".ReceiveCFPBehaviour ";
	private ResourceAgent myAgent;
	public float buffer = 5*60*1000;	//5 minutes Buffer in ms
	public long buffer_time_that_production_can_start_earlier = 0;
	public long buffer_time_that_production_can_start_later = 0;
	public float deadline_not_met = 0; //or 1000	TBD
	public ArrayList<String> sender = new ArrayList<String>();
	public Timeslot timeslot_for_schedule = new Timeslot();
	public float duration_for_price = 0;
	Boolean sharedResourcesStillToBeConsidered = false;
	public int step = 0;
	//public Timeslot timeslot_for_proposal;
	//private Operation requested_operation;
	private ArrayList<Proposal> proposals = new ArrayList<Proposal>();
	public Boolean [] shared_resource_asked;
	public Boolean [] shared_resource_available;
	public float duration_setup;
	public float time_increment_or_decrement_to_be_added_for_setup_of_next_task;
	private int numberOfRefusals = 0;
	private boolean reservation_lock = false;
	//public CFP cfp;
	private ArrayList<Storage_element_slot> proposed_slots = new ArrayList<Storage_element_slot>();
	

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
			 ACLMessage msg = null;
			if(!reservation_lock) {
				msg = myAgent.receive(mt1);	
			}
	       			
			
			if ( msg != null ) {			
				Date deadline = msg.getReplyByDate();
				conversationID = msg.getConversationId();			
				sender.add(msg.getSender().getLocalName());
				//check if current time < deadline for ANSWERING CFP TBD TODO

			//analyze msg.content
				_SendCFP sendcfp_onto = null;
				try {	

					Action act = (Action) myAgent.getContentManager().extractContent(msg);
					sendcfp_onto = (_SendCFP) act.getAction();
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
				
				  Iterator<CFP> it = sendcfp_onto.getAllHasCFP();		   
				    while(it.hasNext()) {
						CFP cfp = it.next();
						Operation operation = cfp.getHasOperation();
						//requested_operation = operation;				
							
							//check if operation can be fulfilled and add avg duration to the operation
							Boolean feasable = myAgent.feasibilityCheckAndDetermineDurationParameters(operation);
							cfp.setHasOperation(operation);
							if(feasable) {
								
								//extract CFP Timeslot
								Timeslot cfp_timeslot = cfp.getHasTimeslot();	
								long startdate_cfp = Long.parseLong(cfp_timeslot.getStartDate());
								long enddate_cfp = Long.parseLong(cfp_timeslot.getEndDate());
								if(myAgent.getLocalName().equals("Kranschiene")) {							
								}else {
									System.out.println(System.currentTimeMillis()+" "+_Agent_Template.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+"cfp received from "+msg.getSender().getLocalName()+". Reply by: "+deadline+" order should start at: "+myAgent.SimpleDateFormat.format(startdate_cfp)+" and end at "+myAgent.SimpleDateFormat.format(enddate_cfp));									
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
								//timeslot_for_schedule = new Timeslot();		//TBD not needed anymore?							
								proposals.add(myAgent.checkScheduleDetermineTimeslotAndCreateProposal(cfp));																						
								deadline_not_met = 0;
							}
							else {
								numberOfRefusals++;								
							}    	
				    }
				    if(proposals.size()+numberOfRefusals ==sendcfp_onto.getHasCFP().size()&& myAgent.getNeeded_shared_resources().size()>0) {
						step = 2;
						break;
														
					}/*else if (proposals.size()>0){	//no need for shared resources to be considered
						step = 1;
						break;
					}*/else if(numberOfRefusals == sendcfp_onto.getHasCFP().size()) {
				    	System.out.println(_Agent_Template.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+" no proposal can be made. Product type not enabled or coordinates not reachable.");						
				    	myAgent.sendRefusal(msg);
				    	sender.clear(); //clears the arraylist of senders for the next message
				    	numberOfRefusals = 0;
						break;
				    }else if(proposals.size()+numberOfRefusals==sendcfp_onto.getHasCFP().size()) {
				    	step=1;
				    }else {	//error handling
						System.out.println(_Agent_Template.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+" no proposal can be made. No free slot found --> should not happen.");
						step = 0;
						break;
					}
			}
			else {
				block();
				break;
			}
			break;
		case 1:
			//send proposal
			myAgent.sendProposal(proposals, conversationID, sender, 2.5); //2,5 x reply_by_time 
					
			//set agent to reservation mode
			this.reservation_lock  = true;
			
			/*
			AllocatedWorkingStep for_schedule = (AllocatedWorkingStep) proposals.getConsistsOfAllocatedWorkingSteps().get(0);	
			for_schedule.setHasTimeslot(timeslot_for_schedule);	
			Boolean bool = 	myAgent.bookIntoSchedule(for_schedule, time_increment_or_decrement_to_be_added_for_setup_of_next_task);
			if(!bool) {
				System.out.println("ERROR______ReceiveCFP__"+myAgent.getLocalName()+"___STEP could not be added");
			}*/

			// / ADD BEHAVIOURS
	        // /////////////////////////////////////////////////////////
			
			myAgent.addBehaviour(myAgent.tbf.wrap(new ReceiveOrderBehaviour(myAgent, conversationID, (long) (2*myAgent.reply_by_time), System.currentTimeMillis()+(long)(2.5*myAgent.reply_by_time), proposals.get(0).getID_Number())));
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
			
			//TODO TBD shared Resources!!
			//myAgent.addBehaviour(new WaitForSharedResourcesBehaviour(myAgent, requested_operation, conversationID, proposals, timeslot_for_schedule, time_increment_or_decrement_to_be_added_for_setup_of_next_task, sender, cfp));
			sender.clear(); //clears the arraylist of senders for the next message
			step = 0;
			break;		
		}
	}
	
	
	
/*
	private Timeslot createReservationTimeslot(ArrayList<Proposal> proposals) {
		long startdate = Long.MAX_VALUE;
		long enddate = 0;
		for(Proposal proposal : proposals) {
			long startdate_thisProposal = Long.parseLong(((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getStartDate());
			long enddate_thisProposal = Long.parseLong(((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getEndDate());
			if(startdate_thisProposal<startdate) {
				startdate = startdate_thisProposal;
			}
			if(enddate_thisProposal>enddate) {
				enddate = enddate_thisProposal;
			}		
		}
		Timeslot return_slot = new Timeslot();
		return_slot.setStartDate(String.valueOf(startdate));
		return_slot.setEndDate(String.valueOf(enddate));
		return return_slot;
	}
*/
	
	public ArrayList<Proposal> getProposals() {
		return proposals;
	}

	public void setProposals(ArrayList<Proposal> proposals) {
		this.proposals = proposals;
	}

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

	public ArrayList<Storage_element_slot> getProposed_slots() {
		return proposed_slots;
	}

	public void setProposed_slots(ArrayList<Storage_element_slot> proposed_slots) {
		this.proposed_slots = proposed_slots;
	}
	
	public boolean isReservation_lock() {
		return reservation_lock;
	}

	public void setReservation_lock(boolean reservation_lock) {
		this.reservation_lock = reservation_lock;
	}

}
