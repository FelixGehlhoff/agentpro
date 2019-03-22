package agentPro_Prototype_Agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.jfree.ui.RefineryUtilities;

import com.sun.media.sound.SimpleSoundbank;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Delay;
import agentPro.onto.Machine_Error;
import agentPro.onto.Operation;
import agentPro.onto.Resource;
import agentPro.onto.Setup_state;
import agentPro.onto.State;
import agentPro.onto.Timeslot;
import agentPro.onto.WorkPlan;
import agentPro.onto.Workpiece;
import support_classes.Geometry;
import support_classes.LineSegment;
import support_classes.Point;
import support_classes.Resource_Extension;
import support_classes.XYTaskDataset_Total;

public class CreateGanttMySQL {
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
	
	public final static String dbaddress = "jdbc:ucanaccess://C:/Users/Mitarbeiter/Dropbox (HSU_Agent.Pro)/_AgentPro/Prototyp/Database.accdb";	//Address od database
	private static String dbaddress_sim = "jdbc:mysql://localhost:3306/MySQL?"+"user=root&password=SQL_0518&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";	//Address od database;
	private static Connection connection;			//Connection to database
	private static ArrayList<String>operation_names = new ArrayList();
	private static ArrayList<String>resources = new ArrayList();
	private static HashMap<Integer, Resource_Extension> resource_hashmap = new HashMap();
	public static String columnNameOfChangeover = "prod_changeover";
	public static String tableNameResourceSetupMatrix = "agentpro.resources_setupmatrix";
	
private static HashMap<String, Double> setup_matrix = new HashMap();
	
	public static HashMap<String, Double> getSetup_matrix() {
		return setup_matrix;
	}

	public static void setSetup_matrix( HashMap<String, Double> hashmap) {
		setup_matrix = hashmap;
	}
	
	public static void main(String[] args) {
		/*
		Delay delay = new Delay();
		Delay delay2 = new Delay();
	
		Machine_Error  er = new Machine_Error();
		System.out.println(delay.getClass().getSimpleName()+" "+delay2.getClass().getSimpleName()+" "+er.getClass().getSimpleName()	);
		int eins = 1;
		int eine_null = 0;
		Boolean b = (eins != 0);
				Boolean c = (eine_null != 0);
		System.out.println("wert b "+b+" wert c "+c);
		*/
		/*
		operation_names.add("Wickeln_1");
		operation_names.add("Entfernen_Wickelspiess");
		operation_names.add("Besaeumen_1.1");
		operation_names.add("Einbau_E115");
		operation_names.add("Fraesen_E115.1");
		operation_names.add("Fraesen_E115.2");
		operation_names.add("Besaeumen_1.2");
		operation_names.add("Konfektion/ExitE115");
		
		resources.add("Wickelfertigung");
		resources.add("Entfernen_Wickelspiess");
		resources.add("kl_Bk");
		resources.add("FAD");
		resources.add("Skoda_1_1");
		resources.add("Skoda_1_2");
		resources.add("gr_Bk_Ost");
		resources.add("Exit_E115");

		createResourceHashMap();
		
		
		WorkPlan wp = new WorkPlan();
		receiveValuesFromDB(wp);
		WorkPlan sorted_workplan = sortWorkplanChronologically(wp);
		//create GANTT Chart
		
		 XYTaskDataset_Total demo = new XYTaskDataset_Total(
	                "JFreeChart : XYTaskDataset_Total.java", sorted_workplan);
	        demo.pack();
	        RefineryUtilities.centerFrameOnScreen(demo);
	        demo.setVisible(false);	
		*/
		createSetupMatrix();
		
		Setup_state start_next_task = new Setup_state ();
		start_next_task.setID_String("A");
		Setup_state end_new = new Setup_state ();
		end_new.setID_String("B");
		
		float diff = calculateTimeBetweenStates(start_next_task, end_new , 1);
		System.out.println("diff "+diff);
/*
 * 
		int size = 2;
		Boolean [ ] shared_resource_asked = {false,true};
		Boolean [ ] shared_resource_available = {false,false};
		Boolean sharedResourcesStillToBeConsidered = true;
		
		
		
		for(int j = 0;j<size;j++) {
			System.out.println("DEBUG___myAgent.getNeeded_shared_resources().size() "+" ____shared_resource_asked[j]_____j = _"+j+" "+shared_resource_asked[j]);
			if(shared_resource_asked[j] == false) {		
				
			} else if(j == size-1) {
				System.out.println("stay");
				
				for(int k = 0;k<size;k++) {
					System.out.println("DEBUG_______shared_resource_asked[j]______j = _"+j+" "+shared_resource_asked[j]+" shared_resource_available[k] "+shared_resource_available[k]+" k = "+k);
					if(shared_resource_available[k] != null && shared_resource_available[k] == false) {
						
						System.out.println(" not all shared Resources available --> make no offer");
					
						sharedResourcesStillToBeConsidered = false;
						break;
					}else if(j == size-1) {
						
						sharedResourcesStillToBeConsidered = false;
						System.out.println(" ALL shared Resources available");
						
						break;
					}
					
				}
				
			}
			}
		
		*/
		
		
		/*
		
		Point a1 = new Point(0, 0);
		Point a2 = new Point(1, 0);
		Point a3 = new Point(2, 2);
		Point b1 = new Point(0, 2);
		Point b2 = new Point(1, 1);
		Point b3 = new Point(2, 0);
		
		Point [] a = new Point [2];
		a[0] = a1;
		a[1] = a3;
		//a[2] = a3;
		
		Point [] b = new Point [2];
		b[0] = b1;
		b[1] = b3;
		//b[2] = b3;
		
		Boolean boxesIntersect = Geometry.doBoundingBoxesIntersect(a,b);
		LineSegment line_segment_a1 = new LineSegment(a1,a2,"a1,2");
		LineSegment line_segment_a2 = new LineSegment(a2,a3,"a2,3");
		
		LineSegment line_segment_b1 = new LineSegment(b1,b2,"b1,2");
		LineSegment line_segment_b2 = new LineSegment(b2,b3,"b2,3");
		LineSegment[] allLineSegments = new LineSegment [4];
		
		allLineSegments[0] = line_segment_a1;
		allLineSegments[1] = line_segment_a2;
		allLineSegments[2] = line_segment_b1;
		allLineSegments[3] = line_segment_b2;
		
		Boolean linesIntersect = Geometry.doLinesIntersect(line_segment_a2,line_segment_b2);
		System.out.println("Boxes intersect = "+boxesIntersect+" lines intersect = "+linesIntersect);
		

		//Set<LineSegment[]> allIntersectingLineSegments = Geometry.getAllIntersectingLines(allLineSegments);
		Set<LineSegment[]> allIntersectingLineSegments = Geometry.getAllIntersectingLinesByBruteForce(allLineSegments);
		
		@SuppressWarnings("unchecked")
		Iterator<LineSegment[]> ite = allIntersectingLineSegments.iterator();
	    String printout = "crossing lines ";
	    while(ite.hasNext()) {		//checks for every allWS in Workplan
	    	LineSegment[] ls = ite.next();
	    	printout = printout + "new line:   ";
	    	for(LineSegment linesegment : ls) {
	    		printout = printout + " ; " +linesegment.toString();
	    	}
	    	printout = printout + "  next  ";
	    }
	    System.out.println(printout);
		*/
	}
	
