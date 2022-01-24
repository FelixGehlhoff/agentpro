package agentPro_Prototype_Agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import agentPro.onto.AgentPro_ProductionOntology;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Delay;
import agentPro.onto.Disturbance;
import agentPro.onto.DisturbanceType;
import agentPro.onto.Location;
import agentPro.onto.Machine_Error;
import agentPro.onto.Order;
import agentPro.onto.OrderPosition;
import agentPro.onto.Product;
import agentPro.onto.Resource;
import agentPro.onto.Warehouse_Resource;
import agentPro.onto.Workpiece_Error;
import agentPro.onto._Incoming_Disturbance;
import agentPro.onto._Incoming_Order;
import agentPro.onto._SendProposal;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/*
 * Serves as a Dummy ERP System and sends Order to Interface Agent
 * Order is sent in a JSON format via ACL Message
 */

public class _MQTT_DUMMY extends Agent{
	private static final long serialVersionUID = 1L;
	double initial_Wait = 15000;
	//Ontology
	private Ontology ontology = AgentPro_ProductionOntology.getInstance();
	private Codec codec = new SLCodec();
	
	private AID[] workpieceAgents;
	
	public final String dbaddress = "jdbc:ucanaccess://C:/Users/Mitarbeiter/Dropbox (HSU_Agent.Pro)/_AgentPro/Prototyp/Database.accdb";	//Address od database
	private static Connection connection;	
	private static String nameOfMES_Data = "MES_Data";
		private static String columnNameOfOperation = "Operation";
		private String columnNameOfResource = "Ressource";
		private String columnNameOfResource_ID = "Ressource_ID";
		private static String columnNameOfIstStart = "IstStart";
		private static String columnNameOfIstEnde = "IstEnde";
		private static String columnNameAuftrags_ID = "Auftrags_ID";
		private static String columnNameOfStarted = "Started";
		private static String columnNameOfFinished = "Finished";
		private static String columnNameOfErrorStep = "Error";
		private static  String columnNameOfPlanStart = "PlanStart";
		private static  String columnNameOfPlanEnd = "PlanEnde";
		private static  String columnNameOfistStart = "IstStart";
		private static  String columnNameOfIstEnd = "IstEnde";
		
		//old Access
		private String op_1 = "Fräsen_1.1";
		private String op_2 = "0.0;0.0_Fräsmaschine1";
		private String op_3 = "Montieren_3.2";
		private String op_4 = "20.0;20.0_Montageplatz1";
		private String op_5 = "Lackieren_3";
		private String op_6 = "40.0;40.0_Lackierkabine1";
		private static String Auftrags_ID = "Workpiece_No_1.1";
		private String name_of_WorkpieceAgent = "WorkpieceAgent_No_1.1";
		private String product = "ABC";
		
		private String op_1a = "0.0;0.0_Montageplatz1";
		
		private static long time_to_substract_for_date_of_disturbace_occurance = 1*60*60*1000;	//1 hour
	
