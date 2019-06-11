package support_classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Location;
import agentPro.onto.Operation;
import agentPro.onto.Production_Operation;
import agentPro.onto.Proposal;
import agentPro.onto.Timeslot;
import agentPro.onto.WorkPlan;
import agentPro_Prototype_Agents._Agent_Template;

public class OperationCombination {
	private ArrayList<Proposal> proposal_list_transport = new ArrayList<Proposal>();
	private Proposal initial_proposal_production;
	
	private AllocatedWorkingStep prod_allWS;
	private String identification_string;
	private ArrayList<Proposal> buffer_operations = new ArrayList<Proposal>();
	private Boolean buffer_needed;

	private AllocatedWorkingStep lastProductionStepAllocated;
	
	private ArrayList<OperationCombination_SinglePath> singlePaths = new ArrayList<OperationCombination_SinglePath>();
	private OperationCombination_SinglePath best_path;
	
	
	public OperationCombination (Proposal prop_production, AllocatedWorkingStep lastStepAllocated) {
		initial_proposal_production = prop_production;
		identification_string = prop_production.getID_String();
		prod_allWS = (AllocatedWorkingStep) prop_production.getConsistsOfAllocatedWorkingSteps().get(0);
		System.out.println("DEBUG____operation comb constructor "+prod_allWS.getID_String());
		//workplan.addConsistsOfAllocatedWorkingSteps(prod_allWS);
		setBuffer_needed(false);
		lastProductionStepAllocated = lastStepAllocated;
	}
	
	public void addProposal(Proposal proposal) {
		proposal_list_transport.add(proposal);
		//workplan.addConsistsOfAllocatedWorkingSteps((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0));
	}
	
