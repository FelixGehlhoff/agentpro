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

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Shared_Resource;
import agentPro.onto.Timeslot;
import agentPro.onto._SendCFP;
//import agentPro.onto._SendProposal;
import agentPro_Prototype_Agents.ResourceAgent;
import agentPro_Prototype_Agents._Agent_Template;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
//import jade.core.AID;
import jade.core.behaviours.Behaviour;
//import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import support_classes.Interval;
import support_classes.Run_Configuration;
import support_classes.Storage_element_slot;
//import support_classes.XYTaskDatasetDemo2;

public class ReceiveCFPBehaviour extends Behaviour {

	private static final long serialVersionUID = 1L;
	// private Integer offerNumber = 0;
	private String conversationID;
	private String logLinePrefix = ".ReceiveCFPBehaviour ";
	private ResourceAgent myAgent;

	public long buffer_time_that_production_can_start_earlier = 0;
	public long buffer_time_that_production_can_start_later = 0;
	public float deadline_not_met = 0; // or 1000 TBD
	public ArrayList<String> sender = new ArrayList<String>();
	public Timeslot timeslot_for_schedule = new Timeslot();
	public float duration_for_price = 0;
	Boolean sharedResourcesStillToBeConsidered = false;
	public int step = 0;
	// public Timeslot timeslot_for_proposal;
	// private Operation requested_operation;
	private ArrayList<Proposal> proposals = new ArrayList<Proposal>();
	public Boolean[] shared_resource_asked;
	public Boolean[] shared_resource_available;
	public float duration_setup;
	public float time_increment_or_decrement_to_be_added_for_setup_of_next_task;
	private int numberOfRefusals = 0;
	private boolean reservation_lock = false;
	// public CFP cfp;
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
			// receive Message from Inbox
			MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = null;
			if (!reservation_lock) {
				msg = myAgent.receive(mt1);
			}

