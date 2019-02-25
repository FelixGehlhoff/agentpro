package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: DetailedOperationDescription
* @author ontology bean generator
* @version 2019/01/15, 10:47:24
*/
public class DetailedOperationDescription implements Concept {

   /**
* Protege name: hasRequest_Points
   */
   private List hasRequest_Points = new ArrayList();
   public void addHasRequest_Points(Request_Point elem) { 
     List oldList = this.hasRequest_Points;
     hasRequest_Points.add(elem);
   }
   public boolean removeHasRequest_Points(Request_Point elem) {
     List oldList = this.hasRequest_Points;
     boolean result = hasRequest_Points.remove(elem);
     return result;
   }
   public void clearAllHasRequest_Points() {
     List oldList = this.hasRequest_Points;
     hasRequest_Points.clear();
   }
   public Iterator getAllHasRequest_Points() {return hasRequest_Points.iterator(); }
   public List getHasRequest_Points() {return hasRequest_Points; }
   public void setHasRequest_Points(List l) {hasRequest_Points = l; }

}
