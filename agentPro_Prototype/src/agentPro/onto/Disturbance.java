package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Disturbance
* @author ontology bean generator
* @version 2019/03/4, 13:24:52
*/
public class Disturbance extends Event{ 

   /**
* Protege name: localizationDelta
   */
   private float localizationDelta;
   public void setLocalizationDelta(float value) { 
    this.localizationDelta=value;
   }
   public float getLocalizationDelta() {
     return this.localizationDelta;
   }

   /**
* Protege name: detectedBy
   */
   private Person detectedBy;
   public void setDetectedBy(Person value) { 
    this.detectedBy=value;
   }
   public Person getDetectedBy() {
     return this.detectedBy;
   }

   /**
* Protege name: occuresAt
   */
   private Resource occuresAt;
   public void setOccuresAt(Resource value) { 
    this.occuresAt=value;
   }
   public Resource getOccuresAt() {
     return this.occuresAt;
   }

   /**
* Protege name: error_occurance_time
   */
   private float error_occurance_time;
   public void setError_occurance_time(float value) { 
    this.error_occurance_time=value;
   }
   public float getError_occurance_time() {
     return this.error_occurance_time;
   }

   /**
* Protege name: id_workpiece
   */
   private int id_workpiece;
   public void setId_workpiece(int value) { 
    this.id_workpiece=value;
   }
   public int getId_workpiece() {
     return this.id_workpiece;
   }

   /**
* Protege name: hasDisturbanceType
   */
   private DisturbanceType hasDisturbanceType;
   public void setHasDisturbanceType(DisturbanceType value) { 
    this.hasDisturbanceType=value;
   }
   public DisturbanceType getHasDisturbanceType() {
     return this.hasDisturbanceType;
   }

   /**
* Protege name: processDelta
   */
   private float processDelta;
   public void setProcessDelta(float value) { 
    this.processDelta=value;
   }
   public float getProcessDelta() {
     return this.processDelta;
   }

   /**
* Protege name: timeDelta
   */
   private float timeDelta;
   public void setTimeDelta(float value) { 
    this.timeDelta=value;
   }
   public float getTimeDelta() {
     return this.timeDelta;
   }

}
