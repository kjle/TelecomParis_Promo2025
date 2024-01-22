package demo;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import demo.Message.StoreMessage;
import demo.Message.RetrieveMessage;
import demo.Message.AddNodeMessage;
import demo.Message.ResultMessage;

public class NodeActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	private final String id;
    // The simulated data store for this node
    private final Map<String, String> dataStore = new HashMap<>();
	// Routing table
	private final List<Bucket> routingTable;


    // Constructor
    public NodeActor(String id, int bucketSize, int keySpaceSize) {
		// bucketSize is the number of nodes in each bucket
		// keySpaceSize is the number of buckets in the routing table
		this.id = id;
		this.routingTable = initializeRoutingTable(bucketSize, keySpaceSize);
    }

	// Initialize routing table
	private List<Bucket> initializeRoutingTable(int bucketSize, int keySpaceSize) {
		List<Bucket> table = new ArrayList<>();

		for (int i = 0; i < keySpaceSize; i++) {
			table.add(new Bucket(bucketSize));
		}

		return table;
	}

    // Static method to create Props
    public static Props createActor(String id, int bucketSize, int keySpaceSize) {
        return Props.create(NodeActor.class, () -> {
			return new NodeActor(id, bucketSize, keySpaceSize);
		});
    }

	public String getId() {
		return id;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeActor node = (NodeActor) o;
        return id.equals(node.getId());
    }

	@Override
    public int hashCode() {
        int result = id.hashCode();
        return result;
    }

	@Override
    public String toString() {
        return "Node{" +
               "id='" + id + '\'' +
			   "hash='" + hashCode() + '\'' +
               '}';
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(StoreMessage.class, this::handleStoreMessage)
            .match(RetrieveMessage.class, this::handleRetrieveMessage)
			.match(AddNodeMessage.class, this::handleAddNodeMessage)
            .matchAny(o -> log.info("Received unknown message"))
            .build();
    }

    private void handleStoreMessage(StoreMessage message) {
        dataStore.put(message.getKey(), message.getValue());
        log.info("Stored data with key: {}", message.getKey());
    }

    private void handleRetrieveMessage(RetrieveMessage message) {
        String value = dataStore.getOrDefault(message.key, "Not Found");
        getSender().tell(new ResultMessage(message.key, value), getSelf());
        log.info("Retrieved data for key: {}", message.key);
    }

	private void handleAddNodeMessage(AddNodeMessage message) {
		// Add the node to the routing table
		int bucketIndex = getBucketIndex(message.id);
		Bucket bucket = routingTable.get(bucketIndex);
		bucket.addNode(message.node, success -> {
			if (success) {
				log.info("Node {} added to bucket {}", message.node, bucketIndex);
			} else {
				log.info("Node {} not added. Bucket {} is full.", message.node, bucketIndex);
			}
		});
	}

	private int getBucketIndex(String id) {
		String xorRes = xorString(this.id, id);
		

		return 0;
	}

	private String xorString(String s1, String s2) {
		int maxLength = Math.max(s1.length(), s2.length());
		s1 = String.format("%" + maxLength + "s", s1).replace(' ', '0');
		s2 = String.format("%" + maxLength + "s", s2).replace(' ', '0');
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < maxLength; i++) {
			if (s1.charAt(i) == s2.charAt(i)) {
				result.append('0');
			} else {
				result.append('1');
			}
		}
		return result.toString();
	}

}
