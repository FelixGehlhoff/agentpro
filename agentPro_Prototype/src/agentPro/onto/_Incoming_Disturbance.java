package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: _Incoming_Disturbance
* @author ontology bean generator
* @version 2019/02/25, 12:12:45
*/
public class _Incoming_Disturbance implements AgentAction {

   /**
* Protege name: hasDisturbance
   */
   private Disturbance hasDisturbance;
   public void setHasDisturbance(Disturbance value) { 
    this.hasDisturbance=value;
   }
   public Disturbance getHasDisturbance() {
     return this.hasDisturbance;
   }

}
