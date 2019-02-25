package agentPro_Prototype_WorkpieceAgent_Behaviours;


import java.util.Date;
import java.util.Iterator;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Cancellation;
import agentPro.onto.Operation;
import agentPro.onto.OrderedOperation;
import agentPro.onto.Transport_Operation;
import agentPro.onto.WorkPlan;
import agentPro.onto._SendCancellation;
import agentPro_Prototype_Agents.WorkpieceAgent;
import agentPro_Prototype_ResourceAgent.RequestDatabaseEntryBehaviour;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import support_classes.GanttDemo1;

import org.jfree.ui.RefineryUtilities;

/*
 * Determines the next step to be performed and starts the CFP Behaviour. If there are no more steps needed
 * it starts the CFP Behaviour for the transport to the warehouse and contacts the order agent.
 */
public class ProductionManagerBehaviour extends Behaviour{
	private static final long serialVersionUID = 1L;
	private String logLinePrefix = ".ProductionManager ";
	private WorkpieceAgent myAgent;
	private int step = 0;
	public long reply_by_time = 450; //reply within 0,5 seconds
	
	//private JSONObject workpiece;	
	//private double average_speed = 1; //m/s
	
	//database
	/*
	private String nameOfMES_Data = "MES_Data";
	private String columnNameOfOperation = "Operation";
	private String columnNameOfResource = "Ressource";
	private String columnNameOfResource_ID = "Ressource_ID";
	private String columnNameOfPlanStart = "PlanStart";
	private String columnNameOfPlanEnd = "PlanEnde";
	private String columnNameAuftrags_ID = "Auftrags_ID";
	private String columnNameOperation_Type = "Operation_Type";
	private String columnNameOfStarted = "Started";
	//private String columnNameFinished = "Finished";
	//private String columnNameOfIstStart = "IstStart";
	//private String columnNameOfIstEnde = "IstEnde";
	 * 
	 */
	private boolean backwards_scheduling_activ = false;

	

	public ProductionManagerBehaviour(WorkpieceAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;	
		
		//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" started");
	}
	
