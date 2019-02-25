package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Crane
* @author ontology bean generator
* @version 2019/02/25, 12:12:45
*/
public class Crane extends TransportResource{ 

   /**
* Protege name: requiresSharedResource
   */
   private List requiresSharedResource = new ArrayList();
   public void addRequiresSharedResource(Object elem) { 
     List oldList = this.requiresSharedResource;
     requiresSharedResource.add(elem);
   }
   public boolean removeRequiresSharedResource(Object elem) {
     List oldList = this.requiresSharedResource;
     boolean result = requiresSharedResource.remove(elem);
     return result;
   }
   public void clearAllRequiresSharedResource() {
     List oldList = this.requiresSharedResource;
     requiresSharedResource.clear();
   }
   public Iterator getAllRequiresSharedResource() {return requiresSharedResource.iterator(); }
   public List getRequiresSharedResource() {return requiresSharedResource; }
   public void setRequiresSharedResource(List l) {requiresSharedResource = l; }

}
