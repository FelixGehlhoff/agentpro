package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Person
* @author ontology bean generator
* @version 2019/11/11, 09:45:55
*/
public class Person implements Concept {

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
