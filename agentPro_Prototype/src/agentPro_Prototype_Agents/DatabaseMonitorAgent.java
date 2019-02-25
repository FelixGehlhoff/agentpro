package agentPro_Prototype_Agents;

import DatabaseConnection.MonitorDBChangedBehaviour;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import agentPro.onto.AgentPro_ProductionOntology;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.WorkPlan;


/*
 * Provides the interface to the ERP system (here: the dummy agent). It receives orders from the ERP system. 
 * To fulfill the order an order agent is created.
 * If the order is finished the interface agent is contacted by the order agent. It then sends an order completion message
 * to the ERP system.
 */

public class DatabaseMonitorAgent extends _Agent_Template{
	private static final long serialVersionUID = 1L;
	
	protected MonitorDBChangedBehaviour MonitorDBChangedBehaviour;
	
	private String DateFormat = "yyyy-MM-dd HH:mm:ss";
	public SimpleDateFormat SimpleDateFormat = new SimpleDateFormat(DateFormat);
	public String logLinePrefix;
	//private String dbaddress = "jdbc:mysql://localhost:3306/MySQL?"+"user=root&password=SQL_0518&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";	//Address od database;
	public Ontology ontology = AgentPro_ProductionOntology.getInstance();
	public Codec codec = new SLCodec();
	
	//public String ability;
	
	//Datenbankverbindung
		//public final String dbaddress = "jdbc:ucanaccess://C:/Users/Mitarbeiter/Dropbox (HSU_Agent.Pro)/_AgentPro/Prototyp/Database.accdb";	//Address od database
		private Connection connection;			//Connection to database

	
	protected void setup (){
		super.setup();
		logLinePrefix = " DatabaseMonitorAgent ";
		// / INITIALISATION
		// /////////////////////////////////////////////////////////
		registerAtDF();
		getContentManager().registerOntology(ontology);
		getContentManager().registerLanguage(codec);
		//Datenbank
				Connection con;			
				
				try {
					Class.forName("com.mysql.cj.jdbc.Driver");
					
						con = DriverManager.getConnection(dbaddress_sim);	// Verbindung zur DB mit ucanaccess	
					
				
					this.setConnection(con);	     
			        
			    } catch (SQLException e ) {
			        e.printStackTrace();
			    }catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


		
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
        MonitorDBChangedBehaviour = new MonitorDBChangedBehaviour(this);
        addBehaviour(MonitorDBChangedBehaviour);
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
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
	/*
	public void addDataToDatabase(String agent_type, WorkPlan workplan) throws SQLException {
		
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
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try (Connection con = DriverManager.getConnection(dbaddress); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				){
				
			@SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = workplan.getConsistsOfAllocatedWorkingSteps().iterator();
		    while(it.hasNext()) {
		    	AllocatedWorkingStep allWorkingStep = it.next();
		    	
		    	if(!allWorkingStep.getIsFinished()) {		//if the step is not already finished --> update or put it into database
			    	//A Statement is an interface that represents a SQL statement. You execute Statement objects, 
					//and they generate ResultSet objects, which is a table of data representing a database result set. 
					//You need a Connection object to create a Statement object.
			    	/*
			    	Statement stmt = con.createStatement(
				            ResultSet.TYPE_SCROLL_SENSITIVE, // Vor- und Rücksprünge möglich
				            ResultSet.CONCUR_UPDATABLE);     // Veränderbar
			    	*/
	/*
			    	ResultSet rs = stmt.executeQuery(		
			 	            "SELECT * FROM ["+mes_table_to_be_used+"]"); //Selektiere alle Spalten (*) der Tabelle tblOrder in SQL Sprache
			    	ResultSet rs2 = stmt.executeQuery(		
			    			//"select * from "+nameOfMES_Data+" where "+columnNameOfOperation+" = '"+allWorkingStep.getHasOperation().getName()+"' and "+columnNameAuftrags_ID+" = '"+allWorkingStep.getHasOperation().getAppliedOn().getID_String()+"' and "+columnNameFinished+" = 'false'"); 
			    			"select * from "+mes_table_to_be_used+" where "+columnNameOfOperation+" = '"+allWorkingStep.getHasOperation().getName()+"' and "+columnNameAuftrags_ID+" = '"+allWorkingStep.getHasOperation().getAppliedOn().getID_String()+"'"); 
					       
			    	if (rs2.isBeforeFirst() ) {  //the SQL query has returned data  
			    		rs2.next(); 
			        	//rs.moveToInsertRow();		  //Jetzt steht das RS in der "Insert" Zeile, diese ist quasi "separat" zum Rest der Tabelle  
			            //RS bzw. jede Spalte wird mit Werten gefüllt	  
			    		Calendar cal = Calendar.getInstance();
			    		cal.setTimeInMillis(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()));
			    		
			    		java.sql.Date sql_date = new java.sql.Date(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()));
			    		rs2.updateDate(columnNameOfPlanStart, sql_date);				    		
			    		//rs2.updateInt(columnNameOfPlanStart_zahl, );
			    	    //java.sql.Date sqlDate = new java.sql.Date(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()));
			    			//System.out.println("DEBUG______agent_Template____________sqldate "+sqlDate+" sqlDate.getTime() "+sqlDate.getTime());
			 	        rs2.updateDate(columnNameOfPlanEnd, new java.sql.Date(Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate())));	
			 	       
			 	        if(getLocalName().equals("Pufferplatz1")) {
			 	        	System.out.println("DEBUG_________columnNameOfPlanStart__"+sql_date.getTime());
			 	        	System.out.println("DEBUG_________columnNameOfPlanEnd__"+new java.sql.Date(Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate())).getTime());			 	        	
			 	        }
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
			    	 
			    	rs.moveToInsertRow();		  //Jetzt steht das RS in der "Insert" Zeile, diese ist quasi "separat" zum Rest der Tabelle  
			        //RS bzw. jede Spalte wird mit Werten gefüllt
			        rs.updateString(columnNameOfResource, allWorkingStep.getHasResource().getName());	  
			        rs.updateString(columnNameOfOperation, allWorkingStep.getHasOperation().getName());
			        rs.updateInt(columnNameOfResource_ID, allWorkingStep.getHasResource().getID_Number());		        
			        rs.updateDate(columnNameOfPlanStart, new java.sql.Date(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate())));	        
			        rs.updateDate(columnNameOfPlanEnd, new java.sql.Date(Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate())));	
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
		       
	}
	*/
	public void registerAtDF() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("database_monitoring");
		sd.setName("database_monitoring");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
	}

}
