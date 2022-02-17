package support_classes;

public interface Run_Configuration {
	//Orders and Interfaces
	int limit = 2; //number of orders to create	
	long initial_wait =12000;
	boolean IntervalWait = false;
	double wait_between_agent_creation = 750;	
	double wait_betwee_agent_creation_max = 1200;
	long db_actication_wait = 15000;
	boolean random_order_generation = false;
	int numberOfProducts = 3;
	long reply_by_time_wp_agent = 2550; //war 200 war 1250
	long reply_by_time_resource_agent = 600; //war 125 war 300  //momentan 2*reply_ny_time in receive order behaviour
	double factor_request_performer = 0.015;
	
	boolean webservice_mode = false;
	boolean simulate_order_generation = true;
	Boolean simulation_enercon_mode = false;
	
	//Agent System Cofiguration
		int duration_repair_workpiece = 20;
		int duration_light_disturbance = 2;
		int duration_severe_disturbance = 8;
		long bufferThreshold = 15;
		//float buffer = 5*60*1000;	//5 minutes Buffer in ms
		long start_simulation = 1533074400000L; //01.08.2018 00:00
		long start_simulation_agentpto = 1556632800000L; //Tue Apr 30 2019 16:00:00 GMT+0200 1556632800000L
		long time_until_end = (long) 1000*60*60*30*31;// 25 pieces (24+6 buffer) * 31 h 1000*60*60*24; //24 h
		String opimizationCriterion = "time_of_finish"; //duration_setup    //TODO receive that from database? TBD
		long start_free_interval_resources = 1556632800000L;
		
	//For algorithm
		/*needs to be adjusted in case of transportation*/ int avg_pickUp = 10;//10; 
		int minimal_distance = 5;
		double transport_speed = 0.0833333333;   // resulting in 60s for 5 m
		long transport_estimation = (long) 1000*60*(avg_pickUp*2+ (long) (minimal_distance/(transport_speed*60)));//(long) 1000*60*15;	//estimated duration of transport = 15 min
		long transport_estimation_CFP = 0;//(long) 1000*60*15;	//estimated duration of transport = 15 min
		//long transport_estimation = 0;//(long) 1000*60*15;
		
		boolean transport_needed = true;
		
		boolean consider_shared_resources = false;
		
		Boolean parallel_processing_pick_and_setup_possible = false;
		String gant_1 = "D:/TeamDrive/Agent.Pro/04_AgentPro/Prototyp/GANTT_Charts/eclipse/";
		String gant_2 = "_gantt";
		boolean include_locations_in_transport_operation_name = false;
		
		
		
		
}
