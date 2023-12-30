package demo;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class MyActor extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	public MyActor() {}

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
	public void onReceive(Object message) throws Throwable {
		if(message instanceof MyMessage){
			MyMessage m = (MyMessage) message;
			log.info("["+getSelf().path().name()+"] received message from ["+ getSender().path().name() +"] with data: ["+m.data+"]");
		}
	}

}
