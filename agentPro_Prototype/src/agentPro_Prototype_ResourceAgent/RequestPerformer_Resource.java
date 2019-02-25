package agentPro_Prototype_ResourceAgent;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import agentPro.onto.Accept_Proposal;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;

import agentPro.onto.Operation;

import agentPro.onto.Proposal;
import agentPro.onto.Reject_Proposal;

import agentPro.onto.Timeslot;
import agentPro.onto.Transport_Operation;
import agentPro.onto._SendAccept_Proposal;
import agentPro.onto._SendCFP;

import agentPro.onto._SendProposal;
import agentPro.onto._SendReject_Proposal;
import agentPro_Prototype_Agents.ResourceAgent;

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

public class RequestPerformer_Resource extends Behaviour {

	private static final long serialVersionUID = 1L;
	private int step = 0;
	
	private String logLinePrefix = ".RequestPerformer_Resource";
	private ResourceAgent myAgent;
	private long reply_by_time; //reply within x seconds
	private String conversationID = "";
	
	//Send CFPs
	//private long deadline_for_this_task;
	private String service_type = "shared_resource";
	//private CFP cfp_sent;

	//receiveProposals
		private long reply_by_date_CFP; //
		private long reply_by_date_inform;
		//private Long startdate_for_this_task;
		private Timeslot timeslot_for_proposal;
		private AID bestSeller;
		private double bestPrice;
		private long earliest_end;
		private int proposal_id;
		private Proposal proposal_bestseller;
		private ArrayList<AID> resourceAgents = new ArrayList<AID>();
		private String requested_capability_shared_resource;
		private ArrayList<AID> proposal_senders = new ArrayList <AID>();
		private ArrayList<Proposal> received_proposals = new ArrayList <Proposal>();
		
		//error handling
		//private Timeslot timeslot_for_buffer_place;
		private Boolean error_handling_active;
		private Operation requested_operation;
		private int index;
		//private Boolean [] shared_resource_asked;
		//private Boolean [] shared_resource_available;
	
	public RequestPerformer_Resource(ResourceAgent myAgent, String requested_capability, Timeslot timeslot_for_schedule, Boolean error_handling_active, int i, Operation requested_operation, long reply_by_time) { //start_date of production / startdate is null in case of transport 
		super(myAgent);
		this.myAgent = myAgent;	
		this.requested_capability_shared_resource = requested_capability;
		this.timeslot_for_proposal = timeslot_for_schedule;
		this.conversationID = requested_capability+"."+Long.toString(System.currentTimeMillis());
		this.logLinePrefix = this.logLinePrefix+"."+requested_capability_shared_resource+" ";
		this.requested_operation = requested_operation;
		this.error_handling_active = error_handling_active;
		index = i;
		//this.shared_resource_asked = shared_resource_asked;
		//this.shared_resource_available = shared_resource_available;
		this.reply_by_time = reply_by_time;
		
	}
	
	public void action() {
		switch (step) {		
		case 0:		
			//find agents with capability X
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType(service_type); 	
			sd.setName(requested_capability_shared_resource);
			template.addServices(sd);
			
			
			//check the agents we already have stored
			if(myAgent.resourceAgents.size() > 0) {
				//DFAgentDescription[] result = 
				for(DFAgentDescription a : myAgent.resourceAgents) {
					
					  @SuppressWarnings("unchecked")
						Iterator<ServiceDescription> it = a.getAllServices();
					    while(it.hasNext()) {
					    	ServiceDescription service_description = it.next();
					    	if(service_description.getType().equals(service_type) && service_description.getName().equals(requested_capability_shared_resource)) {
					    		resourceAgents.add(a.getName());
					    	}
					    }
				}
			}
			//if there is no agent with ne needed capabiltiy stored, search the DF
			if(resourceAgents.size()==0) {	//TBD subscription!!

				
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					
				if (result.length != 0){
					
					//resourceAgents = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						//resourceAgents[i] = result[i].getName();
						myAgent.addResourceAgent(result[i]); //adds new ones
						resourceAgents.add(result[i].getName());
					}
				}else {
					System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"no Agent with operation"+requested_capability_shared_resource+" was found");
					//Handling of this situation --> tbd
				}
				
				
				} catch (FIPAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
						 	//TBD
					 }else {
						 cfp_timeslot = timeslot_for_proposal;	 
					 }
					 
