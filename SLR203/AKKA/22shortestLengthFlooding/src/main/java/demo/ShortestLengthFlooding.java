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
public class ShortestLengthFlooding {

	public static void main(String[] args) {

		final ActorSystem system = ActorSystem.create("system");
		final LoggingAdapter log = Logging.getLogger(system, "main");
		final ArrayList<String> actorNameList = new ArrayList<String>();
		
		Matrix matrix = new Matrix(new int[][] {
			{0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 
			{1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, 
			{1, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}, 
			{1, 1, 1, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 
			{1, 1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 
			{1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, 
			{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, 
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, 
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, 
			{0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, 
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}, 
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, 
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, 
			{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, 
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}
		});

		actorNameList.add("a");
		actorNameList.add("b");
		actorNameList.add("c");
		actorNameList.add("d");
		actorNameList.add("e");
		actorNameList.add("f");
		actorNameList.add("g");
		actorNameList.add("h");
		actorNameList.add("i");
		actorNameList.add("j");
		actorNameList.add("k");
		actorNameList.add("l");
		actorNameList.add("m");
		actorNameList.add("n");
		actorNameList.add("o");
		actorNameList.add("p");
		actorNameList.add("q");
		actorNameList.add("r");
		
		final ArrayList<ActorRef> actors = new ArrayList<ActorRef>();
		for (String name : actorNameList) {
			actors.add(system.actorOf(Actor.createActor(), name));
		}

		for (int i = 0; i < actors.size(); i++) {
			ArrayList<ActorRef> actorRefList = new ArrayList<ActorRef>();
			for (int j = 0; j < actors.size(); j++) {
				if (matrix.m[i][j] == 1) {
					actorRefList.add(actors.get(j));
				}
			}
			actors.get(i).tell(new RefList(actorRefList), ActorRef.noSender());
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// send "0" to actor1
		actors.get(0).tell("0", ActorRef.noSender());
		// actors.get(0).tell("1", ActorRef.noSender());
		// actors.get(0).tell("2", ActorRef.noSender());
		
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
