package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Inform_ArrivalAndDeparture
* @author ontology bean generator
* @version 2019/02/26, 10:51:45
*/
public class Inform_ArrivalAndDeparture extends Message{ 

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
* Protege name: ArrivalTime
   */
   private String arrivalTime;
   public void setArrivalTime(String value) { 
    this.arrivalTime=value;
   }
   public String getArrivalTime() {
     return this.arrivalTime;
   }

   /**
* Protege name: DepartureTime
   */
   private String departureTime;
   public void setDepartureTime(String value) { 
    this.departureTime=value;
   }
   public String getDepartureTime() {
     return this.departureTime;
   }

}
