package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Capability
* @author ontology bean generator
* @version 2019/09/27, 11:08:18
*/
public class Capability implements Concept {

   /**
* Protege name: enables
   */
   private List enables = new ArrayList();
   public void addEnables(Operation elem) { 
     List oldList = this.enables;
     enables.add(elem);
   }
   public boolean removeEnables(Operation elem) {
     List oldList = this.enables;
     boolean result = enables.remove(elem);
     return result;
   }
   public void clearAllEnables() {
     List oldList = this.enables;
     enables.clear();
   }
   public Iterator getAllEnables() {return enables.iterator(); }
   public List getEnables() {return enables; }
   public void setEnables(List l) {enables = l; }

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
