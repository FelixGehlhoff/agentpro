package agentPro_Prototype_Agents;

import DatabaseConnection.CreateAgentsAccordingToDatabase;


/*
 * Serves as a Dummy ERP System and sends Order to Interface Agent
 * Order is sent in a JSON format via ACL Message
 */

public class _Simulation_Order_Generator extends _Agent_Template{
	private static final long serialVersionUID = 1L;
	//private double initial_wait = 2000;
	//double a = 4000;
	//public Connection connection;
	
	//private String dbaddress = "jdbc:mysql://localhost:3306/MySQL?"+"user=root&password=SQL_0518&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";	//Address od database;
	
	//private static Connection connection;	
	//private static String nameOfProductionPlan = "productionplan";
	//private static String nameOfMES_Data_Resource_View = "MES_Data_Resource_View";
	
	protected void setup (){
		super.setup();
	
		CreateAgentsAccordingToDatabase setup_agents = new CreateAgentsAccordingToDatabase(this);
		this.addBehaviour(setup_agents);
		
		
		
	}

}
