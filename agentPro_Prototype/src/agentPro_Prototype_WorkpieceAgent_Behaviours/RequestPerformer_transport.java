package agentPro_Prototype_WorkpieceAgent_Behaviours;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

import agentPro.onto.Accept_Proposal;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.OrderedOperation;
import agentPro.onto.Production_Operation;
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
import agentPro_Prototype_Agents._Agent_Template;
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
import jade.util.leap.List;
import support_classes.Interval;
import support_classes.OperationCombination;

public class RequestPerformer_transport extends Behaviour {

	private static final long serialVersionUID = 1L;
	private int step = 0;
	
	private String logLinePrefix = ".RequestPerformer_transport";
	private WorkpieceAgent myAgent;
	
	private String conversationID = "";
	private String conversationID_production = "";
	
	//Send CFPs
	//private long deadline_for_this_task;
	//private String service_type;
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
		private ArrayList<Proposal> receivedProposals_production;
		private ArrayList<AID> proposal_senders = new ArrayList <AID>();
		private ArrayList<AID> bestCombination_senders = new ArrayList <AID>();
		private ArrayList<AID> receivers_of_rejection = new ArrayList <AID>();
		private ArrayList<Proposal> received_proposals = new ArrayList <Proposal>();
		private OperationCombination combination_best;
		
	//arrange last transport
		private Boolean last_operation;
		private Boolean error_handling_active;
		
		//error handling
		private Timeslot timeslot_for_buffer_place;
		private long earliest_end;
		private String necessary_resource_agent_for_this_step;
		ArrayList<OperationCombination> listOfCombinations = new ArrayList<OperationCombination>();
		private int numberOfAnswers;
	
	//used for production and transport
	public RequestPerformer_transport(WorkpieceAgent myAgent, Operation requested_operation, Long startdate_for_this_task, Boolean last_operation, Boolean error_handling_active) { //start_date of production / startdate is null in case of transport 
		super(myAgent);
		this.myAgent = myAgent;	
		this.requested_operation = requested_operation;
		//if(startdate_for_this_task != null) {
			this.startdate_for_this_task = startdate_for_this_task;
		//}	
		this.conversationID = Long.toString(System.currentTimeMillis());		
		this.logLinePrefix = this.logLinePrefix+"."+requested_operation.getName()+" ";
		this.last_operation = last_operation;
		this.error_handling_active = error_handling_active;
	}
	
	public RequestPerformer_transport(WorkpieceAgent myAgent, ArrayList<Proposal> receivedProposals_production, Long startdate_for_this_task, Boolean last_operation, Boolean error_handling_active, String conversationID_reqPerf) { //start_date of production / startdate is null in case of transport 
		super(myAgent);
		this.myAgent = myAgent;	
		this.receivedProposals_production = receivedProposals_production;
		//if(startdate_for_this_task != null) {
			this.startdate_for_this_task = startdate_for_this_task;
		//}	
		this.conversationID = Long.toString(System.currentTimeMillis());
		this.conversationID_production = conversationID_reqPerf;
		this.logLinePrefix = this.logLinePrefix+"."+_Agent_Template.printOutArrayList(receivedProposals_production)+" ";
		this.last_operation = last_operation;
		this.error_handling_active = error_handling_active;
	}
	
	//this constructor is used fot transport steps (dont need startdate) and for error handling purposes which needs a given timeslot
	public RequestPerformer_transport(WorkpieceAgent myAgent, Operation buffer, Timeslot timeslot_to_book_buffer_place, Boolean last_operation, Boolean error_handling_active) {
		super(myAgent);
		this.myAgent = myAgent;	
		this.requested_operation = buffer;
		
		this.conversationID = Long.toString(System.currentTimeMillis());
		this.logLinePrefix = this.logLinePrefix+"."+buffer.getName()+" ";
		this.last_operation = last_operation;
		this.error_handling_active = error_handling_active;
		this.timeslot_for_buffer_place = timeslot_to_book_buffer_place;

		
	}


	public RequestPerformer_transport(WorkpieceAgent myAgent, Operation requested_operation, long startdate_for_this_task,
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
		System.out.println("DEBUG______________________QQQQQQQQQQQQQQQQQQQQQQQQQQ WHEN USED         ????????????   constuctor 3");
	}

