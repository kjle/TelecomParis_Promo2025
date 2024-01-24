package demo;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import demo.Message.StoreMessage;
import demo.Message.FindValueMessage;
import demo.Message.AddNodeMessage;
import demo.Message.FindNodeMessage;
import demo.Message.ResponseFindNodeMessage;
import demo.Message.PrintRoutingTableMessage;

public class NodeActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	private final int id;
	private final int nodeIdLength = 6; // The length of the node id in bits, cause we have 8 nodes
    // The simulated data store for this node
    private final Map<String, String> dataStore = new HashMap<>();
	// Routing table
	private final List<Bucket> routingTable;


    // Constructor
    public NodeActor(int id, int bucketSize, int keySpaceSize) {
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
    public static Props createActor(int id, int bucketSize, int keySpaceSize) {
        return Props.create(NodeActor.class, () -> {
			return new NodeActor(id, bucketSize, keySpaceSize);
		});
    }

	public int getId() {
		return id;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeActor node = (NodeActor) o;
		return id == node.getId();
    }

	@Override
    public int hashCode() {
		int result = Integer.hashCode(id);
        return result;
    }

	@Override
    public String toString() {
        return "Node{" +
               "id='" + id + '\'' +
			   "hash='" + hashCode() + '\'' +
			   "routingTable='" + printRoutingTable() + '\'' +
               '}';
    }

	private String printRoutingTable() {
		String result = "";
		for (int i = 0; i < routingTable.size(); i++) {
			result += "Bucket " + i + ": ";
			for (int nodeId : routingTable.get(i).getNodes()) {
				result += nodeId + " ";
			}
			result += "\n";
		}
		return result;
	}

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(StoreMessage.class, this::handleStoreMessage)
            .match(FindValueMessage.class, this::handleFindValueMessage)
			.match(AddNodeMessage.class, this::handleAddNodeMessage)
			.match(FindNodeMessage.class, this::handleFindNodeMessage)
			.match(PrintRoutingTableMessage.class, this::handlePrintRoutingTableMessage)
            .matchAny(o -> log.info("Received unknown message"))
            .build();
    }

	private void handlePrintRoutingTableMessage(PrintRoutingTableMessage message) {
		log.info("[RoutingTable]: Node {} : {}", this.id, printRoutingTable());
	}

	private int findClosestNode(int key) {
		int xorDistance = Integer.toString(this.id).hashCode() ^ Integer.toString(key).hashCode();
		int minId = this.id;
		for (int i = 0; i < routingTable.size(); i++) {
			for (int nodeId : routingTable.get(i).getNodes()) {
				int xorDistance2 = Integer.toString(nodeId).hashCode() ^ Integer.toString(key).hashCode();
				if (xorDistance2 < xorDistance) {
					xorDistance = xorDistance2;
					minId = nodeId;
				}
			}
		}
		return minId;
	}
	
    private void handleStoreMessage(StoreMessage message) {
		int minId = findClosestNode(Integer.parseInt(message.getKey()));

		if (minId == this.id) {
			dataStore.put(message.getKey(), message.getValue());
			log.info("[Store]: Stored data with key: {}", message.getKey());
		} else {
			// Forward the message to the determined target node
			ActorSelection targetNode = getContext().getSystem().actorSelection("akka://system/user/node" + minId);
			targetNode.tell(message, getSelf());
		}

    }

    private void handleFindValueMessage(FindValueMessage message) {
		int minId = findClosestNode(Integer.parseInt(message.key));
		if (minId == this.id) {
			String value = dataStore.get(message.key);
			log.info("[Find_Value]: The value of key {} is {}", message.key, value);
		} else {
			// Forward the message to the determined target node
			ActorSelection targetNode = getContext().getSystem().actorSelection("akka://system/user/node" + minId);
			targetNode.tell(message, getSelf());
		}
	}

	private void handleAddNodeMessage(AddNodeMessage message) {
		// Add the node to the routing table
		int bucketIndex = getBucketIndex(message.id);
		Bucket bucket = routingTable.get(bucketIndex);
		boolean success = bucket.addNode(message.id);
		// if(success)	log.info(this.toString());
		// if (success) {
		// 	log.info("Node {} added to bucket {}", message.node, bucketIndex);
		// } else {
		// 	log.info("Node {} not added. Bucket {} is full.", message.node, bucketIndex);
		// }
	}

	private void handleFindNodeMessage(FindNodeMessage message) {
		int minId = findClosestNode(message.id);
		if (minId == this.id) {
			log.info("[Find_Node]: the closest Node to node {} is {}", message.id, this.id);
		} else {
			// Forward the message to the determined target node
			ActorSelection targetNode = getContext().getSystem().actorSelection("akka://system/user/node" + minId);
			targetNode.tell(message, getSelf());
		}
   
    }

	private int getBucketIndex(int id) {
		if (id == this.id) {
			log.error("Cannot add node to its own routing table");
			return -1;
		}
		int xorDistance = this.id ^ id; 
		String xorDistanceString = Integer.toBinaryString(xorDistance);
		xorDistanceString = String.format("%" + nodeIdLength + "s", xorDistanceString).replace(' ', '0');
		int commonZeroPrefixLength = 0;
		for (int i = 0; i < xorDistanceString.length(); i++) {
			if (xorDistanceString.charAt(i) == '0') {
				commonZeroPrefixLength += 1;
			} else {
				break;
			}
		}
		// System.out.println("commonZeroPrefixLength: " + commonZeroPrefixLength);
		return commonZeroPrefixLength;
	}

}
