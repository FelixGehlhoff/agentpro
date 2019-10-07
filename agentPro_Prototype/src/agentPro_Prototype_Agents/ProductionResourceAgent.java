package agentPro_Prototype_Agents;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Capability;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.ProductionResource;
import agentPro.onto.Production_Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Setup_state;
import agentPro.onto.State;
import agentPro.onto.Timeslot;
import agentPro.onto.Workpiece;
import agentPro_Prototype_ResourceAgent.ReceiveInformWorkpieceDepartureBehaviour;
import agentPro_Prototype_ResourceAgent.ReceiveIntervalForConnectedResourceBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import support_classes.Interval;
import support_classes.Storage_element_slot;

/* 
 * Models a production resource. It has capabilites (at the moment 16.05. only one) which it registers at the DF.
 * It Receives CFPs by the workpiece agents, answers with an offer and waits for an order on that offer (if 
 * there is no offer, the behaviour quits). After receiving an order the working step is performed.
 * Current workaround 1 16.05. : Instead of sensing the arrival of the workpiece the agent waits for a message from
 * 		the transport agent.
 * Current workaround 2 16.05. : Instead of really performing the working step, the agent waits for a specified time
 * 		for the performed capability (given as argument at creation).
 */

public class ProductionResourceAgent extends ResourceAgent{

	private static final long serialVersionUID = 1L;
	//private String production_capability;				//for testing only one capability
	private int duration_of_process;
	//private ReceiveCFPBehaviour ReceiveCFPBehav;
	//private int offerNumber = 1;
	private ReceiveInformWorkpieceDepartureBehaviour ReceiveInformWorkpieceDepartureBehav;
	protected ProductionResource representedResource = new ProductionResource();
	private int avg_pickUp = 10; 
	//private int avg_setUp = 0;
	private String columnNameOfEnablesWPType = "enables_wp_type";
	private ArrayList<String> enabledWorkpieces = new ArrayList<>();
	private HashMap<String, Double> setup_matrix = new HashMap<String, Double>();
	private String startState = "B";
	public Boolean parallel_processing_pick_and_setup_possible = false;
	
	public HashMap<String, Double> getSetup_matrix() {
		return setup_matrix;
	}

	public void setSetup_matrix( HashMap<String, Double> hashmap) {
		this.setup_matrix = hashmap;
	}

	protected void setup (){		
			
		super.setup();
		// / INITIALISATION
		// /////////////////////////////////////////////////////////
		logLinePrefix = logLinePrefix+".ProductionRessourceAgent.";						
		//Object[] args = getArguments();
		//production_capability = args[0].toString();
		//duration_of_process = Integer.valueOf(args[1].toString());
			
		 // Register the service in the yellow pages
		//registerAtDF();
		representedResource = new ProductionResource();
		representedResource.setName(this.getLocalName());
		receiveValuesFromDB(representedResource);
		setStartState();

		
		receiveCapabilityOperationsValuesFromDB(representedResource);
		receiveWorkPlanValuesFromDB(representedResource);
		createSetupMatrix(representedResource);
		
		//logLinePrefix = "ProductionRessourceAgent."+production_capability;
		registerAtDF();
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
		ReceiveInformWorkpieceDepartureBehav = new ReceiveInformWorkpieceDepartureBehaviour(this);
        addBehaviour(ReceiveInformWorkpieceDepartureBehav);
        if(simulation_enercon_mode) {
        	 if(getLocalName().equals("Skoda_1_1") || getLocalName().equals("Skoda_2_1") || getLocalName().equals("Skoda_3_1")) {
        		 addBehaviour(new ReceiveIntervalForConnectedResourceBehaviour(this));
			    }
        }
	}

	public void setStartState() {
		Setup_state start = new Setup_state();
		start.setID_String(startState);
		this.getRepresentedResource().setStartState(start);
	}
	

	public int getDuration_of_process() {
		return duration_of_process;
	}

	public void setDuration_of_process(int duration_of_process) {
		this.duration_of_process = duration_of_process;
	}
	/*
	public String getProduction_capability() {
		return production_capability;
	}

	public void setProduction_capability(String production_capability) {
		this.production_capability = production_capability;
	}*/
/*
	public int getOfferNumber() {
		return offerNumber;
	}

	public void setOfferNumber(int offerNumber) {
		this.offerNumber = offerNumber;
	}*/