	public void action() {

		switch (step) {
		
		case 0:		
			
			//send CFPs
			_SendCFP sendCFP = new _SendCFP();
			for(Proposal proposal : receivedProposals_production) {
				OperationCombination opcomb = new OperationCombination(proposal, myAgent.getLastProductionStepAllocated());	//last step as the starting point
				
				//check if a buffer is needed for this operation
				if(checkIfBufferPlaceisNeeded(proposal, myAgent.getLastProductionStepAllocated())) {
					if(!arrangeBuffer(opcomb)) { //similar to normal RequestPerformer Behaviour (type: buffer), adds buffers to OperationCombination	
						System.out.println(logLinePrefix+"   ERROR  no buffer could be arranged ");//buffer is needed but could not be arranged		
						//TODO routine to skip this proposal		
						continue;
					}
				}			
				listOfCombinations.add(opcomb);
			}						
			
			for(OperationCombination operationComb : listOfCombinations) {			
				createAndAddCFPtransport(operationComb, sendCFP);						
			}
			System.out.println("DEBUG_______cfp size "+sendCFP.getHasCFP().size()+" no transport needed ");	
			if(sendCFP.getHasCFP().size()==0) {	//no transport is needed				
				step = 3;							
				break;
			}					
			sendCfps(sendCFP);
					
			step = 1;
			block((long) (0.5*myAgent.getProductionManagerBehaviour().reply_by_time));
			break;
		case 1:	
			//deadline	
			//if deadline expired or all proposals are received --> book best offer
			if(System.currentTimeMillis()>reply_by_date_CFP || numberOfAnswers == resourceAgents.size()) {
				step = 3;
				break;
			}else if(System.currentTimeMillis()<=reply_by_date_CFP){ 
				step = 2;				
				block(10);
				break;
			} 
			
			//break;
		case 2:

			// Receive all proposals and rejections
			numberOfAnswers += myAgent.receiveProposals(conversationID, proposal_senders, received_proposals);												
			numberOfAnswers += myAgent.receiveRejection(conversationID, proposal_senders, received_proposals);		
			
			step = 1;			
			break;
			
		case 3:
			//determine combinations
		
			for(OperationCombination operationCombination: listOfCombinations) {	//check all transport proposals for each production proposal --> put together the ones that belong together				
				for(Proposal prop_transport : received_proposals) {				
					if(prop_transport.getID_String().contentEquals(operationCombination.getIdenticiation_string())) {	//compares id of prod resource with id of transp operation
						operationCombination.addProposal(prop_transport);					
					}					
				}
			}
			if(listOfCombinations.size()>0) {
				//calculate values and sort by criterion
				combination_best = sortAndDetermineBestCombinationByCriterion();			
				System.out.println("Best combination = "+combination_best.toString());

				//book best offers
				acceptProposalsOfCombination(combination_best);	//do not send the proposal itself again			
				//reject the rest
				rejectProposals(listOfCombinations);
				//book into schedule	    
		    	checkConsistencyAndAddStepsToWorkplan(combination_best);
		    	//agenten informieren, wann Werkstück abgeholt wird
		    	if(combination_best.getBest_path().getFirstTransport()!= null) { //is null if there is no transport
		    		myAgent.addBehaviour(new InformWorkpieceArrivalAndDeparture(myAgent, combination_best.getBest_path().getFirstTransport(), combination_best.getBest_path().getFirstTransportAllWS()));		    		
		    	}
		    	
				myAgent.getProductionManagerBehaviour().setStep(0);
				myAgent.getProductionManagerBehaviour().restart();
				step = 4;					
				break;
			}else {
				System.out.println("ERROR Operation could not be arranged.");
				step=4;
				break;
			}
			
		
		
		}//switch        
	}

	private void checkConsistencyAndAddStepsToWorkplan(OperationCombination combination_best2) {
		@SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep>it = combination_best2.getBest_path().getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
		Boolean success = true;
		while(it.hasNext()) {
			if(success == true) {	//if its already false, keep it that way
				AllocatedWorkingStep allWS = it.next();
				if(!stepAlreadyPlanned(allWS)) {
					success = myAgent.checkConsistencyAndAddStepToWorkplan(allWS);
					if(!success) {
						System.out.println("ERROR_____________allWS "+allWS.toString()+" could not be added.");
					}
				}
				
			}
		}
		
	}

