package testing;

import java.util.ArrayList;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Proposal;
import agentPro.onto.Timeslot;
import support_classes.Interval;

public class Tests {
	static ArrayList<Interval> busyIntervals = new ArrayList<Interval>();
	public static void main(String[] args) {
		Proposal prop = new Proposal();
		prop.setID_String("A");
		AllocatedWorkingStep allWS = new AllocatedWorkingStep();
		Timeslot ts = new Timeslot();
		ts.setStartDate("100");
		ts.setEndDate("200");		
		allWS.setHasTimeslot(ts);
		prop.addConsistsOfAllocatedWorkingSteps(allWS);
		
		//Interval busy2 = new Interval(300,400,false);
		Interval busy1 = new Interval(50,150,false);
		
		busyIntervals.add(busy1);
		//busyIntervals.add(busy2);
		
		
		CFP cfp = new CFP();
		cfp.setID_String("A");
		Timeslot ts_cfp = new Timeslot();
		ts_cfp.setStartDate("500");
		ts_cfp.setEndDate("700");
		cfp.setHasTimeslot(ts_cfp);
		
		
		Boolean test = checkProposalDependency(prop, cfp);
		System.out.println(test);

	}
	
	private static Boolean checkProposalDependency(Proposal prop, CFP cfp) {
		if(prop.getID_String().equals(cfp.getID_String())) {		//the proposal was created for the same ID such as A_1_Order@Durchsatz
			//now check if the proposal timeslot is the closest to the cfp compared to existing busy intervals
			for(int i = busyIntervals.size()-1;i>=0;i--) {
				Interval busy_interval = busyIntervals.get(i);		
				AllocatedWorkingStep allWS = (AllocatedWorkingStep) prop.getConsistsOfAllocatedWorkingSteps().get(0);
				if(Long.parseLong(cfp.getHasTimeslot().getStartDate())-busy_interval.upperBound() < Long.parseLong(cfp.getHasTimeslot().getStartDate())-Long.parseLong(allWS.getHasTimeslot().getEndDate())) {
					return false; //there is no dependency
				}
			}
		}
		return true;
	}

}
