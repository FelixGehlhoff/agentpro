package agentPro_Prototype_ResourceAgent;

import java.util.Date;

import agentPro.onto.Accept_Proposal;
import agentPro.onto._SendAccept_Proposal;
import agentPro_Prototype_Agents.ResourceAgent;
import agentPro_Prototype_Agents._Agent_Template;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/*
 * Listens for a specified time for orders on a specific PROPOSAL. If there is no offer within the time the behaviour
 * quits. If an order is received the transport is carried out ( duration 3000 ms).
 * Informs workpiece agent of the outcome.
 * 
 * TBD: Failure handling (if working step failed).
 * 
 */

public class ReceiveOrderBehaviour extends Behaviour {
	private String logLinePrefix = ".ReceiveOrderBehaviour ";
	private static final long serialVersionUID = 1L;
	private int step = 0;
	private ResourceAgent myAgent;
	// private long reply_by_time;
	private long reply_by_date_long;
	private int proposal_id;
	// private String requested_transport;
	private ACLMessage received_order;
	// private long continue_at;
	// private Inform_Scheduled inform_scheduled;
	private long order_received_at_date = 0;
	private String conversationID;

	public ReceiveOrderBehaviour(ResourceAgent myAgent, String conversationID, long reply_by_time,
			long reply_by_date_long, int proposalNumber) {
		super(myAgent);
		this.myAgent = myAgent;
		this.conversationID = conversationID;
		// this.reply_by_time = reply_by_time;
		this.reply_by_date_long = reply_by_date_long;
		this.proposal_id = proposalNumber;
		// System.out.println("__________________________"+myAgent.getLocalName()+"
		// offerID at ReceiveOrderBehav creation"+offerNumber);
	}

