package agentPro_Prototype_WorkpieceAgent_Behaviours;


import java.util.ArrayList;
import java.util.Date;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Production_Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Timeslot;
import agentPro_Prototype_Agents.WorkpieceAgent;

import jade.core.AID;
import jade.core.behaviours.Behaviour;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import support_classes.Run_Configuration;


public class RequestPerformer extends Behaviour {

	private static final long serialVersionUID = 1L;
	private int step = 0;
	
	private String logLinePrefix = ".RequestPerformer";
	private WorkpieceAgent myAgent;
	
	private String conversationID = "";

	//receiveProposals
		private Long reply_by_date_CFP; //
		private Long startdate_for_this_task;
		private ArrayList<AID> resourceAgents = new ArrayList <AID>();
		private Production_Operation requested_operation;
		private ArrayList<AID> proposal_senders = new ArrayList <AID>();
		private ArrayList<Proposal> received_proposals = new ArrayList <Proposal>();
		
	//arrange last transport
		private Boolean last_operation;
		private String necessary_resource_agent_for_this_step;
		private int numberOfAnswers;
	
	
	public RequestPerformer(WorkpieceAgent myAgent, Production_Operation requested_operation, Long startdate_for_this_task, Boolean last_operation) { //start_date of production / startdate is null in case of transport 
		super(myAgent);
		this.myAgent = myAgent;	
		this.requested_operation = requested_operation;
		if(startdate_for_this_task != null) {
			this.startdate_for_this_task = startdate_for_this_task;
		}	
		this.conversationID = Long.toString(System.currentTimeMillis());
		this.logLinePrefix = this.logLinePrefix+"."+requested_operation.getName()+" ";
		this.last_operation = last_operation;
	}
	

	public RequestPerformer(WorkpieceAgent myAgent, Production_Operation requested_operation, long startdate_for_this_task,
			Boolean last_operation, String necessary_resource_agent_for_this_step) {
		super(myAgent);
		this.myAgent = myAgent;	
		this.startdate_for_this_task = startdate_for_this_task;
		this.requested_operation = requested_operation;		
		this.conversationID = Long.toString(System.currentTimeMillis());
		this.logLinePrefix = this.logLinePrefix+"."+requested_operation.getName()+" ";
		this.last_operation = last_operation;
		this.necessary_resource_agent_for_this_step = necessary_resource_agent_for_this_step;
	}

	public void action() {
		switch (step) {
		
		case 0:		
			
			if(necessary_resource_agent_for_this_step != null) {
				for(DFAgentDescription a : myAgent.resourceAgents) {
					//System.out.println("DEBUG___________"+a.getName().getLocalName()+"  necessary_resource_agent_for_this_step  "+necessary_resource_agent_for_this_step);
					if(a.getName().getLocalName().equals(necessary_resource_agent_for_this_step)) {
						resourceAgents.add(a.getName());
						break;
					}
				}
			}else {
				//find agents with capability X
				resourceAgents = myAgent.findOfferingAgents(requested_operation);
			}

			//determine the requested timeslot (earliest start date, latest finish date) of the operation
			Timeslot cfp_timeslot =  determineCFPTimeslot();	 
					 
					 
			//determine reply by time
						long reply_by_date_long = System.currentTimeMillis()+Run_Configuration.reply_by_time_wp_agent;
						Date reply_by_date = new Date(reply_by_date_long);
						reply_by_date_CFP = reply_by_date_long;
			myAgent.sendCfps(cfp_timeslot, requested_operation, conversationID, resourceAgents, reply_by_date);
				
			step = 1;
			block((long) (0.5*Run_Configuration.reply_by_time_wp_agent));
			break;
		case 1:	
			//deadline	
			//if deadline expired or all proposals are received --> get transport proposals
			//if(System.currentTimeMillis()>reply_by_date_CFP || resourceAgents.size() == received_proposals.size()) {
			if(System.currentTimeMillis()>reply_by_date_CFP || resourceAgents.size() == numberOfAnswers) {

				step = 3;
				break;
			}else if(System.currentTimeMillis()<=reply_by_date_CFP){ 
				step = 2;				
				block(10);
				break;
			} 
			
			//break;
		case 2:

			// Receive all proposals
			numberOfAnswers += myAgent.receiveProposals(conversationID, proposal_senders, received_proposals);												
			numberOfAnswers += myAgent.receiveRejection(conversationID, proposal_senders, received_proposals);		
			
			step = 1;			
			break;
			
		case 3:
			//arrange transports (if necessary)
			myAgent.addBehaviour(new RequestPerformer_transport(myAgent, received_proposals, null, last_operation, false, conversationID));
			step = 4; 
			break;
		}//switch        
	}
	
	private Timeslot determineCFPTimeslot() {
		Timeslot timeslot = new Timeslot();

		AllocatedWorkingStep LAST_alWS_Production = myAgent.getLastProductionStepAllocated();
	
			if(LAST_alWS_Production != null && LAST_alWS_Production.getIsFinished()) {		//if if the last allocatedWStep is already finished, there are no new steps planned which would have to be used as a start and a new planning has started
				//timeslot.setStartDate(Long.toString(System.currentTimeMillis())); --> Workaraound needed because current time is before the finished step   TBD			
				//start for the next production step is end of last + transport time
				timeslot.setStartDate(Long.toString(Long.parseLong(LAST_alWS_Production.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation_CFP()));			
			}else if(LAST_alWS_Production != null && !LAST_alWS_Production.getIsFinished()){	//we are within a new planning cycle	
				timeslot.setStartDate(Long.toString(Long.parseLong(LAST_alWS_Production.getHasTimeslot().getEndDate())+myAgent.getTransport_estimation_CFP()));			
			}
			else {		//first step to be scheduled
				timeslot.setStartDate(Long.toString(startdate_for_this_task));			
			}
			String enddate_for_this_task = Long.toString(startdate_for_this_task+myAgent.getTime_until_end());//e.g. end of the week --> tbd TODO			
			timeslot.setEndDate(enddate_for_this_task);

		return timeslot;
	}
	

	public boolean done() {
	
		return step == 4;
	}
}  