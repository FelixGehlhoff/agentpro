package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Resource
* @author ontology bean generator
* @version 2019/02/26, 10:51:45
*/
public class Resource implements Concept {

   /**
* Protege name: detailed_Type
   */
   private String detailed_Type;
   public void setDetailed_Type(String value) { 
    this.detailed_Type=value;
   }
   public String getDetailed_Type() {
     return this.detailed_Type;
   }

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
* Protege name: hasCapability
   */
   private Capability hasCapability;
   public void setHasCapability(Capability value) { 
    this.hasCapability=value;
   }
   public Capability getHasCapability() {
     return this.hasCapability;
   }

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
* Protege name: hasDisturbance
   */
   private Disturbance hasDisturbance;
   public void setHasDisturbance(Disturbance value) { 
    this.hasDisturbance=value;
   }
   public Disturbance getHasDisturbance() {
     return this.hasDisturbance;
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

   /**
* Protege name: type
   */
   private String type;
   public void setType(String value) { 
    this.type=value;
   }
   public String getType() {
     return this.type;
   }

}
