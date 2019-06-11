package agentPro_Prototype_Agents;

import agentPro_Prototype_InterfaceAgent_Behaviours.ReceiveOrderFromERPBehaviour;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import agentPro.onto.AgentPro_ProductionOntology;
import agentPro_Prototype_InterfaceAgent_Behaviours.ReceiveInformOrderCompletionBehaviour;

/*
 * Provides the interface to the ERP system (here: the dummy agent). It receives orders from the ERP system. 
 * To fulfill the order an order agent is created.
 * If the order is finished the interface agent is contacted by the order agent. It then sends an order completion message
 * to the ERP system.
 */

public class InterfaceAgent extends _Agent_Template{
	private static final long serialVersionUID = 1L;
	protected ReceiveOrderFromERPBehaviour ReceiveOrderFromERPBehav;
	protected ReceiveInformOrderCompletionBehaviour ReceiveInformOrderCompletionBehaviour;
	private String conversationID_forOrderAgent = "OrderAgent";
	private AID ERP_system;
	private String DateFormat = "yyyy-MM-dd HH:mm:ss";
	public SimpleDateFormat SimpleDateFormat = new SimpleDateFormat(DateFormat);
	public String logLinePrefix;
	//public String ability;
	//Datenbankverbindung
		//public final String dbaddress = "jdbc:ucanaccess://C:/Users/Mitarbeiter/Dropbox (HSU_Agent.Pro)/_AgentPro/Prototyp/Database.accdb";	//Address od database
		//private String dbaddress_sim = "jdbc:mysql://localhost:3306/MySQL?"+"user=root&password=SQL_0518&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";	//Address od database;
		private Connection connection;			//Connection to database
		
		/*
		private String nameOfProduction_Plan_Def_Table = "Production_Plan_Def";
		private String columnNameOfOperation = "Operation";
		private String columnNameOfStep = "Step";
		private String columnNameOfProductName = "ProductName";
		private String columnNameOfFirstOperation = "FirstOperation";
		private String columnNameOfLastOperation = "LastOperation";
	*/
		
	
	protected void setup (){
		//super.setup();
		logLinePrefix = " InterfaceAgent ";
		// / INITIALISATION
		// /////////////////////////////////////////////////////////
		//System.out.println("InterfaceAgent startet...");
		registerAtDF();
		//Datenbank
				Connection con;			
				
				try {			
						con = DriverManager.getConnection(dbaddress_sim);	// Verbindung zur DB mit ucanaccess	
									
					this.setConnection(con);			     
			        
			    } catch (SQLException e ) {
			        e.printStackTrace();
			    }
				
		
		//Ontology
		//getContentManager().registerOntology(ontology);
		//getContentManager().registerLanguage(codec);
		
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
		ReceiveOrderFromERPBehav = new ReceiveOrderFromERPBehaviour(conversationID_forOrderAgent, this);
        addBehaviour(ReceiveOrderFromERPBehav);
        ReceiveInformOrderCompletionBehaviour = new ReceiveInformOrderCompletionBehaviour(this,conversationID_forOrderAgent);
        addBehaviour(ReceiveInformOrderCompletionBehaviour);
	}

	public AID getERP_system() {
		return ERP_system;
	}

	public void setERP_system(AID eRP_system) {
		ERP_system = eRP_system;
	}	
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	void registerAtDF() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("interface");
		sd.setName("interface");			
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
	}

}
