package agentPro_Prototype_InterfaceAgent_Behaviours;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Iterator;


import org.jfree.ui.RefineryUtilities;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Operation;
import agentPro.onto.Resource;
import agentPro.onto.Timeslot;
import agentPro.onto.WorkPlan;
import agentPro.onto.Workpiece;
import agentPro_Prototype_Agents.InterfaceAgent;
import agentPro_Prototype_Agents._Agent_Template;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import support_classes.XYTaskDataset_Total;

/*
 * Listens for OrderCompletion messages and sends INFORM to the ERP system (dummy agent)
 */

public class ReceiveInformOrderCompletionBehaviour extends CyclicBehaviour{

	private static final long serialVersionUID = 1L;
	private InterfaceAgent myAgent;
	private String conversationID_forOrderagent;
	private String logLinePrefix = ".ReceiveInformOrderCompletionBehaviour ";
	private int numberOfMessagesReceived = 0;
	
	//database
	/*
		public String nameOfMES_Data_Resource_Veiw = "MES_Data_Resource_View";
		public String columnNameOfOperation = "Operation";
		public String columnNameOfResource = "Ressource";
		public String columnNameOfResource_ID = "Ressource_ID";
		public String columnNameOfPlanStart = "PlanStart";
		public String columnNameOfPlanEnd = "PlanEnde";
		public String columnNameAuftrags_ID = "Auftrags_ID";
		public String columnNameOperation_Type = "Operation_Type";
		public String columnNameOfStarted = "Started";
		public String columnNameOfOperation_Type = "Operation_Type";
	*/
	public ReceiveInformOrderCompletionBehaviour(InterfaceAgent myAgent, String conversationID) {
		super(myAgent);
		this.myAgent = myAgent;
		conversationID_forOrderagent = conversationID;
		
	}
	
	@Override
	public void action() {
		
		// Receive message

		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate mt2 = MessageTemplate.MatchConversationId(conversationID_forOrderagent);	
        MessageTemplate mt_total = MessageTemplate.and(mt1,mt2);
        
		ACLMessage inform = myAgent.receive(mt_total);
		if (inform != null) {
			System.out.println(System.currentTimeMillis()+" "+myAgent.SimpleDateFormat.format(new java.util.Date())+" "+myAgent.getLocalName()+logLinePrefix+inform.getContent());
			enterOrderDataIntoDatabase(inform);
			
			numberOfMessagesReceived++;
			if(_Agent_Template.simulation_enercon_mode) {
				
			}else {
				
				
				/*
				if(numberOfMessagesReceived==_Agent_Template.limit) {
					//wait for last entry in DB (transport process to warehouse outbound
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				
					ArrayList <String> orders = new ArrayList<String>();
					try {
						orders = getOrdersFromDB();
						System.out.println(orders);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					WorkPlan total_wp = createWorkPlan(orders);

					 XYTaskDataset_Total demo = new XYTaskDataset_Total("JFreeChart : XYTaskDataset_Total.java", total_wp);
				        demo.pack();
				        RefineryUtilities.centerFrameOnScreen(demo);
				        demo.setVisible(false);	
					
				}	
				*/
			}
			
			
			//pass info about finished order to XYZ
			ACLMessage inform_done = new ACLMessage(ACLMessage.INFORM);
			
			inform_done.addReceiver(myAgent.getERP_system());
			inform_done.setContent(inform.getContent());	
			inform_done.setConversationId("ERP");
		
			myAgent.send(inform_done);		
			
		}
		else {
			block();
		}
		
	}
	private void enterOrderDataIntoDatabase(ACLMessage inform) {
		String [] split = inform.getContent().split("_");
		String type = split[0];
		int id = Integer.parseInt(split[1]);
		long start = Long.parseLong(split[3]);
		long end = Long.parseLong(split[4]);
		int quantity = Integer.parseInt(split[5]);
		
			    	try(Connection con = DriverManager.getConnection(_Agent_Template.dbaddress_sim); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
							) {
			    		ResultSet rs = stmt.executeQuery(						    			
				    			"select * from "+"agentpro.orderbook"+" where orderID = "+id); 
						// System.out.println("mes__table_to_be_used "+mes_table_to_be_used);      
				    	if (rs.isBeforeFirst() ) {  //the SQL query has returned data  
				    		rs.next(); 
				    		rs.updateString("product", type);
				    		rs.updateInt("number", quantity);
				    		rs.updateDouble("StartCoordination", start);	    		
				    		rs.updateDouble("EndCoordination", end);
				    		rs.updateRow();
				    	}else {
				    		stmt.executeUpdate( 
									"Insert into "+_Agent_Template.nameOfOrderbook+" (orderID, product, number, StartCoordination, EndCoordination) Values ("+id+", '"+type+"' , "+quantity+", "+start+", "+end+");");
						
				    	}
						} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// System.out.println("mes__table_to_be_used "+mes_table_to_be_used);      
		}


