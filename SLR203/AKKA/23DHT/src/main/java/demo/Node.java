package demo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.lang.*;
import akka.dispatch.*;
import akka.pattern.*;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Await;
import scala.concurrent.Promise;
import akka.util.*;
import java.util.*;
import java.time.Duration;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Node extends UntypedAbstractActor {
    public final int m = 160; //Number of bits in SHA-1
    public final String id; //Encrypted ip is the id
    public final String ip;
	public final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    public ActorRef[] fingerTable;
    public ActorRef predecessor;
    public ArrayList<Key> keys;
    private static final Timeout timeout = Timeout.create(Duration.ofSeconds(5));

    private static String encryptThisString(String input)
        {
            try {
                // getInstance() method is called with algorithm SHA-1
                MessageDigest md = MessageDigest.getInstance("SHA-1");

                // digest() method is called
                // to calculate message digest of the input string
                // returned as array of byte
                byte[] messageDigest = md.digest(input.getBytes());

                // Convert byte array into signum representation
                BigInteger no = new BigInteger(1, messageDigest);

                // Convert message digest into hex value
                String hashtext = no.toString(16);

                // Add preceding 0s to make it 32 bit
                while (hashtext.length() < 32) {
                    hashtext = "0" + hashtext;
                }

                // return the HashText
                return hashtext;
            }
            // For specifying wrong message digest algorithms
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
	}
    

	public Node(String ip) {
        this.ip = ip;
        id = encryptThisString(ip);
        fingerTable = new ActorRef[m];
        keys = new ArrayList<Key>();
    }

    static public class Key {
        String id;
        String name;
        String data;
        public Key (String name, String data) {
            this.name = name;
            this.data = data;
            id = encryptThisString (name);
        }
    }

    static public class findSuccessorREQ {
        String id;
        public findSuccessorREQ(String id) {
            this.id = id;
        }
    }

    static public class findSuccessorRES {
        ActorRef successor;
        public findSuccessorRES(ActorRef successor) {
            this.successor = successor;
        }
    }

    static public class successorREQ {
        public successorREQ() {}
    }

    static public class successorRES {
        ActorRef successor;
        public successorRES(ActorRef successor) {
            this.successor = successor;
        }
    }

    public ActorRef findSuccessor(String id) {
        ActorRef nPrime = findPredecessor(id);
        Future<Object> future = Patterns.ask(nPrime, new successorREQ(), timeout);
        ActorRef successor = null;
        try {
            successorRES res = (successorRES) Await.result(future, timeout.duration());
            successor = res.successor;
        } catch (Exception e) {};
        return successor;
    }

    static public class idREQ {
        public idREQ() {}
    }

    static public class idRES {
        String id;
        public idRES(String id) {
            this.id = id;
        }
    }

    public ActorRef findPredecessor(String id) {
        ActorRef nPrime = getSelf();
        while (true) {
            Future<Object> future1 = Patterns.ask(nPrime, new idREQ(), timeout);
            Future<Object> future2 = Patterns.ask(nPrime, new successorREQ(), timeout);
            idRES res1 = null;
            successorRES res2 = null;
            idRES res3 = null;
            closestPrecedingFingerRES res4 = null;

            String nPrimeID = null;
            ActorRef nPrimeSuc = null;
            String nPrimeSucID = null;
            ActorRef newNPrime = null;

            try {
                res1 = (idRES) Await.result(future1, timeout.duration());
                nPrimeID = res1.id;
                res2 = (successorRES) Await.result(future2, timeout.duration());
                nPrimeSuc = res2.successor;
            } catch (Exception e) {};
            Future<Object> future3 = Patterns.ask(nPrimeSuc, new idREQ(), timeout);
            try {
                res3 = (idRES) Await.result(future3, timeout.duration());
                nPrimeSucID = res3.id;
            } catch (Exception e) {};
            if (id.compareTo(nPrimeID) <= 0 || id.compareTo(nPrimeSucID) > 0) {
            //if (id <= nPrimeID || id > nPrimeSucID) {
                Future<Object> future4 = Patterns.ask(nPrime, new closestPrecedingFingerREQ(id), timeout);
                try {
                    res4 = (closestPrecedingFingerRES) Await.result(future4, timeout.duration());
                    newNPrime = res4.closest;
                } catch (Exception e) {};
                nPrime = newNPrime;
            }
            else
                return nPrime;
        }
    }

    public static class closestPrecedingFingerREQ {
        String id;
        public closestPrecedingFingerREQ(String id) {
            this.id = id;
        }
    }

    public static class closestPrecedingFingerRES {
        ActorRef closest;
        public closestPrecedingFingerRES(ActorRef closest) {
            this.closest = closest;
        }
    }

    public ActorRef closestPrecedingFinger(String idToFind)
    {
        for (int i = m - 1; i >= 0; i--) {

            idRES res = null;
            String fingerID = null;
            Future<Object> future = Patterns.ask(fingerTable[i], new idREQ(), timeout);
            try {
                res = (idRES) Await.result(future, timeout.duration());
                fingerID = res.id;
                } catch (Exception e) {};
            if (fingerID.compareTo(id) > 0 && fingerID.compareTo(idToFind) < 0)
            //if (fingerID > id && fingerID < id)
                return fingerTable[i];
        }
        return getSelf();
    }

    static public class keyREQ {
        public keyREQ() {}
    }

    static public class keyRES {
        ArrayList<Key> keys;
        public keyRES(ArrayList<Key> keys) {
            this.keys = keys;
        }
    }

    static public class predecessorREQ {
        public predecessorREQ() {}
    }

    static public class predecessorRES {
        ActorRef predecessor;
        public predecessorRES(ActorRef predecessor) {
            this.predecessor = predecessor;
        }
    }

    public static class JoinMSG {
        ActorRef nPrime;
        public JoinMSG(ActorRef nPrime) {
            this.nPrime = nPrime;
        }
    }

    public void join(ActorRef nPrime) {
        // Ici j'ai fait une adaptation où le null est representé par le propre acteur
        if (nPrime == getSelf()) {
            for (int i = 0; i < m; i++)
                fingerTable[i] = getSelf();
            predecessor = getSelf();
        }
        else {
            initFingerTable(nPrime);
            updateOthers();
            Future<Object> future1 = Patterns.ask(fingerTable[0], new keyREQ(), timeout);
            Future<Object> future2 = Patterns.ask(predecessor, new idREQ(), timeout);
            keyRES res1 = null;
            ArrayList<Key> sucKeys = null;
            idRES res2 = null;
            String predID = null;
            try {
                res1 = (keyRES) Await.result(future1, timeout.duration());
                sucKeys = res1.keys;
                res2 = (idRES) Await.result(future2, timeout.duration());
                predID = res2.id;
            } catch (Exception e) {};
            ArrayList<Key> sucKeysCopy = new ArrayList<>(sucKeys);
            for (Key k : sucKeysCopy) 
                if (k.id.compareTo(predID) > 0 && k.id.compareTo(id) <= 0) {
                //if (k.id > predID && k.id <= id) {
                    sucKeys.remove(k);
                    this.keys.add(k);
                }
        }
    }

    public static class setPredecessorMSG {
        ActorRef predecessor;
        public setPredecessorMSG(ActorRef predecessor) {
            this.predecessor = predecessor;
        }
    }

    public void initFingerTable(ActorRef nPrime) {
        BigInteger intID = new BigInteger(id, 16);
        String finger0start = intID.add(BigInteger.valueOf(1)).toString(16);

        Future<Object> future1 = Patterns.ask(nPrime, new findSuccessorREQ(finger0start), timeout);
        findSuccessorRES res1 = null;
        ActorRef successor = null;
        try {
            res1 = (findSuccessorRES) Await.result(future1, timeout.duration());
            successor = res1.successor;
        } catch (Exception e) {};

        fingerTable[0] = successor;
        Future<Object> future2 = Patterns.ask(successor, new predecessorREQ(), timeout);
        predecessorRES res2 = null;
        try {
            res2 = (predecessorRES) Await.result(future2, timeout.duration());
            predecessor = res2.predecessor;
        } catch (Exception e) {};
        successor.tell(new setPredecessorMSG(getSelf()), getSelf());
        for (int i = 1; i < m; i++) {
            BigInteger inc = BigInteger.valueOf(2).pow(i);
            inc = inc.remainder(BigInteger.valueOf(2).pow(m));
            String fingerIstart = intID.add(inc).toString(16);
            Future<Object> future3 = Patterns.ask(fingerTable[i-1], new idREQ(), timeout);
            idRES res3 = null;
            String im1ID = null;
            try {
                res3 = (idRES) Await.result(future3, timeout.duration());
                im1ID = res3.id;
            } catch (Exception e) {};
            if (fingerIstart.compareTo(id) >= 0 && fingerIstart.compareTo(im1ID) < 0)
            //if (fingerIstart >= id && fingerIstart < im1ID)
                fingerTable[i] = fingerTable[i-1];
            else {
                findSuccessorRES res4 = null;
                Future<Object> future4 = Patterns.ask(nPrime, new findSuccessorREQ(fingerIstart), timeout);
                try {
                    res4 = (findSuccessorRES) Await.result(future4, timeout.duration());
                    fingerTable[i] = res4.successor;
                } catch (Exception e) {};
            }
        }
    }

    static public class updateFingerTableMSG {
        ActorRef s;
        int index;
        public updateFingerTableMSG(ActorRef s, int index) {
            this.s = s;
            this.index = index;
        }
    }

    public void updateOthers() {
        BigInteger intID = new BigInteger(id, 16);
        for (int i = 0; i < m; i++) {
            BigInteger dec = BigInteger.valueOf(2).pow(i);
            String mark = intID.subtract(dec).toString(16);
            ActorRef p = findPredecessor(mark);
            p.tell(new updateFingerTableMSG(getSelf(), i), getSelf());
        }
    }

    public void updateFingerTable(ActorRef s, int index) {
        Future<Object> future1 = Patterns.ask(s, new idREQ(), timeout);
        Future<Object> future2 = Patterns.ask(fingerTable[index], new idREQ(), timeout);
        String sID = null;
        String fingerID = null;
        try {
            idRES res1 = (idRES) Await.result(future1, timeout.duration());
            idRES res2 = (idRES) Await.result(future2, timeout.duration());
            sID = res1.id;
            fingerID = res2.id;
        } catch (Exception e) {};
        if (sID.compareTo(id) >= 0 && sID.compareTo(fingerID) < 0) {
        //if (sID >= id && sID < fingerID) {
            fingerTable[index] = s;
            predecessor.tell(new updateFingerTableMSG(s, index), getSelf());
        }
    }

    public static class keyPlaceMSG {
        Key k;
        public keyPlaceMSG (Key k) {
            this.k = k;
        }
    }

    public static class keyInsertMSG {
        Key k;
        public keyInsertMSG (Key k) {
            this.k = k;
        }
    }

    public static class keySearch {
        String name;
        public keySearch(String name) {
            this.name = name;
        }
    }

    public static class keyFetch {
        Key k;
        public keyFetch(Key k) {
            this.k = k;
        }
    }

	// Static function creating actor
	public static Props createActor(String ip) {
		return Props.create(Node.class, () -> {
			return new Node(ip);
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if(message instanceof findSuccessorREQ) {
            findSuccessorREQ m = (findSuccessorREQ) message;
            findSuccessorRES res = new findSuccessorRES(findSuccessor(m.id));
            getSender().tell(res, getSelf());
		}
        if(message instanceof successorREQ) {
            getSender().tell(new successorRES(fingerTable[0]), getSelf());
        }
        if(message instanceof idREQ) {
            getSender().tell(new idRES(id), getSelf());
        }
        if(message instanceof closestPrecedingFingerREQ) {
            closestPrecedingFingerREQ m = (closestPrecedingFingerREQ) message;
            closestPrecedingFingerRES res = new closestPrecedingFingerRES(closestPrecedingFinger(m.id));
            getSender().tell(res, getSelf());
        }
        if(message instanceof keyREQ) {
            getSender().tell(new keyRES(keys), getSelf());
        }
        if(message instanceof predecessorREQ) {
            getSender().tell(new predecessorRES(predecessor), getSelf());
        }
        if(message instanceof setPredecessorMSG) {
            setPredecessorMSG m = (setPredecessorMSG) message;
            predecessor = m.predecessor;
        }
        if(message instanceof updateFingerTableMSG) {
            updateFingerTableMSG m = (updateFingerTableMSG) message;
            updateFingerTable(m.s, m.index);
        }
        if(message instanceof JoinMSG) {
            JoinMSG m = (JoinMSG) message;
            join(m.nPrime);
        }
        if(message instanceof keyPlaceMSG) {
            keyPlaceMSG m = (keyPlaceMSG) message;
            ActorRef s = findSuccessor (m.k.id);
            s.tell(new keyInsertMSG(m.k), getSelf());
        }
        if(message instanceof keyInsertMSG) {
            keyInsertMSG m = (keyInsertMSG) message;
            keys.add(m.k);
            log.info("["+getSelf().path().name()+"] will save ["+ m.k.name + "]");
        }
        if(message instanceof keySearch) {
            keySearch m = (keySearch) message;
            ActorRef holder = findSuccessor (encryptThisString(m.name));
            Future<Object> future = Patterns.ask(holder, new keyREQ(), timeout);
            keyRES res = null;
            ArrayList<Key> copyKeys = null;
            try {
                res = (keyRES) Await.result(future, timeout.duration());
                copyKeys = new ArrayList<>(res.keys);
            } catch (Exception e) {};
            for (Key k : copyKeys) {
                if (k.name == m.name) {
                    log.info("["+getSelf().path().name()+"] has found ["+ k.name +"] its content is : [" + k.data +"]");
                    return;
                }
            }
        }
	}
}
