package agentPro_Prototype_ResourceAgent;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Timeslot;
import agentPro.onto._SendCFP;
import agentPro_Prototype_Agents.ResourceAgent;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import support_classes.Run_Configuration;

public class SharedPhysicalResourceCalculationBehaviour extends Behaviour{

	private static final long serialVersionUID = 1L;
	private ResourceAgent myAgent;
	private int step = 0;
	private long timeToWait;
	private long initialTime;
	private Timeslot cfp_timeslot;
	private Proposal proposal = new Proposal();;
	private int deadline_not_met;
	private Operation requested_operation;
	private ArrayList<Proposal> proposals = new ArrayList<Proposal>();

	
	public SharedPhysicalResourceCalculationBehaviour(ResourceAgent myAgent, long timeToWait) {
		super(myAgent);
		this.myAgent = myAgent;
		this.timeToWait = timeToWait;
		//this.cfp_timeslot = cfp_timeslot;
		//this.requested_operation = requested_operation;
		initialTime = System.currentTimeMillis();
	}
	
	@Override
	public void action() {
		
		switch(step) {
		case 0:
			//System.out.println(myAgent.getLocalName()+" DEBUG_____"+myAgent.getLocalName()+"______"+System.currentTimeMillis()+" >= "+ (initialTime+timeToWait+" or "+(Boolean)this.getDataStore().get(0)+" = true"));
			if(System.currentTimeMillis()>= initialTime+timeToWait || (Boolean)this.getDataStore().get(0) == true) { // timeout or all messages there --> start calculation		
				myAgent.getReceiveCFPBehav().sender.clear();
				ArrayList<ACLMessage>listOfCFPMessagesFromTransportResources = new ArrayList<ACLMessage>();		
				for(ACLMessage msg: (ArrayList<ACLMessage>) this.getDataStore().get(1)) {
					listOfCFPMessagesFromTransportResources.add(msg);
				}
				//listOfCFPMessagesFromTransportResources = (ArrayList<ACLMessage>) this.getDataStore().get(1);
				//System.out.println(myAgent.getLocalName()+" DEBUG_____________listOfCFPMessagesFromTransportResources "+listOfCFPMessagesFromTransportResources.get(0).getContent());
				for(ACLMessage msg: listOfCFPMessagesFromTransportResources) {
					//CFP cfp = new CFP();
					Operation operation = new Operation();
					try {	

						Action act = (Action) myAgent.getContentManager().extractContent(msg);
						_SendCFP sendcfp_onto = (_SendCFP) act.getAction();
						
						  @SuppressWarnings("unchecked")
						Iterator<CFP> it = sendcfp_onto.getAllHasCFP();		   
						    while(it.hasNext()) {
								CFP cfp = it.next();
								operation = cfp.getHasOperation();
								//timeslot_for_schedule = new Timeslot();
								//this.proposal = myAgent.checkScheduleDetermineTimeslotAndCreateProposal(Long.parseLong(cfp.getHasTimeslot().getStartDate()), Long.parseLong(cfp.getHasTimeslot().getEndDate()), operation);								
								ArrayList<Proposal> list = myAgent.checkScheduleDetermineTimeslotAndCreateProposal(cfp);
								for(Proposal p : list) {
									proposals.add(p);
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
					//System.out.println(myAgent.getLocalName()+" DEBUG_______proposal calculated "+proposal.getID_Number());
					//deadline_not_met = 0;
					
					//send proposal
					myAgent.sendProposal(proposals, msg.getConversationId(), msg.getSender().getLocalName(), 2.5); //2,5 x reply_by_time 
					
					//myAgent.sendProposal(proposal, msg.getConversationId(), msg.getSender().getLocalName(), null);
					
					//as a reservation the slot is booked into the schedule
					
					//AllocatedWorkingStep for_schedule = (AllocatedWorkingStep) proposal.getConsistsOfAllocatedWorkingSteps().get(0);	
					//for_schedule.setHasTimeslot(timeslot_for_schedule);	
					//myAgent.getReceiveCFPBehav().bookIntoSchedule(for_schedule, time_increment_or_decrement_to_be_added_for_setup_of_next_task);
					
					// / ADD BEHAVIOURS
			        // /////////////////////////////////////////////////////////
					ReceiveOrderBehaviour rob = new ReceiveOrderBehaviour(myAgent, msg.getConversationId(), Run_Configuration.reply_by_time_resource_agent, System.currentTimeMillis()+Run_Configuration.reply_by_time_resource_agent, proposal.getID_Number());
					//rob.setDataStore(this.getDataStore());				
					myAgent.addBehaviour(rob);		
					myAgent.setOfferNumber(myAgent.getOfferNumber()+1);			
				}
				 
				
		        step = 2;				
				
			}else {	//wait for 10 ms and check again
				this.block(10);
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
				break;
			}

			break;
		case 1:
			
			break;
		}

	
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return step == 2;
	}

}
