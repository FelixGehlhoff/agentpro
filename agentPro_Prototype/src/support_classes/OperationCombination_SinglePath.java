package support_classes;

import java.util.ArrayList;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Proposal;
import agentPro.onto.Transport_Operation;
import agentPro.onto.WorkPlan;
import agentPro_Prototype_Agents.WorkpieceAgent;
import agentPro_Prototype_Agents._Agent_Template;

public class OperationCombination_SinglePath {
	
	private AllocatedWorkingStep startStep;
	private AllocatedWorkingStep transport_to_production;
	private AllocatedWorkingStep transport_to_buffer;
	private WorkPlan workplan = new WorkPlan();
	private Proposal proposal_buffer;
	private AllocatedWorkingStep buffer;
	private AllocatedWorkingStep nextProductionStep;

	private long timeOfFinish;
	private long timeOfStart;
	private long total_duration;
	private double durationSetup;
	private double costs = 0;
	private double utilization;
	private Proposal best_proposal_transport_to_buffer;
	private Proposal best_proposal_transport_to_production;
	private Proposal proposal_production;
	private ArrayList<Proposal>list_of_best_proposals = new ArrayList<Proposal>();

	public OperationCombination_SinglePath(AllocatedWorkingStep lastProductionStepAllocated, Proposal initial_proposal_production, Proposal proposal_buffer) {
		startStep = lastProductionStepAllocated;
		if(proposal_buffer != null) {
			this.proposal_buffer = proposal_buffer;
			this.buffer = (AllocatedWorkingStep)proposal_buffer.getConsistsOfAllocatedWorkingSteps().get(0);
		}	
		nextProductionStep = (AllocatedWorkingStep) initial_proposal_production.getConsistsOfAllocatedWorkingSteps().get(0);
		proposal_production = initial_proposal_production;
		System.out.println("DEBUG__operation comb single constructur "+initial_proposal_production.toString()+ " setup "+nextProductionStep.getHasOperation().getSet_up_time()+" all ws "+nextProductionStep.toString());
	}
	
	//workplan and values are calculated and the timeslot is replaced
	public void calculateWorkplan() {
		if(startStep != null) {
			workplan.addConsistsOfAllocatedWorkingSteps(startStep);	//neue Zeit muss noch übergeben werden an den Anbieter von startStep
				
		}
		if(buffer!= null) {
			if(transport_to_buffer != null) {
				workplan.addConsistsOfAllocatedWorkingSteps(transport_to_buffer);
				((AllocatedWorkingStep) best_proposal_transport_to_buffer.getConsistsOfAllocatedWorkingSteps().get(0)).setHasTimeslot(transport_to_buffer.getHasTimeslot());			
			}
			workplan.addConsistsOfAllocatedWorkingSteps(buffer);
			((AllocatedWorkingStep) proposal_buffer.getConsistsOfAllocatedWorkingSteps().get(0)).setHasTimeslot(buffer.getHasTimeslot());
		}
		if(transport_to_production != null) {
			workplan.addConsistsOfAllocatedWorkingSteps(transport_to_production);
			((AllocatedWorkingStep) best_proposal_transport_to_production.getConsistsOfAllocatedWorkingSteps().get(0)).setHasTimeslot(transport_to_production.getHasTimeslot());	
		}
		workplan.addConsistsOfAllocatedWorkingSteps(nextProductionStep);	
			((AllocatedWorkingStep) proposal_production.getConsistsOfAllocatedWorkingSteps().get(0)).setHasTimeslot(nextProductionStep.getHasTimeslot());
		_Agent_Template.printoutWorkPlan(workplan, "agent ");
		durationSetup = _Agent_Template.calculateDurationSetup(workplan);
		utilization = _Agent_Template.calculateUtilization(workplan);
		AllocatedWorkingStep lastStep = (AllocatedWorkingStep) workplan.getConsistsOfAllocatedWorkingSteps().get(workplan.getConsistsOfAllocatedWorkingSteps().size()-1);
		AllocatedWorkingStep firstStep = (AllocatedWorkingStep) workplan.getConsistsOfAllocatedWorkingSteps().get(0);
		
		timeOfFinish = (Long.parseLong(lastStep.getHasTimeslot().getEndDate()));
		timeOfStart = (Long.parseLong(firstStep.getHasTimeslot().getStartDate()));
		total_duration = timeOfFinish-timeOfStart;
		costs = calculateCosts();
		if(buffer!= null) {
			if(best_proposal_transport_to_buffer!= null) {
				list_of_best_proposals.add(best_proposal_transport_to_buffer);
			}		
			list_of_best_proposals.add(proposal_buffer);
		}
		if(transport_to_production != null) {
			list_of_best_proposals.add(best_proposal_transport_to_production);	
		}
		list_of_best_proposals.add(proposal_production);
	}
	public ArrayList<Proposal> getList_of_best_proposals() {
		return list_of_best_proposals;
	}