	private boolean stepAlreadyPlanned(AllocatedWorkingStep allWStoBeAdded) {
		@SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep>it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
		while(it.hasNext()) {
			AllocatedWorkingStep allWS1 = it.next();
			if(allWS1.getID_String().contentEquals(allWStoBeAdded.getID_String())) {
				//timeslot needs to be adjusted
				allWS1.getHasTimeslot().setEndDate(allWStoBeAdded.getHasTimeslot().getEndDate());
				return true;
			}
		}
		return false;
	}

	private void createAndAddCFPtransport(OperationCombination opComb, _SendCFP sendCFP) {
			
		if(opComb.getBuffer_needed()) {	//there are >0 buffer operations in the opComb
			ArrayList<Proposal> buffers = opComb.getBuffer_operations();
			for(Proposal prop_buffer : buffers) {	//for each buffer operation there needs to be a transport from the old prod. step to the buffer
				if(checkIfTransportIsNeeded(opComb.getInitial_proposal_production())) {	
					sendCFP.addHasCFP(createCFPSingleTransport(myAgent.getLastProductionStepAllocated(), (AllocatedWorkingStep)prop_buffer.getConsistsOfAllocatedWorkingSteps().get(0)));		//add buffer operations to CFPs --> Start = last alloc step, end = buffer
					//and from the buffer to the new step
					sendCFP.addHasCFP(createCFPSingleTransport((AllocatedWorkingStep)prop_buffer.getConsistsOfAllocatedWorkingSteps().get(0), (AllocatedWorkingStep)opComb.getInitial_proposal_production().getConsistsOfAllocatedWorkingSteps().get(0)));		//add buffer operations to CFPs --> Start = last alloc step, end = buffer			
				
				}
			}		
		}else {
			//there needs to be a transport from the old one to the new production step if a transport is needed		
			System.out.println("DEBUG____________transport needed "+checkIfTransportIsNeeded(opComb.getInitial_proposal_production()));
			if(checkIfTransportIsNeeded(opComb.getInitial_proposal_production())) {		
			sendCFP.addHasCFP(createCFPSingleTransport(myAgent.getLastProductionStepAllocated(), (AllocatedWorkingStep)opComb.getInitial_proposal_production().getConsistsOfAllocatedWorkingSteps().get(0)));		//add buffer operations to CFPs --> Start = last alloc step, end = buffer							
			}
		}	
	}

	private CFP createCFPSingleTransport(AllocatedWorkingStep start, AllocatedWorkingStep end) {										
			CFP cfp_onto = new CFP();
				cfp_onto.setHasTimeslot(determineCFPTimeslotTransport(start, end)); //determine the requested timeslot (earliest start date, latest finish date) of the operation	
				cfp_onto.setHasOperation(createTransportOperation(start, end));
				cfp_onto.setHasSender(myAgent.getAID());
				cfp_onto.setID_String(end.getID_String());
				//26.02.2019 add quantity
				cfp_onto.setQuantity(myAgent.getOrderPos().getQuantity());
		return cfp_onto;
	}

	private Timeslot determineCFPTimeslotTransport(AllocatedWorkingStep start, AllocatedWorkingStep end) {
		Timeslot timeslot = new Timeslot();
		timeslot.setStartDate(start.getHasTimeslot().getEndDate());
		timeslot.setEndDate(end.getHasTimeslot().getStartDate());
		return timeslot;
	}

	private Transport_Operation createTransportOperation(AllocatedWorkingStep start, AllocatedWorkingStep end) {
		Transport_Operation transport_operation = new Transport_Operation();									//
		transport_operation.setType("transport");
		
		transport_operation.setStartState(start.getHasResource().getHasLocation());
		transport_operation.setEndState(end.getHasResource().getHasLocation());
		
		//Name = Start_Ziel in format  X;Y_DestinationResource
		transport_operation.setName(start.getHasResource().getHasLocation().getCoordX()+";"+start.getHasResource().getHasLocation().getCoordY()+"_"+end.getHasResource().getName());
		transport_operation.setAppliedOn(myAgent.getRepresented_Workpiece());
		transport_operation.setBuffer_before_operation(((Operation) end.getHasOperation()).getBuffer_before_operation());
			
		return transport_operation;
	}

