package demo;

import java.util.ArrayList;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.RefList;

public class Actor extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private ArrayList<ActorRef> actorRef = new ArrayList<ActorRef>();


	public Actor() {}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(Actor.class, () -> {
			return new Actor();
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
        if (message instanceof RefList) {
			actorRef = ((RefList)message).actorRefList;
			log.info("["+getSelf().path().name()+"] received RefList");
        } else if (message instanceof String) {
			if (((String)message).equals("show")) {
				log.info("["+getSelf().path().name()+"] ref list : "+actorRef.toString());
			} else {
            	log.info("["+getSelf().path().name()+"] received message : "+message);
			}
        }
    }

}
