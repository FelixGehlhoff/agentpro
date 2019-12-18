package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ProductionPlan
* @author ontology bean generator
* @version 2019/11/11, 09:45:55
*/
public class ProductionPlan implements Concept {

   /**
* Protege name: definesProduct
   */
   private Product definesProduct;
   public void setDefinesProduct(Product value) { 
    this.definesProduct=value;
   }
   public Product getDefinesProduct() {
     return this.definesProduct;
   }

   /**
* Protege name: consistsOfOrderedOperations
   */
   private List consistsOfOrderedOperations = new ArrayList();
   public void addConsistsOfOrderedOperations(OrderedOperation elem) { 
     List oldList = this.consistsOfOrderedOperations;
     consistsOfOrderedOperations.add(elem);
   }
   public boolean removeConsistsOfOrderedOperations(OrderedOperation elem) {
     List oldList = this.consistsOfOrderedOperations;
     boolean result = consistsOfOrderedOperations.remove(elem);
     return result;
   }
   public void clearAllConsistsOfOrderedOperations() {
     List oldList = this.consistsOfOrderedOperations;
     consistsOfOrderedOperations.clear();
   }
   public Iterator getAllConsistsOfOrderedOperations() {return consistsOfOrderedOperations.iterator(); }
   public List getConsistsOfOrderedOperations() {return consistsOfOrderedOperations; }
   public void setConsistsOfOrderedOperations(List l) {consistsOfOrderedOperations = l; }

}
