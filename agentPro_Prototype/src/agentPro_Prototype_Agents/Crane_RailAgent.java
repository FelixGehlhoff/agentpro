package agentPro_Prototype_Agents;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Capability;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Shared_Resource;
import agentPro.onto.State;
import agentPro.onto.Timeslot;
import agentPro.onto.Transport_Operation;
import agentPro_Prototype_ResourceAgent.ReceiveCFPBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import support_classes.Interval;
import support_classes.Storage_element_slot;

/*
 * Models a shared resource. 
 */
public class Crane_RailAgent extends SharedResourceAgent{

	private static final long serialVersionUID = 1L;
	//private ReceiveCFPBehaviour ReceiveCFPBehav;
	private Interval range;

	protected void setup (){
		super.setup();
		//this.numberOfResourcesPossibleForCalculationOfSharedResourceProposal = 2; //TBD dynamisch
		logLinePrefix = 	logLinePrefix+"Crane_RailAgent";
		
		
	}


	@Override
	public boolean feasibilityCheckAndDetermineDurationParameters(Operation operation) {
		boolean return_value = true;
		Transport_Operation transport_op = (Transport_Operation) operation;
		Location start = (Location) transport_op.getStartStateNeeded();
		Location end = (Location) transport_op.getEndState();
		
		//System.out.println("DEBUG___"+this.getName()+" range "+range.toString()+" contains "+end.getCoordX()+" and contains "+ start.getCoordX());
		
		if(range.contains((long)start.getCoordX()) && range.contains((long)end.getCoordX())) {
			
		}else {
			return_value =  false;
		}
	
		return return_value;
	}

