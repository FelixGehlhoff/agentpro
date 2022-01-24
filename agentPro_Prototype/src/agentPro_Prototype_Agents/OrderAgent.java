package agentPro_Prototype_Agents;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import agentPro.onto.Location;
import agentPro.onto.Order;
import agentPro.onto.OrderPosition;
import agentPro.onto.OrderedOperation;
import agentPro.onto.Product;
import agentPro.onto.ProductionPlan;
import agentPro.onto.Production_Operation;
import agentPro_Prototype_OrderAgent_Behaviours.ReceiveInformWorkpieceCompletionBehaviour;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/*
 * Created by the interface agent. Creates Workpiece agents for each workpiece that has to be created. Collects 
 * "finish" messages by workpieces until all workpieces are completed. It then contacts the interface agent.
 */

public class OrderAgent extends _Agent_Template{

	private static final long serialVersionUID = 1L; 
	private JSONObject order;
	//private int orderNumber;
	private AID InterfaceAgent;
	private String conversationID_forInterfaceAgent;
	
	private String conversationID_forWorkpieces = "workpiece";
	private ReceiveInformWorkpieceCompletionBehaviour ReceiveInformCompletionBehaviour;
	
	private JSONArray orderPositions;
	private JSONArray finishedUnits = new JSONArray(); //keeps track of units finished
	
	//Datenbankverbindung
	public final String dbaddress = "jdbc:ucanaccess://C:/Users/Mitarbeiter/Dropbox (HSU_Agent.Pro)/_AgentPro/Prototyp/Database.accdb";	//Address od database
	private Connection connection;			//Connection to database
	private String nameOfProduction_Plan_Def_Table = "Production_Plan_Def";
	private String columnNameOfOperation = "Operation";
	private String columnNameOfStep = "Step";
	private String columnNameOfProductName = "ProductName";
	private String columnNameOfFirstOperation = "FirstOperation";
	private String columnNameOfLastOperation = "LastOperation";
		
	protected void setup (){
		super.setup();
		
         
		// / INITIALISATION
		// /////////////////////////////////////////////////////////
		
		//Datenbank
		/*
		Connection con;			
		
		try {
			con = DriverManager.getConnection(dbaddress);	// Verbindung zur DB mit ucanaccess	
			this.setConnection(con);			     
	        
	    } catch (SQLException e ) {
	        e.printStackTrace();
	    }
		*/
		//Weiteres
		
		Object[] args = getArguments();							//arguments of agent
		
		Order order = (Order) args[0];
		//String order_String = args[0].toString(); 				//String of order
		setInterfaceAgent((AID) args[1]);
		setConversationID_forInterfaceAgent(args[2].toString());	
		int orderNumber = order.getID_Number();
		
		logLinePrefix = " OrderAgent."+orderNumber+" ";
		
		String printOut = SimpleDateFormat.format(new Date())+logLinePrefix+"Order with number "+orderNumber+" received.";
			
			
		@SuppressWarnings("unchecked")
		Iterator<OrderPosition> it = order.getConsistsOfOrderPositions().iterator();
	    while(it.hasNext()) {
	    	OrderPosition orderPos = it.next();
	    	System.out.println(logLinePrefix+" orderPosition start at: "+orderPos.getStartDate()+" for Product "+orderPos.getContainsProduct().getName());
	    	/*
	    	 * 09.01.2018 quantity is always one per OrderPosition!
	    	 */
	    	
	    	//int quantity = orderPos.getQuantity();
	    	//int orderPos_Sequence_number = orderPos.getSequence_Number();

		    ProductionPlan pP = new ProductionPlan();	    
		    receiveValuesFromDB(pP, orderPos.getContainsProduct());					//production Plan for specified product is filled out with data from the database	
		    orderPos.getContainsProduct().setHasProductionPlan(pP);
		    
	    	
		    //create Workpiece Agent
		    //for(int j = 1; j <= quantity ; j++){
				ContainerController cc = this.getContainerController();
				AgentController ac;
				Object [] args_WorkpieceAgent = new Object [5];
				args_WorkpieceAgent[0] = orderPos;			
				args_WorkpieceAgent[1] = this.getAID();
				args_WorkpieceAgent[2] = conversationID_forWorkpieces;
				args_WorkpieceAgent[3] = orderNumber;
				//args_WorkpieceAgent[4] = orderPos;

				//TBD: StartLocation might have to be dynamically determined
				
				Location location = new Location();
				float startx = 0;
				float starty = 0;
				location.setCoordX(startx);
				location.setCoordY(starty);
				
				args_WorkpieceAgent[4] = location ;
				
				try {
					ac = cc.createNewAgent("WorkpieceAgentNo_"+orderNumber, "agentPro_Prototype_Agents.WorkpieceAgent", args_WorkpieceAgent);
					ac.start();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    //}
	    }			    
		
		System.out.println(printOut);
		 
		
		
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
		ReceiveInformCompletionBehaviour = new ReceiveInformWorkpieceCompletionBehaviour(this, conversationID_forWorkpieces);
        addBehaviour(ReceiveInformCompletionBehaviour);
	}

	public JSONArray getOrderPositions() {
		return orderPositions;
	}

	public void setOrderPositions(JSONArray orderPositions) {
		this.orderPositions = orderPositions;
	}

	public JSONArray getFinishedUnits() {
		return finishedUnits;
	}

	public void setFinishedUnits(JSONArray finishedUnits) {
		this.finishedUnits = finishedUnits;
	}

	public JSONObject getOrder() {
		return order;
	}

	public void setOrder(JSONObject order) {
		this.order = order;
	}

	public AID getInterfaceAgent() {
		return InterfaceAgent;
	}

	public void setInterfaceAgent(AID interfaceAgent) {
		InterfaceAgent = interfaceAgent;
	}

	public String getConversationID_forInterfaceAgent() {
		return conversationID_forInterfaceAgent;
	}

	public void setConversationID_forInterfaceAgent(String conversationID_forInterfaceAgent) {
		this.conversationID_forInterfaceAgent = conversationID_forInterfaceAgent;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	public void receiveValuesFromDB(ProductionPlan p, Product product) {
		
		String product_name = product.getName();	//name is needed to find the right production plan in the database
		
		
	    Statement stmt = null;
	    String query1 =
	        "select "+columnNameOfStep+" , "+columnNameOfOperation+" , "+columnNameOfFirstOperation+" , "+columnNameOfLastOperation+" from "+nameOfProduction_Plan_Def_Table+" where "+columnNameOfProductName+" = '"+product_name+"'";
	    
	    try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(query1);
       		
	        while (rs.next()) {
			    OrderedOperation orderedOp = new OrderedOperation();
			    Production_Operation op = new Production_Operation();
			    op.setName(rs.getString(columnNameOfOperation));
			    orderedOp.setFirstOperation(rs.getBoolean(columnNameOfFirstOperation));
			    orderedOp.setLastOperation(rs.getBoolean(columnNameOfLastOperation));
			    orderedOp.setHasProductionOperation(op);
	        	orderedOp.setSequence_Number(rs.getInt(columnNameOfStep)); 
	        	p.addConsistsOfOrderedOperations(orderedOp);		   
	        }
	        
	        
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    } finally {
	        if (stmt != null) { try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} }
	    }
		
	}

	
	void registerAtDF() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("order");
		sd.setName(Integer.toString(order.getInt("order_number")));			// order registered at DF with order Number as String
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
	}

}