	private double calculateCosts() {
		costs = proposal_production.getPrice();
		if(transport_to_production != null) {
			costs += best_proposal_transport_to_production.getPrice();
		}
		if(buffer!= null) {
			if(best_proposal_transport_to_buffer != null) {
				costs += best_proposal_transport_to_buffer.getPrice();
			}	
			costs += proposal_buffer.getPrice();
		}
		
		return costs;
	}

	public void addTransportToBuffer(Proposal proposal_transport, String opimizationCriterion) {
		AllocatedWorkingStep allWS_of_proposal = (AllocatedWorkingStep) proposal_transport.getConsistsOfAllocatedWorkingSteps().get(0);
		if(transport_to_buffer != null) {
			switch(opimizationCriterion) {
			case "time_of_finish":
				if(Long.parseLong(transport_to_buffer.getHasTimeslot().getEndDate())>Long.parseLong(allWS_of_proposal.getHasTimeslot().getEndDate())) {
					setTransport_to_buffer(allWS_of_proposal); //adjusts the other steps too
					best_proposal_transport_to_buffer = proposal_transport;
				}else if(Long.parseLong(transport_to_buffer.getHasTimeslot().getEndDate())==Long.parseLong(allWS_of_proposal.getHasTimeslot().getEndDate())) {
					if(transport_to_buffer.getHasOperation().getSet_up_time()>allWS_of_proposal.getHasOperation().getSet_up_time()) {
						setTransport_to_buffer(allWS_of_proposal);
						best_proposal_transport_to_buffer = proposal_transport;
					}
				}
			}
		}else {
			setTransport_to_buffer(allWS_of_proposal);
			best_proposal_transport_to_buffer = proposal_transport;
		
		}	
	}
	//adds the step if it finishes earlier (current criterion) than the current step. If equals look at setup time
	public void addTransportToProduction(Proposal proposal_transport, String opimizationCriterion) {
		AllocatedWorkingStep allWS_of_proposal = (AllocatedWorkingStep) proposal_transport.getConsistsOfAllocatedWorkingSteps().get(0);
			if(transport_to_production != null) {
				switch(opimizationCriterion) {
				case "time_of_finish":		//take earliest, if equal consider setup
					if(Long.parseLong(transport_to_production.getHasTimeslot().getEndDate())>Long.parseLong(allWS_of_proposal.getHasTimeslot().getEndDate())) {
						setTransport_to_production(allWS_of_proposal); //adjusts the other steps too
						best_proposal_transport_to_production = proposal_transport;
					}else if(Long.parseLong(transport_to_production.getHasTimeslot().getEndDate())==Long.parseLong(allWS_of_proposal.getHasTimeslot().getEndDate())) {
						if(transport_to_production.getHasOperation().getSet_up_time()>allWS_of_proposal.getHasOperation().getSet_up_time()) {
							setTransport_to_production(allWS_of_proposal);
							best_proposal_transport_to_production = proposal_transport;
						}
					}
				case "duration_setup":		//take least setup, if equal earliest
					if(transport_to_production.getHasOperation().getSet_up_time()>allWS_of_proposal.getHasOperation().getSet_up_time()) {
						setTransport_to_production(allWS_of_proposal); //adjusts the other steps too
						best_proposal_transport_to_production = proposal_transport;
					}else if(transport_to_production.getHasOperation().getSet_up_time()==allWS_of_proposal.getHasOperation().getSet_up_time()) {
						if(Long.parseLong(transport_to_production.getHasTimeslot().getEndDate())>Long.parseLong(allWS_of_proposal.getHasTimeslot().getEndDate())) {
							setTransport_to_production(allWS_of_proposal);
							best_proposal_transport_to_production = proposal_transport;
						}
					}
				}
			}else {
				setTransport_to_production(allWS_of_proposal);
				best_proposal_transport_to_production = proposal_transport;
			
			}	
	}
	
