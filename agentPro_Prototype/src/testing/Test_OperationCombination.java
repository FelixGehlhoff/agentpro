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

	
	private static double calculateCosts(Proposal initial_proposal_production2, ArrayList<Proposal> proposal_list_followup2) {
		double costs = 0;
		costs += initial_proposal_production2.getPrice();
		for(Proposal prop : proposal_list_followup2) {
			costs += prop.getPrice();
		}
		return costs;
	}
}
