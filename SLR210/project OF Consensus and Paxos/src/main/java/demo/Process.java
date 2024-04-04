/**
 * @file Process.java
 * @brief File containing the implementation of the actors
*/
package demo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Random;

import demo.Main.*;

/**
 * @class Process
 * @brief Class representing the actor
*/
public class Process extends AbstractActor {
	static double CRASH_PROBABILITY; // Probability of crashing, alpha
	static int BOUND_OF_PROPOSED_NUMBER; // Bound of the proposed number (exclusive)
	static int ABORT_TIMEOUT; // Timeout for abort

	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private int id, N;
	private ActorRef[] actors;
	private boolean launched = false, shouldCrash = false, crashed = false, hold = false, decided = false;

	private int ballot, proposal, readBallot, imposeBallot, estimate;
	private int maxAbortBallot = Integer.MIN_VALUE;
	private Pair[] states;

	private int receivedStates = 0;
	private boolean biggerThanHalf = false;

	private int ACKnum = 0;
	private boolean ACKconfirmed = false;

	private int proposeResult = -2;

	private long startTime = 0;
	private long endTime = 0;

	/**
	 * @brief Initializes the actor
	 * @param id The unique identifier of the actor
	*/
	public Process(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public int getProposeResult() {
		return proposeResult;
	}

	/**
	 * @brief Static function creating actor
	 * @param id The unique identifier of the actor
	 * @return
	*/
	public static Props createActor(int id) {
		return Props.create(Process.class, () -> {
			return new Process(id);
		});
	}

	/**
	 * @brief Creates the actor's behavior
	 * @return
	*/
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(ActorinfoMessage.class, this::receiveActorinfoMessage)
			.match(LaunchMessage.class, this::receiveLaunchMessage)
			.match(CrashMessage.class, this::receiveCrashMessage)
			.match(HoldMessage.class, this::receiveHoldMessage)
			.match(ReadMessage.class, this::receiveReadMessage)
			.match(AbortMessage.class, this::receiveAbortMessage)
			.match(GatherMessage.class, this::receiveGatherMessage)
			.match(ImposeMessage.class, this::receiveImposeMessage)
			.match(ACKMessage.class, this::receiveACKMessage)
			.match(DecideMessage.class, this::receiveDecideMessage)
			.build();
	}

	/**
	 * @brief Proposes a value
	 * @param v The value to propose
	*/
	void propose (int v) {
		if (crashed) return;
		if (shouldCrash) {
			double r = Math.random();
			if (r < CRASH_PROBABILITY) {
				log.debug("["+getSelf().path().name()+"] crashed");
				crashed = true;
			}
		}
		if (!crashed) {
			proposal = v;
			ballot += N;
			for (int i = 0; i < N; i++) {
				states[i].first = 0;
				states[i].second = 0;
			}
			ReadMessage readMessage = new ReadMessage(ballot);
			for (int i = 0; i < N; i++) {
				actors[i].tell(readMessage, getSelf());
			}
			log.debug("["+getSelf().path().name()+"] proposed (value ["+v+"], ballot ["+(ballot)+"])");
		}
	}

	public void receiveActorinfoMessage (ActorinfoMessage m) {
		log.debug("["+getSelf().path().name()+"] received ACTORINFO from ["+ getSender().path().name() +"]");
		this.actors = m.actors;
		this.N = m.length;
		this.CRASH_PROBABILITY = m.crashProbability;
		this.BOUND_OF_PROPOSED_NUMBER = m.boundOfProposedNumber;
		this.ABORT_TIMEOUT = m.abortTimeout;
		ballot = id - N;
		proposal = 0;
		readBallot = 0;
		imposeBallot = id - N;
		estimate = 0;
		states = new Pair[N];
		for (int i = 0; i < N; i++) {
			states[i] = new Pair(0, 0);
		}
		crashed = false;
		hold = false;
		receivedStates = 0;
	}

	public void receiveLaunchMessage (LaunchMessage m) {
		log.debug("["+getSelf().path().name()+"] received LAUNCH from ["+ getSender().path().name() +"]");
		if (!this.launched) {
			this.launched = true;
			startTime = System.currentTimeMillis();
			Random rand = new Random();
			int proposedNumber = rand.nextInt(BOUND_OF_PROPOSED_NUMBER);

			// Propose a value
			propose(proposedNumber);
		}
	}

	public void receiveCrashMessage (CrashMessage m) {
		log.debug("["+getSelf().path().name()+"] received CRASH from ["+ getSender().path().name() +"]");
		this.shouldCrash = true;
	}

	public void receiveHoldMessage (HoldMessage m) {
		log.debug("["+getSelf().path().name()+"] received HOLD from ["+ getSender().path().name() +"]");
		hold = true;
	}

	public void receiveReadMessage (ReadMessage m) {
		if (crashed) return;
		log.debug("["+getSelf().path().name()+"] received READ from ["+ getSender().path().name() +"], ballot ["+m.ballot+"]");
		if (proposeResult >= 0) return;
		if (shouldCrash) {
			double r = Math.random();
			if (r < CRASH_PROBABILITY) {
				log.debug("["+getSelf().path().name()+"] crashed");
				crashed = true;
			}
		}
		if (!crashed) {
			if (readBallot > m.ballot || imposeBallot > m.ballot) {
				getSender().tell(new AbortMessage(m.ballot), getSelf());
			}
			else {
				readBallot = m.ballot;
				getSender().tell(new GatherMessage(m.ballot, imposeBallot, estimate), getSelf());
			}
		}
	}

