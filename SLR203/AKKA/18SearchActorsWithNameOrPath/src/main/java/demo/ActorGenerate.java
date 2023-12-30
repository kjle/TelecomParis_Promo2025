package demo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.StringMessage;

public class ActorGenerate extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	public ActorGenerate() {}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(ActorGenerate.class, () -> {
			return new ActorGenerate();
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof String) {
            if (((String)message).equals("path")) {
                log.info("path: ["+getSelf().path()+"]");
            }
		}
    }

}
