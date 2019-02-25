package DatabaseConnection;


import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;

import agentPro.onto.WorkPlan;
import agentPro.onto._SendRequest_DatabaseEntry;
import agentPro_Prototype_Agents.DatabaseConnectorAgent;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/*
 * Listens for OrderCompletion messages and sends INFORM to the ERP system (dummy agent)
 */

public class ReceiveDatabaseQueryRequestBehaviour extends CyclicBehaviour{

	private static final long serialVersionUID = 1L;
	private DatabaseConnectorAgent myAgent;
	private String logLinePrefix = ".ReceiveDatabaseQueryRequestBehaviour ";
	
	//database
		public String nameOfMES_Data_Resource_Veiw = "MES_Data_Resource_View";
		public String columnNameOfOperation = "Operation";
		public String columnNameOfResource = "Ressource";
		public String columnNameOfResource_ID = "Ressource_ID";
		public String columnNameOfPlanStart = "PlanStart";
		public String columnNameOfPlanEnd = "PlanEnde";
		public String columnNameAuftrags_ID = "Auftrags_ID";
		public String columnNameOperation_Type = "Operation_Type";
		public String columnNameOfStarted = "Started";
		public String nameOfMES_Data_Resource = "MES_Data_Resource_View";
		public String columnNameOfOperation_Type = "Operation_Type";
		private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		private String agent_type = "";
	
	public ReceiveDatabaseQueryRequestBehaviour(DatabaseConnectorAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
	
	}
	
	@Override
	public void action() {
		
		// Receive message
		ACLMessage request = myAgent.receive(mt);
		
		if (request != null) {
		String sender = request.getSender().getLocalName();
			if(request.getSender().getLocalName().contains("Workpiece")) {
				agent_type = "workpiece";
				
			}else {
				agent_type = "resource";
			}
			WorkPlan workplan = new WorkPlan();
			
			try {
				Action act = (Action) myAgent.getContentManager().extractContent(request);
				_SendRequest_DatabaseEntry reqDBentry = (_SendRequest_DatabaseEntry) act.getAction();
							
			    @SuppressWarnings("unchecked")
				Iterator<AllocatedWorkingStep> it = reqDBentry.getHasRequest_DatabaseEntry().getConsistsOfAllocatedWorkingSteps().iterator();
			    while(it.hasNext()) {
			    	AllocatedWorkingStep allocWorkingstep = it.next();
			    	workplan.addConsistsOfAllocatedWorkingSteps(allocWorkingstep);		    	
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
	
			try {
				myAgent.addDataToDatabase(agent_type, workplan, sender);
				//System.out.println(System.currentTimeMillis() + " "+myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+" entry added to Database.");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else {
			block();
		}
		
	}
	
}
