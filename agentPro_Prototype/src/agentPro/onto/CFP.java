package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: CFP
* @author ontology bean generator
* @version 2019/01/15, 10:47:24
*/
public class CFP extends Message{ 

   /**
* Protege name: hasOperation
   */
   private Operation hasOperation;
   public void setHasOperation(Operation value) { 
    this.hasOperation=value;
   }
   public Operation getHasOperation() {
     return this.hasOperation;
   }

   /**
* Protege name: hasTimeslot
   */
   private Timeslot hasTimeslot;
   public void setHasTimeslot(Timeslot value) { 
    this.hasTimeslot=value;
   }
   public Timeslot getHasTimeslot() {
     return this.hasTimeslot;
   }

}
