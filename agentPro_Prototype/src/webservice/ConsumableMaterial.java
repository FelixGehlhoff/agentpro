package webservice;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ConsumableMaterial {
	
	public int ID;
	public Article Article;
	public String Unit;
	public double Amount;

}
