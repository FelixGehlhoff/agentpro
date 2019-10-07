package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Event
* @author ontology bean generator
* @version 2019/09/27, 11:08:18
*/
public class Event implements Concept {

   /**
* Protege name: topic
   */
   private String topic;
   public void setTopic(String value) { 
    this.topic=value;
   }
   public String getTopic() {
     return this.topic;
   }

}
