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
* @version 2019/01/15, 10:47:24
*/
public class AllocatedWorkingStep implements Concept {

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
* Protege name: buffer_before_operation
   */
   private float buffer_before_operation;
   public void setBuffer_before_operation(float value) { 
    this.buffer_before_operation=value;
   }
   public float getBuffer_before_operation() {
     return this.buffer_before_operation;
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

   /**
* Protege name: buffer_after_operation
   */
   private float buffer_after_operation;
   public void setBuffer_after_operation(float value) { 
    this.buffer_after_operation=value;
   }
   public float getBuffer_after_operation() {
     return this.buffer_after_operation;
   }

}
