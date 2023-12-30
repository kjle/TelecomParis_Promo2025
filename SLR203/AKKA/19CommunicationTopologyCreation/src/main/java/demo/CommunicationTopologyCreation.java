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
 * @description creates a communication topology
 */
public class CommunicationTopologyCreation {

	public static void main(String[] args) {

		final ActorSystem system = ActorSystem.create("system");
		final LoggingAdapter log = Logging.getLogger(system, "main");

		// Instantiate the actor 1 to 4
		final ActorRef actor1 = system.actorOf(Actor.createActor(), "actor1");
		final ActorRef actor2 = system.actorOf(Actor.createActor(), "actor2");
		final ActorRef actor3 = system.actorOf(Actor.createActor(), "actor3");
		final ActorRef actor4 = system.actorOf(Actor.createActor(), "actor4");

		// Instantiate the matrix
		Matrix matrix = new Matrix(new int[][] {{0, 1, 1, 0}, {0, 0, 0, 1}, {1, 0, 0, 1}, {1, 0, 0, 1}});

		for (int i = 0; i < 4; i++) {
			ArrayList<ActorRef> actorRefList = new ArrayList<ActorRef>();
			for (int j = 0; j < 4; j++) {
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
			}
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// verify the communication topology
		actor1.tell("show", ActorRef.noSender());
		actor2.tell("show", ActorRef.noSender());
		actor3.tell("show", ActorRef.noSender());
		actor4.tell("show", ActorRef.noSender());

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
