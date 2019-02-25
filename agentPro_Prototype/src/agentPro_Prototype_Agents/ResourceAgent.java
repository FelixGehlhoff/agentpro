package agentPro_Prototype_Agents;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.CFP;
import agentPro.onto.Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Resource;
import agentPro.onto.Timeslot;
import agentPro.onto.WorkPlan;
import agentPro.onto._SendProposal;
import agentPro_Prototype_ResourceAgent.ReceiveCFPBehaviour;
import agentPro_Prototype_ResourceAgent.ReceiveCancellationBehaviour;
import agentPro_Prototype_ResourceAgent.ReceiveIntervalForConnectedResourceBehaviour;
import agentPro_Prototype_ResourceAgent.ReceiveRequestBufferBehaviour;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
//import agentPro_Prototype_ResourceAgent.ReceiveRejectProposalBehaviour;
import support_classes.Interval;

/* 
 * Models a resource.
 */

public abstract class ResourceAgent extends _Agent_Template{

	private static final long serialVersionUID = 1L;
	//private String production_capability;				//for testing only one capability
	//private int duration_of_process;
	private ReceiveCFPBehaviour ReceiveCFPBehav;
	private ReceiveRequestBufferBehaviour ReceiveRequestBufferBehaviour;
	private ReceiveCancellationBehaviour ReceiveCancellationBehav;
	//private ReceiveRejectProposalBehaviour ReceiveRejectProposalBehaviour;
	private int offerNumber = 1;
	//protected Object[] args;
	//protected Resource representedResource;
	private ArrayList <String> needed_shared_resources = new ArrayList <String>();
	//private WorkPlan workplan;
	private ArrayList <Interval> busy_interval_array = new ArrayList <Interval>();
	private ArrayList <Interval> free_interval_array = new ArrayList <Interval>();
	
	//private long time_until_end = 1000*60*60*24*10; //10 Tage 
	public long reply_by_time = 350; //ms KRAN_WS
	public long reply_by_time_shared_resources = 150;
	public int numberOfResourcesPossibleForCalculationOfSharedResourceProposal = 0;
	
	//Datenbankverbindung
		
		protected String nameOfResource_Definitions_Table = "Resource_Definitions";
		protected String columnNameOfResource_Name = "Resource_Name";
		protected String columnNameOfResource_Type = "Resource_Type";
		protected String columnNameOfResource_Detailed_Type = "Resource_Detailed_Type";
		protected String columnNameOfCapability = "Capability";
		protected String columnNameOfLocationX = "LocationX";
		protected String columnNameOfLocationY = "LocationY";
		protected String columnNameOfAvg_Transportation_Speed = "Avg_Transportation_Speed";
		protected String columnNameOfAvg_PickUp_Time = "Avg_PickUp_Time";
		
		protected String nameOfCapability_Operations_Mapping_Table = this.prefix_schema+".capability_operations_mapping";
		protected String columnNameOfID = "ID";
		protected String columnNameOfCapability_Name = "Capability_Name";
		protected String columnNameOfEnables_Operation = "Enables_Operation";
		protected String columnNameOfOperation_Number = "Operation_Number";
		protected String columnNameOfTimeConsumption = "TimeConsumption";
		
		public ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

	protected void setup (){
		super.setup();
		//representedResource = new Resource();
		setWorkplan(new WorkPlan());
		Interval free_starting_interval = new Interval();
		if(simulation_mode) {		
			/*	
			if(getLocalName().equals("gr_Bk_West")) {
					addBehaviour(new ReceiveIntervalForConnectedResourceBehaviour((ProductionResourceAgent)this));
				}else {*/
			
					free_starting_interval = new Interval (this.start_simulation-24*60*60*1000, this.start_simulation+time_until_end, false);
					free_interval_array.add(free_starting_interval);
				//}
		}else {
			free_starting_interval = new Interval (System.currentTimeMillis(), System.currentTimeMillis()+time_until_end, false);
			free_interval_array.add(free_starting_interval);
		}
		
		
		logLinePrefix = getLocalName();
		// / INITIALISATION
		// /////////////////////////////////////////////////////////	
		
		// / ADD BEHAVIOURS
        // /////////////////////////////////////////////////////////
		ReceiveCFPBehav = new ReceiveCFPBehaviour(this);
        addBehaviour(ReceiveCFPBehav);
        ReceiveRequestBufferBehaviour = new ReceiveRequestBufferBehaviour(this);
        addBehaviour(ReceiveRequestBufferBehaviour);
        ReceiveCancellationBehav = new ReceiveCancellationBehaviour(this);
        addBehaviour(ReceiveCancellationBehav);
       // ReceiveRejectProposalBehaviour = new ReceiveRejectProposalBehaviour(this);
        //addBehaviour(ReceiveRejectProposalBehaviour);
	}


	


