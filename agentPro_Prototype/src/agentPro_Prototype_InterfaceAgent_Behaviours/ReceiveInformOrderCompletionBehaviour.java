package agentPro_Prototype_InterfaceAgent_Behaviours;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.sql.Date;

import java.util.Iterator;


import org.jfree.ui.RefineryUtilities;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Operation;
import agentPro.onto.Resource;
import agentPro.onto.Timeslot;
import agentPro.onto.WorkPlan;
import agentPro.onto.Workpiece;
import agentPro_Prototype_Agents.InterfaceAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import support_classes.XYTaskDataset_Total;

/*
 * Listens for OrderCompletion messages and sends INFORM to the ERP system (dummy agent)
 */

public class ReceiveInformOrderCompletionBehaviour extends CyclicBehaviour{

	private static final long serialVersionUID = 1L;
	private InterfaceAgent myAgent;
	private String conversationID_forOrderagent;
	private String logLinePrefix = ".ReceiveInformOrderCompletionBehaviour ";
	
	private String DateFormat = "yyyy-MM-dd HH:mm:ss";
	public SimpleDateFormat SimpleDateFormat = new SimpleDateFormat(DateFormat);
	
	//database
	/*
		public String nameOfMES_Data_Resource_Veiw = "MES_Data_Resource_View";
		public String columnNameOfOperation = "Operation";
		public String columnNameOfResource = "Ressource";
		public String columnNameOfResource_ID = "Ressource_ID";
		public String columnNameOfPlanStart = "PlanStart";
		public String columnNameOfPlanEnd = "PlanEnde";
		public String columnNameAuftrags_ID = "Auftrags_ID";
		public String columnNameOperation_Type = "Operation_Type";
		public String columnNameOfStarted = "Started";
		public String columnNameOfOperation_Type = "Operation_Type";
	*/
	public ReceiveInformOrderCompletionBehaviour(InterfaceAgent myAgent, String conversationID) {
		super(myAgent);
		this.myAgent = myAgent;
		conversationID_forOrderagent = conversationID;
		
	}
	
	@Override
	public void action() {
		
		// Receive message

		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate mt2 = MessageTemplate.MatchConversationId(conversationID_forOrderagent);	
        MessageTemplate mt_total = MessageTemplate.and(mt1,mt2);
        
		ACLMessage inform = myAgent.receive(mt_total);
		if (inform != null) {
			System.out.println(myAgent.SimpleDateFormat.format(new java.util.Date())+" "+myAgent.getLocalName()+logLinePrefix+" Order: "+inform.getContent()+" complete "+inform.getContent());
			
			if(myAgent.simulation_mode) {
				
			}else {
				//wait for last entry in DB (transport process to warehouse outbound
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//create Workplan
				WorkPlan workplan = new WorkPlan();
				receiveValuesFromDB(workplan);
				
				WorkPlan sorted_workplan = sortWorkplanChronologically(workplan);
				//printoutWorkPlan(sorted_workplan);
				
				//create GANTT Chart
				
				 XYTaskDataset_Total demo = new XYTaskDataset_Total("JFreeChart : XYTaskDataset_Total.java", sorted_workplan);
			        demo.pack();
			        RefineryUtilities.centerFrameOnScreen(demo);
			        demo.setVisible(false);	
				
			}
			
			
			//pass info about finished order to XYZ
			ACLMessage inform_done = new ACLMessage(ACLMessage.INFORM);
			
			inform_done.addReceiver(myAgent.getERP_system());
			inform_done.setContent(inform.getContent());	
			inform_done.setConversationId("ERP");
		
			myAgent.send(inform_done);		
			
		}
		else {
			block();
		}
		
	}
	
