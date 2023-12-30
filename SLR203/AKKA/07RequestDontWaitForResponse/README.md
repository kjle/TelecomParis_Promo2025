You can try sending more message from a to b (maybe 30 messages ?) to test this pattern.
As always, be sure that the main function is only sending one message to a, so there is one and only one "tell" call from the main function. The other tell calls are from the actors. 

Remember that the sender can also use the getSelf() method to get its own reference, the receiver can use the getSender() to get the sender reference.

![](img.png)

```
title request don't wait for reponse

main-->a:create
main-->b:create
main->a:ref:b
a->b:req1
note right of a:here a doesn't have to wait for a response
a->b:req2
b->a:res1
b->a:res2

```

https://sequencediagram.org/index.html#initialData=C4S2BsFMAICdII4FdIGdjQCYHsB2ByDAdwEMxoAzbWOSABz1UgChmBbM3AWi4D4SAXAGN4JYCw4hufAEbDR49pz6D4FATOYlZA+AgCMzXNnFwQAcwAWGbBWiDLkePazY0BDJZIA3GMGzQpORUNCS0qAy4TFo6egBMzDIqumiGSfwpqAlAA
