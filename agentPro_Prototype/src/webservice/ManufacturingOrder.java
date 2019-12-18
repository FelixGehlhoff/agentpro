package webservice;




import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ManufacturingOrder {
	
	public int ID;
	//@XmlElement
	public String Name;
	//@XmlElement
	public Date DeliveryDate;
	//@XmlElement
	public Article Article;
	public int Quantity;
	
    //@XmlElement
		public ActualProcess ActualProcess;
	/*
    @XmlElement (name = "ActualProcess")
	public ActualProcess getActualProcess() {
		return ActualProcess;
	}
	public void setActualProcess(ActualProcess actualProcess) {
		ActualProcess = actualProcess;
	}
	@XmlElement (name = "ID")
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	@XmlElement (name = "Name")
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	@XmlElement (name = "DeliveryDate")
	public String getDeliveryDate() {
		return DeliveryDate;
	}
	public void setDeliveryDate(String deliveryDate) {
		DeliveryDate = deliveryDate;
	}
	@XmlElement (name = "Article")
	public Article getArticle() {
		return Article;
	}
	public void setArticle(Article article) {
		this.Article = article;
	}
	
	*/
	
	

}
