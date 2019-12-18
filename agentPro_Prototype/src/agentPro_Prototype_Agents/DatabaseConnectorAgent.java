package agentPro_Prototype_Agents;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import support_classes.Resource_Extension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import DatabaseConnection.ReceiveDatabaseQueryRequestBehaviour;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Location;
import agentPro.onto.Resource;
import agentPro.onto.WorkPlan;
import support_classes.GeneralFunctions;


public class DatabaseConnectorAgent extends _Agent_Template{
	private static final long serialVersionUID = 1L;
	
	protected ReceiveDatabaseQueryRequestBehaviour ReceiveDatabaseQueryRequestBehaviour;
	
	private String DateFormat = "yyyy-MM-dd HH:mm:ss";
	public SimpleDateFormat SimpleDateFormat = new SimpleDateFormat(DateFormat);
	public String logLinePrefix;
	//private String dbaddress_SQL = "jdbc:mysql://localhost:3306/MySQL?"+"user=root&password=SQL_0518&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";	//Address od database;
	//public String ability;
	
	//Datenbankverbindung
		//public final String dbaddress = "jdbc:ucanaccess://C:/Users/Mitarbeiter/Dropbox (HSU_Agent.Pro)/_AgentPro/Prototyp/Database.accdb";	//Address od database
		//private Connection connection;			//Connection to database

	private HashMap<Integer, Resource_Extension> resource_hashmap = new HashMap<Integer, Resource_Extension>();
	
	
	protected void setup (){
		super.setup();
		logLinePrefix = " DatabaseConnectorAgent ";
		// / INITIALISATION
		// /////////////////////////////////////////////////////////
		registerAtDF();
		
		//Datenbank
		activateConnection();
		createResourceHashMap();		


		
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
        ReceiveDatabaseQueryRequestBehaviour = new ReceiveDatabaseQueryRequestBehaviour(this);
        addBehaviour(ReceiveDatabaseQueryRequestBehaviour);
	}

	/*
	public void addDataToDatabase(String agent_type, WorkPlan workplan) throws SQLException {
		
		Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	ResultSet rs = stmt.executeQuery(		
 	            "SELECT * FROM ("+"mydb.processtimes"+")"); //Selektiere alle Spalten (*) der Tabelle tblOrder in SQL Sprache
    	rs.moveToInsertRow();
    	int number = 0;
    	long div = 10000000;
    	number = number + (int) (System.nanoTime()/div);
    	double ms = System.currentTimeMillis();
    	//rs.updateFloat("Proc1", number);
    	rs.updateInt("NanoSeconds", number);
    	rs.updateDouble("MilliSeconds", ms);
    	System.out.println(number);
    	   rs.insertRow();   //Einfügen der Zeile in die Datenbank

	        // Close ResultSet and Statement
	        rs.close();
	        //stmt.close();		        
	}
	*/
	
