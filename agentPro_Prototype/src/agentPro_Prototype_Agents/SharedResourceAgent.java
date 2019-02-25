package agentPro_Prototype_Agents;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import agentPro.onto.CFP;
import agentPro.onto.Capability;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Shared_Resource;
import agentPro.onto.Timeslot;
import agentPro.onto.TransportResource;
import agentPro_Prototype_ResourceAgent.ReceiveCFPBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import support_classes.Interval;

/*
 * Models a shared resource. 
 */
public abstract class SharedResourceAgent extends ResourceAgent{

	private static final long serialVersionUID = 1L;
	//private ReceiveCFPBehaviour ReceiveCFPBehav;
	protected Shared_Resource representedResource;

	protected void setup (){
		super.setup();
		this.numberOfResourcesPossibleForCalculationOfSharedResourceProposal = 1; //TBD dynamisch
		logLinePrefix = logLinePrefix+"-SharedResourceAgent.";
		
		
		    representedResource = new Shared_Resource();
			representedResource.setName(this.getLocalName());
			receiveValuesFromDB(representedResource);
			reply_by_time = 600;
							
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
			sd.setType("shared_resource");
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
	public boolean feasibilityCheck(Operation operation) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public void receiveValuesFromDB(Resource r) {
		
		Shared_Resource shared_r = (Shared_Resource) r;
		
		 Statement stmt = null;
		    String query = "";
	
		    	query = "select "+columnNameOfID+" , "+columnNameOfResource_Detailed_Type+" , "+columnNameResourceName_simulation+" , "+columnNameOfCapability+" , "+columnNameOfResource_Type+" from "+tableNameResource+" where "+columnNameResourceName_simulation+" = '"+representedResource.getName()+"'"; 	    		    
 
	    try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	      
	        while (rs.next()) {
	        	
			    	Capability cap = new Capability();
			    	cap.setName(rs.getString(columnNameOfCapability));
			    shared_r.setHasCapability(cap);
			    shared_r.setType(rs.getString(columnNameOfResource_Type));
			    shared_r.setID_Number(rs.getInt(columnNameOfID));		
			    shared_r.setDetailed_Type(rs.getString(columnNameOfResource_Detailed_Type));
	        }	   
	        
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    } 
	    
	    r = shared_r;
	    
	}

	//@Override
	//public Proposal checkScheduleDetermineTimeslotAndCreateProposal(long startdate_cfp, long enddate_cfp, Operation operation) {
	/*
	public Proposal checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp) {
		
		Proposal proposal = new Proposal();
		Timeslot timeslot_for_proposal = new Timeslot();
		
		long estimated_start_date = 0;
		long estimated_enddate = 0;
		long set_up_time = 0; //no set up time needed at this moment
		
		//extract CFP Timeslot
		Timeslot cfp_timeslot = cfp.getHasTimeslot();	
		Operation operation = cfp.getHasOperation();
		long startdate_cfp = Long.parseLong(cfp_timeslot.getStartDate());
		long enddate_cfp = Long.parseLong(cfp_timeslot.getEndDate());
		
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
				break;
			
			}else if(getFree_interval_array().get(i).getSize() >= timeslot_interval_to_be_booked_shared_resource.getSize()){
				//the desired slot is not fully in a free interval
				//check whether the slot can be postponed to a later interval (which has the correct size) but not the earliest start date
				//for(int j = 0;j<getFree_interval_array().size();j++) {
					//if(getFree_interval_array().get(j).getSize() >= timeslot_interval_to_be_booked_production.getSize()) {
					if(getFree_interval_array().get(i).lowerBound() >= timeslot_interval_to_be_booked_shared_resource.lowerBound()) {	
						estimated_start_date = getFree_interval_array().get(i).lowerBound();
						estimated_enddate = (long) (estimated_start_date+operation.getAvg_Duration()*60*1000+set_up_time*60*1000);	//start at the lower bound with the set up + duration = enddate
						//this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier = 0; //because the lowerBound of a free Interval is taken --> before that is a busy interval
						break;
					}
					
				//}				
			}
			
		}	
		
		timeslot_for_proposal.setEndDate(Long.toString(estimated_enddate));
		timeslot_for_proposal.setStartDate(Long.toString(estimated_start_date+set_up_time*60*1000));	
		//this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(Long.toString(estimated_enddate));
		//this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(Long.toString(estimated_start_date));
		
		float price = operation.getAvg_Duration();	
		//this.getReceiveCFPBehav().duration_for_price = operation.getAvg_Duration(); //in min
	//TBD --> Before the real values can be used, the transport resource has to analyze the feedback			
	timeslot_for_proposal.setEndDate(Long.toString(enddate_cfp));
	timeslot_for_proposal.setStartDate(Long.toString(startdate_cfp));	
		//this.getReceiveCFPBehav().timeslot_for_schedule.setEndDate(Long.toString(enddate_cfp));
		//this.getReceiveCFPBehav().timeslot_for_schedule.setStartDate(Long.toString(startdate_cfp));
		
		proposal = createProposal(price, operation, timeslot_for_proposal, cfp.getHasSender());
		return proposal;
	}
*/
	@Override
	public Shared_Resource getRepresentedResource() {
		return representedResource;
	}

	@Override
	public void setRepresentedResource(Resource res) {
		representedResource = (Shared_Resource) res;
		
	}



} 

