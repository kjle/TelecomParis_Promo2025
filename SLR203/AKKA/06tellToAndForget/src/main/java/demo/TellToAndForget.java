package demo;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.Ref2;
import demo.Message.StringMessage;


/**
 * @author KANG Jiale
 * @description
 */
public class TellToAndForget {

	public static void main(String[] args) {

		final ActorSystem system = ActorSystem.create("system");
		final LoggingAdapter log = Logging.getLogger(system, "main");

		// Instantiate the actor a, transmitter and the actor b 
		final ActorRef a = system.actorOf(ActorA.createActor(), "a");
		final ActorRef transmitter = system.actorOf(ActorTransmitter.createActor(), "transmitter");
		final ActorRef b = system.actorOf(ActorB.createActor(), "b");

		// send to a 
		Ref2 ref2 = new Ref2(transmitter, b);
		StringMessage m = new StringMessage("start");
		a.tell(ref2, ActorRef.noSender());
		a.tell(m, ActorRef.noSender());

		
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
