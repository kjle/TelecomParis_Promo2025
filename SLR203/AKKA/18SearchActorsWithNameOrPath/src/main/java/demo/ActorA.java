package demo;

import java.time.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.CreateMessage;
import demo.Message.Ref1;
import demo.Message.StringMessage;

public class ActorA extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private ActorRef actorRef;

    private int id = 0;

	public ActorA() {}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(ActorA.class, () -> {
			return new ActorA();
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
        if (message instanceof CreateMessage) {
            id++;
            this.getContext().actorOf(Props.create(ActorGenerate.class), "actor"+id);
            log.info("["+getSelf().path().name()+"] created [actor" + id +"]");
        } else if (message instanceof String) {
            if (((String)message).equals("path")) {
                log.info("path: ["+getSelf().path()+"]");
            }
        }
    }

}
