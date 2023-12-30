package demo;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.StringMessage;

public class ActorB extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	public ActorB() {}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(ActorB.class, () -> {
			return new ActorB();
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
        if (message instanceof StringMessage) {
            log.info("["+getSelf().path().name()+"] received message from ["+ getSender().path().name() +"] : " + ((StringMessage) message).s);

            StringMessage m = new StringMessage("hi!");
            this.getSender().tell(m, this.getSelf());
            log.info("["+getSelf().path().name()+"] sent message to ["+ getSender().path().name() +"] : " + ((StringMessage) m).s);
        } else {
            log.info("("+getSelf().path().name()+") waiting for message !");
        }
            }

}
