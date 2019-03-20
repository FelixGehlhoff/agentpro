package agentPro_Prototype_Agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JTable;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Capability;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.ProductionResource;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Timeslot;
import agentPro.onto.TransportResource;
import agentPro.onto.Transport_Operation;
import agentPro_Prototype_ResourceAgent.ReceiveInformWorkpieceDepartureBehaviour;
import agentPro_Prototype_ResourceAgent.ReceiveIntervalForConnectedResourceBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import support_classes.Interval;
import support_classes.Resource_Extension;

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
	protected ProductionResource representedResource;
	private int avg_pickUp = 5; 
	private int avg_setUp = 0;
	private String columnNameOfEnablesWPType = "enables_wp_type";
	private ArrayList<String> enabledWorkpieces = new ArrayList<>();
	private HashMap<String, Double> setup_matrix = new HashMap();
	
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
		
		receiveCapabilityOperationsValuesFromDB(representedResource);
		
		//logLinePrefix = "ProductionRessourceAgent."+production_capability;
		registerAtDF();
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
		ReceiveInformWorkpieceDepartureBehav = new ReceiveInformWorkpieceDepartureBehaviour(this);
        addBehaviour(ReceiveInformWorkpieceDepartureBehav);
        if(simulation_mode) {
        	 if(getLocalName().equals("Skoda_1_1") || getLocalName().equals("Skoda_2_1") || getLocalName().equals("Skoda_3_1")) {
        		 addBehaviour(new ReceiveIntervalForConnectedResourceBehaviour(this));
			    }
        }
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
		sd.setType("production");
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
	    			System.out.println("DEBUG_____possible_op.getName() "+possible_op.getName()+possible_op.getAvg_Duration()+"___Duration set "+possible_op.getAvg_Duration());	
	    		}
	    		
	    		return true;
	    	}
		
	    }	

		
		return false;
	}
	


public void receiveValuesFromDB(Resource r) {
		
	    Statement stmt = null;
	    String query = "";

	    	query = "select "+columnNameResourceName_simulation+" , "+columnNameOfSetupTime+" , "+columnNameOfID+" , "+columnNameOfLocationX+" , "+columnNameOfLocationY+" , "+columnNameOfCapability+" , "+columnNameOfResource_Type+" from "+tableNameResource+" where "+columnNameResourceName_simulation+" = '"+representedResource.getName()+"'"; 
	   
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
			    r.setID_Number(rs.getInt(columnNameOfID));
			    this.avg_setUp = (int) rs.getDouble(columnNameOfSetupTime);
	        }
	       
	        
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    }
	    
	    //create setup matrix
	    String query2 = "";

    	query2 = "select "+columnNameOfChangeover+" , `"+this.getRepresentedResource().getName()+"` from "+tableNameResourceSetupMatrix; 
   
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
	long estimated_enddate = 0;
	long set_up_time = avg_setUp; //can be time to reach to workpiece or set up time at a machine
	float duration_for_price = 0;
	int deadline_not_met = 0;
	
	
	//extract CFP Timeslot
			Timeslot cfp_timeslot = cfp.getHasTimeslot();	
			Operation operation = cfp.getHasOperation();
			long startdate_cfp = Long.parseLong(cfp_timeslot.getStartDate());
			long enddate_cfp = Long.parseLong(cfp_timeslot.getEndDate());
			float duration_before = operation.getAvg_Duration();
			operation.setAvg_Duration(duration_before*cfp.getQuantity());	//set duration to timePerPiece * quantity
			
	long enddate_interval = 0;
	//operation avg duration = 0 in case of buffer place
	if(operation.getAvg_Duration() == 0) {
		enddate_interval = enddate_cfp;
	}else {
		enddate_interval =  startdate_cfp+(long) (operation.getAvg_Duration()*60*1000);

		
		duration_for_price = set_up_time + operation.getAvg_Duration();
	//this.getReceiveCFPBehav().duration_for_price = set_up_time + operation.getAvg_Duration(); //in min
	}

