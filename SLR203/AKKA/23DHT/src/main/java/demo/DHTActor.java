package demo;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.ArrayList;
import java.util.List;
import demo.Message.AddNodeMessage;

public class DHTActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    // List to keep track of node actors in the DHT
    private final List<ActorRef> nodes = new ArrayList<>();
    private final List<Bucket> routingTable;

    // Constructor
    public DHTActor(int bucketSize, int keySpaceSize) {
        this.routingTable = initializeRoutingTable(bucketSize, keySpaceSize);
        // ... [other initializations]
    }

    // Static method to create Props for this actor
    public static Props createActor() {
        return Props.create(DHTActor.class, () -> {
			return new DHTActor(2, 8);
		});
    }

    private List<Bucket> initializeRoutingTable(int bucketSize, int keySpaceSize) {
        List<Bucket> table = new ArrayList<>();

        for (int i = 0; i < keySpaceSize; i++) {
            table.add(new Bucket(bucketSize));
        }

        return table;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(AddNodeMessage.class, this::handleAddNode)
            .matchAny(o -> log.info("Received unknown message"))
            .build();
    }

    private void handleAddNode(AddNodeMessage message) {
        nodes.add(message.node);
        log.info("Node added to the DHT: {}", message.node);
        // Additional logic to integrate the node into the DHT (e.g., updating routing tables)
        int bucketIndex = getBucketIndex(newNode.getId());
        Bucket bucket = routingTable.get(bucketIndex);
        bucket.addNode(newNode, success -> {
            if (success) {
                log.info("Node {} added to bucket {}", newNode, bucketIndex);
            } else {
                log.info("Node {} not added. Bucket {} is full.", newNode, bucketIndex);
            }
        });
    }

}
