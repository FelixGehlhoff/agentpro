package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: _Incoming_Order
* @author ontology bean generator
* @version 2020/06/26, 11:54:55
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
