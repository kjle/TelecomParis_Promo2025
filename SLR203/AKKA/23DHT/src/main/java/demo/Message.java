package demo;

import java.util.LinkedList;

import akka.actor.ActorRef;

public class Message {


    public static class FindValueMessage {
        public final String key;

        public FindValueMessage(String key) {
            this.key = key;
        }
    }

    public static class PrintRoutingTableMessage {
    
    }
    
    public static class AddNodeMessage {
        public final ActorRef node;
        public final int id;

        public AddNodeMessage(ActorRef node, int id) {
            this.node = node;
            this.id = id;
        }
    }

    // Base class for messages
    public static class BaseMessage {
        // Common fields for all messages, if any
    }

    // Message to store a key-value pair
    public static class StoreMessage extends BaseMessage {
        public final KeyValue keyValue;

        public StoreMessage(KeyValue keyValue) {
            this.keyValue = keyValue;
        }

        public String getKey() {
            return keyValue.getKey();
        }

        public String getValue() {
            return keyValue.getValue();
        }
    }

    public static class FindNodeMessage extends BaseMessage {
        public final int id;
        public final int numberOfNodes;
        public LinkedList<Integer> closestNodes = new LinkedList<Integer>();
        public int minDistance = Integer.MAX_VALUE;

        public FindNodeMessage(int id, int numberOfNodes) {
            this.id = id;
            this.numberOfNodes = numberOfNodes;
        }

    }

    public static class ResponseFindNodeMessage extends BaseMessage {
        
        public LinkedList<Integer> closestNodes = new LinkedList<Integer>();


    }

    public static class NodeDist {
        public int id;
        public int distance;
    }
}
