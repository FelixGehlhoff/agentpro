package testing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;

import org.jfree.ui.RefineryUtilities;

import com.sun.media.sound.SimpleSoundbank;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Delay;
import agentPro.onto.Machine_Error;
import agentPro.onto.Operation;
import agentPro.onto.Resource;
import agentPro.onto.Timeslot;
import agentPro.onto.WorkPlan;
import agentPro.onto.Workpiece;
import agentPro_Prototype_Agents._Agent_Template;
import support_classes.Geometry;
import support_classes.LineSegment;
import support_classes.Point;
import support_classes.Resource_Extension;
import support_classes.XYTaskDataset_Total;

public class Testing_mySQL_Dates {
	public static long start_simulation = 1533074400000L; //01.08.2018 00:00
	//database
	public static String nameOfMES_Data_Resource_Veiw = "Test_new";
	public static String columnNameOfOperation = "Operation";
	public static String columnNameOfResource = "Ressource";
	public static String columnNameOfResource_ID = "Ressource_ID";
	public static String columnNameOfPlanStart = "PlanStart";
	public static String columnNameOfPlanEnd = "PlanEnde";
	public static String columnNameAuftrags_ID = "Auftrags_ID";
	//public static String columnNameOperation_Type = "Operation_Type";
	public static String columnNameOfStarted = "Started";
	public static String nameOfMES_Data_Resource = "Test_new";
	public static String columnNameOfOperation_Type = "Operation_Type";
	private static String nameOfProduction_Plan_Def_Table = "Production_Plan_Def";

	private static String columnNameOfStep = "Step";
	private static String columnNameOfProductName = "ProductName";
	private static String columnNameOfFirstOperation = "FirstOperation";
	private static String columnNameOfLastOperation = "LastOperation";
	public static String columnNameName = "Bezeichnung";
	public static String columnNameErrorType = "Error_Type";
	public String columnNameError_Occur_Time = "Error_Occur_Time";
	public static String columnNameID = "ID";
	public String columnNameRunning = "Status";//"On(1)/Off(0)";
	public static String columnNameResourceType = "resource_type";
	public String columnNameResourceDetailedType = "resource_detailed_type";
	public static String columNameStartIst = "StartIst";		//number is missing, e.g. StartIst1
	public static String columNameGestartet = "Gestartet?";
	public static String columNameEndeIst = "EndeIst";
	public static String columNameBeendet = "Beendet?";
	public static String columnNameStartSoll = "StartSoll";
	public static String columnNameEndeSoll = "EndeSoll";
	public static String nameOfProductionPlan ="flexsimdata.productionplan";
	public String nameOfResources ="flexsimdata.resources";
	public static String tableNameResource = "flexsimdata.resources";
	public String tableNameProductionPlan = "flexsimdata.productionplan";
	public static String columnNameColumnInProductionPlan = "columninproductionplan";
	public static String columnNameLocationX = "locationX";
	public static String columnNameLocationY = "locationY";
	public static String columNameColumnNameInProductionPlan = "columnnameinproductionplan";
	public static String columnNameOfDueDate = "dueDate";
	public static String columnNameOfReleaseDate = "releaseDate";
	private static String DateFormat = "yyyy-MM-dd HH:mm:ss";
	public static SimpleDateFormat SimpleDateFormat = new SimpleDateFormat(DateFormat);
	
	public final static String dbaddress = "jdbc:ucanaccess://C:/Users/Mitarbeiter/Dropbox (HSU_Agent.Pro)/_AgentPro/Prototyp/Database.accdb";	//Address od database
	private static String dbaddress_sim = "jdbc:mysql://localhost:3306/MySQL?"+"user=root&password=SQL_0518&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";	//Address od database;
	private static Connection connection;			//Connection to database
	//private static Boolean simulation_mode = true;
	private static ArrayList<String>operation_names = new ArrayList();
	private static ArrayList<String>resources = new ArrayList();
	private static HashMap<Integer, Resource_Extension> resource_hashmap = new HashMap();

