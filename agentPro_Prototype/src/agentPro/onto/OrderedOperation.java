package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: OrderedOperation
* @author ontology bean generator
* @version 2020/06/26, 11:54:55
*/
public class OrderedOperation implements Concept {

   /**
* Protege name: hasFollowUpOperation
   */
   private boolean hasFollowUpOperation;
   public void setHasFollowUpOperation(boolean value) { 
    this.hasFollowUpOperation=value;
   }
   public boolean getHasFollowUpOperation() {
     return this.hasFollowUpOperation;
   }

   /**
* Protege name: sequence_Number
   */
   private int sequence_Number;
   public void setSequence_Number(int value) { 
    this.sequence_Number=value;
   }
   public int getSequence_Number() {
     return this.sequence_Number;
   }

   /**
* Protege name: hasProductionOperation
   */
   private Production_Operation hasProductionOperation;
   public void setHasProductionOperation(Production_Operation value) { 
    this.hasProductionOperation=value;
   }
   public Production_Operation getHasProductionOperation() {
     return this.hasProductionOperation;
   }

   /**
* Protege name: firstOperation
   */
   private boolean firstOperation;
   public void setFirstOperation(boolean value) { 
    this.firstOperation=value;
   }
   public boolean getFirstOperation() {
     return this.firstOperation;
   }

   /**
* Protege name: lastOperation
   */
   private boolean lastOperation;
   public void setLastOperation(boolean value) { 
    this.lastOperation=value;
   }
   public boolean getLastOperation() {
     return this.lastOperation;
   }

   /**
* Protege name: withOperationInStep
   */
   private int withOperationInStep;
   public void setWithOperationInStep(int value) { 
    this.withOperationInStep=value;
   }
   public int getWithOperationInStep() {
     return this.withOperationInStep;
   }

}
