package demo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.Ref1;
import demo.Message.StringMessage;

public class ActorReceiver extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private ActorRef actorRef;

	public ActorReceiver() {}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(ActorReceiver.class, () -> {
			return new ActorReceiver();
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
        if (message instanceof Ref1) {
            this.actorRef = ((Ref1) message).actorRef;
            log.info("["+getSelf().path().name()+"] received message from ["+ getSender().path().name() +"]");
            log.info("Actor reference updated ! New reference is: {}", this.actorRef);

            // Send a message to the broadcaster
            StringMessage m = new StringMessage("join");
            this.actorRef.tell(m,this.getSelf());
            log.info("["+getSelf().path().name()+"] send message to ["+ actorRef.path().name() +"] : " + ((StringMessage) m).s);

        } else if (message instanceof StringMessage) {
            log.info("["+getSelf().path().name()+"] received a message from ["+ getSender().path().name() +"] : " + ((StringMessage) message).s);
        } else {
            log.info("("+getSelf().path().name()+") waiting for message !");
        }
    }

}
