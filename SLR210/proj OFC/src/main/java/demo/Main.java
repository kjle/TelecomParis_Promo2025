/**
 * @file Main.java
 * @brief Main file of the project containing the main method
*/
package demo;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;

import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;
import java.util.Collections;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @class Main
 * @brief Main class of the project, which serves as a sender to send messages to the actors
*/
public class Main {

	static int N; // Number of actors
	static int LEADER_ELECTION_TIMEOUT;// Timeout for leader election, t_le
	static int CRASH_NUMBER; // Number of actors to crash
	static double CRASH_PROBABILITY; // Probability of crashing, alpha
	static int BOUND_OF_PROPOSED_NUMBER; // Bound of the proposed number (exclusive)
	static int ABORT_TIMEOUT; // Timeout for abort
	final static String FLAG = "DEBUG";

    public static void main (String[] args) {

		String paramFile = "param.txt";
		try (BufferedReader reader = new BufferedReader(new FileReader(paramFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("=");
				if (parts.length == 2) {
					switch(parts[0].trim()) {
						case "N":
							N = Integer.parseInt(parts[1].trim());
							break;
						case "LEADER_ELECTION_TIMEOUT":
							LEADER_ELECTION_TIMEOUT = Integer.parseInt(parts[1].trim());
							break;
						case "CRASH_NUMBER":
							CRASH_NUMBER = Integer.parseInt(parts[1].trim());
							break;
						case "CRASH_PROBABILITY":
							CRASH_PROBABILITY = Double.parseDouble(parts[1].trim());
							break;
						case "BOUND_OF_PROPOSED_NUMBER":
							BOUND_OF_PROPOSED_NUMBER = Integer.parseInt(parts[1].trim());
							break;
						case "ABORT_TIMEOUT":
							ABORT_TIMEOUT = Integer.parseInt(parts[1].trim());
							break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		        
        final ActorSystem system = ActorSystem.create("system");
		final ActorRef[] actors = new ActorRef[N];

		switch(FLAG) {
			case "DEBUG":
				system.getEventStream().setLogLevel(Logging.DebugLevel());
				break;
			case "INFO":
				system.getEventStream().setLogLevel(Logging.InfoLevel());
				break;
			default:
				system.getEventStream().setLogLevel(Logging.InfoLevel());
				break;
		}


		for (int i = 1; i <= N; i++) {
			actors[i-1] = system.actorOf(Process.createActor(i), "Actor" + i);
		}

		ActorinfoMessage actorinfoMessage = new ActorinfoMessage(actors);

		for (int i = 0; i < N; i++) {
			actors[i].tell(actorinfoMessage, ActorRef.noSender());
		}

		LaunchMessage launchMessage = new LaunchMessage();
		for (int i = 0; i < N; i++) {
			actors[i].tell(launchMessage, ActorRef.noSender());
		}

		List<Integer> crashList = new ArrayList<>();
		for (int i = 0; i < N;i++) {
			crashList.add(i);
		}
		Collections.shuffle(crashList);

		// Send CRASH messages to the actors in the crash list
		for (int i = 0; i < CRASH_NUMBER; i++) {
			actors[crashList.get(i)].tell(new CrashMessage(), ActorRef.noSender());
		}

		// The list is shuffled, so we can just set the leader with the first actor not in the crash list

		// We now send HOLD message to every other process
		for (int i = 0; i < N; i++) {
			if (i != CRASH_NUMBER) {
				system.scheduler().scheduleOnce(Duration.create(LEADER_ELECTION_TIMEOUT, TimeUnit.MILLISECONDS), actors[crashList.get(i)], new HoldMessage(), system.dispatcher(), null);
			}
		}

        try {
			waitBeforeTerminate();
		} catch (InterruptedException E) {
			E.printStackTrace();
		} finally {
			system.terminate();
		}
    }

    public static void waitBeforeTerminate() throws InterruptedException {
		Thread.sleep(10000);
	}

	/**
	 * @class ActorinfoMessage
	 * @brief Message containing the list of actors
	*/
    static public class ActorinfoMessage {
		public ActorRef[] actors;
		public int length;
		public double crashProbability;
		public int boundOfProposedNumber;
		public int abortTimeout;

		public ActorinfoMessage(ActorRef[] actors) {
			this.actors = actors;
			this.length = actors.length;
			this.crashProbability = CRASH_PROBABILITY;
			this.boundOfProposedNumber = BOUND_OF_PROPOSED_NUMBER;
			this.abortTimeout = ABORT_TIMEOUT;
		}
	}

	/**
	 * @class LaunchMessage
	 * @brief Message to launch the actors
	*/
	static public class LaunchMessage {
		public LaunchMessage() {
		}
	}

	/**
	 * @class CrashMessage
	 * @brief Message to tell the actors to crash at certain point
	*/
	static public class CrashMessage {
		public CrashMessage() {
		}
	}

	static public class HoldMessage {
		public HoldMessage() {
		}
	}

	static public class ReadMessage {
		public int ballot;
		public ReadMessage(int ballot) {
			this.ballot = ballot;
		}
	}

	static public class AbortMessage {
		public int ballot;
		public AbortMessage(int ballot) {
			this.ballot = ballot;
		}
	}

	static public class GatherMessage {
		public int ballot;
		public int imposeBallot;
		public int estimate;
		public GatherMessage(int ballot, int imposeBallot, int estimate) {
			this.ballot = ballot;
			this.imposeBallot = imposeBallot;
			this.estimate = estimate;
		}
	}
	
	static public class ImposeMessage {
		public int ballot;
		public int proposal;
		public ImposeMessage(int ballot, int proposal) {
			this.ballot = ballot;
			this.proposal = proposal;
		}
	}

	static public class ACKMessage {
		public int ballot;
		public ACKMessage(int ballot) {
			this.ballot = ballot;
		}
	}

	static public class DecideMessage {
		public int proposal;
		public DecideMessage(int proposal) {
			this.proposal = proposal;
		}
	}
}