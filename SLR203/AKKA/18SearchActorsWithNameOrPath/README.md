How do you get an actor reference with its name or path ?

Create an actor named "a" that creates an actor when he receives the CREATE message. The actor created has the name "actorX" where X is incremented by one after each creation, starting at 1.

Send at least two CREATE message to "a"

Search for the actors "a" "actor1" and "actor2" using their name. Print their respective paths.

Search for the same actors using their paths.

Here is a little bit of documentation to do that:

Akka ActorSelection: 

https://doc.akka.io/docs/akka/current/actors.html#identifying-actors-via-actor-selection

Create one very basic actor.

Try to print the paths of all local actors in the "top level scope" (path starting with /):

https://doc.akka.io/docs/akka/current/general/addressing.html#top-level-scopes-for-actor-paths

You have to print the paths of all actors under 

* "/user"
* "/system"
* "/deadLetters"
* "/temp"
* "/remote"