			if (msg != null) {
				Date deadline = msg.getReplyByDate();
				conversationID = msg.getConversationId();
				sender.add(msg.getSender().getLocalName());
				// check if current time < deadline for ANSWERING CFP TBD TODO

				// analyze msg.content
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

				if (myAgent.getRepresentedResource().getClass().equals(Shared_Resource.class)) {
					// determineSharedResourceBehaviour(); TODO
				} else {
					// TODO Ablauf muss unterschieden werden
				}

				Iterator<CFP> it = sendcfp_onto.getAllHasCFP();
				//TODO Sort by starting date (now assume that earliest is first)
				while (it.hasNext()) {
					CFP cfp = it.next();
					Operation operation = cfp.getHasOperation();
					// requested_operation = operation;

					// check if operation can be fulfilled and add avg duration to the operation
					Boolean feasable = myAgent.feasibilityCheckAndDetermineDurationParameters(operation);
					cfp.setHasOperation(operation);
					if (feasable) {

						// extract CFP Timeslot
						Timeslot cfp_timeslot = cfp.getHasTimeslot();
						long startdate_cfp = Long.parseLong(cfp_timeslot.getStartDate());
						long enddate_cfp = Long.parseLong(cfp_timeslot.getEndDate());

						System.out.println(System.currentTimeMillis() + " "
								+ _Agent_Template.SimpleDateFormat.format(new Date()) + " " + myAgent.getLocalName()
								+ logLinePrefix + "cfp received from " + msg.getSender().getLocalName() + ". Reply by: "
								+ deadline + " order should start at: "
								+ _Agent_Template.SimpleDateFormat.format(startdate_cfp) + " and end at "
								+ _Agent_Template.SimpleDateFormat.format(enddate_cfp));
						
						//check if there is already a proposal to the id (for example, a transport to a buffer)
						//and where the endlocation equals this CFP's start location
						/*if(cfp.getHasOperation().getName().equals("5.0;5.0_Puffer_1")){
							if(myAgent.getLocalName().equals("Transport1")) {
								System.out.println("here");
							}
							
						}*/
						if(myAgent.getLocalName().equals("Transport1") && cfp.getHasOperation().getName().equals("10.0;11.0_Fraese")) {
							System.out.println("here");
						}
						boolean proposalAdded = false;
						ArrayList<Proposal> list_to_add = new ArrayList<Proposal>();
						if(myAgent.getRepresentedResource().getType().equals("Transport")) {
							for(Proposal prop : proposals) {
								AllocatedWorkingStep allWS_of_Proposal = (AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0);
								//there can only be one							
								Boolean proposal_dependency = checkProposalDependency(prop, cfp);
								if(proposal_dependency) {
									myAgent.bookIntoSchedule(prop);
									ArrayList<Proposal> newProposals = myAgent.checkScheduleDetermineTimeslotAndCreateProposal(cfp);
									//add the operation of proposal 1 as "required Operation" to the (dependand) proposal 2
									for(Proposal p : newProposals) {
										((AllocatedWorkingStep)p.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().addRequiresOperation(allWS_of_Proposal.getHasOperation());							
										list_to_add.add(p);								
									}
									myAgent.removeAllocatedWorkingStepFromWorkPlanAndBusyIntervalsAndCreateFreeIntervals(allWS_of_Proposal);
									
									//now add the proposal again without the one before
									ArrayList<Proposal> newProposals_independent = myAgent.checkScheduleDetermineTimeslotAndCreateProposal(cfp);
									for(Proposal p_ind : newProposals_independent) {
										list_to_add.add(p_ind);
									}
									
									proposalAdded = true;
								}
								
								
								/* 24.01.2022
								if(prop.getID_String().equals(cfp.getID_String()) && _Agent_Template.doLocationsMatch((Location) allWS_of_Proposal.getHasOperation().getEndState(),(Location)cfp.getHasOperation().getStartStateNeeded())) {
									myAgent.bookIntoSchedule(prop);
									//cfp.getHasOperation().addRequiresOperation(allWS_of_Proposal.getHasOperation());
									Proposal newProposal = myAgent.checkScheduleDetermineTimeslotAndCreateProposal(cfp);
									//add the operation of proposal 1 as "required Operation" to the (dependand) proposal 2
									((AllocatedWorkingStep)newProposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().addRequiresOperation(allWS_of_Proposal.getHasOperation());
									//proposals.add(myAgent.checkScheduleDetermineTimeslotAndCreateProposal(cfp));
									proposals.add(newProposal);
									myAgent.removeAllocatedWorkingStepFromWorkPlanAndBusyIntervalsAndCreateFreeIntervals(allWS_of_Proposal);
									//now add the proposal again without the one before
									Proposal newProposal_independent = myAgent.checkScheduleDetermineTimeslotAndCreateProposal(cfp);
									proposals.add(newProposal_independent);
									proposalAdded = true;
									break;
								}*/
							}
							for(Proposal p : list_to_add) {  //adding out of the for each is a ConcurrentModificationException
								proposals.add(p);
							}
						}
						
						
		
					if(!proposalAdded) {
						ArrayList<Proposal> newProposals_independent = myAgent.checkScheduleDetermineTimeslotAndCreateProposal(cfp);
						for(Proposal p_ind : newProposals_independent) {
							proposals.add(p_ind);
						}
							
					}
			
						
						deadline_not_met = 0;
					} else {
						numberOfRefusals++;
					}
				}

				if (numberOfRefusals == sendcfp_onto.getHasCFP().size()) {
					System.out.println(_Agent_Template.SimpleDateFormat.format(new Date()) + " "
							+ myAgent.getLocalName() + logLinePrefix
							+ " no proposal can be made. Product type not enabled or coordinates not reachable.");
					myAgent.sendRefusal(msg);
					sender.clear(); // clears the arraylist of senders for the next message
					numberOfRefusals = 0;
					break;
				} else if (proposals.size() + numberOfRefusals >= sendcfp_onto.getHasCFP().size()) {
					step = 1;
				} else { // error handling
					System.out.println(_Agent_Template.SimpleDateFormat.format(new Date()) + " "
							+ myAgent.getLocalName() + logLinePrefix
							+ " no proposal can be made. No free slot found --> should not happen.");
					step = 0;
					break;
				}
			} else {
				block();
				break;
			}
			break;
		case 1:
			// send proposal
			myAgent.sendProposal(proposals, conversationID, sender, 2.5); // 2,5 x reply_by_time

			// set agent to reservation mode
			this.reservation_lock = true;

			/*
			 * AllocatedWorkingStep for_schedule = (AllocatedWorkingStep)
			 * proposals.getConsistsOfAllocatedWorkingSteps().get(0);
			 * for_schedule.setHasTimeslot(timeslot_for_schedule); Boolean bool =
			 * myAgent.bookIntoSchedule(for_schedule,
			 * time_increment_or_decrement_to_be_added_for_setup_of_next_task); if(!bool) {
			 * System.out.println("ERROR______ReceiveCFP__"+myAgent.getLocalName()
			 * +"___STEP could not be added"); }
			 */

			// / ADD BEHAVIOURS
			// /////////////////////////////////////////////////////////
			if (myAgent.getLocalName().equals("Kranschiene")) {
				// currently it gets no message, therefore very short reply by time
				myAgent.addBehaviour(myAgent.tbf
						.wrap(new ReceiveOrderBehaviour(myAgent, conversationID, (long) (0.1 * Run_Configuration.reply_by_time_resource_agent),
								System.currentTimeMillis() + (long) (0.02 * Run_Configuration.reply_by_time_resource_agent),
								proposals.get(0).getID_Number())));

			} else {
				myAgent.addBehaviour(myAgent.tbf
						.wrap(new ReceiveOrderBehaviour(myAgent, conversationID, (long) (2 * Run_Configuration.reply_by_time_resource_agent),
								System.currentTimeMillis() + (long) (2.5 * Run_Configuration.reply_by_time_resource_agent),
								proposals.get(0).getID_Number())));

			}

			myAgent.setOfferNumber(myAgent.getOfferNumber() + 1);
			step = 0;
			sender.clear(); // clears the arraylist of senders for the next message
			numberOfRefusals = 0;

			break;
		case 2:
			
			sender.clear(); // clears the arraylist of senders for the next message
			numberOfRefusals = 0;
			step = 0;
			break;
		}
	}

	private Boolean checkProposalDependency(Proposal prop, CFP cfp) {
		if(prop.getID_String().equals(cfp.getID_String())) {		//the proposal was created for the same ID such as A_1_Order@Durchsatz
			//now check if the proposal timeslot is the closest to the cfp compared to existing busy intervals
			for(int i = myAgent.getBusyInterval_array().size()-1;i>=0;i--) {
				Interval busy_interval = myAgent.getBusyInterval_array().get(i);		
				AllocatedWorkingStep allWS = (AllocatedWorkingStep) prop.getConsistsOfAllocatedWorkingSteps().get(0);
				if(Long.parseLong(cfp.getHasTimeslot().getStartDate())-busy_interval.upperBound() < Long.parseLong(cfp.getHasTimeslot().getStartDate())-Long.parseLong(allWS.getHasTimeslot().getEndDate()) && Long.parseLong(cfp.getHasTimeslot().getStartDate())-busy_interval.upperBound()>0) {
					return false; //there is no dependency
				}
			}
		}
		return true;
	}

	private void determineSharedResourceBehaviour(ACLMessage msg) {
		// for e.g. a Crane RAIL Resource Agent the number is > 0
		if (myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal > 0 && sender.size() == 1) { // first
																													// msg
																													// received
			this.getDataStore().clear();
			// store message
			ArrayList<ACLMessage> listOfMessages = new ArrayList<ACLMessage>();
			listOfMessages.add(msg);
			this.getDataStore().put(0, false);
			this.getDataStore().put(1, listOfMessages);
			// determine time to wait, e.g. 50 ms
			long timeToWait = 50;
			// System.out.println("DEBUG__________________"+myAgent.getLocalName()+"
			// SharedPhysicalResourceCalculationBehaviour started");
			SharedPhysicalResourceCalculationBehaviour sprcb = new SharedPhysicalResourceCalculationBehaviour(myAgent,
					timeToWait);
			sprcb.setDataStore(this.getDataStore());
			// myAgent.addBehaviour(myAgent.tbf.wrap(sprcb));
			myAgent.addBehaviour(sprcb);
			// Special Case: Only one crane
			if (sender.size() == myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal) {
				this.getDataStore().put(0, true); // all msg's received --> calculation can start
				sender.clear();
				// break;
			}
			// break;
		} else if (myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal > 0 && sender.size() > 1
				&& sender.size() < myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal) {
			// store message
			((ArrayList<ACLMessage>) this.getDataStore().get(1)).add(msg);
			// break;
		} else if (myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal > 0
				&& sender.size() == myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal) {
			((ArrayList<ACLMessage>) this.getDataStore().get(1)).add(msg);
			this.getDataStore().put(0, true); // all msg's received --> calculation can start
			sender.clear();
			// break;
		}

	}

	/*
	 * private Timeslot createReservationTimeslot(ArrayList<Proposal> proposals) {
	 * long startdate = Long.MAX_VALUE; long enddate = 0; for(Proposal proposal :
	 * proposals) { long startdate_thisProposal =
	 * Long.parseLong(((AllocatedWorkingStep)proposal.
	 * getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getStartDate())
	 * ; long enddate_thisProposal = Long.parseLong(((AllocatedWorkingStep)proposal.
	 * getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getEndDate());
	 * if(startdate_thisProposal<startdate) { startdate = startdate_thisProposal; }
	 * if(enddate_thisProposal>enddate) { enddate = enddate_thisProposal; } }
	 * Timeslot return_slot = new Timeslot();
	 * return_slot.setStartDate(String.valueOf(startdate));
	 * return_slot.setEndDate(String.valueOf(enddate)); return return_slot; }
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
