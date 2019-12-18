package webservice;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.Duration;


@XmlRootElement
public class TimeSlot {
	public Date Start;
	public Date End;
	
	//public Duration Timespan;
	

}
