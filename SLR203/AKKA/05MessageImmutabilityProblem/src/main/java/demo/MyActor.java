package demo;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class MyActor extends UntypedAbstractActor{

	// Logger attached to actor
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private MyBrokenMessage brokenMessage;
	private MyCorrectMessage correctMessage;

	public MyActor() {}

	// Static function creating actor
	public static Props createActor() {
		return Props.create(MyActor.class, () -> {
			return new MyActor();
		});
	}

	  static public class PrintBrokenMessage{	  }
	  static public class PrintCorrectMessage{	  }

	@Override
	public void onReceive(Object message) throws Throwable {
		if(message instanceof MyBrokenMessage){
			this.brokenMessage = (MyBrokenMessage) message;
			log.info("("+getSelf().path().name()+") received a broken message from ("+ getSender().path().name() +")");
		}
		if(message instanceof MyCorrectMessage){
			this.correctMessage = (MyCorrectMessage) message;
			log.info("("+getSelf().path().name()+") received a correct message from ("+ getSender().path().name() +")");
		}
		if(message instanceof PrintBrokenMessage){
			log.info("Broken message: list="+this.brokenMessage.list+" i="+this.brokenMessage.i+" p.getName()="+this.brokenMessage.p.getName()+ " s="+this.brokenMessage.s);
		}
		if(message instanceof PrintCorrectMessage){
			log.info("Correct message: list="+this.correctMessage.list+" i="+this.correctMessage.i+" p.getName()="+this.correctMessage.p.getName()+ " s="+this.correctMessage.s);
		}
	}

}
