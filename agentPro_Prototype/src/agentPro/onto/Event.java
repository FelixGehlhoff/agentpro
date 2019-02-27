package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Event
* @author ontology bean generator
* @version 2019/02/26, 10:51:45
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
