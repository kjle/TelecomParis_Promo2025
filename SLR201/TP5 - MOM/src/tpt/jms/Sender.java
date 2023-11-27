package tpt.jms;
import javax.jms.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Sender {
	
	//connection identifiers
	public static final String USERNAME = "admin";
	public static final String PSW = "joram";

	
	public static void main(String[] args) 
    {
		//admin objects
        Context context = null;
        ConnectionFactory factory = null;
        //naming configs
        String factoryName = "ConnectionFactory";
        String destName = null;
        Destination dest = null;
        //jms
        JMSContext jmsContext;
        JMSProducer jmsProducer = null;
        //
        int count = 1;
        String text = "Message ";

        //check arguments
        if (args.length < 1 || args.length > 2){
        	System.out.println("usage: Sender <destination> [count]");
        	System.exit(1);
	    }
        
        //get the destination name
        destName = args[0];
        
        //get the number of messages to be sent
        if (args.length == 2){
        	count = Integer.parseInt(args[1]);
	    }

        try{
			// create the JNDI initial context
			context = new InitialContext();
			
			// look up the ConnectionFactory
			factory = (ConnectionFactory) context.lookup(factoryName);
			
			// look up the Destination (queue or topic)
			dest = (Destination) context.lookup(destName);
			
			//close intitialContext
        	context.close();
			
			//create the jms context (replacing connection & session in JMS1)
			//the session will be non-transacted and messages received by this session will be acknowledged automatically
			jmsContext = factory.createContext(USERNAME, PSW, JMSContext.AUTO_ACKNOWLEDGE );
			
			// create the producer
			jmsProducer = jmsContext.createProducer();
	
			//send the <text> message for <count> times 
			for (int i = 0; i < count; ++i) {
				jmsProducer.send(dest, text);
				System.out.println("Sender:: sent message " + text + " " + i + " to destination " + destName );
			}
			
			//close JMSConext
			jmsContext.close();
		} 
        catch (NamingException exception){
        	exception.printStackTrace();
	    }  
    }
	
}