	public void receiveAbortMessage (AbortMessage m) {
		if (crashed) return;
		log.debug("["+getSelf().path().name()+"] received ABORT from ["+ getSender().path().name() +"], ballot ["+m.ballot+"], maxAbortBallot ["+maxAbortBallot+"]");
		if (shouldCrash) {
			double r = Math.random();
			if (r < CRASH_PROBABILITY) {
				log.debug("["+getSelf().path().name()+"] crashed");
				crashed = true;
			}
		}
		if (!crashed) {
			proposeResult = -1;
			if (!hold) {
				if (!decided && m.ballot > maxAbortBallot) {
					log.debug("["+getSelf().path().name()+"] RE-PROPOSE, ballot ["+m.ballot+"], maxAbortBallot ["+maxAbortBallot+"]");
					maxAbortBallot = m.ballot;
					propose(proposal);
				}
			}
		}
	}

	public void receiveGatherMessage (GatherMessage m) {
		if (crashed) return;
		log.debug("["+getSelf().path().name()+"] received GATHER from ["+ getSender().path().name() +"], (ballot ["+m.ballot+"], imposeBallot ["+m.imposeBallot+"], estimate ["+m.estimate+"])");
		if (proposeResult >= 0) return;
		if (shouldCrash) {
			double r = Math.random();
			if (r < CRASH_PROBABILITY) {
				log.debug("["+getSelf().path().name()+"] crashed");
				crashed = true;
			}
		}
		if (!crashed) {
			states[(m.ballot + N) % N].first = m.estimate;
			states[(m.ballot + N) % N].second = m.imposeBallot;
			receivedStates++;
			if (receivedStates > N/2 && !biggerThanHalf) {
				biggerThanHalf = true;
				int maxidx = -1;
				for (int i = 0; i < N; i++) {
					if (states[i].second > 0) {
						if (maxidx == -1 || states[i].second > states[maxidx].second) {
							maxidx = i;
						}
					}
				}
				if (maxidx != -1) proposal = states[maxidx].first;
				for (int i = 0; i < N; i++) {
					states[i].first = 0;
					states[i].second = 0;
				}
				receivedStates = 0;
				biggerThanHalf = false;
				for (int i = 0; i < N; i++) {
					actors[i].tell(new ImposeMessage(ballot, proposal), getSelf());
				}
			}
		}
	}

	public void receiveImposeMessage (ImposeMessage m) {
		if (crashed) return;
		log.debug("["+getSelf().path().name()+"] received IMPOSE from ["+ getSender().path().name() +"], (ballot ["+m.ballot+"], proposal ["+m.proposal+"])");
		if (proposeResult >= 0) return;
		if (shouldCrash) {
			double r = Math.random();
			if (r < CRASH_PROBABILITY) {
				log.debug("["+getSelf().path().name()+"] crashed");
				crashed = true;
			}
		}
		if (!crashed) {
			if (readBallot > m.ballot || imposeBallot > m.ballot) {
				getSender().tell(new AbortMessage(m.ballot), getSelf());
			}
			else {
				estimate = m.proposal;
				imposeBallot = m.ballot;
				getSender().tell(new ACKMessage(m.ballot), getSelf());
			}
		}
	}

	public void receiveACKMessage (ACKMessage m) {
		if (crashed) return;
		log.debug("["+getSelf().path().name()+"] received ACK from ["+ getSender().path().name() +"], ballot ["+m.ballot+"]");
		if (proposeResult >= 0) return;
		if (shouldCrash) {
			double r = Math.random();
			if (r < CRASH_PROBABILITY) {
				log.debug("["+getSelf().path().name()+"] crashed");
				crashed = true;
			}
		}
		if (!crashed) {
			ACKnum++;
			if (ACKnum > N/2 && !ACKconfirmed) {
				ACKconfirmed = true;
				endTime = System.currentTimeMillis();
				decided = true;
				log.info("/!\\ Total time for the Process ["+id+"] to decide (value ["+proposal+"] ballot ["+ballot+"]): " + (endTime - startTime) + "ms");
				for (int i = 0; i < N; i++) {
					actors[i].tell(new DecideMessage(proposal), getSelf());
				}
			}
		}
	}

	public void receiveDecideMessage (DecideMessage m) {
		if (crashed) return;
		if (shouldCrash) {
			double r = Math.random();
			if (r < CRASH_PROBABILITY) {
				log.debug("["+getSelf().path().name()+"] crashed");
				crashed = true;
			}
		}
		if (!crashed) {
			proposeResult = m.proposal;
			this.decided = true;
		}
		log.info("/!\\ ["+getSelf().path().name()+"] received DECIDE from ["+ getSender().path().name() +"], proposal ["+proposeResult+"]");
	}

	public class Pair {
		public int first, second;

		public Pair(int first, int second) {
			this.first = first;
			this.second = second;
		}
	}
}
