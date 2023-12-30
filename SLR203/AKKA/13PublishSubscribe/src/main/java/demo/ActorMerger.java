package demo;


import java.util.ArrayList;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.StringMessage;
import demo.Message.JoinMessage;
import demo.Message.UnjoinMessage;

public class ActorMerger extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private ArrayList<ActorRef> actorSenderList = new ArrayList<ActorRef>();
    private ArrayList<ActorRef> actorReceiverList = new ArrayList<ActorRef>();
    private ArrayList<String> msgList = new ArrayList<String>();

	public ActorMerger() {}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(ActorMerger.class, () -> {
			return new ActorMerger();
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
        if (message instanceof ActorRef) {
            // if receive actor reference, add it to the list
            this.actorReceiverList.add((ActorRef) message);
            log.info("["+getSelf().path().name()+"] add ["+ ((ActorRef)message).path().name() +"] to the receiver list");
        } else if (message instanceof JoinMessage) {
            this.actorSenderList.add(getSender());
            log.info("["+getSelf().path().name()+"] add ["+ getSender().path().name() +"] to the sender list");
        } else if (message instanceof UnjoinMessage) {
            this.actorSenderList.remove(getSender());
            log.info("["+getSelf().path().name()+"] remove ["+ getSender().path().name() +"] from the sender list");
        } else if (message instanceof StringMessage) {
            log.info("["+getSelf().path().name()+"] received a message from ["+ getSender().path().name() +"] : " + ((StringMessage) message).s);
            msgList.add(((StringMessage) message).s);
            // log.info("["+getSelf().path().name()+"] : size of msgList is " + msgList.size() + ", size of actorSenderList is " + actorSenderList.size());

            if (msgList.size() == actorSenderList.size()) {
                StringMessage m = new StringMessage(msgList.get(0));
                while (msgList.size() != 0) {
                    msgList.remove(0);
                }
                for (ActorRef actorReceiver : actorReceiverList) {
                    actorReceiver.tell(m, this.getSelf());
                    log.info("["+getSelf().path().name()+"] sent message to ["+ actorReceiver.path().name() +"] : " + m.s);
                }
            }
        } else {
            log.info("("+getSelf().path().name()+") waiting for message !");
        }
    }

}
