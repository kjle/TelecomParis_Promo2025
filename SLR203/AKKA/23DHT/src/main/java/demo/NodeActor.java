package demo;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import demo.Message.StoreMessage;
import demo.Message.FindNodeMessage;
import demo.Message.FindValueMessage;

public class NodeActor extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	private final int nodeId;
	private final Map<Integer, ActorRef> routingTree;
	private final Map<Integer, String> keyMap; // key-value pair, key is the hash value of the key string


	public NodeActor(int nodeId) {
		this.nodeId = nodeId;
		this.routingTree = new HashMap<Integer, ActorRef>();
		this.keyMap = new HashMap<Integer, String>();
	}

	// Static function creating actor
	public static Props createActor(int nodeId) {
		return Props.create(NodeActor.class, () -> {
			return new NodeActor(nodeId);
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof StoreMessage) {
			StoreMessage m = (StoreMessage) message;
			keyMap.put(m.key, m.value);
			log.info("["+getSelf().path().name()+"] stored key ("+m.key+") value ("+m.value+")");
		} else if (message instanceof FindNodeMessage) {
			FindNodeMessage m = (FindNodeMessage) message;
			log.info("["+getSelf().path().name()+"] received FindNodeMessage");
		} else if (message instanceof FindValueMessage) {
			FindValueMessage m = (FindValueMessage) message;
			log.info("["+getSelf().path().name()+"] received FindValueMessage");
		} else {
			log.info("["+getSelf().path().name()+"] waiting for message !");
		}




        if (message instanceof RefList) {
			actorRef = ((RefList)message).actorRefList;
			log.info("["+getSelf().path().name()+"] received RefList");
        } else if (message instanceof String) {
			if (messageList.contains((String)message)) {
				log.info("["+getSelf().path().name()+"] received "+(String)message+" from ["+getSender().path().name()+"]");
			} else {
				messageList.add((String)message);

				String m = new String(messageList.get(messageList.size()-1));
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
