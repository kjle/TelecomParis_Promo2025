package com.example.synod;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.example.synod.message.Launch;
import com.example.synod.message.Membership;

import java.util.*;

public class Main {
    public static int N = 3;
    public static void main(String[] args) throws InterruptedException {
        // Instantiate an actor system
        final ActorSystem system = ActorSystem.create("system");
        system.log().info("System started with N=" + N );

        ArrayList<ActorRef> processes = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            final ActorRef a = system.actorOf(Process.createActor(N, i));
            processes.add(a);
        }

        //give each process a view of all the other processes
        Membership m = new Membership(processes);
        for (ActorRef actor : processes) {
            actor.tell(m, ActorRef.noSender());
        }

        processes.get(0).tell(
                new Launch(),
                ActorRef.noSender());

    }
}
