Same as before, use the scheduler to do the right timing for this pattern. Here the scheduler is not displayed for simplicity reasons.

![pubsub](img.png?raw=true)
```
title publish subscribe : 1 actor per topic
participant a
participant b
participant c
participant publisher1
participant publisher2
participant topic1
participant topic2

a->topic1: subscribe
b->topic1: subscribe
b->topic2: subscribe
c->topic2: subscribe
publisher1->topic1: hello
topic1->a:hello
topic1->b:hello
publisher2->topic2: world
topic2->a:world
topic2->b:world
a->topic1: unsubscribe
publisher1->topic1: hello2
topic1->b:hello2
```

http://sequencediagram.org/index.html#initialData=C4S2BsFMAIAcFcBG4QGcAW1VNQYwE4iIwBc0AjNAIa7AD2+ckj9sIuAULFfqLiNwB2walx58BVYdERje7SdM7d5-ISITI06ZuTkT1cJCgzMATPoWHW7PSoNSRN3BY5UAtAD5n5MtkR4hMQciF4+fjgERJAhYXRsLhEBUcG4cQlmSYHRXMbauum2ZDrg4HQcPl5UJCVlFfG2Xog1kKXlmiY6+GaFidAA7gzgACb1GVUkg-gjY+w9ns1TMx7eDbi+0PCC-tnBHfn45L0btXQWlQstbWZAA
