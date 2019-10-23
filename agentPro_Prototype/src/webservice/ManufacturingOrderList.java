package webservice;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name = "ManufacturingOrderList")
//@XmlAccessorType(XmlAccessType.FIELD)
public class ManufacturingOrderList {
	


		
	 

		@XmlElementWrapper(name = "ManufacturingOrders")
	    // XmlElement sets the name of the entities
	  
	    @XmlElement(name = "ManufacturingOrder")
			public List  <ManufacturingOrder> manufacturingorders = new ArrayList<ManufacturingOrder>();
			
		 //@XmlElement(name = "ManufacturingOrder")
		/*
		   public List<ManufacturingOrder> getManufacturingorders() {
				return manufacturingorders;
			}

			public void setManufacturingorders(List<ManufacturingOrder> manufacturingorders) {
				this.manufacturingorders = manufacturingorders;
			}
		*/
	

}
