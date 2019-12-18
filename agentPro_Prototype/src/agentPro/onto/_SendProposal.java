package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: _SendProposal
* @author ontology bean generator
* @version 2019/11/11, 09:45:55
*/
public class _SendProposal implements AgentAction {

   /**
* Protege name: hasProposal
   */
   private List hasProposal = new ArrayList();
   public void addHasProposal(Proposal elem) { 
     List oldList = this.hasProposal;
     hasProposal.add(elem);
   }
   public boolean removeHasProposal(Proposal elem) {
     List oldList = this.hasProposal;
     boolean result = hasProposal.remove(elem);
     return result;
   }
   public void clearAllHasProposal() {
     List oldList = this.hasProposal;
     hasProposal.clear();
   }
   public Iterator getAllHasProposal() {return hasProposal.iterator(); }
   public List getHasProposal() {return hasProposal; }
   public void setHasProposal(List l) {hasProposal = l; }

}
