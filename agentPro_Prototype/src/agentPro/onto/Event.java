package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Event
* @author ontology bean generator
* @version 2019/03/4, 13:24:52
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
