package agentPro_Prototype_Agents;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Capability;
import agentPro.onto.DetailedOperationDescription;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Setup_state;
import agentPro.onto.State;
import agentPro.onto.Timeslot;
import agentPro.onto.TransportResource;
import agentPro.onto.Transport_Operation;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import support_classes.Interval;


/*
 * Models a transport resource. It has capabilites  
 * which it registers at the DF --> tbd if this makes sense
 * It Receives CFPs by the workpiece agents, answers with an offer and waits for an order on that offer 
 */
public class TransportResourceAgent extends ResourceAgent{

	private static final long serialVersionUID = 1L;

	private final int capacity = 10000;	//can be determined dynamically if needed
	
	private String dependency_crane_name_in_database = "Kran";
	public float buffer = 0;	//5 minutes 
	protected TransportResource representedResource = new TransportResource();
	private boolean consider_shared_resources = false;
	private Interval range = new Interval();
	private String enabled_operation = "";
	
	protected void setup (){
		super.setup();
		
		logLinePrefix = logLinePrefix+".TransportRessourceAgent.";
		
		
		//representedResource = new TransportResource();
			representedResource.setName(this.getLocalName());
			receiveValuesFromDB(representedResource);
		
		registerAtDF();
		
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
		//ReceiveCFPBehav = new ReceiveCFPBehaviour(this);
        //addBehaviour(ReceiveCFPBehav);
	}

	void registerAtDF() {
		
		
		//Object[] args = getArguments();
		//for(Object argument : args){
		//	serviced_routes.add(argument.toString());
		//}
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
			
		//for(String serviced_route : serviced_routes){
			 // Register the service in the yellow pages		
			ServiceDescription sd = new ServiceDescription();
			sd.setType("transport");
			sd.setName(this.getRepresentedResource().getHasCapability().getName());
			dfd.addServices(sd);		
		//}
		
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
	}

	@Override
	public boolean feasibilityCheckAndDetermineDurationParameters(Operation operation) {		
		
		boolean return_value = true;
		Transport_Operation transport_op = (Transport_Operation) operation;
		Location start = (Location) transport_op.getStartState();
		Location end = (Location) transport_op.getEndState();
		
		//System.out.println("DEBUG___"+this.getName()+" range "+range.toString()+" contains "+end.getCoordX()+" and contains "+ start.getCoordX());
		
		if(range.contains((long)start.getCoordX()) && range.contains((long)end.getCoordX())) {
			
		}else {
			return_value =  false;
		}
	
		return return_value;
	}
	
