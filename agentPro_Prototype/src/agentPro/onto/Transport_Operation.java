package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Transport_Operation
* @author ontology bean generator
* @version 2019/04/24, 14:43:54
*/
public class Transport_Operation extends Operation{ 

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
* Protege name: avg_PickupTime
   */
   private int avg_PickupTime;
   public void setAvg_PickupTime(int value) { 
    this.avg_PickupTime=value;
   }
   public int getAvg_PickupTime() {
     return this.avg_PickupTime;
   }

}
