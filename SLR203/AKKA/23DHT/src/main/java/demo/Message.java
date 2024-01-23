package demo;

import java.util.LinkedList;

import akka.actor.ActorRef;

public class Message {

    // Message types used by NodeActor
    // public static class StoreMessage {
    //     public final String key;
    //     public final String value;

    //     public StoreMessage(String key, String value) {
    //         this.key = key;
    //         this.value = value;
    //     }
    // }

    public static class RetrieveMessage {
        public final String key;

        public RetrieveMessage(String key) {
            this.key = key;
        }
    }

    public static class ResultMessage {
        public final String key;
        public final String value;

        public ResultMessage(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    
    // Message types used by DHTActor
    public static class AddNodeMessage {
        public final ActorRef node;
        public final int id;

        public AddNodeMessage(ActorRef node, int id) {
            this.node = node;
            this.id = id;
        }
    }

    // Message types used by MessageActor
    public static class NodeMessage {
        public final String content; // Consider using a more specific type or class

        public NodeMessage(String content) {
            this.content = content;
        }
    }

    // Base class for messages
    public static class BaseMessage {
        // Common fields for all messages, if any
    }

    // Message to check if a node is alive
    public static class PingMessage extends BaseMessage {
        public final int id;

        public PingMessage(int id) {
            this.id = id;
        }
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

    // Message to respond with a requested value or status
    public static class ResponseMessage extends BaseMessage {
        public final String response;

        public ResponseMessage(String response) {
            this.response = response;
        }
    }

    // Message for a new node to join the network
    // public static class NodeJoinMessage extends BaseMessage {
    //     public final Node node;

    //     public NodeJoinMessage(Node node) {
    //         this.node = node;
    //     }
    // }

    // Message to find information about a specific node
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
