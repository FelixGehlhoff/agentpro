package agentPro.onto.copy;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Workpiece
* @author ontology bean generator
* @version 2019/04/24, 14:43:54
*/
public class Workpiece implements Concept {

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
* Protege name: becomes
   */
   private Product becomes;
   public void setBecomes(Product value) { 
    this.becomes=value;
   }
   public Product getBecomes() {
     return this.becomes;
   }

}