					 cfp_onto.setHasTimeslot(cfp_timeslot);
					 //requested_operation.setName("Kran");
					 //requested_operation.setType("shared_resource");
					 
					 //meant to reduce traffic				 
					 if(!requested_capability_shared_resource.equals("Schiene")) {
						 ((Transport_Operation) requested_operation).setHasDetailedOperationDescription(null);
					 }
					cfp_onto.setHasOperation(requested_operation);
					cfp_onto.setHasSender(myAgent.getAID());
					
			//	cfp_sent = cfp_onto;									
				_SendCFP sendCFP = new _SendCFP();
				sendCFP.setHasCFP(cfp_onto);			
				Action content = new Action(myAgent.getAID(),sendCFP);
				
				//create ACL Message			
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				//cfp.setContent(message_content);	
				cfp.setLanguage(myAgent.getCodec().getName());
				cfp.setOntology(myAgent.getOntology().getName());
				cfp.setConversationId(conversationID);
				
				//determine reply by time
				reply_by_date_long = System.currentTimeMillis()+reply_by_time;
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
				myAgent.printOutSent(cfp);			
				myAgent.send(cfp);				
				

			step = 1;
			break;
		case 1:	
			//delay			
			if(System.currentTimeMillis()>reply_by_date_CFP || resourceAgents.size() == received_proposals.size()){ //go to step 2 after deadline expired
				step = 3;	
				break;
			}else if(System.currentTimeMillis()<= reply_by_date_CFP){
				step = 2;
			}
			
			//break;
		case 2:

			// Receive all proposals

						MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
				        MessageTemplate mt2 = MessageTemplate.MatchConversationId(conversationID);	
				        MessageTemplate mt_total = MessageTemplate.and(mt1,mt2);
				        
						ACLMessage reply = myAgent.receive(mt_total);
						if (reply != null) {
							//ontology
							//System.out.println("proposal REQUEST PERFORMER RESOURCE received at______"+System.currentTimeMillis());
							
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
						
						}
						else { 
							block(10);
							step = 1;
						}
			break;
		case 3:
			
			if(bestSeller != null){ //best one found
				//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"Best Proposal from "+bestSeller.getLocalName()+" with price: "+bestPrice+" received.");
				
				((Boolean[]) this.getDataStore().get(0))[index] = true;	//angefragt
				//System.out.println("DEBUG__________"+ ((Boolean[]) this.getDataStore().get(0))[index]+ " in datastore set to true");
				((Boolean[]) this.getDataStore().get(1))[index] = true; //positive Antwort
				
						//TBD Here can be checked, if the conditions of the proposal are ok 
		    			//--> timeslot good enough? Durch die Strafkosten für Proposals, die die Bedingungen des CFP nicht erfüllen, wird der bestseller immer besser oder gleich gut sein, wie andere Angebote
						//der bestseller wird also nur die Zeit nicht einhalten, wenn die Zeit von keinem eingehalten werden kann
						@SuppressWarnings("unchecked")
						Iterator<AllocatedWorkingStep> it = proposal_bestseller.getConsistsOfAllocatedWorkingSteps().iterator();
					    while(it.hasNext()) {
					    	AllocatedWorkingStep allocWorkingstep = it.next();			//proposed allocated working step (attribute: price, resource, timeslot, operation)
					    	timeslot_for_proposal = allocWorkingstep.getHasTimeslot();		
					    }
					    //can the  proposal fulfill the desired latest end to match the already scheduled next production step?
						//check this centrally in wait for shared resource behaviour
					    ((Proposal[]) this.getDataStore().get(4))[index] = proposal_bestseller;
					    
					    //if(Long.parseLong(timeslot_for_porposal.getEndDate())>Long.parseLong(cfp_sent.getHasTimeslot().getEndDate())) {
						//TBD for shared resources										
						//}

				step = 4;
				break;
			}else{					//no one found
				System.out.println(System.currentTimeMillis()+" "+myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"No proposals for operation "+requested_capability_shared_resource+" received.");
				step = 8;
				
				//myAgent.getReceiveCFPBehav().shared_resource_asked[index] = true;
				//myAgent.getReceiveCFPBehav().shared_resource_available[index] = false;
				//myAgent.getReceiveCFPBehav().restart();
				
				((Boolean[]) this.getDataStore().get(0))[index] = true;
				((Boolean[]) this.getDataStore().get(1))[index] = false;
				
				//better handling --> tbd
			}
			break;
			
