package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Transport_Operation
* @author ontology bean generator
* @version 2019/01/15, 10:47:24
*/
public class Transport_Operation extends Operation{ 

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
* Protege name: buffer_before_operation
   */
   private float buffer_before_operation;
   public void setBuffer_before_operation(float value) { 
    this.buffer_before_operation=value;
   }
   public float getBuffer_before_operation() {
     return this.buffer_before_operation;
   }

   /**
* Protege name: hasEndLocation
   */
   private Location hasEndLocation;
   public void setHasEndLocation(Location value) { 
    this.hasEndLocation=value;
   }
   public Location getHasEndLocation() {
     return this.hasEndLocation;
   }

   /**
* Protege name: hasStartLocation
   */
   private Location hasStartLocation;
   public void setHasStartLocation(Location value) { 
    this.hasStartLocation=value;
   }
   public Location getHasStartLocation() {
     return this.hasStartLocation;
   }

   /**
* Protege name: hasDetailedOperationDescription
   */
   private DetailedOperationDescription hasDetailedOperationDescription;
   public void setHasDetailedOperationDescription(DetailedOperationDescription value) { 
    this.hasDetailedOperationDescription=value;
   }
   public DetailedOperationDescription getHasDetailedOperationDescription() {
     return this.hasDetailedOperationDescription;
   }

   /**
* Protege name: buffer_after_operation
   */
   private float buffer_after_operation;
   public void setBuffer_after_operation(float value) { 
    this.buffer_after_operation=value;
   }
   public float getBuffer_after_operation() {
     return this.buffer_after_operation;
   }

}
