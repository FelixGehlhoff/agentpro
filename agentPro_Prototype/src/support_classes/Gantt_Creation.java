package support_classes;

import org.jfree.ui.RefineryUtilities;

import agentPro.onto.WorkPlan;

public class Gantt_Creation implements Runnable{
	private String agent_local_name;
	private WorkPlan Workplan;
	
	public Gantt_Creation (WorkPlan wp, String local_name) {
		this.agent_local_name = local_name;
		this.Workplan = wp;
	}

	@Override
	public void run() {
		XYTaskDatasetDemo2 demo = new XYTaskDatasetDemo2(
                "JFreeChart : XYTaskDatasetDemo2.java", Workplan, agent_local_name);

	 
        demo.pack();
      
        RefineryUtilities.centerFrameOnScreen(demo);
   
        demo.setVisible(false);	
   
		
	}

}
