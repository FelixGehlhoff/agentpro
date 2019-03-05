package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: TransportResource
* @author ontology bean generator
* @version 2019/03/4, 13:24:52
*/
public class TransportResource extends Resource{ 

   /**
* Protege name: avg_PickupTime
   */
   private int avg_PickupTime;
   public void setAvg_PickupTime(int value) { 
    this.avg_PickupTime=value;
   }
   public int getAvg_PickupTime() {
     return this.avg_PickupTime;
   }

   /**
* Protege name: avg_Speed
   */
   private float avg_Speed;
   public void setAvg_Speed(float value) { 
    this.avg_Speed=value;
   }
   public float getAvg_Speed() {
     return this.avg_Speed;
   }

}
