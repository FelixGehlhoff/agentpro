package agentPro_Prototype_ResourceAgent;

import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Resource;
import agentPro.onto.Transport_Operation;
import agentPro.onto._SendInform_Scheduled;
import agentPro_Prototype_Agents.InterfaceAgent;
import agentPro_Prototype_Agents.ProductionResourceAgent;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import support_classes.Interval;

public class ReceiveIntervalForConnectedResourceBehaviour extends CyclicBehaviour{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ProductionResourceAgent myAgent;
	
	public ReceiveIntervalForConnectedResourceBehaviour(ProductionResourceAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;	
	}

	@Override
	public void action() {
		// Receive inform if allocated step is scheduled

					MessageTemplate mt3 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			        MessageTemplate mt4 = MessageTemplate.MatchProtocol("Connected_Resource");
			        MessageTemplate mt_total2 = MessageTemplate.and(mt3,mt4);
			        
					ACLMessage reply_inform = myAgent.receive(mt_total2);
					if (reply_inform != null) {	// Reply received
	
						System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+myAgent.logLinePrefix+" inform received.");
							//add interval to free intervals					
						try {
							Action act = (Action) myAgent.getContentManager().extractContent(reply_inform);
							_SendInform_Scheduled infSched = (_SendInform_Scheduled) act.getAction();
										
						    @SuppressWarnings("unchecked")
							Iterator<AllocatedWorkingStep> it = infSched.getHasInform_Scheduled().getConsistsOfAllocatedWorkingSteps().iterator();
						    while(it.hasNext()) {
						    	AllocatedWorkingStep allWS = it.next();
						    	if(myAgent.getReceiveCFPBehav().bookIntoSchedule(allWS, 0)) {
						    		System.out.println("DEBUG  "+myAgent.getLocalName()+" busy interval added "+allWS.getHasTimeslot().getStartDate()+" "+allWS.getHasTimeslot().getEndDate());			
						    	}else {
						    		System.out.println("DEBUG  "+myAgent.getLocalName()+" busy interval coudl not be added");
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
								/*
								String [] dates = content.split(";");
								long start = Long.parseLong(dates[0]);
								long end = Long.parseLong(dates[1]);
							    	Interval interval = new Interval(start, end);
							    	myAgent.getBusyInterval_array().add(interval);
							    	
								*/
					}else {
						block();
					}
		
	}

	
}
