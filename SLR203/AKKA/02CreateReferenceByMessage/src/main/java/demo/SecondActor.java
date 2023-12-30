package demo;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;

public class SecondActor extends UntypedAbstractActor {

	// Empty Constructor
	public SecondActor() {}

	// Static function that creates actor
	public static Props createActor() {
		return Props.create(SecondActor.class, () -> {
			return new SecondActor();
		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {

	}
	
	
}
