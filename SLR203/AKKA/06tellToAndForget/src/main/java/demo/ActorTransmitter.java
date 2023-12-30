package demo;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.StringRef;
import demo.Message.StringMessage;

public class ActorTransmitter extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	public ActorTransmitter() {}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(ActorTransmitter.class, () -> {
			return new ActorTransmitter();
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
        if (message instanceof StringRef) {
            log.info("["+getSelf().path().name()+"] received message from ["+ getSender().path().name() +"] : " + ((StringRef) message).s);

            StringMessage m = new StringMessage("hello");
            ((StringRef) message).actorRef.tell(m, this.getSender());
            log.info("["+getSelf().path().name()+"] send message to ["+  ((StringRef) message).actorRef.path().name() +"] : " + ((StringMessage) m).s);

        } else {
            log.info("("+getSelf().path().name()+") waiting for message !");
        }
            }

}
