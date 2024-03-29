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
import agentPro.onto.Capability;
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
import support_classes.Run_Configuration;

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
			if(receivedProposals_production == null) {
				System.out.println("DEBUG___ERROR null element");
			}
			for(Proposal proposal : receivedProposals_production) {
				OperationCombination opcomb = new OperationCombination(proposal, myAgent.getLastProductionStepAllocated());	//last step as the starting point		
				listOfCombinations.add(opcomb);
			}	
			
			//check if a buffer is needed for this operation
			if(!arrangeAdditionalOperation(listOfCombinations, "buffer")) { //similar to normal RequestPerformer Behaviour (type: buffer), adds buffers to OperationCombination	
				System.out.println(logLinePrefix+"   ERROR  no buffer could be arranged ");//buffer is needed but could not be arranged	
			}
			for(OperationCombination operationComb : listOfCombinations) {			
				createAndAddCFPtransport(operationComb, sendCFP);						
			}	
			if(sendCFP.getHasCFP().size()==0) {	//no transport is needed				
				step = 3;							
				break;
			}					
			sendCfps(sendCFP);
					
			step = 1;
			block((long) (0.1*Run_Configuration.reply_by_time_wp_agent));
			break;
		case 1:	
			//deadline	
			//if deadline expired or all proposals are received --> book best offer
			if(System.currentTimeMillis()>reply_by_date_CFP) {
				System.out.println(System.currentTimeMillis()+" DEBUG_________________ "+myAgent.getLocalName()+" requestperformer transport  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@  WWWWWWWWWWWWWWWW   TIME EXPIRED");
				step = 3;
				break;
			}else if(numberOfAnswers == resourceAgents.size()) {
				System.out.println(System.currentTimeMillis()+" DEBUG_________________   @@@@@@@@@@@@@@@@@@@@@@@@@@@@@  WWWWWWWWWWWWWWWW   ALL ANSWERS THERE");
				
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
			
			// organize shared resoures
			
 			if(!arrangeAdditionalOperation(listOfCombinations, "shared_resource")) { //similar to normal RequestPerformer Behaviour (type: buffer), adds buffers to OperationCombination	
				System.out.println(logLinePrefix+"   ERROR  no shared res could be arranged ");//buffer is needed but could not be arranged	
			}
			/*
			for(OperationCombination operationCombination: listOfCombinations) {
				if(checkIfSharedResourceIsNecessary(operationCombination.getInitial_proposal_production())){
					arrangeSharedResource(operationCombination.getInitial_proposal_production());
				}			
			}
			for(Proposal prop_transport : received_proposals) {	
				if(checkIfSharedResourceIsNecessary(prop_transport)){
					arrangeSharedResource(prop_transport);
				}	
			}
			break;
			*/
			step = 4;
			
		case 4:
			
			
			

			if(listOfCombinations.size()>0) {
				//calculate values and sort by criterion
				combination_best = sortAndDetermineBestCombinationByCriterion();	
				if(combination_best != null) {
					System.out.println("Best combination = "+combination_best.toString());

					//book best offers
					if(last_operation) {
						for(Proposal prop : combination_best.getProposals()) {
							((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).setLastOperation(last_operation);
						}						
						//System.out.println(myAgent.logLinePrefix+" DEBUG LAST OPERATION is set");
					}
					acceptProposalsOfCombination(combination_best);	//do not send the proposal itself again			
					//reject the rest
					rejectProposals(listOfCombinations);
					//book into schedule	    
			    	checkConsistencyAndAddStepsToWorkplan(combination_best);
			    	//agenten informieren, wann Werkst�ck abgeholt wird
			    	if(combination_best.getBest_path().getFirstTransport()!= null) { //is null if there is no transport
			    		myAgent.addBehaviour(new InformWorkpieceArrivalAndDeparture(myAgent, combination_best.getLastProductionStepAllocated(), combination_best.getBest_path().getFirstTransport(), combination_best.getBest_path().getFirstTransportAllWS()));		    		
			    	}else if(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>1){
			    		myAgent.addBehaviour(new InformWorkpieceArrivalAndDeparture(myAgent, combination_best.getLastProductionStepAllocated()));
			    	}
			    	//if(combination_best.getBest_path().getTransport_to_buffer() != null) {
			    	//	myAgent.addBehaviour(new InformWorkpieceArrivalAndDeparture(myAgent, combination_best.getLastProductionStepAllocated(), combination_best.getBest_path().getTransportToProductionOperation(), combination_best.getBest_path().getTransport_to_production()));
			    	//}
					myAgent.getProductionManagerBehaviour().setStep(0);
					myAgent.getProductionManagerBehaviour().restart();
					step = 5;					
					break;
				}else {
					System.out.println("ERROR____________________________ no feasible OC could be arranged");
					step = 5;
					break;
				}

			}else {
				System.out.println(System.currentTimeMillis()+" "+myAgent.logLinePrefix+" ERROR Operation could not be arranged. No combination");
				step=5;
				break;
			}
			
		
		
		}//switch        
	}

	private boolean checkIfSharedResourceIsNecessary(Proposal prop) {
		//if(((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getRequiresOperation()!= null) {
		if(((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getIsEnabledBy().size() > 0) {	
			return true;
		}else {
		return false;
		}}

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
				if(Long.parseLong(allWStoBeAdded.getHasTimeslot().getEndDate())-Long.parseLong(allWS1.getHasTimeslot().getEndDate())>myAgent.getTransport_estimation()+_Agent_Template.bufferThreshold*60*1000){
					//arrangeBuffer();
					System.out.println(logLinePrefix+"______________________ERROR_____________A BUFFER SHOULD BE ARRANGED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Waiting time now: 	"+(Long.parseLong(allWStoBeAdded.getHasTimeslot().getEndDate())-Long.parseLong(allWS1.getHasTimeslot().getEndDate())));
					//this is necessary because the transport can be done only too late --> maybe a trnapsort to a buffer can be arranged (however
					// that is unlikely..
				}
				allWS1.getHasTimeslot().setEndDate(allWStoBeAdded.getHasTimeslot().getEndDate()); //--> NEIN ODER? Aber wird trotzdem gesetzt
				return true;
			}
		}
		return false;
	}

	private void createAndAddCFPtransport(OperationCombination opComb, _SendCFP sendCFP) {
			
		if(opComb.getBuffer_needed()) {	//there are >0 buffer operations in the opComb
			ArrayList<Proposal> buffers = opComb.getBuffer_operations();
			for(Proposal prop_buffer : buffers) {	//for each buffer operation there needs to be a transport from the old prod. step to the buffer
				System.out.println("DEBUG____________transport needed "+checkIfTransportIsNeeded(opComb, prop_buffer));
				if(checkIfTransportIsNeeded(opComb, prop_buffer)) {	
					sendCFP.addHasCFP(createCFP_ontologyElement(myAgent.getLastProductionStepAllocated(), (AllocatedWorkingStep)prop_buffer.getConsistsOfAllocatedWorkingSteps().get(0), opComb, null, null ));		//add buffer operations to CFPs --> Start = last alloc step, end = buffer
					//and from the buffer to the new step
					sendCFP.addHasCFP(createCFP_ontologyElement((AllocatedWorkingStep)prop_buffer.getConsistsOfAllocatedWorkingSteps().get(0), (AllocatedWorkingStep)opComb.getInitial_proposal_production().getConsistsOfAllocatedWorkingSteps().get(0), opComb, null, null));		//add buffer operations to CFPs --> Start = last alloc step, end = buffer			
				
				}
			}		
		}else {
			//there needs to be a transport from the old one to the new production step if a transport is needed		
			System.out.println("DEBUG____________transport needed "+checkIfTransportIsNeeded(opComb, opComb.getInitial_proposal_production()));
			if(checkIfTransportIsNeeded(opComb, opComb.getInitial_proposal_production())) {		
			sendCFP.addHasCFP(createCFP_ontologyElement(myAgent.getLastProductionStepAllocated(), (AllocatedWorkingStep)opComb.getInitial_proposal_production().getConsistsOfAllocatedWorkingSteps().get(0), opComb, null, null));		//add buffer operations to CFPs --> Start = last alloc step, end = buffer							
			}
		}		
	}

	private CFP createCFP_ontologyElement(AllocatedWorkingStep start, AllocatedWorkingStep end, OperationCombination opcomb, Operation op, Timeslot timeslot) {										
			CFP cfp_onto = new CFP();
			String id_of_production_step = opcomb.getIdenticiation_string();
			if(timeslot != null) {
				cfp_onto.setHasTimeslot(timeslot); //determine the requested timeslot (earliest start date, latest finish date) of the operation					
			}else {
				cfp_onto.setHasTimeslot(determineCFPTimeslotTransport(start, end, opcomb)); //determine the requested timeslot (earliest start date, latest finish date) of the operation										
			}
			if(op != null) {
				cfp_onto.setHasOperation(op);	
			}else {
				cfp_onto.setHasOperation(createTransportOperation(start, end));	
			}		
				cfp_onto.setHasSender(myAgent.getAID());
				cfp_onto.setID_String(id_of_production_step); //vorher end.id --> f�hrt zu ..._puffer
				//26.02.2019 add quantity
				cfp_onto.setQuantity(myAgent.getOrderPos().getQuantity());
				//24.01.2022 Buffer after operation start muss eig das Minimum aus den beiden Buffer after operation End sein
				//Beispiel: Wenn ich zwar 10h sp�ter beim Zuscnitt weg kann, aber nur 2 h sp�ter beim Durchsatz ankommen darf
				// kann ich ja effektiv nicht 6h sp�ter abholen beim Zuschnitt
				Float min_start_end = Math.min(start.getHasOperation().getBuffer_after_operation_end(), end.getHasOperation().getBuffer_after_operation_start());
				Float buffer_after_initial_proposal_production;
				Float buffer_after_end_operation;
				Float new_Buffer_after_operation_start;
				Float buffer_after_operation_end;
				//transport zum Puffer													
				if(opcomb.getInitial_proposal_production() != null && end.getHasResource().getDetailed_Type().contentEquals("buffer")) {
					//start timeslot = end n-1     end = start of next production step --> here = start of buffer proposal
							//Start 18  earliest start = from cutting 18 Uhr
							//End 18    earliest end = from buffer 18 Uhr
					// buffer after operation basiert auf = min (LSn-1, LSB-Tmin, LSn-2Tmin-T buffer min)		
					
					buffer_after_initial_proposal_production = ((AllocatedWorkingStep)opcomb.getInitial_proposal_production().getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getBuffer_after_operation_end();
					buffer_after_end_operation = end.getHasOperation().getBuffer_after_operation_start();
					Float buffer_after_start_operation = start.getHasOperation().getBuffer_after_operation_end();
					Long term1 = Long.parseLong(start.getHasTimeslot().getStartDate())+buffer_after_start_operation.longValue();
					Long term2 = Long.parseLong(end.getHasTimeslot().getStartDate()) + buffer_after_end_operation.longValue()-Run_Configuration.transport_estimation;
					Long term3 = Long.parseLong(((AllocatedWorkingStep)opcomb.getInitial_proposal_production().getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getStartDate())+buffer_after_initial_proposal_production.longValue()-2*Run_Configuration.transport_estimation-Run_Configuration.bufferThreshold*60*1000;
					Long min_pre = Math.min(term1, term2);
					Long latest_start = Math.min(min_pre, term3);
					Long buffer_after_operation_start = latest_start-Long.parseLong(cfp_onto.getHasTimeslot().getStartDate());
					new_Buffer_after_operation_start = buffer_after_operation_start.floatValue();
					
					Long latest_finish = latest_start + Run_Configuration.transport_estimation;
					Long buffer_after_operation_end_l = latest_finish - Long.parseLong(cfp_onto.getHasTimeslot().getEndDate());
					buffer_after_operation_end = buffer_after_operation_end_l.floatValue();
					
					//transport ab puffer zur ressource
					//start interval = ES = end n-1 + transport estimation + minimal buffer threshold    end = start of next production step
				}else if(start.getHasResource().getDetailed_Type().contentEquals("buffer")) {
					//TBD
					Float buffer_after_start_operation = start.getHasOperation().getBuffer_after_operation_end();
					Long latest_finish_buffer = Long.parseLong(start.getHasTimeslot().getStartDate())+buffer_after_start_operation.longValue();
					buffer_after_end_operation = end.getHasOperation().getBuffer_after_operation_start();
					Long term2 = Long.parseLong(end.getHasTimeslot().getStartDate()) + buffer_after_end_operation.longValue()-Run_Configuration.transport_estimation;
					Long latest_start = Math.min(latest_finish_buffer, term2);
					Long buffer_after_operation_start = latest_start-Long.parseLong(cfp_onto.getHasTimeslot().getStartDate());
					new_Buffer_after_operation_start = buffer_after_operation_start.floatValue();
					
					long latest_finish = Long.parseLong(end.getHasTimeslot().getStartDate()) + buffer_after_end_operation.longValue();
					Long buffer_after_operation_end_l = latest_finish - Long.parseLong(cfp_onto.getHasTimeslot().getEndDate());
					buffer_after_operation_end = buffer_after_operation_end_l.floatValue();
					
					//Float value1 = end.getHasOperation().getBuffer_after_operation_start();
					//Long value = Long.parseLong(end.getHasTimeslot().getStartDate())+ value1.longValue()-Run_Configuration.transport_estimation-Long.parseLong(cfp_onto.getHasTimeslot().getStartDate());
					//new_Buffer_after_operation_start = value.floatValue();
				}
				//interval  ESB = 18.00  EF B = S n - Tmin
				else if(op != null && op.getType().equalsIgnoreCase("buffer")) {
					Float buffer_after_end_operation_1 = end.getHasOperation().getBuffer_after_operation_end();
					Long latest_finish = Long.parseLong(cfp_onto.getHasTimeslot().getEndDate())+buffer_after_end_operation_1.longValue();
					Long buffer_after_operation_end_2 = latest_finish - Long.parseLong(cfp_onto.getHasTimeslot().getEndDate());
					buffer_after_operation_end = buffer_after_operation_end_2.floatValue();
					Long latest_start = latest_finish - Run_Configuration.bufferThreshold*1000*60;
					Long buffer_after_operation_start = latest_start-Long.parseLong(cfp_onto.getHasTimeslot().getStartDate());
					new_Buffer_after_operation_start = buffer_after_operation_start.floatValue();
					
					
				}
				else {
					buffer_after_initial_proposal_production = min_start_end;
					buffer_after_end_operation = end.getHasOperation().getBuffer_after_operation_start();
					buffer_after_operation_end = buffer_after_end_operation;
					
					//13.02.
					new_Buffer_after_operation_start = Math.min(min_start_end, buffer_after_initial_proposal_production);
				}
				cfp_onto.getHasOperation().setBuffer_after_operation_end(buffer_after_operation_end); //24.01.2022
				cfp_onto.getHasOperation().setBuffer_after_operation_start(new_Buffer_after_operation_start);
				cfp_onto.getHasOperation().setBuffer_before_operation_start(0); // nicht fr�her starten, weil Vor-Operation nicht fr�her starten und beendet werden kann (Annahme). Au�er z.B. bei Teillieferungen??
				
				Float new_Buffer_before_operation_start = Math.min(start.getHasOperation().getBuffer_before_operation_end(), end.getHasOperation().getBuffer_before_operation_start());
				cfp_onto.getHasOperation().setBuffer_before_operation_end(new_Buffer_before_operation_start); // //buffer before steht f�r fr�her beenden 
				
			/*
				if(start.getHasResource().getDetailed_Type().contentEquals("buffer")) {
					Float value1 = end.getHasOperation().getBuffer_after_operation_start();
					Long value = Long.parseLong(end.getHasTimeslot().getStartDate())+ value1.longValue()-Run_Configuration.transport_estimation-Long.parseLong(cfp_onto.getHasTimeslot().getStartDate());
					new_Buffer_after_operation_start = value.floatValue();
				}else {
					new_Buffer_after_operation_start = Math.min(min_start_end, buffer_after_initial_proposal_production);
				}
				*/
				//25.06.2020
				//cfp_onto.getHasOperation().setBuffer_after_operation_start(start.getHasOperation().getBuffer_after_operation_end()); //is currently not checked by the crane
				
				//cfp_onto.getHasOperation().setBuffer_after_operation_end(end.getHasOperation().getBuffer_after_operation_end());
				
				//Beispiel: Zuschnitt: Dort kann ich 1 h fr�her abgeholt werden. Beim Durchsatz kann ich 2 h fr�her anfangen. Also kann ich effektiv 1h fr�her abgeholt werden
				
				//cfp_onto.getHasOperation().setAvg_Duration(myAgent.getTransport_estimation());
				//cfp_onto.getHasOperation().setBuffer_before_operation_end(end.getHasOperation().getBuffer_before_operation_start()); // //buffer before steht f�r fr�her beenden 
				//cfp_onto.getHasOperation().setBuffer_before_operation_start(0); // nicht fr�her starten, weil Vor-Operation nicht fr�her starten und beendet werden kann (Annahme). Au�er z.B. bei Teillieferungen??
				//27.01.2022 Does this need to be the minimum of the buffer of production and transport?
				//or 0???
				//Float new_buffer_before_operation_start = Math.min(a, b);
				//cfp_onto.getHasOperation().setBuffer_before_operation_start(start.getHasOperation().getBuffer_before_operation_end()); // nicht fr�her starten, weil Vor-Operation nicht fr�her starten und beendet werden kann (Annahme). Au�er z.B. bei Teillieferungen??
				
				
				
				return cfp_onto;
	}

	private Timeslot determineCFPTimeslotTransport(AllocatedWorkingStep start, AllocatedWorkingStep end, OperationCombination opcomb) {
		Timeslot timeslot = new Timeslot();
		if(start.getHasResource().getDetailed_Type().contentEquals("buffer")) {
			long startdate= Long.parseLong(opcomb.getLastProductionStepAllocated().getHasTimeslot().getEndDate())+Run_Configuration.transport_estimation+Run_Configuration.bufferThreshold*1000*60;
			timeslot.setStartDate(String.valueOf(startdate)); //the earlist possible start is the earliest arrival at the buffer + minimal buffer time
		}else {
			timeslot.setStartDate(start.getHasTimeslot().getEndDate());  //the machine says when it is finished with the process
		}
		//if(end.getHasResource().getDetailed_Type().contentEquals("buffer")) {
		//	AllocatedWorkingStep a = (AllocatedWorkingStep)opcomb.getInitial_proposal_production().getConsistsOfAllocatedWorkingSteps().get(0);
			
		//	long enddate = Long.parseLong(a.getHasTimeslot().getStartDate())-Run_Configuration.transport_estimation-Run_Configuration.bufferThreshold*1000*60;
		//	timeslot.setEndDate(String.valueOf(enddate));
		//}else {
			timeslot.setEndDate(end.getHasTimeslot().getStartDate());
		//}
		
		return timeslot;
	}

	private Transport_Operation createTransportOperation(AllocatedWorkingStep start, AllocatedWorkingStep end) {
		Transport_Operation transport_operation = new Transport_Operation();									//
		transport_operation.setType("transport");
		
		transport_operation.setStartStateNeeded(start.getHasResource().getHasLocation());
		transport_operation.setEndState(end.getHasResource().getHasLocation());
		
		//Name = Start_Ziel in format  X;Y_DestinationResource
		transport_operation.setName(start.getHasResource().getHasLocation().getCoordX()+";"+start.getHasResource().getHasLocation().getCoordY()+"_"+end.getHasResource().getName());
		transport_operation.setAppliedOn(myAgent.getRepresented_Workpiece());
			
		return transport_operation;
	}

	private Boolean checkIfTransportIsNeeded(OperationCombination opComb, Proposal proposal) {
		Location nextProductionOrBuffer = ((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasResource().getHasLocation();
		//if(myAgent.useCurrentLocationDueToDisturbance || myAgent.getLastProductionStepAllocated() == null) {
		if(myAgent.useCurrentLocationDueToDisturbance) {
			if(_Agent_Template.doLocationsMatch(myAgent.getLocation(), nextProductionOrBuffer)) {
				opComb.setTransport_needed(false);
				return false;
			}
		}else if(myAgent.getLastProductionStepAllocated() == null) {
			opComb.setTransport_needed(false);
			return false;
		}else if(myAgent.getLastProductionStepAllocated() != null) {
			if(_Agent_Template.doLocationsMatch(opComb.getLastProductionStepAllocated().getHasResource().getHasLocation(), nextProductionOrBuffer)) {
				opComb.setTransport_needed(false);
				return false;
			}
		}
		opComb.setTransport_needed(Run_Configuration.transport_needed);	//true oder false f�r Anwendungsfall
		return WorkpieceAgent.transport_needed;						//true
	}

	private boolean arrangeAdditionalOperation (ArrayList<OperationCombination> list, String operation_details) {
		_SendCFP sendCFP = new _SendCFP();
		for(OperationCombination comb : list) {
			if(operation_details.equals("buffer")) {
				Proposal prop = comb.getInitial_proposal_production();
				if(checkIfBufferPlaceisNeeded(prop, myAgent.getLastProductionStepAllocated())) {
					comb.setBuffer_needed(true);		
					sendCFP.addHasCFP(createCFP(myAgent.getLastProductionStepAllocated(), prop, "buffer", null));
				}
			}else if(operation_details.equals("shared_resource")) {
				for(Proposal prop : comb.getProposals()) {		//check for each proposal (production,transport and buffer) if a shared res is necessary
					if(checkIfSharedResourceIsNecessary(prop)) {
						  @SuppressWarnings("unchecked")
							//Iterator<Operation> it = ((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getRequiresOperation().iterator();
						  Iterator<Capability> it = ((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getIsEnabledBy().iterator();
						  while(it.hasNext()) {
						    	Capability req_cap = it.next();
						    	sendCFP.addHasCFP(createCFP(myAgent.getLastProductionStepAllocated(), prop, "shared_resource", req_cap));
						    }
						
					}
				}
				
			}
		}
		
		if(sendCFP.getHasCFP().size() > 0) {
			ArrayList<AID> agents = new ArrayList<AID>();
			ArrayList<Proposal>proposals = new ArrayList<Proposal>();
			int step1 = 0;
			Boolean finished = false;
			int number_of_answers = 0;
			//long reply_by_date_long = 0;
			//String conversationID_buffer = prop_production.getID_String();
			while(!finished) {
			switch (step1) {
			
			case 0:		
				sendCfps(sendCFP);
				
					
				step1 = 1;
				block((long) (Run_Configuration.factor_request_performer*Run_Configuration.reply_by_time_wp_agent));
				break;
			case 1:	
				//deadline	
				//if deadline expired or all proposals are received --> get transport proposals
				if(System.currentTimeMillis()>reply_by_date_CFP) {
					System.out.println(System.currentTimeMillis()+" DEBUG_______________________________ARRANGE Additional Operation__________________________ALL TIME EXPIRED");
					finished = true;
					for(Proposal prop : proposals) {
						//receivedProposals_buffer.add(prop_buffer);
						for(OperationCombination comb : list) {
							if(prop.getID_String().contentEquals(comb.getIdenticiation_string())) {
								if(((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getType().equals("buffer")){
									comb.getBuffer_operations().add(prop);
								}else if(((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getType().equals("shared_resource")){ //wieso stand da buffer?
									comb.getSharedResource_operations().add(prop);
								}else {
									System.out.println(logLinePrefix+"ERROR__________WRONG OPERATION TYPE "+((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getType());
								}
							}
						}				
					}
					break;
				//}else if(System.currentTimeMillis()<=reply_by_date_long){ 
				}else if(number_of_answers == resourceAgents.size()) {
					//System.out.println("DEBUG_______________________________ARRANGE Additional Operation __________________________ALL ANSWERS THERE");
					finished = true;
					for(Proposal prop1 : proposals) {
						//receivedProposals_buffer.add(prop_buffer);
						for(OperationCombination comb : list) {
							if(prop1.getID_String().contentEquals(comb.getIdenticiation_string())) {
								if(((AllocatedWorkingStep)prop1.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getType().equals("buffer") && Long.parseLong(((AllocatedWorkingStep)prop1.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getEndDate())+Run_Configuration.transport_estimation == Long.parseLong(((AllocatedWorkingStep)comb.getProd_allWS()).getHasTimeslot().getStartDate())){
									comb.getBuffer_operations().add(prop1);
								}else if(((AllocatedWorkingStep)prop1.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getType().equals("shared_resource")){ //wieso stand da buffer?
									comb.getSharedResource_operations().add(prop1);
								}else {
									System.out.println(logLinePrefix+"ERROR__________WRONG OPERATION TYPE "+((AllocatedWorkingStep)prop1.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getType());
								}
							}
						}				
					}
					break;
				}else if(System.currentTimeMillis()<=reply_by_date_CFP){ 
					step1 = 2;				
					//block(10);
					break;
				} 
				
				//break;
			case 2:

				// Receive all proposals
				number_of_answers += myAgent.receiveProposals(conversationID, agents, proposals);												
				number_of_answers += myAgent.receiveRejection(conversationID, agents, proposals);		
				
				step1 = 1;			
				break;		
			}//switch   
			}//while
			if(sendCFP.getHasCFP().size() == proposals.size()) {
				return true;
			}else {
				return false;
			}
		}else { //no buffer/x needed to be arranged
			return true;
		}
		
		
	}

	//arranges a buffer operation / proposal for a production operation and adds it to the list of buffer proposals
	/*
	private boolean arrangeBuffer(ArrayList <OperationCombination> list) {
		_SendCFP sendCFP = new _SendCFP();
		for(OperationCombination comb : list) {
			Proposal prop = comb.getInitial_proposal_production();
			if(checkIfBufferPlaceisNeeded(prop, myAgent.getLastProductionStepAllocated())) {
				comb.setBuffer_needed(true);		
				sendCFP.addHasCFP(createCFP(myAgent.getLastProductionStepAllocated(), prop, "buffer", null));
			}
		}
		
		
		
		
		ArrayList<AID> buffer_agents = new ArrayList<AID>();
		ArrayList<Proposal>proposals_buffer = new ArrayList<Proposal>();
		int step1 = 0;
		Boolean finished = false;
		int number_of_answers = 0;
		//long reply_by_date_long = 0;
		//String conversationID_buffer = prop_production.getID_String();
		while(!finished) {
		switch (step1) {
		
		case 0:		
			sendCfps(sendCFP);
			//buffer_agents = myAgent.findOfferingAgents(requested_operation_buffer);
			
			
			//myAgent.sendCfps(cfp_timeslot, requested_operation_buffer, conversationID, buffer_agents, reply_by_date);
				
			step1 = 1;
			block((long) (0.25*myAgent.getProductionManagerBehaviour().reply_by_time));
			break;
		case 1:	
			//deadline	
			//if deadline expired or all proposals are received --> get transport proposals
			if(System.currentTimeMillis()>reply_by_date_CFP) {
				System.out.println("DEBUG_______________________________ARRANGE BUFFER__________________________ALL TIME EXPIRED");
				finished = true;
				for(Proposal prop_buffer : proposals_buffer) {
					//receivedProposals_buffer.add(prop_buffer);
					for(OperationCombination comb : list) {
						if(prop_buffer.getID_String().contentEquals(comb.getIdenticiation_string())) {
							comb.getBuffer_operations().add(prop_buffer);
						}
					}				
				}
				break;
			//}else if(System.currentTimeMillis()<=reply_by_date_long){ 
			}else if(number_of_answers == resourceAgents.size()) {
				System.out.println("DEBUG_______________________________ARRANGE BUFFER __________________________ALL ANSWERS THERE");
				finished = true;
				for(Proposal prop_buffer : proposals_buffer) {
					//receivedProposals_buffer.add(prop_buffer);
					for(OperationCombination comb : list) {
						if(prop_buffer.getID_String().contentEquals(comb.getIdenticiation_string())) {
							comb.getBuffer_operations().add(prop_buffer);
						}
					}				
				}
				break;
			}else if(System.currentTimeMillis()<=reply_by_date_CFP){ 
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
		if(sendCFP.getHasCFP().size() == proposals_buffer.size()) {
			return true;
		}else {
			return false;
		}
	}
*/
	private CFP createCFP(AllocatedWorkingStep lastProductionStepAllocated, Proposal prop, String type, Capability req_cap) {
		Operation requested_operation = new Operation();
		if(req_cap != null) {
			requested_operation = ((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation();
		}else {
			requested_operation = new Production_Operation();
			requested_operation.setType(type);
		}
		
		//requested_operation.setAppliedOn(myAgent.getRepresented_Workpiece());
			if(type.equalsIgnoreCase("buffer")) { //falls Bereiche f�r Puffer festgelegt sind
				requested_operation.setAppliedOn(myAgent.getRepresented_Workpiece());
				requested_operation.setStartStateNeeded(lastProductionStepAllocated.getHasResource().getHasLocation());
				requested_operation.setEndState(((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasResource().getHasLocation());			
			}
				
				//requested_operation.setEndState(((AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getEndState());		
				
				//}
			//determine the requested timeslot (earliest start date, latest finish date) of the operation
			Timeslot cfp_timeslot =  determineCFPTimeslot(prop);	 
			OperationCombination opcomb = new OperationCombination(prop, null);
			
			CFP cfp = createCFP_ontologyElement(lastProductionStepAllocated, (AllocatedWorkingStep)prop.getConsistsOfAllocatedWorkingSteps().get(0), opcomb, requested_operation, cfp_timeslot);
		
		return cfp;
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
			long reply_by_date_long = System.currentTimeMillis()+Run_Configuration.reply_by_time_wp_agent;
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
				if(cfp_current.getHasOperation().getIsEnabledBy().size() >0) {
					@SuppressWarnings("unchecked")
					Iterator<Capability>it2 = cfp_current.getHasOperation().getIsEnabledBy().iterator();
					while(it2.hasNext()) {	
						Capability cap = it2.next();
						resourceAgents = myAgent.findOfferingAgents(cap);
						for (int i = 0;i<resourceAgents.size();i++){
							cfp.addReceiver(resourceAgents.get(i));
							//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"cfp sent to receiver: "+result[i].getName().getLocalName()+" with content "+cfp.getContent());
						}
						myAgent.send(cfp);	
					}
					break;
					
				}else {//wird jeweils �berschrieben, sollte aber f�r alle CFPs gleich sein
					resourceAgents = myAgent.findOfferingAgents(cfp_current.getHasOperation());
					for (int i = 0;i<resourceAgents.size();i++){
						cfp.addReceiver(resourceAgents.get(i));
						//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+"cfp sent to receiver: "+result[i].getName().getLocalName()+" with content "+cfp.getContent());
					}
					myAgent.send(cfp);
					break;									
				}			
			}
			
			
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
		long reply_by_date_long2 = System.currentTimeMillis()+Run_Configuration.reply_by_time_wp_agent;
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
				accept_proposal.setInReplyTo((String.valueOf(combination_best.getBest_path().getList_of_best_proposals().get(i).getID_Number())));
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
		int successful_combination_found = 0;

	    
		for(OperationCombination combination : listOfCombinations) {				
			if(combination.calculateValues()==1) {
				successful_combination_found++;
			}
		}
		
		if(successful_combination_found>0) {
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
		}else {
			return null;
		}
		
	}

	private Boolean checkIfBufferPlaceisNeeded(Proposal proposal, AllocatedWorkingStep allocatedWorkingStep) {
		if(allocatedWorkingStep != null) {
			long startdate = Long.parseLong(((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getStartDate());
			if((startdate-myAgent.getTransport_estimation())-(Long.parseLong(allocatedWorkingStep.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation())>_Agent_Template.bufferThreshold*60*1000){	//110 - 10   - 100 + 10     100 - 110
				System.out.println("(startdate-myAgent.getTransport_estimation()) "+(startdate-myAgent.getTransport_estimation())+" - "+(Long.parseLong(allocatedWorkingStep.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation())+"  (Long.parseLong(allocatedWorkingStep.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation())"+"_____"+((startdate-myAgent.getTransport_estimation())-(Long.parseLong(allocatedWorkingStep.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation())));
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
		//	timeslot.setStartDate(Long.toString(Long.parseLong(LAST_alWS_Production.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation_CFP()));
			timeslot.setStartDate(Long.toString(Long.parseLong(LAST_alWS_Production.getHasTimeslot().getEndDate())));
			timeslot.setEndDate(Long.toString(Long.parseLong(((AllocatedWorkingStep)prop_production.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().getStartDate())-myAgent.getTransport_estimation()));
	
		return timeslot;
	}
	

	public boolean done() {
	
		return step == 5;
	}
}  