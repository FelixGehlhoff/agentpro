package agentPro_Prototype_WorkpieceAgent_Behaviours;

import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Timeslot;
import agentPro.onto.Transport_Operation;
import agentPro_Prototype_Agents.WorkpieceAgent;
import agentPro_Prototype_Agents._Agent_Template;
import jade.core.behaviours.Behaviour;
import support_classes.Interval;

public class BookBufferPlaceProcedureBehaviour extends Behaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String logLinePrefix = ".BookBufferPlaceProcedureBehaviour ";
	private WorkpieceAgent myAgent;
	private AllocatedWorkingStep relevant_AllWS;
	private AllocatedWorkingStep failure_step;
	private int step = 0;
	int position_in_Allocated_Working_step_list;
	long startdate_for_this_task;
	//private int count_of_allocated_working_steps_before_new_task_is_scheduled;
	private long threshold_for_buffer_place = 1*60*60*1000; // 1 h 
	private Location location_buffer_place;
	private boolean reschedulingWasActive = false;
	private AllocatedWorkingStep allWS_buffer_place;
	private AllocatedWorkingStep allWS_transport_to_buffer;
	private Boolean waitForBuffer = false;
	private long clock = 0;
	


	public BookBufferPlaceProcedureBehaviour(WorkpieceAgent myAgent, AllocatedWorkingStep relevant_allWS) {
		this.myAgent = myAgent;
		this.relevant_AllWS = relevant_allWS;
		this.failure_step = relevant_allWS;
		//the step we use can be a transport step!
		position_in_Allocated_Working_step_list = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1;
		 //in a real application the current time would be correct --> here a workaraound is needed (because the finished steps are later in the future) TBD
    	//workaround: start_date is the end of the last finished prod. step + transport_estimation
		//startdate_for_this_task = Long.parseLong(((AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(position_in_Allocated_Working_step_list)).getHasTimeslot().getEndDate())+myAgent.getTransport_estimation();
		//startdate_for_this_task = Long.parseLong(((AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(position_in_Allocated_Working_step_list)).getHasTimeslot().getEndDate());
		
		//startdate_for_this_task is the start of the next regular production step
		if(myAgent.simulation_mode) {
			startdate_for_this_task = (long) relevant_allWS.getEnddate()+2*myAgent.getTransport_estimation()+(long)myAgent.duration_repair_workpiece*60*60*1000;
		}else {
			//26.03. new workaround: use old enddate as startdate	
			//12.02.2019 --> gleich wie bei Simulatin?
			//startdate_for_this_task = Long.parseLong(relevant_allWS.getHasTimeslot().getEndDate());
			startdate_for_this_task = (long) relevant_allWS.getEnddate()+2*myAgent.getTransport_estimation()+(long)myAgent.duration_repair_workpiece*60*60*1000;
		}
	
		System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+" order should start at: "+myAgent.SimpleDateFormat.format(startdate_for_this_task));
		
	}

	@Override
	public void action() {
	    //start error handling procedure --> new bookings

		/*
		if(step > 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
    	
	    switch (step) {
	    case 0:
	    	//schedule next production step
	    	myAgent.addBehaviour(new RequestPerformer_transport(myAgent, relevant_AllWS.getHasOperation(), startdate_for_this_task, null, true));		//error step = true --> production manager is not started		    	
    		step = 1;
    		this.block(5);
    		break;
	    case 1:
	    	//schedule buffer place
    		//start a new request Performer that finds a buffer place and arranges transport to buffer
	    	//System.out.println("DEBUG_______________WAIT FOR STEP TO BE ADDED "+System.currentTimeMillis());
	    	if(myAgent.getLastProductionStepAllocated() != null && !myAgent.getLastProductionStepAllocated().getIsFinished()) {	//new step has been added
	    		AllocatedWorkingStep next_production_step = myAgent.getLastProductionStepAllocated();
	    		relevant_AllWS = next_production_step;
	    		
	    		/*
	    		if(myAgent.simulation_mode) {
	    			Timeslot timeslot_to_book_buffer_place = new Timeslot();
		    		//start is "now" (with workaround its the end of the last finished production step) + transport estimation
		    		timeslot_to_book_buffer_place.setStartDate(String.valueOf(startdate_for_this_task+myAgent.getTransport_estimation()));
		    		//end is start of next production - transport estimation
		    		timeslot_to_book_buffer_place.setEndDate(String.valueOf(Long.parseLong(next_production_step.getHasTimeslot().getStartDate())-myAgent.getTransport_estimation()));

		    		Operation buffer = new Operation();
				    buffer.setType("production");
				    buffer.setName("Nachbearbeitung");
				    buffer.setAppliedOn(myAgent.getRepresented_Workpiece());
				    long duration = (Long.parseLong(next_production_step.getHasTimeslot().getStartDate())-myAgent.getTransport_estimation())-(startdate_for_this_task+myAgent.getTransport_estimation());
				    buffer.setAvg_Duration(duration/(1000*60));	//in minutes
				  		    if(duration < myAgent.duration_repair_workpiece*60*60*1000) {
				  		    	System.out.println("DEBUG_BOOK BUFFER PLACE BEHAVIOUR____DURATION TOO SHORT!!!!");
				  		    }
				    //use the same startdate_for_this_task as before
				    System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+" Buffer is necessary for = "+duration/(1000*60)+" min.");
				    myAgent.addBehaviour(new RequestPerformer(myAgent, buffer, timeslot_to_book_buffer_place, null, true));		//!last operation = false"  triggers the start of the production manager				    				    		
				    
				step = 2;	
				this.block(5);
				*/
	    		//}else {
		    		//check whether we need a buffer place
		    	
		    		//interval between now and the start of the next production step
	    		Interval interval_between_now_and_Startdate_production = new Interval();
	    		
	    		if(myAgent.simulation_mode) {
	    			interval_between_now_and_Startdate_production = new Interval((long)failure_step.getEnddate(), Long.parseLong(next_production_step.getHasTimeslot().getStartDate()));
		    			
	    		}else {
	    			interval_between_now_and_Startdate_production = new Interval(startdate_for_this_task, Long.parseLong(next_production_step.getHasTimeslot().getStartDate()));
		    			
	    		}
		    		System.out.println("DEBUG___________"+myAgent.SimpleDateFormat.format(interval_between_now_and_Startdate_production.lowerBound())+ " upper bound "+myAgent.SimpleDateFormat.format(interval_between_now_and_Startdate_production.upperBound()));
		    		if(interval_between_now_and_Startdate_production.getSize()<0) {
		    			System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+" ______________ERROR___________ interval <  0 --> Something went wrong");
			    		step = 5;
			    		break;
		    		}else if(interval_between_now_and_Startdate_production.getSize()<=threshold_for_buffer_place) {
		    			System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+" ______________ERROR___________ Buffering is not necessary. Restart Production Manager.");
			    		
			    		//restart production manager for next "official" production step
						myAgent.getProductionManagerBehaviour().setStep(0);
						myAgent.getProductionManagerBehaviour().restart();
						step = 5;
			    		break;
			    		
		    		}else { //if the interval is big enough --> book buffer
		    			Timeslot timeslot_to_book_buffer_place = new Timeslot();
			    		//start is "now" (with workaround its the end of the last finished production step) + transport estimation
		    			if(myAgent.simulation_mode) {
		    				timeslot_to_book_buffer_place.setStartDate(String.valueOf((long)failure_step.getEnddate()+myAgent.getTransport_estimation()));
				    		//end is start of next production - transport estimation				    		
		    			}else {
		    				timeslot_to_book_buffer_place.setStartDate(String.valueOf(startdate_for_this_task+myAgent.getTransport_estimation()));				    						    		
		    			}
		    			//end is start of next production - transport estimation
		    			timeslot_to_book_buffer_place.setEndDate(String.valueOf(Long.parseLong(next_production_step.getHasTimeslot().getStartDate())-myAgent.getTransport_estimation()));

			    		Operation buffer = new Operation();
					    buffer.setType("production");
					    long duration = 0;
						if(myAgent.simulation_mode) {
							buffer.setName("Nachbearbeitung");
						 duration = (Long.parseLong(next_production_step.getHasTimeslot().getStartDate())-myAgent.getTransport_estimation())-((long)failure_step.getEnddate()+myAgent.getTransport_estimation());
							    
						}else {
							buffer.setName("Puffern_Klein");
						 duration = (Long.parseLong(next_production_step.getHasTimeslot().getStartDate())-myAgent.getTransport_estimation())-(startdate_for_this_task+myAgent.getTransport_estimation());
							    
						}
					 
					    buffer.setAppliedOn(myAgent.getRepresented_Workpiece());
					   buffer.setAvg_Duration(duration/(1000*60));	//in minutes
					  		    
					    //use the same startdate_for_this_task as before
					    System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+" Buffer is necessary for = "+duration/(1000*60)+" min.");
					    myAgent.addBehaviour(new RequestPerformer_transport(myAgent, buffer, timeslot_to_book_buffer_place, null, true));		//!last operation = false"  triggers the start of the production manager				    				    		
					    
					step = 2;	
					this.block(5);
		    		}	
	    		//}

																										//15.02.18 debugging last operation = null --> vorher false
	    	}
		    
	    	break;
	    case 2: //schedule transport to buffer place
	    	

	    	
	    	if(myAgent.getLastProductionStepAllocated().getIsErrorStep()) {		//buffer operation has been added
	    		AllocatedWorkingStep buffer_place = myAgent.getLastProductionStepAllocated();		    		
	    		//allWS_buffer_place = buffer_place;
	    		Transport_Operation transport_operation = new Transport_Operation();									//
				transport_operation.setType("transport");					
				Location location_buffer_place = buffer_place.getHasResource().getHasLocation();
				Location location_current = myAgent.getLocation();
				
				transport_operation.setStartState(location_current);
				transport_operation.setEndState(location_buffer_place);
				
				//Name = Start_Ziel in format  X;Y_DestinationResource
				transport_operation.setName(location_current.getCoordX()+";"+location_current.getCoordY()+"_"+buffer_place.getHasResource().getName());
				transport_operation.setAppliedOn(myAgent.getRepresented_Workpiece());

	    		Timeslot timeslot_to_book_transport_to_buffer = new Timeslot();
	    		//start is now
	    		timeslot_to_book_transport_to_buffer.setStartDate(String.valueOf(startdate_for_this_task));	    		
	    		//end is start of the buffer place  		
	    		timeslot_to_book_transport_to_buffer.setEndDate(myAgent.getLastProductionStepAllocated().getHasTimeslot().getStartDate());
	    		
	    		//count_of_allocated_working_steps_before_new_task_is_scheduled = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size();
	    		clock = System.currentTimeMillis()+2*myAgent.getProductionManagerBehaviour().reply_by_time;
	    		myAgent.addBehaviour(new RequestPerformer_transport(myAgent, transport_operation, timeslot_to_book_transport_to_buffer, null, true));		//error step = true --> production manager is not started		    			    		
	    		step = 3;
	    		this.block(5);
	    	}
	    	break;
	    case 3:
	    	System.out.println("DEBUG___Case 3 "+System.currentTimeMillis()+" >= clock "+clock);
	    	//find the correct step --> transport to buffer
	    	//if(System.currentTimeMillis()>= clock) {
		    	for(int i = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1;i>=0;i--) {
	    			AllocatedWorkingStep allWS_transport_to_buffer = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(i);
	    			Boolean doLocationsmatch_startTransporttoProduction_LocationOfOldProductionStep = false;
	    			if(allWS_transport_to_buffer.getHasOperation().getType().equals("transport")) {
	    				if(_Agent_Template.simulation_mode) {
	    					doLocationsmatch_startTransporttoProduction_LocationOfOldProductionStep = myAgent.doLocationsMatch(((Location)allWS_transport_to_buffer.getHasOperation().getStartState()), failure_step.getHasResource().getHasLocation());    		     				  	    			
	    				}else {
	    					doLocationsmatch_startTransporttoProduction_LocationOfOldProductionStep = myAgent.doLocationsMatch(((Location)allWS_transport_to_buffer.getHasOperation().getStartState()), relevant_AllWS.getHasResource().getHasLocation());    		     				    			
	    				}
	    			}
	    			//System.out.println("DEBUG______QQQQQQQQQ___________allWS: "+allWS_transport_to_buffer.getID_String()+" "+allWS_transport_to_buffer.getHasOperation().getName()+" do locations match? "+doLocationsmatch_startTransporttoProduction_LocationOfOldProductionStep+" myAgent.getProductionManagerBehaviour().isBackwards_scheduling_activ()  "+myAgent.getProductionManagerBehaviour().isBackwards_scheduling_activ());
	    			
	    			if(doLocationsmatch_startTransporttoProduction_LocationOfOldProductionStep) {	//correct step found
	    				this.allWS_transport_to_buffer = allWS_transport_to_buffer;
	    				//does the buffer have to be rescheduled?
	    				if(myAgent.getProductionManagerBehaviour().isBackwards_scheduling_activ()) {

	    		    		Timeslot new_timeslot_to_book_buffer_place = new Timeslot();
	    		    		//start is determined by scheduled transport operation
	    		    		new_timeslot_to_book_buffer_place.setStartDate(allWS_transport_to_buffer.getHasTimeslot().getEndDate());
	    		    		
	    		    		//end is start of next production step - transport estimation
	    		    		
	    		    		new_timeslot_to_book_buffer_place.setEndDate(String.valueOf(Long.parseLong(relevant_AllWS.getHasTimeslot().getStartDate())-myAgent.getTransport_estimation()));
		    		
	    		    		Operation buffer = new Operation();
	    				    buffer.setType("production");
	    				    buffer.setName("Puffern_Klein");
	    				    buffer.setAppliedOn(myAgent.getRepresented_Workpiece());
	    				    long duration = ((Long.parseLong(relevant_AllWS.getHasTimeslot().getStartDate())-myAgent.getTransport_estimation()-Long.parseLong(allWS_transport_to_buffer.getHasTimeslot().getEndDate())));
	    				    buffer.setAvg_Duration(duration/(1000*60));	//in minutes
	    				  		    
	    				    //use the same startdate_for_this_task as before
	    				    myAgent.addBehaviour(new RequestPerformer_transport(myAgent, buffer, new_timeslot_to_book_buffer_place, null, true));		//!last operation = false"  triggers the start of the production manager				    				    		
	    				    
	    				    //reset backwards scheduling
	    				    myAgent.getProductionManagerBehaviour().setBackwards_scheduling_activ(false);
	    				    break;
	    		    	
	    		    	}
	    				//no rescheduling necessary
	    				else {
	    					//schedule transport to next production step from buffer place
	    			    	//is the buffer step scheduled now? (after rescheduling)
	    					AllocatedWorkingStep buffer_place = myAgent.getLastProductionStepAllocated();	
	    					if(buffer_place.getHasOperation().getName().contains("Puffer") || buffer_place.getHasOperation().getName().contains("Nachbearbeitung")) {
	        		    		//AllocatedWorkingStep next_production_step = myAgent.get_BEFORE_LastProductionStepAllocated();
	    						allWS_buffer_place = myAgent.getLastProductionStepAllocated();	    								
	        					Location location_buffer_place = (Location)allWS_transport_to_buffer.getHasOperation().getEndState();
	        					this.location_buffer_place = location_buffer_place;
	        					
	        		    		//long startdate_for_this_task_2 = Long.parseLong(relevant_AllWS.getHasTimeslot().getStartDate()) - myAgent.getTransport_estimation();
	        		    		//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+" next_production_step = "+relevant_AllWS.getHasOperation().getName()+" @ resource =  "+relevant_AllWS.getHasOperation().getName()+"  order should start at: "+myAgent.SimpleDateFormat.format(startdate_for_this_task_2));
	        		    		
	        		    		
	        		    		Transport_Operation transport_operation2 = new Transport_Operation();									//
	        					transport_operation2.setType("transport");					
	        					//Location location_buffer_place = buffer_place.getHasResource().getHasLocation();
	        					Location location_next_production_step = relevant_AllWS.getHasResource().getHasLocation();
	        					
	        					transport_operation2.setStartState(location_buffer_place);
	        					transport_operation2.setEndState(location_next_production_step);
	        					
	        					//Name = Start_Ziel in format  X;Y_DestinationResource
	        					transport_operation2.setName(location_buffer_place.getCoordX()+";"+location_buffer_place.getCoordY()+"_"+relevant_AllWS.getHasResource().getName());
	        					transport_operation2.setAppliedOn(myAgent.getRepresented_Workpiece());
	        					
	        					Timeslot timeslot_to_book_transport_to_production = new Timeslot();
	        		    		//start is the end of the buffering step
	        					timeslot_to_book_transport_to_production.setStartDate(allWS_buffer_place.getHasTimeslot().getStartDate());	    		
	        		    		//end is start of production step		
	        					timeslot_to_book_transport_to_production.setEndDate(relevant_AllWS.getHasTimeslot().getStartDate());
	        		    		clock = System.currentTimeMillis()+2*myAgent.getProductionManagerBehaviour().reply_by_time;
	        		    		myAgent.addBehaviour(new RequestPerformer_transport(myAgent, transport_operation2, timeslot_to_book_transport_to_production, null, true));		//error step = true --> production manager is not started		    			    		
	        		    		//count_of_allocated_working_steps_before_new_task_is_scheduled = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size();
	        		    		step = 4;
	        		    		System.out.println("DEBUG___step = 4");
	        		    		this.block(5);
	        		    		break;
	        		    		
	    					}else {
	    						break;
	    						
	    					}
	    					
	    		    	}
	    			}
	    			//step not correct --> try next
	    			else {
	    				
	    			}
		    	
		   		//AllocatedWorkingStep buffer_place = myAgent.getLastProductionStepAllocated();
		    	// if(count_of_allocated_working_steps_before_new_task_is_scheduled<myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()) {
	    			
		    	//}
	    		//block();
		    	
		    	}//for loop	    		
	    	//}
		    	if(System.currentTimeMillis()>= clock) {
    				System.out.println("DEBUG______ERROR clock is over ");
    				step = 5;
    				break;
    			}
	    	this.block(10);
	    	break;
	    case 4:	  	  
	    	if(System.currentTimeMillis()>=clock) {
	    		Timeslot new_timeslot_to_book_production = new Timeslot();
	    		//is the transport from buffer to production already arranged?
			//get transport to production
    		for(int i = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1;i>=0;i--) {
    			AllocatedWorkingStep allWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(i);
    			//System.out.println("DEBUG_________________allWS: "+allWS.getID_String()+" "+allWS.getHasOperation().getName());
    			Boolean doLocationsmatch_end = false;
    			Boolean doLocationsmatch_start = false;
    			if(allWS.getHasOperation().getType().equals("transport")) {
    				
    				doLocationsmatch_end = myAgent.doLocationsMatch((Location)allWS.getHasOperation().getEndState(), relevant_AllWS.getHasResource().getHasLocation());    		     			
    				doLocationsmatch_start = myAgent.doLocationsMatch((Location)allWS.getHasOperation().getStartState(), location_buffer_place);
    			}
    			if(doLocationsmatch_start && doLocationsmatch_end) {	//correct step found
	    			
    				//Does the production step have to be rescheduled? (Workpiece cannot arrive at the production step as planned)   				
	    	    	if(myAgent.getProductionManagerBehaviour().isBackwards_scheduling_activ()) {
	    	    		reschedulingWasActive = true;
	    	    		//start is determined by scheduled transport operation
	    	    		new_timeslot_to_book_production.setStartDate(allWS.getHasTimeslot().getEndDate());
	    	    		//end is random end TBD
	    	    		new_timeslot_to_book_production.setEndDate(String.valueOf(Long.parseLong(allWS.getHasTimeslot().getEndDate())+myAgent.getTime_until_end()));
	    	    		

	    	    		myAgent.addBehaviour(new RequestPerformer_transport(myAgent, relevant_AllWS.getHasOperation(), new_timeslot_to_book_production, null, true));		//error step = true --> production manager is not started		    	
	    	    		
	    			    //reset backwards scheduling
	    			    myAgent.getProductionManagerBehaviour().setBackwards_scheduling_activ(false);
	    			    
	    	    	
	    	    	}
	    	    	//no rescheduling of production necessary (anymore)
	    	    	else{
	    				if(reschedulingWasActive) { //wait that production step is rescheduled
							
	    	    			if(myAgent.check_if_element_of_production_plan(myAgent.getLastProductionStepAllocated()) && !waitForBuffer) {
	    	    				

		    			    	//in that case we also need to reschedule to buffer place
		    			    		//the buffer place is not cancelled yet
										//find step to be cancelled, remove it from Workplan and cancel at resource
									 @SuppressWarnings("unchecked")
									Iterator<AllocatedWorkingStep> iter = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
									  while(iter.hasNext()) {
										  AllocatedWorkingStep current_step = iter.next();
										 
										  if(current_step.getID_String().equals(allWS_buffer_place.getID_String())) {																			
											iter.remove();
											break;
										}
									}
		    			    		myAgent.cancelAllocatedWorkingSteps(allWS_buffer_place);
		    			    		
			    			    Timeslot new_timeslot_to_book_buffer_place = new Timeslot();
		    		    		//start is determined by scheduled transport operation
		    		    		new_timeslot_to_book_buffer_place.setStartDate(allWS_transport_to_buffer.getHasTimeslot().getEndDate());
		    		    		
		    		    		//end is start of next production step - transport estimation
		    		    		
		    		    		new_timeslot_to_book_buffer_place.setEndDate(String.valueOf(Long.parseLong(myAgent.getLastProductionStepAllocated().getHasTimeslot().getStartDate())-myAgent.getTransport_estimation()));
		
		    		    		
		    		    		Operation buffer = new Operation();
		    				    buffer.setType("production");
		    				    buffer.setName("Puffern_Klein");
		    				    buffer.setAppliedOn(myAgent.getRepresented_Workpiece());
		    				    long duration = ((Long.parseLong(myAgent.getLastProductionStepAllocated().getHasTimeslot().getStartDate())-myAgent.getTransport_estimation()-Long.parseLong(allWS_transport_to_buffer.getHasTimeslot().getEndDate())));
		    				    buffer.setAvg_Duration(duration/(1000*60));	//in minutes
		    				  		    
		    				    //use the same startdate_for_this_task as before
		    				    myAgent.addBehaviour(new RequestPerformer_transport(myAgent, buffer, new_timeslot_to_book_buffer_place, null, true));		//!last operation = false"  triggers the start of the production manager				    				    		
		    				    waitForBuffer = true;
		    				    break;
		    				    //the buffer has been scheduled
	    	    			}else if(myAgent.getLastProductionStepAllocated().getHasResource().getName().contains("Puffer") && waitForBuffer){
	    	    				
	    	    				myAgent.getProductionManagerBehaviour().sortWorkplanChronologically();
	    	    	    		step = 5;
	    	    	    		
	    	    	    		//restart production manager for next "official" production step
	    	    				myAgent.getProductionManagerBehaviour().setStep(0);
	    	    				myAgent.getProductionManagerBehaviour().restart();
	    	    				break;
	    	    			}else {
	    	    				break;
	    	    			}
	    	    			
	    	    		}
						//rescheduling was not active
						else {	//bookBufferProcedure is done
		    	    		myAgent.getProductionManagerBehaviour().sortWorkplanChronologically();
		    	    		step = 5;
		    	    		
		    	    		//restart production manager for next "official" production step
		    				myAgent.getProductionManagerBehaviour().setStep(0);
		    				myAgent.getProductionManagerBehaviour().restart();
		    				myAgent.printoutWorkPlan();
	    	    		}
						
	    	    		
	    	    	}
	    			
		    		break;//leave the for loop
		    	}//if
	    		//locations dont match
	    		else {
		    			//System.out.println("DEBUG____________ERROR---> expected: end location of operation "+allWS.getHasOperation().getName()+ " "+((Transport_Operation)allWS.getHasOperation()).getHasEndLocation().getCoordX()+";"+((Transport_Operation)allWS.getHasOperation()).getHasEndLocation().getCoordY()+" does not equal = "+relevant_AllWS.getHasResource().getHasLocation().getCoordX()+";"+relevant_AllWS.getHasResource().getHasLocation().getCoordY());
		    		}
	    		
    		}//for loop
	    	}
			
    		
	    	//if(count_of_allocated_working_steps_before_new_task_is_scheduled<myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()) {
	    		
	    	//}    	
	    	break;
	    	}//switch case 
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return step == 5;
	}
	

}
