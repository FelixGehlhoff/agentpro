package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Resource
* @author ontology bean generator
* @version 2020/06/26, 11:54:55
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
* Protege name: startState
   */
   private State startState;
   public void setStartState(State value) { 
    this.startState=value;
   }
   public State getStartState() {
     return this.startState;
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
* Protege name: currentState
   */
   private State currentState;
   public void setCurrentState(State value) { 
    this.currentState=value;
   }
   public State getCurrentState() {
     return this.currentState;
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
