package demo;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.Ref1;


/**
 * @author KANG Jiale
 * @description the convergecast pattern is also called merger or converger
 */
public class PublishSubscribe {

	public static void main(String[] args) {

		final ActorSystem system = ActorSystem.create("system");
		final LoggingAdapter log = Logging.getLogger(system, "main");

		// Instantiate the actor a (sender), b (sender), c (sender), d (receiver) and merger
		final ActorRef a = system.actorOf(ActorSender.createActor(), "a");
		final ActorRef b = system.actorOf(ActorSender.createActor(), "b");
		final ActorRef c = system.actorOf(ActorSender.createActor(), "c");
		final ActorRef d = system.actorOf(ActorReceiver.createActor(), "d");
		final ActorRef merger = system.actorOf(ActorMerger.createActor(), "merger");

		// Send the merger reference to all the other actors
		Ref1 mergerRef = new Ref1(merger);
		a.tell(mergerRef, ActorRef.noSender());
		b.tell(mergerRef, ActorRef.noSender());
		c.tell(mergerRef, ActorRef.noSender());
		merger.tell(d, ActorRef.noSender());
		
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
