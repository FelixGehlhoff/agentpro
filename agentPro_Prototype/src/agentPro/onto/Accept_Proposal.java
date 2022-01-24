package agentPro.onto;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Accept_Proposal
* @author ontology bean generator
* @version 2020/06/26, 11:54:55
*/
public class Accept_Proposal extends Message{ 

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

   /**
* Protege name: ID_String
   */
   private String iD_String;
   public void setID_String(String value) { 
    this.iD_String=value;
   }
   public String getID_String() {
     return this.iD_String;
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
