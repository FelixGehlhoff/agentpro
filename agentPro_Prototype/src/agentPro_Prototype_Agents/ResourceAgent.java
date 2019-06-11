package agentPro_Prototype_Agents;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import agentPro.onto.Accept_Proposal;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.DetailedOperationDescription;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Setup_state;
import agentPro.onto.State;
import agentPro.onto.Timeslot;
import agentPro.onto.Transport_Operation;
import agentPro.onto.WorkPlan;
import agentPro.onto._SendProposal;
import agentPro_Prototype_ResourceAgent.ReceiveCFPBehaviour;
import agentPro_Prototype_ResourceAgent.ReceiveCancellationBehaviour;
import agentPro_Prototype_ResourceAgent.ReceiveIntervalForConnectedResourceBehaviour;
import agentPro_Prototype_ResourceAgent.ReceiveRequestBufferBehaviour;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
//import agentPro_Prototype_ResourceAgent.ReceiveRejectProposalBehaviour;
import support_classes.Interval;
import support_classes.Storage_element_slot;

/* 
 * Models a resource.
 */

public abstract class ResourceAgent extends _Agent_Template{

	private static final long serialVersionUID = 1L;
	//private String production_capability;				//for testing only one capability
	//private int duration_of_process;
	private ReceiveCFPBehaviour ReceiveCFPBehav;
	private ReceiveRequestBufferBehaviour ReceiveRequestBufferBehaviour;
	private ReceiveCancellationBehaviour ReceiveCancellationBehav;
	//private ReceiveRejectProposalBehaviour ReceiveRejectProposalBehaviour;
	private int offerNumber = 1;
	//protected Object[] args;
	//protected Resource representedResource;
	private ArrayList <String> needed_shared_resources = new ArrayList <String>();
	//private WorkPlan workplan;
	private ArrayList <Interval> busy_interval_array = new ArrayList <Interval>();
	private ArrayList <Interval> free_interval_array = new ArrayList <Interval>();
	
	//private long time_until_end = 1000*60*60*24*10; //10 Tage 
	public long reply_by_time = 350; //ms KRAN_WS
	public long reply_by_time_shared_resources = 150;
	public int numberOfResourcesPossibleForCalculationOfSharedResourceProposal = 0;
	protected Resource representedResource;
	//Datenbankverbindung
		
		protected String nameOfResource_Definitions_Table = "Resource_Definitions";
		protected String columnNameOfResource_Name = "Resource_Name";
		protected String columnNameOfResource_Type = "Resource_Type";
		protected String columnNameOfResource_Detailed_Type = "Resource_Detailed_Type";
		protected String columnNameOfCapability = "Capability";
		protected String columnNameOfLocationX = "LocationX";
		protected String columnNameOfLocationY = "LocationY";
		protected String columnNameOfAvg_Transportation_Speed = "Avg_Transportation_Speed";
		protected String columnNameOfAvg_PickUp_Time = "Avg_PickUp_Time";
		
		protected String nameOfCapability_Operations_Mapping_Table = _Agent_Template.prefix_schema+".capability_operations_mapping";
		protected String columnNameOfID = "ID";
		protected String columnNameOfCapability_Name = "Capability_Name";
		protected String columnNameOfEnables_Operation = "Enables_Operation";
		protected String columnNameOfOperation_Number = "Operation_Number";
		protected String columnNameOfTimeConsumption = "TimeConsumption";
		
		public ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

	protected void setup (){
		super.setup();
		//representedResource = new Resource();
		setWorkplan(new WorkPlan());
		Interval free_starting_interval = new Interval();
		if(simulation_enercon_mode) {		
			/*	
			if(getLocalName().equals("gr_Bk_West")) {
					addBehaviour(new ReceiveIntervalForConnectedResourceBehaviour((ProductionResourceAgent)this));
				}else {*/
			
					free_starting_interval = new Interval (this.start_simulation-24*60*60*1000, this.start_simulation+time_until_end, false);
					free_interval_array.add(free_starting_interval);
				//}
		}else {
			free_starting_interval = new Interval (1556632800000L, 1556632800000L+time_until_end, false); //30.04. 16 Uhr = start
			//System.out.println("Starting interval = "+_Agent_Template.SimpleDateFormat.format(1556632800000L)+" ; "+_Agent_Template.SimpleDateFormat.format(1556632800000L+time_until_end));
			free_interval_array.add(free_starting_interval);
		}
		
		
		logLinePrefix = getLocalName();
		//setStartState();
		// / INITIALISATION
		// /////////////////////////////////////////////////////////	
		
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
		ReceiveCFPBehav = new ReceiveCFPBehaviour(this);
        addBehaviour(ReceiveCFPBehav);
        ReceiveRequestBufferBehaviour = new ReceiveRequestBufferBehaviour(this);
        addBehaviour(ReceiveRequestBufferBehaviour);
        ReceiveCancellationBehav = new ReceiveCancellationBehaviour(this);
        addBehaviour(ReceiveCancellationBehav);
       // ReceiveRejectProposalBehaviour = new ReceiveRejectProposalBehaviour(this);
        //addBehaviour(ReceiveRejectProposalBehaviour);
	}


