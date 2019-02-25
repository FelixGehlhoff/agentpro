package DatabaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Disturbance;
import agentPro.onto.DisturbanceType;
import agentPro.onto.Location;
import agentPro.onto.Machine_Error;
import agentPro.onto.Operation;
import agentPro.onto.Request_DatabaseEntry;
import agentPro.onto.Resource;
import agentPro.onto.Workpiece_Error;
import agentPro.onto._Incoming_Disturbance;
import agentPro.onto._SendRequest_DatabaseEntry;
import agentPro_Prototype_Agents.DatabaseMonitorAgent;
import agentPro_Prototype_Agents._Agent_Template;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class MonitorDBChangedBehaviour extends CyclicBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DatabaseMonitorAgent myAgent;
	//private String dbaddress = "jdbc:mysql://localhost:3306/MySQL?"+"user=root&password=SQL_0518&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";	//Address od database;
	private double double_stored = 0;
	
	//new Simulation
	

	public MonitorDBChangedBehaviour(DatabaseMonitorAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
		double_stored = System.currentTimeMillis();
	
	}

	@Override
	public void action() {
	    Statement stmt = null;
	    String query = "SELECT * FROM flexsimdata.monitoring";
	    int action_needed = 0;
	    Disturbance disturbance = new Disturbance();
	    try {
	    	if(myAgent.getConnection().isClosed()) {
				myAgent.activateConnection();
			}
	        //stmt = myAgent.getConnection().createStatement();
	        stmt = myAgent.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	        ResultSet rs = stmt.executeQuery(query);
	       
	        while (rs.next()) {
	        	action_needed = rs.getInt(1);
	        }
  
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    }
	    
	        	if(action_needed==1) {
	        		System.out.println(System.currentTimeMillis()+"______________Resource Failure at Resource ___________");
	        		
	        		try {
						disturbance = receiveDisturbanceValuesFromDB();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						
					}
	        		
	        		sendMessageToWorkpieceAgent(disturbance);

	        	}else {

	        	}

	    this.block(10);
	}
	
	private void sendMessageToWorkpieceAgent(Disturbance disturbance) {

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);	
		
		//find agents with id
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		//service_type = requested_operation.getType();
		sd.setType("workpiece"); 	
		sd.setName(Integer.toString(disturbance.getId_workpiece()));
		template.addServices(sd);
		
		try {
			DFAgentDescription[] result = DFService.search(myAgent, template);	
			if(result.length > 0) {
				msg.addReceiver(result[0].getName());
				msg.setConversationId("Disturbance_Simulation");
				msg.setLanguage(myAgent.codec.getName());
				msg.setOntology(myAgent.ontology.getName());
				//msg.setProtocol(data_from_file);
				
				_Incoming_Disturbance incDisturbance = new _Incoming_Disturbance();	
				incDisturbance.setHasDisturbance(disturbance);
				
				Action content = new Action(myAgent.getAID(), incDisturbance);				
				try {
					myAgent.getContentManager().fillContent(msg, content);
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				System.out.println("DEBUG_________msg prepared");
				myAgent.send(msg);		
			}else {
				System.out.println(System.currentTimeMillis()+" No WP Agent found for disturbance happend at time "+disturbance.getError_occurance_time()+" for wp "+disturbance.getId_workpiece()+" at res "+((Resource)disturbance.getOccuresAt()).getName());
			}
			
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	//System.out.println("DEBUG______________msg sent to receiver "+workpieceAgents[0]);

	//System.out.println(logLinePrefix+" Sender Agent: "+this.getName()+" INFORM sent to receiver: "+workpieceAgents[0].getName()+ "content "+msg.getContent());
	
		
	}

	private Disturbance receiveDisturbanceValuesFromDB() throws SQLException {
		Disturbance disturbance = new Disturbance();
		DisturbanceType dis_type = new DisturbanceType();
		Resource res = new Resource();
		String error_name = "";	
		try (Connection con = myAgent.getConnection(); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				){
			ResultSet rs = null;
			ResultSet rs2 = null;
		String query_resource = "select "+myAgent.columnNameResourceName_simulation+" , "+myAgent.columNameColumnNameInProductionPlan+" , "+myAgent.columnNameColumnInProductionPlan+" , "+myAgent.columnNameResourceType+" , "+myAgent.columnNameErrorType+" , "+myAgent.columnNameError_Occur_Time+" , "+myAgent.columnNameID+" , "+myAgent.columnNameLocationX+" , "+myAgent.columnNameLocationY+" from "+myAgent.tableNameResource+" where "+myAgent.columnNameErrorType+" > "+0;
		System.out.println("DEBUG______________"+query_resource);
		rs = stmt.executeQuery(query_resource); 
		
		int error_type = 0;
		String res_name = "";
		//String res_detailed_type = "";
		String res_type = "";
		float occurance_time = 0;
		int resource_id = 0;
		int column_in_production_plan = 0;
		Location loc = new Location();
		String columnNameinProductonPlan = "";
		while (rs.next()) {		
			error_type = rs.getInt(myAgent.columnNameErrorType);
			resource_id = rs.getInt(myAgent.columnNameID);
			res_name = rs.getString(myAgent.columnNameResourceName_simulation);			
			occurance_time = (float) rs.getDouble(myAgent.columnNameError_Occur_Time);
			res_type = rs.getString(myAgent.columnNameResourceType);	
			column_in_production_plan = rs.getInt(myAgent.columnNameColumnInProductionPlan);
			loc.setCoordX(rs.getInt(myAgent.columnNameLocationX));
			loc.setCoordY(rs.getInt(myAgent.columnNameLocationY));
			columnNameinProductonPlan = rs.getString(myAgent.columNameColumnNameInProductionPlan);
			rs.updateInt(myAgent.columnNameErrorType, 0);
			rs.updateDouble(myAgent.columnNameError_Occur_Time, 0);
			rs.updateRow();
        }
			res.setName(res_name);
			res.setType(res_type);	
			res.setID_Number(resource_id);
			res.setHasLocation(loc);
			
			System.out.println("DEBUG____________"+resource_id);
		long converted_occurance_time = myAgent.convertOccuranceTime(occurance_time);
			disturbance.setError_occurance_time(converted_occurance_time);
		
		if(error_type == 2) {	//light disturbance, 2 hours
			dis_type = new Machine_Error();
			((Machine_Error) dis_type).setExpected_Duration_Of_Repair(myAgent.duration_light_disturbance);
			((Machine_Error) dis_type).setError_type(error_type);
			error_name = "light disturbance";
		}else if(error_type == 3) {	//severe disturbance, 8 hours
			dis_type = new Machine_Error();
			dis_type = (Machine_Error) dis_type;
			((Machine_Error) dis_type).setExpected_Duration_Of_Repair(myAgent.duration_severe_disturbance);
			((Machine_Error) dis_type).setError_type(error_type);
			error_name = "severe disturbance";
		}else if(error_type == 1) {	//workpiece error
			dis_type = new Workpiece_Error();
			dis_type = (Workpiece_Error) dis_type;
			((Workpiece_Error) dis_type).setExpected_Duration_Of_Repair(myAgent.duration_repair_workpiece);
			error_name = "workpiece error";
		}
		
		
		disturbance.setHasDisturbanceType(dis_type);
		//String query_productionPlan = "select "+myAgent.columnNameID+" from "+myAgent.tableNameProductionPlan+" where `"+myAgent.columNameGestartet+resource_id+"` = "+1+" and `"+myAgent.columNameBeendet+resource_id+"` = "+0;
		String query_productionPlan = "select "+myAgent.columnNameorderid+" from "+myAgent.tableNameProductionPlan+" where `"+myAgent.columNameGestartet+column_in_production_plan+"` = "+1+" and `"+myAgent.columNameBeendet+column_in_production_plan+"` = "+0+" and "+columnNameinProductonPlan+" = "+resource_id;
		System.out.println("DEBUG________________query_productionPlan   "+query_productionPlan);
		int workpiece_id = 0;
		rs2 = stmt.executeQuery(query_productionPlan); 
			while(rs2.next()) {
				//workpiece_id = rs2.getInt(2+7*(id-1));		//first name in 2, then 9 then 16 --> 2 + 7 * (ID - 1)
				workpiece_id = rs2.getInt(myAgent.columnNameorderid); //04.02.2019 new: orderid
				disturbance.setId_workpiece(workpiece_id);	
			}
		if(workpiece_id == 0) {
			System.out.println("No entry found that was started but not finished.");
		}
		disturbance.setOccuresAt(res);
		System.out.println("DEBUG___________vor_monitoring");
		ResultSet rs3 = stmt.executeQuery("SELECT * FROM flexsimdata.monitoring");
		  while (rs3.next()) {
				rs3.updateInt(1,0);
				rs3.updateRow();
				break;
		  }
		  
		fillDisturbanceLog(error_type, error_name, occurance_time, converted_occurance_time, resource_id, res_name, workpiece_id, stmt);
		  

		}
	
		
	
		return disturbance;
	}

	private void fillDisturbanceLog(int error_type, String error_name, float occurance_time,
			long converted_occurance_time, int resource_id, String res_name, int workpiece_id, Statement stmt) throws SQLException {
		ResultSet rs = stmt.executeQuery("SELECT * FROM flexsimdata.disturbance_log");
		rs.moveToInsertRow();
		rs.updateInt("error_type", error_type);
		rs.updateString("error_name", error_name);
		rs.updateDouble("error_occurance_time", occurance_time);
		rs.updateDouble("occurance_time_converted", converted_occurance_time);
		rs.updateInt("resource_id", resource_id);
		rs.updateString("resource_name", res_name);
		rs.updateInt("workpiece", workpiece_id);
		rs.insertRow();

	}

}