	void registerAtDF() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());		
		ServiceDescription sd = new ServiceDescription();
		sd.setType(this.getRepresentedResource().getDetailed_Type());
			String capability = this.getRepresentedResource().getHasCapability().getName();
			String[] parts = capability.split("_");			
			if(parts.length > 1) {
				sd.setName(parts[0]);
			}else {
				sd.setName(capability);
			}
		sd.addProtocols(this.getLocalName());		//to enable search for a specific agent
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
	}

	@Override
	public boolean feasibilityCheckAndDetermineDurationParameters(Operation operation) {
		String wp_type = operation.getAppliedOn().getID_String().split("_")[0];
		
		Boolean type_enabled = findMatchStringInArrayList(wp_type, enabledWorkpieces);
		//System.out.println("DEBUG_____wp_type"+wp_type+"_____TYPE ENABLED? "+type_enabled);
		
	    @SuppressWarnings("unchecked")
		Iterator<Operation> it = representedResource.getHasCapability().getEnables().iterator();
	    while(it.hasNext()) {
	    	Operation possible_op = it.next();
	    	if(possible_op.getName().equals(operation.getName()) && type_enabled) {
	    		if(operation.getAvg_Duration() > 0) {	//When does this happen??
	    			System.out.println("DEBUG_________________"+this.getLocalName()+"____________________________WHY IS OPERATION DURATION NOT 0?");
	    		}else {	    			
	    			operation.setAvg_Duration(possible_op.getAvg_Duration());
	    			//System.out.println("DEBUG_____possible_op.getName() "+possible_op.getName()+possible_op.getAvg_Duration()+"___Duration set "+possible_op.getAvg_Duration());	
	    		}
	    		
	    		return true;
	    	}
		
	    }	

		
		return false;
	}
	

@Override
public void receiveValuesFromDB(Resource r) {
		
	    Statement stmt = null;
	    String query = "";

	    	query = "select "+columnNameResourceName_simulation+" , "+columnNameOfSetupTime+" , "+columnNameOfID+" , "+columnNameOfLocationX+" , "+columnNameOfLocationY+" , "+columnNameOfCapability+" , "+columnNameOfResource_Type+" , "+columnNameResourceDetailedType+" from "+tableNameResource+" where "+columnNameResourceName_simulation+" = '"+representedResource.getName()+"'"; 
	   
	    try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	      
	        while (rs.next()) {
	        	Location l = new Location(); 
			    l.setCoordX((float) rs.getDouble(columnNameOfLocationX));
			    l.setCoordY((float) rs.getDouble(columnNameOfLocationY));
			    r.setHasLocation(l);
			    	Capability cap = new Capability();
			    	cap.setName(rs.getString(columnNameOfCapability));
			    r.setHasCapability(cap);
			    r.setType(rs.getString(columnNameOfResource_Type));
			    r.setDetailed_Type(rs.getString(columnNameResourceDetailedType));
			    r.setID_Number(rs.getInt(columnNameOfID));
			   // this.avg_setUp = (int) rs.getDouble(columnNameOfSetupTime);
	        }
	       
	        
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    }
	    
	    //create setup matrix
	    if(!r.getDetailed_Type().contentEquals("buffer")) {
		    String query2 = "";

	    	query2 = "select "+columnNameOfChangeover+" , `"+this.getRepresentedResource().getName()+"` from "+tableNameResourceSetupMatrix; 
	   
	    
	    	
	    	if(!_Agent_Template.prefix_schema.equals("flexsimdata")){
	    		try {
	    		ResultSet rs2 = stmt.executeQuery(query2);
	            HashMap<String, Double> matrix = new  HashMap<String, Double>();
	            while (rs2.next()) {
	            	matrix.put(rs2.getString(columnNameOfChangeover), rs2.getDouble(this.getRepresentedResource().getName()));
	            }
	            setSetup_matrix(matrix);
	    	    } catch (SQLException e ) {
	    	    	e.printStackTrace();
	    	    }
	    	}
	    }

	    
	}
    
    //create setup matrix
 public void createSetupMatrix(Resource r) {
    if(!r.getDetailed_Type().contentEquals("buffer")) {
	    String query2 = "";
	    Statement stmt = null;
    	query2 = "select "+columnNameOfChangeover+" , `"+this.getRepresentedResource().getName()+"` from "+tableNameResourceSetupMatrix; 
   
    
    	
    	if(!_Agent_Template.prefix_schema.equals("flexsimdata")){
    		try {
    			
    		stmt = connection.createStatement();

    		ResultSet rs2 = stmt.executeQuery(query2);
            HashMap<String, Double> matrix = new  HashMap<String, Double>();
            while (rs2.next()) {
            	matrix.put(rs2.getString(columnNameOfChangeover), rs2.getDouble(this.getRepresentedResource().getName()));
            }
            setSetup_matrix(matrix);
    	    } catch (SQLException e ) {
    	    	e.printStackTrace();
    	    }
    	}
    }
	
}

