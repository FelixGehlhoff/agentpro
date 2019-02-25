package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: LocalizationCheckpoint
* @author ontology bean generator
* @version 2019/01/15, 10:47:24
*/
public class LocalizationCheckpoint implements Concept {

   /**
* Protege name: timeDeviation
   */
   private float timeDeviation;
   public void setTimeDeviation(float value) { 
    this.timeDeviation=value;
   }
   public float getTimeDeviation() {
     return this.timeDeviation;
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
* Protege name: time
   */
   private String time;
   public void setTime(String value) { 
    this.time=value;
   }
   public String getTime() {
     return this.time;
   }

   /**
* Protege name: coordDeviationX
   */
   private float coordDeviationX;
   public void setCoordDeviationX(float value) { 
    this.coordDeviationX=value;
   }
   public float getCoordDeviationX() {
     return this.coordDeviationX;
   }

   /**
* Protege name: coordDeviationY
   */
   private float coordDeviationY;
   public void setCoordDeviationY(float value) { 
    this.coordDeviationY=value;
   }
   public float getCoordDeviationY() {
     return this.coordDeviationY;
   }

}
