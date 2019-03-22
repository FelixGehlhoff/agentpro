package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Request_Buffer
* @author ontology bean generator
* @version 2019/03/20, 15:44:38
*/
public class Request_Buffer extends Message{ 

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
