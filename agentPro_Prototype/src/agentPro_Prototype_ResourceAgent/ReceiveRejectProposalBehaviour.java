package agentPro_Prototype_ResourceAgent;

import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Reject_Proposal;
import agentPro.onto._SendReject_Proposal;
import agentPro_Prototype_Agents.ResourceAgent;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;



public class ReceiveRejectProposalBehaviour extends CyclicBehaviour{
	private String logLinePrefix = ".ReceiveRejectProposalBehaviour ";
	private static final long serialVersionUID = 1L;
	private ResourceAgent myAgent;
	private int proposal_id;
	//private ACLMessage rejected_proposal;

	

	public ReceiveRejectProposalBehaviour(ResourceAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
		
	}
	
	@Override
	public void action() {

			MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);	        
			ACLMessage reject_proposal_message = myAgent.receive(mt1);
			if (reject_proposal_message != null) {
				//rejected_proposal = reject_proposal_message;
			
				//analyze msg.content
				Reject_Proposal reject_proposal = new Reject_Proposal();
				try {			
					
					Action act = (Action) myAgent.getContentManager().extractContent(reject_proposal_message);
					_SendReject_Proposal reject_proposal_onto = (_SendReject_Proposal) act.getAction();
				
					reject_proposal = reject_proposal_onto.getHasReject_Proposal();
					proposal_id = reject_proposal.getID_Number();
					System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"reject_proposal from "+reject_proposal_message.getSender().getLocalName()+" received for offer "+proposal_id);
					
					 @SuppressWarnings("unchecked")
						Iterator<AllocatedWorkingStep> it = reject_proposal.getConsistsOfAllocatedWorkingSteps().iterator();		 	
					    while(it.hasNext()) {
					    	AllocatedWorkingStep allWS = it.next();
					    	
					    	myAgent.removeAllocatedWorkingStepFromWorkPlanAndBusyIntervalsAndCreateFreeIntervals(allWS);
				
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
				
			}			
	}
}
