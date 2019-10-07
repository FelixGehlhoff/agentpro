package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Location
* @author ontology bean generator
* @version 2019/09/30, 10:42:56
*/
public class Location extends State{ 

//////////////////////////// User code
@Override     public String toString() {return "["+coordX+";"+coordY+"]";}
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
* Protege name: coordY
   */
   private float coordY;
   public void setCoordY(float value) { 
    this.coordY=value;
   }
   public float getCoordY() {
     return this.coordY;
   }

}