	public static float calculateTimeBetweenStates(State start_next_task_generic, State end_new_generic, int counter_free_interval_i) {
		Setup_state start_next_task = (Setup_state) start_next_task_generic;
		Setup_state end_new = (Setup_state) end_new_generic;
		
		String combination = end_new.getID_String()+"_"+start_next_task.getID_String();
		float duration_of_reaching_next_start_state_new = 0;
		if(start_next_task.getID_String().equals(end_new.getID_String())) {
			return 0;
		}else {
			double d = getSetup_matrix().get(combination);
			duration_of_reaching_next_start_state_new = (float) d;
		}
		float duration_of_reaching_next_start_state_current = 10F; // in min		
		float difference = duration_of_reaching_next_start_state_new - duration_of_reaching_next_start_state_current;
		//System.out.println("DEBUG___"+logLinePrefix+" time_increment_or_decrement_to_be_added = "+difference+" __START NEXT TASK___location found: "+start_next_task.getCoordX()+";"+start_next_task.getCoordY()+" location end new "+end_new.getCoordX()+";"+end_new.getCoordY()+"  distance   = "+distance_TransportResource_fromResourceAtDestination_toStart_next_Job+" duration_of_reaching_next_target_new "+duration_of_reaching_next_target_new+" duration_of_reaching_next_target_current "+duration_of_reaching_next_target_current);
		
		return difference;
	}
	
	private static void createSetupMatrix() {
	    String query2 = "";

    	query2 = "select "+columnNameOfChangeover+" , `Zuschnitt` from "+tableNameResourceSetupMatrix; 
   
    try {
    	Connection con = DriverManager.getConnection(dbaddress_sim);
    	Statement stmt = con.createStatement();
        ResultSet rs2 = stmt.executeQuery(query2);
        HashMap<String, Double> matrix = new  HashMap<String, Double>();
        while (rs2.next()) {
        	matrix.put(rs2.getString(columnNameOfChangeover), rs2.getDouble("Zuschnitt"));
        }
        setSetup_matrix(matrix);
       
        
    } catch (SQLException e ) {
    	e.printStackTrace();
    }
    
    System.out.println(setup_matrix.get("A_B")+" "+setup_matrix.size());
		
	}

