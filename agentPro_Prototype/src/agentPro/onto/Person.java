package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Person
* @author ontology bean generator
* @version 2019/02/25, 12:12:45
*/
public class Person implements Concept {

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