	protected abstract void setStartState();
	protected abstract void receiveValuesFromDB(Resource r);

	public Boolean bookIntoSchedule(Accept_Proposal accept_proposal) {			
		 //add interval (busy) and new resulting free intervals	 *  
		Boolean booking_successful = false;
		@SuppressWarnings("unchecked")		
		Iterator<Proposal> it = accept_proposal.getHasProposal().iterator();	  
	    while(it.hasNext()) {	
			Proposal proposal = it.next();
			
			//receive the right interval from the list of intervals
			long long_time_increment_or_decrement_to_be_added_for_setup_of_next_task = 0;
			Timeslot timeslot_to_add = new Timeslot();
			String type = "";
			Storage_element_slot right_slot = null;
			for(Storage_element_slot slot : this.getReceiveCFPBehav().getProposed_slots()) {
				if(slot.getID().contentEquals(((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getName())) {
					if(((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getName().contentEquals("20.0;5.0_Rollformen")){
						System.out.println("here");
					}
					considerPickup((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0));	//prod. res adds the pick up at the beginning
					if(slot.checkNewTimeslot((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0))) {
						timeslot_to_add = ((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot();
						//timeslot_to_add.setStartDate(Long.toString(Long.parseLong(timeslot_to_add.getStartDate())-(long)slot.getDuration_to_get_to_workpiece()));
						//slot_to_add = slot.getTimeslot();							//use the timeslot from the list (includes setup etc.
						long_time_increment_or_decrement_to_be_added_for_setup_of_next_task = slot.getTime_increment();
						type = slot.getType();
						right_slot = slot;
						((AllocatedWorkingStep) right_slot.getProposal().getConsistsOfAllocatedWorkingSteps().get(0)).setHasTimeslot(timeslot_to_add);
						
					}else {
						System.out.println(logLinePrefix  + "______________ERROR_________step has wrong timeslot!!"); //TODO better error handling
						return false;
					}
					

				}
			}
			
			//long long_time_increment_or_decrement_to_be_added_for_setup_of_next_task = (long) (time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000);
			long startdate_busy_interval_new = Long.parseLong(timeslot_to_add.getStartDate())-(long)right_slot.getDuration_to_get_to_workpiece();
			((AllocatedWorkingStep)right_slot.getProposal().getConsistsOfAllocatedWorkingSteps().get(0)).getHasTimeslot().setStartDate(Long.toString(Long.parseLong(timeslot_to_add.getStartDate())-(long)right_slot.getDuration_to_get_to_workpiece()));
			long enddate_busy_interval_new = Long.parseLong(timeslot_to_add.getEndDate());
			Location new_endLocation = null;
			if(type.equals("transport")) {
				new_endLocation = (Location) ((Transport_Operation)((AllocatedWorkingStep)right_slot.getProposal().getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation()).getEndState();				
			}
			
			Interval timeslot_interval_busy = new Interval(startdate_busy_interval_new, enddate_busy_interval_new, false);
			timeslot_interval_busy.setId(accept_proposal.getID_String());
			//System.out.println("DEBUG_                  REceiveCFPBookintoSchedule  allocWorkingstep.getHasTimeslot().getStartDate() "+allocWorkingstep.getHasTimeslot().getStartDate()+" allocWorkingstep.getHasTimeslot().getEndDate() "+allocWorkingstep.getHasTimeslot().getEndDate()+" Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate()  "+Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate())+" Long.parseLong(allocWorkingstep.getHasTimeslot().getEndDate())  "+Long.parseLong(allocWorkingstep.getHasTimeslot().getEndDate())+" time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000+ "+(long) time_increment_or_decrement_to_be_added_for_setup_of_next_task*60*1000+" timeslot_interval_busy "+timeslot_interval_busy.toString());
			
			
			
			booking_successful = adjustIntervals(timeslot_interval_busy, long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, new_endLocation);

			if(booking_successful) {
				getBusyInterval_array().add(timeslot_interval_busy);	
				sortArrayListIntervalsEarliestFirst(getBusyInterval_array(), "start");
				
				getWorkplan().addConsistsOfAllocatedWorkingSteps((AllocatedWorkingStep) right_slot.getProposal().getConsistsOfAllocatedWorkingSteps().get(0));
				
				if(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>1) {
					this.setWorkplan(_Agent_Template.sortWorkplanChronologically(this.getWorkplan()));
				}
			}
	    
	    }
		

		
		printoutFreeIntervals();
		printoutBusyIntervals();
		return booking_successful;		
		//create GANTT chart
		/*
			 XYTaskDatasetDemo2 demo = new XYTaskDatasetDemo2(
		                "JFreeChart : XYTaskDatasetDemo2.java", getWorkplan(), getLocalName());
		        demo.pack();
		        RefineryUtilities.centerFrameOnScreen(demo);
		        demo.setVisible(false);	*/
	}
	
	protected abstract void considerPickup(AllocatedWorkingStep allocatedWorkingStep);


	protected boolean adjustIntervals(Interval timeslot_interval_busy, long long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, Location new_endLocation) {
		Boolean booking_successful = false;
		for(int i = 0;i<getFree_interval_array().size();i++) {		//check the free intervals and find the one that fits		
			if(getFree_interval_array().get(i).contains(timeslot_interval_busy)) {
				booking_successful = true;
				//eg from 0 - 10 contains 5-10
				//store the free interval
				Interval free_interval_that_existed_before = getFree_interval_array().get(i);
				
				//remove the free interval that contains the new busy interval (new ones are created later)
				getFree_interval_array().remove(i);
				//check which new intervals are needed
					//long enddate_busy_interval_before = 0;
					//long startdate_busy_interval_after = 0;
					boolean busy_interval_before_contains_startdate = false;
					boolean busy_interval_after_contains_enddate = false;
					
					//as there can be no (real) overlap between busy intervals --> contains means start & enddate match (or vice versa)		
					if(getBusyInterval_array().size()>0) {
						//check for every interval in busy intervals
						for(int j = 0;j < getBusyInterval_array().size();j++) {
							
							//if this busy interval contains the start date of the new busy interval --> enddate before and start new are equal
							if(getBusyInterval_array().get(j).contains(timeslot_interval_busy.lowerBound())) {	
								busy_interval_before_contains_startdate = true;
								//if there is a busy interval after
								if(j+1<getBusyInterval_array().size()) {
									if(getBusyInterval_array().get(j).contains(timeslot_interval_busy.upperBound())) {	//new 12.02.19
										busy_interval_after_contains_enddate = true;
									}
									
									long old_start = getBusyInterval_array().get(j+1).lowerBound();
									Interval new_busy_interval_AFTER = new Interval(old_start - long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, getBusyInterval_array().get(j+1).upperBound());
									new_busy_interval_AFTER.setId(getBusyInterval_array().get(j+1).getId());
									getBusyInterval_array().remove(j+1);
									getBusyInterval_array().add(j+1, new_busy_interval_AFTER);	
									setStartOfAllocatedWorkingStepThatStartsAtTimeXToYandChangeStartLocation(old_start, long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, new_endLocation);
									
								}						
								break;
							//if this busy interval contains the end date of the new busy interval --> startdate after and end new are equal
							}else if(getBusyInterval_array().get(j).contains(timeslot_interval_busy.upperBound())) {								
								busy_interval_after_contains_enddate = true;
							//startdate of the new free interval BEFORE must be the old start date of the free interval
							//enddate of the new free interval BEFORE must be the start date of the new busy interval
							//startdate of the new free interval AFTER must be the enddate of the new busy interval
							//enddate of the new free interval AFTER must be the enddate of the old free intervall
								//04.04.18 time increment has to be considered! The old busy interval AFTER must be increased (or decreased in time 
								//(or vice versa)) because the setup now takes longer (or shorter)
							long old_start = getBusyInterval_array().get(j).lowerBound(); //new 12.02.19
							Interval new_busy_interval_AFTER = new Interval(getBusyInterval_array().get(j).lowerBound() - long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, getBusyInterval_array().get(j).upperBound());
							new_busy_interval_AFTER.setId(getBusyInterval_array().get(j).getId()); //new 12.02.19
							getBusyInterval_array().remove(j);
							getBusyInterval_array().add(j, new_busy_interval_AFTER);
							setStartOfAllocatedWorkingStepThatStartsAtTimeXToYandChangeStartLocation(old_start, long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, new_endLocation); //new 12.02.19
							
							break;
							//new busy interval does not connect to another but still influences the next one
							}else if(getBusyInterval_array().get(j).contains(free_interval_that_existed_before.upperBound())) {
								long old_start = getBusyInterval_array().get(j).lowerBound(); //new 12.02.19
								Interval new_busy_interval_AFTER = new Interval(getBusyInterval_array().get(j).lowerBound() - long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, getBusyInterval_array().get(j).upperBound());
								new_busy_interval_AFTER.setId(getBusyInterval_array().get(j).getId()); //new 12.02.19
								getBusyInterval_array().remove(j);
								getBusyInterval_array().add(j, new_busy_interval_AFTER);
								setStartOfAllocatedWorkingStepThatStartsAtTimeXToYandChangeStartLocation(old_start, long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, new_endLocation); //new 12.02.19
								break;
							}
						}					
					}

					
					//if only the startdate is element of a busy interval
					// --> one new free interval AFTER the new busy interval
					if(busy_interval_before_contains_startdate && !busy_interval_after_contains_enddate) {
						Interval new_free_intervall_after = new Interval(timeslot_interval_busy.upperBound(), free_interval_that_existed_before.upperBound()- long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, false);
						getFree_interval_array().add(i, new_free_intervall_after);
					}
					
					//if the enddate is element of a busy interval
					// --> one new free interval BEFORE the new busy interval
					else if(!busy_interval_before_contains_startdate && busy_interval_after_contains_enddate) {
						Interval new_free_intervall_before = new Interval(free_interval_that_existed_before.lowerBound(), timeslot_interval_busy.lowerBound(), false);
						getFree_interval_array().add(i, new_free_intervall_before);	
						
					}											
					//if neither the startdate nor the enddate is element of any busy interval --> two new free intervals are needed
					// --> two new free intervals are needed (BEFORE and AFTER)
					else if(!busy_interval_before_contains_startdate && !busy_interval_after_contains_enddate) {
						
						Interval new_free_intervall_before = new Interval(free_interval_that_existed_before.lowerBound(), timeslot_interval_busy.lowerBound(), false);
						Interval new_free_intervall_after = new Interval(timeslot_interval_busy.upperBound(), free_interval_that_existed_before.upperBound()- long_time_increment_or_decrement_to_be_added_for_setup_of_next_task, false);						
						getFree_interval_array().add(i, new_free_intervall_after);
						getFree_interval_array().add(i, new_free_intervall_before);
							
					}
						//if both are elements of two different busy intervals
						// --> no new free interval is needed because it is "replaced" by a busy interval	
					else {
						
					}							
			}
			//break;
			
		}
		return booking_successful;
	}


	private void setStartOfAllocatedWorkingStepThatStartsAtTimeXToYandChangeStartLocation(long old_start_date, long time_increment, Location new_endLocation) {
	
		long new_start_date = old_start_date - time_increment;
		for(int i = 0; i<getWorkplan().getConsistsOfAllocatedWorkingSteps().size();i++) {   	   	
				AllocatedWorkingStep a = (AllocatedWorkingStep) getWorkplan().getConsistsOfAllocatedWorkingSteps().get(i);				
				if(Long.parseLong(a.getHasTimeslot().getStartDate()) == old_start_date) {	    		
					a.getHasOperation().setSet_up_time(a.getHasOperation().getSet_up_time()+(float)time_increment/(60*1000));
					a.getHasTimeslot().setStartDate(String.valueOf(new_start_date));	    		
		    		if(new_endLocation != null) {
		    			((Transport_Operation)a.getHasOperation()).setStartState(new_endLocation);
		    		}
		    		
		    		
		    		break;
		    	}
		    }
		
	}


	public int getOfferNumber() {
		return offerNumber;
	}

	public void setOfferNumber(int offerNumber) {
		this.offerNumber = offerNumber;
	}
	public abstract boolean feasibilityCheckAndDetermineDurationParameters(Operation operation);
	
	
	protected float calculateTimeIncrement(Operation op, int counter_free_interval_i, DetailedOperationDescription operation_description) {
		float time_increment_or_decrement_to_be_added = 0;

		//check if there is a task that starts at the end of the free interval
		if(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>0) {		
			@SuppressWarnings("unchecked")		
			Iterator<AllocatedWorkingStep> it = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();	  
		    while(it.hasNext()) {		//checks for every allWS in Workplan
		    	AllocatedWorkingStep a = it.next();	  
		    	if(Long.parseLong(a.getHasTimeslot().getStartDate()) == getFree_interval_array().get(counter_free_interval_i).upperBound()) {
		    		//if yes

		    		State start_next_task_needed = a.getHasOperation().getstartStateNeeded();
		    		
		    		//Location start_next_task = (Location) ((Transport_Operation)a.getHasOperation()).getStartState();
		    		
		    		State end_new = op.getEndState();
		    		//Location end_new = (Location) transport_op_to_destination.getEndState();
		    		
		    		time_increment_or_decrement_to_be_added = calculateTimeBetweenStates(start_next_task_needed, end_new, counter_free_interval_i);
		    				    		
		    		if(time_increment_or_decrement_to_be_added != 0) {
		    			//addPointToList(operation_description, end_new.getCoordX(), getFree_interval_array().get(i).upperBound()-(long)(time_increment_or_decrement_to_be_added*60*1000), "Start:set_up");		    			    					    		
		    		}
		    		break;
		    	}
		    }	  
		}		
		return time_increment_or_decrement_to_be_added;
	}

	
	public float calculateTimeBetweenStates(State start_next_task, State end_new, int busy_interval_i) {
		return 0;		
	}

	protected float getDurationOfNextSetupStartingAt(long upperBound) {
		float duration_setup = 0;	
			@SuppressWarnings("unchecked")		
			Iterator<AllocatedWorkingStep> i = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();  
		    while(i.hasNext()) {		//checks for every allWS in Workplan
		    	AllocatedWorkingStep a = i.next();	  
		    	if(Long.parseLong(a.getHasTimeslot().getStartDate()) == upperBound) {
		    		duration_setup = a.getHasOperation().getSet_up_time();
		    		//System.out.println("DEBUG__________QQQQQQQQQQQQQQQQQQQ          a.getHasOperation().name "+a.getHasOperation().getName()+"_____________duration step up = "+duration_setup);
		    		break;
		    	}
	
		    }	    
		return duration_setup;
	}
	
	//checks the state of the following allocated working step
	public State getStateAtTime(long lowerBound_freeInterval) {
		State state = new State();
		Boolean allocatedWorkingStepWithGreaterTimeFound = false;
		if(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>0) {		
			@SuppressWarnings("unchecked")		
			Iterator<AllocatedWorkingStep> i = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();	  
		    while(i.hasNext()) {		//checks for every allWS in Workplan
		    	AllocatedWorkingStep a = i.next();	  
		    	if(Long.parseLong(a.getHasTimeslot().getStartDate()) > lowerBound_freeInterval) {
		    		state = a.getHasOperation().getStartState();
		    		allocatedWorkingStepWithGreaterTimeFound = true;
		    		break;
		    	}
		    }
		   
		    if(!allocatedWorkingStepWithGreaterTimeFound) {	//the time is after the last allocated step
		    	//take the end location of the last allocated step
		    	Operation op = ((AllocatedWorkingStep) getWorkplan().getConsistsOfAllocatedWorkingSteps().get(getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1)).getHasOperation();
		    	state = op.getEndState();
		    }
		}else { //first step
			state = getRepresentedResource().getStartState(); 
			//System.out.println(this.getLocalName()+"  DEBUG___________First step____________"+state.getClass().getSimpleName());
		}
		return state;
	
	}
	
	//public abstract Proposal checkScheduleDetermineTimeslotAndCreateProposal(long startdate_cfp, long enddate_cfp, Operation operation);
	public abstract Proposal checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp);
	
	//refers to the actual process (without setup)
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
	}
	
	public Proposal createProposal(float price, Operation operation, Timeslot timeslot_for_proposal, AID sender, String id_string) {
		Proposal proposal = new Proposal();
		int proposal_id = getOfferNumber();
		proposal.setID_Number(proposal_id);
		setOfferNumber(getOfferNumber()+1);
		proposal.setID_String(id_string);
		proposal.setHasSender(this.getAID());
		AllocatedWorkingStep proposed_slot = new AllocatedWorkingStep();
		//25.02. in Operation
		//operation.setBuffer_before_operation(this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier);
		proposed_slot.setHasOperation(operation);
		
		Resource thisResource = new Resource();

		thisResource.setName(getRepresentedResource().getName());  		//not all parameters are relevant
		thisResource.setHasLocation(getRepresentedResource().getHasLocation());
		thisResource.setType(getRepresentedResource().getType());
		thisResource.setDetailed_Type(getRepresentedResource().getDetailed_Type());
		thisResource.setID_Number(getRepresentedResource().getID_Number());
		proposed_slot.setHasResource(thisResource);
		
		proposal.setPrice(price);

		proposed_slot.setHasTimeslot(timeslot_for_proposal);
		proposed_slot.setID_String(sender.getLocalName()+"@"+getLocalName()+"."+proposal_id);
		//System.out.println("DEBUG_________Res Agent create Proposal ID String "+sender.getLocalName()+"@"+getLocalName()+"."+proposal_id);
		//25.02. in Operation
		//proposed_slot.setBuffer_before_operation(this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier);
		
		proposal.addConsistsOfAllocatedWorkingSteps(proposed_slot);
		
		return proposal;
	}

	public abstract Resource getRepresentedResource();

	public abstract void setRepresentedResource(Resource res);
	/*
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
*/
	public ArrayList <Interval> getBusyInterval_array() {
		return busy_interval_array;
	}

	public void setBusyInterval_array(ArrayList <Interval> interval_array) {
		this.busy_interval_array = interval_array;
	}
	
	public ArrayList <Interval> getFree_interval_array() {
		return free_interval_array;
	}

	public void setFree_interval_array(ArrayList <Interval> free_interval_array) {
		this.free_interval_array = free_interval_array;
	}
	
	public void removeAllocatedWorkingStepFromWorkPlanAndBusyIntervalsAndCreateFreeIntervals(AllocatedWorkingStep allWS) {
    	//find allWS in Workplan
    	//int counter = 0;
		Operation op = null;
		
		//find correct allWS
		//delete it in workplan and busy interval array
		//add it in free interval array
		if(numberOfResourcesPossibleForCalculationOfSharedResourceProposal > 0) {
			printoutBusyIntervals();
		}
		
		
		 @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it_2 = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();		 	
		    while(it_2.hasNext()) {
		    	AllocatedWorkingStep allocWorkingstep = it_2.next();
		    	op = allocWorkingstep.getHasOperation();	
		    	String name_Workpiece = allocWorkingstep.getHasOperation().getAppliedOn().getID_String();
		    	Interval busy_interval = new Interval (Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate()), Long.parseLong(allocWorkingstep.getHasTimeslot().getEndDate()), false);
		    	
		    	//if operaiton name and workpiece ID are correct --> delete
		    	if(op.getName().equals(allWS.getHasOperation().getName()) && name_Workpiece.equals(allWS.getHasOperation().getAppliedOn().getID_String())) {	//find out the position of the relevant step			    	
		    		 //getWorkplan().getConsistsOfAllocatedWorkingSteps().remove(counter);
		    		it_2.remove();		    		
		    		 //find the corresponding busy interval and delete it + add free interval
		    		 for(int i = 0;i < getBusyInterval_array().size(); i++) {
		    			
		    			 if(getBusyInterval_array().get(i).getId().equals(allocWorkingstep.getID_String())) {
		    				// System.out.println(getLocalName()+" DEBUG________busy interval removed "+SimpleDateFormat.format(getBusyInterval_array().get(i).lowerBound())+" "+SimpleDateFormat.format(getBusyInterval_array().get(i).upperBound()));
		    				getBusyInterval_array().remove(i);
		    			 	getFree_interval_array().add(busy_interval); 
		    			 }
		    			 
		    			 /*
		    			 if(getBusyInterval_array().get(i).lowerBound() == busy_interval.lowerBound() && getBusyInterval_array().get(i).upperBound() == busy_interval.upperBound()) {
		    				 System.out.println(getLocalName()+" DEBUG________busy interval removed "+SimpleDateFormat.format(getBusyInterval_array().get(i).lowerBound())+" "+SimpleDateFormat.format(getBusyInterval_array().get(i).upperBound()));
		    				getBusyInterval_array().remove(i);
		    			 	getFree_interval_array().add(busy_interval); //TBD sorting and merging!!
		    			 }*/
		    		 }
		    		 //sortWorkplanChronologically();
		    		 //sortFreeIntervalsChronologically();
		    		 sortArrayListIntervalsEarliestFirst(this.getFree_interval_array(), "start");
		    		 //merging has to be done at least twice --> TBD if there is a better way
		    		 //if(getLocalName().equals("Kran1")) {
		    			 mergeAdjacentFreeIntervals();
			    		 mergeAdjacentFreeIntervals();	 
		    		 //}
	    		 				    		 
		    	}
		    	//counter++;
		    }
		    if(numberOfResourcesPossibleForCalculationOfSharedResourceProposal > 0) {
				printoutBusyIntervals();
			}
		
	}
	
	public void sortArrayListIntervalsEarliestFirst(ArrayList<Interval> arrayList, String startOrEnd) {
		if(startOrEnd.equals("start")) {
			Comparator<Interval> comparator = Comparator.comparing(Interval::lowerBound);
			Collections.sort(arrayList, comparator);
		}else if(startOrEnd.equals("end")) {
			Comparator<Interval> comparator = Comparator.comparing(Interval::upperBound);
			Collections.sort(arrayList, comparator);
		}	
	}
	
	
	private void mergeAdjacentFreeIntervals() {
			
		/*
		for(Interval i : getFree_interval_array()) {
			System.out.println(getLocalName()+" DEBUG________BEFORE MERGER lower bound "+SimpleDateFormat.format(i.lowerBound())+ " upper bound "+SimpleDateFormat.format(i.upperBound()));
		}*/
		
		ArrayList <Interval> new_merged_list = new ArrayList <Interval>();
		for(int i = 0;i <= getFree_interval_array().size()-1;i++) {	
			Interval free_interval_i_plus1 = null;
			Interval free_interval_i = getFree_interval_array().get(i);
			
			//if there exists an element behind i (so i+1), get that element
			if(i<getFree_interval_array().size()-1) {
				free_interval_i_plus1 = getFree_interval_array().get(i+1);
			}else{ // i = size -1 --> e.g. i = 4, array size = 5 --> last element --> add the last element to the new list
				new_merged_list.add(free_interval_i);
				break; //last element was added --> leave the for loop
			}
		
			//if the intervals share a common bound (lower of i and upper of i-1) --> create a new one with lower bound i-1 and upper bound i
			if(free_interval_i.upperBound() == free_interval_i_plus1.lowerBound()) {
				Interval new_free_intervall = new Interval(free_interval_i.lowerBound(), free_interval_i_plus1.upperBound(), false);
				//System.out.println(getLocalName()+" DEBUG________MERGED__________"+logLinePrefix+" free_interval_i.lowerBound() "+SimpleDateFormat.format(free_interval_i.lowerBound())+" free_interval_i_plus1.upperBound() "+SimpleDateFormat.format(free_interval_i_plus1.upperBound()));
				//getFree_interval_array().add(i-1, new_free_intervall);
				new_merged_list.add(new_free_intervall);
				i++; //nächstes überspringen, da bereits in dem merged interval enthalten

			//this should not be needed (overlap of more than a shared bound)
			}else if(free_interval_i.intersection(free_interval_i_plus1).getSize()>0){
				Interval new_free_intervall = new Interval(free_interval_i.lowerBound(), free_interval_i_plus1.upperBound(), false);
				new_merged_list.add(new_free_intervall);
			}
			//no overlap
			else{
				new_merged_list.add(getFree_interval_array().get(i));
			}
		}
		//set the new list as the free interval list
		setFree_interval_array(new_merged_list);
		/*
		for(Interval i : getFree_interval_array()) {
			System.out.println(getLocalName()+" DEBUG________AFTER MERGER lower bound "+SimpleDateFormat.format(i.lowerBound())+ " upper bound "+SimpleDateFormat.format(i.upperBound()));
		}*/
		
	}



	public void printoutBusyIntervals() {
		String printout = getLocalName()+" DEBUG____BUSY INTERVALS	";
		for(Interval i : getBusyInterval_array()) {
			printout = printout + " id "+i.getId()+" "+ SimpleDateFormat.format(i.lowerBound())+";"+SimpleDateFormat.format(i.upperBound());
		}
		 System.out.println(printout);
	}
	
	public void printoutFreeIntervals() {
		String printout = getLocalName()+" DEBUG____________FREE INTERVALS	";
		for(Interval i : getFree_interval_array()) {
			printout = printout + " "+ SimpleDateFormat.format(i.lowerBound())+";"+SimpleDateFormat.format(i.upperBound());
		}
		 System.out.println(printout);
	}

	public ArrayList <String> getNeeded_shared_resources() {
		return needed_shared_resources;
	}

	public void setNeeded_shared_resources(ArrayList <String> needed_shared_resources) {
		this.needed_shared_resources = needed_shared_resources;
	}

	public ReceiveCFPBehaviour getReceiveCFPBehav() {
		return ReceiveCFPBehav;
	}





	public void sendRefusal(ACLMessage msg) {
		ACLMessage refusal = new ACLMessage(ACLMessage.REFUSE);
		AID receiver = new AID();		
		receiver.setLocalName(msg.getSender().getLocalName());
		refusal.addReceiver(receiver);
		refusal.setConversationId(msg.getConversationId());
		refusal.setLanguage(getCodec().getName());
		refusal.setOntology(getOntology().getName());
	//proposal.setReplyWith(String.valueOf(proposal_onto.getID_Number()));
		//refusal.setInReplyTo(String.valueOf(proposal_onto.getID_Number()));
	
		/*
	_SendProposal sendProposal = new _SendProposal();
	sendProposal.setHasProposal(proposal_onto);
	
	Action content = new Action(getAID(),sendProposal);
	
	try {
		getContentManager().fillContent(proposal, content);
	} catch (CodecException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (OntologyException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	*/
	
	send(refusal);		
	printOutSent(refusal);
	//System.out.println("DEBUG REsource Agent refusal sent at______"+System.currentTimeMillis());
	//System.out.println(SimpleDateFormat.format(
		
	}
	public void sendRefusal(String conversationID, ArrayList<String> sender) {
		ACLMessage refusal = new ACLMessage(ACLMessage.REFUSE);
		AID receiver = new AID();		
		receiver.setLocalName(sender.get(0));
		refusal.addReceiver(receiver);
		refusal.setConversationId(conversationID);
		refusal.setLanguage(getCodec().getName());
		refusal.setOntology(getOntology().getName());
	//proposal.setReplyWith(String.valueOf(proposal_onto.getID_Number()));
		//refusal.setInReplyTo(String.valueOf(proposal_onto.getID_Number()));

	
	send(refusal);		
	printOutSent(refusal);
		
	}

	//i for x times replybytime
		public void sendProposal(Proposal proposal_onto, String conversationID, String sender, Double i) {
			ArrayList<Proposal>list = new ArrayList<Proposal>();
			list.add(proposal_onto);
			ArrayList<String>list_sender = new ArrayList<String>();
			list_sender.add(sender);
			sendProposal(list, conversationID, list_sender, i);
			//System.out.println("DEBUG_______proposal sent at______"+System.currentTimeMillis());
			//System.out.println(SimpleDateFormat.format(new Date()) +" "+getLocalName()+logLinePrefix+" PROPOSAL sent to receiver: "+receiver.getLocalName()+" with content: "+proposal.getContent());
			
		}
		
		public void sendProposal(Proposal proposal_onto, String conversationID, ArrayList<String> sender, Double i) {
			for(String sender_localName : sender) {
					sendProposal(proposal_onto, conversationID, sender_localName, i);			
				}
		}
		
	public void sendProposal(ArrayList<Proposal> proposals, String conversationID, ArrayList<String> sender, Double i) {
		ACLMessage proposal = new ACLMessage(ACLMessage.PROPOSE);
		for(String receiver_name : sender) {
			AID receiver = new AID();		
			receiver.setLocalName(receiver_name);
			proposal.addReceiver(receiver);
		}
		
	proposal.setConversationId(conversationID);
	proposal.setLanguage(getCodec().getName());
	proposal.setOntology(getOntology().getName());
	//proposal.setReplyWith(String.valueOf(proposal_onto.getID_Number()));
	
	
	_SendProposal sendProposal = new _SendProposal();
	for(Proposal proposal_onto : proposals) {
		sendProposal.addHasProposal((proposal_onto));
		proposal.setInReplyTo(String.valueOf(proposal_onto.getID_Number()));	//wird überschrieben, ist aber bei allen gleich
	}
	
	
	Action content = new Action(getAID(),sendProposal);
	
	try {
		getContentManager().fillContent(proposal, content);
	} catch (CodecException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (OntologyException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	//determine reply by time
	long reply_by_date_long = 0;
	if(i != null) {
		reply_by_date_long = (long) (System.currentTimeMillis()+i*reply_by_time);
	}else {
		reply_by_date_long = System.currentTimeMillis()+reply_by_time;
	}
	
	Date reply_by_date = new Date(reply_by_date_long);
	proposal.setReplyByDate(reply_by_date);
	send(proposal);		
	printOutSent(proposal);
		
	}
	
	protected Storage_element_slot createStorageElement(Operation operation, Timeslot timeslot, float duration_to_get_to_workpiece,
			float time_increment) {
		
		Storage_element_slot slot = new Storage_element_slot(operation, timeslot, duration_to_get_to_workpiece, (long) time_increment);
		
			return slot;
		
	}
} 

