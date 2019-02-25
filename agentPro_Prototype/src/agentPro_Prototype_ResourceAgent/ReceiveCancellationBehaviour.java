package agentPro_Prototype_ResourceAgent;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;

import agentPro.onto.Operation;
import agentPro.onto.Transport_Operation;
import agentPro.onto.WorkPlan;
import agentPro.onto._SendCancellation;

import agentPro_Prototype_Agents.ResourceAgent;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import support_classes.Interval;

public class ReceiveCancellationBehaviour extends CyclicBehaviour{
	/**
	 * 
	 */
	private String logLinePrefix = ".ReceiveCancellationBehaviour ";
	private static final long serialVersionUID = 1L;
	private ResourceAgent myAgent;

	
	public ReceiveCancellationBehaviour(ResourceAgent resourceAgent) {
		this.myAgent = resourceAgent;
	}


	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
        //MessageTemplate mt2 = MessageTemplate.MatchConversationId(conversationID);	
        //MessageTemplate mt_total = MessageTemplate.and(mt1,mt2);
        
		ACLMessage cancel_message = myAgent.receive(mt);
		if (cancel_message != null) {
			System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+logLinePrefix+"cancellation received.");
		
			try {	

				Action act = (Action) myAgent.getContentManager().extractContent(cancel_message);
				_SendCancellation cancel_onto = (_SendCancellation) act.getAction();

				/*
				 * for each element the allocated Working step must be cancelled
				 */
				
				 @SuppressWarnings("unchecked")
					Iterator<AllocatedWorkingStep> it = cancel_onto.getHasCancellation().getConsistsOfAllocatedWorkingSteps().iterator();		 	
				    while(it.hasNext()) {
				    	AllocatedWorkingStep allWS = it.next();
				    	
				    	myAgent.removeAllocatedWorkingStepFromWorkPlanAndBusyIntervalsAndCreateFreeIntervals(allWS);
						if(myAgent.numberOfResourcesPossibleForCalculationOfSharedResourceProposal == 0 && myAgent.getRepresentedResource().getType().equals("transport")) {	//for crane agents
							for(DFAgentDescription a : myAgent.resourceAgents) {						
								  @SuppressWarnings("unchecked")
									Iterator<ServiceDescription> it2 = a.getAllServices();
								    while(it2.hasNext()) {
								    	ServiceDescription service_description = it2.next();
								    	if(service_description.getType().equals("shared_resource")) {
								    		myAgent.cancelAllocatedWorkingSteps(allWS, a.getName().getLocalName());
								    	}
								    }
							}
								
							
						}		
				    }
				    			    
			
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
			

			
		}else {
			block();
		}
		
	}
	/*
	public void removeAllocatedWorkingStepFromWorkPlanAndBusyIntervalsAndCreateFreeIntervals(
			AllocatedWorkingStep allWS) {
    	//find allWS in Workplan
    	//int counter = 0;
		Operation op = null;
		
		//find correct allWS
		//delete it in workplan and busy interval array
		//add it in free interval array
		
		 @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it_2 = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();		 	
		    while(it_2.hasNext()) {
		    	AllocatedWorkingStep allocWorkingstep = it_2.next();
		    	op = allocWorkingstep.getHasOperation();	
		    	String name_Workpiece = allocWorkingstep.getHasOperation().getAppliedOn().getID_String();
		    	Interval busy_interval = new Interval (Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate()), Long.parseLong(allocWorkingstep.getHasTimeslot().getEndDate()), false);
		    	
		    	//if operaiton name and workpiece ID are correct --> delete
		    	if(op.getName().equals(allWS.getHasOperation().getName()) && name_Workpiece.equals(allWS.getHasOperation().getAppliedOn().getID_String())) {	//find out the position of the relevant step			    	
		    		 //myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().remove(counter);
		    		it_2.remove();
		    		
		    		 //find the corresponding busy interval and delete it + add free interval
		    		 for(int i = 0;i < myAgent.getBusyInterval_array().size(); i++) {
		    			 if(myAgent.getBusyInterval_array().get(i).lowerBound() == busy_interval.lowerBound() && myAgent.getBusyInterval_array().get(i).upperBound() == busy_interval.upperBound()) {
		    				 System.out.println(myAgent.getLocalName()+" DEBUG________busy interval removed "+myAgent.SimpleDateFormat.format(myAgent.getBusyInterval_array().get(i).lowerBound())+" "+myAgent.SimpleDateFormat.format(myAgent.getBusyInterval_array().get(i).upperBound()));
		    				myAgent.getBusyInterval_array().remove(i);
		    			 	myAgent.getFree_interval_array().add(busy_interval); //TBD sorting and merging!!
		    			 }
		    		 }
		    		 //sortWorkplanChronologically();
		    		 sortFreeIntervalsChronologically();
		    		 //merging has to be done at least twice --> TBD if there is a better way
		    		 //if(myAgent.getLocalName().equals("Kran1")) {
		    			 mergeAdjacentFreeIntervals();
			    		 mergeAdjacentFreeIntervals();	 
		    		 //}
	    		 				    		 
		    	}
		    	//counter++;
		    }		
		
	}


	private void sortFreeIntervalsChronologically() {
		
		ArrayList <Interval> new_list = new ArrayList <Interval>();

		
		//check for every free interval
		
		for(Interval i : myAgent.getFree_interval_array()) {
			if(new_list.size()==0) {	//first element, just add
				new_list.add(i);
				
			}else {		//determine position where the new interval should be added
				int position = 0;
				for(int j = 0;j <new_list.size();j++) {
					if(new_list.get(j).lowerBound()>i.lowerBound()) {	//if the element's lower bound in the list is greater than the lower bound of the current element, do nothing
						
					}else {	//if the element's lower bound is lower (which means that the current element starts later!)  --> increase position by one, meaning: add behind that element
						position++;
					}
				}
				new_list.add(position,i);
			}
		}
		myAgent.setFree_interval_array(new_list);
		
	}


	
	private void mergeAdjacentFreeIntervals() {
		
		
		ArrayList <Interval> new_merged_list = new ArrayList <Interval>();
		for(int i = 0;i <= myAgent.getFree_interval_array().size()-1;i++) {	
			Interval free_interval_i_plus1 = null;
			Interval free_interval_i = myAgent.getFree_interval_array().get(i);
			
			//if there exists an element behind i (so i+1), get that element
			if(i<myAgent.getFree_interval_array().size()-1) {
				free_interval_i_plus1 = myAgent.getFree_interval_array().get(i+1);
			}else{ // i = size -1 --> e.g. i = 4, array size = 5 --> last element --> add the last element to the new list
				new_merged_list.add(free_interval_i);
				break; //last element was added --> leave the for loop
			}
		
			//if the intervals share a common bound (lower of i and upper of i-1) --> create a new one with lower bound i-1 and upper bound i
			if(free_interval_i.upperBound() == free_interval_i_plus1.lowerBound()) {
				Interval new_free_intervall = new Interval(free_interval_i.lowerBound(), free_interval_i_plus1.upperBound(), false);
				//System.out.println(myAgent.getLocalName()+" DEBUG________MERGED__________"+logLinePrefix+" free_interval_i.lowerBound() "+myAgent.SimpleDateFormat.format(free_interval_i.lowerBound())+" free_interval_i_plus1.upperBound() "+myAgent.SimpleDateFormat.format(free_interval_i_plus1.upperBound()));
				//myAgent.getFree_interval_array().add(i-1, new_free_intervall);
				new_merged_list.add(new_free_intervall);
				i++; //nächstes überspringen, da bereits in dem merged interval enthalten

			//this should not be needed (overlap of more than a shared bound)
			}else if(free_interval_i.intersection(free_interval_i_plus1).getSize()>0){
				Interval new_free_intervall = new Interval(free_interval_i.lowerBound(), free_interval_i_plus1.upperBound(), false);
				new_merged_list.add(new_free_intervall);
			}
			//no overlap
			else{
				new_merged_list.add(myAgent.getFree_interval_array().get(i));
			}
		}
		//set the new list as the free interval list
		myAgent.setFree_interval_array(new_merged_list);
		
		
	}*/
	
	

}