	public int getOfferNumber() {
		return offerNumber;
	}

	public void setOfferNumber(int offerNumber) {
		this.offerNumber = offerNumber;
	}
	public abstract boolean feasibilityCheck(Operation operation);
	
	//public abstract Proposal checkScheduleDetermineTimeslotAndCreateProposal(long startdate_cfp, long enddate_cfp, Operation operation);
	public abstract Proposal checkScheduleDetermineTimeslotAndCreateProposal(CFP cfp);
	
	public Proposal createProposal(float price, Operation operation, Timeslot timeslot_for_proposal, AID sender) {
		Proposal proposal = new Proposal();
		int proposal_id = getOfferNumber();
		proposal.setID_Number(proposal_id);
		
		AllocatedWorkingStep proposed_slot = new AllocatedWorkingStep();
		
		proposed_slot.setHasOperation(operation);
		
		Resource thisResource = new Resource();

		thisResource.setName(getRepresentedResource().getName());  		//not all parameters are relevant
		thisResource.setHasLocation(getRepresentedResource().getHasLocation());
		thisResource.setType(getRepresentedResource().getType());
		thisResource.setID_Number(getRepresentedResource().getID_Number());
		proposed_slot.setHasResource(thisResource);
		
		proposal.setPrice(price);

		proposed_slot.setHasTimeslot(timeslot_for_proposal);
		proposed_slot.setID_String(sender.getLocalName()+"@"+getLocalName()+"."+proposal_id);
		//System.out.println("DEBUG_________Res Agent create Proposal ID String "+sender.getLocalName()+"@"+getLocalName()+"."+proposal_id);
		proposed_slot.setBuffer_before_operation(this.getReceiveCFPBehav().buffer_time_that_production_can_start_earlier);
		
		proposal.addConsistsOfAllocatedWorkingSteps(proposed_slot);
		
		return proposal;
	}

	public abstract Resource getRepresentedResource();

	public abstract void setRepresentedResource(Resource res);
	/*
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
*/
	public ArrayList <Interval> getBusyInterval_array() {
		return busy_interval_array;
	}

	public void setBusyInterval_array(ArrayList <Interval> interval_array) {
		this.busy_interval_array = interval_array;
	}
	
	public ArrayList <Interval> getFree_interval_array() {
		return free_interval_array;
	}