	public AllocatedWorkingStep getTransport_to_production() {
		return transport_to_production;
	}
	//also adjusts the other steps
	public void setTransport_to_production(AllocatedWorkingStep transport_to_production) {
		this.transport_to_production = transport_to_production;
		if(buffer != null) {
			buffer.getHasTimeslot().setEndDate(transport_to_production.getHasTimeslot().getStartDate());	//TODO muss hier noch geprüft werden?
			buffer.getHasOperation().setAvg_PickupTime(transport_to_production.getHasOperation().getAvg_PickupTime());
		}else {
			if(Long.parseLong(transport_to_production.getHasTimeslot().getStartDate())-Long.parseLong(startStep.getHasTimeslot().getEndDate())>WorkpieceAgent.transport_estimation+_Agent_Template.bufferThreshold*60*1000){
				System.out.println("OPERATIONCOMBINATION_SINGLEPATH______________________ERROR_____________A BUFFER SHOULD BE ARRANGED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Waiting time now: 	"+(Long.parseLong(transport_to_production.getHasTimeslot().getStartDate())-Long.parseLong(startStep.getHasTimeslot().getEndDate())));
			}
			startStep.getHasTimeslot().setEndDate(transport_to_production.getHasTimeslot().getStartDate());
			startStep.getHasOperation().setAvg_PickupTime(transport_to_production.getHasOperation().getAvg_PickupTime());
		}
		long length = (long) nextProductionStep.getHasTimeslot().getLength();
		//check if the later arrival can be accounted for with the buffer after operation
		if(Long.parseLong(nextProductionStep.getHasTimeslot().getEndDate())+(long)nextProductionStep.getHasOperation().getBuffer_after_operation()>= Long.parseLong(transport_to_production.getHasTimeslot().getEndDate())+length) {
			nextProductionStep.getHasTimeslot().setStartDate(transport_to_production.getHasTimeslot().getEndDate());
			nextProductionStep.getHasTimeslot().setEndDate(Long.toString(Long.parseLong(nextProductionStep.getHasTimeslot().getStartDate())+length));
			nextProductionStep.getHasOperation().setAvg_PickupTime(transport_to_production.getHasOperation().getAvg_PickupTime());
		}else {
			System.out.println("ERROR_______________TRANSPORT IS TOO LATE FOR PRODUCTION STEP"); //TODO ERROR routine, e.g. restart procedure with earliest arrival?
		}
		
	}
	public AllocatedWorkingStep getTransport_to_buffer() {
		return transport_to_buffer;
	}

	public void setTransport_to_buffer(AllocatedWorkingStep transport_to_buffer) {
		this.transport_to_buffer = transport_to_buffer;
		buffer.getHasTimeslot().setStartDate(transport_to_buffer.getHasTimeslot().getEndDate());
		buffer.getHasOperation().setAvg_PickupTime(transport_to_buffer.getHasOperation().getAvg_PickupTime());
		startStep.getHasTimeslot().setEndDate(transport_to_buffer.getHasTimeslot().getStartDate());
		startStep.getHasOperation().setAvg_PickupTime(transport_to_buffer.getHasOperation().getAvg_PickupTime());
	}
	
	public long getTimeOfFinish() {
		return timeOfFinish;
	}

	public void setTimeOfFinish(long timeOfFinish) {
		this.timeOfFinish = timeOfFinish;
	}

	public long getTimeOfStart() {
		return timeOfStart;
	}

	public void setTimeOfStart(long timeOfStart) {
		this.timeOfStart = timeOfStart;
	}

	public long getTotal_duration() {
		return total_duration;
	}

	public void setTotal_duration(long total_duration) {
		this.total_duration = total_duration;
	}

	public double getDurationSetup() {
		return durationSetup;
	}

	public void setDurationSetup(double durationSetup) {
		this.durationSetup = durationSetup;
	}

	public double getCosts() {
		return costs;
	}

	public void setCosts(double costs) {
		this.costs = costs;
	}
	@Override
	public String toString() {
		String string = "Combination: From "+startStep.getHasOperation().getName()+" @ "+startStep.getHasResource().getHasLocation().toString();
		if(buffer != null) {
			if(transport_to_buffer != null) {
				string += " via "+transport_to_buffer.getHasOperation().getName();	
			}
			string += " to buffer "+buffer.getHasResource().getName()+" and";
		}
		if(transport_to_production != null) {
			string += " via "+transport_to_production.getHasOperation().getName();
		}
		string += " to "+nextProductionStep.getHasResource().getName();
		return string;
	}
	public WorkPlan getWorkplan() {
		return workplan;
	}
	public void setWorkplan(WorkPlan workplan) {
		this.workplan = workplan;
	}

	public double getUtilization() {
		return utilization;
	}
	public void setUtilization(double utilization) {
		this.utilization = utilization;
	}

	public Transport_Operation getFirstTransport() {
		if(transport_to_buffer != null) {
			return (Transport_Operation) transport_to_buffer.getHasOperation();
		}else if(transport_to_production != null){
			return (Transport_Operation) transport_to_production.getHasOperation();
		}else {
			return null;
		}
	}

	public AllocatedWorkingStep getFirstTransportAllWS() {
		if(buffer != null) {
			return transport_to_buffer;
		}else {
			return transport_to_production;
		}
	}
}