public void receiveCapabilityOperationsValuesFromDB(Resource r) {	
	Capability cap2 = r.getHasCapability();
    Statement stmt = null;
    String query = "";
	

		query = "select "+columnNameOfID+" , "+columnNameOfTimeConsumption+" , "+columnNameOfCapability_Name+" , "+columnNameOfEnables_Operation+" , "+columnNameOfOperation_Number+" , "+columnNameOfEnablesWPType+" from "+nameOfCapability_Operations_Mapping_Table+" where "+columnNameOfCapability_Name+" = '"+cap2.getName()+"'";;

	
	    try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	     
	        while (rs.next()) {
	        	Operation op = new Operation();
	        		op.setName(rs.getString(columnNameOfEnables_Operation));
	        		cap2.setID_Number(rs.getInt(columnNameOfID));
	        		op.setAvg_Duration(rs.getInt(columnNameOfTimeConsumption));   	
	        	cap2.addEnables(op);
	        		String enabledWorkpieces = rs.getString(columnNameOfEnablesWPType);
	        		String [] split = enabledWorkpieces.split(",");
	        		for(String part : split) {
	        			this.enabledWorkpieces.add(part);
	        		}
	        }
	     r.setHasCapability(cap2);
	        
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    } 
}

@Override
//public Proposal checkScheduleDetermineTimeslotAndCreateProposal(long startdate_cfp, long enddate_cfp, Operation operation) {
public Proposal checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp) {
	
	Proposal proposal = new Proposal();
	Timeslot timeslot_for_proposal = new Timeslot();
	long estimated_start_date = 0;


	int deadline_not_met = 0;
	float duration_total_for_price = 0;
	//float duration_for_answering_CFP_so_for_Workpiece_schedule = 0;
	
	//extract CFP Timeslot
			Timeslot cfp_timeslot = cfp.getHasTimeslot();	
			Operation operation = cfp.getHasOperation();
			operation.setType("production");
			long startdate_cfp = Long.parseLong(cfp_timeslot.getStartDate());
			long enddate_cfp = Long.parseLong(cfp_timeslot.getEndDate());
			
			
			float duration_eff = calculateDurationOfProcessWithoutSetup(operation, cfp.getQuantity());
			
			float time_increment_or_decrement_to_be_added_for_setup_of_next_task = 0; //TODO integrieren
	//long enddate_interval = 0;
	

	ArrayList<Interval> listOfIntervals = new ArrayList<Interval>();
//System.out.println("DEBUG_______productionResourceAgent  timeslot_interval_to_be_booked_production "+timeslot_interval_to_be_booked_production.getSize()/(60*1000));
	
	boolean slot_found = false;
	double duration_setup = 0;
	long setup_and_pickup_to_consider = 0;
	Storage_element_slot slot = null;
	
	for(int i = 0;i<getFree_interval_array().size();i++) {	
		//dependent parameters
		duration_setup = calculateDurationSetup(getFree_interval_array().get(i), operation);	// in min
		time_increment_or_decrement_to_be_added_for_setup_of_next_task = calculateTimeIncrement(operation, i, null); // in min    null is detailed operation description (ony needed for transport)
		
			if(parallel_processing_pick_and_setup_possible) {
				setup_and_pickup_to_consider = Math.max((long)duration_setup, (long)avg_pickUp);
			}else {
				setup_and_pickup_to_consider = (long)duration_setup+(long)avg_pickUp;
			}
		operation.setSet_up_time((float)setup_and_pickup_to_consider);
		operation.setAvg_PickupTime(avg_pickUp);
		//System.out.println("DEBUG_________duration_setup "+duration_setup+" setup_and_pickup_to_consider "+setup_and_pickup_to_consider);
		float buffer = 0;
		duration_total_for_price = (float) setup_and_pickup_to_consider + duration_eff + buffer + time_increment_or_decrement_to_be_added_for_setup_of_next_task;	// min
		//duration_for_answering_CFP_so_for_Workpiece_schedule 	=  	 duration_eff + buffer;

		
		if(operation.getAvg_Duration() == 0) { //operation avg duration = 0 in case of buffer place
			System.out.println("DEBUG_________________"+this.getLocalName()+" PRODUCTION AGENT check schedule and determine timeslot --> operation == 0 --> should not be needed");
			//enddate_interval = enddate_cfp;
		}else {
			//enddate_interval =  startdate_cfp+(long) (operation.getAvg_Duration()*60*1000);	
			//duration_for_price = (float) duration_setup + operation.getAvg_Duration();
		//this.getReceiveCFPBehav().duration_for_price = set_up_time + operation.getAvg_Duration(); //in min
		}
		
		//Interval timeslot_interval_to_be_booked_production = new Interval( startdate_cfp-(Math.max((long)duration_setup, avg_pickUp))*60*1000, enddate_interval+avg_pickUp*60*1000, false);	//18.06. pick-up added

		//calculate possible slots within this free interval (FI)
		listOfIntervals = calculateIntervals(startdate_cfp, enddate_cfp, (long)(setup_and_pickup_to_consider*60*1000), (long)(duration_eff*60*1000), (long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000), 0, i); //0 = buffer that production can start earlier
		//System.out.println(this.printoutArraylistIntervals(listOfIntervals));
		
		 checkFeasibility(listOfIntervals, startdate_cfp, enddate_cfp, 0); //no buffer that production can start earlier
		 //System.out.println(this.printoutArraylistIntervals(listOfIntervals));
		
		 //checks the schedule --> LB and UB violated?
		 checkSchedule(listOfIntervals, (long)(setup_and_pickup_to_consider*60*1000), (long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000), i);
		// System.out.println(this.printoutArraylistIntervals(listOfIntervals));
		 
		 if(listOfIntervals.size()>0) {
			 slot_found = true;
			 //sort earliest end first
			 this.sortArrayListIntervalsEarliestFirst(listOfIntervals, "end");
			 //now the best slot is found --> calculate buffers
			 operation.setBuffer_before_operation((listOfIntervals.get(0).lowerBound()-(long)(setup_and_pickup_to_consider*60*1000))-getFree_interval_array().get(i).lowerBound());
			 if(listOfIntervals.get(0).upperBound()>enddate_cfp) {
				 deadline_not_met = 10000; 
			 }
			
			operation.setBuffer_after_operation((getFree_interval_array().get(i).upperBound()-(long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000))-listOfIntervals.get(0).upperBound());
			//System.out.println("DEBUG_prod res agent buffer before operation: "+operation.getBuffer_before_operation()+" buffer after operation: "+operation.getBuffer_after_operation()+" free int: "+getFree_interval_array().get(i).toString()+" work int: "+listOfIntervals.get(0).toString()+" setup/pickup: "+setup_and_pickup_to_consider+" time increment "+time_increment_or_decrement_to_be_added_for_setup_of_next_task);
			timeslot_for_proposal.setEndDate(String.valueOf(listOfIntervals.get(0).upperBound()));
			timeslot_for_proposal.setStartDate(String.valueOf(listOfIntervals.get(0).lowerBound()));	
			timeslot_for_proposal.setLength(listOfIntervals.get(0).upperBound()-listOfIntervals.get(0).lowerBound());
			slot = createStorageElement(operation, timeslot_for_proposal, setup_and_pickup_to_consider*60*1000, (long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000));
			 this.getReceiveCFPBehav().getProposed_slots().add(slot);
			 //TBD Buffer
			 break;
		 }
	}

			if(!slot_found) {
				//if(startdate_cfp+(long)(duration_for_answering_CFP_so_for_Workpiece_schedule*60*1000)>=earliest_finish_date_from_arrive_at_resource) {
					//System.out.println("DEBUG_______WRONG DATA FROM CFP --> startdate_cfp+(long)(duration_for_answering_CFP_so_for_Workpiece_schedule*60*1000)  "+startdate_cfp+(long)(duration_for_answering_CFP_so_for_Workpiece_schedule*60*1000)+" >= "+earliest_finish_date_from_arrive_at_resource+" earliest_finish_date_from_arrive_at_resource    is violated");
				//}
				System.out.println("DEBUG----------"+"NO FREE SLOT FOUND ---> this should not happen   printout   	");
				proposal = null;
			}else {
				

	float price = duration_total_for_price + deadline_not_met;		//strafkosten, wenn deadline_not_met
	
	
	if(simulation_enercon_mode && (this.getRepresentedResource().getName().contains("Skoda_2") || this.getRepresentedResource().getName().contains("Skoda_3")) ) {		//skoda 1 is better
		price = price + 50;
	}else if(simulation_enercon_mode && this.getRepresentedResource().getName().contains("Konfektion") || this.getRepresentedResource().getName().contains("FAD")) {
		if(this.getWorkplan().getConsistsOfAllocatedWorkingSteps().size() > 0) {
			AllocatedWorkingStep lastAllWS = (AllocatedWorkingStep) this.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(this.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1);
			long difference = estimated_start_date - Long.parseLong(lastAllWS.getHasTimeslot().getEndDate());
			double difference_double = difference/(1000*60);
			price = price + this.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()*10000-(float)difference_double;	//biggest difference should get order
		}	
	}
	//timeslot_for_proposal.setEndDate(Long.toString(estimated_enddate));
	//timeslot_for_proposal.setStartDate(Long.toString(estimated_start_date));	
	//timeslot_for_proposal.setLength(estimated_enddate-estimated_start_date);
	
	
	//this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(Long.toString(timeslot_interval_to_be_booked_production.upperBound()));
	//this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(Long.toString(timeslot_interval_to_be_booked_production.lowerBound()));
	//this.getReceiveCFPBehav().timeslot_for_schedule.setLength(timeslot_interval_to_be_booked_production.getSize());
	
	//this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(Long.toString(listOfIntervals.get(0).upperBound()+(long)avg_pickUp*1000*60));
	//this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(Long.toString(listOfIntervals.get(0).lowerBound()-(long)setup_and_pickup_to_consider*1000*60));
	//this.getReceiveCFPBehav().timeslot_for_schedule.setLength((listOfIntervals.get(0).upperBound()-(long)setup_and_pickup_to_consider*1000*60)-(listOfIntervals.get(0).upperBound()+(long)avg_pickUp*1000*60));
	//System.out.println("DEBUG____________timeslot for schedule end"+this.getReceiveCFPBehav().timeslot_for_schedule.getEndDate()+" start "+this.getReceiveCFPBehav().timeslot_for_schedule.getStartDate());
	
	proposal = createProposal(price, operation, timeslot_for_proposal, cfp.getHasSender(), cfp.getHasSender().getLocalName()+"@"+this.getLocalName());	//cfp.getIDString() is empty in CFPs to production resources
	//System.out.println("DEBUG____"+getLocalName()+" getFree_interval_array().size() = "+getFree_interval_array().size());
	slot.setProposal(proposal);
	}
		if(timeslot_for_proposal.getLength() == 0) {
		proposal = null;	
		}
	//System.out.println("DEBUG_____operation.getAvg_Duration()*quantity "+operation.getAvg_Duration()+"______"+this.getLocalName()+" timeslot "+timeslot_for_proposal.getStartDate()+" "+timeslot_for_proposal.getEndDate()+" "+timeslot_for_proposal.getLength());
	return proposal;
}

