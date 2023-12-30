Akka has different ways to stop actors.

Try those 4 ways for stopping actors:

* stop https://doc.akka.io/docs/akka/current/actors.html#stopping-actors
* poison pill https://doc.akka.io/docs/akka/current/actors.html#poisonpill
* kill https://doc.akka.io/docs/akka/current/actors.html#poisonpill
* graceful stop https://doc.akka.io/docs/akka/current/actors.html#poisonpill

Both stop and PoisonPill will terminate the actor and stop the message queue. They will cause the actor to cease processing messages, send a stop call to all its children, wait for them to terminate, then call its postStop hook. All further messages are sent to the dead letters mailbox.

The difference is in which messages get processed before this sequence starts. In the case of the stop call, the message currently being processed is completed first, with all others discarded. When sending a PoisonPill, this is simply another message in the queue, so the sequence will start when the PoisonPill is received. All messages that are ahead of it in the queue will be processed first.

By contrast, the Kill message causes the actor to throw an ActorKilledException which gets handled using the normal supervisor mechanism. So the behaviour here depends on what you've defined in your supervisor strategy. The default is to stop the actor. But the mailbox persists, so when the actor restarts it will still have the old messages except for the one that caused the failure.

from 

https://stackoverflow.com/a/13848350
