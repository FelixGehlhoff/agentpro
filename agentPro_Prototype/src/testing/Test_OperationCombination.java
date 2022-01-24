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

import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.lang.math.RandomUtils;
import org.jfree.ui.RefineryUtilities;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Timeslot;
import agentPro.onto.Transport_Operation;
import agentPro.onto.WorkPlan;
import agentPro.onto.Workpiece;
import agentPro_Prototype_Agents._Agent_Template;
import agentPro_Prototype_InterfaceAgent_Behaviours.ReceiveInformOrderCompletionBehaviour;
import support_classes.Interval;
import support_classes.OperationCombination;
import support_classes.XYTaskDataset_Total;
import webservice.ManufacturingOrder;

public class Test_OperationCombination {
	public static long timeOfFinish = 0;
	public static long timeOfStart = 0;
	public static long total_duration = 0;
	static double durationSetup = 0;
	static double costs = 0;
	static double utilization = 0;
	static Comparator<Helper> comparatorDuedate = new Comparator<Helper>() {  
        @Override  
        public int compare(Helper m1, Helper m2) {  
        	return m1.number.compareTo(m2.number); 
        	//return o1.name.compareToIgnoreCase(o2.name);  
        }  
   };  
   static Comparator<Helper> comparatorMaterial = new Comparator<Helper>() {  
       @Override  
       public int compare(Helper m1, Helper m2) {  
       	return m1.name.compareToIgnoreCase(m2.name);
       	//return o1.name.compareToIgnoreCase(o2.name);  
       }  
  };  
  public class Helper{
	  Long number;
	  String name;
  }
  
	@SuppressWarnings("unchecked")
	public static void main(String[] args){
		String xx = "A_B_C";
		String [] split = xx.split("_", 2);
		System.out.println(split[0]);
		System.out.println(split[1]);
		Test_OperationCombination b = new Test_OperationCombination();
		Helper one = b.new Helper();
		one.number = 100L;
		one.name = "ABC";
		Helper two = b.new Helper();
		two.number = 250L;
		two.name = "BCD";
		ArrayList<Helper>a = new ArrayList<Helper>();
		
		a.add(two);
		a.add(one);
		Helper three = b.new Helper();
		three.name = "CCC";
		three.number = 250L;
		a.add(three);
		Helper f = b.new Helper();
		f.name = "BCD";
		f.number = 250L;
		a.add(f);
		//Sortierung
		@SuppressWarnings("rawtypes")
		ComparatorChain chain = new ComparatorChain();  
        chain.addComparator(comparatorDuedate);  
        chain.addComparator(comparatorMaterial);  
		for(Helper mo : a) {
			System.out.println(" Delivery Date: "+mo.number+" Article: "+mo.name);
		}
		Collections.sort(a, chain);
		for(Helper mo : a) {
			System.out.println(" Delivery Date: "+mo.number+" Article: "+mo.name);
		}
		
		
		
		/*int quantity = (int) (Math.random()*100);

		System.out.println(quantity);
		
		System.out.println(feasibilityCheckAndDetermineDurationParameters());
		*/
		createGantt();
		
		
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
	
	private static void createGantt() {
		ArrayList <String> orders = new ArrayList<String>();
		try {
			orders = ReceiveInformOrderCompletionBehaviour.getOrdersFromDB();
			System.out.println(orders);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WorkPlan total_wp = ReceiveInformOrderCompletionBehaviour.createWorkPlan(orders);

		 XYTaskDataset_Total demo = new XYTaskDataset_Total("JFreeChart : XYTaskDataset_Total.java", total_wp);
	        demo.pack();
	        RefineryUtilities.centerFrameOnScreen(demo);
	        demo.setVisible(false);	
		
	}
public static boolean feasibilityCheckAndDetermineDurationParameters() {		
		Interval range = new Interval(25,100, false);
		
		boolean return_value = true;
		
		//System.out.println("DEBUG___"+this.getName()+" range "+range.toString()+" contains "+end.getCoordX()+" and contains "+ start.getCoordX());
		
		if(range.contains((long)35) && range.contains((long)60)) {
			
		}else {
			return_value =  false;
		}
	
		return return_value;
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
