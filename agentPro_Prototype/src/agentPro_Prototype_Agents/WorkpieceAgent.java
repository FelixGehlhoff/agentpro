package agentPro_Prototype_Agents;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
//import java.util.ArrayList;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Capability;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.OrderPosition;
import agentPro.onto.OrderedOperation;
import agentPro.onto.ProductionPlan;
import agentPro.onto.Proposal;
import agentPro.onto.Timeslot;
import agentPro.onto.Transport_Operation;
import agentPro.onto.WorkPlan;
import agentPro.onto.Workpiece;
import agentPro.onto._SendCFP;
import agentPro.onto._SendProposal;
import agentPro_Prototype_WorkpieceAgent_Behaviours.ProductionManagerBehaviour;
import agentPro_Prototype_WorkpieceAgent_Behaviours.InformWorkpieceArrivalAndDeparture;
import agentPro_Prototype_WorkpieceAgent_Behaviours.MQTTListener_dummy;
import agentPro_Prototype_WorkpieceAgent_Behaviours.Simulation_Listener;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import support_classes.Interval;
import support_classes.OperationCombination;
import support_classes.Run_Configuration;
import webservice.ManufacturingOrder;

/*
 * Models a workpiece. Is created by order agent. Sends CFPs, receives PROPOSALS, sends ACCEPT_PROPOSALs and receives
 * INFORM if job was done or FAILURE. If all necessary steps are preformed the agent contacts the order agent
 * that the workpiece is complete.
 */
public class WorkpieceAgent extends _Agent_Template{
	private static final long serialVersionUID = 1L;
	//private ArrayList<JSONObject> inform_messages = new ArrayList<JSONObject>();	
	private ProductionManagerBehaviour productionManagerBehaviour;
	private MQTTListener_dummy MQTTListener_dummy;
	private Simulation_Listener Simulation_Listener;
	
	private AID orderAgent;
	private String conversationID_forOrderAgent;
	

	//private WorkPlan workplan;
	//private String location_old = "Warehouse"; //start location
	private Location locationOfWorkpiece;
	private Location locationOfStartingWarehouse;
	//private int amountOfTimeLeft;
	private int orderNumber;
	
	//public int avg_pickUp = 5;
	
	private OrderPosition orderPos;
	private ProductionPlan prodPlan;
	private Workpiece represented_Workpiece;
	//private Location lastLocation;
	//private Resource nextProductionResource;
	public boolean useCurrentLocationDueToDisturbance = false;
	
	public long startCoordinationProcess;
	public long EndCoordinationProcess;
	private ManufacturingOrder mo;
	
