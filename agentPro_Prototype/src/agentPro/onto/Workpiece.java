package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Workpiece
* @author ontology bean generator
* @version 2019/01/15, 10:47:24
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

}
