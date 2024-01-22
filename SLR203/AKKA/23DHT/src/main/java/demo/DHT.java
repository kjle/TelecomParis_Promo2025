package demo;

import java.util.ArrayList;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;


/**
 * @author KANG Jiale
 * @description 
 */
public class DHT {

	public static void main(String[] args) {

		final ActorSystem system = ActorSystem.create("system");
		final LoggingAdapter log = Logging.getLogger(system, "main");

		// Create the DHT actor
		ActorRef dht = system.actorOf(DHTActor.createActor(), "dht");

		// Create the message actor
		ActorRef messageActor = system.actorOf(MessageActor.createActor(), "messageActor");

		// Create the nodes		
		ArrayList<ActorRef> nodes = new ArrayList<>();

        // Create a few nodes
        for (int i = 0; i < 8; i++) {
            ActorRef node = system.actorOf(NodeActor.createActor(Integer.toString(i), 2, 3), "node" + i);
            nodes.add(node);
        }

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
