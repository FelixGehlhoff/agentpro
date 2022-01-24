package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Production_Operation
* @author ontology bean generator
* @version 2020/06/26, 11:54:55
*/
public class Production_Operation extends Operation{ 

   /**
* Protege name: requiresMaterial
   */
   private Material requiresMaterial;
   public void setRequiresMaterial(Material value) { 
    this.requiresMaterial=value;
   }
   public Material getRequiresMaterial() {
     return this.requiresMaterial;
   }

}
