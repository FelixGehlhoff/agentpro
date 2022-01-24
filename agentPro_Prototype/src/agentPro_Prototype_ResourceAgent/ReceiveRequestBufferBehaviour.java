package agentPro_Prototype_ResourceAgent;

import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Inform_Buffer;
import agentPro.onto.Operation;
import agentPro.onto._SendInform_Buffer;
import agentPro.onto._SendRequest_Buffer;
import agentPro_Prototype_Agents.ResourceAgent;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveRequestBufferBehaviour extends CyclicBehaviour{
	/**
	 * 
	 */
	private String logLinePrefix = ".ReceiveRequestBufferBehaviour ";
	private static final long serialVersionUID = 1L;
	private String conversationID = "Buffer_Determination";
	private ResourceAgent myAgent;

	
	public ReceiveRequestBufferBehaviour(ResourceAgent resourceAgent) {
		this.myAgent = resourceAgent;
	}


	@Override
	public void action() {
		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate mt2 = MessageTemplate.MatchConversationId(conversationID);	
        MessageTemplate mt_total = MessageTemplate.and(mt1,mt2);
        
		ACLMessage request_buffer = myAgent.receive(mt_total);
		if (request_buffer != null) {
			System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+"request_Buffer received.");

			//prepare answer Message
			 //create ontology content
			Inform_Buffer inform_buffer = new Inform_Buffer();
			
			try {	

				Action act = (Action) myAgent.getContentManager().extractContent(request_buffer);
				_SendRequest_Buffer sendRequest_buffer = (_SendRequest_Buffer) act.getAction();
				
				//allWS = (AllocatedWorkingStep) sendRequest_buffer.getHasRequest_Buffer().getConsistsOfAllocatedWorkingSteps().get(0);
	
				/*
				 * for each requested element the buffer must be calculated
				 */
				
				 @SuppressWarnings("unchecked")
					Iterator<AllocatedWorkingStep> it = sendRequest_buffer.getHasRequest_Buffer().getConsistsOfAllocatedWorkingSteps().iterator();		 	
				    while(it.hasNext()) {
				    	AllocatedWorkingStep allWS = it.next();
				    	
				    	//find allWS in Workplan
				    	int counter = 0;
					 	int position = 0;
					 	
						AllocatedWorkingStep relevant_allWS = null;
						Operation op = null;
						
						//find correct allWS
						 @SuppressWarnings("unchecked")
							Iterator<AllocatedWorkingStep> it_2 = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();		 	
						    while(it_2.hasNext()) {
						    	AllocatedWorkingStep allocWorkingstep = it_2.next();
						    	//System.out.println("DEBUG________________all WS    counter "+counter+" allocWorkingstep "+allocWorkingstep.getHasOperation().getName());
						    	op = allocWorkingstep.getHasOperation();	
						    	String name_Workpiece = allocWorkingstep.getHasOperation().getAppliedOn().getID_String();
						    	if(op.getName().equals(allWS.getHasOperation().getName()) && name_Workpiece.equals(allWS.getHasOperation().getAppliedOn().getID_String())) {	//find out the position of the relevant step			    	
						    		position = counter;
						    		break;
						    	}
						    	counter++;
						    }
						    relevant_allWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(position);
						 //determine & set Buffer
						    float buffer = determineBuffer(relevant_allWS, position);
						    ((Operation)allWS.getHasOperation()).setBuffer_before_operation_start(buffer);
						 //add to ontology element
						  inform_buffer.addConsistsOfAllocatedWorkingSteps(allWS);			    	
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
			
			//create answer ontology content and message
			_SendInform_Buffer sendInform_Buffer = new _SendInform_Buffer();
			sendInform_Buffer.setHasInform_Buffer(inform_buffer);		
			Action content = new Action(this.getAgent().getAID(),sendInform_Buffer);
		
			//create ACLMessage

			ACLMessage inform_buffer_acl = new ACLMessage(ACLMessage.INFORM);
			inform_buffer_acl.addReceiver(request_buffer.getSender());
			//conversationID = Long.toString(System.currentTimeMillis());	//TBD if this is suitable										
			inform_buffer_acl.setConversationId(request_buffer.getConversationId());
			inform_buffer_acl.setReplyWith(request_buffer.getReplyWith());
			inform_buffer_acl.setLanguage(myAgent.getCodec().getName());
			inform_buffer_acl.setOntology(myAgent.getOntology().getName());
			
			//ontology --> fill content
			try {
				myAgent.getContentManager().fillContent(inform_buffer_acl, content);
			} catch (CodecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OntologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			myAgent.send(inform_buffer_acl);
			
			
		}else {
			block();
		}
		
	}


	private float determineBuffer(AllocatedWorkingStep relevant_allWS, int position) {
		long enddate_relevant_allWS = Long.parseLong(relevant_allWS.getHasTimeslot().getEndDate());	
		long buffer;
		//System.out.println(logLinePrefix+" DEBUG___________________ position "+position+" myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size() "+myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size());
		
		//if its the only step scheduled or the last step in schedule --> max. Buffer
		if(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size() == 1 || myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size() == position+1) {
			buffer = Long.MAX_VALUE;
			//System.out.println("DEBUG_______ max buffer");
			
		}else {
			
			long startdate_next_allWS = Long.parseLong(((AllocatedWorkingStep)myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(position+1)).getHasTimeslot().getStartDate());			
			buffer = startdate_next_allWS - enddate_relevant_allWS;
			//System.out.println("DEBUG_______startdate_next_allWS "+myAgent.SimpleDateFormat.format(startdate_next_allWS)+"enddate_relevant_allWS "+myAgent.SimpleDateFormat.format(enddate_relevant_allWS)+" buffer = "+myAgent.SimpleDateFormat.format(buffer)+"_____________ asked Operation "+relevant_allWS.getHasOperation().getName()+" next Operation "+((AllocatedWorkingStep)myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(position+1)).getHasOperation().getName());
			
		}
		
		
		return buffer;
	}

}
