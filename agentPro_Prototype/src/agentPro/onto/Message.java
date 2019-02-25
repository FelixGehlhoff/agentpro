package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Message
* @author ontology bean generator
* @version 2019/01/15, 10:47:24
*/
public class Message implements Concept {

   /**
* Protege name: hasSender
   */
   private AID hasSender;
   public void setHasSender(AID value) { 
    this.hasSender=value;
   }
   public AID getHasSender() {
     return this.hasSender;
   }

}