public void checkSchedule(ArrayList<Interval> listOfIntervals_possibleFromResourceSide, long setup_and_pickup_to_consider, long time_increment_or_decrement_to_be_added_for_setup_of_next_task, int i2) {
	
	//Interval timeslot_interval_to_be_booked_production = new Interval( startdate_cfp-(Math.max((long)duration_setup, avg_pickUp))*60*1000, enddate_interval+avg_pickUp*60*1000, false);	//18.06. pick-up added
//if(this.getRepresentedResource().getName().contentEquals("Durchsatz")) {
//	System.out.println("Break");
//}
	//int counter = 1; //no of element
	 Iterator<Interval> it = listOfIntervals_possibleFromResourceSide.iterator();		 	
	    while(it.hasNext()) {
	    	Interval i = it.next();
	    	//check Free Interval parameters --> Prod Res stays block for [start interval - setup&pickup ; end interval + pickup]
	    	if(i.lowerBound()-setup_and_pickup_to_consider< getFree_interval_array().get(i2).lowerBound() && i.upperBound()+avg_pickUp*60*1000<=getFree_interval_array().get(i2).upperBound()-time_increment_or_decrement_to_be_added_for_setup_of_next_task) {
	    		//System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because LB_Ress-setup_and_pickup_to_consider "+(i.lowerBound()-setup_and_pickup_to_consider)+" < "+getFree_interval_array().get(i2).lowerBound()+" lower_bound_Transporter --> start too early FOR PRODUCTION");
	    		it.remove();
	 		 }else if(i.lowerBound()-setup_and_pickup_to_consider>= getFree_interval_array().get(i2).lowerBound() && i.upperBound() +avg_pickUp*60*1000 > getFree_interval_array().get(i2).upperBound()-time_increment_or_decrement_to_be_added_for_setup_of_next_task){
	 			//System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because UB_Ress "+i.upperBound()+avg_pickUp+" > "+(getFree_interval_array().get(i2).upperBound()-time_increment_or_decrement_to_be_added_for_setup_of_next_task)+" upper_bound_transporter --> Finish too late for TRANSPORTER");
	    		it.remove();
	 		 }else if(i.lowerBound()-setup_and_pickup_to_consider< getFree_interval_array().get(i2).lowerBound() && i.upperBound() +avg_pickUp*60*1000 > getFree_interval_array().get(i2).upperBound()-time_increment_or_decrement_to_be_added_for_setup_of_next_task){
	 			//System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because UB "+i.upperBound()+avg_pickUp+" > "+(getFree_interval_array().get(i2).upperBound()-time_increment_or_decrement_to_be_added_for_setup_of_next_task)+" upper_bound_transporter  --> Finish too late for TRANSPORTER AND because LB_Ress "+(i.lowerBound()-setup_and_pickup_to_consider)+" < "+getFree_interval_array().get(i2).lowerBound()+" lower_bound_Transporter --> start too early FOR TRANSPORTER");
	    		it.remove();
	 		 }else {
	 			 //fine
	 		 }
	    	//counter++;
	    }
}

