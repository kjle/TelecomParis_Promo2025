package demo;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import java.util.ArrayList;
import demo.ChordNode.*;

/**
 * @author Luciano Freitas
 * @description Implementation du protocol Chord d'après l'article qui lui défini.
 * Le code a été adapté pour le modèle orienté à acteurs. 
 */

public class ChordProtocol {

	public static void main(String[] args) {
		// Instantiate an actor system
		final ActorSystem system = ActorSystem.create("system");
        Key k0 = new Key ("K0", "FOO");
        Key k1 = new Key ("K1", "BAR");
        // Initial actors:
        ActorRef n0 = system.actorOf(ChordNode.createActor("192.168.1.0"), "node0");
        n0.tell(new JoinMSG(n0), ActorRef.noSender());
        try {
			wait1s();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        ActorRef n1 = system.actorOf(ChordNode.createActor("192.168.1.1"), "node1");
        n1.tell(new JoinMSG(n0), ActorRef.noSender());
        try {
			wait1s();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        ActorRef n2 = system.actorOf(ChordNode.createActor("192.168.1.2"), "node2");
        n2.tell(new JoinMSG(n1), ActorRef.noSender());
        try {
			wait1s();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        n0.tell(new keyPlaceMSG(k0), ActorRef.noSender());
        try {
			wait1s();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
        ActorRef n3 = system.actorOf(ChordNode.createActor("192.168.1.3"), "node3");
        n3.tell(new JoinMSG(n1), ActorRef.noSender());
        try {
			wait1s();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        n2.tell(new keyPlaceMSG(k1), ActorRef.noSender());
        try {
			wait1s();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        n1.tell(new keySearch ("K1"), ActorRef.noSender());
        n3.tell(new keySearch ("K0"), ActorRef.noSender());
        try {
			wait1s();
			wait1s();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			system.terminate();
		}
	}
    
	

	public static void wait1s() throws InterruptedException {
		Thread.sleep(1000);
	}
}
