package agentPro_Prototype_Agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AgentPro_ProductionOntology;
import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Cancellation;
import agentPro.onto.Resource;
import agentPro.onto.WorkPlan;
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

/*
 * Serves as a template for other agents and provides take down procedure and some other variables
 */

public abstract class _Agent_Template extends Agent{
	
	private static final long serialVersionUID = 1L;
	private String DateFormat = "yyyy-MM-dd HH:mm:ss";
	public SimpleDateFormat SimpleDateFormat = new SimpleDateFormat(DateFormat);
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
	public String dbaddress_sim = "jdbc:mysql://localhost:3306/MySQL?"+"user=root&password=SQL_0518&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false";	//Address od database;
	
	public String columnNameOfOperation = "Operation"; //Operation
	public String columnNameOfResource = "Resource";
	public String columnNameOfResource_ID = "Resource_ID";
	public String columnNameOfSetupTime = "SetupTime";
	public String columnNameOfPlanStart = "PlanStart";
	public String columnNameOfPlanEnd = "PlanEnde";
	public String columnNameOfIstStart = "IstStart";
	public String columnNameOfIstEnd = "IstEnde";
	public String columnNameOfDueDate = "dueDate";
	public String columnNameOfReleaseDate = "releaseDate";
	public String columnNameOfPriority = "priority";
	public String columnNameOfNumber = "number";
	public String columnNameOfProduct = "product";
	public String columnNameOfTargetWarehouse = "targetWarehouse";
	public String columnNameAuftrags_ID = "Auftrags_ID"; //Auftrags_ID
	public String columnNameOperation_Type = "Operation_Type";
	public String columnNameOfStarted = "Started";
	
	public String nameOfMES_Data_Resource = prefix_schema+".total_operations";
	public String nameOfMES_Data = prefix_schema+".productionplan";
	public String nameOfOrderbook = prefix_schema+".orderbook2";
	//private String columnNameFinished = "Finished";
	//private String columnNameOfIstStart = "IstStart";
	//private String columnNameOfIstEnde = "IstEnde";
	
	public static Boolean simulation_mode = false;
	//public static String prefix_schema = "flexsimdata";
	public static String prefix_schema = "agentpro";
	
	public int duration_repair_workpiece = 20;
	public int duration_light_disturbance = 2;
	public int duration_severe_disturbance = 8;
	public long start_simulation = 1533074400000L; //01.08.2018 00:00
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
	public String tableNameResource = prefix_schema+".resources";
	public String tableNameProductionPlan = prefix_schema+".productionplan";
	public String columnNameColumnInProductionPlan = "columninproductionplan";
	public String columnNameLocationX = "locationX";
	public String columnNameLocationY = "locationY";
	public String columNameColumnNameInProductionPlan = "columnnameinproductionplan";
	public String columnNameorderid = "orderid";
	
	
	
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
	public void setResourceAgents(ArrayList<DFAgentDescription> resourceAgents) {
		this.resourceAgents = resourceAgents;
	}
	public WorkPlan getWorkplan() {
		return workplan;
	}

	public void setWorkplan(WorkPlan workplan) {
		this.workplan = workplan;
	}
	
	public void sortWorkplanChronologically() {

		WorkPlan wP_toBeSorted = new WorkPlan();
		
	    @SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> it = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
	   
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
	    setWorkplan(wP_toBeSorted);
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
	public void printoutWorkPlan() {
		String printout = getLocalName()+" DEBUG____WORKPLAN	";
		@SuppressWarnings("unchecked")
		
		Iterator<AllocatedWorkingStep> it_2 = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();		 	
	    while(it_2.hasNext()) {
	    	AllocatedWorkingStep a = it_2.next();
	    	printout = printout +" "+a.getID_String()+" operation "+a.getHasOperation().getName()+" Slot "+ SimpleDateFormat.format(Long.parseLong(a.getHasTimeslot().getStartDate()))+";"+SimpleDateFormat.format(Long.parseLong(a.getHasTimeslot().getEndDate()));
	    }
	    System.out.println(printout);
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
}