//checks if for this free interval the calculated intervals are feasible from resource side
	private void checkFeasibility(ArrayList<Interval> listOfIntervals, long startdate_cfp, long enddate_cfp, long buffer_time_that_production_can_start_earlier) {
		//int counter = 1; //no of element
		 Iterator<Interval> it = listOfIntervals.iterator();		 	
		    while(it.hasNext()) {
		    	Interval i = it.next();
		    
				//start >= earliest start, the check "end >= earliest end" is not necessary for production resources
		    	if(i.lowerBound()< startdate_cfp) {
		    		//System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because LB "+i.lowerBound()+" < "+startdate_cfp+" start_cfp --> start too early");
		    		it.remove();
		 		 }
		 		 else {
		 			 //fine
		 		 }
		    	//counter++;
		    }
	}
	
//refers to the actual process (without setup)
	/*
public ArrayList<Interval> calculateIntervals(long startdate_cfp, long enddate_cfp, long duration_setup, long duration_eff, long time_increment_or_decrement_to_be_added_for_setup_of_next_task, long buffer_time_that_production_can_start_earlier, int i) {	//for feasibility checking the arrival dates AT THE RESSOURCES are important
	ArrayList<Interval> array = new ArrayList<>();
	long effr = enddate_cfp - buffer_time_that_production_can_start_earlier;
	Interval end_at_effr = new Interval((long) (effr-duration_eff), effr, false);
	Interval start_at_CFP_start_minus_d2WP = new Interval(startdate_cfp, (long) (startdate_cfp+duration_eff), false);
	Interval start_at_CFP_start = new Interval ((long)(startdate_cfp+duration_setup), (long) (startdate_cfp+duration_setup+duration_eff), false); // should not be needed!
	Interval end_at_latest_end = new Interval((long) (enddate_cfp-duration_eff), enddate_cfp);
	Interval start_at_lowerbound = new Interval((long) (getFree_interval_array().get(i).lowerBound()+duration_setup), (long) (getFree_interval_array().get(i).lowerBound()+duration_setup+duration_eff), false);
	Interval end_at_upperbound = new Interval((long) (getFree_interval_array().get(i).upperBound()-duration_eff-time_increment_or_decrement_to_be_added_for_setup_of_next_task), (long) (getFree_interval_array().get(i).upperBound()-time_increment_or_decrement_to_be_added_for_setup_of_next_task), false);
	array.add(end_at_effr);
	array.get(0).setId("end_at_effr");
	array.add(start_at_CFP_start_minus_d2WP);
	array.get(1).setId("start_at_CFP_start_minus_d2WP");
	array.add(start_at_CFP_start);
	array.get(2).setId("start_at_CFP_start");
	array.add(end_at_latest_end);
	array.get(3).setId("end_at_latest_end");
	array.add(start_at_lowerbound);
	array.get(4).setId("start_at_lowerbound");
	array.add(end_at_upperbound);
	array.get(5).setId("end_at_upperbound");
		
	return array;
}*/

