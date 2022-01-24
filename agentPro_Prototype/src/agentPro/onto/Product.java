package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Product
* @author ontology bean generator
* @version 2020/06/26, 11:54:55
*/
public class Product implements Concept {

   /**
* Protege name: hasProductionPlan
   */
   private ProductionPlan hasProductionPlan;
   public void setHasProductionPlan(ProductionPlan value) { 
    this.hasProductionPlan=value;
   }
   public ProductionPlan getHasProductionPlan() {
     return this.hasProductionPlan;
   }

   /**
* Protege name: isDefinedByProductionPlan
   */
   private ProductionPlan isDefinedByProductionPlan;
   public void setIsDefinedByProductionPlan(ProductionPlan value) { 
    this.isDefinedByProductionPlan=value;
   }
   public ProductionPlan getIsDefinedByProductionPlan() {
     return this.isDefinedByProductionPlan;
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

   /**
* Protege name: iD_Number
   */
   private int iD_Number;
   public void setID_Number(int value) { 
    this.iD_Number=value;
   }
   public int getID_Number() {
     return this.iD_Number;
   }

}
