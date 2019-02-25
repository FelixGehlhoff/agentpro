package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Location
* @author ontology bean generator
* @version 2019/02/25, 12:12:45
*/
public class Location implements Concept {

   /**
* Protege name: coordY
   */
   private float coordY;
   public void setCoordY(float value) { 
    this.coordY=value;
   }
   public float getCoordY() {
     return this.coordY;
   }

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

}
