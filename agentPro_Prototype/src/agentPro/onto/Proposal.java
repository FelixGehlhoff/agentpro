package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Proposal
* @author ontology bean generator
* @version 2019/03/20, 15:44:38
*/
public class Proposal extends Message{ 

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

}