	public void onStart() {
		/*
		 * try { Thread.sleep((long)0.1*reply_by_time); } catch (InterruptedException e)
		 * { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
	}

	@Override
	public void action() {
		switch (step) {

		case 0:
//checks if reply by time has expired

			if (System.currentTimeMillis() <= reply_by_date_long) { // go to step 1 if deadline has not expired yet
				step = 1;
				break;
			} else if (System.currentTimeMillis() > reply_by_date_long) {
				myAgent.getReceiveCFPBehav().getProposed_slots().clear(); // erase slots
				myAgent.getReceiveCFPBehav().getProposals().clear();
				myAgent.getReceiveCFPBehav().setReservation_lock(false); // reactivate Receive CFP Behaviour
				step = 3;
				System.out.println(System.currentTimeMillis() + " DEBUG_____" + myAgent.getLocalName() + " "
						+ logLinePrefix + " time expired in recieve Order    conv id  " + this.conversationID
						+ " proposal id " + this.proposal_id);
				break;
			}
			// step = 1;
			break;
		case 1:
			// Receive order

			MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			MessageTemplate mt2 = MessageTemplate.MatchConversationId(conversationID);
			// MessageTemplate mt3 =
			// MessageTemplate.MatchInReplyTo(String.valueOf(proposal_id));
			MessageTemplate mt_total = MessageTemplate.and(mt1, mt2);
			// MessageTemplate mt_total = MessageTemplate.and(mt_12,mt3);

			MessageTemplate mt1_reject = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
			MessageTemplate mt2_reject = MessageTemplate.MatchConversationId(conversationID);
			// MessageTemplate mt3_reject =
			// MessageTemplate.MatchInReplyTo(String.valueOf(proposal_id));
			MessageTemplate mt_total_reject = MessageTemplate.and(mt1_reject, mt2_reject);
			// MessageTemplate mt_total_reject = MessageTemplate.and(mt_12_reject,mt3);

			ACLMessage order = myAgent.receive(mt_total);
			if (order != null) {
				received_order = order;
				order_received_at_date = System.currentTimeMillis();
				if (!myAgent.getLocalName().equals("Kranschiene")) {
					System.out
							.println(order_received_at_date + " " + _Agent_Template.SimpleDateFormat.format(new Date())
									+ " " + myAgent.getLocalName() + logLinePrefix + "Order from "
									+ order.getSender().getLocalName() + " received for offer " + proposal_id);
				}

				step = 2;

			} else {
				ACLMessage reject_proposal_message = myAgent.receive(mt_total_reject);

				if (reject_proposal_message != null) {				

					myAgent.getReceiveCFPBehav().getProposed_slots().clear(); // erase slots
					myAgent.getReceiveCFPBehav().getProposals().clear();
					myAgent.getReceiveCFPBehav().setReservation_lock(false); // reactivate Receive CFP Behaviour

					step = 3;
					break;

				} else {
					step = 0;
					this.block(5);
					break;
				}
			}
			break;

		case 2:

			Accept_Proposal accept_proposal = new Accept_Proposal();
			try {
				if(received_order == null) {
					System.out.println("DEBUG__QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ "+logLinePrefix+myAgent.getLocalName());
				}
				Action act = (Action) myAgent.getContentManager().extractContent(received_order);
				_SendAccept_Proposal accept_proposal_onto = (_SendAccept_Proposal) act.getAction();

				accept_proposal = accept_proposal_onto.getHasAccept_Proposal();
				
				// inform_scheduled = new Inform_Scheduled();
				if(myAgent.getLocalName().equals("Durchsatz")) {
					myAgent.printoutBusyIntervals();
					_Agent_Template.printoutWorkPlan(myAgent.getWorkplan(), myAgent.getLocalName());
				}

				Boolean bool1 = myAgent.bookIntoSchedule(accept_proposal);
				if (!bool1) {
					System.out.println(
							"ERROR______ReceiveOrder__" + myAgent.getLocalName() + "___STEP could not be added");
				}
				// inform_scheduled.addConsistsOfAllocatedWorkingSteps(allocWorkingstep);
				// System.out.println(myAgent.SimpleDateFormat.format(new Date())+"
				// "+myAgent.getLocalName()+logLinePrefix+"allocatedWorkingStep added to
				// schedule: Operation: "+allocWorkingstep.getHasOperation().getName()+"
				// Resource: "+allocWorkingstep.getHasResource().getName()+" timeslot_start:
				// "+myAgent.SimpleDateFormat.format(Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate()))+"
				// timeslot_end:
				// "+myAgent.SimpleDateFormat.format(Long.parseLong(allocWorkingstep.getHasTimeslot().getEndDate())));

				// for resources like Crane_Rail the reservation / booking in slots is done here
				if (myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal > 0) {
					if (!myAgent.getLocalName().equals("Kranschiene")) {
						// System.out.println("DEBUG "+myAgent.getLocalName() + " book Into Schedule
						// "+allocWorkingstep.getHasTimeslot().getStartDate()+ "
						// "+allocWorkingstep.getHasTimeslot().getEndDate());
						// myAgent.printoutBusyIntervals();
						// Boolean bool = myAgent.bookIntoSchedule(allocWorkingstep, 0); //no time
						// increment necessary
						Boolean bool = myAgent.bookIntoSchedule(accept_proposal); // no time increment necessary
						if (!bool) {
							System.out.println("ERROR_____________Receive Order " + myAgent.getLocalName()
									+ "_____________step could not be added");
						}
					}

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

			myAgent.getReceiveCFPBehav().setReservation_lock(false); // reactivate Receive CFP Behaviour
			myAgent.getReceiveCFPBehav().getProposals().clear();
			myAgent.getReceiveCFPBehav().getProposed_slots().clear(); // erase slots
			// TBD --> any condition why a failure should occure at this point?

			// step = 3;
			// send inform_done
			/*
			 * AID receiver = new AID(); String localName =
			 * received_order.getSender().getLocalName(); receiver.setLocalName(localName);
			 * 
			 * ACLMessage inform_scheduled_acl = new ACLMessage(ACLMessage.INFORM);
			 * inform_scheduled_acl.setConversationId(conversationID);
			 * inform_scheduled_acl.addReceiver(receiver);
			 * 
			 * inform_scheduled_acl.setLanguage(myAgent.getCodec().getName());
			 * inform_scheduled_acl.setOntology(myAgent.getOntology().getName());
			 * inform_scheduled_acl.setInReplyTo(Integer.toString(proposal_id));
			 * //inform_scheduled_acl.setReplyWith(Integer.toString(proposal_id));
			 * 
			 * _SendInform_Scheduled sendInformScheduled = new _SendInform_Scheduled();
			 * sendInformScheduled.setHasInform_Scheduled(inform_scheduled);
			 * 
			 * Action content = new Action(this.getAgent().getAID(),sendInformScheduled);
			 * 
			 * try { myAgent.getContentManager().fillContent(inform_scheduled_acl, content);
			 * } catch (CodecException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } catch (OntologyException e) { // TODO Auto-generated
			 * catch block e.printStackTrace(); }
			 * 
			 * myAgent.send(inform_scheduled_acl);
			 */
			// System.out.println(System.currentTimeMillis()+"
			// "+myAgent.SimpleDateFormat.format(new Date()) +"
			// "+myAgent.getLocalName()+logLinePrefix+" inform_scheduled_acl for offer
			// "+proposal_id+" and conv ID "+inform_scheduled_acl.getConversationId()+" sent
			// to receiver: "+receiver.getLocalName());

			// sortWorkplan_chronologically();
			// create Gantt chart and add to database for transport resources --> production
			// resources do that when the departure time is known

			// create GANTT Chart

			// WorkPlan wp = myAgent.getWorkplan();
			// Gantt_Creation gantt = new Gantt_Creation(wp, myAgent.getLocalName());
			// Thread thread = new Thread(gantt);
			// thread.start();

			// if(!_Agent_Template.simulation_mode &&
			// (myAgent.getRepresentedResource().getType().equals("transport") ||
			// myAgent.getRepresentedResource().getName().contains("Puffer"))) {

			// create GANTT Chart
			// WorkPlan wp = myAgent.getWorkplan();
			// Gantt_Creation gantt = new Gantt_Creation(wp, myAgent.getLocalName());
			// Thread thread = new Thread(gantt);
			// thread.start();

			// add to database
			myAgent.addBehaviour(new RequestDatabaseEntryBehaviour(myAgent));
			// Database_Entry dbe = new Database_Entry(myAgent, wp);
			// Thread thread2 = new Thread(dbe);
			// thread2.start();

			// }

			step = 3;

			break;

		}
	}

	@Override
	public boolean done() {
		return step == 3;
	}

}
