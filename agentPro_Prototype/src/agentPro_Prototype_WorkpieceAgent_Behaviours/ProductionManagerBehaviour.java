package agentPro_Prototype_WorkpieceAgent_Behaviours;


import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import agentPro_Prototype_Agents.WorkpieceAgent;
import agentPro_Prototype_Agents._Agent_Template;
import agentPro_Prototype_ResourceAgent.RequestDatabaseEntryBehaviour;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import support_classes.GanttDemo1;
import webservice.ManufacturingOrderList;
import webservice.Webservice_agentPro;

import org.jfree.ui.RefineryUtilities;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Location;
import agentPro.onto.OrderedOperation;
import agentPro.onto.Production_Operation;
import agentPro.onto.Transport_Operation;
import agentPro.onto.WorkPlan;

/*
 * Determines the next step to be performed and starts the CFP Behaviour. If there are no more steps needed
 * it starts the CFP Behaviour for the transport to the warehouse and contacts the order agent.
 */
public class ProductionManagerBehaviour extends Behaviour{
	private static final long serialVersionUID = 1L;
	private String logLinePrefix = ".ProductionManager ";
	private WorkpieceAgent myAgent;
	private int step = 0;
	//public long reply_by_time = 2500; //reply within 450 ms
	
	//private JSONObject workpiece;	
	//private double average_speed = 1; //m/s

	//private boolean backwards_scheduling_activ = false;
	

	

	public ProductionManagerBehaviour(WorkpieceAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;	
		myAgent.startCoordinationProcess = System.currentTimeMillis();
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@ START     "+myAgent.startCoordinationProcess);
		//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" started");
	}
	
