package agentPro_Prototype_Agents;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Vector;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jade.tools.sniffer.*;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.core.event.ContainerAdapter;
import jade.core.event.ContainerEvent;
import jade.core.event.ContainerListener;
import jade.core.event.NotificationHelper;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.introspection.IntrospectionOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.tools.ToolAgent;
import jade.tools.sniffer.MessageList;
import jade.tools.sniffer.Sniffer;
import jade.util.Logger;

import jade.content.AgentAction;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Done;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SenderBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAException;
import jade.domain.FIPAService;
import jade.domain.JADEAgentManagement.SniffOff;
import jade.domain.JADEAgentManagement.SniffOn;
import jade.domain.introspection.AMSSubscriber;
import jade.domain.introspection.AddedContainer;
import jade.domain.introspection.BornAgent;
import jade.domain.introspection.DeadAgent;
import jade.domain.introspection.Event;
import jade.domain.introspection.EventRecord;
import jade.domain.introspection.MovedAgent;
import jade.domain.introspection.Occurred;
import jade.domain.introspection.PostedMessage;
import jade.domain.introspection.RemovedContainer;
import jade.domain.introspection.ResetEvents;
import jade.domain.introspection.SentMessage;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.StringACLCodec;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SimpleAchieveREResponder;
import jade.tools.ToolAgent;
import jade.util.ExtendedProperties;
import jade.util.Logger;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


public class CommunicationAnalyzerAgent2 extends Sniffer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RequestPerformer perf = null;


	
@Override
public void sniffMsg(List agents, boolean onFlag) {
    ACLMessage request = getSniffMsg(agents, onFlag);
    if (request != null)
      addBehaviour((Behaviour)new AMSClientBehaviour(onFlag ? "SniffAgentOn" : "SniffAgentOff", request)); 
    	if(perf == null){
    		perf = new RequestPerformer();
    		this.addBehaviour(perf);
    	}
}
private class AMSClientBehaviour extends SimpleAchieveREInitiator {
    private String actionName;
    
    public AMSClientBehaviour(String an, ACLMessage request) {
      super((Agent)CommunicationAnalyzerAgent2.this, request);
      this.actionName = an;
    }
    
    protected void handleNotUnderstood(ACLMessage reply) {
    	//CommunicationAnalyzerAgent2.this.myGUI.showError("NOT-UNDERSTOOD received during " + this.actionName);
    }
    
    protected void handleRefuse(ACLMessage reply) {
    	//CommunicationAnalyzerAgent2.this.myGUI.showError("REFUSE received during " + this.actionName);
    }
    
    protected void handleAgree(ACLMessage reply) {
      if (CommunicationAnalyzerAgent2.this.logger.isLoggable(Logger.FINE))
    	  CommunicationAnalyzerAgent2.this.logger.log(Logger.FINE, "AGREE received"); 
    }
    
    protected void handleFailure(ACLMessage reply) {
    	//CommunicationAnalyzerAgent2.this.myGUI.showError("FAILURE received during " + this.actionName);
    }
    
    protected void handleInform(ACLMessage reply) {
      if (CommunicationAnalyzerAgent2.this.logger.isLoggable(Logger.FINE))
    	  CommunicationAnalyzerAgent2.this.logger.log(Logger.FINE, "INFORM received"); 
    }
  }

	public void receive_request() {
		

	}
	
	private class RequestPerformer extends CyclicBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			MessageTemplate mt2 = MessageTemplate.MatchConversationId("Communication");
			MessageTemplate mt_total = MessageTemplate.and(mt1, mt2);
			ACLMessage msg = myAgent.receive(mt_total);

			if (msg != null) {
				receive_request();
			}else {
				block();
			}
			
		}



	}
}
