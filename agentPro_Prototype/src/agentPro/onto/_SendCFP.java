package agentPro.onto;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: _SendCFP
* @author ontology bean generator
* @version 2019/04/24, 14:43:54
*/
public class _SendCFP implements AgentAction {

//////////////////////////// User code
int a;
   /**
* Protege name: hasCFP
   */
   private List hasCFP = new ArrayList();
   public void addHasCFP(CFP elem) { 
     List oldList = this.hasCFP;
     hasCFP.add(elem);
   }
   public boolean removeHasCFP(CFP elem) {
     List oldList = this.hasCFP;
     boolean result = hasCFP.remove(elem);
     return result;
   }
   public void clearAllHasCFP() {
     List oldList = this.hasCFP;
     hasCFP.clear();
   }
   public Iterator getAllHasCFP() {return hasCFP.iterator(); }
   public List getHasCFP() {return hasCFP; }
   public void setHasCFP(List l) {hasCFP = l; }

}
