package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Operation
* @author ontology bean generator
* @version 2019/01/15, 10:47:24
*/
public class Operation implements Concept {

   /**
* Protege name: type
   */
   private String type;
   public void setType(String value) { 
    this.type=value;
   }
   public String getType() {
     return this.type;
   }

   /**
* Protege name: appliedOn
   */
   private Workpiece appliedOn;
   public void setAppliedOn(Workpiece value) { 
    this.appliedOn=value;
   }
   public Workpiece getAppliedOn() {
     return this.appliedOn;
   }

   /**
* Protege name: avg_Duration
   */
   private float avg_Duration;
   public void setAvg_Duration(float value) { 
    this.avg_Duration=value;
   }
   public float getAvg_Duration() {
     return this.avg_Duration;
   }

   /**
* Protege name: isEnabledBy
   */
   private List isEnabledBy = new ArrayList();
   public void addIsEnabledBy(Capability elem) { 
     List oldList = this.isEnabledBy;
     isEnabledBy.add(elem);
   }
   public boolean removeIsEnabledBy(Capability elem) {
     List oldList = this.isEnabledBy;
     boolean result = isEnabledBy.remove(elem);
     return result;
   }
   public void clearAllIsEnabledBy() {
     List oldList = this.isEnabledBy;
     isEnabledBy.clear();
   }
   public Iterator getAllIsEnabledBy() {return isEnabledBy.iterator(); }
   public List getIsEnabledBy() {return isEnabledBy; }
   public void setIsEnabledBy(List l) {isEnabledBy = l; }

   /**
* Protege name: set_up_time
   */
   private float set_up_time;
   public void setSet_up_time(float value) { 
    this.set_up_time=value;
   }
   public float getSet_up_time() {
     return this.set_up_time;
   }

   /**
* Protege name: name
   */
   private String name;
   public void setName(String value) { 
    this.name=value;
   }
   public String getName() {
     return this.name;
   }

}
