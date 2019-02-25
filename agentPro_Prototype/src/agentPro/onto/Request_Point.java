package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Request_Point
* @author ontology bean generator
* @version 2019/01/15, 10:47:24
*/
public class Request_Point implements Concept {

   /**
* Protege name: coordX
   */
   private float coordX;
   public void setCoordX(float value) { 
    this.coordX=value;
   }
   public float getCoordX() {
     return this.coordX;
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

   /**
* Protege name: time
   */
   private String time;
   public void setTime(String value) { 
    this.time=value;
   }
   public String getTime() {
     return this.time;
   }

}
