package agentPro_Prototype_ResourceAgent;

import java.util.Date;
import java.util.Iterator;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Request_DatabaseEntry;
import agentPro.onto._SendRequest_DatabaseEntry;
import agentPro_Prototype_Agents._Agent_Template;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class RequestDatabaseEntryBehaviour extends OneShotBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private _Agent_Template myAgent;
	
	public RequestDatabaseEntryBehaviour(_Agent_Template myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
	
	}

	@Override
	public void action() {
		//find agents with capability X
		String service_type = "database_entry";
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(service_type); 	
		template.addServices(sd);
		DFAgentDescription[] result = null;
		
		//check the agents we already have stored
		if(myAgent.resourceAgents.size() > 0) {
			//DFAgentDescription[] result = 
			for(DFAgentDescription a : myAgent.resourceAgents) {
				
				  @SuppressWarnings("unchecked")
					Iterator<ServiceDescription> it = a.getAllServices();
				    while(it.hasNext()) {
				    	ServiceDescription service_description = it.next();
				    	if(service_description.getType().equals(service_type)) {
				    		result = new DFAgentDescription[1];
				    		result[0] = a;
				    		break;
				    	}
				    }
			}
		}
		//if there is no agent with ne needed capabiltiy stored, search the DF
		if(result  == null) {	//TBD subscription!!
		
		try {
			result = DFService.search(myAgent, template);	
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		if(result.length>0) {
			//create ACL Message				
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.setLanguage(myAgent.getCodec().getName());
			request.setOntology(myAgent.getOntology().getName());
			
		//create ontology contents
			_SendRequest_DatabaseEntry send_request_db_entry = new _SendRequest_DatabaseEntry();
			Request_DatabaseEntry request_db_entry = new Request_DatabaseEntry();
			@SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = myAgent.getWorkplan().getConsistsOfAllocatedWorkingSteps().iterator();
			    while(it.hasNext()) {
			    	AllocatedWorkingStep alloc_WS = it.next();
			    	//if(myAgent.simulation_mode) {		//in simulation mode only production steps --> 03.12.2018: Send full plan
			    		//if(alloc_WS.getHasOperation().getType().equals("production")) {
			    			//request_db_entry.addConsistsOfAllocatedWorkingSteps(alloc_WS);
			    		//}   		
			    	//}else {
			    		request_db_entry.addConsistsOfAllocatedWorkingSteps(alloc_WS);
			    	//}
			    }
			send_request_db_entry.setHasRequest_DatabaseEntry(request_db_entry);			
			Action content = new Action(myAgent.getAID(),send_request_db_entry);
					
			request.addReceiver(result[0].getName());
			
		
			//ontology --> fill content
			try {
				myAgent.getContentManager().fillContent(request, content);
			} catch (CodecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OntologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myAgent.send(request);
			System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+myAgent.logLinePrefix+" request database entry sent to receiver "+result[0].getName()+" with content "+request.getContent());
		
		}else {
			System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+myAgent.logLinePrefix+" error. No database agent found.");
		}
		
	}

}
