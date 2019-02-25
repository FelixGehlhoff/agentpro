package support_classes;

import java.util.Date;

import jade.core.messaging.PersistentDeliveryFilter;
import jade.lang.acl.ACLMessage;
/*
 * Filter class. Stores messages either indefinitely or checks the reply by date and stores until then.
 */
public class MyFilter implements PersistentDeliveryFilter{

	@Override
	public long delayBeforeExpiration(ACLMessage msg) {
		
		Date d = msg.getReplyByDate();						//checks for a reply by date
		if(d != null) {			
			long delay = d.getTime() - System.currentTimeMillis();
			return (delay > 0) ? delay : 0;			
			/*
			 * (a > b) ? a : b; is an expression which returns one of two values, a or b. The condition, (a > b), 
			 * is tested. If it is true the first value, a, is returned. If it is false, the second value, b, is returned. Whichever value is returned is dependent on the conditional test, a > b. The condition can be any expression which returns a boolean value.
			 */
		}
		else {
			int time_to_buffer = 5*60*1000;
			return time_to_buffer;
		}
		//NOW = 0 message is not claimed
		//NEVER = -1 message is stored indefinitely
		//If a filter returns a different value from NOW, it is considered as a time delay in milliseconds
	//return 20000;
	}

}
