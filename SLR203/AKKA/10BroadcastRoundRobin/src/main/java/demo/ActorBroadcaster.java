package demo;


import java.util.ArrayList;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.StringMessage;

public class ActorBroadcaster extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private ArrayList<ActorRef> actorRefList = new ArrayList<ActorRef>();

	public ActorBroadcaster() {}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(ActorBroadcaster.class, () -> {
			return new ActorBroadcaster();
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
        if (message instanceof StringMessage) {
            log.info("["+getSelf().path().name()+"] received message from ["+ getSender().path().name() +"]");
            if (((StringMessage) message).s.equals("join")) { // Use equals() to compare strings
                this.actorRefList.add(getSender());
                log.info("["+getSelf().path().name()+"] add ["+ getSender().path().name() +"] to the list");
            } else {
                for (ActorRef actorRef : actorRefList) {
                    log.info("["+getSelf().path().name()+"] send message to ["+ actorRef.path().name() +"]");
                    actorRef.tell(message, this.getSender());
                }
            }
        } else {
            log.info("("+getSelf().path().name()+") waiting for message !");
        }
    }

}
