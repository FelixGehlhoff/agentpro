package DatabaseConnection;


import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections4.comparators.ComparatorChain;

import agentPro.onto.Location;
import agentPro.onto.Material;
import agentPro.onto.OrderPosition;
import agentPro.onto.OrderedOperation;
import agentPro.onto.Product;
import agentPro.onto.ProductionPlan;
import agentPro.onto.Production_Operation;
import agentPro.onto.Warehouse_Resource;
import agentPro_Prototype_Agents._Agent_Template;
import agentPro_Prototype_Agents._Simulation_Order_Generator;

import jade.core.behaviours.OneShotBehaviour;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import support_classes.DatabaseValues;
import webservice.ManufacturingOrder;
import webservice.ManufacturingOrderList;
import webservice.ProcessSchedule;
import webservice.ProcessStep;
import webservice.Webservice_agentPro;

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
	    String query = "SELECT * FROM "+DatabaseValues.tableNameResource+" where "+DatabaseValues.columnNameResourceType+" != 'simulation'";
	    
	   
	    try {
	        //stmt = myAgent.getConnection().createStatement();
	        stmt = myAgent.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	        ResultSet rs = stmt.executeQuery(query);
	        
	        while (rs.next()) {
	        	/*
	        	try {
					Thread.sleep(150);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
	        	Boolean createAgent = true;
	        	String name = rs.getString(DatabaseValues.columnNameResourceName_simulation);
	        	//int id = rs.getInt(columnNameID);
	        	String res_type = rs.getString(DatabaseValues.columnNameResourceType);
	        	String res_detailed_type = rs.getString(DatabaseValues.columnNameResourceDetailedType);
	        	String path_for_agent_class = "agentPro_Prototype_Agents.";
	        	if(res_type.equals("transport") && res_detailed_type.equals("buffer")) {
	        		path_for_agent_class = path_for_agent_class + "BufferAgent_Stringer";
	        	}else if(res_type.equals("production") && res_detailed_type.equals("buffer")){
	        		path_for_agent_class = path_for_agent_class + "BufferAgent_new";
	        	}else if(res_type.equals("transport") && res_detailed_type.equals("crane_rail")){
	        		path_for_agent_class = path_for_agent_class + "Crane_RailAgent";
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
	    
	    
	  //für Projekt / Webservice
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		orderGenerationBehaviour.searchDFForInterfaceAgent(myAgent);
		
		if(_Agent_Template.webservice_mode) {
			createAgentsAccordingToWebservice();
		}else {			 
		    //activate orders			
		    orderGenerationBehaviour orders = new orderGenerationBehaviour(myAgent);
			myAgent.addBehaviour(orders);	
		}
		
	  
	}
	
	@SuppressWarnings("unchecked")
	private void createAgentsAccordingToWebservice() {
		HttpClient client = HttpClient.newBuilder()	
				.version(HttpClient.Version.HTTP_1_1)			
				  .build();
		HttpRequest request = Webservice_agentPro.buildRequest(Webservice_agentPro.soapAction_getAll, Webservice_agentPro.soap_getAll);
		String return_string = null;
		try {
			return_string = client.sendAsync(request, BodyHandlers.ofString())
					.thenApply(HttpResponse::body).get();
		} catch (InterruptedException | ExecutionException e1) {			
			e1.printStackTrace();
		}
		
		ManufacturingOrderList mol = Webservice_agentPro.unmarshallStringToManufacturingOrderList(return_string);
		
		//Sortierung
		@SuppressWarnings("rawtypes")
		ComparatorChain chain = new ComparatorChain();  
        chain.addComparator(comparatorDuedate);  
        chain.addComparator(comparatorMaterial);  
        	
		Collections.sort(mol.manufacturingorders, chain);
		//for(ManufacturingOrder mo : mol.manufacturingorders) {
		//	System.out.println("ID: "+mo.ID+" Delivery Date: "+mo.DeliveryDate+" Article: "+mo.Article.Name);
		//}
		
		for(ManufacturingOrder mo : mol.manufacturingorders) {
			OrderPosition orderPos = new OrderPosition();
        	orderPos.setQuantity(mo.Quantity);
        	orderPos.setDueDate(_Agent_Template.SimpleDateFormat.format(mo.DeliveryDate));
        	orderPos.setReleaseDate(_Agent_Template.SimpleDateFormat.format(System.currentTimeMillis()));
        	orderPos.setSequence_Number(mo.ID);   	
        	orderPos.setStartDate(_Agent_Template.SimpleDateFormat.format(System.currentTimeMillis()));
			Warehouse_Resource warehouse = new Warehouse_Resource();
			Location loc = new Location();
			//loc.setCoordX(100);
			//loc.setCoordY(100);
			//warehouse.setHasLocation(loc);
			warehouse.setName("Exit");
			//warehouse.setID_Number(rs.getInt(myAgent.columnNameID));
			//name_of_exit = rs.getString(myAgent.columnNameOfTargetWarehouse);
			
	        		loc.setCoordX((float)20);
	        		loc.setCoordY((float)10);
	        	 
				 warehouse.setHasLocation(loc);
		 orderPos.setHasTargetWarehouse(warehouse);    
			ProductionPlan pp = createProductionPlan(mo.ActualProcess.ProcessSchedule);
			Product p = new Product();
			p.setName(mo.Article.Name);
			p.setHasProductionPlan(pp);
			orderPos.setContainsProduct(p);
			orderGenerationBehaviour.createAgent(myAgent, orderPos, mo);
			try {
				Thread.sleep((long) 2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
		}
		
		
	}
	
	Comparator<ManufacturingOrder> comparatorDuedate = new Comparator<ManufacturingOrder>() {  
        @Override  
        public int compare(ManufacturingOrder m1, ManufacturingOrder m2) {  
        	return m1.DeliveryDate.compareTo(m2.DeliveryDate); 
        	//return o1.name.compareToIgnoreCase(o2.name);  
        }  
   };  
   Comparator<ManufacturingOrder> comparatorMaterial = new Comparator<ManufacturingOrder>() {  
       @Override  
       public int compare(ManufacturingOrder m1, ManufacturingOrder m2) {  
       	return m1.Article.Name.compareToIgnoreCase(m2.Article.Name);
       	//return o1.name.compareToIgnoreCase(o2.name);  
       }  
  };  
	
	private ProductionPlan createProductionPlan(ProcessSchedule processSchedule) {
		ProductionPlan pp = new ProductionPlan();
		OrderedOperation[] array = new OrderedOperation [4];	//andere Schritte erstmal nicht?
		for(ProcessStep ps : processSchedule.ProcessSteps) {
			OrderedOperation oop = new OrderedOperation();
			Production_Operation op = new Production_Operation();
				//String name_edited = ps.Name.substring(3) ;
				String name_edited2 = ps.Name.replace("amp;", "");
			op.setName(name_edited2);
			op.setAvg_Duration((float) ps.JobTimeMinutes);
			op.setSet_up_time((float) ps.SetUpTimeMinutes);
			if(ps.ConsumableMaterial.size()>0) {
				Material m = new Material();
				m.setName(ps.ConsumableMaterial.get(0).Article.Name);
				op.setRequiresMaterial(m);	
			}		
			oop.setHasProductionOperation(op);
			oop.setSequence_Number(ps.Order);
			if(ps.Order == 1) {	oop.setFirstOperation(true);}
			if(ps.Name.contains("Zerspanen")) {	oop.setLastOperation(true);}
			if(ps.Order < 5) {
				array[ps.Order-1] = oop;
			}				
		}
		for (int i = 0; i < array.length ; i++) {
			pp.getConsistsOfOrderedOperations().add(array[i]);
		}

		return pp;
	}


}
