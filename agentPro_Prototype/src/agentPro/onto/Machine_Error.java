package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Machine_Error
* @author ontology bean generator
* @version 2020/06/26, 11:54:55
*/
public class Machine_Error extends DisturbanceType{ 

   /**
* Protege name: error_type
   */
   private int error_type;
   public void setError_type(int value) { 
    this.error_type=value;
   }
   public int getError_type() {
     return this.error_type;
   }

   /**
* Protege name: expected_Duration_Of_Repair
   */
   private float expected_Duration_Of_Repair;
   public void setExpected_Duration_Of_Repair(float value) { 
    this.expected_Duration_Of_Repair=value;
   }
   public float getExpected_Duration_Of_Repair() {
     return this.expected_Duration_Of_Repair;
   }

   /**
* Protege name: expected_date_of_repair_Start
   */
   private float expected_date_of_repair_Start;
   public void setExpected_date_of_repair_Start(float value) { 
    this.expected_date_of_repair_Start=value;
   }
   public float getExpected_date_of_repair_Start() {
     return this.expected_date_of_repair_Start;
   }

}
