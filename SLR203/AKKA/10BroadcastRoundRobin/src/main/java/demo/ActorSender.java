package demo;

import java.time.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.Ref1;
import demo.Message.StringMessage;

public class ActorSender extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private ActorRef actorRef;

	public ActorSender() {}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(ActorSender.class, () -> {
			return new ActorSender();
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
        if (message instanceof Ref1) {
            this.actorRef = ((Ref1) message).actorRef;
            log.info("["+getSelf().path().name()+"] received message from ["+ getSender().path().name() +"]");
            log.info("Actor reference updated ! New reference is: {}", this.actorRef);

            getContext().system().scheduler().scheduleOnce(Duration.ofMillis(1000), getSelf(), "go", getContext().system().dispatcher(), ActorRef.noSender());

        } else if (message instanceof String) {
            log.info("["+getSelf().path().name()+"] received a message from ["+ getSender().path().name() +"] : " + message);
            if (((String) message).equals("go")) { // Use equals() to compare strings
                StringMessage m = new StringMessage("broadcaster content");
                this.actorRef.tell(m,this.getSelf());
            } 
        }else if (message instanceof StringMessage) {
            log.info("["+getSelf().path().name()+"] received a message from ["+ getSender().path().name() +"] : " + ((StringMessage) message).s);

        } else {
            log.info("("+getSelf().path().name()+") waiting for message !");
        }
    }

}