	public static WorkPlan createWorkPlan(ArrayList<String> orders) {
		WorkPlan wp_total = new WorkPlan();
		for(String order : orders) {
			WorkPlan wp_order = createWorkplanFromDatabase(order);
			@SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep>it = wp_order.getConsistsOfAllocatedWorkingSteps().iterator();
			while(it.hasNext()) {
				wp_total.addConsistsOfAllocatedWorkingSteps(it.next());
			}
		}
		return wp_total;
	}

	public static ArrayList<String> getOrdersFromDB() throws SQLException {
		ArrayList<String>orders = new ArrayList<String>();
		try (Connection con = DriverManager.getConnection(_Agent_Template.dbaddress_sim); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				){

			    	ResultSet rs = stmt.executeQuery(		
			    			//"select * from "+nameOfMES_Data+" where "+columnNameOfOperation+" = '"+allWorkingStep.getHasOperation().getName()+"' and "+columnNameAuftrags_ID+" = '"+allWorkingStep.getHasOperation().getAppliedOn().getID_String()+"' and "+columnNameFinished+" = 'false'"); 
			    			"select "+_Agent_Template.columnNameAuftrags_ID+" from "+_Agent_Template.nameOfMES_Data_Resource+" group by "+_Agent_Template.columnNameAuftrags_ID); 
					// System.out.println("mes__table_to_be_used "+mes_table_to_be_used);      
			    	while(rs.next()) {
			    		orders.add(rs.getString(_Agent_Template.columnNameAuftrags_ID));
			    	}
		}
		return orders;
	}
	
	public static WorkPlan createWorkplanFromDatabase(String wp_id) {
		WorkPlan workplan = new WorkPlan();
		
		try (Connection con = DriverManager.getConnection(_Agent_Template.dbaddress_sim); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				){

			    	ResultSet rs = stmt.executeQuery(		
			    			//"select * from "+nameOfMES_Data+" where "+columnNameOfOperation+" = '"+allWorkingStep.getHasOperation().getName()+"' and "+columnNameAuftrags_ID+" = '"+allWorkingStep.getHasOperation().getAppliedOn().getID_String()+"' and "+columnNameFinished+" = 'false'"); 
			    			"select * from "+_Agent_Template.nameOfMES_Data_Resource+" where "+_Agent_Template.columnNameAuftrags_ID+" = '"+wp_id+"'"); 
					// System.out.println("mes__table_to_be_used "+mes_table_to_be_used);      
			    	if (rs.isBeforeFirst() ) {			    	//the SQL query has returned data  
			    		while(rs.next()) {
			    			AllocatedWorkingStep allWS = new AllocatedWorkingStep();
			    			allWS.setID_String("Test");
				        	Timeslot timeslot = new Timeslot();		     
				        		timeslot.setStartDate(String.valueOf(rs.getTimestamp(_Agent_Template.columnNameOfPlanStart).getTime()));
				        		timeslot.setEndDate(String.valueOf(rs.getTimestamp(_Agent_Template.columnNameOfPlanEnd).getTime()));
				        		timeslot.setLength(rs.getTimestamp(_Agent_Template.columnNameOfPlanEnd).getTime()-rs.getTimestamp(_Agent_Template.columnNameOfPlanStart).getTime());
				        		allWS.setHasTimeslot(timeslot);
				        	allWS.setIsStarted(rs.getBoolean(_Agent_Template.columnNameOfStarted));	
				        	allWS.setIsFinished(rs.getBoolean(_Agent_Template.columnNameOfFinished));	
				        	Resource res = new Resource();
				        		res.setName(rs.getString(_Agent_Template.columnNameOfResource));
				        		res.setID_Number(rs.getInt(_Agent_Template.columnNameOfResource_ID));
				        		res.setDetailed_Type("Test");
				        		allWS.setHasResource(res);
				        	Operation op = new Operation();
				        		op.setName(rs.getString(_Agent_Template.columnNameOfOperation));
				        			Workpiece wp = new Workpiece();
				        			wp.setID_String(wp_id);
				        		op.setAppliedOn(wp);
				        		//op.setType(rs.getString(_Agent_Template.columnNameOperation_Type));			        		
				        		//op.setSet_up_time(10);
				        		//op.setAvg_Duration(20);
				        		allWS.setHasOperation(op);
				        	workplan.addConsistsOfAllocatedWorkingSteps(allWS);
			    		}
			    	rs.close();	 
			        System.out.println(_Agent_Template.printoutWorkPlan(workplan, "test_agent"));   
			    	}else {
			    		System.out.println("No data found for id: "+wp_id);
			    
			    	}
      
	    } catch (SQLException e ) {
	        e.printStackTrace();
	    }	
		return workplan;
	}
}
