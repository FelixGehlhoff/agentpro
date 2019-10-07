package agentPro.onto.copy;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Product_Part
* @author ontology bean generator
* @version 2019/04/24, 14:43:54
*/
public class Product_Part implements Concept {

   /**
* Protege name: hasLocation
   */
   private Location hasLocation;
   public void setHasLocation(Location value) { 
    this.hasLocation=value;
   }
   public Location getHasLocation() {
     return this.hasLocation;
   }

   /**
* Protege name: isNeededFor
   */
   private List isNeededFor = new ArrayList();
   public void addIsNeededFor(Object elem) { 
     List oldList = this.isNeededFor;
     isNeededFor.add(elem);
   }
   public boolean removeIsNeededFor(Object elem) {
     List oldList = this.isNeededFor;
     boolean result = isNeededFor.remove(elem);
     return result;
   }
   public void clearAllIsNeededFor() {
     List oldList = this.isNeededFor;
     isNeededFor.clear();
   }
   public Iterator getAllIsNeededFor() {return isNeededFor.iterator(); }
   public List getIsNeededFor() {return isNeededFor; }
   public void setIsNeededFor(List l) {isNeededFor = l; }

}