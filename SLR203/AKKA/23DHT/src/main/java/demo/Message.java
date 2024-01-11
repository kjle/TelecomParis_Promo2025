package demo;

import java.util.ArrayList;

import akka.actor.ActorRef;

public class Message {

    static public class StoreMessage {
        public final int key;
        public final String value;

        public StoreMessage(int key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    static public class FindNodeMessage {
        public final int nodeId;

        public FindNodeMessage(int nodeId) {
            this.nodeId = nodeId;
        }

    }

    static public class FindValueMessage {
        public final int key;

        public FindValueMessage(int key) {
            this.key = key;
        }
    }

    

}