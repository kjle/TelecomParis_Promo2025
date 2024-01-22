package demo;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import akka.actor.ActorRef;

public class Bucket {
    private final int maxSize;
    private final LinkedList<ActorRef> nodes;

    // Constructor
    public Bucket(int maxSize) {
        this.maxSize = maxSize;
        this.nodes = new LinkedList<>();
    }

    // Adds a node to the bucket
    public boolean addNode(ActorRef node, Consumer<Boolean> pingResponseHandler) {
        if (!nodes.contains(node)) {
            if (nodes.size() < maxSize) {
                nodes.addFirst(node);
                return true;
            } else {
                // Get the least recently seen node (last in the list)
                ActorRef leastRecentlySeen = nodes.getLast();

                // Ping the least recently seen node
                pingNode(leastRecentlySeen, isSuccess -> {
                    if (isSuccess) {
                        // If the node is still alive, move it to the front
                        nodes.remove(leastRecentlySeen);
                        nodes.addFirst(leastRecentlySeen);
                    } else {
                        // If the node is not responding, remove it and add the new node
                        nodes.removeLast();
                        nodes.addFirst(node);
                    }
                    pingResponseHandler.accept(isSuccess);
                });
                return false;
            }
        }
        return true; // The node is already in the bucket
    }

    // Removes a node from the bucket
    public void removeNode(ActorRef node) {
        nodes.remove(node);
    }

    // Gets the list of nodes in the bucket
    public List<ActorRef> getNodes() {
        return new LinkedList<>(nodes);
    }

    // Finds if a node is in the bucket
    public boolean contains(ActorRef node) {
        return nodes.contains(node);
    }

    // Gets the size of the bucket
    public int size() {
        return nodes.size();
    }

    // Ping a node to check if it is alive
    // Dummy implementation of the pingNode method
    private void pingNode(ActorRef node, Consumer<Boolean> callback) {
        // Simulate a network ping operation. In a real implementation,
        // this would involve network communication.
        // For simplicity, this example calls the callback with 'true' after a delay.

        // Asynchronous execution to simulate network delay
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate delay
                callback.accept(true); // Simulate a successful ping response
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