private float calculateDurationOfProcessWithoutSetup(Operation operation, int number_of_times_to_be_executed) {
	float duration_before = operation.getAvg_Duration();
	operation.setAvg_Duration(duration_before*number_of_times_to_be_executed);	//set duration to timePerPiece * quantity
	return duration_before*number_of_times_to_be_executed;
}

//defines also the operation start and end states
public double calculateDurationSetup(Interval free_interval, Operation operation) {
	
	String id_String_workpiece = operation.getAppliedOn().getID_String();
	String wp_type = id_String_workpiece.split("_")[0];
		//System.out.println("wp_type "+wp_type+" id_String_workpiece "+id_String_workpiece+" free_interval.lowerBound() "+free_interval.lowerBound()+ " "+((Setup_state)getRepresentedResource().getStartState()).getID_String());
	String wp_startstate_nextStep = ((Setup_state) this.getStateAtTime(free_interval.lowerBound())).getID_String();
		//System.out.println("wp_state_nextStep "+wp_startstate_nextStep);
	Setup_state endstate_of_new_operation = new Setup_state();
	endstate_of_new_operation.setID_String(wp_type);
	
	operation.setStartState((Setup_state) this.getStateAtTime(free_interval.lowerBound()));
	operation.setStartStateNeeded(endstate_of_new_operation);
	operation.setEndState(endstate_of_new_operation);
	
	if(wp_type.equals(wp_startstate_nextStep)) {
		return 0;
	}else {
		//System.out.println("duration setup "+this.getSetup_matrix().get(wp_startstate_nextStep+"_"+wp_type));
		if(this.getSetup_matrix().get(wp_startstate_nextStep+"_"+wp_type)!=null) {	
			return this.getSetup_matrix().get(wp_startstate_nextStep+"_"+wp_type);
		}else {
			return 0;
		}
			
	}
}