	protected void setup (){
		logLinePrefix = getLocalName();
	
		Object[] args = getArguments();							//arguments of agent
		
		//String workpiece_string = args[0].toString(); 				//String of order
		//get OrderPosition and ProductionPlan(ontology)
		setOrderPos((OrderPosition) args[0]);
		setProductionPlan(orderPos.getContainsProduct().getHasProductionPlan());
		//System.out.println(((OrderedOperation)orderPos.getContainsProduct().getHasProductionPlan().getConsistsOfOrderedOperations().get(0)).getHasProductionOperation().getName());
		setTypeOfOperationsToProduction();												//sets the Type of all production operations to production --> needed in RequestPerformer line 367
		//createWorkplan
		setWorkplan(new WorkPlan());
		setOrderAgent((AID) args[1]);
		this.setConversationID_forOrderAgent(args[2].toString());
		orderNumber = (int) args[3];
		
		Workpiece rep_WP = new Workpiece();
		//rep_WP.setID_String("Workpiece_No_"+orderNumber+"."+this.getOrderPos().getSequence_Number());
		rep_WP.setID_String(orderPos.getContainsProduct().getName()+"_"+orderNumber+"."+this.getOrderPos().getSequence_Number());
		//rep_WP.setBecomes(orderPos.getContainsProduct());
		setRepresented_Workpiece(rep_WP);


		
		//JSONObject obj = new JSONObject(workpiece_string);			//create JSON Object
		//workpiece = obj;
		
		//determine production steps
		/*
		JSONArray production_steps1 = obj.getJSONArray("production_steps");	//create JSON Array of production steps
		for(int i = 0;i<production_steps1.length();i++){
			 String step = (String) production_steps1.get(i);
			 production_steps.add(step);			 
		}*/
		
		// TBD determine start location --> from loc component?
		setLocation((Location) args[4]);
		setLocationOfStartingWarehouse((Location) args[4]);	
		
		if(_Agent_Template.webservice_mode) {
			this.setMo((ManufacturingOrder) args[5]);
		}
		
		super.setup();
		registerAtDF();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
		productionManagerBehaviour = new ProductionManagerBehaviour(this);
        addBehaviour(productionManagerBehaviour);
        MQTTListener_dummy = new MQTTListener_dummy(this);
        addBehaviour(MQTTListener_dummy);
        Simulation_Listener = new Simulation_Listener(this);
        addBehaviour(Simulation_Listener);	
	}
	public ArrayList<AID> findOfferingAgents(Capability cap){ //translate the capability to a similar operation
		
		Operation requested_operation = new Operation();
		requested_operation.setName(cap.getName());
		requested_operation.setType("shared_resource");
		
		return findOfferingAgents(requested_operation);	
	}
	public ArrayList<AID> findOfferingAgents(Operation requested_operation){
		ArrayList<AID> resourceAgents = new ArrayList<AID>();
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		//service_type = requested_operation.getType();
		sd.setType(requested_operation.getType()); 	
		String service_name = null;
		if(requested_operation.getType().equals("production")){							//es wird nach Anbietern von z.B. Fräsen gesucht
			String req_capability = requested_operation.getName();
			String[] parts = req_capability.split("_");	
			sd.setName(parts[0]);	//or requested_destination
			service_name = parts[0];
		}else if(requested_operation.getType().equals("shared_resource")) {
			sd.setName(requested_operation.getName());	//or requested_destination
			service_name = requested_operation.getName();
		}
		
		template.addServices(sd);
		
		//check the agents we have already stored
		if(this.resourceAgents.size() > 0) {
			//DFAgentDescription[] result = 
			for(DFAgentDescription a : this.resourceAgents) {				
				  @SuppressWarnings("unchecked")
					Iterator<ServiceDescription> it = a.getAllServices();
				    while(it.hasNext()) {
				    	ServiceDescription service_description = it.next();
				    	if(service_description.getType().equals(requested_operation.getType())) {
				    		if(requested_operation.getType().equals("production")){
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
				DFAgentDescription[] result = DFService.search(this, template);
			if (result.length != 0){					
				//resourceAgents = new AID[result.length];
				for (int i = 0; i < result.length; ++i) {
					//resourceAgents[i] = result[i].getName();
					this.addResourceAgent(result[i]);
					resourceAgents.add(result[i].getName());
				}
			}else {
				System.out.println(_Agent_Template.SimpleDateFormat.format(new Date())+" "+this.getLocalName()+logLinePrefix+"no Agent with operation"+requested_operation.getName()+" was found");
				//Handling of this situation --> tbd
			}
		}

	
			 catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			 }
		}	
		return resourceAgents;	
	}
	private void setTypeOfOperationsToProduction() {
		@SuppressWarnings("unchecked")
		Iterator<OrderedOperation> it = prodPlan.getConsistsOfOrderedOperations().iterator();
	    while(it.hasNext()) {
	    	OrderedOperation orderedOp = it.next();	  
	    	orderedOp.getHasProductionOperation().setType("production");
	    }		
	}
	public ProductionManagerBehaviour getProductionManagerBehaviour() {
		return productionManagerBehaviour;
	}

	public void setProductionManagerBehaviour(ProductionManagerBehaviour productionManagerBehaviour) {
		this.productionManagerBehaviour = productionManagerBehaviour;
	}
	public Workpiece getRepresented_Workpiece() {
		return represented_Workpiece;
	}
	public void setRepresented_Workpiece(Workpiece represented_Workpiece) {
		this.represented_Workpiece = represented_Workpiece;
	}
	
	public AID getOrderAgent() {
		return orderAgent;
	}
	public void setOrderAgent(AID orderAgent) {
		this.orderAgent = orderAgent;
	}
	public String getConversationID_forOrderAgent() {
		return conversationID_forOrderAgent;
	}
	public void setConversationID_forOrderAgent(String conversationID_forOrderAgent) {
		this.conversationID_forOrderAgent = conversationID_forOrderAgent;
	}
	public Location getLocation() {
		return locationOfWorkpiece;
	}
	public void setLocation(Location location) {
		this.locationOfWorkpiece = location;
	}
	
	void registerAtDF() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("workpiece");
		//sd.setName(Integer.toString(orderNumber)+"."+orderPos.getSequence_Number()+"."+orderPos.getContainsProduct().getName());			// workpiece registered at DF with order_number.orderPosition.product_name as String, e.g. 123.1.ABC
		sd.setName(Integer.toString(orderNumber));			// workpiece registered at DF with order_number	
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
	}
	public OrderPosition getOrderPos() {
		return orderPos;
	}
	public void setOrderPos(OrderPosition orderPos) {
		this.orderPos = orderPos;
	}
	public ProductionPlan getProdPlan() {
		return prodPlan;
	}
	public void setProductionPlan(ProductionPlan prodPlan) {
		this.prodPlan = prodPlan;
	}

	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}


