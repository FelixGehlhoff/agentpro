package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: OrderPosition
* @author ontology bean generator
* @version 2019/01/15, 10:47:24
*/
public class OrderPosition implements Concept {

   /**
* Protege name: containsProduct
   */
   private Product containsProduct;
   public void setContainsProduct(Product value) { 
    this.containsProduct=value;
   }
   public Product getContainsProduct() {
     return this.containsProduct;
   }

   /**
* Protege name: priority
   */
   private int priority;
   public void setPriority(int value) { 
    this.priority=value;
   }
   public int getPriority() {
     return this.priority;
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
* Protege name: sequence_Number
   */
   private int sequence_Number;
   public void setSequence_Number(int value) { 
    this.sequence_Number=value;
   }
   public int getSequence_Number() {
     return this.sequence_Number;
   }

   /**
* Protege name: hasTargetWarehouse
   */
   private Warehouse_Resource hasTargetWarehouse;
   public void setHasTargetWarehouse(Warehouse_Resource value) { 
    this.hasTargetWarehouse=value;
   }
   public Warehouse_Resource getHasTargetWarehouse() {
     return this.hasTargetWarehouse;
   }

   /**
* Protege name: startDate_actual
   */
   private float startDate_actual;
   public void setStartDate_actual(float value) { 
    this.startDate_actual=value;
   }
   public float getStartDate_actual() {
     return this.startDate_actual;
   }

   /**
* Protege name: quantity
   */
   private int quantity;
   public void setQuantity(int value) { 
    this.quantity=value;
   }
   public int getQuantity() {
     return this.quantity;
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

   /**
* Protege name: dueDate
   */
   private String dueDate;
   public void setDueDate(String value) { 
    this.dueDate=value;
   }
   public String getDueDate() {
     return this.dueDate;
   }

   /**
* Protege name: endDate_actual
   */
   private float endDate_actual;
   public void setEndDate_actual(float value) { 
    this.endDate_actual=value;
   }
   public float getEndDate_actual() {
     return this.endDate_actual;
   }

   /**
* Protege name: releaseDate
   */
   private String releaseDate;
   public void setReleaseDate(String value) { 
    this.releaseDate=value;
   }
   public String getReleaseDate() {
     return this.releaseDate;
   }

}
