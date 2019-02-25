package agentPro_Prototype_Agents;
import java.util.ArrayList;
import jade.Boot;

/*
 * 	starts the system with production, transport, interface and dummy agent
 */

public class Run_System {

	 public static void main(String[] args){
		
	        ArrayList<String> paramList = new ArrayList<String>();
	        
	        paramList.add("-gui");
	        
	        //starts persistent delivery filter with own filter class	 
	        //paramList.add("–mtps");
	        //paramList.add("jade.mtp.http.MessageTransportProtocol(http://192.168.178.41:1234)");
	        paramList.add("-persistent-delivery-filter");
	        paramList.add("support_classes.MyFilter");
	        paramList.add("-services");
	      
	        paramList.add("jade.core.messaging.PersistentDeliveryService;jade.core.event.NotificationService");
	        
	        //2 Krane, mit Störung und Pufferplatz
	        //paramList.add("QS1:agentPro_Prototype_Agents.ProductionResourceAgent();Pufferplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();Kran2:agentPro_Prototype_Agents.TransportResourceAgent();Kran1:agentPro_Prototype_Agents.TransportResourceAgent();Fräsmaschine1:agentPro_Prototype_Agents.ProductionResourceAgent();Montageplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();Lackierkabine1:agentPro_Prototype_Agents.ProductionResourceAgent();InterfaceAgent:agentPro_Prototype_Agents.InterfaceAgent;Dummy:agentPro_Prototype_Agents._DUMMY;MQTT_DUMMY:agentPro_Prototype_Agents._MQTT_DUMMY");	//for testing only short time
	        //2 Krane ohne Störung
	        //paramList.add("QS1:agentPro_Prototype_Agents.ProductionResourceAgent();Kran1:agentPro_Prototype_Agents.TransportResourceAgent();Kran2:agentPro_Prototype_Agents.TransportResourceAgent();Fräsmaschine1:agentPro_Prototype_Agents.ProductionResourceAgent();Montageplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();Lackierkabine1:agentPro_Prototype_Agents.ProductionResourceAgent();InterfaceAgent:agentPro_Prototype_Agents.InterfaceAgent;Dummy:agentPro_Prototype_Agents._DUMMY");	//for testing only short time
	    	//1 Kran ohne Störung ohne QS
	        //paramList.add("Kran1:agentPro_Prototype_Agents.TransportResourceAgent();Fräsmaschine1:agentPro_Prototype_Agents.ProductionResourceAgent();Montageplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();Lackierkabine1:agentPro_Prototype_Agents.ProductionResourceAgent();InterfaceAgent:agentPro_Prototype_Agents.InterfaceAgent;Dummy:agentPro_Prototype_Agents._DUMMY");	//for testing only short time
	        //2 Kran ohne Störung ohne QS mit shared REsources Kranschiene:agentPro_Prototype_Agents.SharedResourceAgent();
	        //paramList.add("DatabaseMonitorAgent:agentPro_Prototype_Agents.DatabaseMonitorAgent();DatabaseConnectorAgent:agentPro_Prototype_Agents.DatabaseConnectorAgent();Kranschiene:agentPro_Prototype_Agents.SharedResourceAgent();Operator_1:agentPro_Prototype_Agents.SharedResourceAgent();Kran1:agentPro_Prototype_Agents.TransportResourceAgent();Kran2:agentPro_Prototype_Agents.TransportResourceAgent();Fräsmaschine1:agentPro_Prototype_Agents.ProductionResourceAgent();Montageplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();Lackierkabine1:agentPro_Prototype_Agents.ProductionResourceAgent();InterfaceAgent:agentPro_Prototype_Agents.InterfaceAgent;Dummy:agentPro_Prototype_Agents._DUMMY");	//for testing only short time
	        	//MQTT_DUMMY:agentPro_Prototype_Agents._MQTT_DUMMY;
	        //paramList.add("MQTT_DUMMY:agentPro_Prototype_Agents._MQTT_DUMMY;QS1:agentPro_Prototype_Agents.ProductionResourceAgent();Fräsmaschine1:agentPro_Prototype_Agents.ProductionResourceAgent();Pufferplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();DatabaseMonitorAgent:agentPro_Prototype_Agents.DatabaseMonitorAgent();DatabaseConnectorAgent:agentPro_Prototype_Agents.DatabaseConnectorAgent();Kranschiene:agentPro_Prototype_Agents.Crane_RailAgent();Operator_1:agentPro_Prototype_Agents.OperatorAgent();Kran1:agentPro_Prototype_Agents.TransportResourceAgent();Kran2:agentPro_Prototype_Agents.TransportResourceAgent();Kran3:agentPro_Prototype_Agents.TransportResourceAgent();Montageplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();Lackierkabine1:agentPro_Prototype_Agents.ProductionResourceAgent();InterfaceAgent:agentPro_Prototype_Agents.InterfaceAgent;Dummy:agentPro_Prototype_Agents._DUMMY");	//for testing only short time	        
	        
	        String paramlist = "DatabaseConnectorAgent:agentPro_Prototype_Agents.DatabaseConnectorAgent();Simulation_Infrastrucutre:agentPro_Prototype_Agents._Simulation_Order_Generator;InterfaceAgent:agentPro_Prototype_Agents.InterfaceAgent()";
	        //SIMULATION
	        if(_Agent_Template.simulation_mode) {
	        	paramlist += ";DatabaseMonitorAgent:agentPro_Prototype_Agents.DatabaseMonitorAgent()";
	        }
	        paramList.add(paramlist);	     
	        //paramList.add("DatabaseMonitorAgent:agentPro_Prototype_Agents.DatabaseMonitorAgent()");	     
	        
	        //Dummy:agentPro_Prototype_Agents._DUMMY;
	        
	        //3 Kräne 2 Operator
	       // paramList.add("QS1:agentPro_Prototype_Agents.ProductionResourceAgent();Fräsmaschine1:agentPro_Prototype_Agents.ProductionResourceAgent();Pufferplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();DatabaseMonitorAgent:agentPro_Prototype_Agents.DatabaseMonitorAgent();DatabaseConnectorAgent:agentPro_Prototype_Agents.DatabaseConnectorAgent();Kranschiene:agentPro_Prototype_Agents.Crane_RailAgent();Operator_1:agentPro_Prototype_Agents.OperatorAgent();Operator_2:agentPro_Prototype_Agents.OperatorAgent();Kran1:agentPro_Prototype_Agents.TransportResourceAgent();Kran2:agentPro_Prototype_Agents.TransportResourceAgent();Kran3:agentPro_Prototype_Agents.TransportResourceAgent();Montageplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();Montageplatz2:agentPro_Prototype_Agents.ProductionResourceAgent();Lackierkabine1:agentPro_Prototype_Agents.ProductionResourceAgent();InterfaceAgent:agentPro_Prototype_Agents.InterfaceAgent;Dummy:agentPro_Prototype_Agents._DUMMY");	//for testing only short time	        
	        
	        
	        //paramList.add("DatabaseMonitorAgent:agentPro_Prototype_Agents.DatabaseMonitorAgent();DatabaseConnectorAgent:agentPro_Prototype_Agents.DatabaseConnectorAgent();Kranschiene:agentPro_Prototype_Agents.SharedResourceAgent();Operator_1:agentPro_Prototype_Agents.SharedResourceAgent();Kran1:agentPro_Prototype_Agents.TransportResourceAgent();Fräsmaschine1:agentPro_Prototype_Agents.ProductionResourceAgent();Montageplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();Lackierkabine1:agentPro_Prototype_Agents.ProductionResourceAgent();InterfaceAgent:agentPro_Prototype_Agents.InterfaceAgent;Dummy:agentPro_Prototype_Agents._DUMMY");	//for testing only short time
	        //2 Krane ohne Störung ohne QS mit shared REsources  ranschiene:agentPro_Prototype_Agents.SharedResourceAgent()
	        //paramList.add("Kranschiene:agentPro_Prototype_Agents.SharedResourceAgent();Operator_1:agentPro_Prototype_Agents.SharedResourceAgent();Kran1:agentPro_Prototype_Agents.TransportResourceAgent();Kran2:agentPro_Prototype_Agents.TransportResourceAgent();Fräsmaschine1:agentPro_Prototype_Agents.ProductionResourceAgent();Montageplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();Lackierkabine1:agentPro_Prototype_Agents.ProductionResourceAgent();InterfaceAgent:agentPro_Prototype_Agents.InterfaceAgent;Dummy:agentPro_Prototype_Agents._DUMMY");	//for testing only short time
	      
	        //paramList.add("Pufferplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();Kran1:agentPro_Prototype_Agents.TransportResourceAgent();Fräsmaschine1:agentPro_Prototype_Agents.ProductionResourceAgent();Montageplatz1:agentPro_Prototype_Agents.ProductionResourceAgent();Lackierkabine1:agentPro_Prototype_Agents.ProductionResourceAgent();InterfaceAgent:agentPro_Prototype_Agents.InterfaceAgent;Dummy:agentPro_Prototype_Agents._DUMMY;MQTT_DUMMY:agentPro_Prototype_Agents._MQTT_DUMMY");	//for testing only short time
	        //MQTT_DUMMY:agentPro_Prototype_Agents._MQTT_DUMMY
	        String [] param = paramList.toArray(new String[paramList.size()]);
	        Boot.main( param );
	        
	        
	    }
	}


