package tpt.jms;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Administers an agent server for the classic samples.
 */
public class ClassicAdmin {
	
	public static void main(String[] args) throws Exception {
	    System.out.println();
	    System.out.println("Classic administration...");

	    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
	    AdminModule.connect(cf, "root", "root");

	    Queue queue = Queue.create("queue");
	    queue.setFreeReading();
	    queue.setFreeWriting();
	    Queue queue2 = Queue.create("queue2");
	    queue2.setFreeReading();
	    queue2.setFreeWriting();
	    Topic topic = Topic.create("topic");
	    topic.setFreeReading();
	    topic.setFreeWriting();
	    
	    //User.create("anonymous", "anonymous");
	    User.create("admin", "joram");

//	    ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters().addOutInterceptor("classic.Interceptor");
	    QueueConnectionFactory qcf = TcpConnectionFactory.create("localhost", 16010);
	    TopicConnectionFactory tcf = TcpConnectionFactory.create("localhost", 16010);

	    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	    jndiCtx.bind("ConnectionFactory", cf);
	    jndiCtx.bind("qcf", qcf);
	    jndiCtx.bind("tcf", tcf);
	    jndiCtx.bind("queue1", queue);
	    jndiCtx.bind("queue2", queue2);
	    jndiCtx.bind("topic1", topic);
	    jndiCtx.close();

	    AdminModule.disconnect();
	    System.out.println("Admin closed.");
	  }
}
