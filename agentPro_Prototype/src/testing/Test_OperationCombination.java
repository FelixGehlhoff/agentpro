package testing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.lang.math.RandomUtils;
import org.jfree.ui.RefineryUtilities;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Timeslot;
import agentPro.onto.WorkPlan;
import agentPro.onto.Workpiece;
import agentPro_Prototype_Agents._Agent_Template;
import support_classes.OperationCombination;
import support_classes.XYTaskDataset_Total;

public class Test_OperationCombination {
	public static long timeOfFinish = 0;
	public static long timeOfStart = 0;
	public static long total_duration = 0;
	static double durationSetup = 0;
	static double costs = 0;
	static double utilization = 0;
	public static void main(String[] args){
		String wp_id = "A_1.1";
		String wp_id2 = "B_2.2";
		String wp_id3 = "A_3.3";
		WorkPlan wp = createWorkplanFromDatabase(wp_id);
		WorkPlan wp2 = createWorkplanFromDatabase(wp_id2);
		WorkPlan wp3 = createWorkplanFromDatabase(wp_id3);
		
		@SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep>it = wp.getConsistsOfAllocatedWorkingSteps().iterator();
		while(it.hasNext()) {
			wp2.addConsistsOfAllocatedWorkingSteps(it.next());
		}
		@SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep>ite = wp3.getConsistsOfAllocatedWorkingSteps().iterator();
		while(ite.hasNext()) {
			wp2.addConsistsOfAllocatedWorkingSteps(ite.next());
		}
		 XYTaskDataset_Total demo = new XYTaskDataset_Total("JFreeChart : XYTaskDataset_Total.java", wp2);
	        demo.pack();
	        RefineryUtilities.centerFrameOnScreen(demo);
	        demo.setVisible(false);	
		
	/*
		WorkPlan wp2 = createWorkplanFromDatabase(wp_id2);
		calculateValues(wp);
		System.out.println("start : "+_Agent_Template.SimpleDateFormat.format(timeOfStart)+" finish : "+_Agent_Template.SimpleDateFormat.format(timeOfFinish)+" total_duration= "+total_duration/(1000*60)+" setup= "+durationSetup+" costs= "+costs+" utilization= "+utilization);
		calculateValues(wp2);
		System.out.println("start : "+_Agent_Template.SimpleDateFormat.format(timeOfStart)+" finish : "+_Agent_Template.SimpleDateFormat.format(timeOfFinish)+" total_duration= "+total_duration/(1000*60)+" setup= "+durationSetup+" costs= "+costs+" utilization= "+utilization);

		Timeslot a = new Timeslot();
			a.setStartDate("10");
			a.setEndDate("100");
			
		Timeslot b = new Timeslot();
			b.setStartDate("20");
			b.setEndDate("200");
			ArrayList<Timeslot>list = new ArrayList<Timeslot>();
			list.add(b);
			list.add(a);
			
			Comparator<Timeslot> comparator = Comparator.comparing(Timeslot::getEndDate);
			Collections.sort(list, comparator);
			System.out.println(list.get(0).getEndDate());
			int ab = 15;
			long longNumber = (long)ab*1000*60;
			long longNumber2 = (long) (ab*1000*60);
			System.out.println(longNumber+" "+longNumber2);*/
	}
	
	public static void calculateValues(WorkPlan wp) {		
		wp = _Agent_Template.sortWorkplanChronologically(wp);	//sorts it chronologically
		AllocatedWorkingStep lastStep = (AllocatedWorkingStep) wp.getConsistsOfAllocatedWorkingSteps().get(wp.getConsistsOfAllocatedWorkingSteps().size()-1);
		AllocatedWorkingStep firstStep = (AllocatedWorkingStep) wp.getConsistsOfAllocatedWorkingSteps().get(0);
		
		timeOfFinish = (Long.parseLong(lastStep.getHasTimeslot().getEndDate()));
		timeOfStart = (Long.parseLong(firstStep.getHasTimeslot().getStartDate()));
		total_duration = timeOfFinish-timeOfStart;
		durationSetup = _Agent_Template.calculateDurationSetup(wp);
			Proposal initial_proposal_production = new Proposal();
			initial_proposal_production.setPrice(10);
			ArrayList<Proposal>proposal_list_followup = new ArrayList<Proposal>();
			proposal_list_followup.add(initial_proposal_production);
			proposal_list_followup.add(initial_proposal_production);
			proposal_list_followup.add(initial_proposal_production);
		costs = calculateCosts(initial_proposal_production, proposal_list_followup);
		utilization = _Agent_Template.calculateUtilization(wp);	
		
		}

