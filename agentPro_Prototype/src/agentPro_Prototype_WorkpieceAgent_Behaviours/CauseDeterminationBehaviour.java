package agentPro_Prototype_WorkpieceAgent_Behaviours;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import org.json.JSONObject;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Delay;
import agentPro.onto.Disturbance;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Resource;
import agentPro_Prototype_Agents.WorkpieceAgent;
import jade.core.behaviours.OneShotBehaviour;


public class CauseDeterminationBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Disturbance disturbance;
	private String data_from_file_s;
	private WorkpieceAgent myAgent;

	
	private static String nameOfMES_Data = "MES_Data";
	private static String columnNameOfOperation = "Operation";
	//private String columnNameOfResource = "Ressource";
	//private String columnNameOfResource_ID = "Ressource_ID";
	//private static String columnNameOfIstStart = "IstStart";
	//private static String columnNameOfIstEnde = "IstEnde";
	private static String columnNameAuftrags_ID = "Auftrags_ID";
	private static String columnNameOfStarted = "Started";
	private static String columnNameOfFinished = "Finished";
	private static String disturbance_type = "";
	
	public CauseDeterminationBehaviour(WorkpieceAgent myAgent, Disturbance disturbance, String data_from_file) {
		this.disturbance = disturbance;
		System.out.println("DEBUG__CauseDetermination  "+disturbance.getId_workpiece()+"   "+myAgent.getRepresented_Workpiece().getID_String());
		this.data_from_file_s = data_from_file;
		this.myAgent = myAgent;
		disturbance_type = disturbance.getHasDisturbanceType().getClass().getSimpleName();
	}

	@Override
	public void action() {
		
		//receive all started and finished values from the database		
		/*
		 @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> ite = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();		 	
		    while(ite.hasNext()) {
		    	AllocatedWorkingStep a = ite.next();
		    	receiveValuesFromDB(a);
		    }*/
		
		    try {
				receiveValuesFromDB();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(disturbance_type.equals("Delay")) {
				//determine the relevant allocated working step
				float localization_delta = disturbance.getLocalizationDelta();
				float time_delta = disturbance.getTimeDelta();
				float process_delta = disturbance.getProcessDelta();
				//"{ 	\"checkpoint_sequence_number\": 4 ,\"checkpoint_name\": \"transport#40.0;40.0_Lackierkabine1\"}";
				JSONObject data_from_file = new JSONObject(data_from_file_s);
				
				/*
				 * Die Lokalisierungskomponente erwartet den Beginn des Transports --> Der Fehler hier im Beispiel liegt 
				 * aber in einer Verzögerung des Arbeitsschritts.
				 * Andersherum wäre bei einer Verzögerung des Transports diese Verzögerung direkt erkennbar (da kein rechtzeitiges Ankommen an der 
				 * Produktionsressource)
				 */
				String data = data_from_file.getString("checkpoint_name");

				String[] parts = data.split("#");	
				String operation_type = parts[0];
				String operation_where_the_error_occured_from_LS = parts[1];
				System.out.println("DEBUG___________operation_where_the_error_occured_from_LS  "+ operation_where_the_error_occured_from_LS);
				
				String [] parts2 = operation_where_the_error_occured_from_LS.split("_");
				String [] parts3 = parts2[0].split(";");	
				System.out.println("DEBUG___________x coord = "+parts3[0]+" y coord = "+parts3[1]);
						
				//WORKAROUND FÜR DIE AKTUELLE POSITION
				Location location = new Location();
				location.setCoordX(Float.parseFloat(parts3[0]));
				location.setCoordY(Float.parseFloat(parts3[1]));
				myAgent.setLocation(location);
				
				
				int counter = 0;
			 	Integer position = null;
			 	
				AllocatedWorkingStep relevant_allWS = null;
				Operation op = null;
				
				//find correct allWS
				 @SuppressWarnings("unchecked")
					Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();		 	
				    while(it.hasNext()) {
				    	AllocatedWorkingStep allocWorkingstep = it.next();
				    	op = allocWorkingstep.getHasOperation();
				    	if(op.getName().equals(operation_where_the_error_occured_from_LS)) {	//find out the position of the relevant step			    	
				    		position = counter;
				    		break;
				    	}
				    	counter++;
				    }
				    if(position != null) {
					    switch(operation_type) {
					    case "transport":
					    	relevant_allWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(position-1); //not the transport step is relevant but the production step before that 
					    	break;
					    case "production":
					    	 relevant_allWS = (AllocatedWorkingStep) myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().get(position);	//not the transport step is relevant but the production step before that 
					    	break;
					    }
				    
					if(localization_delta != 0) {
						//do this
					}else if (time_delta != 0) {
						// 1. delay from next transport resource?
						//TBD --> workaround
						boolean delay_message_from_next_transport_resource = false;
						
						// 2. are there any MES messages to be read?
							// 2.1 check step started & finished messages
						
						//receive all started and finished values from the database
						/*
						 @SuppressWarnings("unchecked")
							Iterator<AllocatedWorkingStep> ite = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();		 	
						    while(ite.hasNext()) {
						    	AllocatedWorkingStep a = ite.next();
						    	receiveValuesFromDB(a);
						    }*/
										boolean step_started = relevant_allWS.getIsStarted();								
										boolean step_finished = relevant_allWS.getIsFinished();							
										
							// 2.2 check disturbance messages from workers
										//TBD --> workaround
										boolean disturbance_Message_from_Worker = false;
						
						//keine Delay Nachricht von Trans.Ress, step gestartet aber nicht fertig, keine Nachricht vom Worker
						if(!delay_message_from_next_transport_resource && step_started && !step_finished && !disturbance_Message_from_Worker) {
							//determine Buffer
							System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+myAgent.logLinePrefix+" cause: small delay --> start Buffer Determination");
							myAgent.addBehaviour(new BufferDeterminationBehaviour(myAgent, relevant_allWS, true, null)); //buffer place needed afterwards
						}
						
						
					}else if(process_delta != 0) {
						//do this
					}else {
						
						}
				 }// if position unequal to zero
				    //the step was not found in the allocated Working Steps
				 else {
				    	System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+myAgent.logLinePrefix+"no step with the name "+operation_where_the_error_occured_from_LS+" was found. Cause Determination without success.");
				 }
				    
				    
				    
			}else if(disturbance_type.equals("Machine_Error") || disturbance_type.equals("Workpiece_Error")) {
				
				Resource res_that_error_occures_at = disturbance.getOccuresAt();	
				AllocatedWorkingStep relevant_allWS = null;
				Resource res = null;
				//find correct allWS 
				 @SuppressWarnings("unchecked")
					Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();		 	
				    while(it.hasNext()) {
				    	AllocatedWorkingStep allocWorkingstep = it.next();			      	
				    	res = allocWorkingstep.getHasResource();
				    	if(res.getName().equals(res_that_error_occures_at.getName())) {	//find out the position of the relevant step			    	
				    		allocWorkingstep.setEnddate(disturbance.getError_occurance_time());
				    		relevant_allWS = allocWorkingstep;	    		
				    		if(myAgent.simulation_enercon_mode) {
				    			myAgent.setLocation(res_that_error_occures_at.getHasLocation());
				    		}			    		
				    		break;
				    	}
				    }
				    if(disturbance_type.equals("Machine_Error")){				//determine Buffer
				    	System.out.println(myAgent.SimpleDateFormat.format(new Date())+" " +myAgent.getLocalName()+myAgent.logLinePrefix+" cause: Machine Error --> start Buffer Determination");
						
				    	myAgent.addBehaviour(new BufferDeterminationBehaviour(myAgent, relevant_allWS, false, disturbance));  // true = buffer place needed if no buffer
				    }else if(disturbance_type.equals("Workpiece_Error")) {	//find buffer place
				    	//cancel unfinished working steps
				    	@SuppressWarnings("unchecked")
						Iterator<AllocatedWorkingStep> ite = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
					    while(ite.hasNext()) {
					    	AllocatedWorkingStep allWorkingStep = ite.next();
					    	if(!allWorkingStep.getIsFinished()) {	//sends cancellations to transport units --> TBD
					    	myAgent.cancelAllocatedWorkingSteps(allWorkingStep);
					    	ite.remove();
					    	}
					    	
					    }
					    
				    	myAgent.addBehaviour(new BookBufferPlaceProcedureBehaviour(myAgent, relevant_allWS));
				    }
				    
	
			}

			    

 
		
	}
	private void receiveValuesFromDB() throws SQLException {
		if(disturbance_type.equals("Delay")) {
			@SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> ite = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();		 	
		    while(ite.hasNext()) {
		    	AllocatedWorkingStep a = ite.next();
		    	receiveValuesFromDB(a);
		    }
		    
		   //try one query for all data 
		}else if(disturbance_type.equals("Machine_Error") || disturbance_type.equals("Workpiece_Error")){
			if(myAgent.getConnection().isClosed()) {
				myAgent.activateConnection();
			}
			try (Connection con = myAgent.getConnection(); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
					){
				ResultSet rs = null;
				
			//int number_of_error_resource = disturbance.getOccuresAt().getID_Number();
				int number_of_production_steps = myAgent.getProdPlan().getConsistsOfOrderedOperations().size();
			String query_production_plan = "select * from "+myAgent.tableNameProductionPlan+" where "+myAgent.columnNameorderid+" = "+disturbance.getId_workpiece();
			/*
			for(int i = 1;i<= number_of_production_steps ;i++) {
				String c_StartIst = myAgent.columNameStartIst + i;
				String c_Gestartet = myAgent.columNameGestartet + i;
				String c_EndeIst = myAgent.columNameEndeIst + i;
				String c_Beendet = myAgent.columNameBeendet + i;
				query_production_plan = query_production_plan + c_StartIst+" , "+c_Gestartet+" , "+c_EndeIst+" , "+c_Beendet;
			}
			query_production_plan = query_production_plan + " from "+myAgent.nameOfProductionPlan+" where "+myAgent.columnNameID+" = "+disturbance.getId_workpiece();
			*/
			rs = stmt.executeQuery(query_production_plan); 	//result set should contain StartIst1 = 123 .... StartIst7 = 789 <-- error resource
				while (rs.next()) {
					for(int i = 1;i<= number_of_production_steps ;i++) {	//find each corresponding resource in workplan
						int number_of_column_for_production_step = 2+7*(i-1);
						int resource_id = rs.getInt(number_of_column_for_production_step);
						System.out.println("DEBUG______number_of_production_steps = "+number_of_production_steps+" number_of_column_for_production_step "+number_of_column_for_production_step+" resource_id "+resource_id);
						
						@SuppressWarnings("unchecked")
						Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();		 	
					    while(it.hasNext()) {
					    	AllocatedWorkingStep a = it.next();	
					    	if(a.getHasResource().getID_Number() == resource_id) {
					    		a.setIsStarted(rs.getInt((myAgent.columNameGestartet+i))!=0);	//converts to boolean
					    		a.setIsFinished(rs.getInt((myAgent.columNameBeendet+i))!=0);	//converts to boolean
					    			double hours_sim_time_start = rs.getDouble(myAgent.columNameStartIst + i);
					    			double hours_sim_time_end = rs.getDouble(myAgent.columNameEndeIst + i);
					    			
					    				
					    		//a.setStartdate((float) (hours_sim_time_start * (1000*60*60)) + myAgent.start_simulation);
					    		//a.setEnddate((float) (hours_sim_time_end * (1000*60*60)) + myAgent.start_simulation);
					    		
					    		//double hours_sim_time_start_soll = (double) (Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()) - this.start_simulation) / (1000*60*60);
					    		//long real_time = (hours_sim_time_start_soll * (1000*60*60)) + myAgent.start_simulation;
					    		break;			//leave the while loop
					    	}
					    	/*
					    	if(a.getHasOperation().getType().equals("production")) {
					    		a.setIsStarted(rs.getInt((myAgent.columNameGestartet+i))!=0);	//converts to boolean
					    		a.setIsFinished(rs.getInt((myAgent.columNameBeendet+i))!=0);	//converts to boolean
					    		a.setStartdate((float) rs.getDouble(myAgent.columNameStartIst + i));
					    		a.setEnddate((float) rs.getDouble(myAgent.columNameEndeIst + i));
					    		break;			//leave the while loop
					    	}
					    	
					    	if(a.getHasResource().getID_Number() == i) {
					    		a.setIsStarted(rs.getInt((myAgent.columNameGestartet+i))!=0);	//converts to boolean
					    		a.setIsFinished(rs.getInt((myAgent.columNameBeendet+i))!=0);	//converts to boolean
					    		a.setStartdate((float) rs.getDouble(myAgent.columNameStartIst + i));
					    		a.setEnddate((float) rs.getDouble(myAgent.columNameEndeIst + i));
					    		break;			//leave the while loop
					    	}*/
					    }
					}			
		        }
			}
		}				 
	}


	private void receiveValuesFromDB(AllocatedWorkingStep relevant_allWS) {
		
		
		
		
		try (Connection con = myAgent.getConnection(); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				){
			ResultSet rs = null;
			if(disturbance_type.equals("Delay")) {	//old testing with access
				rs = stmt.executeQuery(		
		    			"select "+columnNameOfFinished+" , "+columnNameOfStarted+" from "+nameOfMES_Data+" where "+columnNameOfOperation+" = '"+relevant_allWS.getHasOperation().getName()+"' and "+columnNameAuftrags_ID+" = '"+relevant_allWS.getHasOperation().getAppliedOn().getID_String()+"'"); //Selektiere alle Spalten (*) der Tabelle tblOrder in SQL Sprache	 	      
		        while (rs.next()) {
		        	relevant_allWS.setIsFinished(rs.getBoolean(columnNameOfFinished));
		        	relevant_allWS.setIsStarted(rs.getBoolean(columnNameOfStarted));
		        }	        
			}
		
			/*
			else if(disturbance_type.equals("Workpiece_Error")) {
				rs = stmt.executeQuery(		
		    			"select "+myAgent.columnNameOfIstStart+" , "+myAgent.columnNameOfIstEnd+" , "+columnNameOfFinished+" , "+columnNameOfStarted+" from "+nameOfMES_Data+" where "+columnNameOfOperation+" = '"+relevant_allWS.getHasOperation().getName()+"' and "+columnNameAuftrags_ID+" = '"+relevant_allWS.getHasOperation().getAppliedOn().getID_String()+"'"); //Selektiere alle Spalten (*) der Tabelle tblOrder in SQL Sprache	 	      
				while (rs.next()) {
		        	relevant_allWS.setIsFinished(rs.getBoolean(columnNameOfFinished));
		        	relevant_allWS.setIsStarted(rs.getBoolean(columnNameOfStarted));
		        		
		        	relevant_allWS.setHasTimeslot(null);
		        }
			}*/
			

	       
	        rs.close();
		}

	    catch (SQLException e ) {
	    	e.printStackTrace();
	    } 
		
	}


}
