package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Proposal
* @author ontology bean generator
* @version 2019/09/27, 11:08:18
*/
public class Proposal extends Message{ 

//////////////////////////// User code
@Override     public String toString() {return iD_String+" timeslot "+((AllocatedWorkingStep)consistsOfAllocatedWorkingSteps.get(0)).getHasTimeslot().toString();}
   /**
* Protege name: price
   */
   private float price;
   public void setPrice(float value) { 
    this.price=value;
   }
   public float getPrice() {
     return this.price;
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
* Protege name: consistsOfAllocatedWorkingSteps
   */
   private List consistsOfAllocatedWorkingSteps = new ArrayList();
   public void addConsistsOfAllocatedWorkingSteps(AllocatedWorkingStep elem) { 
     List oldList = this.consistsOfAllocatedWorkingSteps;
     consistsOfAllocatedWorkingSteps.add(elem);
   }
   public boolean removeConsistsOfAllocatedWorkingSteps(AllocatedWorkingStep elem) {
     List oldList = this.consistsOfAllocatedWorkingSteps;
     boolean result = consistsOfAllocatedWorkingSteps.remove(elem);
     return result;
   }
   public void clearAllConsistsOfAllocatedWorkingSteps() {
     List oldList = this.consistsOfAllocatedWorkingSteps;
     consistsOfAllocatedWorkingSteps.clear();
   }
   public Iterator getAllConsistsOfAllocatedWorkingSteps() {return consistsOfAllocatedWorkingSteps.iterator(); }
   public List getConsistsOfAllocatedWorkingSteps() {return consistsOfAllocatedWorkingSteps; }
   public void setConsistsOfAllocatedWorkingSteps(List l) {consistsOfAllocatedWorkingSteps = l; }

   /**
* Protege name: iD_Number
   */
   private int iD_Number;
   public void setID_Number(int value) { 
    this.iD_Number=value;
   }
   public int getID_Number() {
     return this.iD_Number;
   }

}
