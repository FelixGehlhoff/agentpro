package agentPro_Prototype_Agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import agentPro.onto.AgentPro_ProductionOntology;
import agentPro.onto.Location;
import agentPro.onto.Order;
import agentPro.onto.OrderPosition;
import agentPro.onto.Product;
import agentPro.onto.Warehouse_Resource;
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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/*
 * Serves as a Dummy ERP System and sends Order to Interface Agent
 * Order is sent in a JSON format via ACL Message
 */

public class _DUMMY extends Agent{
	private static final long serialVersionUID = 1L;
	//Ontology
	private Ontology ontology = AgentPro_ProductionOntology.getInstance();
	private Codec codec = new SLCodec();
	private double initial_wait = 2000;
	double a = 4000;
	
	public final String dbaddress = "jdbc:ucanaccess://C:/Users/Mitarbeiter/Dropbox (HSU_Agent.Pro)/_AgentPro/Prototyp/Database.accdb";	//Address od database
	private static Connection connection;	
	private static String nameOfMES_Data = "MES_Data";
	private static String nameOfMES_Data_Resource_View = "MES_Data_Resource_View";
	
	protected void setup (){
		
		 this.addBehaviour(new ListeningBehaviour(this));
		 
		super.setup();
		String logLinePrefix = " _DUMMY ";
		String product = "ABC";
		String product2 = "ABC";
		// / INITIALISATION
		// /////////////////////////////////////////////////////////
		int x = 4;
		//String order;
		emtpyDB();
		
		
		int j;
		for (int i = 1;i<x;i++){
			Order order_onto = new Order();		
				if(i==2 || i == 4 || i == 5){
					//System.out.println("TEST GUT i = "+i);
					OrderPosition orderPos = new OrderPosition();
					Product prod = new Product();
					prod.setName(product);
					prod.setID_Number(1);
					orderPos.setContainsProduct(prod);
					orderPos.setStartDate(Long.toString(System.currentTimeMillis()+1000*60*60*24+(i-1)*1000*60*45));	//is the startdate for the first production step
					orderPos.setQuantity(1);
					orderPos.setSequence_Number(1);
						Warehouse_Resource warehouse = new Warehouse_Resource();
						Location loc = new Location();
						loc.setCoordX(100);
						loc.setCoordY(100);
						warehouse.setHasLocation(loc);
						warehouse.setName("LagerOutbound");
					orderPos.setHasTargetWarehouse(warehouse);
				
							
					order_onto.addConsistsOfOrderPositions(orderPos);
					order_onto.setID_Number(i);
					
					//order = 
					//		"{ 	\"order_number\":"+i+",\"order_positions\": 	[{	\"order_position\" : 1, \"product_name\" : \"ABC\", \"production_steps\" : [\"Casting\",\"Screwing\",\"Polishing\"],\"quantity\"	 :  2, \"due_date\"	 : 1494839827157 },{   \"order_position\" : 2, \"product_name\" : \"XYZ\", \"production_steps\" : [\"Forging\",\"Screwing\"], \"quantity\" :  1,  \"due_date\"	 : 1494839827157 }]}";
					j = i;
				}else if(i==3 || i ==6){
					OrderPosition orderPos = new OrderPosition();
					Product prod = new Product();
					prod.setName(product2);
					prod.setID_Number(1);
					orderPos.setContainsProduct(prod);
					orderPos.setStartDate(Long.toString(System.currentTimeMillis()+1000*60*60*24+(i-1)*1000*60*45));	//is the startdate for the first production step
					orderPos.setQuantity(1);
					orderPos.setSequence_Number(1);
						Warehouse_Resource warehouse = new Warehouse_Resource();
						Location loc = new Location();
						loc.setCoordX(100);
						loc.setCoordY(100);
						warehouse.setHasLocation(loc);
						warehouse.setName("LagerOutbound");
					orderPos.setHasTargetWarehouse(warehouse);
				
							
					order_onto.addConsistsOfOrderPositions(orderPos);
					order_onto.setID_Number(i);
					
					j = i;
				}else{
					OrderPosition orderPos = new OrderPosition();
					Product prod = new Product();
					prod.setName(product);
					prod.setID_Number(1);
					orderPos.setContainsProduct(prod);
					orderPos.setStartDate(Long.toString(System.currentTimeMillis()+1000*60*60*24));	//is the startdate for the first production step
					orderPos.setQuantity(1);
					orderPos.setSequence_Number(1);
						Warehouse_Resource warehouse = new Warehouse_Resource();
						Location loc = new Location();
						loc.setCoordX(100);
						loc.setCoordY(100);
						warehouse.setHasLocation(loc);
						warehouse.setName("LagerOutbound");
					orderPos.setHasTargetWarehouse(warehouse);
				
							
					order_onto.addConsistsOfOrderPositions(orderPos);
					order_onto.setID_Number(i);
					//order = 
					//		"{ 	\"order_number\":"+i+",\"order_positions\": 	[{   \"order_position\" : 1, \"product_name\" : \"XYZ\", \"production_steps\" : [\"Forging\",\"Screwing\"], \"quantity\" :  1,  \"due_date\"	 : 1494839827157 }]}";
					j = i;
				}
				
				//Ontology
				getContentManager().registerOntology(ontology);
				getContentManager().registerLanguage(codec);
			
			AID receiver = new AID();
			String localName = "InterfaceAgent";
			receiver.setLocalName(localName);
			
			Random r = new Random();
			double d = r.nextDouble();
			if(j ==1) {
				d = initial_wait;
			}else {
				d = a + d*initial_wait ;	
			}
		
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);	
			msg.addReceiver(receiver);
			//msg.setContent(order);
			msg.setConversationId("ERP");

			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
			
			_Incoming_Order incOrder = new _Incoming_Order();	
			incOrder.setHasOrder(order_onto);
			
			Action content = new Action(this.getAID(), incOrder);
			
			try {
				this.getContentManager().fillContent(msg, content);
			} catch (CodecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OntologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			try {
				Thread.sleep((long) d);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.send(msg);		

			System.out.println(logLinePrefix+" Sender Agent: "+this.getName()+" INFORM sent to receiver: "+receiver.getName()+ "content "+msg.getContent());
			
		}
		
		
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
	private void emtpyDB() {
		Connection con;		
		
		try {
			con = DriverManager.getConnection(dbaddress);	// Verbindung zur DB mit ucanaccess	
			connection = con;			     
	        
	    } catch (SQLException e ) {
	        e.printStackTrace();
	    }

			Statement stmt;
			try {
				stmt = connection.createStatement(
				        ResultSet.TYPE_SCROLL_SENSITIVE, // Vor- und Rücksprünge möglich
				        ResultSet.CONCUR_UPDATABLE);
				
				Boolean execution = stmt.execute("Delete * from "+nameOfMES_Data);
				Boolean execution2 = stmt.execute("Delete * from "+nameOfMES_Data_Resource_View);

		       
		        stmt.close();		
		        
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}     // Veränderbar
	    	
	    	        
	    
	    
		
	}
	private class ListeningBehaviour extends CyclicBehaviour{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private ListeningBehaviour(Agent a){
			
		}

		@Override
		public void action() {
			// Receive message

			MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	        MessageTemplate mt2 = MessageTemplate.MatchConversationId("ERP");	
	        MessageTemplate mt_total = MessageTemplate.and(mt1,mt2);
	        
			ACLMessage inform = myAgent.receive(mt_total);
			if (inform != null) {
				System.out.println(System.currentTimeMillis()+" ERP received. "+inform.getSender().getLocalName()+" "+inform.getPostTimeStamp());
			}else {
				block();
			}
			
		}
		
	}
	

}
