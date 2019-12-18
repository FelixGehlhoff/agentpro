package DatabaseConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import agentPro.onto.Location;
import agentPro.onto.OrderPosition;
import agentPro.onto.OrderedOperation;
import agentPro.onto.Product;
import agentPro.onto.ProductionPlan;
import agentPro.onto.Production_Operation;
import agentPro.onto.Warehouse_Resource;
import agentPro_Prototype_Agents._Agent_Template;
import agentPro_Prototype_Agents._Simulation_Order_Generator;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import webservice.ManufacturingOrder;

public class orderGenerationBehaviour extends OneShotBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private _Simulation_Order_Generator myAgent;
	private ArrayList<ProductionPlan> productionPlans = new ArrayList<ProductionPlan>();
	
	private String columnNameOfStep = "Step";
	private String columnNameOfOperation = "Operation";
	private String columnNameOfFirstOperation = "FirstOperation";
	private String columnNameOfLastOperation = "LastOperation";
	private String nameOfProduction_Plan_Def_Table = _Agent_Template.prefix_schema+".production_plan_def";
	private String columnNameOfProductName = "ProductName";
	private int numberOfProducts = 3;
	
	public static String conversationID_forInterfaceAgent = "OrderAgent"; //for directly contacting the interface agent
	public static DFAgentDescription interface_agent;
	private long initial_wait =1000;
	private double wait_between_agent_creation = 2500;
	private String columnNameOfFollowUpConstraint = "hasFollowUpOperationConstraint";
	private String columnNameOfWithStep = "withStep";
	private String columnNameLocationX = "locationX";
	private String columnNameLocationY = "locationY";
	
	


	public orderGenerationBehaviour(_Simulation_Order_Generator myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
	}

	@Override
	public void action() {
		//find interface agent
		searchDFForInterfaceAgent(myAgent);
		//receive Production Plans from DB
		 receiveProductionPlansFromDB(productionPlans);
		
		 try {
				Thread.sleep(initial_wait);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
		//receive all data from sheet orderbook
		if(_Agent_Template.simulate_order_generation) {
			simluateOrder();
		}else {
			ArrayList<OrderPosition> orders = new ArrayList<OrderPosition>();
			receiveOrderDataFromDB(orders);
		}	
	}

	private void simluateOrder() {
		
		Statement stmt = null;
		//String query = "select "+myAgent.columnNameID+" , "+columnNameProduct+" , "+columnNameNumber+" , "+columnNameTargetWarehouse+" from "+nameOfOrderbook; 	    
		String query = "select * from agentpro.betriebskalender where Bezeichnung = 'Fraese_2'"; 
		//SELECT * FROM People ORDER BY FirstName DESC, YearOfBirth ASC
		OrderPosition orderPos = new OrderPosition();
	    try {
	        stmt = myAgent.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);	   
	        ResultSet rs = stmt.executeQuery(query);
	        while (rs.next()) {
	        	
	        	orderPos.setDueDate(_Agent_Template.SimpleDateFormat.format(rs.getTime("StartSoll")));
	        	orderPos.setReleaseDate(_Agent_Template.SimpleDateFormat.format(rs.getTime("StartSoll")));        	
    			orderPos.setStartDate(_Agent_Template.SimpleDateFormat.format(rs.getTimestamp("StartSoll")));
	        	orderPos.setEndDate_String(_Agent_Template.SimpleDateFormat.format(rs.getTimestamp("StartSoll")));
	        }
	    }catch (SQLException e ) {
	    	e.printStackTrace();
	    } 
	    _Agent_Template.limit = 2;
		int number_of_orders = _Agent_Template.limit;						//create x orders
		double proportion_AvsB = 1;
		

    	String prod_name = "";
    	Product product = new Product();
		for(int i = 1; i<=number_of_orders;i++) {
			double rand = Math.random();
			//int quantity = Math.max(5, (int) (Math.random()*20));
			int quantity = 10;
			if(rand>proportion_AvsB) {
				prod_name = "A";
			}else {
				prod_name = "B";
			}	
			product.setName(prod_name);
			orderPos.setContainsProduct(product);
			orderPos.setQuantity(quantity);
	    	for(ProductionPlan pP : productionPlans) {
	    		
	    		if(pP.getDefinesProduct().getName().equals(prod_name)) {
	    			 orderPos.getContainsProduct().setHasProductionPlan(pP);    			
	    				orderPos.setSequence_Number(i);   			
	    				Warehouse_Resource warehouse = new Warehouse_Resource();
						Location loc = new Location();
						//loc.setCoordX(100);
						//loc.setCoordY(100);
						//warehouse.setHasLocation(loc);
						warehouse.setName("Exit");
						//warehouse.setID_Number(rs.getInt(myAgent.columnNameID));
						//name_of_exit = rs.getString(myAgent.columnNameOfTargetWarehouse);
						
				        		loc.setCoordX((float)60);
				        		loc.setCoordY((float)5);
				        	 
							 warehouse.setHasLocation(loc);
	    			 orderPos.setHasTargetWarehouse(warehouse);    
	    			
	    			 
	    			 break;
	    		}
	    	}
 		    //create Workpiece Agent
 		    //for(int j = 1; j <= quantity ; j++){
 				ContainerController cc = myAgent.getContainerController();
 				AgentController ac;
 				Object [] args_WorkpieceAgent = new Object [5];
 				args_WorkpieceAgent[0] = orderPos;		
 				//System.out.println("DEBUG_____"+orderPos.getHasTargetWarehouse().getHasLocation().getCoordX());
 				args_WorkpieceAgent[1] = interface_agent.getName();
 				args_WorkpieceAgent[2] = conversationID_forInterfaceAgent;
 				args_WorkpieceAgent[3] = orderPos.getSequence_Number();
 				//args_WorkpieceAgent[4] = orderPos;

 				//TBD: StartLocation might have to be dynamically determined	--> now = Coordinates of Wickelfertigung
 				
 				Location location = new Location();
 				float startx = 5;
 				float starty = 5;
 				location.setCoordX(startx);
 				location.setCoordY(starty);
 				
 				args_WorkpieceAgent[4] = location ;
 				
 				try {
 					//ac = cc.createNewAgent("WorkpieceAgentNo_"+orderPos.getSequence_Number(), "agentPro_Prototype_Agents.WorkpieceAgent", args_WorkpieceAgent);
 					ac = cc.createNewAgent(orderPos.getContainsProduct().getName()+"_"+orderPos.getSequence_Number()+"_Order", "agentPro_Prototype_Agents.WorkpieceAgent", args_WorkpieceAgent);
 					
 					ac.start();
 				} catch (StaleProxyException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				//Random r = new Random();
 				//double d = r.nextDouble();
 				/*double d = 0;
 				if(i ==1) {
 					d = initial_wait;
 				}else {
 					d = i*a + initial_wait ;	
 				}*/
 				
 				try {
 					Thread.sleep((long) wait_between_agent_creation);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}   	
		} 	

    	
    	
    	

		

		
	}

	public static void searchDFForInterfaceAgent(Agent myAgent) {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			String service_type = "interface";
			sd.setType(service_type); 	
			String service_name = "interface";		
			sd.setName(service_name);
			template.addServices(sd);
			
			try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
				if (result.length != 0){					
					interface_agent = result[0];
				
				}else {
					System.out.println(_Agent_Template.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+" no Interface Agent was found");
				}
			}
				 catch (FIPAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				 }
			
		
	}

	private void receiveProductionPlansFromDB(ArrayList<ProductionPlan> productionPlans) {


	    Statement stmt = null;
	
	   
	    	String print = "DEBUG___orderGen pp added ";
	    	    try {
	    	    	stmt = myAgent.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	    	    	
	    	    for(int i = 1;i<=numberOfProducts ;i++) {
	    	 	    ProductionPlan pP = new ProductionPlan();
	    	    	String query1 = "select "+columnNameOfStep+" , "+myAgent.columnNameID+" , "+columnNameOfOperation+" , "+columnNameOfFirstOperation+" , "+columnNameOfLastOperation+" , "+columnNameOfProductName +" , "+columnNameOfFollowUpConstraint +" , "+columnNameOfWithStep +" from "+nameOfProduction_Plan_Def_Table+" where "+myAgent.columnNameID+" = "+i;	     
	    	        ResultSet rs = stmt.executeQuery(query1);
	           		Product product = new Product();
	    	        while (rs.next()) {
	    	        	 OrderedOperation orderedOp = new OrderedOperation();
	    				    Production_Operation op = new Production_Operation();
	    				    op.setName(rs.getString(columnNameOfOperation));
	    				    op.setType("production");
	    				    orderedOp.setFirstOperation(rs.getBoolean(columnNameOfFirstOperation));
	    				    orderedOp.setLastOperation(rs.getBoolean(columnNameOfLastOperation));
	    				    orderedOp.setHasProductionOperation(op);
	    		        	orderedOp.setSequence_Number(rs.getInt(columnNameOfStep)); 
	    		        	orderedOp.setHasFollowUpOperation(rs.getBoolean(columnNameOfFollowUpConstraint));
	    		        	orderedOp.setWithOperationInStep(rs.getInt(columnNameOfWithStep));
	    	        		product.setName(rs.getString(columnNameOfProductName));
	    	        		pP.addConsistsOfOrderedOperations(orderedOp);	   
	    	        		print = print + op.getName()+" "+i+" ";
	    	        }
		        	pP.setDefinesProduct(product);
	        	    productionPlans.add(pP);  	 
	    	    }
	    	    
	    	    } catch (SQLException e ) {
	    	    	e.printStackTrace();
	    	    } 
	    	    System.out.println(print);
	  
	    
		
	}

	private void receiveOrderDataFromDB(ArrayList<OrderPosition> orders) {
		Statement stmt = null;
		Statement stmt2 = null;
		//String query = "select "+myAgent.columnNameID+" , "+columnNameProduct+" , "+columnNameNumber+" , "+columnNameTargetWarehouse+" from "+nameOfOrderbook; 	    
		String query = "select * from "+_Agent_Template.nameOfOrderbook+" ORDER BY "+myAgent.columnNameOfDueDate+", "+myAgent.columnNameOfProduct+", "+myAgent.columnNameOfPriority+", "+myAgent.columnNameOfNumber+" DESC"; 
		//SELECT * FROM People ORDER BY FirstName DESC, YearOfBirth ASC
		
	    try {
	        stmt = myAgent.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	        stmt2 = myAgent.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	        ResultSet rs = stmt.executeQuery(query);
	       
	        int i = 1;
	        while (rs.next() && i<=_Agent_Template.limit) {
	        	String query2 = "select "+columnNameLocationX+" , "+myAgent.columnNameID+" , "+columnNameLocationY+" from "+myAgent.tableNameResource+" where "+myAgent.columnNameResourceName_simulation+" = ";
	        		        	
	        	OrderPosition orderPos = new OrderPosition();
	        	orderPos.setQuantity(rs.getInt(myAgent.columnNameOfNumber));
	        	
	        	//15.01.19 dates for backwards calculation etc.
	        	orderPos.setDueDate(_Agent_Template.SimpleDateFormat.format(rs.getTime(myAgent.columnNameOfDueDate)));
	        	orderPos.setReleaseDate(_Agent_Template.SimpleDateFormat.format(rs.getTime(myAgent.columnNameOfReleaseDate)));
	        	 if(_Agent_Template.simulation_enercon_mode) {
    				 orderPos.setStartDate(String.valueOf(myAgent.start_simulation));
    			 }else {
    				 orderPos.setStartDate(_Agent_Template.SimpleDateFormat.format(rs.getTimestamp(_Agent_Template.columnNameOfPlanStart)));
    			}
	        
	        	orderPos.setEndDate_String(_Agent_Template.SimpleDateFormat.format(rs.getTimestamp(_Agent_Template.columnNameOfPlanEnd)));
	        	
	        	String prod_name = rs.getString(myAgent.columnNameOfProduct);
	        	Product product = new Product();
	        	product.setName(prod_name);
	        	orderPos.setContainsProduct(product);
	        	for(ProductionPlan pP : productionPlans) {
	        		//System.out.println("DEBUG____"+prod_name+" pP.getDefinesProduct().getName() "+pP.getDefinesProduct().getName());
	        		if(pP.getDefinesProduct().getName().equals(prod_name)) {
	        			 orderPos.getContainsProduct().setHasProductionPlan(pP);
	        			 if(_Agent_Template.simulation_enercon_mode) {
	        				 orderPos.setSequence_Number(i);
	        			 }else {
	        				 orderPos.setSequence_Number(rs.getInt(myAgent.columnNameID)); 
	        			 }
	        			
	        				Warehouse_Resource warehouse = new Warehouse_Resource();
							Location loc = new Location();
							//loc.setCoordX(100);
							//loc.setCoordY(100);
							//warehouse.setHasLocation(loc);
							warehouse.setName(rs.getString(myAgent.columnNameOfTargetWarehouse));
							warehouse.setID_Number(rs.getInt(myAgent.columnNameID));
							//name_of_exit = rs.getString(myAgent.columnNameOfTargetWarehouse);
							query2 = query2+"'"+warehouse.getName()+"'";
								 ResultSet rs2 = stmt2.executeQuery(query2);
								 while (rs2.next()) {
					        		loc.setCoordX((float)rs2.getInt(columnNameLocationX));
					        		loc.setCoordY((float)rs2.getInt(columnNameLocationY));
					        	  }
								 warehouse.setHasLocation(loc);
	        			 orderPos.setHasTargetWarehouse(warehouse);    
	        			
	        			 
	        			 break;
	        		}
	        	}
	        	
	        	 
	        	createAgent(myAgent, orderPos);
	        	
	        	try {
					Thread.sleep((long) wait_between_agent_creation);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   	
	         		 
	 				i++;
	        }
	       
	        
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    } 
		
	}
	
	public static void createAgent(Agent myAgent, OrderPosition orderPos) {
		ContainerController cc = myAgent.getContainerController();
			AgentController ac;
			Object [] args_WorkpieceAgent = new Object [5];
			args_WorkpieceAgent[0] = orderPos;		
			//System.out.println("DEBUG_____"+orderPos.getHasTargetWarehouse().getHasLocation().getCoordX());
			args_WorkpieceAgent[1] = interface_agent.getName();
			args_WorkpieceAgent[2] = conversationID_forInterfaceAgent;
			args_WorkpieceAgent[3] = orderPos.getSequence_Number();
			//args_WorkpieceAgent[4] = orderPos;

			//TBD: StartLocation might have to be dynamically determined	--> now = Coordinates of Wickelfertigung
			
			Location location = new Location();
			float startx = 5;
			float starty = 5;
			location.setCoordX(startx);
			location.setCoordY(starty);
			
			args_WorkpieceAgent[4] = location ;
			
			try {
				//ac = cc.createNewAgent("WorkpieceAgentNo_"+orderPos.getSequence_Number(), "agentPro_Prototype_Agents.WorkpieceAgent", args_WorkpieceAgent);
				ac = cc.createNewAgent(orderPos.getContainsProduct().getName()+"_"+orderPos.getSequence_Number()+"_Order", "agentPro_Prototype_Agents.WorkpieceAgent", args_WorkpieceAgent);
				
				ac.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
	}
	
	public static void createAgent(Agent myAgent, OrderPosition orderPos, ManufacturingOrder mo) {
		ContainerController cc = myAgent.getContainerController();
			AgentController ac;
			Object [] args_WorkpieceAgent = new Object [6];
			args_WorkpieceAgent[0] = orderPos;		
			//System.out.println("DEBUG_____"+orderPos.getHasTargetWarehouse().getHasLocation().getCoordX());
			args_WorkpieceAgent[1] = interface_agent.getName();
			args_WorkpieceAgent[2] = conversationID_forInterfaceAgent;
			args_WorkpieceAgent[3] = orderPos.getSequence_Number();
			//args_WorkpieceAgent[4] = orderPos;

			//TBD: StartLocation might have to be dynamically determined	--> now = Coordinates of Wickelfertigung
			
			Location location = new Location();
			float startx = 5;
			float starty = 5;
			location.setCoordX(startx);
			location.setCoordY(starty);
			
			args_WorkpieceAgent[4] = location ;
			args_WorkpieceAgent[5] = mo;
			
			try {
				//ac = cc.createNewAgent("WorkpieceAgentNo_"+orderPos.getSequence_Number(), "agentPro_Prototype_Agents.WorkpieceAgent", args_WorkpieceAgent);
				ac = cc.createNewAgent(orderPos.getContainsProduct().getName()+"_"+orderPos.getSequence_Number()+"_Order", "agentPro_Prototype_Agents.WorkpieceAgent", args_WorkpieceAgent);
				
				ac.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
	}
	public void receiveValuesFromDB(ProductionPlan p, Product product) {
		
		String product_name = product.getName();	//name is needed to find the right production plan in the database
		
		
	    Statement stmt = null;
	    String query1 =
	        "select "+columnNameOfStep+" , "+columnNameOfOperation+" , "+columnNameOfFirstOperation+" , "+columnNameOfLastOperation+" from "+nameOfProduction_Plan_Def_Table+" where "+columnNameOfProductName+" = '"+product_name+"'";
	    
	    try {
	        stmt = myAgent.getConnection().createStatement();
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
	

}