	public WorkPlan sortWorkplanChronologically(WorkPlan workplan) {
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
public void receiveValuesFromDB(WorkPlan workplan) {
	  Statement stmt = null;
	  String query1 = "";
	  /*
		if(myAgent.simulation_mode) {		//workplan only contains production steps
			 try {
			        stmt = myAgent.getConnection().createStatement();
			        ResultSet rs = null;
		query1 = "select * from "+myAgent.nameOfProductionPlan+" where "+myAgent.columnNameID+" = "+disturbance.getId_workpiece();
					
	    int number_of_production_steps_without_buffer = determine_number_of_planned_production_steps(workplan);
		for(int i = 1;i <= number_of_production_steps_without_buffer ;i++) {	
			String c_StartIst = myAgent.columNameStartIst + i;
			String c_Gestartet = myAgent.columNameGestartet + i;
			String c_EndeIst = myAgent.columNameEndeIst + i;
			String c_Beendet = myAgent.columNameBeendet + i;
			query1 = query1 + c_StartIst+" , "+c_Gestartet+" , "+c_EndeIst+" , "+c_Beendet;
		}
		query1 = query1 + " from "+myAgent.nameOfProductionPlan+" where "+myAgent.columnNameID+" = "+disturbance.getId_workpiece();
		rs = stmt.executeQuery(query1); 	//result set should contain StartIst1 = 123 .... StartIst7 = 789 <-- error resource
			while (rs.next()) {
			
	        }
			} catch (SQLException e ) {
		    	e.printStackTrace();
		    } 	
		
		
		}
		*/
		//else {
		 query1 = "select "+myAgent.columnNameOfOperation+" , "+myAgent.columnNameOperation_Type +" , "+myAgent.columnNameOfResource+" , "+myAgent.columnNameOfResource_ID+" , "+myAgent.columnNameOfPlanStart+" , "+myAgent.columnNameOfPlanEnd+" , "+myAgent.columnNameAuftrags_ID+" from "+myAgent.nameOfMES_Data_Resource;
		    try {
		        stmt = myAgent.getConnection().createStatement();
		        ResultSet rs = stmt.executeQuery(query1);
	       		
		        while (rs.next()) {
		        	AllocatedWorkingStep allocWS = new AllocatedWorkingStep();
		        		Operation op = new Operation();      		
		        			Workpiece wp = new Workpiece();
		        			wp.setID_String(rs.getString(myAgent.columnNameAuftrags_ID));
		        		op.setAppliedOn(wp);
		        		op.setName(rs.getString(myAgent.columnNameOfOperation));
		        		op.setType(rs.getString(myAgent.columnNameOperation_Type));
		        	allocWS.setHasOperation(op);
		        		Resource res = new Resource();
		        		res.setID_Number(rs.getInt(myAgent.columnNameOfResource_ID));
		        		res.setName(rs.getString(myAgent.columnNameOfResource));
		        	allocWS.setHasResource(res);
		        		Timeslot ts = new Timeslot();
		        			Date startdate = rs.getDate(myAgent.columnNameOfPlanStart);
		        			Time start_time	= rs.getTime(myAgent.columnNameOfPlanStart);
		        			//TBD plus eine Stunde muss gerechnet werden --> warum?
		    
		        		    
		        			//Calendar cal = Calendar.getInstance(TimeZone.);	        			
		        			//Date date_new = rs.getDate(columnNameOfPlanStart, cal);
		        			//cal.setTimeInMillis(date_new.getTime());
		        			long long_value_start = startdate.getTime()+start_time.getTime()+1*60*60*1000;
		        			
		        		  	//System.out.println("DEBUG____test long "+long_value_start+"____startdate__"+startdate+" getTime "+startdate.getTime()+" time "+start_time.getTime());
		        			Date enddate = rs.getDate(myAgent.columnNameOfPlanEnd);
		        			Time end_time	= rs.getTime(myAgent.columnNameOfPlanEnd);
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
		//}  
		    
	
		

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

public void printoutWorkPlan(WorkPlan wp) {
	String printout = myAgent.getLocalName()+" DEBUG____WORKPLAN	";
	@SuppressWarnings("unchecked")
	
	Iterator<AllocatedWorkingStep> it_2 = wp.getConsistsOfAllocatedWorkingSteps().iterator();		 	
    while(it_2.hasNext()) {
    	AllocatedWorkingStep a = it_2.next();
    	printout = printout +" "+ SimpleDateFormat.format(Long.parseLong(a.getHasTimeslot().getStartDate()))+";"+SimpleDateFormat.format(Long.parseLong(a.getHasTimeslot().getEndDate()));
    }
    System.out.println(printout);
}
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
}
}