	public void removeProposal(Proposal proposal) {
		Iterator<Proposal> it = proposal_list_transport.iterator();
		while(it.hasNext()) {
			if(it.next().getID_Number() == proposal.getID_Number() && it.next().getHasSender().getLocalName().contentEquals(proposal.getHasSender().getLocalName()) && it.next().getPrice() == proposal.getPrice()) {
				it.remove();
			}
		}
		/*
		@SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> ite = workplan.getAllConsistsOfAllocatedWorkingSteps();
		while(ite.hasNext()) {
			if(ite.next().getID_String().contentEquals(((AllocatedWorkingStep)proposal.getConsistsOfAllocatedWorkingSteps().get(0)).getID_String())) {
				ite.remove();
			}
		}*/
	}
	//determine the exact and best combination
	public void calculateValues() {
		if(buffer_needed) {
			for(Proposal proposal_buffer : buffer_operations) { //get Transports to each buffer
				OperationCombination_SinglePath comb = new OperationCombination_SinglePath(lastProductionStepAllocated, initial_proposal_production, proposal_buffer);			
				Location prod_loc = prod_allWS.getHasResource().getHasLocation();
				//add the best transport to production (earliest)
				for(Proposal proposal_transport : proposal_list_transport) {
					Location end_of_transport = (Location)((AllocatedWorkingStep)proposal_transport.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getEndState();
					if(_Agent_Template.doLocationsMatch(end_of_transport, prod_loc)){
						comb.addTransportToProduction(proposal_transport, _Agent_Template.opimizationCriterion);	//adds the transport if its better than the existing one
					}
				}
				if(comb.getTransport_to_production()!= null) {
					
				}
				Location buffer_loc =((AllocatedWorkingStep)proposal_buffer.getConsistsOfAllocatedWorkingSteps().get(0)).getHasResource().getHasLocation();
				//add the best transport to buffer (earliest)
				for(Proposal proposal_transport : proposal_list_transport) {
					Location end_of_transport = (Location)((AllocatedWorkingStep)proposal_transport.getConsistsOfAllocatedWorkingSteps().get(0)).getHasOperation().getEndState();
					if(_Agent_Template.doLocationsMatch(end_of_transport, buffer_loc)){
						comb.addTransportToBuffer(proposal_transport, _Agent_Template.opimizationCriterion);	//adds the transport if its better than the existing one
					}				
				}
				singlePaths.add(comb);	//here more than one is possible (because we can have multiple buffers
			}
			if(buffer_operations.size()>1) { //if there is more than one path
				System.out.println("DEBUG___operationCombination__buffer>1 "+singlePaths.get(0).toString());
				best_path = determineBestPath(singlePaths);
				best_path.calculateWorkplan();
			}else {
				best_path = singlePaths.get(0); //there should be only one element
				System.out.println("DEBUG___operationCombination__singepatchs "+singlePaths.get(0).toString());
				best_path.calculateWorkplan();
				if(singlePaths.size()>1) {
					System.out.println("DEBUG_____OperationCombination "+singlePaths.get(0).toString()+"  and   "+singlePaths.get(1).toString()+" total size is "+singlePaths.size());
				}				
			}
		}else{
			System.out.println("DEBUG__before constr");
			OperationCombination_SinglePath comb = new OperationCombination_SinglePath(lastProductionStepAllocated, initial_proposal_production, null);
			for(Proposal proposal_transport : proposal_list_transport) {
					comb.addTransportToProduction(proposal_transport, _Agent_Template.opimizationCriterion);	//adds the transport if its better than the existing one					
			}
			//singlePaths.add(comb);	//here there is only one combination possible (the best transport operation is used 
			best_path = comb; //there should be only one element
			best_path.calculateWorkplan(); //and sets values
		}
		//singlePaths contains all alternative paths (all = via Buffer 1 or Buffer 2 or with no buffer. For each the best transport operations are used		
		}
		
	public OperationCombination_SinglePath getBest_path() {
		return best_path;
	}

	private OperationCombination_SinglePath determineBestPath(ArrayList<OperationCombination_SinglePath> singlePaths2) {
		// Den Pfad nehmen, der am ehesten da ist & wenigste R�stzeit hat (abh. vom Criterion)		private OperationCombination sortAndDetermineBestCombinationByCriterion() {

			Comparator<OperationCombination_SinglePath> comparator = null;
			switch (_Agent_Template.opimizationCriterion) {
			case "time_of_finish":
				comparator = Comparator.comparing(OperationCombination_SinglePath::getTimeOfFinish)
				.thenComparing(OperationCombination_SinglePath::getCosts)
				.thenComparing(OperationCombination_SinglePath::getTotal_duration);		
				break;
			case "duration_setup":
				comparator = Comparator.comparing(OperationCombination_SinglePath::getCosts)
				.thenComparing(OperationCombination_SinglePath::getTimeOfFinish)
				.thenComparing(OperationCombination_SinglePath::getTotal_duration);	
				break;
			}
			Collections.sort(singlePaths2, comparator);	
			OperationCombination_SinglePath combination_best = singlePaths2.get(0);
			return combination_best;
	}

	public String getIdenticiation_string() {
		return identification_string;
	}

	public void setIdenticiation_string(String identiciation_string) {
		this.identification_string = identiciation_string;
	}

	public long getTimeOfFinish() {
		return best_path.getTimeOfFinish();
	}
	public long getTimeOfStart() {
		return best_path.getTimeOfStart();
	}



	public double getCosts() {
		return best_path.getCosts();
	}


	public double getDurationSetup() {
		return best_path.getDurationSetup();
	}

	public AllocatedWorkingStep getLastProductionStepAllocated() {
		return lastProductionStepAllocated;
	}

	public void setLastProductionStepAllocated(AllocatedWorkingStep lastProductionStepAllocated) {
		this.lastProductionStepAllocated = lastProductionStepAllocated;
	}

	public long getTotal_duration() {
		return best_path.getTotal_duration();
	}


	public double getUtilization() {
		return best_path.getUtilization();
	}


	public String toString() {
		String return_string = _Agent_Template.printoutWorkPlan(getWorkplan(), identification_string)+" start : "+_Agent_Template.SimpleDateFormat.format(getTimeOfStart())+" finish : "+_Agent_Template.SimpleDateFormat.format(getTimeOfFinish())+" total_duration= "+getTotal_duration()/(1000*60)+" setup= "+getDurationSetup()+" costs= "+getCosts()+" utilization= "+getUtilization();
		return return_string;
		
	}
	public ArrayList<Proposal>getProposals() {
		ArrayList<Proposal> new_list = proposal_list_transport;
		new_list.add(initial_proposal_production);
		return new_list;
	}

	public Boolean getBuffer_needed() {
		return buffer_needed;
	}

	public void setBuffer_needed(Boolean buffer_needed) {
		this.buffer_needed = buffer_needed;
	}
	public Proposal getInitial_proposal_production() {
		return initial_proposal_production;
	}

	public void setInitial_proposal_production(Proposal initial_proposal_production) {
		this.initial_proposal_production = initial_proposal_production;
	}

	public ArrayList<Proposal> getBuffer_operations() {
		return buffer_operations;
	}

	public void setBuffer_operations(ArrayList<Proposal> buffer_operations) {
		this.buffer_operations = buffer_operations;
	}
	public WorkPlan getWorkplan() {
		return best_path.getWorkplan();
	}
	
}
