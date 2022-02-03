package agentPro_Prototype_ResourceAgent;

import java.util.Date;
import java.util.Iterator;

import org.jfree.ui.RefineryUtilities;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Inform_ArrivalAndDeparture;
import agentPro.onto.Inform_Scheduled;
import agentPro.onto._SendInform_ArrivalAndDeparture;
import agentPro.onto._SendInform_Scheduled;
import agentPro_Prototype_Agents.ResourceAgent;
import agentPro_Prototype_Agents._Agent_Template;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import support_classes.Interval;
import support_classes.XYTaskDatasetDemo2;

public class ReceiveInformWorkpieceDepartureBehaviour extends CyclicBehaviour{
	
	private static final long serialVersionUID = 1L;
	private ResourceAgent myAgent;
	private String logLinePrefix = ".ReceiveInformWorkpieceDepartureBehaviour ";
	
	public ReceiveInformWorkpieceDepartureBehaviour(ResourceAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
		
	}
	
	@Override
	public void action() {
		
		// Receive message

		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate mt2 = MessageTemplate.MatchConversationId("Inform_Departure");	
        MessageTemplate mt_total1 = MessageTemplate.and(mt1,mt2);
        MessageTemplate mt3 = MessageTemplate.MatchConversationId("Inform_Arrival");
        MessageTemplate mt_total2 = MessageTemplate.and(mt1,mt3);
        
		ACLMessage inform_departure = myAgent.receive(mt_total1);
		ACLMessage inform_arrival = myAgent.receive(mt_total2);
		if (inform_departure != null) {
			//System.out.println("DEBUG_1");		
			//myAgent.printoutBusyIntervals();
			//myAgent.printoutFreeIntervals();
			//myAgent.printoutWorkPlan();
			
				Action act;
				String id_workpiece = "";
				String time_of_departure_process_started = "";
				int avg_pickUp = 0;
				String id_string = "";
				try {
					act = (Action) myAgent.getContentManager().extractContent(inform_departure);
					_SendInform_ArrivalAndDeparture send_infArrivalAndDep = (_SendInform_ArrivalAndDeparture) act.getAction();
					Inform_ArrivalAndDeparture infArrivalAndDep = send_infArrivalAndDep.getHasInform_Departure();
					id_string = infArrivalAndDep.getID_String();
					
					time_of_departure_process_started = infArrivalAndDep.getDepartureTime();
					avg_pickUp = infArrivalAndDep.getAvg_PickupTime();
					//System.out.println("DEBUG____RECEIVEINFORMWPARR avg_pickUp "+avg_pickUp);
					
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
				
				//find and increase the allocatedWS and the busy interval accordingly
				long old_enddate = 0;
				long new_enddate = 0;
				AllocatedWorkingStep edited_Step = null;
				@SuppressWarnings("unchecked")
				Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
			    while(it.hasNext()) {
			    	AllocatedWorkingStep allocWS = it.next();
			    	//if(allocWS.getHasOperation().getAppliedOn().getID_String().equals(id_workpiece) && allocWS.getHasOperation().getName().equals("")) {
			    	if(allocWS.getID_String().equals(id_string)) {
			    		old_enddate = Long.parseLong(allocWS.getHasTimeslot().getEndDate());
			    		new_enddate = Long.parseLong(time_of_departure_process_started)+avg_pickUp*60*1000;
			    		allocWS.getHasTimeslot().setEndDate(String.valueOf(new_enddate));	//d
			    		//System.out.println("DEBUG ______time_of_departure_process_started___"+time_of_departure_process_started+"  avg_pickUp  "+avg_pickUp*60*1000+" old_enddate "+old_enddate);
			    		if(_Agent_Template.simulation_enercon_mode && (myAgent.getLocalName().equals("Skoda_1_2") || myAgent.getLocalName().equals("Skoda_2_2") || myAgent.getLocalName().equals("Skoda_3_2"))) {
					    	sendIntervalToOtherAgent(allocWS);
					    }
			    		
			    		edited_Step = allocWS;
			    	
			    	}
			    }
			    //find busy interval which has the old_enddate as upperBound
			    //replace it with a new busy interval that has the time of departure as upperBound (and the same old lower Bound)
			    Interval new_busy_interval = new Interval();
			    for(int i = 0;i<myAgent.getBusyInterval_array().size();i++) {
			    	//System.out.println("DEBUG______________myAgent.getBusyInterval_array().get(i).upperBound()_"+myAgent.getBusyInterval_array().get(i).upperBound()+" = old_enddate "+old_enddate);
			    	if(myAgent.getBusyInterval_array().get(i).upperBound() == old_enddate) {
			    		//check whether the new busy interval is possible or if the new enddate is within an existing busy interval
			    		//new_busy_interval = new Interval (myAgent.getBusyInterval_array().get(i).lowerBound(), new_enddate, false);
			    		//11.06.19 neu: weiteres busy interval für blocked state
			    		new_busy_interval = new Interval (myAgent.getBusyInterval_array().get(i).upperBound(), new_enddate, false);
			    		new_busy_interval.setId(myAgent.getBusyInterval_array().get(i).getId()+"_waitingForDeparture");
			    		//System.out.println("DEBUG_______RECEIVE INFWP ARR__NEW BUSY INTERVAL  "+new_busy_interval.toString());
			    			//look at the next busy interval that behind in the array and has not been checked yet (if it exists)--> they are sorted chronologically
			    		if(i+1<myAgent.getBusyInterval_array().size()) {
			    			Interval interval_to_be_checked = myAgent.getBusyInterval_array().get(i+1);		    			
		    				if(interval_to_be_checked.intersection(new_busy_interval).getSize()>1) { //more than the Bound is shared
		    					System.out.println(_Agent_Template.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" Interval cannot be increased to that size. Busy interval "+interval_to_be_checked.toString()+" conflicts. ");
		    					//Better ErrorHandling TBD
		    					//TODO
		    					//if this is not possible, the resource must inform the workpiece, that it has to leave before X
		    					//the workpiece might have to arrange a buffer place in that case or follow another routine to fix the issue
		    				}else if(interval_to_be_checked.intersection(new_busy_interval).getSize()==1){ //now the free interval can be deleted 
		    					for(int j = 0; j<myAgent.getFree_interval_array().size();j++) {
		    						Interval free_interval_old = myAgent.getFree_interval_array().get(j);
		    						//the lower bound of the free interval was the same as the upperbound of the old busy interval
		    						if(free_interval_old.lowerBound() == old_enddate) {
		    							myAgent.getFree_interval_array().remove(j);
		    						}
		    					}
		    				}else { //no intersection
		    					addNewBusyIntervalAndReduceFreeInterval(i, new_busy_interval, old_enddate);
		    				}
			    		}else { //no intersection because no other element exists
			    			addNewBusyIntervalAndReduceFreeInterval(i, new_busy_interval, old_enddate);
			    					
			    				}		
			    	}
			    }
			    
			    
			     //create GANTT Chart
			    //System.out.println("DEBUG__ReceiveInformWPDeparture__"+myAgent.getLocalName()+id_workpiece);
			    if(id_workpiece.equals("B_1.1")&&myAgent.getLocalName().equals("QS")) {
			    	 XYTaskDatasetDemo2 demo = new XYTaskDatasetDemo2(
				                "JFreeChart : XYTaskDatasetDemo2.java", myAgent.getWorkplan(), myAgent.getLocalName());
				        demo.pack();
				        RefineryUtilities.centerFrameOnScreen(demo);
				        demo.setVisible(false);		
			    }
				
			        //System.out.println("DEBUG_2");		
			    	//myAgent.printoutBusyIntervals();
					//myAgent.printoutFreeIntervals();
					//myAgent.printoutWorkPlan();
					
		     //add to database
			    
			    if(_Agent_Template.simulation_enercon_mode) {
			    //}else if(!myAgent.simulation_mode){
			    }else {
			    	 myAgent.addBehaviour(new RequestDatabaseEntryBehaviour(myAgent, edited_Step));  
			    }
			    
			    myAgent.getReceiveCFPBehav().setReservation_lock(false); // reactivate Receive CFP Behaviour
				myAgent.getReceiveCFPBehav().getProposals().clear();
				myAgent.getReceiveCFPBehav().getProposed_slots().clear(); // erase slots
				myAgent.getReceiveCFPBehav().restart();  //new
				
			    /*
			    myAgent.getReceiveCFPBehav().done();
				ReceiveCFPBehaviour receiveCFPBehav = new ReceiveCFPBehaviour(myAgent);
				myAgent.setReceiveCFPBehav(receiveCFPBehav);
				myAgent.addBehaviour(receiveCFPBehav);
		     */
		     /*
		        try {
					myAgent.addDataToDatabase("resource", myAgent.getWorkplan());
				} catch (SQLException e) {
					e.printStackTrace();				
				}
			*/
		}else if(inform_arrival != null) {
			//System.out.println("DEBUG_3");		
			//myAgent.printoutBusyIntervals();
			//myAgent.printoutFreeIntervals();
			//myAgent.printoutWorkPlan();
			
			Action act;
			String id_workpiece = "";
			String time_of_pick_up_finished_and_Work_can_start = "";
			int avg_Pickup = 0;
			try {
				act = (Action) myAgent.getContentManager().extractContent(inform_arrival);
				_SendInform_ArrivalAndDeparture send_infArrivalAndDep = (_SendInform_ArrivalAndDeparture) act.getAction();
				Inform_ArrivalAndDeparture infArrivalAndDep = send_infArrivalAndDep.getHasInform_Departure();
				id_workpiece = infArrivalAndDep.getID_String();
				time_of_pick_up_finished_and_Work_can_start = infArrivalAndDep.getArrivalTime();
				avg_Pickup = infArrivalAndDep.getAvg_PickupTime();
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
			
			//find and Decrease / move the allocatedWS and the busy interval accordingly
			long old_startdate = 0;
			long old_enddate = 0;
			//AllocatedWorkingStep new_ALLWS = null;
			long new_startdate = 0;
			long new_enddate = 0;
			AllocatedWorkingStep edited_step = null;
			@SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
		    while(it.hasNext()) {
		    	AllocatedWorkingStep allocWS = it.next();
		    	long lengthOfAllocWSBeforeChange = (long) allocWS.getHasTimeslot().getLength();
		    	if(allocWS.getHasOperation().getAppliedOn().getID_String().equals(id_workpiece)) {
		    		old_startdate = Long.parseLong(allocWS.getHasTimeslot().getStartDate()); //busy interval included 2x pick up
		    		old_enddate = Long.parseLong(allocWS.getHasTimeslot().getEndDate());
		    		new_startdate = Long.parseLong(time_of_pick_up_finished_and_Work_can_start) - avg_Pickup*60*1000;
		    		allocWS.getHasTimeslot().setStartDate(String.valueOf(new_startdate));		//alloc as well
		    		//14.06.2018 NEW (see below) 18.06. pick_up + working time + pick_up = blocked
		    		//new_enddate = Long.parseLong(time_of_pick_up_finished_and_Work_can_start)+ (long)(allocWS.getHasOperation().getAvg_Duration()*60*1000) + avg_Pickup*60*1000;
		    		new_enddate = Long.parseLong(time_of_pick_up_finished_and_Work_can_start)+ lengthOfAllocWSBeforeChange + avg_Pickup*60*1000;
		    		System.out.println("DEBUG____________allocWS.getHasTimeslot().getLength()  "+allocWS.getHasTimeslot().getLength());
		    		allocWS.getHasTimeslot().setEndDate(String.valueOf(new_enddate));
		    		//new_ALLWS = allocWS;
		    		edited_step = allocWS;
		    		//System.out.println("DEBUG_____________RECEIVE INF Arrival -----> allocWS.getHasTimeslot().getStartDate()"+allocWS.getHasTimeslot().getStartDate()+" allocWS.getHasTimeslot().getEndDate()  "+allocWS.getHasTimeslot().getEndDate());
		    	}
		    }
		    //find busy interval which has the old_startdate as lowerBound
		    //replace it with a new busy interval that has the time of arrival as lowerBound (and the same old upper Bound)
		    //14.06.2018 It should also adjust the upper bound --> because production can start earlier
		    for(int i = 0;i<myAgent.getBusyInterval_array().size();i++) {
		    	if(myAgent.getBusyInterval_array().get(i).lowerBound() == old_startdate) {
		    		//check whether the new busy interval is possible or if the new startdate is within an existing busy interval
		    		
		    		//Interval new_busy_interval = new Interval (Long.parseLong(time_of_arrival), myAgent.getBusyInterval_array().get(i).upperBound(), false);
		    			//new: use the length of the old one + the new start date
		    		//Interval new_busy_interval = new Interval (Long.parseLong(time_of_arrival), Long.parseLong(time_of_arrival)+myAgent.getBusyInterval_array().get(i).getSize(), false);
		    		Interval new_busy_interval = new Interval (new_startdate, new_enddate, false);	
		    		new_busy_interval.setId(myAgent.getBusyInterval_array().get(i).getId());
		    		
		    			//look at the busy interval that is BEFORE in the array and has not been checked yet (if it exists)--> they are sorted chronologically
		    		if(i-1>=0) {
		    			Interval interval_to_be_checked = myAgent.getBusyInterval_array().get(i-1);		    			
	    				if(interval_to_be_checked.intersection(new_busy_interval).getSize()>1) { //more than the Bound is shared
	    					System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" Interval cannot be decreased to that size. Busy interval "+interval_to_be_checked.toString()+" conflicts. ");
	    					//Better ErrorHandling TBD
	    					
	    					//if this is not possible, the resource must inform the workpiece, that it has to leave before X
	    					//the workpiece might have to arrange a buffer place in that case or follow another routine to fix the issue
	    				}else if(interval_to_be_checked.intersection(new_busy_interval).getSize()==1){ //now the free interval can be deleted 
	    					for(int j = 0; j<myAgent.getFree_interval_array().size();j++) {
	    						Interval free_interval_old = myAgent.getFree_interval_array().get(j);
	    						//the upper bound of the free interval was the same as the lower bound of the old busy interval
	    						if(free_interval_old.upperBound() == old_startdate) {
	    							myAgent.getFree_interval_array().remove(j);
	    						}
	    						myAgent.getBusyInterval_array().set(i, new_busy_interval); //replace old busy interval
	    					}
	    				}else { //no intersection
	    					replaceOldBusyIntervalAndReduceFreeInterval2(i, new_busy_interval, old_startdate, old_enddate);
	    				}
		    		}else { //no intersection because no other element exists
		    			replaceOldBusyIntervalAndReduceFreeInterval2(i, new_busy_interval, old_startdate, old_enddate);
		    					
		    				}		
		    	}
		    	//System.out.println("DEBUG__________REC INF Arrival");
		    	//myAgent.printoutBusyIntervals();
		    }
		    
		  //create GANTT Chart
		    /*
			 XYTaskDatasetDemo2 demo = new XYTaskDatasetDemo2(
		                "JFreeChart : XYTaskDatasetDemo2.java", myAgent.getWorkplan(), myAgent.getLocalName());
		        demo.pack();
		        RefineryUtilities.centerFrameOnScreen(demo);
		        demo.setVisible(false);	
		        */
		       // System.out.println("DEBUG_4");		
		    	//myAgent.printoutBusyIntervals();
		    	//myAgent.printoutFreeIntervals();
				//myAgent.printoutWorkPlan();        
		        	
	     //add to database
		    if(myAgent.simulation_enercon_mode) {		    	  
		    }else {
		    	myAgent.addBehaviour(new RequestDatabaseEntryBehaviour(myAgent, edited_step));  
		    }
	 
		/*
	        try {
				myAgent.addDataToDatabase("resource", myAgent.getWorkplan());
			} catch (SQLException e) {
				e.printStackTrace();				
			}*/
		}
		else{
			block();
		}
	}

	private void sendIntervalToOtherAgent(AllocatedWorkingStep allocWS) {
		ACLMessage inform_scheduled_acl = new ACLMessage(ACLMessage.INFORM);
		Inform_Scheduled inform_scheduled = new Inform_Scheduled();	
		inform_scheduled.addConsistsOfAllocatedWorkingSteps(allocWS);
		AID res = new AID();
		String localName = "";
		if(myAgent.getLocalName().equals("Skoda_1_2")) {
			localName = "Skoda_1_1";
	    }else if(myAgent.getLocalName().equals("Skoda_2_2")) {
	    	localName = "Skoda_2_1";
	    }else if(myAgent.getLocalName().equals("Skoda_3_2")) {
	    	localName = "Skoda_3_1";
	    }
		res.setLocalName(localName);
		inform_scheduled_acl.addReceiver(res);
		inform_scheduled_acl.setProtocol("Connected_Resource");
		
		
		inform_scheduled_acl.setLanguage(myAgent.getCodec().getName());
		inform_scheduled_acl.setOntology(myAgent.getOntology().getName());

		_SendInform_Scheduled sendInformScheduled = new _SendInform_Scheduled();
		sendInformScheduled.setHasInform_Scheduled(inform_scheduled);
		
		Action content = new Action(this.getAgent().getAID(),sendInformScheduled);
		
		try {
			myAgent.getContentManager().fillContent(inform_scheduled_acl, content);
		} catch (CodecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myAgent.send(inform_scheduled_acl);	
			
		
	}

	private void addNewBusyIntervalAndReduceFreeInterval(int i, Interval new_busy_interval, long old_enddate) {
		//myAgent.getBusyInterval_array().set(i, new_busy_interval); //replace old busy interval
		myAgent.getBusyInterval_array().add(i+1, new_busy_interval); //add new busy interval
		//reduce free interval accordingly
		for(int j = 0; j<myAgent.getFree_interval_array().size();j++) {
			Interval free_interval_old = myAgent.getFree_interval_array().get(j);
			//the lower bound of the free interval was the same as the upperbound of the old busy interal
			if(free_interval_old.lowerBound() == old_enddate) {
				Interval free_interval_new = new Interval (new_busy_interval.upperBound(), free_interval_old.upperBound(), false);	//old upperbound stays the same
				myAgent.getFree_interval_array().set(j, free_interval_new);
			}
		}
		
	}
	private void replaceOldBusyIntervalAndReduceFreeInterval2(int i, Interval new_busy_interval, long old_startdate, long old_enddate) {
		myAgent.getBusyInterval_array().set(i, new_busy_interval); //replace old busy interval
		//reduce free interval accordingly
		for(int j = 0; j<myAgent.getFree_interval_array().size();j++) {
			Interval free_interval_old = myAgent.getFree_interval_array().get(j);
			//the lower bound of the free interval was the same as the upperbound of the old busy interal
			if(free_interval_old.upperBound() == old_startdate) {
				Interval free_interval_new = new Interval (free_interval_old.lowerBound(), new_busy_interval.lowerBound(),  false);	
				myAgent.getFree_interval_array().set(j, free_interval_new);
			}else if(free_interval_old.lowerBound() == old_enddate){
				Interval free_interval_new = new Interval (new_busy_interval.upperBound(), free_interval_old.upperBound(),  false);	
				myAgent.getFree_interval_array().set(j, free_interval_new);			
			}
		}
		
	}

}