	public void setFree_interval_array(ArrayList <Interval> free_interval_array) {
		this.free_interval_array = free_interval_array;
	}
	//i for x times replybytime
	public void sendProposal(Proposal proposal_onto, String conversationID, String sender, Double i) {
		ACLMessage proposal = new ACLMessage(ACLMessage.PROPOSE);
			AID receiver = new AID();		
			receiver.setLocalName(sender);
		proposal.addReceiver(receiver);
		proposal.setConversationId(conversationID);
		proposal.setLanguage(getCodec().getName());
		proposal.setOntology(getOntology().getName());
		//proposal.setReplyWith(String.valueOf(proposal_onto.getID_Number()));
		proposal.setInReplyTo(String.valueOf(proposal_onto.getID_Number()));
		
		_SendProposal sendProposal = new _SendProposal();
		sendProposal.setHasProposal(proposal_onto);
		
		Action content = new Action(getAID(),sendProposal);
		
		try {
			getContentManager().fillContent(proposal, content);
		} catch (CodecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//determine reply by time
		long reply_by_date_long = 0;
		if(i != null) {
			reply_by_date_long = (long) (System.currentTimeMillis()+i*reply_by_time);
		}else {
			reply_by_date_long = System.currentTimeMillis()+reply_by_time;
		}
		
		Date reply_by_date = new Date(reply_by_date_long);
		proposal.setReplyByDate(reply_by_date);
		send(proposal);		
		printOutSent(proposal);
		//System.out.println("DEBUG_______proposal sent at______"+System.currentTimeMillis());
		//System.out.println(myAgent.SimpleDateFormat.format(new Date()) +" "+myAgent.getLocalName()+logLinePrefix+" PROPOSAL sent to receiver: "+receiver.getLocalName()+" with content: "+proposal.getContent());
		
	}
	
	public void sendProposal(Proposal proposal_onto, String conversationID, ArrayList<String> sender, Double i) {
		for(String sender_localName : sender) {
				sendProposal(proposal_onto, conversationID, sender_localName, i);			
			}
	}
	
	public void removeAllocatedWorkingStepFromWorkPlanAndBusyIntervalsAndCreateFreeIntervals(AllocatedWorkingStep allWS) {
    	//find allWS in Workplan
    	//int counter = 0;
		Operation op = null;
		
		//find correct allWS
		//delete it in workplan and busy interval array
		//add it in free interval array
		if(numberOfResourcesPossibleForCalculationOfSharedResourceProposal > 0) {
			printoutBusyIntervals();
		}
		
		
		 @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it_2 = getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();		 	
		    while(it_2.hasNext()) {
		    	AllocatedWorkingStep allocWorkingstep = it_2.next();
		    	op = allocWorkingstep.getHasOperation();	
		    	String name_Workpiece = allocWorkingstep.getHasOperation().getAppliedOn().getID_String();
		    	Interval busy_interval = new Interval (Long.parseLong(allocWorkingstep.getHasTimeslot().getStartDate()), Long.parseLong(allocWorkingstep.getHasTimeslot().getEndDate()), false);
		    	
		    	//if operaiton name and workpiece ID are correct --> delete
		    	if(op.getName().equals(allWS.getHasOperation().getName()) && name_Workpiece.equals(allWS.getHasOperation().getAppliedOn().getID_String())) {	//find out the position of the relevant step			    	
		    		 //myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().remove(counter);
		    		it_2.remove();		    		
		    		 //find the corresponding busy interval and delete it + add free interval
		    		 for(int i = 0;i < getBusyInterval_array().size(); i++) {
		    			
		    			 if(getBusyInterval_array().get(i).getId().equals(allocWorkingstep.getID_String())) {
		    				// System.out.println(getLocalName()+" DEBUG________busy interval removed "+SimpleDateFormat.format(getBusyInterval_array().get(i).lowerBound())+" "+SimpleDateFormat.format(getBusyInterval_array().get(i).upperBound()));
		    				getBusyInterval_array().remove(i);
		    			 	getFree_interval_array().add(busy_interval); 
		    			 }
		    			 
		    			 /*
		    			 if(getBusyInterval_array().get(i).lowerBound() == busy_interval.lowerBound() && getBusyInterval_array().get(i).upperBound() == busy_interval.upperBound()) {
		    				 System.out.println(getLocalName()+" DEBUG________busy interval removed "+SimpleDateFormat.format(getBusyInterval_array().get(i).lowerBound())+" "+SimpleDateFormat.format(getBusyInterval_array().get(i).upperBound()));
		    				getBusyInterval_array().remove(i);
		    			 	getFree_interval_array().add(busy_interval); //TBD sorting and merging!!
		    			 }*/
		    		 }
		    		 //sortWorkplanChronologically();
		    		 //sortFreeIntervalsChronologically();
		    		 sortArrayListIntervalsEarliestFirst(this.getFree_interval_array(), "start");
		    		 //merging has to be done at least twice --> TBD if there is a better way
		    		 //if(myAgent.getLocalName().equals("Kran1")) {
		    			 mergeAdjacentFreeIntervals();
			    		 mergeAdjacentFreeIntervals();	 
		    		 //}
	    		 				    		 
		    	}
		    	//counter++;
		    }
		    if(numberOfResourcesPossibleForCalculationOfSharedResourceProposal > 0) {
				printoutBusyIntervals();
			}
		
	}
	
	public void sortArrayListIntervalsEarliestFirst(ArrayList<Interval> arrayList, String startOrEnd) {
		if(startOrEnd.equals("start")) {
			Comparator<Interval> comparator = Comparator.comparing(Interval::lowerBound);
			Collections.sort(arrayList, comparator);
		}else if(startOrEnd.equals("end")) {
			Comparator<Interval> comparator = Comparator.comparing(Interval::upperBound);
			Collections.sort(arrayList, comparator);
		}	
	}
	
	
	private void mergeAdjacentFreeIntervals() {
			
		/*
		for(Interval i : myAgent.getFree_interval_array()) {
			System.out.println(myAgent.getLocalName()+" DEBUG________BEFORE MERGER lower bound "+myAgent.SimpleDateFormat.format(i.lowerBound())+ " upper bound "+myAgent.SimpleDateFormat.format(i.upperBound()));
		}*/
		
		ArrayList <Interval> new_merged_list = new ArrayList <Interval>();
		for(int i = 0;i <= getFree_interval_array().size()-1;i++) {	
			Interval free_interval_i_plus1 = null;
			Interval free_interval_i = getFree_interval_array().get(i);
			
			//if there exists an element behind i (so i+1), get that element
			if(i<getFree_interval_array().size()-1) {
				free_interval_i_plus1 = getFree_interval_array().get(i+1);
			}else{ // i = size -1 --> e.g. i = 4, array size = 5 --> last element --> add the last element to the new list
				new_merged_list.add(free_interval_i);
				break; //last element was added --> leave the for loop
			}
		
			//if the intervals share a common bound (lower of i and upper of i-1) --> create a new one with lower bound i-1 and upper bound i
			if(free_interval_i.upperBound() == free_interval_i_plus1.lowerBound()) {
				Interval new_free_intervall = new Interval(free_interval_i.lowerBound(), free_interval_i_plus1.upperBound(), false);
				//System.out.println(myAgent.getLocalName()+" DEBUG________MERGED__________"+logLinePrefix+" free_interval_i.lowerBound() "+myAgent.SimpleDateFormat.format(free_interval_i.lowerBound())+" free_interval_i_plus1.upperBound() "+myAgent.SimpleDateFormat.format(free_interval_i_plus1.upperBound()));
				//myAgent.getFree_interval_array().add(i-1, new_free_intervall);
				new_merged_list.add(new_free_intervall);
				i++; //nächstes überspringen, da bereits in dem merged interval enthalten

			//this should not be needed (overlap of more than a shared bound)
			}else if(free_interval_i.intersection(free_interval_i_plus1).getSize()>0){
				Interval new_free_intervall = new Interval(free_interval_i.lowerBound(), free_interval_i_plus1.upperBound(), false);
				new_merged_list.add(new_free_intervall);
			}
			//no overlap
			else{
				new_merged_list.add(getFree_interval_array().get(i));
			}
		}
		//set the new list as the free interval list
		setFree_interval_array(new_merged_list);
		/*
		for(Interval i : myAgent.getFree_interval_array()) {
			System.out.println(myAgent.getLocalName()+" DEBUG________AFTER MERGER lower bound "+myAgent.SimpleDateFormat.format(i.lowerBound())+ " upper bound "+myAgent.SimpleDateFormat.format(i.upperBound()));
		}*/
		
	}



	public void printoutBusyIntervals() {
		String printout = getLocalName()+" DEBUG____BUSY INTERVALS	";
		for(Interval i : getBusyInterval_array()) {
			printout = printout + " id "+i.getId()+" "+ SimpleDateFormat.format(i.lowerBound())+";"+SimpleDateFormat.format(i.upperBound());
		}
		 System.out.println(printout);
	}
	
	public void printoutFreeIntervals() {
		String printout = getLocalName()+" DEBUG____________FREE INTERVALS	";
		for(Interval i : getFree_interval_array()) {
			printout = printout + " "+ SimpleDateFormat.format(i.lowerBound())+";"+SimpleDateFormat.format(i.upperBound());
		}
		 System.out.println(printout);
	}

	public ArrayList <String> getNeeded_shared_resources() {
		return needed_shared_resources;
	}

	public void setNeeded_shared_resources(ArrayList <String> needed_shared_resources) {
		this.needed_shared_resources = needed_shared_resources;
	}

	public ReceiveCFPBehaviour getReceiveCFPBehav() {
		return ReceiveCFPBehav;
	}





	public void sendRefusal(ACLMessage msg) {
		ACLMessage refusal = new ACLMessage(ACLMessage.REFUSE);
		AID receiver = new AID();		
		receiver.setLocalName(msg.getSender().getLocalName());
		refusal.addReceiver(receiver);
		refusal.setConversationId(msg.getConversationId());
		refusal.setLanguage(getCodec().getName());
		refusal.setOntology(getOntology().getName());
	//proposal.setReplyWith(String.valueOf(proposal_onto.getID_Number()));
		//refusal.setInReplyTo(String.valueOf(proposal_onto.getID_Number()));
	
		/*
	_SendProposal sendProposal = new _SendProposal();
	sendProposal.setHasProposal(proposal_onto);
	
	Action content = new Action(getAID(),sendProposal);
	
	try {
		getContentManager().fillContent(proposal, content);
	} catch (CodecException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (OntologyException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	*/
	
	send(refusal);		
	printOutSent(refusal);
	//System.out.println("DEBUG REsource Agent refusal sent at______"+System.currentTimeMillis());
	//System.out.println(myAgent.SimpleDateFormat.format(
		
	}
	public void sendRefusal(String conversationID, ArrayList<String> sender) {
		ACLMessage refusal = new ACLMessage(ACLMessage.REFUSE);
		AID receiver = new AID();		
		receiver.setLocalName(sender.get(0));
		refusal.addReceiver(receiver);
		refusal.setConversationId(conversationID);
		refusal.setLanguage(getCodec().getName());
		refusal.setOntology(getOntology().getName());
	//proposal.setReplyWith(String.valueOf(proposal_onto.getID_Number()));
		//refusal.setInReplyTo(String.valueOf(proposal_onto.getID_Number()));

	
	send(refusal);		
	printOutSent(refusal);
		
	}
} 