	@Override
	public ArrayList<Proposal> checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp) {
		
		ArrayList<Proposal> proposal_list = new ArrayList<Proposal>();
		//Timeslot timeslot_for_proposal = new Timeslot();
		
		//extract CFP Timeslot
		Timeslot cfp_timeslot = cfp.getHasTimeslot();	
		long startdate_cfp = Long.parseLong(cfp_timeslot.getStartDate());
		long enddate_cfp = Long.parseLong(cfp_timeslot.getEndDate());
		Transport_Operation operation = (Transport_Operation) cfp.getHasOperation();
		
		//TBD real procedure
		operation.setBuffer_after_operation_end(3*60*60*1000);
		operation.setBuffer_after_operation_start(3*60*60*1000);
		operation.setBuffer_before_operation_start(3*60*60*1000);
		
		/*
		long estimated_start_date = 0;
		long estimated_enddate = 0;
		long set_up_time = 0; //no set up time needed at this moment

		
		long enddate_interval = 0;
			//operation avg duration = 0 in case of buffer place
		if(operation.getAvg_Duration() == 0) {
			enddate_interval = enddate_cfp;
		}else {		//shared resource should get an operation with a duration from e.g. a transport resource
			enddate_interval = (long) (startdate_cfp+operation.getAvg_Duration()*60*1000);
			this.getReceiveCFPBehav().duration_for_price = set_up_time + operation.getAvg_Duration(); //in min
			//duration_for_price = set_up_time + operation.getAvg_Duration(); //in min
		}
		
		Interval timeslot_interval_to_be_booked_shared_resource = new Interval( startdate_cfp-set_up_time*60*1000, enddate_cfp, false);	
		
		for(int i = 0;i<getFree_interval_array().size();i++) {	
			if(getFree_interval_array().get(i).contains(timeslot_interval_to_be_booked_shared_resource)){
				//desired slot can be fulfilled
				estimated_start_date = startdate_cfp-set_up_time*60*1000;
				estimated_enddate = enddate_interval;	
				//determine how much time there is between this operation and the one before --> the workpiece can only arrive if the one before is already gone
				//	put that time in the allocated working step as Buffer
				this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier = estimated_start_date-getFree_interval_array().get(i).lowerBound(); 
				break;
			
			}else if(getFree_interval_array().get(i).getSize() >= timeslot_interval_to_be_booked_shared_resource.getSize()){
				//the desired slot is not fully in a free interval
				//check whether the slot can be postponed to a later interval (which has the correct size) but not the earliest start date
				//for(int j = 0;j<getFree_interval_array().size();j++) {
					//if(getFree_interval_array().get(j).getSize() >= timeslot_interval_to_be_booked_production.getSize()) {
					if(getFree_interval_array().get(i).lowerBound() >= timeslot_interval_to_be_booked_shared_resource.lowerBound()) {	
						estimated_start_date = getFree_interval_array().get(i).lowerBound();
						estimated_enddate = (long) (estimated_start_date+operation.getAvg_Duration()*60*1000+set_up_time*60*1000);	//start at the lower bound with the set up + duration = enddate
						this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier = 0; //because the lowerBound of a free Interval is taken --> before that is a busy interval
						break;
					}
					
				//}				
			}
			
		}	
		
		timeslot_for_proposal.setEndDate(Long.toString(estimated_enddate));
		timeslot_for_proposal.setStartDate(Long.toString(estimated_start_date+set_up_time*60*1000));	
		this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(Long.toString(estimated_enddate));
		this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(Long.toString(estimated_start_date));
		*/
		float price = operation.getAvg_Duration();	
		//this.getReceiveCFPBehav().duration_for_price = operation.getAvg_Duration(); //in min
		Timeslot timeslot_for_proposal = new Timeslot();
		timeslot_for_proposal.setEndDate(Long.toString(enddate_cfp));
		timeslot_for_proposal.setStartDate(Long.toString(startdate_cfp));	
		
		Proposal proposal = createProposal(price, operation, timeslot_for_proposal, cfp.getHasSender(), "", 0);	//cfp.getIDString() is empty in CFPs to production resources
		proposal_list.add(proposal);
		return proposal_list;
	}


	@Override
	protected void setStartState() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected Operation setStateAndOperation(ResultSet rs) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected void considerPickup(AllocatedWorkingStep allocatedWorkingStep, Storage_element_slot slot) {
		// TODO Auto-generated method stub
		
	}


	
	@Override
	public void receiveValuesFromDB(Resource r) {
		
		//Shared_Resource shared_r = (Shared_Resource) r;
		
		 Statement stmt = null;
		 String query = "select * from "+tableNameResource+" where "+columnNameResourceName_simulation+" = '"+representedResource.getName()+"'"; 	    		    
 
	    try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	      
	        while (rs.next()) {
	        	
			    	Capability cap = new Capability();
			    	cap.setName(rs.getString(columnNameOfCapability));
			    r.setHasCapability(cap);
			    r.setType(rs.getString(columnNameOfResource_Type));
			    r.setID_Number(rs.getInt(columnNameOfID));		
			    r.setDetailed_Type(rs.getString(columnNameOfResource_Detailed_Type));
	        }	   
	        
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    } 
	    
	    //r = shared_r;
	    //Statement stmt2 = null;
	    String query2 = "";
	   
	    query2 = "select * from "+nameOfCapability_Operations_Mapping_Table+" where "+columnNameOfCapability_Name+" = '"+representedResource.getHasCapability().getName()+"'";
   
	    try {
	        //stmt2 = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(query2);
	     if(consider_shared_resources ) {
	    	 while (rs.next()) {
	    		 String enabled_operation = rs.getString(columnNameOfEnables_Operation); 
	    		 Operation enabled_operation_onto = new Operation();
	    		 enabled_operation_onto.setName(enabled_operation);
	    		 enabled_operation_onto.setType("transport");
	 
	    		 enabled_operation_onto.getIsEnabledBy().add(this.getRepresentedResource().getHasCapability());
	    		 enabled_operations.add(enabled_operation_onto);
	    		 String [] split =enabled_operation_onto.getName().split("_");
	    	        String [] coordinates = split[1].split(";");
	    	        
	    	        range = new Interval(coordinates[0], coordinates[1], false);
	    	 	}
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
        

      //System.out.println("DEBUG______enabled_operation _"+enabled_operation+"  range "+range.toString());
        
	    
	}


	@Override
	public float calculateTimeBetweenStates(State start_next_task, State end_new, long end_of_free_interval) {
		// TODO Auto-generated method stub
		return 0;
	}



} 

