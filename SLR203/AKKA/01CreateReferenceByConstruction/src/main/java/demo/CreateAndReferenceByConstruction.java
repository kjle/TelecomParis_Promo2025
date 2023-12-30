package demo;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * @author Remi SHARROCK and Axel Mathieu
 * @description Create an actor by passing the reference of another actor at construction time.
 */
public class CreateAndReferenceByConstruction {

	public static void main(String[] args) {
		final ActorSystem system = ActorSystem.create("system");
	    final ActorRef a2 = system.actorOf(SecondActor.createActor(), "a2");
		@SuppressWarnings("unused")
		//we create the actor a1 of type FirstActor and gives a2 as a reference during the construction
		final ActorRef a1 = system.actorOf(FirstActor.createActor(a2), "a1");

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
