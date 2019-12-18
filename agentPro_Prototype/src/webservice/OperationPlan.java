package webservice;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name = "OperationPlan")
public class OperationPlan {
	
	public int Ressource_ID;
	public String Ressource_Name;
	
	@XmlElementWrapper(name = "TimeSlots")
    @XmlElement(name = "TimeSlot")
		public List  <TimeSlot> TimeSlots = new ArrayList<TimeSlot>();

}
