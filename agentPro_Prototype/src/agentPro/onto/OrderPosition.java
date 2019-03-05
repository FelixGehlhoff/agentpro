package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: OrderPosition
* @author ontology bean generator
* @version 2019/03/4, 13:24:52
*/
public class OrderPosition implements Concept {

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
* Protege name: releaseDate
   */
   private String releaseDate;
   public void setReleaseDate(String value) { 
    this.releaseDate=value;
   }
   public String getReleaseDate() {
     return this.releaseDate;
   }

   /**
* Protege name: endDate_String
   */
   private String endDate_String;
   public void setEndDate_String(String value) { 
    this.endDate_String=value;
   }
   public String getEndDate_String() {
     return this.endDate_String;
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
