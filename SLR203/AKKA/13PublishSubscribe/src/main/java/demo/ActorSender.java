package demo;

import java.time.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.JoinMessage;
import demo.Message.Ref1;
import demo.Message.StringMessage;
import demo.Message.UnjoinMessage;

public class ActorSender extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private ActorRef actorRef;

    private int msgCnt = 0;

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
            // log.info("Actor reference updated ! New reference is: {}", this.actorRef);

            // sending "join" message to the merger
            JoinMessage m = new JoinMessage();
            this.actorRef.tell(m,this.getSelf());

            // sending "go" message to scheduler
            getContext().system().scheduler().scheduleOnce(Duration.ofMillis(1000), getSelf(), "go", getContext().system().dispatcher(), ActorRef.noSender());
        
        } else if (message instanceof String) {
            log.info("["+getSelf().path().name()+"] received a message from ["+ getSender().path().name() +"] : " + message);
            if (((String) message).equals("go")) { // receive "go" message from scheduler
                if (msgCnt == 0) {
                    // sending "hi" message to the merger
                    msgCnt++;
                    StringMessage m = new StringMessage("hi");
                    this.actorRef.tell(m,this.getSelf());
                    log.info("["+getSelf().path().name()+"] sent message to ["+ this.actorRef.path().name() +"]");

                    // if is actor C send unjoin message to the merger
                    // if not, send "go" message to scheduler
                    if (getSelf().path().name().equals("c")) {
                        UnjoinMessage m2 = new UnjoinMessage();
                        this.actorRef.tell(m2,this.getSelf());
                        log.info("["+getSelf().path().name()+"] sent message to ["+ this.actorRef.path().name() +"]");
                    } else {
                        getContext().system().scheduler().scheduleOnce(Duration.ofMillis(1000), getSelf(), "go", getContext().system().dispatcher(), ActorRef.noSender());
                    }
                } else {
                    // sending "hi2" message to the merger
                    StringMessage m = new StringMessage("hi2");
                    this.actorRef.tell(m,this.getSelf());
                    log.info("["+getSelf().path().name()+"] sent message to ["+ this.actorRef.path().name() +"]");
                }

            } 
        }else if (message instanceof StringMessage) {
            log.info("["+getSelf().path().name()+"] received a message from ["+ getSender().path().name() +"] : " + ((StringMessage) message).s);

        } else {
            log.info("("+getSelf().path().name()+") waiting for message !");
        }
    }

}