	public static void main(String[] args) {
		Float test = -0.4333F;
		long test2 = test.longValue();
		System.out.println(test2);
		
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Germany/Hamburg"));
        calendar.setTimeInMillis(1551400337821L);
        final Date result = calendar.getTime();
        System.out.println(result.getTime()+" "+SimpleDateFormat.format(result.getTime()));
		
		try {
			Connection con = DriverManager.getConnection(dbaddress_sim);
			connection = con;	
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	// Verbindung zur DB mit ucanaccess	
		 
		try {
		        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		      
		        ResultSet rs = null;
		        ResultSet rs2 = null;
	
		        String query = "select * from agentpro.test"; 

	rs = stmt.executeQuery(query); 	//result set should contain StartIst1 = 123 .... StartIst7 = 789 <-- error resource
	//int index = 0;
	if(rs.isBeforeFirst()) {
		rs.next();
		
	}
		rs.moveToInsertRow();
		
		java.sql.Timestamp sql_timestamp = new java.sql.Timestamp(result.getTime());
		
			rs.updateTimestamp(1,  sql_timestamp);
			  rs.insertRow();   //Einfügen der Zeile in die Datenbank
	            rs.close();
	
		
				
			
		}catch (SQLException e ) {
	    	e.printStackTrace();
	    } 
		/*
		
		
		
		
		if(_Agent_Template.simulation_mode) {
			
			try {
				Connection con = DriverManager.getConnection(dbaddress_sim);
				connection = con;	
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	// Verbindung zur DB mit ucanaccess	
			 
			try {
			        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			      
			        ResultSet rs = null;
			        ResultSet rs2 = null;
		
			        String query = "select * from flexsimdata.orderbook"; 
	
		rs = stmt.executeQuery(query); 	//result set should contain StartIst1 = 123 .... StartIst7 = 789 <-- error resource
		//int index = 0;
		
		while (rs.next()) {
			java.text.SimpleDateFormat sdf = 
	        	     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			//USE TIMESTAMP?
			long current_time = System.currentTimeMillis();
			java.sql.Timestamp timestamp = new java.sql.Timestamp(current_time);
			rs.updateString(columnNameOfPlanEnd, sdf.format(timestamp));
			rs.updateString(columnNameOfReleaseDate, sdf.format(timestamp));
			rs.updateString(columnNameOfDueDate, sdf.format(timestamp));
			
			java.util.Date dt = new java.util.Date();

        	

        	String currentTime = sdf.format(dt);
    		
        	//java.sql.Date sql_date = new java.sql.Date(time_in_ms);
        	System.out.println("DEBUG________________________"+currentTime);
        		rs.updateString(columnNameOfPlanStart, currentTime);	
        		//java.sql.Date sql_date2 = rs.getDate(columnNameOfPlanStart);
        		//System.out.println("DEBUG________________________"+sql_date2);
		
			rs.updateRow();	
	        }
		
		rs2 = stmt.executeQuery(query); 	//result set should contain StartIst1 = 123 .... StartIst7 = 789 <-- error resource
		while (rs2.next()) {
			java.text.SimpleDateFormat sdf =  new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			java.util.Date date = rs2.getDate(columnNameOfPlanStart);
			java.sql.Date date_sql = rs2.getDate(columnNameOfPlanStart);
			java.sql.Timestamp timestamp = rs2.getTimestamp(columnNameOfPlanEnd);
			String date_string = sdf.format(rs2.getTimestamp(columnNameOfPlanEnd));
			System.out.println("DEBUG_"+sdf.format(date)+" "+sdf.format(date.getTime())+" "+date.getTime()+" "+date_sql.getTime()+ " "+sdf.format(date_sql)+" "+timestamp+" "+sdf.format(timestamp)+" "+date_string);
		}
			
		
			} catch (SQLException e ) {
		    	e.printStackTrace();
		    } 
			
			
			
			
		}
		*/
		
	}
	

	public static void receiveValuesFromDB(WorkPlan workplan) {
		//Datenbank
	
		Statement stmt = null;

		//connection = con;	
		
		
			if(_Agent_Template.simulation_enercon_mode) {
				
				try {
					Connection con = DriverManager.getConnection(dbaddress_sim);
					connection = con;	
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	// Verbindung zur DB mit ucanaccess	
				 
				try {
				        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				        ResultSet rs = null;
			
				        String query = "select * from flexsimdata.orderbook"; 
		
			rs = stmt.executeQuery(query); 	//result set should contain StartIst1 = 123 .... StartIst7 = 789 <-- error resource
			//int index = 0;
			
			while (rs.next()) {
				
				//USE TIMESTAMP?
				
				
				java.util.Date dt = new java.util.Date();

	        	java.text.SimpleDateFormat sdf = 
	        	     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	        	String currentTime = sdf.format(dt);
	    		
	        	//java.sql.Date sql_date = new java.sql.Date(time_in_ms);
	        	System.out.println("DEBUG________________________"+currentTime);
	        		rs.updateString(columnNameOfPlanStart, currentTime);	
	        		//java.sql.Date sql_date2 = rs.getDate(columnNameOfPlanStart);
	        		//System.out.println("DEBUG________________________"+sql_date2);
			
				rs.updateRow();	
		        }
				} catch (SQLException e ) {
			    	e.printStackTrace();
			    } 	
			}
			
			else {
				try {
					Connection con = DriverManager.getConnection(dbaddress);
					connection = con;
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	// Verbindung zur DB mit ucanaccess	
				String query1 =
				        "select "+columnNameOfOperation+" , "+columnNameOfResource+" , "+columnNameOfResource_ID+" , "+columnNameOfPlanStart+" , "+columnNameOfPlanEnd+" from "+nameOfMES_Data_Resource_Veiw;
				    
				    try {
				        stmt = connection.createStatement();
				        ResultSet rs = stmt.executeQuery(query1);
			       		
				        while (rs.next()) {
				        	AllocatedWorkingStep allocWS = new AllocatedWorkingStep();
				        		Operation op = new Operation();      		
				        		
				        		op.setName(rs.getString(columnNameOfOperation));

				        	allocWS.setHasOperation(op);
				        		Resource res = new Resource();
				        		res.setID_Number(rs.getInt(columnNameOfResource_ID));
				        		res.setName(rs.getString(columnNameOfResource));
				        	allocWS.setHasResource(res);
				        		Timeslot ts = new Timeslot();
				        			Date startdate = rs.getDate(columnNameOfPlanStart);
				        			Time start_time	= rs.getTime(columnNameOfPlanStart);
				        			//TBD plus eine Stunde muss gerechnet werden --> warum?
				    
				        		    
				        			//Calendar cal = Calendar.getInstance(TimeZone.);	        			
				        			//Date date_new = rs.getDate(columnNameOfPlanStart, cal);
				        			//cal.setTimeInMillis(date_new.getTime());
				        			long long_value_start = startdate.getTime()+start_time.getTime()+1*60*60*1000;
				        			
				        		  	//System.out.println("DEBUG____test long "+long_value_start+"____startdate__"+startdate+" getTime "+startdate.getTime()+" time "+start_time.getTime());
				        			Date enddate = rs.getDate(columnNameOfPlanEnd);
				        			Time end_time	= rs.getTime(columnNameOfPlanEnd);
				        			long long_value_end = enddate.getTime()+end_time.getTime()+1*60*60*1000;
				        		ts.setStartDate(String.valueOf(long_value_start));
				        		ts.setEndDate(String.valueOf(long_value_end));
				        	allocWS.setHasTimeslot(ts);
				        	//System.out.println("DEBUG__________start__"+ts.getStartDate()+" end "+ts.getEndDate());
				        	workplan.addConsistsOfAllocatedWorkingSteps(allocWS);
						  		   
				        }
				        
				        
				    } catch (SQLException e ) {
				    	e.printStackTrace();
				    } 	
			}
					     
	        
	    
		
		 

			  
			    


		    /*
		    finally {
		        if (stmt != null) { try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} }
		    }*/
			
		}
	
	public static WorkPlan sortWorkplanChronologically(WorkPlan workplan) {
		/*
	    @SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> ite = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
	    String printout = myAgent.getLocalName()+" DEBUG__________SORTING_____________";
	    while(ite.hasNext()) {		//checks for every allWS in Workplan
	    	AllocatedWorkingStep a = ite.next();	  
	    	printout = printout + " NEXT " + a.getHasTimeslot().getStartDate()+" - "+a.getHasOperation().getName();

	    }
		System.out.println(printout);
		*/
			WorkPlan wP_toBeSorted = new WorkPlan();
			
		    @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = workplan.getConsistsOfAllocatedWorkingSteps().iterator();
		   
		    while(it.hasNext()) {		//checks for every allWS in Workplan
		    	AllocatedWorkingStep allocWorkingstep = it.next();	  
		    	long startdate_of_current_step = Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate());	//get the startdate of the step
		    	
		    	if(wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().size()>0) {	//not first element
			    	int position_to_be_added = 0;
			    	
			    	for(int i = 0;i<wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().size();i++) {	//check for every element already in the new list if the current one from WP has an earlier startdate
			    		//startdate of the element in the new list
			    		long startdate_step_in_to_be_sorted = Long.parseLong(((AllocatedWorkingStep) wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().get(i)).getHasTimeslot().getStartDate());
			    		//if the startdate of the element in the new list is smaller than the startdate of the current step, 
			    		//the current step has to be added afterwards    			
			    		if(startdate_step_in_to_be_sorted < startdate_of_current_step) {	
			    			position_to_be_added++; //so the position must be increased by one
			    		}else {
			    			//if not it can be added on that position and all behind are moved one position "to the right"
			    		}
			    	}
			    	wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().add(position_to_be_added, allocWorkingstep);
			    	
		    	}else {	//first element can just be added (list was still null)
		    		wP_toBeSorted.getConsistsOfAllocatedWorkingSteps().add(allocWorkingstep);
		    		}

		    }
		    //workplan = wP_toBeSorted;
		    return wP_toBeSorted;

	}
	
	public static long convertOccuranceTime(float occurance_time) {
		long converted_time = start_simulation + (long) (occurance_time*60*60*1000);
		return converted_time;
	}

}
