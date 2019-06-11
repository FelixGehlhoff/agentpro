package agentPro_Prototype_Agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import agentPro.onto.AgentPro_ProductionOntology;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Cancellation;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Resource;
import agentPro.onto.Timeslot;
import agentPro.onto.WorkPlan;
import agentPro.onto.Workpiece;
import agentPro.onto._SendCancellation;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import support_classes.Interval;
import support_classes.OperationCombination;

/*
 * Serves as a template for other agents and provides take down procedure and some other variables
 */

public abstract class _Agent_Template extends Agent{
	
	private static final long serialVersionUID = 1L;
	private static String DateFormat = "yyyy-MM-dd HH:mm:ss";
	public static SimpleDateFormat SimpleDateFormat = new SimpleDateFormat(DateFormat);
	public String logLinePrefix;
	public String ability;
	public ArrayList <DFAgentDescription> resourceAgents = new ArrayList<DFAgentDescription>();
	public Boolean showMessageContent = true;
	private WorkPlan workplan;
	public long time_until_end = (long) 1000*60*60*30*31;// 25 pieces (24+6 buffer) * 31 h 1000*60*60*24; //24 h
	
	
	//Ontology
	private Ontology ontology = AgentPro_ProductionOntology.getInstance();
	private Codec codec = new SLCodec();
	
	//database
	protected Connection connection;			//Connection to database
	//public final String dbaddress = "jdbc:ucanaccess://C:/Users/Mitarbeiter/Dropbox (HSU_Agent.Pro)/_AgentPro/Prototyp/Database.accdb";	//Address od database
	//public final String dbaddress = "jdbc:mysql://localhost/feedback?"+"user=root&password=SQL_0518";	//Address od database
	public static String dbaddress_sim = "jdbc:mysql://localhost:3306/MySQL?"+"user=root&password=SQL_0518&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false";	//Address od database   serverTimezone=UTC
	
	public static String columnNameChangeOfState = "changeOfState";
	public static String columnNameChangedState = "ChangedState";
	public static String columnNameOfOperation = "Operation"; //Operation
	public static String columnNameOfResource = "Resource";
	public static String columnNameOfResource_ID = "Resource_ID";
	public String columnNameOfSetupTime = "SetupTime";
	public static String columnNameOfPlanStart = "PlanStart";
	public static String columnNameOfPlanEnd = "PlanEnde";
	public String columnNameOfIstStart = "IstStart";
	public String columnNameOfIstEnd = "IstEnde";
	public String columnNameOfDueDate = "dueDate";
	public String columnNameOfReleaseDate = "releaseDate";
	public String columnNameOfPriority = "priority";
	public String columnNameOfNumber = "number";
	public String columnNameOfProduct = "product";
	public String columnNameOfTargetWarehouse = "targetWarehouse";
	public static String columnNameAuftrags_ID = "Auftrags_ID"; //Auftrags_ID
	public static String columnNameOperation_Type = "Operation_Type";
	public static String columnNameOfStarted = "Started";
	public static String columnNameOfFinished = "Finished";
	public static String prefix_schema = "agentpro";
	public static String nameOfMES_Data_Resource = prefix_schema+".total_operations_my";
	public String nameOfMES_Data = prefix_schema+".productionplan_new";
	public String nameOfOrderbook = prefix_schema+".orderbook";
	//private String columnNameFinished = "Finished";
	//private String columnNameOfIstStart = "IstStart";
	//private String columnNameOfIstEnde = "IstEnde";
	
	public static Boolean simulation_enercon_mode = false;
	//public static String prefix_schema = "flexsimdata";
	
	public static String opimizationCriterion = "time_of_finish"; //duration_setup    //TODO receive that from database? TBD
	public static long bufferThreshold = 30;
	
	public static int limit = 2; //number of orders to create
	