	private static void createResourceHashMap() {
		try {
			Connection con = DriverManager.getConnection(dbaddress_sim);
			connection = con;	
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	// Verbindung zur DB mit ucanaccess	
		
		try {
	        Statement stmt = connection.createStatement();
	        ResultSet rs = null;
		String query_resource = "select "+columnNameName+" , "+columNameColumnNameInProductionPlan+" , "+columnNameColumnInProductionPlan+" , "+columnNameResourceType+" , "+columnNameID+" , "+columnNameLocationX+" , "+columnNameLocationY+" from "+tableNameResource;
		
		rs = stmt.executeQuery(query_resource); 
		
		while(rs.next()) {
			Resource_Extension res = new Resource_Extension();
			res.setID_Number(rs.getInt(columnNameID));
			res.setName(rs.getString(columnNameName));
			res.setColumninproductionplan(rs.getInt(columnNameColumnInProductionPlan));
			resource_hashmap.put(res.getID_Number(), res);
		}
		
		}catch (SQLException e ) {
	    	e.printStackTrace();
	    } 
		
		
	}

	public static void receiveValuesFromDB(WorkPlan workplan) {
		//Datenbank
	
		Statement stmt = null;

		//connection = con;	
		
		
			if(_Agent_Template.simulation_mode) {
				
				try {
					Connection con = DriverManager.getConnection(dbaddress_sim);
					connection = con;	
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	// Verbindung zur DB mit ucanaccess	
				 
				try {
				        stmt = connection.createStatement();
				        ResultSet rs = null;
			/*	        
			String query1 = "Select ";
						
		    int number_of_production_steps_without_buffer = 8;
			for(int i = 1;i <= number_of_production_steps_without_buffer ;i++) {	
				String c_StartIst = "`"+columnNameStartSoll + i+"`";
				String c_Gestartet = "`"+columnNameEndeSoll + i+"`";
				
				//String c_EndeIst = "`"+columNameEndeIst + i+"`";
				//String c_Beendet = "`"+columNameBeendet + i+"`";
				if(i==1) {
					//query1 = query1 +c_StartIst+", "+c_Gestartet+", "+c_EndeIst+", "+c_Beendet;	
					query1 = query1 +c_StartIst+", "+c_Gestartet;	
				}else {
					//query1 = query1 + ", "+c_StartIst+", "+c_Gestartet+", "+c_EndeIst+", "+c_Beendet;
					query1 = query1 + ", "+c_StartIst+", "+c_Gestartet;
				}
				
			}
			query1 = query1 + " from "+nameOfProductionPlan+" where `"+columnNameID+"` < "+9;
			*/
			String query1 = "Select * "+ " from "+nameOfProductionPlan+" where `"+columnNameID+"` < "+5;
			System.out.println(query1);
			rs = stmt.executeQuery(query1); 	//result set should contain StartIst1 = 123 .... StartIst7 = 789 <-- error resource
			//int index = 0;
			
			while (rs.next()) {
				for(int index = 0;index<=8;index++) {
					AllocatedWorkingStep allocWS = new AllocatedWorkingStep();
	        		//Operation op = new Operation();      		       		        		
					//op.setName(operation_names.get(index));
					

	        	//allocWS.setHasOperation(op);
	        		Resource res = new Resource();
	        		//res.setID_Number(rs.getInt(columnNameOfResource_ID));
	        		if(rs.getInt(2+7*(index)) != 0) {
	        			res.setName((resource_hashmap.get(rs.getInt(2+7*(index)))).getName());
	    	        	allocWS.setHasResource(res);
	    	        		Timeslot ts = new Timeslot();
	    	        		
	    	        		long long_value_start = convertOccuranceTime((float) rs.getDouble(columnNameStartSoll+(index+1)));
	    	        		long long_value_end = convertOccuranceTime((float) rs.getDouble(columnNameEndeSoll+(index+1)));
	    	        			
	    	        		ts.setStartDate(String.valueOf(long_value_start));
	    	        		ts.setEndDate(String.valueOf(long_value_end));
	    	        	allocWS.setHasTimeslot(ts);
	    	        	//System.out.println("DEBUG__________start__"+ts.getStartDate()+" end "+ts.getEndDate());
	    	        	workplan.addConsistsOfAllocatedWorkingSteps(allocWS);
	        		}
	        		
				}
			
					
		        }
				} catch (SQLException e ) {
			    	e.printStackTrace();
			    } 	
			}
			
			else {
				try {
					Connection con = DriverManager.getConnection(dbaddress_sim);
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