	public long getTime_until_end() {
		return time_until_end;
	}

	public long getTransport_estimation() {
		return transport_estimation;
	}

	public AllocatedWorkingStep getLastProductionStepAllocated() {
		//counts down from the last object in the allocated WS and tries to find the last production step
		AllocatedWorkingStep LAST_alWS_Production = null;
		int sizeOfAllWSs = getWorkplan().getConsistsOfAllocatedWorkingSteps().size();
		//int counter = 0;
		for(int i = sizeOfAllWSs; i>0 ; i--) {
			AllocatedWorkingStep alWS = (AllocatedWorkingStep) getWorkplan().getConsistsOfAllocatedWorkingSteps().get(i-1);
			if(alWS.getHasOperation().getType().equals("production") && LAST_alWS_Production == null) {
				LAST_alWS_Production = alWS;
				break;
				
			}
		}
		return LAST_alWS_Production;
	}
	public AllocatedWorkingStep get_BEFORE_LastProductionStepAllocated() {
		int sizeOfAllWSs = getWorkplan().getConsistsOfAllocatedWorkingSteps().size();
		AllocatedWorkingStep LAST_alWS_Production = null;
		AllocatedWorkingStep BEFORE_LAST_alWS_Production = null;
		
		//counts down from the last object in the allocated WS and tries to find the last production step
		int counter = 0;
		for(int i = sizeOfAllWSs; i>0 ; i--) {
			AllocatedWorkingStep alWS = (AllocatedWorkingStep) getWorkplan().getConsistsOfAllocatedWorkingSteps().get(i-1);
			if(alWS.getHasOperation().getType().equals("production") && LAST_alWS_Production == null) {
				LAST_alWS_Production = alWS;
				counter = i;
				break;
				
			}
		}
		
		for(int j = counter-1; j>0 ; j--) {
			AllocatedWorkingStep alWS = (AllocatedWorkingStep) getWorkplan().getConsistsOfAllocatedWorkingSteps().get(j-1);
		if(alWS.getHasOperation().getType().equals("production") && BEFORE_LAST_alWS_Production == null) {
			BEFORE_LAST_alWS_Production = alWS;
			break;
		}
		
	}
		return BEFORE_LAST_alWS_Production;
	}
	public boolean checkConsistencyAndAddStepToWorkplan(AllocatedWorkingStep allocWorkingstep_toBeAdded) {
		
		Interval interval_of_WorkingStep_toBeAdded = new Interval(allocWorkingstep_toBeAdded.getHasTimeslot().getStartDate(), allocWorkingstep_toBeAdded.getHasTimeslot().getEndDate(), false);
		Boolean no_conflicting_interval = true;
		String printout= SimpleDateFormat.format(new Date())+" "+getLocalName()+logLinePrefix;
		//is there already a step at this point in time?
		 @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
		    while(it.hasNext()) {
		    	AllocatedWorkingStep allWorkingStep_alreadyInWorkplan = it.next();
		    	Interval interval_of_WorkingStep_inPlan = new Interval(allWorkingStep_alreadyInWorkplan.getHasTimeslot().getStartDate(), allWorkingStep_alreadyInWorkplan.getHasTimeslot().getEndDate(), false);
		    	Interval intersection = interval_of_WorkingStep_toBeAdded.intersection(interval_of_WorkingStep_inPlan);
		    	if(intersection.getSize() > 1) {
		    		no_conflicting_interval = false;
			    	printout = printout + " the step "+allocWorkingstep_toBeAdded.getHasOperation().getName()+" cannot be added. Step "+allWorkingStep_alreadyInWorkplan.getHasOperation().getName()+" shares Interval (intersection) "+intersection.toString();
			    		
		    	}
		    	else {
		    		
		    	}
		    }
		    
		    if(!no_conflicting_interval) {
		    	System.out.println(printout);
		    }else {
		    	getWorkplan().addConsistsOfAllocatedWorkingSteps(allocWorkingstep_toBeAdded); 		//allocWorkingStep is added to workplan	
		    	//System.out.println("DEBUG________________WP allocWorkingstep_toBeAdded.Timeslot.getStartdate() "+allocWorkingstep_toBeAdded.getHasTimeslot().getStartDate()+" allocWorkingstep_toBeAdded.Timeslot.getEnddate() "+allocWorkingstep_toBeAdded.getHasTimeslot().getEndDate());
		    }
		if(!no_conflicting_interval) {
			System.out.println("DEBUG");
		}
		return no_conflicting_interval;
	}
	
