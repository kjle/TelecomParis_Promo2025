package demo;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.Message.NodeMessage;

public class MessageActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    // Static method to create Props for this actor
    public static Props createActor() {
        return Props.create(MessageActor.class, () -> {
			return new MessageActor();
		});
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(NodeMessage.class, this::handleNodeMessage)
            .matchAny(o -> log.info("Received unknown message"))
            .build();
    }

    private void handleNodeMessage(NodeMessage message) {
        // Logic to determine the target node for the message
        ActorRef targetNode = determineTargetNode(message);
        
        // Forward the message to the determined target node
        if (targetNode != null) {
            targetNode.tell(message.content, getSender());
        } else {
            log.info("Target node not found for message");
        }
    }

    private ActorRef determineTargetNode(NodeMessage message) {
        // Implement the logic to determine the appropriate node
        // This would typically involve looking at the message content and deciding
        // which node in the DHT should handle it
        return null; // Placeholder for actual logic
    }

}
