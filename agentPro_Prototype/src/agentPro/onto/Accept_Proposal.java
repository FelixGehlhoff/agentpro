package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Accept_Proposal
* @author ontology bean generator
* @version 2019/02/25, 12:12:45
*/
public class Accept_Proposal extends Message{ 

   /**
* Protege name: hasProposal
   */
   private Proposal hasProposal;
   public void setHasProposal(Proposal value) { 
    this.hasProposal=value;
   }
   public Proposal getHasProposal() {
     return this.hasProposal;
   }

}