	public boolean doLocationsMatch(Proposal prop_production, Proposal prop_transport) {
		AllocatedWorkingStep prod = (AllocatedWorkingStep) prop_production.getConsistsOfAllocatedWorkingSteps().get(0);
		Location res_Prod = prod.getHasResource().getHasLocation();
		AllocatedWorkingStep trans = (AllocatedWorkingStep) prop_transport.getConsistsOfAllocatedWorkingSteps().get(0);
		Location target_transport = (Location)((Transport_Operation)trans.getHasOperation()).getEndState();	
		return doLocationsMatch(res_Prod, target_transport);
	}
	
	public boolean check_if_element_of_production_plan(AllocatedWorkingStep allocWorkingstep) {
		boolean found = false;
		 @SuppressWarnings("unchecked")
			Iterator<OrderedOperation> it = getProdPlan().getAllConsistsOfOrderedOperations();
		    while(it.hasNext()) {
		    	OrderedOperation orOp = it.next();			
		    	if(orOp.getHasProductionOperation().getName().equals(allocWorkingstep.getHasOperation().getName())){
		    		found = true;
		    	}
		    	
		    }
		    
		return found;
	}

	public Location getLocationOfStartingWarehouse() {
		return locationOfStartingWarehouse;
	}

	public void setLocationOfStartingWarehouse(Location locationOfStartingWarehouse) {
		this.locationOfStartingWarehouse = locationOfStartingWarehouse;
	}
	
