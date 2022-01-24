package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: _Incoming_Disturbance
* @author ontology bean generator
* @version 2020/06/26, 11:54:55
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