	//erstmal nur WP einträge?
	public void addDataToDatabase(String agent_type, WorkPlan workplan, String sender) throws SQLException {
		//System.out.println("DEBUG___DATABASE CONNECTOR ADD TO DATABASE FROM SENDER = "+sender);
		int order_id = 0;
		if(agent_type.equals("workpiece")){
			String split [] = sender.split("_");
			order_id = Integer.parseInt(split[1]);
		}else {
			
		}
		
		
		if(_Agent_Template.simulation_enercon_mode) {
			if(this.getConnection().isClosed()) {
				activateConnection();
			}
			try (Connection con = this.getConnection(); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
					){
				
				//String query = "Select * from "+this.nameOfProductionPlan+" where "+this.columnNameID+" = "+order_id;
				//01.02.19 Orders sollen in der Reihenfolge eingetragen werden, wie sie kommen
				addToFlexsimLayoutTable(stmt, order_id, workplan, this.tableNameProductionPlan);
				
				//createRowEntries(rs, workplan, order_id);
				//rs.updateRow();   //Einfügen der Zeile in die Datenbank
		        // Close ResultSet and Statement
		        //rs.close();


			}	
		}
		//anders schema
		
		//else {
			String mes_table_to_be_used = "";
			switch (agent_type) {
			case "workpiece":
				mes_table_to_be_used = nameOfMES_Data;
				break;
			case "resource":
				mes_table_to_be_used = nameOfMES_Data_Resource;
				break;
			}
			
			//Datenbank statement etc. are closed automatically	
			/*
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			
	    	
	    	
			try (Connection con = DriverManager.getConnection(dbaddress_sim); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
					){

				@SuppressWarnings("unchecked")
				Iterator<AllocatedWorkingStep> it = workplan.getConsistsOfAllocatedWorkingSteps().iterator();
			    while(it.hasNext()) {
			    	AllocatedWorkingStep allWorkingStep = it.next();
			    	
			    	if(!allWorkingStep.getIsFinished()) {		//if the step is not already finished --> update or put it into database
				    	//A Statement is an interface that represents a SQL statement. You execute Statement objects, 
						//and they generate ResultSet objects, which is a table of data representing a database result set. 
						//You need a Connection object to create a Statement object.
				    	
				    	//Statement stmt = con.createStatement(
					     //       ResultSet.TYPE_SCROLL_SENSITIVE, // Vor- und Rücksprünge möglich
					     //       ResultSet.CONCUR_UPDATABLE);     // Veränderbar

			    		String new_name = replaceName(allWorkingStep.getHasOperation().getName(), allWorkingStep.getHasResource().getID_Number(), allWorkingStep.getHasResource().getName());
				    //12.02.2019 rs ERstellung ausgeschnitten
				    	ResultSet rs2 = stmt.executeQuery(		
				    			//"select * from "+nameOfMES_Data+" where "+columnNameOfOperation+" = '"+allWorkingStep.getHasOperation().getName()+"' and "+columnNameAuftrags_ID+" = '"+allWorkingStep.getHasOperation().getAppliedOn().getID_String()+"' and "+columnNameFinished+" = 'false'"); 
				    			"select * from "+mes_table_to_be_used+" where "+columnNameOfOperation+" = '"+new_name+"' and "+columnNameAuftrags_ID+" = '"+allWorkingStep.getHasOperation().getAppliedOn().getID_String()+"'"); 
						// System.out.println("mes__table_to_be_used "+mes_table_to_be_used);      
				    	if (rs2.isBeforeFirst() ) {  //the SQL query has returned data  
				    		rs2.next(); 
				        	//rs.moveToInsertRow();		  //Jetzt steht das RS in der "Insert" Zeile, diese ist quasi "separat" zum Rest der Tabelle  
				            //RS bzw. jede Spalte wird mit Werten gefüllt	  
				    		
				    		//Calendar cal = Calendar.getInstance();
				    		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Germany/Hamburg"));
				    		cal.setTimeInMillis(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()));
				    		
				    		//java.sql.Date sql_date = new java.sql.Date(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()));
				    		//java.sql.Timestamp sql_timestamp = new java.sql.Timestamp(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()));
				    		//rs2.updateDate(columnNameOfPlanStart, sql_date);		
				    		//rs2.updateTime(columnNameOfPlanStart, sql_date.getTime());
				    		
				    		rs2.updateTimestamp(columnNameOfPlanStart, new java.sql.Timestamp(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate())));
				    		//rs2.updateInt(columnNameOfPlanStart_zahl, );
				    	    //java.sql.Date sqlDate = new java.sql.Date(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()));
				    			//System.out.println("DEBUG______agent_Template____________sqldate "+sqlDate+" sqlDate.getTime() "+sqlDate.getTime());
				 	        //rs2.updateDate(columnNameOfPlanEnd, new java.sql.Date(Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate())));	
				 	       rs2.updateTimestamp(columnNameOfPlanEnd, new java.sql.Timestamp(Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate())));
				 	       
				 	       //simulation times
				 	       	double hours_sim_time_start_soll = (double) (Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()) - this.start_simulation_agentpto) / (1000*60);
				    		double hours_sim_time_start_soll_rounded = GeneralFunctions.round(hours_sim_time_start_soll,4);
				    		double hours_sim_time_end_soll = (double) (Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate()) - this.start_simulation_agentpto) / (1000*60);							    		
				    		double hours_sim_time_end_soll_rounded = GeneralFunctions.round(hours_sim_time_end_soll,4);		
				    		
				    		rs2.updateDouble(columnNameStartSimulation, hours_sim_time_start_soll_rounded);	
				    		rs2.updateDouble(columnNameEndSimulation, hours_sim_time_end_soll_rounded);		
			
				 	        //only those should be found and updated that are not finished already
				 	        rs2.updateBoolean(columnNameOfStarted, false);
				 	   
				 	        rs2.updateString(columnNameOfResource, allWorkingStep.getHasResource().getName());	  
				 	        //System.out.println("________DEBUG_______________new start "+allWorkingStep.getHasTimeslot().getStartDate()+" for operation "+allWorkingStep.getHasOperation().getName());
				       
				            rs2.updateRow();   //Einfügen der Zeile in die Datenbank
				            rs2.close();
				            //stmt.close();		        
				    	}else {
				 	        //Updates the designated column with a String value. The updater methods are used to update column values 
				 	        //in the current row or the insert row. The updater methods do not update the underlying database; 
				 	        //instead the updateRow or insertRow methods are called to update the database.
				    	//Man sollte die Tabellennamen als String ansprechen - Ansprache mit Zahlen führt häufig zu Fehlern
				    		
				    		ResultSet rs = stmt.executeQuery(		
					 	            "SELECT * FROM "+mes_table_to_be_used); //Selektiere alle Spalten (*) der Tabelle tblOrder in SQL Sprache 
				    		
				    	rs.moveToInsertRow();		  //Jetzt steht das RS in der "Insert" Zeile, diese ist quasi "separat" zum Rest der Tabelle  
				        //RS bzw. jede Spalte wird mit Werten gefüllt
				    
				    	if(new_name == "no match") {
				    		System.out.println(logLinePrefix+"  ERROR Matching Database: Name of Operation");
				    	}
				        rs.updateString(columnNameOfResource, allWorkingStep.getHasResource().getName());	  
				        rs.updateString(columnNameOfOperation, new_name);
				        rs.updateInt(columnNameOfResource_ID, allWorkingStep.getHasResource().getID_Number());		        
				        //rs.updateDate(columnNameOfPlanStart, new java.sql.Date(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate())));	        
				        //rs.updateDate(columnNameOfPlanEnd, new java.sql.Date(Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate())));	
				        rs.updateTimestamp(columnNameOfPlanStart, new java.sql.Timestamp(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate())));
				        rs.updateTimestamp(columnNameOfPlanEnd, new java.sql.Timestamp(Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate())));
				 	       //simulation times
			 	       	double hours_sim_time_start_soll = (double) (Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()) - this.start_simulation_agentpto) / (1000*60);
			    		double hours_sim_time_start_soll_rounded = GeneralFunctions.round(hours_sim_time_start_soll,4);
			    		double hours_sim_time_end_soll = (double) (Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate()) - this.start_simulation_agentpto) / (1000*60);							    		
			    		double hours_sim_time_end_soll_rounded = GeneralFunctions.round(hours_sim_time_end_soll,4);		
			    		
			    		rs.updateDouble(columnNameStartSimulation, hours_sim_time_start_soll_rounded);	
			    		rs.updateDouble(columnNameEndSimulation, hours_sim_time_end_soll_rounded);		
				        
				        rs.updateString(columnNameAuftrags_ID, allWorkingStep.getHasOperation().getAppliedOn().getID_String());
				        rs.updateString(columnNameOperation_Type, allWorkingStep.getHasOperation().getType());
				      
				        rs.insertRow();   //Einfügen der Zeile in die Datenbank

				        // Close ResultSet and Statement
				        rs.close();
				        //stmt.close();		        
				    
				    	}
			    	}
			    	


			    }	
		        
		    } catch (SQLException e ) {
		        e.printStackTrace();
		    }	
		//}
		
		       
	}
	
	private String replaceName(String name_operation, int id, String res_name) {
		if(res_name.contains("Transport")) {
			String [] split = name_operation.split("_", 2); //2 parts
			String res_start_transport = "";
			Location loc = new Location();
			loc.setCoordX(Float.parseFloat(split[0].split(";")[0]));
			loc.setCoordY(Float.parseFloat(split[0].split(";")[1]));
			
			for (Map.Entry<Integer, Resource_Extension> entry : resource_hashmap.entrySet()) {
			    if(_Agent_Template.doLocationsMatch(loc, entry.getValue().getHasLocation())){
			    	res_start_transport =  entry.getValue().getName();
			    	return res_start_transport+"-"+split[1];
			    }
			}			
			
			return "no match";
		}else {
			return name_operation;
		}
		
	}

	private void addToFlexsimLayoutTable(Statement stmt, int order_id, WorkPlan workplan, String tableNameProductionPlan) throws SQLException {
		String query = "Select * from "+tableNameProductionPlan+" where "+this.columnNameID+" = "+order_id;
		ResultSet rs2 = stmt.executeQuery("select * from "+tableNameProductionPlan);
		rs2.last();
		int row_count = rs2.getRow();
		
		ResultSet rs = stmt.executeQuery(query);

		System.out.println("DEBUG______________________________total query_"+row_count);
		if(rs.isBeforeFirst()) {	//data returned
			rs.next();
			System.out.println("DEBUG_____2nd query row count "+rs.getRow());
			createRowEntries(rs, workplan, order_id, 0);	//add 0 to row number
			rs.updateRow();   //Einfügen der Zeile in die Datenbank
		}else {
			rs.moveToInsertRow();					
			createRowEntries(rs, workplan, order_id, row_count+1);	//add 1 to row number	
			rs.updateInt(1, row_count+1);
			rs.insertRow();
		}
		
	}

	private void createRowEntries(ResultSet rs2, WorkPlan workplan, int order_id, int row_count) throws SQLException {
    	int i2 = 1;
    
		@SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> ite = workplan.getConsistsOfAllocatedWorkingSteps().iterator();
	    while(ite.hasNext()) {
	    	AllocatedWorkingStep allWorkingStep = ite.next();
	    	if(!allWorkingStep.getIsFinished() && !allWorkingStep.getIsErrorStep() && allWorkingStep.getHasOperation().getType().equals("production")) {
	    		String c_StartSoll = this.columnNameStartSoll + i2;
				String c_EndeSoll = this.columnNameEndeSoll + i2;
			//falsch	String c_Bezeichnung = allWorkingStep.getHasResource().getName();
	    		double hours_sim_time_start_soll = (double) (Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()) - this.start_simulation) / (1000*60*60);
	    		double hours_sim_time_start_soll_rounded = GeneralFunctions.round(hours_sim_time_start_soll,4);
	    		System.out.println("DEBUG_____DATABASECONNECTOR______ start date ms "+allWorkingStep.getHasTimeslot().getStartDate() + " minus start sim "+this.start_simulation+" div by 1000*60*60 = "+hours_sim_time_start_soll+" rounded 	"+hours_sim_time_start_soll_rounded);
	    		double hours_sim_time_end_soll = (double) (Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate()) - this.start_simulation) / (1000*60*60);							    		
	    		double hours_sim_time_end_soll_rounded = GeneralFunctions.round(hours_sim_time_end_soll,4);
	    		//int res_id = rs2.getInt(c_Bezeichnung);
	    		int number_of_column_for_production_step = 2+7*(i2-1);
	    		System.out.println("DEBUG__c_StartSoll = "+c_StartSoll+"__ and from AS = "+allWorkingStep.getHasResource().getID_Number()+" number_of_column_for_production_step "+number_of_column_for_production_step);
	    		rs2.updateDouble(c_StartSoll, hours_sim_time_start_soll_rounded);	
	    		rs2.updateDouble(c_EndeSoll, hours_sim_time_end_soll_rounded);		
	    		rs2.updateInt(number_of_column_for_production_step, allWorkingStep.getHasResource().getID_Number());
	    		//01.02.2019	variable Odernummer
	    		//System.out.println("DEBUG_____________________________________________________________order_id  "+order_id);
	    		//int row = rs2.getRow() + row_count;
	    		//rs2.updateInt(1, row);
	    		rs2.updateInt(72, order_id); //72 = letzte Spalte
	            i2++;
	    	}else if (allWorkingStep.getIsErrorStep()){
	    		double hours_sim_time_start_soll = (double) (Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()) - this.start_simulation) / (1000*60*60);
	    		double hours_sim_time_start_soll_rounded = GeneralFunctions.round(hours_sim_time_start_soll,4);
	    		System.out.println("DEBUG_____DATABASECONNECTOR______ start date ms "+allWorkingStep.getHasTimeslot().getStartDate() + " minus start sim "+this.start_simulation+" div by 1000*60*60 = "+hours_sim_time_start_soll);
	    		double hours_sim_time_end_soll = (double) (Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate()) - this.start_simulation) / (1000*60*60);							    		
	    		double hours_sim_time_end_soll_rounded = GeneralFunctions.round(hours_sim_time_end_soll,4);
	    		//int res_id = rs2.getInt(c_Bezeichnung);
	    		int number_of_column_for_production_step = 2+7*(10-1);	//=65
	    		
	    		String c_StartSoll = this.columnNameStartSoll + "10";
				String c_EndeSoll = this.columnNameEndeSoll + "10";
	    		rs2.updateDouble(c_StartSoll, hours_sim_time_start_soll_rounded);	
	    		rs2.updateDouble(c_EndeSoll, hours_sim_time_end_soll_rounded);		
	    		rs2.updateInt(number_of_column_for_production_step, allWorkingStep.getHasResource().getID_Number());
	    		//i2++;
	    	}else {
	    		System.out.println("Test "+allWorkingStep.getHasOperation().getName());
	    		if(ite.hasNext() == false) {	//03.12.2018 the last element should be the transport to warehouse --> I want to add that too
		    		String c_StartSoll = this.columnNameStartSoll + i2;
					String c_EndeSoll = this.columnNameEndeSoll + i2;
				//falsch	String c_Bezeichnung = allWorkingStep.getHasResource().getName();
		    		double hours_sim_time_start_soll = (double) (Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()) - this.start_simulation) / (1000*60*60);
		    		double hours_sim_time_start_soll_rounded = GeneralFunctions.round(hours_sim_time_start_soll,4);
		    		System.out.println("DEBUG_____DATABASECONNECTOR___LASTTRANSPORT___ start date ms "+allWorkingStep.getHasTimeslot().getStartDate() + " minus start sim "+this.start_simulation+" div by 1000*60*60 = "+hours_sim_time_start_soll);
		    		double hours_sim_time_end_soll = (double) (Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate()) - this.start_simulation) / (1000*60*60);							    		
		    		double hours_sim_time_end_soll_rounded = GeneralFunctions.round(hours_sim_time_end_soll,4);
		    		//int res_id = rs2.getInt(c_Bezeichnung);
		    		int number_of_column_for_production_step = 2+7*(i2-1);
		    		System.out.println("DEBUG__c_StartSoll = "+c_StartSoll+"__ and from AS = "+allWorkingStep.getHasResource().getID_Number()+" number_of_column_for_production_step "+number_of_column_for_production_step);
		    		rs2.updateDouble(c_StartSoll, hours_sim_time_start_soll_rounded);	
		    		rs2.updateDouble(c_EndeSoll, hours_sim_time_end_soll_rounded);		
		    		int resource_id_of_exit = getResourceID(allWorkingStep.getHasOperation().getName().split("_")[1]); //name of exit as input
		    		rs2.updateInt(number_of_column_for_production_step, resource_id_of_exit);
		    		
		    	}
	    		//i2++;
	    	}
	    }
		
	}

	private int getResourceID(String name_from_operation_target) {
		
		int id = 0;
		for(Map.Entry<Integer, Resource_Extension> entry : resource_hashmap.entrySet()) {
		    //int key = entry.getKey();
		    Resource_Extension value = entry.getValue();

		    if(value.getName().equals(name_from_operation_target)) {
		    	id = value.getID_Number();
		    }
		    // do what you have to do here
		    // In your case, another loop.
		}
		
		return id;
	}

	public void registerAtDF() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("database_entry");
		sd.setName("database_entry");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
	}
	
	private void createResourceHashMap() {
		try {
			if(this.getConnection().isClosed()) {
				activateConnection();
			}
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try (Connection con = this.getConnection(); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				){
	        //Statement stmt = this.getConnection().createStatement();
	        ResultSet rs = null;
	      
			String query_resource = "select * from "+tableNameResource;


	        
		rs = stmt.executeQuery(query_resource); 
		
		while(rs.next()) {
			Resource_Extension res = new Resource_Extension();
			res.setID_Number(rs.getInt(columnNameID));
			res.setName(rs.getString(columnNameResourceName_simulation));
			Location loc = new Location();
			loc.setCoordX(rs.getInt(_Agent_Template.columnNameLocationX));
			loc.setCoordY(rs.getInt(_Agent_Template.columnNameLocationY));
			res.setHasLocation(loc);
			if(_Agent_Template.simulation_enercon_mode) {
				res.setColumninproductionplan(rs.getInt(columnNameColumnInProductionPlan));
			}
			
			resource_hashmap.put(res.getID_Number(), res);
		}
			
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	// Verbindung zur DB mit ucanaccess	

	}
	/*
	private int determine_number_of_planned_production_steps(WorkPlan workplan) {
		int counter = 0;
		
	    @SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> it = workplan.getConsistsOfAllocatedWorkingSteps().iterator();
	    while(it.hasNext()) {
	    	AllocatedWorkingStep allWorkingStep = it.next();
	    	if(!allWorkingStep.getIsFinished() && allWorkingStep.getHasOperation().getType().equals("production") && allWorkingStep.getIsErrorStep() == false) {		//only count production steps
	    		counter++;
	    	}
	    }

		return counter;
	}*/


}
