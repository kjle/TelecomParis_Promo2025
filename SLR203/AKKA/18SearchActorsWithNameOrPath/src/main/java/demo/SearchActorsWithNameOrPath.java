package demo;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.CreateMessage;


/**
 * @author KANG Jiale
 * @description Create an actor named "a" that creates an actor when he receives the CREATE message. The actor created has the name "actorX" where X is incremented by one after each creation, starting at 1.
 */
public class SearchActorsWithNameOrPath {

	public static void main(String[] args) {

		final ActorSystem system = ActorSystem.create("system");
		final LoggingAdapter log = Logging.getLogger(system, "main");

		// Instantiate the actor A
		final ActorRef a = system.actorOf(ActorA.createActor(), "a");

		// Send the "create" message to actor A
		CreateMessage m = new CreateMessage();
		a.tell(m, ActorRef.noSender());
		a.tell(m, ActorRef.noSender());

		//wait for 2 seconds to create the actors
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// search for actors
		system.actorSelection("/user/a/*").tell("path",ActorRef.noSender());
		
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
