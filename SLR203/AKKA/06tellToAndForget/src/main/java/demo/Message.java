package demo;

import akka.actor.ActorRef;

public class Message {

    static public class StringMessage {
        public final String s;

        public StringMessage(String s) {
            this.s = s;
        }

    }

    static public class Ref2 {
        public ActorRef actorRef1 = null;
        public ActorRef actorRef2 = null;

        public Ref2(ActorRef actorRef1, ActorRef actorRef2) {
            this.actorRef1 = actorRef1;
            this.actorRef2 = actorRef2;
        }

    }

    static public class StringRef {
        public String s = null;
        public ActorRef actorRef = null;

        public StringRef(String s, ActorRef actorRef) {
            this.s = s;
            this.actorRef = actorRef;
        }
    }

}