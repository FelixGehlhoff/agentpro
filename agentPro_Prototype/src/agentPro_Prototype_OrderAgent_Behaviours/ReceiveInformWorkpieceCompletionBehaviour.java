package agentPro_Prototype_OrderAgent_Behaviours;

import java.util.Date;
import agentPro_Prototype_Agents.OrderAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveInformWorkpieceCompletionBehaviour extends CyclicBehaviour{
	
	/*
	 * Listens for workpiece completion messages. If all workpieces of the order are complete, it contacts the
	 * Interface agent.
	 */

	private static final long serialVersionUID = 1L;
	private OrderAgent myAgent;
	private String conversationID_forWorkpiece;
	private String logLinePrefix = ".ReceiveInformCompletionBehaviour ";
	
	public ReceiveInformWorkpieceCompletionBehaviour(OrderAgent myAgent, String conversationID) {
		super(myAgent);
		this.myAgent = myAgent;
		conversationID_forWorkpiece = conversationID;
		
	}
	
	@Override
	public void action() {
		
		// Receive message

		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate mt2 = MessageTemplate.MatchConversationId(conversationID_forWorkpiece);	
        MessageTemplate mt_total = MessageTemplate.and(mt1,mt2);
        
		ACLMessage inform = myAgent.receive(mt_total);
		if (inform != null) {
			System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" Workpiece: "+inform.getContent()+" complete");							
				
			sendInformToInterfaceAgent(System.currentTimeMillis());

		}
		else{
			block();
		}
	}

	private void sendInformToInterfaceAgent(long finish_date) {
		//send inform_done message to interface Agent
		
		ACLMessage inform_done = new ACLMessage(ACLMessage.INFORM);
		inform_done.addReceiver(myAgent.getInterfaceAgent());
		
		
		inform_done.setContent(Long.toString(System.currentTimeMillis()));	
		inform_done.setConversationId(myAgent.getConversationID_forInterfaceAgent());
		
		myAgent.send(inform_done);		
		System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"INFORM sent to InterfaceAgent with content: "+inform_done.getContent());
		
	}

}