	private static WorkPlan createWorkplanFromDatabase(String wp_id) {
		WorkPlan workplan = new WorkPlan();
		
		try (Connection con = DriverManager.getConnection(_Agent_Template.dbaddress_sim); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				){

			    	ResultSet rs = stmt.executeQuery(		
			    			//"select * from "+nameOfMES_Data+" where "+columnNameOfOperation+" = '"+allWorkingStep.getHasOperation().getName()+"' and "+columnNameAuftrags_ID+" = '"+allWorkingStep.getHasOperation().getAppliedOn().getID_String()+"' and "+columnNameFinished+" = 'false'"); 
			    			"select * from "+_Agent_Template.prefix_schema+".total_operations"+" where "+_Agent_Template.columnNameAuftrags_ID+" = '"+wp_id+"'"); 
					// System.out.println("mes__table_to_be_used "+mes_table_to_be_used);      
			    	if (rs.isBeforeFirst() ) {			    	//the SQL query has returned data  
			    		while(rs.next()) {
			    			AllocatedWorkingStep allWS = new AllocatedWorkingStep();
			    			allWS.setID_String("Test");
				        	Timeslot timeslot = new Timeslot();		     
				        		timeslot.setStartDate(String.valueOf(rs.getTimestamp(_Agent_Template.columnNameOfPlanStart).getTime()));
				        		timeslot.setEndDate(String.valueOf(rs.getTimestamp(_Agent_Template.columnNameOfPlanEnd).getTime()));
				        		timeslot.setLength(rs.getTimestamp(_Agent_Template.columnNameOfPlanEnd).getTime()-rs.getTimestamp(_Agent_Template.columnNameOfPlanStart).getTime());
				        		allWS.setHasTimeslot(timeslot);
				        	allWS.setIsStarted(rs.getBoolean(_Agent_Template.columnNameOfStarted));	
				        	allWS.setIsFinished(rs.getBoolean(_Agent_Template.columnNameOfFinished));	
				        	Resource res = new Resource();
				        		res.setName(rs.getString(_Agent_Template.columnNameOfResource));
				        		res.setID_Number(rs.getInt(_Agent_Template.columnNameOfResource_ID));
				        		res.setDetailed_Type("Test");
				        		allWS.setHasResource(res);
				        	Operation op = new Operation();
				        		op.setName(rs.getString(_Agent_Template.columnNameOfOperation));
				        			Workpiece wp = new Workpiece();
				        			wp.setID_String(wp_id);
				        		op.setAppliedOn(wp);
				        		op.setType(rs.getString(_Agent_Template.columnNameOperation_Type));			        		
				        		op.setSet_up_time(10);
				        		op.setAvg_Duration(20);
				        		allWS.setHasOperation(op);
				        	workplan.addConsistsOfAllocatedWorkingSteps(allWS);
			    		}
			    	rs.close();	 
			        System.out.println(_Agent_Template.printoutWorkPlan(workplan, "test_agent"));   
			    	}else {
			    		System.out.println("No data found for id: "+wp_id);
			    
			    	}
      
	    } catch (SQLException e ) {
	        e.printStackTrace();
	    }	
		return workplan;
	}
	private static double calculateCosts(Proposal initial_proposal_production2, ArrayList<Proposal> proposal_list_followup2) {
		double costs = 0;
		costs += initial_proposal_production2.getPrice();
		for(Proposal prop : proposal_list_followup2) {
			costs += prop.getPrice();
		}
		return costs;
	}
}
