package agentPro_Prototype_Agents;

import java.sql.Connection;
import java.util.Date;
//import java.util.ArrayList;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;

//import org.json.JSONArray;
//import org.json.JSONObject;

import agentPro.onto.Location;
import agentPro.onto.OrderPosition;
import agentPro.onto.OrderedOperation;
import agentPro.onto.ProductionPlan;
import agentPro.onto.WorkPlan;
import agentPro.onto.Workpiece;
import agentPro_Prototype_WorkpieceAgent_Behaviours.ProductionManagerBehaviour;
import agentPro_Prototype_WorkpieceAgent_Behaviours.MQTTListener_dummy;
import agentPro_Prototype_WorkpieceAgent_Behaviours.Simulation_Listener;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import support_classes.Interval;

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
	
	private long transport_estimation = (long) 1000*60*50;	//estimated duration of transport = 50 min
	public int avg_pickUp = 10;
	
	private OrderPosition orderPos;
	private ProductionPlan prodPlan;
	private Workpiece represented_Workpiece;
	//private Location lastLocation;
	//private Resource nextProductionResource;
	
	protected void setup (){
		logLinePrefix = getLocalName();
		Location location = new Location();
			float startx = 0;
			float starty = 0;
			location.setCoordX(startx);
			location.setCoordY(starty);
		setLocationOfStartingWarehouse(location);	
		Object[] args = getArguments();							//arguments of agent
		
		//String workpiece_string = args[0].toString(); 				//String of order
		//get OrderPosition and ProductionPlan(ontology)
		setOrderPos((OrderPosition) args[0]);
		setProductionPlan(orderPos.getContainsProduct().getHasProductionPlan());
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
		
		super.setup();
		registerAtDF();
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
		productionManagerBehaviour = new ProductionManagerBehaviour(this);
        addBehaviour(productionManagerBehaviour);
        MQTTListener_dummy = new MQTTListener_dummy(this);
        addBehaviour(MQTTListener_dummy);
        Simulation_Listener = new Simulation_Listener(this);
        addBehaviour(Simulation_Listener);	
	}
	
	private void setTypeOfOperationsToProduction() {
		@SuppressWarnings("unchecked")
		Iterator<OrderedOperation> it = prodPlan.getConsistsOfOrderedOperations().iterator();
	    while(it.hasNext()) {
	    	OrderedOperation orderedOp = it.next();	  
	    	orderedOp.getHasOperation().setType("production");
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
	public void setTime_until_end(long time_until_end) {
		this.time_until_end = time_until_end;
	}
	public long getTransport_estimation() {
		return transport_estimation;
	}
	public void setTransport_estimation(long transport_estimation) {
		this.transport_estimation = transport_estimation;
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
		Boolean interval_already_in_list = false;
		String printout= SimpleDateFormat.format(new Date())+" "+getLocalName()+logLinePrefix;
		//is there already a step at this point in time?
		 @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
		    while(it.hasNext()) {
		    	AllocatedWorkingStep allWorkingStep_alreadyInWorkplan = it.next();
		    	Interval interval_of_WorkingStep_inPlan = new Interval(allWorkingStep_alreadyInWorkplan.getHasTimeslot().getStartDate(), allWorkingStep_alreadyInWorkplan.getHasTimeslot().getEndDate(), false);
		    	Interval intersection = interval_of_WorkingStep_toBeAdded.intersection(interval_of_WorkingStep_inPlan);
		    	if(intersection.getSize() > 1) {
		    		interval_already_in_list = true;
			    	printout = printout + " the step "+allocWorkingstep_toBeAdded.getHasOperation().getName()+" cannot be added. Step "+allWorkingStep_alreadyInWorkplan.getHasOperation().getName()+" shares Interval (intersection) "+intersection.toString();
			    		
		    	}
		    	else {
		    		
		    	}
		    }
		    
		    if(interval_already_in_list) {
		    	System.out.println(printout);
		    }else {
		    	getWorkplan().addConsistsOfAllocatedWorkingSteps(allocWorkingstep_toBeAdded); 		//allocWorkingStep is added to workplan	
		    	//System.out.println("DEBUG________________WP allocWorkingstep_toBeAdded.Timeslot.getStartdate() "+allocWorkingstep_toBeAdded.getHasTimeslot().getStartDate()+" allocWorkingstep_toBeAdded.Timeslot.getEnddate() "+allocWorkingstep_toBeAdded.getHasTimeslot().getEndDate());
		    }
		
		return interval_already_in_list;
	}
	public Boolean doLocationsMatch(Location LocationA, Location locationB) {
		if(LocationA.getCoordX() == locationB.getCoordX() && LocationA.getCoordY() == locationB.getCoordY()) {
			return true;
		}else {
			return false;
		}

	}
	
	public boolean check_if_element_of_production_plan(AllocatedWorkingStep allocWorkingstep) {
		boolean found = false;
		 @SuppressWarnings("unchecked")
			Iterator<OrderedOperation> it = getProdPlan().getAllConsistsOfOrderedOperations();
		    while(it.hasNext()) {
		    	OrderedOperation orOp = it.next();			
		    	if(orOp.getHasOperation().getName().equals(allocWorkingstep.getHasOperation().getName())){
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
	

}
