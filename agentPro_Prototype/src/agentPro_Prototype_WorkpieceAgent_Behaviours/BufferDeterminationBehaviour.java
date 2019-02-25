package agentPro_Prototype_WorkpieceAgent_Behaviours;

import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Disturbance;
import agentPro.onto.DisturbanceType;
import agentPro.onto.Machine_Error;
import agentPro.onto.Operation;
import agentPro.onto.Request_Buffer;
import agentPro.onto._SendInform_Buffer;
import agentPro.onto._SendRequest_Buffer;
import agentPro_Prototype_Agents.WorkpieceAgent;
import agentPro_Prototype_Agents._Agent_Template;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BufferDeterminationBehaviour extends Behaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String logLinePrefix = ".BufferDeterminationBehaviour ";
	private WorkpieceAgent myAgent;
	private AllocatedWorkingStep relevant_AllWS;
	private int step = 0;
	private String[] resourceAgents_Names;
	private AID[] resourceAgents_AID;
	private int[] positions_in_allocatedWorkingSteps_List;
	private String conversationID = "Buffer_Determination";
	private int repliesCnt = 0;
	private float shared_minimum_buffer_stored = 0;
	private long reply_by_time = 50;		//09.07.18  vorher 5000??
	private long reply_by_date_message;
	private boolean buffer_place_needed;
	private Disturbance disturbance;

	public BufferDeterminationBehaviour(WorkpieceAgent myAgent, AllocatedWorkingStep relevant_allWS, boolean buffer_place_needed, Disturbance disturbance) {
		this.myAgent = myAgent;
		this.relevant_AllWS = relevant_allWS;
		if(myAgent.simulation_mode) {
			this.buffer_place_needed = !myAgent.simulation_mode;	//no buffer needed in case of simulation
			this.disturbance = disturbance;
		}else {
			this.buffer_place_needed = buffer_place_needed;
		}
		
	}

	@Override
	public void action() {
		switch (step) {
		case 0:
			System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+" Buffer Determination started");
			//find agents that have to be asked for buffer
			if(relevant_AllWS.getHasOperation().getType().equals("transport")) {
				//TBD
			}
			
			
			else {	//production
			//current & next production resource, next transport resource --> of the relevant allocated working step
			//send them a message --> request buffer time
				
			resourceAgents_Names = new String[3];
			resourceAgents_AID = new AID[3];
			positions_in_allocatedWorkingSteps_List = new int[3];
			
			/*
			 * here the agents and the positions in the allocated working step list are determined
			 */
			int counter = 0;
			int counter_2 = 0;
		    @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
		    while(it.hasNext()) {
		    	AllocatedWorkingStep allocWorkingstep = it.next();		
		    		if(allocWorkingstep.getHasOperation().getName().equals(relevant_AllWS.getHasOperation().getName())) {
		    	    	resourceAgents_Names[counter] = allocWorkingstep.getHasResource().getName();
		    	    	positions_in_allocatedWorkingSteps_List[counter] = counter_2;
		    	    	counter++;
		    	    	//counter_2++;
		    		}else if(counter == 1) {
		    			resourceAgents_Names[counter] = allocWorkingstep.getHasResource().getName();
		    	    	positions_in_allocatedWorkingSteps_List[counter] = counter_2;
		    	    	counter++;
		    	    	//counter_2++;
		    		}else if(counter == 2) {
		    			resourceAgents_Names[counter] = allocWorkingstep.getHasResource().getName();
		    	    	positions_in_allocatedWorkingSteps_List[counter] = counter_2;
		    	    	counter++;
		    	    	//counter_2++;
		    		}
		    		counter_2++;	    		
		    }
		    
		    for(int i = 0;i<resourceAgents_Names.length;i++) {
		    	for(DFAgentDescription agent : myAgent.getResourceAgents()) {		    		
		    		if(resourceAgents_Names[i] != null && resourceAgents_Names[i].equals(agent.getName().getLocalName())){
		    			resourceAgents_AID[i]=agent.getName();
		    		}
		    	}	    	
		    }
		    		    
			}
			//AllocatedWorkingStep all = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(positions_in_allocatedWorkingSteps_List[0]);
			//System.out.println("DEBUG_____________allocWS on position 4 (also position 0 im array für positions) = "+all.getHasOperation().getName());
	
			step = 1;
			break;
		case 1:
		    //prepare message
		    //REQUEST BUFFER
		 
			for (int i = 0;i<resourceAgents_Names.length;i++) {
				 //create ontology content
				Request_Buffer request_buffer = new Request_Buffer();
				request_buffer.addConsistsOfAllocatedWorkingSteps((AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(positions_in_allocatedWorkingSteps_List[i]));
				_SendRequest_Buffer sendRequest_Buffer = new _SendRequest_Buffer();
				sendRequest_Buffer.setHasRequest_Buffer(request_buffer);		
				Action content = new Action(this.getAgent().getAID(),sendRequest_Buffer);
			
				//create ACLMessage

				ACLMessage request_buffer_acl = new ACLMessage(ACLMessage.REQUEST);
				request_buffer_acl.addReceiver(resourceAgents_AID[i]);
				//conversationID = Long.toString(System.currentTimeMillis());	//TBD if this is suitable										
				request_buffer_acl.setConversationId(conversationID);
				request_buffer_acl.setReplyWith(Integer.toString(positions_in_allocatedWorkingSteps_List[i]));
				request_buffer_acl.setLanguage(myAgent.getCodec().getName());
				request_buffer_acl.setOntology(myAgent.getOntology().getName());
				
				
				//determine reply by time
				Date reply_by_date = new Date();
				long reply_by_date_long = 0;
				reply_by_date_long = System.currentTimeMillis()+reply_by_time ;
				reply_by_date = new Date(reply_by_date_long);
				this.reply_by_date_message = reply_by_date_long;
				request_buffer_acl.setReplyByDate(reply_by_date);				
				

			
				
				//ontology --> fill content
				try {
					myAgent.getContentManager().fillContent(request_buffer_acl, content);
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				myAgent.send(request_buffer_acl);
				System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+myAgent.logLinePrefix+" request Buffer sent to receiver "+resourceAgents_Names[i]+" with content "+request_buffer_acl.getContent());
				
			
			}
			block();
			step = 2;	
			break;
			
		case 2:
			//receive Messages
			/*
			 * works currently only for 1 buffer that comes back
			 * because of the position (reply with) --> könnte komma getrennt auch mit mehreren gehen
			 */
	        MessageTemplate mt = MessageTemplate.MatchConversationId(conversationID);	
			ACLMessage reply = myAgent.receive(mt);
		
			if (reply != null) {
				// Reply received
			
				int position_in_AllocWS = Integer.parseInt(reply.getReplyWith());
				//AllocatedWorkingStep alloc_WS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(position_in_AllocWS);
				
				try {
					Action act = (Action) myAgent.getContentManager().extractContent(reply);
					_SendInform_Buffer inform_Buffer = (_SendInform_Buffer) act.getAction();	
				    
					@SuppressWarnings("unchecked")
					Iterator<AllocatedWorkingStep> it = inform_Buffer.getHasInform_Buffer().getConsistsOfAllocatedWorkingSteps().iterator();
				    while(it.hasNext()) {
				    	AllocatedWorkingStep step_in_message = it.next();	
				    	AllocatedWorkingStep allWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(position_in_AllocWS);
				    	Operation op = (Operation) allWS.getHasOperation();
				    	op.setBuffer_before_operation(((Operation) step_in_message.getHasOperation()).getBuffer_before_operation());
				    	//((AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(position_in_AllocWS)).setBuffer_before_operation(step_in_message.getBuffer_before_operation());
				    	System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+myAgent.logLinePrefix+" buffer from "+reply.getSender().getLocalName()+" received. Buffer in min = "+((Operation) step_in_message.getHasOperation()).getBuffer_before_operation()/(1000*60));
				    }
					
					//buffer = infBuffer.getHasInform_Buffer().getBuffer();
					
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
			
				repliesCnt++;
				if (repliesCnt >= resourceAgents_Names.length) {
					// We received all replies
					step = 3; 
				} 
			}
			else {
				//block();
				if(System.currentTimeMillis()>=reply_by_date_message) {
					step = 3; 
				}
			}
			
			break;
		case 3:
			/*
			 * check whether there is a buffer at all three steps and what the buffer is
			 */
			boolean buffer_for_all_exists = false;	//soll true sein, wenn alle drei Buffer haben
			Float shared_minimum_buffer = null;
			
			for(int i = 0;i<3;i++) {
				AllocatedWorkingStep allWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(positions_in_allocatedWorkingSteps_List[i]);
				Operation op = allWS.getHasOperation();
				if(op.getBuffer_before_operation()>0) {
					buffer_for_all_exists = true;
					if(shared_minimum_buffer == null) {
						shared_minimum_buffer = op.getBuffer_before_operation();
					}else if(op.getBuffer_before_operation()<shared_minimum_buffer) {
						shared_minimum_buffer = op.getBuffer_before_operation();
					}
				}else {
					buffer_for_all_exists = false;
					shared_minimum_buffer = op.getBuffer_before_operation();
					shared_minimum_buffer_stored = shared_minimum_buffer;
				}
			}
			if(buffer_for_all_exists) {
				System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+myAgent.logLinePrefix+" buffer for all = "+(shared_minimum_buffer/(1000*60)));		    
				step = 4;
			}else {
				step = 5;
			}

			break;
		case 4:
			/*
			 * determine waiting time and arrange wake up --> TBD
			 */
			if(shared_minimum_buffer_stored == Long.MAX_VALUE) {
				System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+myAgent.logLinePrefix+" buffer for all is the maximum Buffer --> nothing has to be done ");		    
				step = 6;
			}
			
			
			if(_Agent_Template.simulation_mode) {	//check if we have to arrange new planning
				Machine_Error dis_type = (Machine_Error) disturbance.getHasDisturbanceType();
			
				if(shared_minimum_buffer_stored>=dis_type.getExpected_Duration_Of_Repair()) {		//nothing else has to be changed
					sendNewDatesToResources(); // TBD needs to be implemented
				}else {		
					buffer_place_needed = true;//new planning needed
					step = 5;
					break;
				}
				
			}else {
				boolean time_elapsed = true;	//workaround
				
				if(time_elapsed) {
					step = 5;
				}
			}
			
			
			break;
		case 5:
		
			/*
			 * new plan needed --> arrange replanning --> TBD
			 */
			boolean error_step_finished = false;	//determine if the current step (where the problem occured) is finished now --> has to come from the database (MES) TBD
			if(error_step_finished) {
				//determine alternative solutions for later steps
				
			}else if(!error_step_finished){
				//the current step needs to be cancelled and rebooked --> + all later steps if necessary
				//cancel & delete all steps in allocated that are not finished (but planned) --> they need to be rebooked
			
				//int i = 0;
				//ArrayList<String> operations_to_be_removed = new ArrayList <String>();
				
				//one message per step --> would be better to put all steps for one receiver in one message --> TBD
				
			    @SuppressWarnings("unchecked")
				Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
			    while(it.hasNext()) {
			    	AllocatedWorkingStep allWorkingStep = it.next();
			    	if(!allWorkingStep.getIsFinished()) {
			    	myAgent.cancelAllocatedWorkingSteps(allWorkingStep);
			    	it.remove();
			    	}
			    	
			    }
			    if(!buffer_place_needed) {	//no buffer place needed
			    	//restart production manager for next "official" production step
					myAgent.getProductionManagerBehaviour().setStep(0);
					myAgent.getProductionManagerBehaviour().restart();

			    }else {
			    	  myAgent.addBehaviour(new BookBufferPlaceProcedureBehaviour(myAgent, relevant_AllWS));
			    }
			  
	    
			}
			
			step = 6;
			break;
			
		}	
	}

	private void sendNewDatesToResources() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return step == 6;
	}
	

}
