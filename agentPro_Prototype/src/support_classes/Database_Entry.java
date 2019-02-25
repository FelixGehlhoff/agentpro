package support_classes;

import java.sql.SQLException;

import agentPro.onto.WorkPlan;
import agentPro_Prototype_Agents.ResourceAgent;

public class Database_Entry implements Runnable{
	private ResourceAgent myAgent;
	private WorkPlan workplan;
	
	public Database_Entry (ResourceAgent agent, WorkPlan wp) {
		this.myAgent = agent;
		this.workplan = wp;
	}

	@Override
	public void run() {
		/*
		try {
			//myAgent.addDataToDatabase("resource", workplan);
		} catch (SQLException e) {
			e.printStackTrace();					
		}
	*/
	}

}
