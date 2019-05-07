package agentPro_Prototype_WorkpieceAgent_Behaviours;

import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Inform_ArrivalAndDeparture;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Transport_Operation;
import agentPro.onto._SendInform_ArrivalAndDeparture;
import agentPro_Prototype_Agents.WorkpieceAgent;
import agentPro_Prototype_Agents._Agent_Template;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class InformWorkpieceArrivalAndDeparture_backup extends OneShotBehaviour {

	private static final long serialVersionUID = 1L;
	private WorkpieceAgent myAgent;
	private Transport_Operation requested_operation;
	private AllocatedWorkingStep allocatedWorkingStep_transport;

	public InformWorkpieceArrivalAndDeparture_backup(WorkpieceAgent myAgent, Transport_Operation requested_operation, AllocatedWorkingStep transport_step_to_give_to_Inform_Arrival) {
		this.myAgent = myAgent;
		this.requested_operation = requested_operation;
		allocatedWorkingStep_transport = transport_step_to_give_to_Inform_Arrival;
	}

	@Override
	public void action() {
		/*
		 * find the production resource that is the start of the "requested_operation" & the one that is the end of the requested_operation
		 * 	by determine location of any resource in the allocWS List and compare it to the start / end location of requested operation 
		 * get its name / their names for the ACL message receiver
		 */
		AllocatedWorkingStep allocWS_start = null;
		AllocatedWorkingStep allocWS_end = null;
		Location startlocation = (Location) requested_operation.getStartState();
		Location endlocation = (Location) requested_operation.getEndState();
		Boolean findWarehouseOutbound = _Agent_Template.doLocationsMatch(myAgent.getOrderPos().getHasTargetWarehouse().getHasLocation(),endlocation);
		Boolean findWarehouseInbound = _Agent_Template.doLocationsMatch(myAgent.getLocationOfStartingWarehouse(),startlocation);
		

		 @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
		    while(it.hasNext()) {
		    	AllocatedWorkingStep this_step_in_allocated_working_steps = it.next();
		    	Location location_of_this_res = this_step_in_allocated_working_steps.getHasResource().getHasLocation();
		    	Boolean doLocationsMatch_startlocation = _Agent_Template.doLocationsMatch(location_of_this_res, startlocation);
		    	Boolean doLocationsMatch_endloaction = _Agent_Template.doLocationsMatch(location_of_this_res, endlocation);
		    	
		    	if(this_step_in_allocated_working_steps.getHasResource().getType().equals("production") && doLocationsMatch_startlocation) {
		    		allocWS_start = this_step_in_allocated_working_steps;
		    	}
		    	if(!findWarehouseOutbound && this_step_in_allocated_working_steps.getHasResource().getType().equals("production") && doLocationsMatch_endloaction) {
		    		allocWS_end = this_step_in_allocated_working_steps;
		    		
		    	}
		    }
		
		
		
		    //1. for production resource that the workpiece exits
		    //2. for prod res that the workpiece arrives at
		    int limit = 2;
		    if(findWarehouseOutbound || findWarehouseInbound) {
		    	limit = 1;		    	
		    }
		   // System.out.println("DEBUG______"+myAgent.getLocation().getCoordX()+";"+myAgent.getLocation().getCoordY()+"______"+findWarehouseOutbound+"_____"+findWarehouseInbound+"____limit "+limit);
		 for(int i = 0;i<limit;i++) {
			//create ACL Message				
				ACLMessage inform_acl = new ACLMessage(ACLMessage.INFORM);
				inform_acl.setLanguage(myAgent.getCodec().getName());
				inform_acl.setOntology(myAgent.getOntology().getName());
				AID receiver = new AID();
				
			//create ontology contents
				_SendInform_ArrivalAndDeparture sendInform_departure = new _SendInform_ArrivalAndDeparture();
				Inform_ArrivalAndDeparture inform_arrivalAndDeparture = new Inform_ArrivalAndDeparture();
				String action = "";
				if(i == 0 && allocWS_start != null) {
					inform_arrivalAndDeparture.setDepartureTime(String.valueOf(Long.parseLong(allocatedWorkingStep_transport.getHasTimeslot().getStartDate())));
					inform_arrivalAndDeparture.setAvg_PickupTime(requested_operation.getAvg_PickupTime());
					receiver.setLocalName(allocWS_start.getHasResource().getName());
					inform_acl.setConversationId("Inform_Departure");
					action = "departure";
				}else if(allocWS_end != null) { //will be null in case of last production step (after that only the warehouse is left)
					inform_arrivalAndDeparture.setArrivalTime(String.valueOf(Long.parseLong(allocatedWorkingStep_transport.getHasTimeslot().getEndDate())));
					inform_arrivalAndDeparture.setAvg_PickupTime(requested_operation.getAvg_PickupTime());
					receiver.setLocalName(allocWS_end.getHasResource().getName());
					inform_acl.setConversationId("Inform_Arrival");
					action = "arrival";
					//System.out.println("DEBUG_______21231241___");
			    	
				}else {
					break; //leave the for loop and dont send another message (if there is no one left to get the message)
				}
				
				inform_arrivalAndDeparture.setID_String(requested_operation.getAppliedOn().getID_String());
				sendInform_departure.setHasInform_Departure(inform_arrivalAndDeparture);			
				Action content = new Action(myAgent.getAID(),sendInform_departure);
						
				inform_acl.addReceiver(receiver);
				
			
				//ontology --> fill content
				try {
					myAgent.getContentManager().fillContent(inform_acl, content);
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				myAgent.send(inform_acl);
				System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+myAgent.logLinePrefix+" inform_acl "+action+" sent to receiver "+receiver.getLocalName()+" with content "+inform_acl.getContent());
				
		 }
		
		
		
		//myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().remove(i);
		
		//operations_to_be_removed.add(allWorkingStep.getHasOperation().getName());
		//i++;
		
		//delete step in the database not necessary --> can be updated
		

	}

}
