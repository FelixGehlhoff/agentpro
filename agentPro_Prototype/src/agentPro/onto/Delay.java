package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Delay
* @author ontology bean generator
* @version 2019/11/11, 09:45:55
*/
public class Delay extends DisturbanceType{ 

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

}
