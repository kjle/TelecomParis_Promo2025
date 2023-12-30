package demo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.Ref2;
import demo.Message.StringRef;
import demo.Message.StringMessage;

public class ActorA extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private ActorRef actorRef1, actorRef2;

	public ActorA() {}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(ActorA.class, () -> {
			return new ActorA();
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
        if (message instanceof Ref2) {
            this.actorRef1 = ((Ref2) message).actorRef1;
            this.actorRef2 = ((Ref2) message).actorRef2;
            log.info("["+getSelf().path().name()+"] received message from ["+ getSender().path().name() +"]");
            log.info("Actor reference updated ! New references are: {}, {}", this.actorRef1, this.actorRef2);
        } else if (message instanceof StringMessage) {
            if (((StringMessage) message).s.equals("start")) { // Use equals() to compare strings
                StringRef m = new StringRef("hello", this.actorRef2);
                log.info("["+getSelf().path().name()+"] received a message from ["+ getSender().path().name() +"] : " + ((StringMessage) message).s);

                actorRef1.tell(m, this.getSelf());
                log.info("["+getSelf().path().name()+"] send message to ["+ actorRef1.path().name() +"] : " + ((StringRef) m).s);
            } else {
                log.info("["+getSelf().path().name()+"] received a message from ["+ getSender().path().name() +"] : " + ((StringMessage) message).s);
            }
        } else {
            log.info("("+getSelf().path().name()+") waiting for message !");
        }
            }

}
