package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Timeslot
* @author ontology bean generator
* @version 2020/06/26, 11:54:55
*/
public class Timeslot implements Concept {

//////////////////////////// User code
@Override     public String toString() {return " timeslot start: "+startDate+" end "+endDate;}
   /**
* Protege name: length
   */
   private float length;
   public void setLength(float value) { 
    this.length=value;
   }
   public float getLength() {
     return this.length;
   }

   /**
* Protege name: endDate
   */
   private String endDate;
   public void setEndDate(String value) { 
    this.endDate=value;
   }
   public String getEndDate() {
     return this.endDate;
   }

   /**
* Protege name: startDate
   */
   private String startDate;
   public void setStartDate(String value) { 
    this.startDate=value;
   }
   public String getStartDate() {
     return this.startDate;
   }

}
