package agentPro_Prototype_WorkpieceAgent_Behaviours;

import java.util.Date;

import agentPro.onto.Disturbance;
import agentPro.onto._Incoming_Disturbance;
import agentPro_Prototype_Agents.WorkpieceAgent;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Simulation_Listener extends CyclicBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	WorkpieceAgent myAgent;
	
	public Simulation_Listener(WorkpieceAgent myAgent) {
		this.myAgent = myAgent;
	}
	
	@Override
	public void action() {
	
		//receive Message from Inbox
		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate mt2 = MessageTemplate.MatchConversationId("Disturbance_Simulation");	
        MessageTemplate mt_total = MessageTemplate.and(mt1,mt2);
        
		ACLMessage msg = myAgent.receive(mt_total);
		
		
		if ( msg != null ) {
			System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+myAgent.logLinePrefix+" Simulation Disturbance Message received ");
			Disturbance disturbance = new Disturbance();
			try {
				Action act = (Action) myAgent.getContentManager().extractContent(msg);
				_Incoming_Disturbance inc_dis = (_Incoming_Disturbance) act.getAction();
								
				disturbance = inc_dis.getHasDisturbance();
				
				myAgent.addBehaviour(new CauseDeterminationBehaviour(myAgent, disturbance, ""));
								
				
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