Interval timeslot_interval_to_be_booked_production = new Interval( startdate_cfp-(Math.max(set_up_time, avg_pickUp))*60*1000, enddate_interval+avg_pickUp*60*1000, false);	//18.06. pick-up added
//System.out.println("DEBUG_______productionResourceAgent  timeslot_interval_to_be_booked_production "+timeslot_interval_to_be_booked_production.getSize()/(60*1000));
for(int i = 0;i<getFree_interval_array().size();i++) {	
	//dependent parameters
	double duration_setup = calculateDurationSetup(getFree_interval_array().get(i), operation.getAppliedOn().getID_String());	// in min
	//time_increment_or_decrement_to_be_added_for_setup_of_next_task = calculateTimeIncrement(transport_op_to_destination, avg_speed, i, operation_description); // in min
	
	
	if(getFree_interval_array().get(i).contains(timeslot_interval_to_be_booked_production)){
		//desired slot can be fulfilled
		estimated_start_date = startdate_cfp;
		estimated_enddate = enddate_interval;	
		//determine how much time there is between this operation and the one before --> the workpiece can only arrive if the one before is already gone
		//	put that time in the allocated working step as Buffer
		this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier = timeslot_interval_to_be_booked_production.lowerBound()-getFree_interval_array().get(i).lowerBound(); 
		//25.02.2019 Buffer after
		this.getReceiveCFPBehav().buffer_time_that_production_can_start_later = getFree_interval_array().get(i).upperBound()-timeslot_interval_to_be_booked_production.upperBound();
		
		//System.out.println("DEBUG____CONTAINS   operation.getAvg_Duration()*60*1000 "+operation.getAvg_Duration()*60*1000+" estimated_start_date "+estimated_start_date+"  estimated_enddate  "+estimated_enddate+"     buffer_time_that_production_can_start_earlier    	"+this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier/60000);
		break;
	
	}else if(getFree_interval_array().get(i).getSize() >= timeslot_interval_to_be_booked_production.getSize()){
		//the desired slot is not fully in a free interval
		//check whether the slot can be postponed to a later interval (which has the correct size) but not the earliest start date
		//for(int j = 0;j<getFree_interval_array().size();j++) {
			//if(getFree_interval_array().get(j).getSize() >= timeslot_interval_to_be_booked_production.getSize()) {
			if(getFree_interval_array().get(i).lowerBound() >= timeslot_interval_to_be_booked_production.lowerBound()) {	
				
				estimated_start_date = getFree_interval_array().get(i).lowerBound()+avg_pickUp*60*1000;
				estimated_enddate =  estimated_start_date+(long) (operation.getAvg_Duration()*60*1000+set_up_time*60*1000);	//start at the lower bound with the set up + duration = enddate
				timeslot_interval_to_be_booked_production = new Interval(estimated_start_date - avg_pickUp*60*1000, estimated_enddate + avg_pickUp*60*1000);
				
				this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier = 0; //because the lowerBound of a free Interval is taken --> before that is a busy interval
				this.getReceiveCFPBehav().buffer_time_that_production_can_start_later = getFree_interval_array().get(i).upperBound()-timeslot_interval_to_be_booked_production.upperBound();
				
				//System.out.println("DEBUG_______________i "+i+" getFree_interval_array().get(i).lowerBound()"+getFree_interval_array().get(i).lowerBound());
				deadline_not_met = 1000;
				break;
			}
			
		//}				
	}
	
}	

	float price = duration_for_price + deadline_not_met;		//strafkosten, wenn deadline_not_met
	if(simulation_mode && (this.getRepresentedResource().getName().contains("Skoda_2") || this.getRepresentedResource().getName().contains("Skoda_3")) ) {		//skoda 1 is better
		price = price + 50;
	}else if(simulation_mode && this.getRepresentedResource().getName().contains("Konfektion") || this.getRepresentedResource().getName().contains("FAD")) {
		if(this.getWorkplan().getConsistsOfAllocatedWorkingSteps().size() > 0) {
			AllocatedWorkingStep lastAllWS = (AllocatedWorkingStep) this.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(this.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1);
			long difference = estimated_start_date - Long.parseLong(lastAllWS.getHasTimeslot().getEndDate());
			double difference_double = difference/(1000*60);
			price = price + this.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()*10000-(float)difference_double;	//biggest difference should get order
		}	
	}
	timeslot_for_proposal.setEndDate(Long.toString(estimated_enddate));
	timeslot_for_proposal.setStartDate(Long.toString(estimated_start_date));	
	timeslot_for_proposal.setLength(estimated_enddate-estimated_start_date);
	this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(Long.toString(timeslot_interval_to_be_booked_production.upperBound()));
	this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(Long.toString(timeslot_interval_to_be_booked_production.lowerBound()));
	this.getReceiveCFPBehav().timeslot_for_schedule.setLength(timeslot_interval_to_be_booked_production.getSize());
	
	
	proposal = createProposal(price, operation, timeslot_for_proposal, cfp.getHasSender());
	//System.out.println("DEBUG____"+getLocalName()+" getFree_interval_array().size() = "+getFree_interval_array().size());
	if(timeslot_for_proposal.getLength() == 0) {
	proposal = null;	
	}
	System.out.println("DEBUG_____operation.getAvg_Duration()*quantity "+operation.getAvg_Duration()+"______"+this.getLocalName()+" timeslot "+timeslot_for_proposal.getStartDate()+" "+timeslot_for_proposal.getEndDate()+" "+timeslot_for_proposal.getLength());
	return proposal;
}

private double calculateDurationSetup(Interval free_interval, String id_String_workpiece) {
	String wp_type = id_String_workpiece.split("_")[0];
	String wp_state_nextStep = getStateAtTime(free_interval.lowerBound());
	
	//this.getSetup_matrix().get(key);
	
	return 0;
}

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

@Override
public ProductionResource getRepresentedResource() {
	return representedResource;
}

@Override
public void setRepresentedResource(Resource res) {
	representedResource = (ProductionResource) res;
}



} 

