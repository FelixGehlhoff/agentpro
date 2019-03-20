package testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import support_classes.Interval;

public class Test_transport_algorithm {
	
	 static int start_cfp;
	 static int end_cfp;
	 static int lower_bound;
	 static int upper_bound;
	 static int buffer;
	 static int effr;
	 static int sfef;
	 static int d_eff;
	 static int d2WP;
	 static int TI;
	
	public static void main(String[] args){
		
		 	 start_cfp = 100;
		 	 end_cfp = 200;
		 	 lower_bound = 100;
		 	 upper_bound = 300;
		 	 buffer = 110;
		 	 effr = end_cfp - buffer;
		 	 d_eff = 20;
		 	 sfef = effr-d_eff;
		 	 d2WP = 20;
		 	 TI = 10;
		 	 
		 	 ArrayList<Interval> listOfIntervals = calculateIntervals();
			 checkFeasibility(listOfIntervals);
		 	
		 	listOfIntervals.forEach((a)->System.out.println(a.toString()));
		 	checkSchedule(listOfIntervals);
		 	System.out.println("AFTER Check Schedule");
		 	 sortArrayEarliestFirst(listOfIntervals);
		 	listOfIntervals.forEach((a)->System.out.println(a.getId()+" "+a.toString()));
		 	
		 	long lb = 1533430988800L;
		 	float duration_to_get_to_workpiece = 25F*60*1000;
		 	System.out.println(duration_to_get_to_workpiece+" "+(long) duration_to_get_to_workpiece+" "+(lb+duration_to_get_to_workpiece)+" "+(long)(lb+duration_to_get_to_workpiece)+" "+(lb+(long)duration_to_get_to_workpiece));
	
	}
	
	
	
	private static void checkSchedule(ArrayList<Interval> listOfIntervals_possibleFromResourceSide) {
		int counter = 1; //no of element
		 @SuppressWarnings("unchecked")
			Iterator<Interval> it = listOfIntervals_possibleFromResourceSide.iterator();		 	
		    while(it.hasNext()) {
		    	Interval i = it.next();
		    	//check Free Interval parameters
		    	if(i.lowerBound()-d2WP< lower_bound && i.upperBound()<=upper_bound-TI) {
		    		System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because LB_Ress-d2WP "+(i.lowerBound()-d2WP)+" < "+lower_bound+" lower_bound_Transporter --> start too early FOR TRANSPORTER");
		    		it.remove();
		 		 }else if(i.lowerBound()-d2WP>= lower_bound && i.upperBound() > upper_bound-TI){
		 			System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because UB_Ress "+i.upperBound()+" > "+(upper_bound-TI)+" upper_bound_transporter --> Finish too late for TRANSPORTER");
		    		it.remove();
		 		 }else if(i.lowerBound()-d2WP< lower_bound && i.upperBound() > upper_bound-TI){
		 			System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because UB "+i.upperBound()+" > "+(upper_bound-TI)+" upper_bound_transporter  --> Finish too late for TRANSPORTER AND because LB_Ress "+(i.lowerBound()-d2WP)+" < "+lower_bound+" lower_bound_Transporter --> start too early FOR TRANSPORTER");
		    		it.remove();
		 		 }else {
		 			 //fine
		 		 }
		    	counter++;
		    }
	}



	private static void sortArrayEarliestFirst(ArrayList<Interval> arrayList) {
		Comparator<Interval> comparator = Comparator.comparing(Interval::upperBound);
		Collections.sort(arrayList, comparator);
		
	}



	//checks if for this free interval the calculated intervals are feasible from resource side
	private static void checkFeasibility(ArrayList<Interval> listOfIntervals) {
		int counter = 1; //no of element
		 @SuppressWarnings("unchecked")
			Iterator<Interval> it = listOfIntervals.iterator();		 	
		    while(it.hasNext()) {
		    	Interval i = it.next();
		    	System.out.println(counter+" "+	i.getId()+" "+i.getSize());
				//start >= earliest start, end >= earliest end
		    	if(i.lowerBound()< start_cfp && i.upperBound()>=effr) {
		    		System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because LB "+i.lowerBound()+" < "+start_cfp+" start_cfp --> start too early");
		    		it.remove();
		 		 }else if(i.lowerBound()>= start_cfp && i.upperBound()< effr){
		 			System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because UB "+i.upperBound()+" < "+effr+" effr --> Finish too early");
		    		it.remove();
		 		 }else if(i.lowerBound()< start_cfp && i.upperBound()< effr){
		 			System.out.println("REMOVED: "+counter+" "+i.getId()+" "+i.toString()+" because UB "+i.upperBound()+" < "+effr+" effr --> Finish too early AND because LB "+i.lowerBound()+" < "+start_cfp+" start_cfp --> start too early");
		    		it.remove();
		 		 }else {
		 			 //fine
		 		 }
		    	counter++;
		    }
	}




	public static ArrayList<Interval> calculateIntervals() {	//for feasibility checking the arrival dates AT THE RESSOURCES are important
		ArrayList<Interval> array = new ArrayList<>();
		
		Interval end_at_effr = new Interval(sfef, effr, false);
		Interval start_at_CFP_start_minus_d2WP = new Interval(start_cfp, start_cfp+d_eff, false);
		Interval start_at_CFP_start = new Interval (start_cfp+d2WP, start_cfp+d2WP+d_eff, false); // should not be needed!
		Interval end_at_latest_end = new Interval(end_cfp-d_eff, end_cfp);
		Interval start_at_lowerbound = new Interval(lower_bound+d2WP, lower_bound+d2WP+d_eff, false);
		Interval end_at_upperbound = new Interval(upper_bound-d_eff-TI, upper_bound-TI, false);
		array.add(end_at_effr);
		array.get(0).setId("end_at_effr");
		array.add(start_at_CFP_start_minus_d2WP);
		array.get(1).setId("start_at_CFP_start_minus_d2WP");
		array.add(start_at_CFP_start);
		array.get(2).setId("start_at_CFP_start");
		array.add(end_at_latest_end);
		array.get(3).setId("end_at_latest_end");
		array.add(start_at_lowerbound);
		array.get(4).setId("start_at_lowerbound");
		array.add(end_at_upperbound);
		array.get(5).setId("end_at_upperbound");
		
		return array;		
	}
	
	public static void calculateValues() {
		
	}

}
