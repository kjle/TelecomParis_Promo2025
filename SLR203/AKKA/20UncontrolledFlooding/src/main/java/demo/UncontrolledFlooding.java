package demo;

import java.util.ArrayList;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Matrix;
import demo.Message.RefList;


/**
 * @author KANG Jiale
 * @description 
 */
public class UncontrolledFlooding {

	public static void main(String[] args) {

		final ActorSystem system = ActorSystem.create("system");
		final LoggingAdapter log = Logging.getLogger(system, "main");

		// Instantiate the actor 1 to 5
		final ActorRef actor1 = system.actorOf(Actor.createActor(), "a");
		final ActorRef actor2 = system.actorOf(Actor.createActor(), "b");
		final ActorRef actor3 = system.actorOf(Actor.createActor(), "c");
		final ActorRef actor4 = system.actorOf(Actor.createActor(), "d");
		final ActorRef actor5 = system.actorOf(Actor.createActor(), "e");

		// Instantiate the matrix
		Matrix matrix = new Matrix(new int[][] {{0, 1, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 0}});
		// infinite loop example
		// Matrix matrix = new Matrix(new int[][] {{0, 1, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {0, 1, 0, 0, 0}});

		for (int i = 0; i < 5; i++) {
			ArrayList<ActorRef> actorRefList = new ArrayList<ActorRef>();
			for (int j = 0; j < 5; j++) {
				if (matrix.m[i][j] == 1) {
					switch (j) {
						case 0:
							actorRefList.add(actor1);
							break;
						case 1:
							actorRefList.add(actor2);
							break;
						case 2:
							actorRefList.add(actor3);
							break;
						case 3:
							actorRefList.add(actor4);
							break;
						case 4:
							actorRefList.add(actor5);
							break;
					}
				}
			}
			switch (i) {
				case 0:
					actor1.tell(new RefList(actorRefList), ActorRef.noSender());
					break;
				case 1:
					actor2.tell(new RefList(actorRefList), ActorRef.noSender());
					break;
				case 2:
					actor3.tell(new RefList(actorRefList), ActorRef.noSender());
					break;
				case 3:
					actor4.tell(new RefList(actorRefList), ActorRef.noSender());
					break;
				case 4:
					actor5.tell(new RefList(actorRefList), ActorRef.noSender());
					break;
			}
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// send "start" to actor1
		actor1.tell("start", ActorRef.noSender());
		

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