	public int duration_repair_workpiece = 20;
	public int duration_light_disturbance = 2;
	public int duration_severe_disturbance = 8;
	public long start_simulation = 1533074400000L; //01.08.2018 00:00
	public long start_simulation_agentpto = 1556632800000L; //Tue Apr 30 2019 16:00:00 GMT+0200 1556632800000L
	public String columnNameStartSimulation = "PlanStart_Simulation";
	public String columnNameEndSimulation = "PlanEnd_Simulation";
	public String columnNameResourceName_simulation = "Bezeichnung";
	public String columnNameErrorType = "Error_Type";
	public String columnNameError_Occur_Time = "Error_Occur_Time";
	public String columnNameID = "ID";
	public String columnNameRunning = "Status";//"On(1)/Off(0)";
	public String columnNameResourceType = "resource_type";
	public String columnNameResourceDetailedType = "resource_detailed_type";
	public String columNameStartIst = "StartIst";		//number is missing, e.g. StartIst1
	public String columNameGestartet = "Gestartet?";
	public String columNameEndeIst = "EndeIst";
	public String columNameBeendet = "Beendet?";
	public String columnNameStartSoll = "StartSoll";
	public String columnNameEndeSoll = "EndeSoll";
	public String tableNameBetriebskalender = prefix_schema+".betriebskalender";
	public String tableNameResource = prefix_schema+".resources";
	public String tableNameProductionPlan = prefix_schema+".productionplan";
	public String tableNameResourceSetupMatrix = prefix_schema+".resources_setupmatrix";
	public String columnNameColumnInProductionPlan = "columninproductionplan";
	public String columnNameLocationX = "locationX";
	public String columnNameLocationY = "locationY";
	public String columNameColumnNameInProductionPlan = "columnnameinproductionplan";
	public String columnNameorderid = "orderid";
	
	public String columnNameOfChangeover = "prod_changeover";
	
	
	protected void setup (){
		System.out.println(SimpleDateFormat.format(new Date())+ " Agent " + getAID().getLocalName() + " started...");
		
		// / INITIALISATION
		// /////////////////////////////////////////////////////////
		//Datenbank
				Connection con;			
				
				try {
					Class.forName("com.mysql.cj.jdbc.Driver");
					con = DriverManager.getConnection(dbaddress_sim);	// Verbindung zur DB mit ucanaccess						
					this.setConnection(con);	     
			        
			    } catch (SQLException e ) {
			        e.printStackTrace();
			    }catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		//registerAtDF();
		
		//Ontology
		getContentManager().registerOntology(ontology);
		getContentManager().registerLanguage(codec);
		
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
		
	}
	
	//abstract void registerAtDF();
	
	protected void takeDown() {
		// Deregister from the yellow pages
		
		try {
			DFService.deregister(this);
			}
			catch (FIPAException fe) {
			fe.printStackTrace();
			}
		System.out.println(SimpleDateFormat.format(new Date()) + logLinePrefix+" Agent "+this.getName()+" has been killed");
		
	}
	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(Ontology ontology) {
		this.ontology = ontology;
	}
	public Codec getCodec() {
		return codec;
	}

	public void setCodec(Codec codec) {
		this.codec = codec;
	}
	
	public static Boolean doLocationsMatch(Location LocationA, Location locationB) {
		if(LocationA.getCoordX() == locationB.getCoordX() && LocationA.getCoordY() == locationB.getCoordY()) {
			return true;
		}else {
			return false;
		}

	}

	public void addResourceAgent(DFAgentDescription result) {
		boolean found = false;
		for(DFAgentDescription agent : resourceAgents) {			
				if(result.getName().getLocalName().equals(agent.getName().getLocalName())) {
					found = true;
				}else {
					
				}			
		}
		if(!found) {
			resourceAgents.add(result);
		}
		
	}
	