	@Override
	public void action() {
		
		switch(step){
		
		case 0:
			//System.out.println("DEBUG____________prod Manager at step = 0");
			
			int position_next_step = 0;
			
			//check which step is needed
			//TBD if this can be handled better
			int number_of_finished_production_steps = determine_number_of_finished_production_steps();		//allocated and finished
			int number_of_planned_production_steps = determine_number_of_planned_production_steps();		//allocated but not finished
			
			position_next_step = number_of_finished_production_steps+number_of_planned_production_steps;	// e.g. one finished step and 1 planned step --> size = 2, position in workplan array = 2 (3rd position)
			String name_of_last_operation = ((OrderedOperation)myAgent.getProdPlan().getConsistsOfOrderedOperations().get(myAgent.getProdPlan().getConsistsOfOrderedOperations().size()-1)).getHasProductionOperation().getName();
			if(myAgent.getLastProductionStepAllocated() != null && myAgent.getLastProductionStepAllocated().getHasOperation().getName().contentEquals(name_of_last_operation)) {
				if(WorkpieceAgent.transport_needed) {
					//arrangeTransportToWarehouse();
				}
				step = 2;
				break;
			}
			
			//determine needed operation
			Production_Operation requested_operation = determineRequestedOperation(position_next_step);
			//System.out.println("DEBUG_________________requestedOperation = "+requested_operation.getName()+" position_next_step = "+position_next_step+" = number_of_finished_production_steps "+number_of_finished_production_steps+" number_of_planned_production_steps "+number_of_planned_production_steps);
			requested_operation.setAppliedOn(myAgent.getRepresented_Workpiece());
			//is it the last operation?
			Boolean last_operation = determineLastOperation(position_next_step);
			long startdate_for_this_task = 0;
			Date d = new Date();
			if(_Agent_Template.simulation_enercon_mode) {
				startdate_for_this_task	= Long.parseLong(myAgent.getOrderPos().getStartDate());
			}else {
				
				try {
					d = _Agent_Template.SimpleDateFormat.parse(myAgent.getOrderPos().getStartDate());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startdate_for_this_task = d.getTime();
			}
			
			//does this step have to consider a successor constraint?
			String necessary_resource_agent_for_this_step = determineFollowUpOperationContraintOfLastProductionOperationScheduled(position_next_step);
			if(necessary_resource_agent_for_this_step != null) {
				 myAgent.addBehaviour(new RequestPerformer(myAgent, requested_operation, startdate_for_this_task, last_operation, necessary_resource_agent_for_this_step));
			}else {
				 myAgent.addBehaviour(new RequestPerformer(myAgent, requested_operation, startdate_for_this_task, last_operation));
			}


			
			 
			 step = 1;
			 block(); //blocks until restarted by Receive Proposal Behaviour or incoming message
			 break;
			
		case 1:
			if(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>1 && ((AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1)).getHasOperation().getType().equals("transport")) {
				AllocatedWorkingStep allWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1);
				if( ( (Location) allWS.getHasOperation().getEndState()).getCoordX() == 100 && ( (Location) allWS.getHasOperation().getEndState()).getCoordY() == 100) {
					step = 2;
					System.out.println("_________________________________________DEBUG____________________WHY NEEDED?");
				}else {
					block(); //blocks until restarted by Receive Proposal Behaviour (which starts step = 0) or incoming message (stays at step = 1)
					
					break;
				}
			}
			else {
				block(); //blocks until restarted by Receive Proposal Behaviour (which starts step = 0) or incoming message (stays at step = 1)
			
				break;
			}
			
			
		case 2:		//if all steps are performed
			
			//check whether the last step is the transport to warehouse outbound (100;100) step
			//AllocatedWorkingStep allWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1);
			//if(((Transport_Operation) allWS.getHasOperation()).getHasEndLocation().getCoordX() == 100 && ((Transport_Operation) allWS.getHasOperation()).getHasEndLocation().getCoordY() == 100) {
				//sort workplan
				//sortWorkplan();
			myAgent.EndCoordinationProcess = System.currentTimeMillis();
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@ END     "+myAgent.EndCoordinationProcess);
				sortWorkplanChronologically();
				//TBD what needs to be done when finished
				 myAgent.addBehaviour(new RequestDatabaseEntryBehaviour(myAgent));   
				 //System.out.println(_Agent_Template.printoutWorkPlan(myAgent.getWorkplan(), "WP1"));
				 /*
				try {
					myAgent.addDataToDatabase("workpiece", myAgent.getWorkplan());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				System.out.println(_Agent_Template.SimpleDateFormat.format(new Date())+" "+logLinePrefix+" order for Workpiece "+myAgent.getLocalName()+" finished");
				
				//send inform_done message to order Agent
				
				ACLMessage inform_done = new ACLMessage(ACLMessage.INFORM);
				inform_done.addReceiver(myAgent.getOrderAgent());
				inform_done.setContent(myAgent.getLocalName()+"_"+myAgent.startCoordinationProcess+"_"+myAgent.EndCoordinationProcess+"_"+myAgent.getOrderPos().getQuantity());	
				inform_done.setConversationId(myAgent.getConversationID_forOrderAgent());			
				myAgent.send(inform_done);		
					//AllocatedWorkingStep allWS1 = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1);
					//AllocatedWorkingStep allWS2 = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-2);
					
					//System.out.println("DEBUG____________Last element_"+allWS1.getHasOperation().getName()+ " before last element "+allWS2.getHasOperation().getName());
					_Agent_Template.printoutWorkPlan(myAgent.getWorkplan(), myAgent.getLocalName());
			       final GanttDemo1 demo = new GanttDemo1("Workplan_"+myAgent.getLocalName(), myAgent.getWorkplan());
			        demo.pack();
			        RefineryUtilities.centerFrameOnScreen(demo);
			        demo.setVisible(false);
			        
			        if(_Agent_Template.webservice_mode) {
			        	
			        	
			        	HttpClient client = HttpClient.newBuilder()	
								.version(HttpClient.Version.HTTP_1_1)			
								  .build();
			        	ManufacturingOrderList mol = Webservice_agentPro.addToManufacturingOrderList(myAgent.getWorkplan(), myAgent.getMo());		        	
						String body = Webservice_agentPro.buildSOAPBodyUpdateManufacturingOrder(mol);
						HttpRequest request = Webservice_agentPro.buildRequest(Webservice_agentPro.soapAction_updateOrder, body);
						//System.out.println("body WS "+body);
						String return_string = "Return String after Update Order:  ";
						try {
							return_string = return_string + client.sendAsync(request, BodyHandlers.ofString())
									.thenApply(HttpResponse::body).get();
						} catch (InterruptedException | ExecutionException e1) {			
							e1.printStackTrace();
						}
						//System.out.println(return_string);
			        }
				
				step = 3;
			//}
			
			
			
			
		case 3:
			block();			
		}
	}

	private String determineFollowUpOperationContraintOfLastProductionOperationScheduled(int position_next_step) {
		String name_of_resource = null;
		String needed_resource = null;
		AllocatedWorkingStep allWS = myAgent.getLastProductionStepAllocated();						//the last operation scheduled--> if that one had a constraint, we need to consider it now
		if(allWS != null) {
			int value = 0;
			 @SuppressWarnings("unchecked")
				Iterator<OrderedOperation> it = myAgent.getProdPlan().getAllConsistsOfOrderedOperations();
			    while(it.hasNext()) {
			    	OrderedOperation orOp = it.next();			
			    	if(orOp.getHasProductionOperation().getName().equals(allWS.getHasOperation().getName())){		//find the operation in question and check for follow up constraint
			    		if(orOp.getHasFollowUpOperation()) {
			    			value = orOp.getWithOperationInStep();										// if there is one, set the return value to the step number
			    		}
			    	}	    	
			    }
			if(value != 0) {
				name_of_resource = allWS.getHasResource().getName(); //this is Skoda_1_1 --> We need to book at Skoda_1_2
				needed_resource = name_of_resource.substring(0, 8)+2;
			}
		}

		return needed_resource;
	}

	public void sortWorkplanChronologically() {
		/*
	    @SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> ite = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
	    String printout = myAgent.getLocalName()+" DEBUG__________SORTING_____________";
	    while(ite.hasNext()) {		//checks for every allWS in Workplan
	    	AllocatedWorkingStep a = ite.next();	  
	    	printout = printout + " NEXT " + a.getHasTimeslot().getStartDate()+" - "+a.getHasOperation().getName();

	    }
		System.out.println(printout);
		*/
			WorkPlan wP_toBeSorted = new WorkPlan();
			
		    @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
		   
		    while(it.hasNext()) {		//checks for every allWS in Workplan
		    	AllocatedWorkingStep allocWorkingstep = it.next();	  
		    	long startdate_of_current_step = Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate());	//get the startdate of the step
		    	
		    	if(wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().size()>0) {	//not first element
			    	int position_to_be_added = 0;
			    	
			    	for(int i = 0;i<wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().size();i++) {	//check for every element already in the new list if the current one from WP has an earlier startdate
			    		//startdate of the element in the new list
			    		long startdate_step_in_to_be_sorted = Long.parseLong(((AllocatedWorkingStep) wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().get(i)).getHasTimeslot().getStartDate());
			    		//if the startdate of the element in the new list is smaller than the startdate of the current step, 
			    		//the current step has to be added afterwards    			
			    		if(startdate_step_in_to_be_sorted < startdate_of_current_step) {	
			    			position_to_be_added++; //so the position must be increased by one
			    		}else {
			    			//if not it can be added on that position and all behind are moved one position "to the right"
			    		}
			    	}
			    	wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().add(position_to_be_added, allocWorkingstep);
			    	
		    	}else {	//first element can just be added (list was still null)
		    		wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().add(allocWorkingstep);
		    		}

		    }
		    myAgent.setWorkplan(wP_toBeSorted);

		    /*
		    @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> iter = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
		    String printout2 = myAgent.getLocalName()+" DEBUG___________SORTING 2____________";
		    while(iter.hasNext()) {		//checks for every allWS in Workplan
		    	AllocatedWorkingStep b = iter.next();	  
		    	printout2 = printout2 + " NEXT " + b.getHasTimeslot().getStartDate()+" - "+b.getHasOperation().getName();

		    }
		
		    System.out.println(printout2);*/
	}
	