	public int receiveProposals (String conversationID, ArrayList<AID>proposal_senders, ArrayList<Proposal>received_proposals) {
		int numberOfAnswers = 0;
		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
        MessageTemplate mt2 = MessageTemplate.MatchConversationId(conversationID);	
        MessageTemplate mt_total = MessageTemplate.and(mt1,mt2);
        
		ACLMessage reply = receive(mt_total);
		if (reply != null) {
			numberOfAnswers++;
			_SendProposal proposal_onto = new _SendProposal();
			try {
				Action act = (Action) getContentManager().extractContent(reply);
				proposal_onto = (_SendProposal) act.getAction();
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
				
			_Agent_Template.addUnique(proposal_senders, reply.getSender());
				
				@SuppressWarnings("unchecked")
				Iterator<Proposal> i = proposal_onto.getAllHasProposal();
				while(i.hasNext()) {
				    	Proposal proposal = i.next();
						received_proposals.add(proposal);				
				}	
	}
		return numberOfAnswers;
	}
	public int receiveRejection(String conversationID, ArrayList<AID>proposal_senders, ArrayList<Proposal>received_proposals) {
		//Boolean rejection_received = false; 
		int numberOfAnswers = 0;
        MessageTemplate ref = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
        MessageTemplate ref2 = MessageTemplate.MatchConversationId(conversationID);
        MessageTemplate mt_total_ref = MessageTemplate.and(ref,ref2);
        ACLMessage refusal = receive(mt_total_ref);	
		if(refusal != null) {
			numberOfAnswers++;
			//rejection_received = true;
			//Proposal ref_workaraound = new Proposal();
			//received_proposals.add(ref_workaraound);		//this empty proposal is added to enable the check in step = 1 to recongnize the answer
			_Agent_Template.addUnique(proposal_senders, refusal.getSender());
		}	
		return numberOfAnswers;
	}

	public void sendCfps(Timeslot cfp_timeslot, Operation requested_operation, String conversationID, ArrayList<AID> resourceAgents, Date reply_by_date) {
		CFP cfp_onto = new CFP();
		 cfp_onto.setHasTimeslot(cfp_timeslot);
		cfp_onto.setHasOperation(requested_operation);
		cfp_onto.setHasSender(getAID());
		//26.02.2019 add quantity
		cfp_onto.setQuantity(getOrderPos().getQuantity());
				
	_SendCFP sendCFP = new _SendCFP();
	sendCFP.addHasCFP(cfp_onto);			
	Action content = new Action(getAID(),sendCFP);

	//create ACL Message
	
	ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
	cfp.setLanguage(getCodec().getName());
	cfp.setOntology(getOntology().getName());
	cfp.setConversationId(conversationID);
	cfp.setReplyByDate(reply_by_date);
	
	

	//ontology --> fill content
	try {
		getContentManager().fillContent(cfp, content);
	} catch (CodecException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (OntologyException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
					
	
	for (int i = 0;i<resourceAgents.size();i++){
		if(this.getLastProductionStepAllocated() != null && resourceAgents.get(i).getLocalName().contentEquals(this.getLastProductionStepAllocated().getHasResource().getName())) {
			InformWorkpieceArrivalAndDeparture.prepareAndSendInformDepartureMessage(this, 0, this.getLastProductionStepAllocated().getHasTimeslot().getEndDate(), this.getLastProductionStepAllocated().getHasResource().getName(), this.getLastProductionStepAllocated().getID_String());
		}
		cfp.addReceiver(resourceAgents.get(i));
		System.out.println(this.SimpleDateFormat.format(new Date())+" "+this.getLocalName()+logLinePrefix+"cfp sent to receiver: "+resourceAgents.get(i).getName()+" with content "+cfp.getContent());
	}
	//myAgent.printOutSent(cfp);			
	send(cfp);
	}

	public boolean checkConsistencyAndAddStepsToWorkplan(OperationCombination combination_best) {
		// TODO Auto-generated method stub
		return false;
	}

	public ManufacturingOrder getMo() {
		return mo;
	}

	public void setMo(ManufacturingOrder mo) {
		this.mo = mo;
	}
	public long getTransport_estimation_CFP() {
		return Run_Configuration.transport_estimation_CFP;
	}

}
