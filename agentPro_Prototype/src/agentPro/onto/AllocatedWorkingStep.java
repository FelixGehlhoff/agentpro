package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * <p style="margin-top: 0">
     isErrorStep: Currently used to signal that a step that is booked into the agent's 
      schedule is not part of the regular production plan (e.g. a buffer 
      place).
    </p>
* Protege name: AllocatedWorkingStep
* @author ontology bean generator
* @version 2022/02/3, 18:30:16
*/
public class AllocatedWorkingStep implements Concept {

//////////////////////////// User code
@Override     public String toString() {return " allocated working step "+iD_String+" "+hasOperation.toString()+" with timeslot "+hasTimeslot.toString();}
   /**
* Protege name: ID_String
   */
   private String iD_String;
   public void setID_String(String value) { 
    this.iD_String=value;
   }
   public String getID_String() {
     return this.iD_String;
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
* Protege name: isErrorStep
   */
   private boolean isErrorStep;
   public void setIsErrorStep(boolean value) { 
    this.isErrorStep=value;
   }
   public boolean getIsErrorStep() {
     return this.isErrorStep;
   }

   /**
* Protege name: hasResource
   */
   private Resource hasResource;
   public void setHasResource(Resource value) { 
    this.hasResource=value;
   }
   public Resource getHasResource() {
     return this.hasResource;
   }

   /**
* Protege name: isFinished
   */
   private boolean isFinished;
   public void setIsFinished(boolean value) { 
    this.isFinished=value;
   }
   public boolean getIsFinished() {
     return this.isFinished;
   }

   /**
* Protege name: startdate
   */
   private float startdate;
   public void setStartdate(float value) { 
    this.startdate=value;
   }
   public float getStartdate() {
     return this.startdate;
   }

   /**
* Protege name: isStarted
   */
   private boolean isStarted;
   public void setIsStarted(boolean value) { 
    this.isStarted=value;
   }
   public boolean getIsStarted() {
     return this.isStarted;
   }

   /**
* Protege name: quantity
   */
   private int quantity;
   public void setQuantity(int value) { 
    this.quantity=value;
   }
   public int getQuantity() {
     return this.quantity;
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
* Protege name: enddate
   */
   private float enddate;
   public void setEnddate(float value) { 
    this.enddate=value;
   }
   public float getEnddate() {
     return this.enddate;
   }

}