@Override
public float calculateTimeBetweenStates(State start_next_task_generic, State end_new_generic, int counter_free_interval_i) {
	Setup_state start_next_task = (Setup_state) start_next_task_generic;
	Setup_state end_new = (Setup_state) end_new_generic;
	
	String combination = end_new.getID_String()+"_"+start_next_task.getID_String();
	float duration_of_reaching_next_start_state_new = 0;
	if(start_next_task.getID_String().equals(end_new.getID_String())) {
		return 0;
	}else {
		if(this.getSetup_matrix().get(combination)!= null) {
			double d = this.getSetup_matrix().get(combination);
			duration_of_reaching_next_start_state_new = (float) d;
		}else {
			duration_of_reaching_next_start_state_new = 0;
		}
		
	}
	float duration_of_reaching_next_start_state_current = getDurationOfNextSetupStartingAt(getFree_interval_array().get(counter_free_interval_i).upperBound()); // in min		
	float difference = duration_of_reaching_next_start_state_new - duration_of_reaching_next_start_state_current;
	//System.out.println("DEBUG___"+logLinePrefix+" time_increment_or_decrement_to_be_added = "+difference+" __START NEXT TASK___location found: "+start_next_task.getCoordX()+";"+start_next_task.getCoordY()+" location end new "+end_new.getCoordX()+";"+end_new.getCoordY()+"  distance   = "+distance_TransportResource_fromResourceAtDestination_toStart_next_Job+" duration_of_reaching_next_target_new "+duration_of_reaching_next_target_new+" duration_of_reaching_next_target_current "+duration_of_reaching_next_target_current);
	
	return difference;
}

