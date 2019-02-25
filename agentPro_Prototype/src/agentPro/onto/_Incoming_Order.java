package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: _Incoming_Order
* @author ontology bean generator
* @version 2019/02/25, 12:12:45
*/
public class _Incoming_Order implements AgentAction {

   /**
* Protege name: hasOrder
   */
   private Order hasOrder;
   public void setHasOrder(Order value) { 
    this.hasOrder=value;
   }
   public Order getHasOrder() {
     return this.hasOrder;
   }

}