	private Boolean checkIfTransportIsNeeded(Proposal proposal) {
		return false;
		/*
		Location nextProduction = ((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasResource().getHasLocation();
		if(_Agent_Template.doLocationsMatch(myAgent.getLocation(), nextProduction)) {
			return false;
		}else {
			return true;
		}*/
	}

	//arranges a buffer operation / proposal for a production operation and adds it to the list of buffer proposals
	private boolean arrangeBuffer(OperationCombination opcomb) {
		opcomb.setBuffer_needed(true);
		Proposal prop_production = opcomb.getInitial_proposal_production();
		
		Production_Operation requested_operation_buffer = new Production_Operation();
		requested_operation_buffer.setType("buffer");
		requested_operation_buffer.setAppliedOn(myAgent.getRepresented_Workpiece());
		requested_operation_buffer.setStartState(opcomb.getLastProductionStepAllocated().getHasResource().getHasLocation());
		requested_operation_buffer.setEndState(((AllocatedWorkingStep)prop_production.getConsistsOfAllocatedWorkingSteps().get(0)).getHasResource().getHasLocation());	
		ArrayList<AID> buffer_agents = new ArrayList<AID>();
		ArrayList<Proposal>proposals_buffer = new ArrayList<Proposal>();
		int step1 = 0;
		Boolean finished = false;
		int number_of_answers = 0;
		long reply_by_date_long = 0;
		//String conversationID_buffer = prop_production.getID_String();
		while(!finished) {
		switch (step1) {
		
		case 0:		
			buffer_agents = myAgent.findOfferingAgents(requested_operation_buffer);
			
			//determine the requested timeslot (earliest start date, latest finish date) of the operation
			Timeslot cfp_timeslot =  determineCFPTimeslot(prop_production);	 
					 
					 
			//determine reply by time
			reply_by_date_long = System.currentTimeMillis()+myAgent.getProductionManagerBehaviour().reply_by_time;		
						Date reply_by_date = new Date(reply_by_date_long);
						//reply_by_date_CFP = reply_by_date_long;
			myAgent.sendCfps(cfp_timeslot, requested_operation_buffer, conversationID, buffer_agents, reply_by_date);
				
			step1 = 1;
			block((long) (0.5*myAgent.getProductionManagerBehaviour().reply_by_time));
			break;
		case 1:	
			//deadline	
			//if deadline expired or all proposals are received --> get transport proposals
			if(System.currentTimeMillis()>reply_by_date_long || number_of_answers == buffer_agents.size()) {
				finished = true;
				for(Proposal prop_buffer : proposals_buffer) {
					//receivedProposals_buffer.add(prop_buffer);
					opcomb.getBuffer_operations().add(prop_buffer);
				}
				break;
			}else if(System.currentTimeMillis()<=reply_by_date_long){ 
				step1 = 2;				
				//block(10);
				break;
			} 
			
			//break;
		case 2:

			// Receive all proposals
			number_of_answers += myAgent.receiveProposals(conversationID, buffer_agents, proposals_buffer);												
			number_of_answers += myAgent.receiveRejection(conversationID, buffer_agents, proposals_buffer);		
			
			step1 = 1;			
			break;		
		}//switch   
		}//while
		if(proposals_buffer.size()>0) {
			return true;
		}else {
			return false;
		}
	}

