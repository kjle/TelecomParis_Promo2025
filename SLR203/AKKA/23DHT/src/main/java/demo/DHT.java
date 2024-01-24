package demo;

import java.util.ArrayList;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.AddNodeMessage;
import demo.Message.FindNodeMessage;
import demo.Message.FindValueMessage;
import demo.Message.PrintRoutingTableMessage;
import demo.Message.StoreMessage;


/**
 * @author KANG Jiale
 * @description 
 */
public class DHT {

	public static void main(String[] args) {

		final ActorSystem system = ActorSystem.create("system");
		final LoggingAdapter log = Logging.getLogger(system, "main");

		// Create the message actor
		// ActorRef messageActor = system.actorOf(MessageActor.createActor(), "messageActor");

		// Create the nodes		
		ArrayList<ActorRef> nodes = new ArrayList<>();

        // Create a few nodes
        for (int i = 0; i <64; i++) {
            ActorRef node = system.actorOf(NodeActor.createActor(i, 2, 6), "node" + i);
            nodes.add(node);

			// AddNodeMessage addNodeMessage = new AddNodeMessage(node, i);
			// for (ActorRef n : nodes) {
			// 	n.tell(addNodeMessage, ActorRef.noSender());
			// }
			// log.info(node.toString());
        }

		int [] computerList = {1, 11, 12, 14, 20, 22, 27, 28, 31, 32, 33, 36, 47, 52, 56};
		// int [] computerList = {0, 5};
		for (int i : computerList) {
			AddNodeMessage addNodeMessage = new AddNodeMessage(nodes.get(i), i);
			for (ActorRef n : nodes) {
				if (n.equals(nodes.get(i))) continue;
				n.tell(addNodeMessage, ActorRef.noSender());
			}
			// PrintRoutingTableMessage printRoutingTableMessage = new PrintRoutingTableMessage();
			// nodes.get(i).tell(printRoutingTableMessage, ActorRef.noSender());

		}

		// tell node 0 to store key 30
		KeyValue kv = new KeyValue("30", "hello");
		nodes.get(10).tell(new StoreMessage(kv), ActorRef.noSender());
		KeyValue kv2 = new KeyValue("19", "bonjour");
		nodes.get(61).tell(new StoreMessage(kv2), ActorRef.noSender());

		// tell node 0 to find node which key = 48
		FindNodeMessage findNodeMessage = new FindNodeMessage(Integer.parseInt("48"), 3);
		nodes.get(10).tell(findNodeMessage, ActorRef.noSender());
		
		// tell node 0 to find value which key = 30
		FindValueMessage findValueMessage = new FindValueMessage("19");
		nodes.get(10).tell(findValueMessage, ActorRef.noSender());


		// We wait 5 seconds before ending system (by default)
		// But this is not the best solution.
		try {
			waitBeforeTerminate();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			system.terminate();
		}
	}

	public static void waitBeforeTerminate() throws InterruptedException {
		Thread.sleep(5000);
	}

}
