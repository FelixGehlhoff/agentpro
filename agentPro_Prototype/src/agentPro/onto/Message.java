package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Message
* @author ontology bean generator
* @version 2019/03/4, 13:24:52
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
