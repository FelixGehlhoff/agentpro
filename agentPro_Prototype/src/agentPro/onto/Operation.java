package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Operation
* @author ontology bean generator
* @version 2020/06/26, 11:54:55
*/
public class Operation implements Concept {

//////////////////////////// User code
@Override     public String toString() {return " operation  "+name+" set up "+set_up_time;}
   /**
* Protege name: set_up_time
   */
   private float set_up_time;
   public void setSet_up_time(float value) { 
    this.set_up_time=value;
   }
   public float getSet_up_time() {
     return this.set_up_time;
   }

   /**
* Protege name: avg_PickupTime
   */
   private int avg_PickupTime;
   public void setAvg_PickupTime(int value) { 
    this.avg_PickupTime=value;
   }
   public int getAvg_PickupTime() {
     return this.avg_PickupTime;
   }

   /**
* Protege name: name
   */
   private String name;
   public void setName(String value) { 
    this.name=value;
   }
   public String getName() {
     return this.name;
   }

   /**
* Protege name: isEnabledBy
   */
   private List isEnabledBy = new ArrayList();
   public void addIsEnabledBy(Capability elem) { 
     List oldList = this.isEnabledBy;
     isEnabledBy.add(elem);
   }
   public boolean removeIsEnabledBy(Capability elem) {
     List oldList = this.isEnabledBy;
     boolean result = isEnabledBy.remove(elem);
     return result;
   }
   public void clearAllIsEnabledBy() {
     List oldList = this.isEnabledBy;
     isEnabledBy.clear();
   }
   public Iterator getAllIsEnabledBy() {return isEnabledBy.iterator(); }
   public List getIsEnabledBy() {return isEnabledBy; }
   public void setIsEnabledBy(List l) {isEnabledBy = l; }

   /**
* Protege name: startStateNeeded
   */
   private State startStateNeeded;
   public void setStartStateNeeded(State value) { 
    this.startStateNeeded=value;
   }
   public State getStartStateNeeded() {
     return this.startStateNeeded;
   }

   /**
* Protege name: buffer_after_operation_start
   */
   private float buffer_after_operation_start;
   public void setBuffer_after_operation_start(float value) { 
    this.buffer_after_operation_start=value;
   }
   public float getBuffer_after_operation_start() {
     return this.buffer_after_operation_start;
   }

   /**
* Protege name: type
   */
   private String type;
   public void setType(String value) { 
    this.type=value;
   }
   public String getType() {
     return this.type;
   }

   /**
* Protege name: requiresOperation
   */
   private List requiresOperation = new ArrayList();
   public void addRequiresOperation(Operation elem) { 
     List oldList = this.requiresOperation;
     requiresOperation.add(elem);
   }
   public boolean removeRequiresOperation(Operation elem) {
     List oldList = this.requiresOperation;
     boolean result = requiresOperation.remove(elem);
     return result;
   }
   public void clearAllRequiresOperation() {
     List oldList = this.requiresOperation;
     requiresOperation.clear();
   }
   public Iterator getAllRequiresOperation() {return requiresOperation.iterator(); }
   public List getRequiresOperation() {return requiresOperation; }
   public void setRequiresOperation(List l) {requiresOperation = l; }

   /**
* Protege name: buffer_before_operation_end
   */
   private float buffer_before_operation_end;
   public void setBuffer_before_operation_end(float value) { 
    this.buffer_before_operation_end=value;
   }
   public float getBuffer_before_operation_end() {
     return this.buffer_before_operation_end;
   }

   /**
* Protege name: buffer_after_operation_end
   */
   private float buffer_after_operation_end;
   public void setBuffer_after_operation_end(float value) { 
    this.buffer_after_operation_end=value;
   }
   public float getBuffer_after_operation_end() {
     return this.buffer_after_operation_end;
   }

   /**
* Protege name: startState
   */
   private State startState;
   public void setStartState(State value) { 
    this.startState=value;
   }
   public State getStartState() {
     return this.startState;
   }

   /**
* Protege name: appliedOn
   */
   private Workpiece appliedOn;
   public void setAppliedOn(Workpiece value) { 
    this.appliedOn=value;
   }
   public Workpiece getAppliedOn() {
     return this.appliedOn;
   }

   /**
* Protege name: buffer_before_operation_start
   */
   private float buffer_before_operation_start;
   public void setBuffer_before_operation_start(float value) { 
    this.buffer_before_operation_start=value;
   }
   public float getBuffer_before_operation_start() {
     return this.buffer_before_operation_start;
   }

   /**
* Protege name: avg_Duration
   */
   private float avg_Duration;
   public void setAvg_Duration(float value) { 
    this.avg_Duration=value;
   }
   public float getAvg_Duration() {
     return this.avg_Duration;
   }

   /**
* Protege name: endState
   */
   private State endState;
   public void setEndState(State value) { 
    this.endState=value;
   }
   public State getEndState() {
     return this.endState;
   }

}
