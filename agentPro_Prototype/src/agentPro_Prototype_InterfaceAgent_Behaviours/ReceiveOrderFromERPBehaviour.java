package agentPro_Prototype_InterfaceAgent_Behaviours;

import org.json.JSONObject;

import agentPro.onto.Order;
import agentPro.onto._Incoming_Order;
import agentPro.onto._SendCFP;
import agentPro_Prototype_Agents.InterfaceAgent;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class ReceiveOrderFromERPBehaviour extends CyclicBehaviour{

	/*
	 * Receives orders from ERP system (here Dummy Agent)
	 */
	private static final long serialVersionUID = 1L;
	private Integer orderNumber = 0;
	private String conversationID_forOrderAgent;
	private InterfaceAgent myAgent;

	public ReceiveOrderFromERPBehaviour(String conversationID, InterfaceAgent myAgent) {
		conversationID_forOrderAgent = conversationID;
		this.myAgent = myAgent;
	}
	@Override
	public void action() {
		
		//receive Message from Inbox
		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate mt2 = MessageTemplate.MatchConversationId("ERP");	
        MessageTemplate mt_total = MessageTemplate.and(mt1,mt2);
		ACLMessage msg = myAgent.receive(mt_total);
		if ( msg != null ) {
			

			try {
				Action act = (Action) myAgent.getContentManager().extractContent(msg);
				_Incoming_Order incoming_Order = (_Incoming_Order) act.getAction();
				Order order = incoming_Order.getHasOrder();
				orderNumber = order.getID_Number();
						
				myAgent.setERP_system(msg.getSender());
				
				//create Order Agent
				ContainerController cc = myAgent.getContainerController();
				AgentController ac;
				Object [] args = new Object [3];
				args[0] = order;
				args[1] = myAgent.getAID();
				args[2] = conversationID_forOrderAgent;
				
				try {
					ac = cc.createNewAgent("OrderAgent_No_"+orderNumber, "agentPro_Prototype_Agents.OrderAgent", args);
					ac.start();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} catch (UngroundedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (CodecException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (OntologyException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		}
		else {
			block();
		}
		
			

	}

}