		// wait in 4 until msg received from WP-Agent	
		case 4:
			if((Boolean)this.getDataStore().get(3) != null && (Boolean)this.getDataStore().get(3) == true) {	//order from WP received
				step = 5;
			}else if((Boolean)this.getDataStore().get(3) != null && (Boolean)this.getDataStore().get(3) == false){
				//System.out.println("DEBUG___________"+logLinePrefix+" at agent "+myAgent.getLocalName()+" no order received!");
				//reject all Proposals
				
				for(int i = 0;i<proposal_senders.size();i++) {
					AID sender_of_proposal = proposal_senders.get(i);
						Proposal proposal_to_reject = received_proposals.get(i);
						Reject_Proposal reject_Proposal_onto = new Reject_Proposal();
						reject_Proposal_onto.setID_Number(proposal_id);
						reject_Proposal_onto.setConsistsOfAllocatedWorkingSteps(proposal_to_reject.getConsistsOfAllocatedWorkingSteps());
						_SendReject_Proposal sendRejectProposal = new _SendReject_Proposal();
						sendRejectProposal.setHasReject_Proposal(reject_Proposal_onto);
						
						Action content2 = new Action(this.getAgent().getAID(),sendRejectProposal);
						
						//create ACLMessage
							ACLMessage reject_proposal_acl = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
							reject_proposal_acl.addReceiver(sender_of_proposal);
							reject_proposal_acl.setConversationId(conversationID);
							reject_proposal_acl.setInReplyTo(Integer.toString(proposal_to_reject.getID_Number()));
							reject_proposal_acl.setLanguage(myAgent.getCodec().getName());
							reject_proposal_acl.setOntology(myAgent.getOntology().getName());
							
							
							//ontology --> fill content
							try {
								myAgent.getContentManager().fillContent(reject_proposal_acl, content2);
							} catch (CodecException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (OntologyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							myAgent.send(reject_proposal_acl);
							//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" reject proposal to proposal "+received_proposals.get(i).getID_Number()+" sent to receiver "+sender_of_proposal.getLocalName());					
				}
				step = 8;
				break;
				
			}
			else {	
				this.block(5);
				break;		
			}
		
		case 5:
			//book best offer
			
			//create ontology content
			Accept_Proposal accept_Proposal_onto = new Accept_Proposal();
			accept_Proposal_onto.setHasProposal(proposal_bestseller);
			_SendAccept_Proposal sendAcceptProposal = new _SendAccept_Proposal();
			sendAcceptProposal.setHasAccept_Proposal(accept_Proposal_onto);					//if necessary there can be more than one allocatedWorkingStep inside a Proposal
			Action content_booking = new Action(this.getAgent().getAID(),sendAcceptProposal);
			
			//create ACLMessage
				ACLMessage accept_proposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				accept_proposal.addReceiver(bestSeller);
				accept_proposal.setConversationId(conversationID);
				accept_proposal.setLanguage(myAgent.getCodec().getName());
				accept_proposal.setOntology(myAgent.getOntology().getName());
				//determine reply by time
				//Date reply_by_date = new Date();
				//long reply_by_date_long = 0;
				reply_by_date_long = (long) (System.currentTimeMillis()+1.25*reply_by_time);
				reply_by_date = new Date(reply_by_date_long);
				this.reply_by_date_inform = reply_by_date_long;
				accept_proposal.setReplyByDate(reply_by_date);
				
				accept_proposal.setInReplyTo(Integer.toString(proposal_id));
				
				//ontology --> fill content
				try {
					myAgent.getContentManager().fillContent(accept_proposal, content_booking);
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				myAgent.send(accept_proposal);
				myAgent.printOutSent(accept_proposal);
				//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" order to offer "+proposal_id+" sent to receiver "+bestSeller.getLocalName());
				
			
			//reject the rest
				for(int i = 0;i<proposal_senders.size();i++) {
					AID sender_of_proposal = proposal_senders.get(i);
					if(!sender_of_proposal.getLocalName().equals(bestSeller.getLocalName())) {
						Proposal proposal_to_reject = received_proposals.get(i);
						Reject_Proposal reject_Proposal_onto = new Reject_Proposal();
						reject_Proposal_onto.setID_Number(proposal_id);
						reject_Proposal_onto.setConsistsOfAllocatedWorkingSteps(proposal_to_reject.getConsistsOfAllocatedWorkingSteps());
						_SendReject_Proposal sendRejectProposal = new _SendReject_Proposal();
						sendRejectProposal.setHasReject_Proposal(reject_Proposal_onto);
						
						Action content2 = new Action(this.getAgent().getAID(),sendRejectProposal);
						
						//create ACLMessage
							ACLMessage reject_proposal_acl = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
							reject_proposal_acl.addReceiver(sender_of_proposal);
							
							reject_proposal_acl.setLanguage(myAgent.getCodec().getName());
							reject_proposal_acl.setOntology(myAgent.getOntology().getName());
							
							
							//ontology --> fill content
							try {
								myAgent.getContentManager().fillContent(reject_proposal_acl, content2);
							} catch (CodecException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (OntologyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							myAgent.send(reject_proposal_acl);
							//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" reject proposal to proposal "+received_proposals.get(i).getID_Number()+" sent to receiver "+sender_of_proposal.getLocalName());
							
						
					}
				}
				
				
				
			step = 6;			
			
			break;
		case 6:
			//delay			
			if(System.currentTimeMillis()<=reply_by_date_inform){ 
				step = 7;	
				
			}else {
				step=8; //go to step 8 after deadline expired
				System.out.println(System.currentTimeMillis()+" DEBUG____ERROR___THIS END IS BECAUSE IT IS TOO LATE in request performer resource at "+myAgent.getLocalName());
			}
			break;
			
		case 7:      
			// Receive inform if allocated step is scheduled
			MessageTemplate mt3 = MessageTemplate.MatchInReplyTo(Integer.toString(proposal_id));
	        MessageTemplate mt4 = MessageTemplate.MatchConversationId(conversationID);	
	        MessageTemplate mt_total2 = MessageTemplate.and(mt3,mt4);
	        
			ACLMessage reply_inform = myAgent.receive(mt_total2);
			//System.out.println(System.currentTimeMillis()+"  DEBUG________"+myAgent.getLocalName()+"  REQ PERF RES WAIT FOR INFORM ALLOCED proposal_id"+proposal_id+" conversationID "+conversationID);
			if (reply_inform != null) {	// Reply received
				if(reply_inform.getPerformative() == ACLMessage.FAILURE) {
					//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" order to offer "+proposal_id+" sent to receiver "+bestSeller.getLocalName()+" cannot be carried out");
					//TBD
					//step = 0;
					/*
					myAgent.getReceiveCFPBehav().shared_resource_asked[index] = true;
					myAgent.getReceiveCFPBehav().shared_resource_available[index] = false;
					myAgent.getReceiveCFPBehav().restart();				
					*/
					//WWWWWWWWWWWWWWWWWWWWWWW
					//shared_resource_asked[index] = true;
					//shared_resource_available[index] = false;
					
					((Boolean[]) this.getDataStore().get(0))[index] = true;
					((Boolean[]) this.getDataStore().get(1))[index] = false;
					
					
					step = 8;
					break;
					
				}else if(reply_inform.getPerformative() == ACLMessage.INFORM) {
					//System.out.println(System.currentTimeMillis()+" "+myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" order to offer "+proposal_id+" sent to receiver "+bestSeller.getLocalName()+" successfully scheduled.");
				/*
					myAgent.getReceiveCFPBehav().shared_resource_asked[index] = true;
					myAgent.getReceiveCFPBehav().shared_resource_available[index] = true;
					myAgent.getReceiveCFPBehav().restart();
				*/
					//shared_resource_asked[index] = true;
					//shared_resource_available[index] = true;
					
					//jetzt wenn proposals da sind
					//((Boolean[]) this.getDataStore().get(0))[index] = true;
					//System.out.println("DEBUG__________"+ ((Boolean[]) this.getDataStore().get(0))[index]+ " in datastore set to true");
					//((Boolean[]) this.getDataStore().get(1))[index] = true;
					
					((Boolean[]) this.getDataStore().get(2))[index] = true;
					
				step = 8;
				break;
				}
			}
			else { 
				this.block(10);
				step = 6;
				break;
				//block();
				//ACLMessage test = myAgent.receive();
				//if(test != null) {
					//System.out.println("DEBUG____MSG RECEIVED BUT NOT WITH EXPECTED PARAMETERS (CONVERSATION ID AND in REply to________"+test.getConversationId()+" "+test.getInReplyTo()+" "+test.getContent());
				//}
			///block();	
			}
		}
	}

	public boolean done() {
		//System.out.println("DEBUG_______________"+myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" test 5");
		
		return step == 8;
	}
}  