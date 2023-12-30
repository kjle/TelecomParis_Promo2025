![](img.png)
```
title load balancer round robin

participant a
participant loadbalancer
participant b
participant c

b->loadbalancer:join
c->loadbalancer:join
a->loadbalancer:m1
loadbalancer->b:m1
a->loadbalancer:m2
loadbalancer->c:m2
a->loadbalancer:m3
loadbalancer->b:m3
c->loadbalancer:unjoin
a->loadbalancer:m4
loadbalancer->b:m4
```

http://sequencediagram.org/index.html#initialData=C4S2BsFMAJwewIYBNoCMHgQOwMaQE7T5wCuWKxqIWAUDQA4L6g4iNbDQINMtvad4ydJlwEezEK3adUEvjOg46qALQA+IUhHY8+AFwArONRo4NWnWIPHTCC4m0ZdBfQFsAjDUvPrG1O5e9pqOVnruAEzeob56GjiRNME+ouFuAMzRwrEE-u6Z5iHZqa5ktrTJMSUGbgAsWU7VeXVAA