	private int determine_number_of_planned_production_steps() {
		int counter = 0;
		
	    @SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
	    while(it.hasNext()) {
	    	AllocatedWorkingStep allWorkingStep = it.next();
	    	if(!allWorkingStep.getIsFinished() && allWorkingStep.getHasOperation().getType().equals("production") && allWorkingStep.getIsErrorStep() == false) {		//only count production steps
	    		counter++;
	    	}
	    }
	
		return counter;
	}

	private Boolean determineLastOperation(int position_next_step) {
		OrderedOperation orOp = (OrderedOperation) myAgent.getProdPlan().getConsistsOfOrderedOperations().get(position_next_step);
		Boolean lastOperation = orOp.getLastOperation();
		
		return lastOperation;
	}

	private Production_Operation determineRequestedOperation(int position_next_step) {		
		OrderedOperation orOp = (OrderedOperation) myAgent.getProdPlan().getConsistsOfOrderedOperations().get(position_next_step);		
		return orOp.getHasProductionOperation();
	}

	private int determine_number_of_finished_production_steps() {
		int counter = 0;
		
		
	    @SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
	    while(it.hasNext()) {
	    	AllocatedWorkingStep allWorkingStep = it.next();
	    	if(allWorkingStep.getIsFinished() && allWorkingStep.getHasOperation().getType().equals("production") && allWorkingStep.getIsErrorStep() == false) {
	    		counter++;
	    	}
	    }

		return counter;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}
private void arrangeTransportToWarehouse() {
		
		
		Transport_Operation transport_operation = new Transport_Operation();									//
		transport_operation.setType("transport");
		transport_operation.setAppliedOn(myAgent.getRepresented_Workpiece());
		Location startlocation = new Location();
		
		//startlocation now needs to be the last element
			int sizeOfAllWSs = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size();
			//AllocatedWorkingStep LAST_alWS_Production = null;
			for(int i = sizeOfAllWSs; i>0 ; i--) {
				AllocatedWorkingStep alWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(i-1);
				if(alWS.getHasOperation().getType().equals("production")) {
					startlocation = alWS.getHasResource().getHasLocation();
										
					break;
					
				}
			}
			
			if(_Agent_Template.doLocationsMatch(startlocation, myAgent.getOrderPos().getHasTargetWarehouse().getHasLocation())) {
				_Agent_Template.printoutWorkPlan(myAgent.getWorkplan(), myAgent.getLocalName());
				myAgent.getProductionManagerBehaviour().setStep(2);
				myAgent.getProductionManagerBehaviour().restart();
			}else {
				transport_operation.setStartStateNeeded(startlocation);
				transport_operation.setEndState(myAgent.getOrderPos().getHasTargetWarehouse().getHasLocation());
				
				
				//Name = Start_Ziel in format  X;Y_DestinationResource
				transport_operation.setName(startlocation.getCoordX()+";"+startlocation.getCoordY()+"_"+myAgent.getOrderPos().getHasTargetWarehouse().getName());
				transport_operation.setBuffer_before_operation_start(2*60*60*1000); //e.g. 2 hours --> does not matter				
				myAgent.addBehaviour(new RequestPerformer_transport(myAgent, transport_operation, (long) 0, null, false));
			}
		
		
	}
	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return step == 4;
	}
/*
	public boolean isBackwards_scheduling_activ() {
		return backwards_scheduling_activ;
	}

	public void setBackwards_scheduling_activ(boolean backwards_scheduling_activ) {
		this.backwards_scheduling_activ = backwards_scheduling_activ;
		System.out.println("DEBUG____________________________________________WWWWWWWWWWWEEEEEEEEEE@@@@@@@@@@@@@@@@@@@@@@@@@ set to "+backwards_scheduling_activ);
	}
*/
}
