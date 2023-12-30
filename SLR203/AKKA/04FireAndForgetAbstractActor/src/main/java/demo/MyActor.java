package demo;

import akka.actor.Props;
import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class MyActor extends AbstractActor {

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	public MyActor() {
	}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(MyActor.class, () -> {
			return new MyActor();
		});
	}

	static public class MyMessage {
		public final String data;

		public MyMessage(String data) {
			this.data = data;
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(MyMessage.class, this::receiveFunction)
			.build();
	  }

	  public void receiveFunction(MyMessage m){
		log.info("["+getSelf().path().name()+"] received message from ["+ getSender().path().name() +"] with data: ["+m.data+"]");
	  }


}
