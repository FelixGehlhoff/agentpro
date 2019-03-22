package DatabaseConnection;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import agentPro_Prototype_Agents._Simulation_Order_Generator;

import jade.core.behaviours.OneShotBehaviour;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class CreateAgentsAccordingToDatabase extends OneShotBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private _Simulation_Order_Generator myAgent;
	//private String dbaddress = "jdbc:mysql://localhost:3306/MySQL?"+"user=root&password=SQL_0518&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";	//Address od database;

	public CreateAgentsAccordingToDatabase(_Simulation_Order_Generator myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
	
	}

	@Override
	public void action() {
	    Statement stmt = null;
	    String query = "SELECT * FROM "+myAgent.tableNameResource+" where "+myAgent.columnNameResourceType+" != 'simulation'";
	    		
	   
	    try {
	        //stmt = myAgent.getConnection().createStatement();
	        stmt = myAgent.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	        ResultSet rs = stmt.executeQuery(query);
	        
	        while (rs.next()) {
	        	Boolean createAgent = true;
	        	String name = rs.getString(myAgent.columnNameResourceName_simulation);
	        	//int id = rs.getInt(columnNameID);
	        	String res_type = rs.getString(myAgent.columnNameResourceType);
	        	String res_detailed_type = rs.getString(myAgent.columnNameResourceDetailedType);
	        	String path_for_agent_class = "agentPro_Prototype_Agents.";
	        	if(res_type.equals("transport") && res_detailed_type.equals("buffer")) {
	        		path_for_agent_class = path_for_agent_class + "BufferAgent_Stringer";
	        	}else if(res_type.equals("transport")){
	        		path_for_agent_class = path_for_agent_class + "TransportResourceAgent";
	        	}else if(res_type.equals("production")){
	        		path_for_agent_class = path_for_agent_class + "ProductionResourceAgent";
	        	}else if(res_type.equals("Shared_Resource") && res_detailed_type.equals("Operator")) {
	        		path_for_agent_class = path_for_agent_class + "OperatorAgent";
	        	}else if(res_type.equals("other")) {
	        		createAgent = false;
	        	}
	        	else {
	        		path_for_agent_class = path_for_agent_class + "Crane_RailAgent";
	        	}
	        	
	        	if(createAgent) {
	        		ContainerController cc = myAgent.getContainerController();
					AgentController ac;
	        		try {
						ac = cc.createNewAgent(name, path_for_agent_class, null);
						ac.start();
					} catch (StaleProxyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	

				
	        }
	      

	        
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    }
	   
	    //activate orders
	    orderGenerationBehaviour orders = new orderGenerationBehaviour(myAgent);
		myAgent.addBehaviour(orders);
	}
	



}
