package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Request_DatabaseEntry
* @author ontology bean generator
* @version 2020/06/26, 11:54:55
*/
public class Request_DatabaseEntry extends Message{ 

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
