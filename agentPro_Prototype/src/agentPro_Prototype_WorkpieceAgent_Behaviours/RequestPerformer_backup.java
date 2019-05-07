package agentPro_Prototype_WorkpieceAgent_Behaviours;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import agentPro.onto.Accept_Proposal;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.OrderedOperation;
import agentPro.onto.Proposal;
import agentPro.onto.Reject_Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Timeslot;
import agentPro.onto.Transport_Operation;
import agentPro.onto._SendAccept_Proposal;
import agentPro.onto._SendCFP;
import agentPro.onto._SendInform_Scheduled;
import agentPro.onto._SendProposal;
import agentPro.onto._SendReject_Proposal;
import agentPro_Prototype_Agents.WorkpieceAgent;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RequestPerformer_backup extends Behaviour {

	private static final long serialVersionUID = 1L;
	private int step = 0;
	
	private String logLinePrefix = ".RequestPerformer";
	private WorkpieceAgent myAgent;
	
	private String conversationID = "";
	
	//Send CFPs
	//private long deadline_for_this_task;
	private String service_type;
	private CFP cfp_sent;

	//receiveProposals
		private long reply_by_date_CFP; //
		private long reply_by_date_inform;
		private Long startdate_for_this_task;
		private AID bestSeller;
		private double bestPrice;
		private int proposal_id;
		private Proposal proposal_bestseller;
		private ArrayList<AID> resourceAgents = new ArrayList <AID>();
		private Operation requested_operation;
		private ArrayList<AID> proposal_senders = new ArrayList <AID>();
		private ArrayList<Proposal> received_proposals = new ArrayList <Proposal>();
		
	//arrange last transport
		private Boolean last_operation;
		private Boolean error_handling_active;
		
		//error handling
		private Timeslot timeslot_for_buffer_place;
		private long earliest_end;
		private String necessary_resource_agent_for_this_step;
	
	
	public RequestPerformer_backup(WorkpieceAgent myAgent, Operation requested_operation, Long startdate_for_this_task, Boolean last_operation, Boolean error_handling_active) { //start_date of production / startdate is null in case of transport 
		super(myAgent);
		this.myAgent = myAgent;	
		this.requested_operation = requested_operation;
		if(startdate_for_this_task != null) {
			this.startdate_for_this_task = startdate_for_this_task;
		}	
		this.conversationID = Long.toString(System.currentTimeMillis());
		this.logLinePrefix = this.logLinePrefix+"."+requested_operation.getName()+" ";
		this.last_operation = last_operation;
		this.error_handling_active = error_handling_active;
	}
	
	//this constructor is used fot transport steps (dont need startdate) and for error handling purposes which needs a given timeslot
	public RequestPerformer_backup(WorkpieceAgent myAgent, Operation buffer, Timeslot timeslot_to_book_buffer_place, Boolean last_operation, boolean error_handling_active) {
		super(myAgent);
		this.myAgent = myAgent;	
		this.requested_operation = buffer;
		
		this.conversationID = Long.toString(System.currentTimeMillis());
		this.logLinePrefix = this.logLinePrefix+"."+buffer.getName()+" ";
		this.last_operation = last_operation;
		this.error_handling_active = error_handling_active;
		this.timeslot_for_buffer_place = timeslot_to_book_buffer_place;


	}


	public RequestPerformer_backup(WorkpieceAgent myAgent, Operation requested_operation, long startdate_for_this_task,
			Boolean last_operation, boolean error_handling_active, String necessary_resource_agent_for_this_step) {
		super(myAgent);
		this.myAgent = myAgent;	
		this.startdate_for_this_task = startdate_for_this_task;
		this.requested_operation = requested_operation;		
		this.conversationID = Long.toString(System.currentTimeMillis());
		this.logLinePrefix = this.logLinePrefix+"."+requested_operation.getName()+" ";
		this.last_operation = last_operation;
		this.error_handling_active = error_handling_active;
		this.necessary_resource_agent_for_this_step = necessary_resource_agent_for_this_step;
	}

	public void action() {
		switch (step) {
		
		case 0:		
			service_type = requested_operation.getType();
			
			if(necessary_resource_agent_for_this_step != null) {
				for(DFAgentDescription a : myAgent.resourceAgents) {
					//System.out.println("DEBUG___________"+a.getName().getLocalName()+"  necessary_resource_agent_for_this_step  "+necessary_resource_agent_for_this_step);
					if(a.getName().getLocalName().equals(necessary_resource_agent_for_this_step)) {
						resourceAgents.add(a.getName());
						break;
					}
				}
			}else {
				//find agents with capability X
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				//service_type = requested_operation.getType();
				sd.setType(service_type); 	
				String service_name = null;
				if(service_type.equals("production")){							//es wird nach Anbietern von z.B. Fräsen gesucht
					String req_capability = requested_operation.getName();
					String[] parts = req_capability.split("_");	
					sd.setName(parts[0]);	//or requested_destination
					service_name = parts[0];
				}		
				
				template.addServices(sd);
				
				//check the agents we have already stored
				if(myAgent.resourceAgents.size() > 0) {
					//DFAgentDescription[] result = 
					for(DFAgentDescription a : myAgent.resourceAgents) {				
						  @SuppressWarnings("unchecked")
							Iterator<ServiceDescription> it = a.getAllServices();
						    while(it.hasNext()) {
						    	ServiceDescription service_description = it.next();
						    	if(service_description.getType().equals(service_type)) {
						    		if(service_type.equals("production")){
						    			if(service_description.getName().equals(service_name)) {
						    				resourceAgents.add(a.getName());
						    			}
						    		}else {
						    			resourceAgents.add(a.getName());
						    		}
						    		
						    	}
						    }
					}				
				}
				//if there is no agent with the needed capability stored, search the DF
				if(resourceAgents.size()==0) {	//TBD subscription!!		
				try {
						DFAgentDescription[] result = DFService.search(myAgent, template);
					if (result.length != 0){					
						//resourceAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							//resourceAgents[i] = result[i].getName();
							myAgent.addResourceAgent(result[i]);
							resourceAgents.add(result[i].getName());
						}
					}else {
						System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"no Agent with operation"+requested_operation.getName()+" was found");
						//Handling of this situation --> tbd
					}
				}

			
					 catch (FIPAException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					 }
				}	
			}
			

				

			//send CFP
				Date reply_by_date = new Date();
				long reply_by_date_long = 0;
				
				//create ontology content
				CFP cfp_onto = new CFP();
			
				
					//determine the requested timeslot (earliest start date, latest finish date) of the operation
					 Timeslot cfp_timeslot = new Timeslot();
					 if(error_handling_active) {
						 if(requested_operation.getType().equals("production") && timeslot_for_buffer_place == null) {
							 cfp_timeslot.setStartDate(String.valueOf(startdate_for_this_task));
							 cfp_timeslot.setEndDate(Long.toString(startdate_for_this_task+myAgent.getTime_until_end()));//e.g. end of the week --> tbd 
						 }else if(requested_operation.getType().equals("production") && timeslot_for_buffer_place != null){		//buffer must be scheduled
							 cfp_timeslot.setStartDate(timeslot_for_buffer_place.getStartDate());
							 cfp_timeslot.setEndDate(timeslot_for_buffer_place.getEndDate());//e.g. end of the week --> tbd 
							 
						 }else if(requested_operation.getType().equals("transport") && timeslot_for_buffer_place != null){
							 cfp_timeslot.setStartDate(timeslot_for_buffer_place.getStartDate());
							 cfp_timeslot.setEndDate(timeslot_for_buffer_place.getEndDate());//e.g. end of the week --> tbd 
						 }else {
									 cfp_timeslot.setStartDate(String.valueOf(startdate_for_this_task));
									 cfp_timeslot.setEndDate(Long.toString(startdate_for_this_task+2*myAgent.getTransport_estimation()));//e.g. end of the week --> tbd 
								 
						 }
						 
					 }else {
						 cfp_timeslot = determineCFPTimeslot();	 
					 }
				
					 cfp_onto.setHasTimeslot(cfp_timeslot);
					cfp_onto.setHasOperation(requested_operation);
					cfp_onto.setHasSender(myAgent.getAID());
					//26.02.2019 add quantity
					cfp_onto.setQuantity(myAgent.getOrderPos().getQuantity());
				cfp_sent = cfp_onto;	
				
								
				_SendCFP sendCFP = new _SendCFP();
				sendCFP.setHasCFP(cfp_onto);			
				Action content = new Action(myAgent.getAID(),sendCFP);
				//Action content = new Action(myAgent.getAID(),cfp_onto);
		
				//create ACL Message
				
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				//cfp.setContent(message_content);	
				cfp.setLanguage(myAgent.getCodec().getName());
				cfp.setOntology(myAgent.getOntology().getName());
				cfp.setConversationId(conversationID);
				
				//determine reply by time
				reply_by_date_long = System.currentTimeMillis()+myAgent.getProductionManagerBehaviour().reply_by_time;
				reply_by_date = new Date(reply_by_date_long);
				this.reply_by_date_CFP = reply_by_date_long;
				cfp.setReplyByDate(reply_by_date);
				
				
			
				//ontology --> fill content
				try {
					myAgent.getContentManager().fillContent(cfp, content);
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
								
			
				for (int i = 0;i<resourceAgents.size();i++){
					cfp.addReceiver(resourceAgents.get(i));
					//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"cfp sent to receiver: "+result[i].getName().getLocalName()+" with content "+cfp.getContent());
				}
				//myAgent.printOutSent(cfp);			
				myAgent.send(cfp);
			step = 1;
			block((long) (0.5*myAgent.getProductionManagerBehaviour().reply_by_time));
			break;
		case 1:	
			//deadline	
			//if deadline expired or all proposals are received --> book best offer
			if(System.currentTimeMillis()>reply_by_date_CFP || resourceAgents.size() == received_proposals.size()) {
				step = 6;
				break;
			}else if(System.currentTimeMillis()<=reply_by_date_CFP){ 
				step = 2;				
				block(10);
				break;
			} 
			
			//break;
		case 2:

			// Receive all proposals

						MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
				        MessageTemplate mt2 = MessageTemplate.MatchConversationId(conversationID);	
				        MessageTemplate mt_total = MessageTemplate.and(mt1,mt2);
				        
				        MessageTemplate ref = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
				        MessageTemplate ref2 = MessageTemplate.MatchConversationId(conversationID);
				        MessageTemplate mt_total_ref = MessageTemplate.and(ref,ref2);
				        
						ACLMessage reply = myAgent.receive(mt_total);
						if (reply != null) {
							//ontology
							//System.out.println("DEBUG_______proposal received at______"+System.currentTimeMillis());
							
							
							float price = 0;
							Proposal proposal = new Proposal();
							try {
								Action act = (Action) myAgent.getContentManager().extractContent(reply);
								_SendProposal proposal_onto = (_SendProposal) act.getAction();
												
								proposal = proposal_onto.getHasProposal();
								price = proposal.getPrice();
								received_proposals.add(proposal);	
								proposal_senders.add(reply.getSender());
							
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
							
							
							// Reply received
							long enddate = 0;
							 @SuppressWarnings("unchecked")
								Iterator<AllocatedWorkingStep> i = proposal.getConsistsOfAllocatedWorkingSteps().iterator();
							    while(i.hasNext()) {
							    	AllocatedWorkingStep allocWorkingstep = i.next();
							    	enddate = Long.parseLong(allocWorkingstep.getHasTimeslot().getEndDate());
							    }
							//if (bestSeller == null || price < bestPrice) {
							//get the earliest
							if (bestSeller == null || enddate < earliest_end) {	
							// This is the best offer at present							
								
							bestPrice = price;
							earliest_end = enddate;
							bestSeller = reply.getSender();
							proposal_id = proposal.getID_Number();
							proposal_bestseller = proposal;
							
							}else if(enddate == earliest_end) {	//if equal --> cheapest
								if(price < bestPrice) {
									bestPrice = price;
									earliest_end = enddate;
									bestSeller = reply.getSender();
									proposal_id = proposal.getID_Number();
									proposal_bestseller = proposal;
								}							
							}
						//step = 1;
						
						}
						else { 
							ACLMessage refusal = myAgent.receive(mt_total_ref);	
							if(refusal != null) {
								Proposal ref_workaraound = new Proposal();
								received_proposals.add(ref_workaraound);		//this empty proposal is added to enable the check in step = 1 to recongnize the answer
								proposal_senders.add(refusal.getSender());
							}
							//step = 1;
								
						}
			step = 1;			
			break;
			
		case 3:
			//book best offer
			
			//create ontology content
			Accept_Proposal accept_Proposal_onto = new Accept_Proposal();
			accept_Proposal_onto.setHasProposal(proposal_bestseller);
			_SendAccept_Proposal sendAcceptProposal = new _SendAccept_Proposal();
			sendAcceptProposal.setHasAccept_Proposal(accept_Proposal_onto);					//if necessary there can be more than one allocatedWorkingStep inside a Proposal
			Action content2 = new Action(this.getAgent().getAID(),sendAcceptProposal);
			
			//create ACLMessage
				ACLMessage accept_proposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				accept_proposal.addReceiver(bestSeller);
				accept_proposal.setConversationId(conversationID);
				accept_proposal.setLanguage(myAgent.getCodec().getName());
				accept_proposal.setOntology(myAgent.getOntology().getName());
				//determine reply by time
				Date reply_by_date2 = new Date();
				long reply_by_date_long2 = 0;
				reply_by_date_long2 = System.currentTimeMillis()+myAgent.getProductionManagerBehaviour().reply_by_time;
				reply_by_date2 = new Date(reply_by_date_long2);
				this.reply_by_date_inform = reply_by_date_long2;
				accept_proposal.setReplyByDate(reply_by_date2);
				
				accept_proposal.setInReplyTo(Integer.toString(proposal_id));
				
				//ontology --> fill content
				try {
					myAgent.getContentManager().fillContent(accept_proposal, content2);
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				myAgent.send(accept_proposal);
				//System.out.println("DEBUG_______accept_Proposal sent at______"+System.currentTimeMillis());
				myAgent.printOutSent(accept_proposal);
				//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" order to offer "+proposal_id+" sent to receiver "+bestSeller.getLocalName());
				
			
			//reject the rest
				for(int i = 0;i<proposal_senders.size();i++) {
					AID sender_of_proposal = proposal_senders.get(i);
					if(!sender_of_proposal.getLocalName().equals(bestSeller.getLocalName()) && received_proposals.get(i).getID_Number() != 0) {
						Proposal proposal_to_reject = received_proposals.get(i);
						Reject_Proposal reject_Proposal_onto = new Reject_Proposal();
						reject_Proposal_onto.setID_Number(proposal_to_reject.getID_Number());
					
						reject_Proposal_onto.setConsistsOfAllocatedWorkingSteps(proposal_to_reject.getConsistsOfAllocatedWorkingSteps());
						_SendReject_Proposal sendRejectProposal = new _SendReject_Proposal();
						sendRejectProposal.setHasReject_Proposal(reject_Proposal_onto);
						
						Action content3 = new Action(this.getAgent().getAID(),sendRejectProposal);
						
						//create ACLMessage
							ACLMessage reject_proposal_acl = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
							reject_proposal_acl.addReceiver(sender_of_proposal);
							reject_proposal_acl.setConversationId(conversationID);
							reject_proposal_acl.setInReplyTo(Integer.toString(proposal_to_reject.getID_Number()));
							reject_proposal_acl.setLanguage(myAgent.getCodec().getName());
							reject_proposal_acl.setOntology(myAgent.getOntology().getName());
							
							
							//ontology --> fill content
							try {
								myAgent.getContentManager().fillContent(reject_proposal_acl, content3);
							} catch (CodecException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (OntologyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							myAgent.send(reject_proposal_acl);						
							System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" reject proposal to proposal "+received_proposals.get(i).getID_Number()+" sent to receiver "+sender_of_proposal.getLocalName());
							
						
					}
				}
				
				
				
			step = 4;			
			
			break;
		case 4:
			//delay			
			if(System.currentTimeMillis()<=reply_by_date_inform){ //go to step 5 after deadline expired
				step = 5;	
				
			}else if(System.currentTimeMillis()>reply_by_date_inform) {
				System.out.println(System.currentTimeMillis()+"  DEBUG______________ERROR_____reply_by_date_inform abgelaufen in RequestPerformer "+myAgent.getLocalName()+"_____this should not be needed!");
				step = 7;
			}
			break;
			
		case 5:      
			// Receive inform if allocated step is scheduled

			MessageTemplate mt3 = MessageTemplate.MatchInReplyTo(Integer.toString(proposal_id));
	        MessageTemplate mt4 = MessageTemplate.MatchConversationId(conversationID);	
	        MessageTemplate mt_total2 = MessageTemplate.and(mt3,mt4);
	        
			ACLMessage reply_inform = myAgent.receive(mt_total2);
			if (reply_inform != null) {	// Reply received
				//System.out.println("DEBUG_______inform received at______"+System.currentTimeMillis());
				
				if(reply_inform.getPerformative() == ACLMessage.FAILURE) {
					System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" order to offer "+proposal_id+" sent to receiver "+bestSeller.getLocalName()+" cannot be carried out");
					//restart last step
					step = 0;
					break;
					
				}else if(reply_inform.getPerformative() == ACLMessage.INFORM) {
					System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" order to offer "+proposal_id+" sent to receiver "+bestSeller.getLocalName()+" successfully scheduled.");

					//add step to workplan & determine Resource (that has Location)  --> can be used to determine transport destination
					Resource res = new Resource();
					Operation operation = new Operation();
					AllocatedWorkingStep step_to_give_to_Inform_DepartureAndArrival = null;
					AllocatedWorkingStep booked_production_step = null;
					Transport_Operation transport_operation = null;
					
					try {
						Action act = (Action) myAgent.getContentManager().extractContent(reply_inform);
						_SendInform_Scheduled infSched = (_SendInform_Scheduled) act.getAction();
									
					    @SuppressWarnings("unchecked")
						Iterator<AllocatedWorkingStep> it = infSched.getHasInform_Scheduled().getConsistsOfAllocatedWorkingSteps().iterator();
					    while(it.hasNext()) {
					    	AllocatedWorkingStep allocWorkingstep_fromInformScheduled = it.next();
					    	res = allocWorkingstep_fromInformScheduled.getHasResource();
					    	operation = allocWorkingstep_fromInformScheduled.getHasOperation();				    
					    	
					    	//check if this step is an element of the production plan
					    	boolean element_of_production_plan = myAgent.check_if_element_of_production_plan(allocWorkingstep_fromInformScheduled);
					    	if(allocWorkingstep_fromInformScheduled.getHasOperation().getType().equals("production") && !element_of_production_plan) {
					    		allocWorkingstep_fromInformScheduled.setIsErrorStep(true);
					    	}
					    	
					    	Boolean entry_failed;
					    	entry_failed = myAgent.checkConsistencyAndAddStepToWorkplan(allocWorkingstep_fromInformScheduled);
					    
					    	if(entry_failed) {
					    		//error handling TBD
					    		System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"allocatedWorkingStep "+allocWorkingstep_fromInformScheduled.getHasOperation().getName()+" could not be added");								
					    	}else {
					    		if(requested_operation.getType().equals("transport")) {		
					    			
					    			step_to_give_to_Inform_DepartureAndArrival = allocWorkingstep_fromInformScheduled;
					    			//new 15.06.2018 adjust production step if earlier
					    			transport_operation = (Transport_Operation) operation;
					    			Location endlocation = (Location) operation.getEndState();
					    			String time_of_arrival_so_production_can_start = allocWorkingstep_fromInformScheduled.getHasTimeslot().getEndDate();
					    			System.out.println("DEBUG request performer Test 1 time_of_arrival "+time_of_arrival_so_production_can_start);
					    			//adjust the production step
					    			@SuppressWarnings("unchecked")
					    			Iterator<AllocatedWorkingStep> ite = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
					    		    while(ite.hasNext()) {
					    		    	AllocatedWorkingStep allocWS = ite.next();
					    		    	if(allocWS.getHasOperation().getType().equals("production") && myAgent.doLocationsMatch(allocWS.getHasResource().getHasLocation(), endlocation)) {		//find the right production step (location)				    		    		
					    		    		//Workpiece Agent does not care if he blocks the resource earlier --> he only saves the actual operations
					    		    		
					    		    		//long new_start_date_for_blocking = Long.parseLong(time_of_arrival_so_production_can_start);// - transport_operation.getAvg_PickupTime()*60*1000; 		    		
					    		    		allocWS.getHasTimeslot().setStartDate(time_of_arrival_so_production_can_start); //time of arrival = fertig mit pick up
					    		    		long new_enddate = Long.parseLong(time_of_arrival_so_production_can_start)+(long)(allocWS.getHasOperation().getAvg_Duration()*60*1000); 
					    		    		allocWS.getHasTimeslot().setEndDate(String.valueOf(new_enddate));
					    		    		System.out.println("DEBUG requestperformer Test 2 set start date "+time_of_arrival_so_production_can_start+" and enddate "+new_enddate+" duration "+(long)allocWS.getHasOperation().getAvg_Duration());
					    		    	}
					    		    }
					    		    
					    		}else if(requested_operation.getType().equals("production")) {
					    			booked_production_step = allocWorkingstep_fromInformScheduled;
					    		}
					    		
					    	}	
							//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"allocatedWorkingStep added: Operation: "+allocWorkingstep.getHasOperation().getName()+" Resource: "+allocWorkingstep.getHasResource().getName()+" timeslot_start: "+myAgent.SimpleDateFormat.format(Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate()))+" timeslot_end: "+myAgent.SimpleDateFormat.format(Long.parseLong(allocWorkingstep.getHasTimeslot().getEndDate())));
							//if error handling is active --> finish here
							if(error_handling_active) {
								System.out.println("DEBUG__step = 7");
								step = 7;
								//break;	04.02.2019 break verschoben
							}
							break;
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
					
					//arrange transport
				 	
					if(!error_handling_active && operation.getType().equals("production")) {
						//if backw sch was active the workplan needs to be sorted again 
						if(myAgent.getProductionManagerBehaviour().isBackwards_scheduling_activ() && !last_operation) {
							myAgent.getProductionManagerBehaviour().sortWorkplanChronologically();
							//set backwards scheduling active back to false
							myAgent.getProductionManagerBehaviour().setBackwards_scheduling_activ(false);
							//and we dont need to book the transport again because we already have it planned
							//restart production manager for next production step
							myAgent.getProductionManagerBehaviour().setStep(0);
							myAgent.getProductionManagerBehaviour().restart();
							step = 7;
							break;
						}else if(myAgent.getProductionManagerBehaviour().isBackwards_scheduling_activ() && last_operation) {
							myAgent.getProductionManagerBehaviour().sortWorkplanChronologically();
							//set backwards scheduling active back to false
							myAgent.getProductionManagerBehaviour().setBackwards_scheduling_activ(false);
							
							arrangeTransportToWarehouse();
							step = 7;
							
							break;
						}
						
						
						//PRÜFUNG Sind wir schon da wo wir sein müssen?	Wenn nein --> Transport arrangieren					
						 if(myAgent.doLocationsMatch(myAgent.getLocation(), res.getHasLocation())) {
							 	myAgent.getProductionManagerBehaviour().setStep(0);
								myAgent.getProductionManagerBehaviour().restart();
								step = 7; 
								break;
							 
						 }else {		//arrange transport to the ressource where production step will be fulfilled
						 	
							 //TBD  known is the needed arrival date from the scheduled production step --> that needs to be the enddate of the transport
							 //(TBD Check ob aktuelle Koordinaten = einer Ressource zuzuordnen sind--> muss dann nur einmal gemacht werden)
						//long finish_date_for_transport = new Long(startdate_for_this_task);									
						Transport_Operation transport_operation_local = new Transport_Operation();									//
						transport_operation_local.setType("transport");
						
						Location location = determineStartLocationForTransportOperation();
						
						transport_operation_local.setStartState(location);
						transport_operation_local.setEndState(res.getHasLocation());
						
						//Name = Start_Ziel in format  X;Y_DestinationResource
						transport_operation_local.setName(location.getCoordX()+";"+location.getCoordY()+"_"+res.getName());
						transport_operation_local.setAppliedOn(myAgent.getRepresented_Workpiece());
						transport_operation_local.setBuffer_before_operation(((Operation) booked_production_step.getHasOperation()).getBuffer_before_operation());
										
						myAgent.addBehaviour(new RequestPerformer_backup(myAgent, transport_operation_local, null, last_operation, false)); //null = start_date_for_this_task
						step = 7; 
						break;
						}
						

					}
					//nach erfolgter Buchung des Transports --> Neustart Production Manager
					if(!error_handling_active && operation.getType().equals("transport")) {
						//System.out.println("DEBUG_______________"+myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" test ");
						
						//Mitteilen an Ressource, wann Werkstück abgeholt bzw. gebracht wird --> Abholen beim einen + duration = bringen beim nächsten
							myAgent.addBehaviour(new InformWorkpieceArrivalAndDeparture(myAgent, transport_operation, step_to_give_to_Inform_DepartureAndArrival));
							//System.out.println("DEBUG______informWP Arrival and Departure added");
						
						
						//if this was the transport to the last production step (but not the transport to warehouse) --> arrange transport to warehouse after the production step
						if(last_operation != null && last_operation && !myAgent.getProductionManagerBehaviour().isBackwards_scheduling_activ()) {	//last operation is given from the production step before	 
								arrangeTransportToWarehouse();															
								step = 7;
								break;
								
						}
						//the missing production step has to be arranged
						else if(last_operation != null && last_operation && myAgent.getProductionManagerBehaviour().isBackwards_scheduling_activ()) {
							myAgent.getProductionManagerBehaviour().setStep(0);
							myAgent.getProductionManagerBehaviour().restart();
							step = 7;
							break;
							
						}
						//the transport to warehouse has been arranged --> last operation is only null in that case
						//for transport operations the startdate is usually null as it can be calculated from the workplan --> only in case of error step it is not null							
						else if(last_operation == null && startdate_for_this_task == null) {	
							//System.out.println("DEBUG_______________"+myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" test 2");
							myAgent.printoutWorkPlan();
							myAgent.getProductionManagerBehaviour().setStep(2);
							myAgent.getProductionManagerBehaviour().restart();
							step = 7;
							break;	
							
						}
						/*else if(startdate_for_this_task != null) {
							
						}*/
						//System.out.println("DEBUG_______________"+myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" test 3");
						
						//restart production manager for next production step
						myAgent.getProductionManagerBehaviour().setStep(0);
						myAgent.getProductionManagerBehaviour().restart();
						step = 7;
						break;

						}//if operation type = transport
					
					}//reply inform_scheduled performative = success received
				
					
					
			}//reply received
		
			else { 
				step = 4;
					block(10);
					break;
			}
			System.out.println("DEBUG_________WWWWWWWWWWWWWWWWWWWWWWWWWQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ___________BEFORE STEP = 6 break ");
			break;
		case 6:
			
			if(bestSeller != null){ //best one found
				System.out.println(System.currentTimeMillis()+" "+myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"Best Proposal from "+bestSeller.getLocalName()+" with price: "+bestPrice+" received.");
				
				//TBD Here can be checked, if the conditions of the proposal are ok 
		    	//--> timeslot good enough? Durch die Strafkosten für Proposals, die die Bedingungen des CFP nicht erfüllen, wird der bestseller immer besser oder gleich gut sein, wie andere Angebote
				//der bestseller wird also nur die Zeit nicht einhalten, wenn die Zeit von keinem eingehalten werden kann
					
					//why is this check here and not later? TBD
					if(service_type.equals("transport")) {	
						//Resource res = new Resource();
						//Operation operation = new Operation();
						Timeslot timeslot_proposal = new Timeslot();
						
						@SuppressWarnings("unchecked")
						Iterator<AllocatedWorkingStep> it = proposal_bestseller.getConsistsOfAllocatedWorkingSteps().iterator();
					    while(it.hasNext()) {
					    	AllocatedWorkingStep allocWorkingstep = it.next();			//proposed allocated working step (attribute: price, resource, timeslot, operation)
					    	//res = allocWorkingstep.getHasResource();
					    	//operation = allocWorkingstep.getHasOperation();
					    	timeslot_proposal = allocWorkingstep.getHasTimeslot();		
					    }
					  //can the transport proposal fulfill the desired latest end to match the already scheduled next production step?
						//if that is not the case --> schedule this transport step and reschedule the production step
						//TBD if the best price is the right measurement 
					  				    
					    if(Long.parseLong(timeslot_proposal.getEndDate())>Long.parseLong(cfp_sent.getHasTimeslot().getEndDate())) {
							
							//int size = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size();
							//AllocatedWorkingStep step_to_be_cancelled = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(size-1);
							AllocatedWorkingStep step_to_be_cancelled = null;
							
							//find step to be cancelled
							 @SuppressWarnings("unchecked")
							Iterator<AllocatedWorkingStep> iter = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
							  while(iter.hasNext()) {
								  AllocatedWorkingStep current_step = iter.next();
								  Transport_Operation transp_op =  (Transport_Operation) requested_operation;

								  ///check whether the end of the transport operation (that is too late) is the same location as the current (production) resource TBD--> locations of transport resources in Workplan?
								  Boolean doLocationsMatch = myAgent.doLocationsMatch(current_step.getHasResource().getHasLocation(), (Location) transp_op.getEndState());
								  if(doLocationsMatch && current_step.getHasOperation().getType().equals("production")) {
									  long current_startdate_production = Long.parseLong(cfp_sent.getHasTimeslot().getEndDate());
									  float buffer_to_start_later_production = ((Operation)current_step.getHasOperation()).getBuffer_after_operation();
									  //check if the buffer is big enough to just adjust the operation
									  if(Long.parseLong(timeslot_proposal.getEndDate())> (current_startdate_production+buffer_to_start_later_production)){
										  System.out.println("DEBUG__________________PRODUCTION MUST BE RESCHEDULED");
										  step_to_be_cancelled = current_step;	
											//remove that element from the list --> results in automatic new scheduling by the production manager
											iter.remove();
											break;
									  }else {
										  step = 3;	//If there is no more message that can be retrieved --> go to step 3 and book offer
											break;

									  }
									
								}
							}
	
							  if(step_to_be_cancelled != null) {
								  myAgent.cancelAllocatedWorkingSteps(step_to_be_cancelled);
								 //unteres verschoben von nach der Klammer nach hier	
								  myAgent.getProductionManagerBehaviour().setBackwards_scheduling_activ(true);	
									System.out.println("DEBUG____________BACKWARDS     SCHEDULING      ACTIVE !!!!!!!!!!!!!!!");
							  }
							
							//remove that element from the list --> results in automatic new scheduling by the production manager
							//myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().remove(size-1); //TBD

							
							
						}
					}	
					
				
				
				
				step = 3;	//If there is no more message that can be retrieved --> go to step 3 and book offer
				break;
			}else{					//no one found
				System.out.println(System.currentTimeMillis()+" "+myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"No proposals for Orderposition "+myAgent.getOrderPos().getSequence_Number()+" and operation "+requested_operation.getName()+" received.");
				step = 7;
				//better handling --> tbd
			}
			break;
		}//switch        
	}

	private void arrangeTransportToWarehouse() {
		
		
		Transport_Operation transport_operation = new Transport_Operation();									//
		transport_operation.setType("transport");
		transport_operation.setAppliedOn(myAgent.getRepresented_Workpiece());
		Location startlocation = new Location();
		
		//startlocation now needs to be the last element
			int sizeOfAllWSs = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size();
			//AllocatedWorkingStep LAST_alWS_Production = null;
			for(int i = sizeOfAllWSs; i>0 ; i--) {
				AllocatedWorkingStep alWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(i-1);
				if(alWS.getHasOperation().getType().equals("production")) {
					startlocation = alWS.getHasResource().getHasLocation();
										
					break;
					
				}
			}
			
			if(myAgent.doLocationsMatch(startlocation, myAgent.getOrderPos().getHasTargetWarehouse().getHasLocation())) {
				myAgent.printoutWorkPlan();
				myAgent.getProductionManagerBehaviour().setStep(2);
				myAgent.getProductionManagerBehaviour().restart();
				step = 7;
			}else {
				transport_operation.setStartState(startlocation);
				transport_operation.setEndState(myAgent.getOrderPos().getHasTargetWarehouse().getHasLocation());
				
				
				//Name = Start_Ziel in format  X;Y_DestinationResource
				transport_operation.setName(startlocation.getCoordX()+";"+startlocation.getCoordY()+"_"+myAgent.getOrderPos().getHasTargetWarehouse().getName());
				transport_operation.setBuffer_before_operation(2*60*60*1000); //e.g. 2 hours --> does not matter				
				myAgent.addBehaviour(new RequestPerformer_backup(myAgent, transport_operation, null, null, false));
			}
		
		
	}
/*
	private boolean check_if_element_of_production_plan(AllocatedWorkingStep allocWorkingstep) {
		boolean found = false;
		 @SuppressWarnings("unchecked")
			Iterator<OrderedOperation> it = myAgent.getProdPlan().getAllConsistsOfOrderedOperations();
		    while(it.hasNext()) {
		    	OrderedOperation orOp = it.next();			
		    	if(orOp.getHasOperation().getName().equals(allocWorkingstep.getHasOperation().getName())){
		    		found = true;
		    	}
		    	
		    }
		    
		return found;
	}*/


	/*
	 * 180104 This method needs to determine, which location is to be used
	 * if its the initial scheduling process, the start location would be Warehouse_inbound
	 * Otherwise it can be
	 * 	- the current location of the Workpiece as a start (somewhere on the shopfloor, e.g. at a Resource
	 * 	- if some steps are already scheduled and maybe even fulfilled --> take the last step scheduled as start location
	 * 
	 */
	private Location determineStartLocationForTransportOperation() {
		Location location = new Location();
		int sizeOfAllWSs = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size();
		AllocatedWorkingStep LAST_alWS_Production = myAgent.getLastProductionStepAllocated();
		AllocatedWorkingStep BEFORE_LAST_alWS_Production = myAgent.get_BEFORE_LastProductionStepAllocated();
		//System.out.println("DEBUG______req performer determine start location BEFORE_LAST_alWS_Production "+BEFORE_LAST_alWS_Production.getHasOperation().getName()+" LAST_alWS_Production "+LAST_alWS_Production.getHasOperation().getName());
		
			if(sizeOfAllWSs == 1 || LAST_alWS_Production.getIsFinished()) {		//if the first Operation was just scheduled or if the last allocatedWStep is already finished, there are no new steps planned which would have to be used as a start and a new planning has started
				location = myAgent.getLocation();
			}else if (BEFORE_LAST_alWS_Production != null)  {		//StartLocation would be the location of the BEFORE LAST operation because the last operation is the destination	
				location = BEFORE_LAST_alWS_Production.getHasResource().getHasLocation();
			}
			/*
			else { //before last alWS is null if it was cancelled due to backwards scheduling	 
				location = LAST_alWS_Production.getHasResource().getHasLocation();
			}*/
		return location;
	}

	private Timeslot determineCFPTimeslot() {
		Timeslot timeslot = new Timeslot();
		
		int sizeOfAllWSs = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size();
		AllocatedWorkingStep LAST_alWS_Production = myAgent.getLastProductionStepAllocated();
		AllocatedWorkingStep BEFORE_LAST_alWS_Production = myAgent.get_BEFORE_LastProductionStepAllocated();	
		
		switch (requested_operation.getType()) {
		case "production":		
			
			if(LAST_alWS_Production != null && LAST_alWS_Production.getIsFinished() && !myAgent.getProductionManagerBehaviour().isBackwards_scheduling_activ()) {		//if if the last allocatedWStep is already finished, there are no new steps planned which would have to be used as a start and a new planning has started
				//timeslot.setStartDate(Long.toString(System.currentTimeMillis()));
				//Workaraound needed because current time is before the finished step   TBD			
				//timeslot.setStartDate(Long.toString(startdate_for_this_task));
				//start for the next production step is end of last + transport time
				timeslot.setStartDate(Long.toString(Long.parseLong(LAST_alWS_Production.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation()));
				
			}else if(LAST_alWS_Production != null && !LAST_alWS_Production.getIsFinished() && !myAgent.getProductionManagerBehaviour().isBackwards_scheduling_activ()){	//we are within a new planning cycle	
				timeslot.setStartDate(Long.toString(Long.parseLong(LAST_alWS_Production.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation()));
				
			}else if(myAgent.getProductionManagerBehaviour().isBackwards_scheduling_activ()) {
				//get last element in allocated Working steps
				AllocatedWorkingStep LAST_alWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(sizeOfAllWSs-1);
				//this element must be a transport step
				timeslot.setStartDate(LAST_alWS.getHasTimeslot().getEndDate());				
				
			}
			else {		//first step to be scheduled
				timeslot.setStartDate(Long.toString(startdate_for_this_task));
				
			}

			//timeslot.setStartDate(Long.toString(startdate_for_this_task));
			String enddate_for_this_task = Long.toString(startdate_for_this_task+myAgent.getTime_until_end());//e.g. end of the week --> tbd
			
			timeslot.setEndDate(enddate_for_this_task);
			break;
			
			/*
			 * for transport resources the end date needs to be the date given as input (which is the startdate of the following production step)
			 * minus (the estimated duration + puffer)
			 * the startdate (earliest) can either be NOW or the end of the before last production step scheduled
			 */
		case "transport":
			//TBD ---->   Wofür wird LAST_alWS_Production.getIsFInished() gebraucht???
			if(sizeOfAllWSs == 1 || LAST_alWS_Production.getIsFinished()) {		//if the first Operation was just scheduled or if the last allocatedWStep is already finished, there are no new steps planned which would have to be used as a start and a new planning has started
				//timeslot.setStartDate(Long.toString(System.currentTimeMillis()));
					//Last_AlWS is in the future (for size = 1)
				timeslot.setStartDate(Long.toString(Long.parseLong(LAST_alWS_Production.getHasTimeslot().getStartDate())-myAgent.getTransport_estimation()*2));	//buffer (*2) because its the first operation and the start date is not really important
				timeslot.setEndDate(LAST_alWS_Production.getHasTimeslot().getStartDate());
				
			}else if(last_operation == null){		//the last operation has been scheduled --> arrange transport to warehouse				
				timeslot.setStartDate(LAST_alWS_Production.getHasTimeslot().getEndDate());
				timeslot.setEndDate(Long.toString(Long.parseLong(LAST_alWS_Production.getHasTimeslot().getEndDate())+2*myAgent.getTransport_estimation()));
		
			}
			/*
			else if (BEFORE_LAST_alWS_Production.getIsFinished()) {	//a planning cycle because of a disruption has started and one new production step (e.g. buffering) has been scheduled
				timeslot.setStartDate(Long.toString(Long.parseLong(LAST_alWS_Production.getHasTimeslot().getStartDate())-myAgent.getTransport_estimation()*2));	//buffer (*2) because its the first operation and the start date is not really important
				timeslot.setEndDate(LAST_alWS_Production.getHasTimeslot().getStartDate());
			}*/
			else { //Also for error_handing because the first step gets its start_date at another place. Starting time would be the end of the BEFORE LAST operation because the last operation is the destination	
				timeslot.setStartDate(BEFORE_LAST_alWS_Production.getHasTimeslot().getEndDate());
				timeslot.setEndDate(LAST_alWS_Production.getHasTimeslot().getStartDate());	
			}

			//timeslot.setEndDate(Long.toString(startdate_for_this_task));	//enddate for transportation = startdate of production
			break;
			
		}
		
	
		return timeslot;
	}
	

	public boolean done() {
	
		return step == 7;
	}
}  