	//send reject to all that do not get an accept proposal message
	private void rejectProposals(ArrayList<OperationCombination> listOfCombinations) {
		ACLMessage reject_proposal_acl = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
		reject_proposal_acl.setConversationId(conversationID);
		reject_proposal_acl.setLanguage(myAgent.getCodec().getName());
		reject_proposal_acl.setOntology(myAgent.getOntology().getName());
		
		//Reject_Proposal reject_Proposal_onto = new Reject_Proposal();
		
		for(OperationCombination combination : listOfCombinations) {
			for(Proposal proposal_to_reject : combination.getProposals()) {
				AID sender_of_proposal = proposal_to_reject.getHasSender();	
					if(!bestCombination_senders.contains(sender_of_proposal) && !receivers_of_rejection.contains(sender_of_proposal)) {
						//reject_Proposal_onto.setID_Number(proposal_to_reject.getID_Number());				
						//reject_Proposal_onto.setConsistsOfAllocatedWorkingSteps(proposal_to_reject.getConsistsOfAllocatedWorkingSteps());
						_SendReject_Proposal sendRejectProposal = new _SendReject_Proposal();
						//sendRejectProposal.setHasReject_Proposal(reject_Proposal_onto);
						
						Action content3 = new Action(this.getAgent().getAID(),sendRejectProposal);	
						reject_proposal_acl.clearAllReceiver();
						reject_proposal_acl.addReceiver(sender_of_proposal);
						//reject_proposal_acl.setInReplyTo(Integer.toString(proposal_to_reject.getID_Number()));
						if(((AllocatedWorkingStep)proposal_to_reject.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getType().contentEquals("production")) {
							reject_proposal_acl.setConversationId(conversationID_production);
						}else {
							reject_proposal_acl.setConversationId(conversationID);
						}
						
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
							System.out.println(_Agent_Template.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" reject proposal to proposal "+proposal_to_reject.getID_Number()+" sent to receiver "+sender_of_proposal.getLocalName());
							receivers_of_rejection.add(sender_of_proposal);
					}
				}

		}
		
	}

	private void sendCfps(_SendCFP sendCFP) {
		
			Action content = new Action(myAgent.getAID(),sendCFP);
			//Action content = new Action(myAgent.getAID(),cfp_onto);
			
			//create ACL Message
			
			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			//cfp.setContent(message_content);	
			cfp.setLanguage(myAgent.getCodec().getName());
			cfp.setOntology(myAgent.getOntology().getName());
			cfp.setConversationId(conversationID);
			
			//determine reply by time
			long reply_by_date_long = System.currentTimeMillis()+myAgent.getProductionManagerBehaviour().reply_by_time;
			Date reply_by_date = new Date(reply_by_date_long);
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
			
			@SuppressWarnings("unchecked")
			Iterator<CFP>it = sendCFP.getAllHasCFP();
			while(it.hasNext()) {
				CFP cfp_current = it.next();
				resourceAgents = myAgent.findOfferingAgents(cfp_current.getHasOperation());
			}

			for (int i = 0;i<resourceAgents.size();i++){
				cfp.addReceiver(resourceAgents.get(i));
				//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"cfp sent to receiver: "+result[i].getName().getLocalName()+" with content "+cfp.getContent());
			}
			myAgent.send(cfp);	
	}

	private void acceptProposalsOfCombination(OperationCombination combination_best) {
		//create ontology content
		Accept_Proposal accept_Proposal_onto = new Accept_Proposal();		
		accept_Proposal_onto.setID_String(combination_best.getIdenticiation_string());
		_SendAccept_Proposal sendAcceptProposal = new _SendAccept_Proposal();
						//if necessary there can be more than one allocatedWorkingStep inside a Proposal
		Action content2 = new Action(this.getAgent().getAID(),sendAcceptProposal);
		ACLMessage accept_proposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL); 
		
		//accept_proposal.setConversationId(conversationID);
		accept_proposal.setLanguage(myAgent.getCodec().getName());
		accept_proposal.setOntology(myAgent.getOntology().getName());
		long reply_by_date_long2 = System.currentTimeMillis()+myAgent.getProductionManagerBehaviour().reply_by_time;
		Date reply_by_date2 = new Date(reply_by_date_long2);
		this.reply_by_date_inform = reply_by_date_long2;
		accept_proposal.setReplyByDate(reply_by_date2);
		
		//there can be multiple proposals from one agent (for transport to buffer & transport to production)
		for(int i = 0;i<combination_best.getBest_path().getList_of_best_proposals().size();i++) {
			if(!bestCombination_senders.contains(combination_best.getBest_path().getList_of_best_proposals().get(i).getHasSender())) {	//we havent sent this agent a message yet
				String name_of_sender = combination_best.getBest_path().getList_of_best_proposals().get(i).getHasSender().getLocalName();
				
				accept_proposal.clearAllReceiver();
				accept_Proposal_onto.clearAllHasProposal(); //put a clean list there
				accept_proposal.addReceiver(combination_best.getBest_path().getList_of_best_proposals().get(i).getHasSender());
				_Agent_Template.addUnique(bestCombination_senders, combination_best.getBest_path().getList_of_best_proposals().get(i).getHasSender());
				//bestCombination_senders.add(combination_best.getBest_path().getList_of_best_proposals().get(i).getHasSender());
				//accept_proposal.setInReplyTo(Integer.toString(proposal.getID_Number()));
				//remove all before
				//accept_Proposal_onto.setID_Number(combination_best.getBest_path().getList_of_best_proposals().get(i).getID_Number());
				//accept_Proposal_onto.setID_String(combination_best.getBest_path().getList_of_best_proposals().get(i).getID_String());
				accept_Proposal_onto.addHasProposal(combination_best.getBest_path().getList_of_best_proposals().get(i));
				
				//if there is another proposal from that agent --> add that as well
				for(int j = i+1;j<combination_best.getBest_path().getList_of_best_proposals().size();j++) {
					if(combination_best.getBest_path().getList_of_best_proposals().get(j).getHasSender().getLocalName().contentEquals(name_of_sender)) {
						accept_Proposal_onto.addHasProposal(combination_best.getBest_path().getList_of_best_proposals().get(j));
					}
				}
				if(((AllocatedWorkingStep)((Proposal)accept_Proposal_onto.getHasProposal().get(0)).getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getType().contentEquals("production")) {
					accept_proposal.setConversationId(conversationID_production);
					}else {
					accept_proposal.setConversationId(conversationID);
				}
				sendAcceptProposal.setHasAccept_Proposal(accept_Proposal_onto);	
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
				myAgent.printOutSent(accept_proposal);
			}
			
		}				
				
				
			
	}

	private OperationCombination sortAndDetermineBestCombinationByCriterion() {

		for(OperationCombination combination : listOfCombinations) {				
			combination.calculateValues();
		}
		Comparator<OperationCombination> comparator = null;
		switch (_Agent_Template.opimizationCriterion) {
		case "time_of_finish":
			comparator = Comparator.comparing(OperationCombination::getTimeOfFinish)
			.thenComparing(OperationCombination::getCosts)
			.thenComparing(OperationCombination::getTotal_duration);		
			break;
		case "duration_setup":
			comparator = Comparator.comparing(OperationCombination::getCosts)
			.thenComparing(OperationCombination::getTimeOfFinish)
			.thenComparing(OperationCombination::getTotal_duration);	
			break;
		}
		Collections.sort(listOfCombinations, comparator);	
		OperationCombination combination_best_from_list = listOfCombinations.get(0);
		return combination_best_from_list;
	}

	private Boolean checkIfBufferPlaceisNeeded(Proposal proposal, AllocatedWorkingStep allocatedWorkingStep) {
		if(allocatedWorkingStep != null) {
			long startdate = Long.parseLong(((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getStartDate());
			if((startdate-myAgent.getTransport_estimation())-(Long.parseLong(allocatedWorkingStep.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation())>_Agent_Template.bufferThreshold){
				System.out.println("(startdate-myAgent.getTransport_estimation()) "+(startdate-myAgent.getTransport_estimation())+" - "+(Long.parseLong(allocatedWorkingStep.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation())+"  (Long.parseLong(allocatedWorkingStep.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation())");
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
		
	}
	
	//for buffer
	private Timeslot determineCFPTimeslot(Proposal prop_production) {
		Timeslot timeslot = new Timeslot();
		AllocatedWorkingStep LAST_alWS_Production = myAgent.getLastProductionStepAllocated();
		//take step allocated in the planning round before as start and start of new step as end
			timeslot.setStartDate(Long.toString(Long.parseLong(LAST_alWS_Production.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation()));
			timeslot.setEndDate(Long.toString(Long.parseLong(((AllocatedWorkingStep)prop_production.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getStartDate())-myAgent.getTransport_estimation()));
	
		return timeslot;
	}
	

	public boolean done() {
	
		return step == 4;
	}
}  