	public void receiveValuesFromDB(Resource r) {
		TransportResource tr = (TransportResource) r;
		 Statement stmt = null;
		 String query = "";
		 

			query = "select "+columnNameOfAvg_Transportation_Speed+" , "+columnNameOfResource_Detailed_Type+" , "+columnNameOfID+" , "+columnNameOfAvg_PickUp_Time+" , "+columnNameResourceName_simulation+" , "+columnNameOfLocationX+" , "+columnNameOfLocationY+" , "+columnNameOfCapability+" , "+columnNameOfResource_Type+" from "+tableNameResource+" where "+columnNameResourceName_simulation+" = '"+representedResource.getName()+"'"; 
		
		
		 try {
		        stmt = connection.createStatement();
		        ResultSet rs = stmt.executeQuery(query);
		      
		        while (rs.next()) {
		        	Location l = new Location(); 
				    l.setCoordX((float) rs.getDouble(columnNameOfLocationX));
				    l.setCoordY((float) rs.getDouble(columnNameOfLocationY));
				    tr.setHasLocation(l);
				    	Capability cap = new Capability();
				    	cap.setName(rs.getString(columnNameOfCapability));
				    tr.setHasCapability(cap);
				    tr.setType(rs.getString(columnNameOfResource_Type));
				    tr.setDetailed_Type(rs.getString(columnNameOfResource_Detailed_Type));
				    tr.setAvg_PickupTime(rs.getInt(columnNameOfAvg_PickUp_Time));
				    tr.setAvg_Speed((float) rs.getDouble(columnNameOfAvg_Transportation_Speed));
				    tr.setID_Number(rs.getInt(columnNameOfID));			   
		        }
		        
		        
		    } catch (SQLException e ) {
		    	e.printStackTrace();
		    } finally {
		        if (stmt != null) { try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} }
		    }
		    //determine detailed resource type --> needed for dependencies if a CFP is received
		   // System.out.println("DEBUG______________"+tr.getDetailed_Type());
		    if(tr.getDetailed_Type() != null && (tr.getDetailed_Type().equals("crane") || tr.getDetailed_Type().equals("buffer"))) {
		    	//check dependencies
				receiveDependencyValuesFromDB();
		    }
		    r = tr;
	    
	}
	
	public void receiveDependencyValuesFromDB() {	
			
	    Statement stmt = null;
	    String query = "";
	    String query2 = "";
	    
	    	query = "select "+columnNameOfCapability_Name+" from "+nameOfCapability_Operations_Mapping_Table+" where "+columnNameOfEnables_Operation+" = '"+dependency_crane_name_in_database+"'";
	    	query2 = "select "+columnNameOfEnables_Operation+" from "+nameOfCapability_Operations_Mapping_Table+" where "+columnNameOfCapability_Name+" = '"+representedResource.getHasCapability().getName()+"'";
	      
	    try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	     if(consider_shared_resources ) {
	    	 while (rs.next()) {
		        	String needed_capability = rs.getString(columnNameOfCapability_Name);
		        	getNeeded_shared_resources().add(needed_capability);
		        	//System.out.println("DEBUG_____needed resource added: "+needed_capability);
		        }
	     }
	     ResultSet rs2 = stmt.executeQuery(query2);
	     while (rs2.next()) {
	    	  enabled_operation = rs2.getString(columnNameOfEnables_Operation); 
	     }
	   
	        
	        
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    } finally {
	        if (stmt != null) { try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} }
	    }
	    
	  //determine the possible operations from the capability
	     //System.out.println("DEBUG______enabled_operation _"+enabled_operation);
        String [] split =enabled_operation.split("_");
        String [] coordinates = split[1].split(";");
        
        range = new Interval(coordinates[0], coordinates[1], false);

      //System.out.println("DEBUG______enabled_operation _"+enabled_operation+"  range "+range.toString());
        
		
	}
	
	
	@SuppressWarnings("null")
	@Override
	public Proposal checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp) {
		/*
		 * if its a transport order the startdate is the earliest startdate (end of "before last production step" or start last prod. step minus 2xtransportestimation 
		 * in case of transport the duration depends on the avg.speed, the distance between the transportation resource & the workpiece and between
		 * the workpiece and the produciton resource (destination) + the pickup time
		 */
		DetailedOperationDescription operation_description = new DetailedOperationDescription();
		
		//extract CFP Timeslot
		Timeslot cfp_timeslot = cfp.getHasTimeslot();	
		Operation operation = cfp.getHasOperation();
		int quantity = cfp.getQuantity();
		long startdate_cfp = Long.parseLong(cfp_timeslot.getStartDate());
		long enddate_cfp = Long.parseLong(cfp_timeslot.getEndDate());
		
		Proposal proposal = new Proposal();
		Timeslot timeslot_for_proposal = new Timeslot();
	
		int deadline_not_met = 0;		
		int number_of_tours_needed = (int) Math.ceil(quantity/capacity);		
		
		float duration_total_for_schedule = 0;
		float duration_for_answering_CFP_so_for_Workpiece_schedule = 0;
		float duration_to_get_to_workpiece = 0;
	
		float time_increment_or_decrement_to_be_added_for_setup_of_next_task = 0;
	

		Transport_Operation transport_op_to_destination = (Transport_Operation) operation;
		long buffer_time_that_production_can_start_earlier = (long) transport_op_to_destination.getBuffer_before_operation();
				
		boolean slot_found = false;
		String printout = "";
		long buffer_after_operation = 0;
		long buffer_before_operation = 0;
		long earliest_finish_date_from_arrive_at_resource = enddate_cfp;

		 ArrayList<Interval> listOfIntervals = new ArrayList<Interval>();
		
		 //independent parameters
		 float duration_eff = calculateDurationOfProcessWithoutSetup(operation, number_of_tours_needed);
		 
	for(int i = 0;i<getFree_interval_array().size();i++) {	
		
			operation_description.clearAllHasRequest_Points();	
			
			//dependent parameters
						//start_old_idle = (Location) this.getStateAtTime(getFree_interval_array().get(i).lowerBound());					
						//float distance_TransportResource_to_Workpiece = calcDistance(start_old_idle, start_new);			
						//duration_to_get_to_workpiece = (distance_TransportResource_to_Workpiece/avg_speed) / 60 ;	// in min
			duration_to_get_to_workpiece = calculateDurationSetup(getFree_interval_array().get(i), operation);
			//time_increment_or_decrement_to_be_added_for_setup_of_next_task = this.calculateTimeIncrement(transport_op_to_destination, avg_speed, i, operation_description); // in min
				time_increment_or_decrement_to_be_added_for_setup_of_next_task = this.calculateTimeIncrement(transport_op_to_destination, i, operation_description); // in min
				
			
				this.getReceiveCFPBehav().time_increment_or_decrement_to_be_added_for_setup_of_next_task = time_increment_or_decrement_to_be_added_for_setup_of_next_task;
				
			duration_total_for_schedule = duration_to_get_to_workpiece + duration_eff + buffer + time_increment_or_decrement_to_be_added_for_setup_of_next_task;	// min
			duration_for_answering_CFP_so_for_Workpiece_schedule 	=  	 duration_eff + buffer;

			
			//calculate possible slots within this free interval (FI)
			listOfIntervals = calculateIntervals(startdate_cfp, enddate_cfp, (long)(duration_to_get_to_workpiece*60*1000), (long)(duration_eff*60*1000), (long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000), buffer_time_that_production_can_start_earlier, i);
			System.out.println("DEBUG__________interval "+listOfIntervals.get(0).toString());
			//check which slots are possible from (production) ressource side --> dont take the WP too early and dont arrive too early
			 checkFeasibility(listOfIntervals, startdate_cfp, enddate_cfp, buffer_time_that_production_can_start_earlier);
			 System.out.println("DEBUG__________interval "+listOfIntervals.get(0).toString());
			 //checks the schedule --> LB and UB violated?
			 checkSchedule(listOfIntervals, (long)(duration_to_get_to_workpiece*60*1000), (long)(time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000), i);
			 
			 if(listOfIntervals.size()>0) {
				 slot_found = true;
				 //sort earliest end first
				 this.sortArrayListIntervalsEarliestFirst(listOfIntervals, "end");
				 //TBD Buffer
				 break;
			 }
	 
	}
	
	if(!slot_found) {
		if(startdate_cfp+(long)(duration_for_answering_CFP_so_for_Workpiece_schedule*60*1000)>=earliest_finish_date_from_arrive_at_resource) {
			System.out.println("DEBUG_______WRONG DATA FROM CFP --> startdate_cfp+(long)(duration_for_answering_CFP_so_for_Workpiece_schedule*60*1000)  "+startdate_cfp+(long)(duration_for_answering_CFP_so_for_Workpiece_schedule*60*1000)+" >= "+earliest_finish_date_from_arrive_at_resource+" earliest_finish_date_from_arrive_at_resource    is violated");
		}
		System.out.println("DEBUG----------"+"NO FREE SLOT FOUND ---> this should not happen   printout   	"+printout);
		proposal = null;
	}else {
		
		/*
		if(consider_shared_resources) {
			addPointToList(operation_description, start_old_idle.getCoordX(), estimated_start_date, "Start:set_up");
			addPointToList(operation_description, start_new.getCoordX(), estimated_start_date+(long)(duration_to_get_to_workpiece*60*1000), "Start:pick_up");
			addPointToList(operation_description, start_new.getCoordX(), estimated_start_date+(long)(duration_to_get_to_workpiece*60*1000+avg_Pickup_time*60*1000), "Start:travel");
			addPointToList(operation_description, transport_op_to_destination.getHasEndLocation().getCoordX(), estimated_start_date+(long)((duration_to_get_to_workpiece+avg_Pickup_time+traveling_time+buffer)*60*1000), "Start:pick_up2");
			addPointToList(operation_description, transport_op_to_destination.getHasEndLocation().getCoordX(), estimated_start_date+(long)((duration_to_get_to_workpiece+2*avg_Pickup_time+traveling_time+buffer)*60*1000), "Start:travel2");			
			addPointToList(operation_description, transport_op_to_destination.getHasEndLocation().getCoordX(), estimated_start_date+(long)((duration_to_get_to_workpiece+2*avg_Pickup_time+traveling_time+time_increment_or_decrement_to_be_added_for_setup_of_next_task+buffer))*60*1000, "Start:idle");			
			
			String printout_2 = "DEBUG__________________points: ";
			int i = 1;
			@SuppressWarnings("unchecked")		
			Iterator<Request_Point> it = operation_description.getHasRequest_Points().iterator();	  
		    while(it.hasNext()) {
		    	Request_Point point = it.next();
		    	printout_2 = printout_2 + " point number "+i+" "+point.getCoordX()+" "+SimpleDateFormat.format(Long.parseLong(point.getTime()))+" "+point.getType();
		    	i++;
		    }
		    //System.out.println(printout_2);
		    transport_op_to_destination.setHasDetailedOperationDescription(operation_description);
		}*/
		
		
	//the duration of the (offered) operation must be reduced by the time that the transport resource needs to get to the workpiece / the set up time of the machine
	transport_op_to_destination.setAvg_Duration(duration_for_answering_CFP_so_for_Workpiece_schedule);
		//System.out.println("DEBUG___________duration_for_answering_CFP_so_for_Workpiece_schedule "+duration_for_answering_CFP_so_for_Workpiece_schedule+" start minus end = "+(estimated_enddate-estimated_start_date)/(1000*60));
		transport_op_to_destination.setSet_up_time(duration_to_get_to_workpiece);
		transport_op_to_destination.setAvg_PickupTime(this.getRepresentedResource().getAvg_PickupTime());
		transport_op_to_destination.setBuffer_after_operation(buffer_after_operation);
		transport_op_to_destination.setBuffer_before_operation(buffer_before_operation);
		
		timeslot_for_proposal.setEndDate(String.valueOf(listOfIntervals.get(0).upperBound()));
		timeslot_for_proposal.setStartDate(String.valueOf(listOfIntervals.get(0).lowerBound()));
		timeslot_for_proposal.setLength(listOfIntervals.get(0).upperBound()-listOfIntervals.get(0).lowerBound());
		
		//TBD LOgic for Book into schedule!!!!!
		
		this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(String.valueOf(listOfIntervals.get(0).upperBound())); //time increment is reduced / put to the other busy interval later
		this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(String.valueOf(listOfIntervals.get(0).lowerBound()-(long) duration_to_get_to_workpiece*60*1000));	
		this.getReceiveCFPBehav().timeslot_for_schedule.setLength(listOfIntervals.get(0).upperBound()-(listOfIntervals.get(0).lowerBound()-(long) duration_to_get_to_workpiece*60*1000));
		
	//timeslot_for_proposal.setEndDate(String.valueOf(estimated_enddate- (long) (time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000)));	
	//timeslot_for_proposal.setStartDate(Long.toString(estimated_start_date+(long)(duration_to_get_to_workpiece*60*1000)));
		
	this.getReceiveCFPBehav().timeslot_for_proposal = timeslot_for_proposal;
	//this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(Long.toString(estimated_enddate)); //time increment is reduced / put to the other busy interval later
	//this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(Long.toString(estimated_start_date));		
	System.out.println("DEBUG_____"+logLinePrefix+"  transport resource agent ______estimated_end_date "+String.valueOf(listOfIntervals.get(0).upperBound())+" estimated_startdate "+(listOfIntervals.get(0).lowerBound()-(long) duration_to_get_to_workpiece)+" time_increment_or_decrement_to_be_added_for_setup_of_next_task "+time_increment_or_decrement_to_be_added_for_setup_of_next_task+" duration_total_for_schedule = "+duration_total_for_schedule+" duration_to_get_to_workpiece = "+duration_to_get_to_workpiece);
	float price = duration_total_for_schedule + deadline_not_met;		//strafkosten, wenn deadline_not_met

	proposal = createProposal(price, transport_op_to_destination, timeslot_for_proposal, cfp.getHasSender());

	}

		return proposal;
	}
	
	private float calculateDurationOfProcessWithoutSetup(Operation operation, int number_of_times_to_be_executed) {
		float distance_Workpiece_to_ProductionResource = calcDistance((Location)operation.getStartState(), (Location)operation.getEndState());	//in m
		 float traveling_time  = (distance_Workpiece_to_ProductionResource/this.getRepresentedResource().getAvg_Speed())/60; // in min
		System.out.println("DEBUG__TR____number_of_times_to_be_executed "+number_of_times_to_be_executed+" calculated (1+(number_of_times_to_be_executed-1)*2) "+(1+(number_of_times_to_be_executed-1)*2));
		 float duration_eff = traveling_time*(1+(number_of_times_to_be_executed-1)*2) + 2*this.getRepresentedResource().getAvg_PickupTime()*number_of_times_to_be_executed;
		 
		return duration_eff;
	}

	private float calculateDurationSetup(Interval interval, Operation operation) {
		Location start_old_idle = (Location) this.getStateAtTime(interval.lowerBound());	
		Location start_new = (Location) operation.getStartState();
		float distance_TransportResource_to_Workpiece = calcDistance(start_old_idle, start_new);			
		float duration_to_get_to_workpiece = (distance_TransportResource_to_Workpiece/this.getRepresentedResource().getAvg_Speed()) / 60 ;	// in min
		return duration_to_get_to_workpiece;
	}

	//checks whether the start >= LB and end <= UB
	public void checkSchedule(ArrayList<Interval> listOfIntervals_possibleFromResourceSide, long duration_to_get_to_workpiece, long time_increment_or_decrement_to_be_added_for_setup_of_next_task, int i2) {
		int counter = 1; //no of element
		 Iterator<Interval> it = listOfIntervals_possibleFromResourceSide.iterator();		 	
		    while(it.hasNext()) {
		    	Interval i = it.next();
		    	//check Free Interval parameters
		    	if(i.lowerBound()-duration_to_get_to_workpiece< getFree_interval_array().get(i2).lowerBound() && i.upperBound()<=getFree_interval_array().get(i2).upperBound()-time_increment_or_decrement_to_be_added_for_setup_of_next_task) {
		    		System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because LB_Ress-d2WP "+(i.lowerBound()-duration_to_get_to_workpiece)+" < "+getFree_interval_array().get(i2).lowerBound()+" lower_bound_Transporter --> start too early FOR TRANSPORTER");
		    		it.remove();
		 		 }else if(i.lowerBound()-duration_to_get_to_workpiece>= getFree_interval_array().get(i2).lowerBound() && i.upperBound() > getFree_interval_array().get(i2).upperBound()-time_increment_or_decrement_to_be_added_for_setup_of_next_task){
		 			System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because UB_Ress "+i.upperBound()+" > "+(getFree_interval_array().get(i2).upperBound()-time_increment_or_decrement_to_be_added_for_setup_of_next_task)+" upper_bound_transporter --> Finish too late for TRANSPORTER");
		    		it.remove();
		 		 }else if(i.lowerBound()-duration_to_get_to_workpiece< getFree_interval_array().get(i2).lowerBound() && i.upperBound() > getFree_interval_array().get(i2).upperBound()-time_increment_or_decrement_to_be_added_for_setup_of_next_task){
		 			System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because UB "+i.upperBound()+" > "+(getFree_interval_array().get(i2).upperBound()-time_increment_or_decrement_to_be_added_for_setup_of_next_task)+" upper_bound_transporter  --> Finish too late for TRANSPORTER AND because LB_Ress "+(i.lowerBound()-duration_to_get_to_workpiece)+" < "+getFree_interval_array().get(i2).lowerBound()+" lower_bound_Transporter --> start too early FOR TRANSPORTER");
		    		it.remove();
		 		 }else {
		 			 //fine
		 		 }
		    	counter++;
		    }
	}
	
	//checks if for this free interval the calculated intervals are feasible from resource side
	private void checkFeasibility(ArrayList<Interval> listOfIntervals, long startdate_cfp, long enddate_cfp, long buffer_time_that_production_can_start_earlier) {
		//int counter = 1; //no of element
		 Iterator<Interval> it = listOfIntervals.iterator();		 	
		    while(it.hasNext()) {
		    	Interval i = it.next();
		    	//System.out.println(counter+" "+	i.getId()+" "+i.getSize());
				//start >= earliest start, end >= earliest end
		    	if(i.lowerBound()< startdate_cfp && i.upperBound()>=enddate_cfp-buffer_time_that_production_can_start_earlier) {
		    		//System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because LB "+i.lowerBound()+" < "+startdate_cfp+" start_cfp --> start too early");
		    		it.remove();
		 		 }else if(i.lowerBound()>= startdate_cfp && i.upperBound()< enddate_cfp-buffer_time_that_production_can_start_earlier){
		 			//System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because UB "+i.upperBound()+" < "+(enddate_cfp-buffer_time_that_production_can_start_earlier)+" effr --> Finish too early");
		    		it.remove();
		 		 }else if(i.lowerBound()< startdate_cfp && i.upperBound()< enddate_cfp-buffer_time_that_production_can_start_earlier){
		 			//System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because UB "+i.upperBound()+" < "+(enddate_cfp-buffer_time_that_production_can_start_earlier)+" effr --> Finish too early AND because LB "+i.lowerBound()+" < "+startdate_cfp+" start_cfp --> start too early");
		    		it.remove();
		 		 }else {
		 			 //fine
		 		 }
		    	//counter++;
		    }
	}
	//refers to the actual process (without setup)
	/*
	public ArrayList<Interval> calculateIntervals(long startdate_cfp, long enddate_cfp, long duration_to_get_to_workpiece, long duration_eff, long time_increment_or_decrement_to_be_added_for_setup_of_next_task, long buffer_time_that_production_can_start_earlier, int i) {	//for feasibility checking the arrival dates AT THE RESSOURCES are important
		ArrayList<Interval> array = new ArrayList<>();
		long effr = enddate_cfp - buffer_time_that_production_can_start_earlier;
		Interval end_at_effr = new Interval((long) (effr-duration_eff), effr, false);
		Interval start_at_CFP_start_minus_d2WP = new Interval(startdate_cfp, (long) (startdate_cfp+duration_eff), false);
		Interval start_at_CFP_start = new Interval ((long)(startdate_cfp+duration_to_get_to_workpiece), (long) (startdate_cfp+duration_to_get_to_workpiece+duration_eff), false); // should not be needed!
		Interval end_at_latest_end = new Interval((long) (enddate_cfp-duration_eff), enddate_cfp);
		Interval start_at_lowerbound = new Interval((long) (getFree_interval_array().get(i).lowerBound()+duration_to_get_to_workpiece), (long) (getFree_interval_array().get(i).lowerBound()+duration_to_get_to_workpiece+duration_eff), false);
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
	/*
	private void addPointToList(DetailedOperationDescription operation_description, float coordX, long estimated_start_date, String task_description) {
		Request_Point req_point = new Request_Point();
		req_point.setCoordX(coordX);
		req_point.setTime(String.valueOf(estimated_start_date));
		req_point.setType(task_description);
		operation_description.addHasRequest_Points(req_point);		
	}
*/
	/*
	private float calculateTimeIncrement(Transport_Operation transport_op_to_destination, float avg_speed, int i, DetailedOperationDescription operation_description) {
		float time_increment_or_decrement_to_be_added = 0;
		
		//check if there is a task that starts at the end of the free interval
		if(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>0) {		
			@SuppressWarnings("unchecked")		
			Iterator<AllocatedWorkingStep> it = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();	  
		    while(it.hasNext()) {		//checks for every allWS in Workplan
		    	AllocatedWorkingStep a = it.next();	  
		    	if(Long.parseLong(a.getHasTimeslot().getStartDate()) == getFree_interval_array().get(i).upperBound()) {
		    		//if yes
		    		Location start_next_task = (Location) ((Transport_Operation)a.getHasOperation()).getStartState();
		    		
		    		Location end_new = (Location) transport_op_to_destination.getEndState();
		    		float distance_TransportResource_fromResourceAtDestination_toStart_next_Job = calcDistance(start_next_task, end_new);
		    		
		    		float duration_of_reaching_next_target_new = (distance_TransportResource_fromResourceAtDestination_toStart_next_Job/avg_speed)/60;
		    		float duration_of_reaching_next_target_current = getDurationOfNextSetupStartingAt(getFree_interval_array().get(i).upperBound()); // in min
		    		
		    		time_increment_or_decrement_to_be_added = duration_of_reaching_next_target_new-duration_of_reaching_next_target_current;  
		    		System.out.println("DEBUG___"+logLinePrefix+" time_increment_or_decrement_to_be_added = "+time_increment_or_decrement_to_be_added+" __START NEXT TASK___location found: "+start_next_task.getCoordX()+";"+start_next_task.getCoordY()+" location end new "+end_new.getCoordX()+";"+end_new.getCoordY()+"  distance   = "+distance_TransportResource_fromResourceAtDestination_toStart_next_Job+" duration_of_reaching_next_target_new "+duration_of_reaching_next_target_new+" duration_of_reaching_next_target_current "+duration_of_reaching_next_target_current);
		    		
		    		if(time_increment_or_decrement_to_be_added != 0) {
		    			//addPointToList(operation_description, end_new.getCoordX(), getFree_interval_array().get(i).upperBound()-(long)(time_increment_or_decrement_to_be_added*60*1000), "Start:set_up");		    			    					    		
		    		}
		    		break;
		    	}
		    }
		  
		}else { //first step
			//return 0;
		}			
		return time_increment_or_decrement_to_be_added;
	}
*/
	@Override
	public float calculateTimeBetweenStates(State start_next_task, State end_new, int busy_interval_i) {
		
		float distance_TransportResource_fromResourceAtDestination_toStart_next_Job = calcDistance((Location)start_next_task, (Location)end_new);
		
		float duration_of_reaching_next_target_new = (distance_TransportResource_fromResourceAtDestination_toStart_next_Job/representedResource.getAvg_Speed())/60;
		float duration_of_reaching_next_target_current = getDurationOfNextSetupStartingAt(getFree_interval_array().get(busy_interval_i).upperBound()); // in min		
		float difference = duration_of_reaching_next_target_new-duration_of_reaching_next_target_current;
		//System.out.println("DEBUG___"+logLinePrefix+" time_increment_or_decrement_to_be_added = "+difference+" __START NEXT TASK___location found: "+start_next_task.getCoordX()+";"+start_next_task.getCoordY()+" location end new "+end_new.getCoordX()+";"+end_new.getCoordY()+"  distance   = "+distance_TransportResource_fromResourceAtDestination_toStart_next_Job+" duration_of_reaching_next_target_new "+duration_of_reaching_next_target_new+" duration_of_reaching_next_target_current "+duration_of_reaching_next_target_current);
		
		return difference;
	}

	public void setStartState() {	
		this.getRepresentedResource().setStartState(this.getRepresentedResource().getHasLocation());
	}

	/*
	public Location getLocationAtTime(long lowerBound) {
		Location location = new Location();
		Boolean allocatedWorkingStepWithGreaterTimeFound = false;
		if(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>0) {		
			@SuppressWarnings("unchecked")		
			Iterator<AllocatedWorkingStep> i = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();	  
		    while(i.hasNext()) {		//checks for every allWS in Workplan
		    	AllocatedWorkingStep a = i.next();	  
		    	if(Long.parseLong(a.getHasTimeslot().getStartDate()) > lowerBound) {
		    		location = (Location) ((Transport_Operation) a.getHasOperation()).getStartState();
		    		allocatedWorkingStepWithGreaterTimeFound = true;
		    		break;
		    	}
		    }
		    if(!allocatedWorkingStepWithGreaterTimeFound) {	//the time is after the last allocated step
		    	//take the end location of the last allocated step
		    	Transport_Operation trans_op = (Transport_Operation) ((AllocatedWorkingStep) getWorkplan().getConsistsOfAllocatedWorkingSteps().get(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1)).getHasOperation();
		    	location = (Location) trans_op.getEndState();
		    }
		}else { //first step
			location = getRepresentedResource().getHasLocation(); //TODO: TBD must actually be initial location
		}
		return location;
	}
	*/
	public float calcDistance(Location location_1, Location location_2) {
		float x1 = location_1.getCoordX();
		float y1 = location_1.getCoordY();
		
		float x2 = location_2.getCoordX();
		float y2 = location_2.getCoordY();
		
		float distance = 0;
	    distance = (float) Math.hypot(x2 - x1, y2 - y1);
	    distance = Math.round(distance);
	    return distance;
	}

	@Override
	public TransportResource getRepresentedResource() {
		return representedResource;
	}

	@Override
	public void setRepresentedResource(Resource res) {
		representedResource = (TransportResource) res;
		
	}


} 