	protected Boolean findMatchStringInArrayList(String wp_type, ArrayList<String> enabledWorkpieces2) {
		for(String enabled : enabledWorkpieces2) {
			//System.out.println(this.getLocalName()+" "+enabled+" equals? "+wp_type);
			if(wp_type.equals(enabled)) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<DFAgentDescription> getResourceAgents() {
		return resourceAgents;
	}
	public void printOutSent(ACLMessage msg) {
		String printout = System.currentTimeMillis()+" "+ SimpleDateFormat.format(new Date())+" "+logLinePrefix+" "+ACLMessage.getPerformative(msg.getPerformative())+" in reply to "+msg.getInReplyTo()+" sent to receiver: ";	
		@SuppressWarnings("unchecked")
		Iterator<AID> it = msg.getAllIntendedReceiver();
		    while(it.hasNext()) {
		    	AID receiver = it.next();			
		    	String printoutx = printout+ receiver.getLocalName();
		    	if(showMessageContent && !ACLMessage.getPerformative(msg.getPerformative()).equals("ACCEPT-PROPOSAL")) {
		    		printoutx = printoutx +" with content "+msg.getContent();
				}
		    	if(!receiver.getLocalName().equals("Kranschiene") && !this.getLocalName().equals("Kranschiene")) {
		    		System.out.println(printoutx);	
		    	}			
		    }
	}
	public static WorkPlan createWorkplanFromDatabase(String wp_id) {
		WorkPlan workplan = new WorkPlan();
		
		try (Connection con = DriverManager.getConnection(_Agent_Template.dbaddress_sim); Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				){

			    	ResultSet rs = stmt.executeQuery(		
			    			//"select * from "+nameOfMES_Data+" where "+columnNameOfOperation+" = '"+allWorkingStep.getHasOperation().getName()+"' and "+columnNameAuftrags_ID+" = '"+allWorkingStep.getHasOperation().getAppliedOn().getID_String()+"' and "+columnNameFinished+" = 'false'"); 
			    			"select * from "+_Agent_Template.nameOfMES_Data_Resource+" where "+_Agent_Template.columnNameAuftrags_ID+" = '"+wp_id+"'"); 
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
	public void setResourceAgents(ArrayList<DFAgentDescription> resourceAgents) {
		this.resourceAgents = resourceAgents;
	}
	public WorkPlan getWorkplan() {
		return workplan;
	}

	public void setWorkplan(WorkPlan workplan) {
		this.workplan = workplan;
	}
	
	public static WorkPlan sortWorkplanChronologically(WorkPlan wp) {

		WorkPlan wP_toBeSorted = new WorkPlan();
		
	    @SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> it = wp.getConsistsOfAllocatedWorkingSteps().iterator();
	   
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
	    return wP_toBeSorted;
	}
	public void cancelAllocatedWorkingSteps(AllocatedWorkingStep allWorkingStep, String receiver_string) {
		Resource res = new Resource();
		res.setName(receiver_string);
		allWorkingStep.setHasResource(res);
		cancelAllocatedWorkingSteps(allWorkingStep);
	}
	
	public void cancelAllocatedWorkingSteps(AllocatedWorkingStep allWorkingStep) {
		//create ontology content
		_SendCancellation sendCancellation = new _SendCancellation();
		Cancellation cancellation = new Cancellation();
		cancellation.addConsistsOfAllocatedWorkingSteps(allWorkingStep);
		sendCancellation.setHasCancellation(cancellation);			
		Action content = new Action(getAID(),sendCancellation);
		
		//create ACL Message				
		ACLMessage cancel_acl = new ACLMessage(ACLMessage.CANCEL);
		cancel_acl.setLanguage(getCodec().getName());
		cancel_acl.setOntology(getOntology().getName());	
		AID receiver = new AID();
		if(allWorkingStep.getHasResource()!= null) {
			receiver.setLocalName(allWorkingStep.getHasResource().getName());
			cancel_acl.addReceiver(receiver);
			
			//ontology --> fill content
			try {
				getContentManager().fillContent(cancel_acl, content);
			} catch (CodecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OntologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			send(cancel_acl);
			System.out.println(SimpleDateFormat.format(new Date())+" "+getLocalName()+logLinePrefix+" cancellation for step "+allWorkingStep.getHasOperation().getName()+" sent to receiver "+receiver.getLocalName()+" with content "+cancel_acl.getContent());
			
		}else {
			System.out.println(SimpleDateFormat.format(new Date())+" "+getLocalName()+logLinePrefix+" no resource found for receiving cancellation for step "+allWorkingStep.getHasOperation().getName());
			
		}
		
		
		//myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().remove(i);
		
		//operations_to_be_removed.add(allWorkingStep.getHasOperation().getName());
		//i++;
		
		//delete step in the database not necessary --> can be updated	
}
	public long convertOccuranceTime(float occurance_time) {
		long converted_time = start_simulation + (long) (occurance_time*60*60*1000);
		return converted_time;
	}
	public static String printoutWorkPlan(WorkPlan workplan, String name_of_agent) {
		String printout = name_of_agent+" DEBUG____WORKPLAN	";
		@SuppressWarnings("unchecked")
		
		Iterator<AllocatedWorkingStep> it_2 = workplan.getConsistsOfAllocatedWorkingSteps().iterator();		 	
	    while(it_2.hasNext()) {
	    	AllocatedWorkingStep a = it_2.next();
	    	printout = printout +" "+a.getID_String()+" operation "+a.getHasOperation().getName()+" Slot "+ SimpleDateFormat.format(Long.parseLong(a.getHasTimeslot().getStartDate()))+";"+SimpleDateFormat.format(Long.parseLong(a.getHasTimeslot().getEndDate()));
	    }
	   return printout;
	}
	
	public void activateConnection() {
		Connection con;			
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		
				con = DriverManager.getConnection(dbaddress_sim);	// Verbindung zur DB mit ucanaccess	

			this.setConnection(con);			     
	        
	    } catch (SQLException e ) {
	        e.printStackTrace();
	    }catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected void setConnection(Connection con) {
		this.connection = con;		
	}
	public Connection getConnection() {
		return connection;
	}
	public String printoutArraylistIntervals (ArrayList<Interval> list) {
		String printout = "";
		int counter = 1;
		for(Interval i : list) {
			printout += counter+" "+i.toString();
			counter++;
		}
		return printout;
		
	}

	public static double calculateDurationSetup(WorkPlan workplan2) {
		double duration_setup = 0;
		@SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> it = workplan2.getAllConsistsOfAllocatedWorkingSteps();
		while(it.hasNext()) {
			float setup = it.next().getHasOperation().getSet_up_time();
			duration_setup += (double) setup;
		}
		
		return duration_setup;
	}
	//exclude operations from utilization
	public static double calculateUtilization(WorkPlan workplan2) {
		double working_time = 0;
		double total_time = 0;
		
		@SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> it = workplan2.getAllConsistsOfAllocatedWorkingSteps();
		while(it.hasNext()) {
			AllocatedWorkingStep allWS = it.next();
			
			Set<String> strings = new HashSet<String>();
			strings.add("buffer");
			//strings.add("transport");

			if (!strings.contains(allWS.getHasResource().getDetailed_Type().toLowerCase()) || !strings.contains(allWS.getHasOperation().getType().toLowerCase()))
			{
				working_time += allWS.getHasOperation().getAvg_Duration();
				total_time += allWS.getHasTimeslot().getLength()/(60*1000);
			}
			/*
			if(!allWS.getHasResource().getDetailed_Type().contentEquals("buffer")) {
				working_time += allWS.getHasOperation().getAvg_Duration();
				total_time += allWS.getHasTimeslot().getLength()/(60*1000);
			}*/
			else {
				total_time += allWS.getHasTimeslot().getLength()/(60*1000);
			}		
		}
		System.out.println("working time "+working_time+" total_time "+total_time);
		return working_time/total_time;
	}
	
	public static OperationCombination getBestCombinationByCriterion(String opimizationCriterion,
			ArrayList<OperationCombination> listOfCombinations) {
		switch (opimizationCriterion){
			case "timeOfFinish":
			
					Comparator<OperationCombination> comparator = Comparator.comparing(OperationCombination::getTimeOfFinish);
					Collections.sort(listOfCombinations, comparator);
			return listOfCombinations.get(0);
				
			case "":
				break;
		}
		return null;
	}

	public static <T> void addUnique(ArrayList<T> proposal_senders, T sender) {
		if (proposal_senders.contains(sender)) { // <- look for item!
			   // ... item already in list
			} else {
				proposal_senders.add(sender);
			}
		
	}

	public static <T> String printOutArrayList(ArrayList<T> elements) {
		String return_string = "";
		for(T element : elements) {
			return_string += element.toString()+" ";
		}
		return return_string;
	}
}
