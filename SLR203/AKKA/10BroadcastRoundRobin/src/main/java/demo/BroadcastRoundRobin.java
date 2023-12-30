package demo;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.Ref1;


/**
 * @author KANG Jiale
 * @description create all the actors and send the broadcaster reference to all the other actors.
 */
public class BroadcastRoundRobin {

	public static void main(String[] args) {

		final ActorSystem system = ActorSystem.create("system");
		final LoggingAdapter log = Logging.getLogger(system, "main");

		// Instantiate the actor a (sender), b (receiver), c (receiver) and broadcaster
		final ActorRef a = system.actorOf(ActorSender.createActor(), "a");
		final ActorRef b = system.actorOf(ActorReceiver.createActor(), "b");
		final ActorRef c = system.actorOf(ActorReceiver.createActor(), "c");
		final ActorRef broadcaster = system.actorOf(ActorBroadcaster.createActor(), "broadcaster");

		// Send the broadcaster reference to all the other actors
		Ref1 broadcasterRef = new Ref1(broadcaster);
		a.tell(broadcasterRef, ActorRef.noSender());
		b.tell(broadcasterRef, ActorRef.noSender());
		c.tell(broadcasterRef, ActorRef.noSender());
		
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
