package demo;

import java.util.ArrayList;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.RefList;
import demo.Message.StringMessage;

public class Actor extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private ArrayList<ActorRef> actorRef = new ArrayList<ActorRef>();
	private ArrayList<String> messageList = new ArrayList<String>();

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
			if (messageList.contains((Integer.parseInt((String)message)-1)+"")) {
				log.info("["+getSelf().path().name()+"] received "+(String)message+" from ["+getSender().path().name()+"]");
			} else {
				messageList.add((String)message);

				String lastMsg = messageList.get(messageList.size()-1);
				int cnt = Integer.parseInt(lastMsg);
				cnt += 1;
				String m = new String(cnt + "");
				for (ActorRef aref : actorRef) {
					aref.tell(m, getSelf());
				}
				log.info("["+getSelf().path().name()+"] received "+(String)message+" from ["+getSender().path().name()+"]");
			}
        } else {
			log.info("["+getSelf().path().name()+"] waiting for message !");
		}
    }

}