/*
private String getStateAtTime(long lowerBound) {

	String wp_state = "";
	Boolean allocatedWorkingStepWithGreaterTimeFound = false;
	if(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>0) {		
		@SuppressWarnings("unchecked")		
		Iterator<AllocatedWorkingStep> i = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();	  
	    while(i.hasNext()) {		//checks for every allWS in Workplan
	    	AllocatedWorkingStep a = i.next();	  
	    	if(Long.parseLong(a.getHasTimeslot().getStartDate()) > lowerBound) {
	    		wp_state = a.getHasOperation().getAppliedOn().getID_String().split("_")[0]; //gets WP Type
	    		allocatedWorkingStepWithGreaterTimeFound = true;
	    		break;
	    	}
	    }
	    if(!allocatedWorkingStepWithGreaterTimeFound) {	//the time is after the last allocated step
	    	//take the end state of the last allocated step
	    	wp_state = ((AllocatedWorkingStep) getWorkplan().getConsistsOfAllocatedWorkingSteps().get(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1)).getHasOperation().getAppliedOn().getID_String().split("_")[0];
	    }
	}else { //first step
		wp_state = "A"; //TODO: TBD must actually be initial state
	}
	
	return null;
}
*/
@Override
public ProductionResource getRepresentedResource() {
	return representedResource;
}

@Override
public void setRepresentedResource(Resource res) {
	representedResource = (ProductionResource) res;
}

@Override
protected void considerPickup(AllocatedWorkingStep allocatedWorkingStep) {
	if(allocatedWorkingStep.getHasOperation().getAvg_PickupTime()!=this.avg_pickUp) {
		System.out.println(logLinePrefix+" pick is different --> what routine??"); //TODO
	}
	/*if(!parallel_processing_pick_and_setup_possible) {
		Timeslot timeslot_new = allocatedWorkingStep.getHasTimeslot();
		//add the pickup time before the "real" startdate of the machine
		timeslot_new.setStartDate(Long.toString((Long.parseLong(timeslot_new.getStartDate())-allocatedWorkingStep.getHasOperation().getAvg_PickupTime()*1000*60)));
		allocatedWorkingStep.setHasTimeslot(timeslot_new);		
	}	*/	
}

@Override
protected Operation setStateAndOperation(ResultSet rs) {
	 Production_Operation op = new Production_Operation(); 
	Setup_state state = new Setup_state();
	try {
		if(rs.getBoolean(_Agent_Template.columnNameChangeOfState)) {       		
			state.setID_String(rs.getString(_Agent_Template.columnNameChangedState));        		
		}else {
			state.setID_String(this.startState);
		}
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	op.setStartStateNeeded(state);//TODO eventuell dynmaiisch bestimmen?
	op.setStartState(state);//TODO eventuell dynmaiisch bestimmen?
	op.setEndState(state);	
	return op;
}



} 