	protected void setup (){
		super.setup();
		String logLinePrefix = " _DUMMY ";
		// / INITIALISATION
		// /////////////////////////////////////////////////////////	
		
		Random r = new Random();
		double d = r.nextDouble();
		d = initial_Wait + d*9000;
		d = initial_Wait;
		try {
			Thread.sleep((long) d);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int i = 4;
		//String order;
		
		//for (int i = 1;i<x;i++){
			Order order_onto = new Order();		
		String cause = "Machine_Error";					//FROM DB
		float date_of_repair_start = 0;					//FROM DB
		float duration_of_repair = 2*60*60*1000;		//FROM DB
		int resource_id = 3;							//FROM DB
		Disturbance disturbance = setDisturbance(cause, date_of_repair_start, duration_of_repair, resource_id);

			
			String data_from_file = "";
			
				if(i==2 || i == 5){	
					//order = 
					//		"{ 	\"order_number\":"+i+",\"order_positions\": 	[{	\"order_position\" : 1, \"product_name\" : \"ABC\", \"production_steps\" : [\"Casting\",\"Screwing\",\"Polishing\"],\"quantity\"	 :  2, \"due_date\"	 : 1494839827157 },{   \"order_position\" : 2, \"product_name\" : \"XYZ\", \"production_steps\" : [\"Forging\",\"Screwing\"], \"quantity\" :  1,  \"due_date\"	 : 1494839827157 }]}";
					data_from_file = "{ 	\"checkpoint_sequence_number\": 6 ,\"checkpoint_name\": \"transport#60.0;60.0_QS1\"}";
					Connection con;			
					
					try {
						con = DriverManager.getConnection(dbaddress);	// Verbindung zur DB mit ucanaccess	
						connection = con;			     
				        
				    } catch (SQLException e ) {
				        e.printStackTrace();
				    }
					
					try {
						fill_DB(op_1, true, true);
						fill_DB(op_2, true, true);
						fill_DB(op_4, true, true);
						fill_DB(op_3, true, true);
						fill_DB(op_6, true, true);
						fill_DB(op_5, true, false);
					
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
					
				}else if(i==4 && product.equals("XYZ")){
					//order = 
					//		"{ 	\"order_number\":"+i+",\"order_positions\": 	[{	\"order_position\" : 1, \"product_name\" : \"ABC\", \"production_steps\" : [\"Casting\",\"Screwing\",\"Polishing\"],\"quantity\"	 :  2, \"due_date\"	 : 1494839827157 },{   \"order_position\" : 2, \"product_name\" : \"XYZ\", \"production_steps\" : [\"Forging\",\"Screwing\"], \"quantity\" :  3,  \"due_date\"	 : 1494839827157 }]}";
					
					data_from_file = 
							"{ 	\"checkpoint_sequence_number\": 4 ,\"checkpoint_name\": \"transport#40.0;40.0_Lackierkabine1\"}";
					
					Connection con;			
					
					try {
						con = DriverManager.getConnection(dbaddress);	// Verbindung zur DB mit ucanaccess	
						connection = con;			     
				        
				    } catch (SQLException e ) {
				        e.printStackTrace();
				    }
					
					try {
						fill_DB(op_1a, true, true);
						fill_DB(op_3, true, false);
					
					
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
					
				}else if (i==4 && product.equals("ABC") && cause.equals("Delay")){

					data_from_file = 
							"{ 	\"checkpoint_sequence_number\": 4 ,\"checkpoint_name\": \"transport#40.0;40.0_Lackierkabine1\"}";
					
					Connection con;			
					
					try {
						con = DriverManager.getConnection(dbaddress);	// Verbindung zur DB mit ucanaccess	
						connection = con;			     
				        
				    } catch (SQLException e ) {
				        e.printStackTrace();
				    }
					
					try {
						fill_DB(op_1, true, true);
						fill_DB(op_2, true, true);
						fill_DB(op_4, true, true);
						fill_DB(op_3, true, false);
					
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
					
				}else if (i==4 && product.equals("ABC") && cause.equals("Machine_Error")){

					
					Connection con;			
					
					try {
						con = DriverManager.getConnection(dbaddress);	// Verbindung zur DB mit ucanaccess	
						connection = con;			     
				        
				    } catch (SQLException e ) {
				        e.printStackTrace();
				    }
					
					try {
						fill_DB(op_1, true, true);
						fill_DB(op_2, true, true);
						fill_DB(op_4, true, true);
						fill_DB(op_3, true, false);
					
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
					
				}		
						
					//order = 
					//		"{ 	\"order_number\":"+i+",\"order_positions\": 	[{   \"order_position\" : 1, \"product_name\" : \"XYZ\", \"production_steps\" : [\"Forging\",\"Screwing\"], \"quantity\" :  1,  \"due_date\"	 : 1494839827157 }]}";
					
				
				
				
				

				//Ontology
				getContentManager().registerOntology(ontology);
				getContentManager().registerLanguage(codec);
			
				
				//find agents with capability X
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				//service_type = requested_operation.getType();
				sd.setType("workpiece"); 	
	
				template.addServices(sd);
				
				try {
					DFAgentDescription[] result = DFService.search(this, template);
				
					if (result.length != 0){
						workpieceAgents = new AID[result.length];
						//System.out.println("DEBUG_______________________workpiece agent gefunden");
						for (int j = 0; j < result.length; ++j) {
							workpieceAgents[j] = result[j].getName();
						}
						
					}
					
					
				} catch (FIPAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			//AID receiver = new AID();
			//String localName = "InterfaceAgent";
			//receiver.setLocalName(localName);
			
			
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);	
			for(int j = 0; j<workpieceAgents.length;j++) {
				if(workpieceAgents[j].getLocalName().equals(name_of_WorkpieceAgent)) {
					msg.addReceiver(workpieceAgents[j]);
				}
					
			}
			
			//msg.setContent(order);
			msg.setConversationId("Disturbance");

			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
			msg.setProtocol(data_from_file);
			
			_Incoming_Disturbance incDisturbance = new _Incoming_Disturbance();	
			incDisturbance.setHasDisturbance(disturbance);
			
			Action content = new Action(this.getAID(), incDisturbance);
			
			try {
				this.getContentManager().fillContent(msg, content);
			} catch (CodecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OntologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			this.send(msg);		
			//System.out.println("DEBUG______________msg sent to receiver "+workpieceAgents[0]);

			//System.out.println(logLinePrefix+" Sender Agent: "+this.getName()+" INFORM sent to receiver: "+workpieceAgents[0].getName()+ "content "+msg.getContent());
			System.out.println("QQQQQQQQQQQQQQQ_______WWWWWWWWWWWWWWWWW_______DISTURBANCE_______WWWWWWWWWWWWWWWWWW_____QQQQQQQQQQQQQQQQQQ");
			
		//}

	     


		
		
		//System.out.println(SimpleDateFormat.format(new Date())+logLinePrefix+" Sender Agent: "+this.getName()+" INFORM sent to receiver: "+receiver.getName());
		/*
		String order2 = 
				"{ 	\"order_number\":2,\"order_positions\": 	[{   \"order_position\" : 1, \"product_name\" : \"XYZ\", \"quantity\" :  3,  \"due_date\"	 : 98765 }]}";
		
		AID receiver2 = new AID();
		String localName2 = "InterfaceAgent";
		receiver2.setLocalName(localName2);
		
		ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);	
		msg2.addReceiver(receiver2);
		msg2.setContent(order2);
		msg2.setConversationId("ERP");
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.send(msg2);		
		*/
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////

	}

	private Disturbance setDisturbance(String cause, float date_of_repair_start, float duration_of_repair, int resource_id) {
		Disturbance disturbance = new Disturbance();
		
		if(cause.equals("Machine_Error")) {			
			Machine_Error machine_error = new Machine_Error();
				machine_error.setExpected_date_of_repair_Start(date_of_repair_start);
				machine_error.setExpected_Duration_Of_Repair(duration_of_repair);
			disturbance.setHasDisturbanceType(machine_error);
			disturbance.setTimeDelta(0);	
			disturbance.setLocalizationDelta(0);
			disturbance.setProcessDelta(50);			//TBD
			disturbance.setTopic("Machine_Error");		
		}else if(cause.equals("Delay")) {
			Delay delay = new Delay();
				delay.setExpected_date_of_repair_Start(date_of_repair_start);
				delay.setExpected_Duration_Of_Repair(duration_of_repair);
			disturbance.setHasDisturbanceType(delay);
			disturbance.setTimeDelta(duration_of_repair);	
			disturbance.setLocalizationDelta(0);
			disturbance.setProcessDelta(0);
			disturbance.setTopic("Delay");
		}else if(cause.equals("Workpiece_Error")) {
			Workpiece_Error wp_error = new Workpiece_Error();
				wp_error.setExpected_date_of_repair_Start(date_of_repair_start);
				wp_error.setExpected_Duration_Of_Repair(duration_of_repair);
			disturbance.setHasDisturbanceType(wp_error);
			disturbance.setTimeDelta(0);	
			disturbance.setLocalizationDelta(0);
			disturbance.setProcessDelta(100);			//TBD
			disturbance.setTopic("Workpiece_Error");		
		}
		
		Resource res = new Resource();
		res.setType("production");		
		//res.setName("Montageplatz1");			//this information should come from the database
		res.setID_Number(resource_id);
	disturbance.setOccuresAt(res);
		return disturbance;
		
	}

	private static void fill_DB(String operation, boolean started, boolean finished) throws SQLException {
		Statement stmt = connection.createStatement(
	            ResultSet.TYPE_SCROLL_SENSITIVE, // Vor- und Rücksprünge möglich
	            ResultSet.CONCUR_UPDATABLE);     // Veränderbar
    	
    	ResultSet rs = stmt.executeQuery(		
    			"select "+columnNameOfPlanStart+" , "+columnNameOfPlanEnd+" , "+columnNameOfFinished+" , "+columnNameOfStarted+" , "+columnNameOfErrorStep+" from "+nameOfMES_Data+" where "+columnNameOfOperation+" = '"+operation+"' and "+columnNameAuftrags_ID+" = '"+Auftrags_ID+"'"); //Selektiere alle Spalten (*) der Tabelle tblOrder in SQL Sprache
 	        
 	        //Updates the designated column with a String value. The updater methods are used to update column values 
 	        //in the current row or the insert row. The updater methods do not update the underlying database; 
 	        //instead the updateRow or insertRow methods are called to update the database.
    	//Man sollte die Tabellennamen als String ansprechen - Ansprache mit Zahlen führt häufig zu Fehlern
    	rs.next(); 
    	//rs.moveToInsertRow();		  //Jetzt steht das RS in der "Insert" Zeile, diese ist quasi "separat" zum Rest der Tabelle  
        //RS bzw. jede Spalte wird mit Werten gefüllt
	
        rs.updateBoolean(columnNameOfStarted, started);	  
        rs.updateBoolean(columnNameOfFinished, finished);
        rs.updateDate(columnNameOfIstStart, rs.getDate(columnNameOfPlanStart));
		
        if(!finished) { //wenn finished = false, dann error_step = true and set is end to plan end minus 1 hour
        	rs.updateBoolean(columnNameOfErrorStep, !finished);
        	java.sql.Date date = rs.getDate(columnNameOfPlanEnd);
        	long long_date = date.getTime();
        	long_date = long_date -time_to_substract_for_date_of_disturbace_occurance;
        	java.sql.Date sql_date = new java.sql.Date(long_date);
        	rs.updateDate(columnNameOfIstEnd, sql_date);
        	
        }else {
    		rs.updateDate(columnNameOfIstEnd, rs.getDate(columnNameOfPlanEnd));
        }
        //System.out.println("________DEBUG_______________started"+started);
   
        rs.updateRow();   //Einfügen der Zeile in die Datenbank
        rs.close();
        stmt.close();		        
    
    }		 

}