	@Override
	public void action() {
		switch(step){
		
		case 0:
		
			int position_next_step = 0;
			
			//check which step is needed
			//TBD if this can be handled better
			int number_of_finished_production_steps = determine_number_of_finished_production_steps();		//allocated and finished
			int number_of_planned_production_steps = determine_number_of_planned_production_steps();		//allocated but not finished
			
			position_next_step = number_of_finished_production_steps+number_of_planned_production_steps;	// e.g. one finished step and 1 planned step --> size = 2, position in workplan array = 2 (3rd position)
													
			
			//determine needed operation
			Operation requested_operation = determineRequestedOperation(position_next_step);
			//System.out.println("DEBUG_________________requestedOperation = "+requested_operation.getName()+" position_next_step = "+position_next_step+" = number_of_finished_production_steps "+number_of_finished_production_steps+" number_of_planned_production_steps "+number_of_planned_production_steps);
			requested_operation.setAppliedOn(myAgent.getRepresented_Workpiece());
			//is it the last operation?
			Boolean last_operation = determineLastOperation(position_next_step);

			long startdate_for_this_task = Long.parseLong(myAgent.getOrderPos().getStartDate());
			
			//does this step have to consider a successor contraint?
			String necessary_resource_agent_for_this_step = determineFollowUpOperationContraintOfLastProductionOperationScheduled(position_next_step);
			if(necessary_resource_agent_for_this_step != null) {
				 myAgent.addBehaviour(new RequestPerformer(myAgent, requested_operation, startdate_for_this_task, last_operation, false, necessary_resource_agent_for_this_step));
			}else {
				 myAgent.addBehaviour(new RequestPerformer(myAgent, requested_operation, startdate_for_this_task, last_operation, false));
			}


			
			 
			 step = 1;
			 block(); //blocks until restarted by Receive Proposal Behaviour or incoming message
			 break;
			
		case 1:
			if(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()>1 && ((AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1)).getHasOperation().getType().equals("transport")) {
				AllocatedWorkingStep allWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1);
				if(((Transport_Operation) allWS.getHasOperation()).getHasEndLocation().getCoordX() == 100 && ((Transport_Operation) allWS.getHasOperation()).getHasEndLocation().getCoordY() == 100) {
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
				sortWorkplanChronologically();
				//TBD what needs to be done when finished
				 myAgent.addBehaviour(new RequestDatabaseEntryBehaviour(myAgent));   
				 /*
				try {
					myAgent.addDataToDatabase("workpiece", myAgent.getWorkplan());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+logLinePrefix+" order for Workpiece "+myAgent.getLocalName()+" finished");
				
				//send inform_done message to order Agent
				
				ACLMessage inform_done = new ACLMessage(ACLMessage.INFORM);
				inform_done.addReceiver(myAgent.getOrderAgent());
				inform_done.setContent("Inform_Done for "+myAgent.getLocalName());	
				inform_done.setConversationId(myAgent.getConversationID_forOrderAgent());			
				myAgent.send(inform_done);		
					//AllocatedWorkingStep allWS1 = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-1);
					//AllocatedWorkingStep allWS2 = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().size()-2);
					
					//System.out.println("DEBUG____________Last element_"+allWS1.getHasOperation().getName()+ " before last element "+allWS2.getHasOperation().getName());
					myAgent.printoutWorkPlan();
			       final GanttDemo1 demo = new GanttDemo1("Workplan_"+myAgent.getLocalName(), myAgent.getWorkplan());
			        demo.pack();
			        RefineryUtilities.centerFrameOnScreen(demo);
			        demo.setVisible(false);
				
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
			    	if(orOp.getHasOperation().getName().equals(allWS.getHasOperation().getName())){		//find the operation in question and check for follow up constraint
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

	private Operation determineRequestedOperation(int position_next_step) {
		
		Operation reqOp = new Operation();
		
		OrderedOperation orOp = (OrderedOperation) myAgent.getProdPlan().getConsistsOfOrderedOperations().get(position_next_step);
		reqOp = orOp.getHasOperation();	
		return reqOp;
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
	
	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return step == 4;
	}

	/*
	public void cancelAllocatedWorkingSteps(AllocatedWorkingStep allWorkingStep) {
    		//create ontology content
			_SendCancellation sendCancellation = new _SendCancellation();
			Cancellation cancellation = new Cancellation();
			cancellation.addConsistsOfAllocatedWorkingSteps(allWorkingStep);
			sendCancellation.setHasCancellation(cancellation);			
			Action content = new Action(myAgent.getAID(),sendCancellation);
			
			//create ACL Message				
			ACLMessage cancel_acl = new ACLMessage(ACLMessage.CANCEL);
			cancel_acl.setLanguage(myAgent.getCodec().getName());
			cancel_acl.setOntology(myAgent.getOntology().getName());	
			AID receiver = new AID();
			if(allWorkingStep.getHasResource()!= null) {
				receiver.setLocalName(allWorkingStep.getHasResource().getName());
				cancel_acl.addReceiver(receiver);
				
				//ontology --> fill content
				try {
					myAgent.getContentManager().fillContent(cancel_acl, content);
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				myAgent.send(cancel_acl);
				System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" cancellation for step "+allWorkingStep.getHasOperation().getName()+" sent to receiver "+receiver.getLocalName()+" with content "+cancel_acl.getContent());
				
			}else {
				System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" no resource found for receiving cancellation for step "+allWorkingStep.getHasOperation().getName());
				
			}
			
			
    		//myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().remove(i);
			
			//operations_to_be_removed.add(allWorkingStep.getHasOperation().getName());
    		//i++;
			
			//delete step in the database not necessary --> can be updated	
	}
	*/
	public boolean isBackwards_scheduling_activ() {
		return backwards_scheduling_activ;
	}

	public void setBackwards_scheduling_activ(boolean backwards_scheduling_activ) {
		this.backwards_scheduling_activ = backwards_scheduling_activ;
		System.out.println("DEBUG____________________________________________WWWWWWWWWWWEEEEEEEEEE@@@@@@@@@@@@@@@@@@@@@@@@@ set to "+backwards_scheduling_activ);
	}

}
