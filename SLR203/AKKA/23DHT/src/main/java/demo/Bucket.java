package demo;

import java.util.LinkedList;
import java.util.List;

public class Bucket {
    private final int maxSize;
    private final LinkedList<Integer> nodes;

    // Constructor
    public Bucket(int maxSize) {
        this.maxSize = maxSize;
        this.nodes = new LinkedList<>();
    }

    // Adds a node to the bucket
    public boolean addNode(int nodeId) {
        if (!nodes.contains(nodeId)) {
            if (nodes.size() < maxSize) {
                nodes.addFirst(nodeId);
                return true;
            } else {
                // // Get the least recently seen node (last in the list)
                // int leastRecentlySeen = nodes.getLast();

                // // Ping the least recently seen node
                // pingNode(leastRecentlySeen, isSuccess -> {
                //     if (isSuccess) {
                //         // If the node is still alive, move it to the front
                //         nodes.remove(leastRecentlySeen);
                //         nodes.addFirst(leastRecentlySeen);
                //     } else {
                //         // If the node is not responding, remove it and add the new node
                //         nodes.removeLast();
                //         nodes.addFirst(nodeId);
                //     }
                //     pingNode(leastRecentlySeen);
                // });
                return false;
            }
        }
        return true; // The node is already in the bucket
    }

    // Removes a node from the bucket
    public void removeNode(int nodeId) {
        nodes.remove(nodeId);
    }

    // Gets the list of nodes in the bucket
    public List<Integer> getNodes() {
        return new LinkedList<>(nodes);
    }

    // Finds if a node is in the bucket
    public boolean contains(int nodeId) {
        return nodes.contains(nodeId);
    }

    // Gets the size of the bucket
    public int size() {
        return nodes.size();
    }

    // // Ping a node to check if it is alive
    // // Dummy implementation of the pingNode method
    // private void pingNode(int nodeId, Consumer<Boolean> callback) {
    //     // Simulate a network ping operation. In a real implementation,
    //     // this would involve network communication.
    //     // For simplicity, this example calls the callback with 'true' after a delay.

    //     // Asynchronous execution to simulate network delay
    //     new Thread(() -> {
    //         try {
    //             Thread.sleep(1000); // Simulate delay
    //             callback.accept(true); // Simulate a successful ping response
    //         } catch (InterruptedException e) {
    //             Thread.currentThread().interrupt();
    //         }
    //     }).start();
    // }
}
