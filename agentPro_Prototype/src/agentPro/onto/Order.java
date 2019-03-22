package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Order
* @author ontology bean generator
* @version 2019/03/20, 15:44:38
*/
public class Order implements Concept {

   /**
* Protege name: consistsOfOrderPositions
   */
   private List consistsOfOrderPositions = new ArrayList();
   public void addConsistsOfOrderPositions(OrderPosition elem) { 
     List oldList = this.consistsOfOrderPositions;
     consistsOfOrderPositions.add(elem);
   }
   public boolean removeConsistsOfOrderPositions(OrderPosition elem) {
     List oldList = this.consistsOfOrderPositions;
     boolean result = consistsOfOrderPositions.remove(elem);
     return result;
   }
   public void clearAllConsistsOfOrderPositions() {
     List oldList = this.consistsOfOrderPositions;
     consistsOfOrderPositions.clear();
   }
   public Iterator getAllConsistsOfOrderPositions() {return consistsOfOrderPositions.iterator(); }
   public List getConsistsOfOrderPositions() {return consistsOfOrderPositions; }
   public void setConsistsOfOrderPositions(List l) {consistsOfOrderPositions = l; }

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
