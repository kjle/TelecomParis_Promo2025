package demo;

import java.util.ArrayList;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.MyBrokenMessage;
import demo.MyActor.PrintBrokenMessage;
import demo.MyActor.PrintCorrectMessage;

/**
 * @author Remi SHARROCK
 * @description
 */
public class MessageImmutabilityProblem {

	public static void main(String[] args) {

		final ActorSystem system = ActorSystem.create("system");
		final LoggingAdapter log = Logging.getLogger(system, "main");

		// Instantiate first and second actor
		final ActorRef a = system.actorOf(MyActor.createActor(), "a");

		// send to a1 the reference of a2 by message
		// be carefull, here it is the main() function that sends a message to a1,
		// not a1 telling to a2 as you might think when looking at this line:
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		int i = 8;
		Person p = new Person("bob");
		String s = "hi";
		MyBrokenMessage brokenMessage = new MyBrokenMessage(list, i, p, s);
		MyCorrectMessage correctMessage = new MyCorrectMessage(list, i, p, s);
		log.info("Message created: list=" + brokenMessage.list + " i=" + brokenMessage.i + " p.getName()="
				+ brokenMessage.p.getName() + " s=" + brokenMessage.s);

		a.tell(brokenMessage, ActorRef.noSender());
		a.tell(correctMessage, ActorRef.noSender());

		sleepFor(1);

		list.add(4);
		i = 9;
		p.changeName("alice");
		s = "bye";

		log.info("modified: list=" + list + " i=" + i + " p.getName()=" + p.getName() + " s=" + s);
	
		a.tell(new PrintBrokenMessage(), ActorRef.noSender());
		a.tell(new PrintCorrectMessage(), ActorRef.noSender());
		
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

	public static void sleepFor(int sec) {
		try {
			Thread.sleep(sec * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
