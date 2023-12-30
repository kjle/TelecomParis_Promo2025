package demo;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * @author Remi SHARROCK and Axel Mathieu
 * @description Create an actor and passing his reference to
 *				another actor by message.
 */
public class CreateAndReferenceByMessage {

	public static void main(String[] args) {
		// Instantiate an actor system
		final ActorSystem system = ActorSystem.create("system");
		
		// Instantiate first and second actor
	    final ActorRef a1 = system.actorOf(FirstActor.createActor(), "a1");
	    final ActorRef a2 = system.actorOf(SecondActor.createActor(), "a2");
	    
			// send to a1 the reference of a2 by message
			//be carefull, here it is the main() function that sends a message to a1, 
			//not a1 telling to a2 as you might think when looking at this line:
	    a1.tell(a2, ActorRef.noSender());
	    
	
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
