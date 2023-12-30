the convergecast pattern is also called merger or converger
To test this pattern you have to schedule some messages in order to get the right order for the demo.
Use the scheduler for that, like this:

```
getContext().system().scheduler().scheduleOnce(Duration.ofMillis(1000), getSelf(), "go", getContext().system().dispatcher(), ActorRef.noSender());
```

![](img.png?raw=true)
```
title convergecast

materialdesignicons F150 scheduler
participant a
participant b
participant c
participant merger
participant d

a->merger:join
space -3
a-->scheduler:scheduleOnce:1000:a:"go"...
b->merger:join
space -3
b-->scheduler:scheduleOnce:1000:b:"go"...
c->merger:join
space -3
c-->scheduler:scheduleOnce:1000:c:"go"...
scheduler->a:"go"
space -3
a->merger:hi
space -3
a-->scheduler:scheduleOnce:1000:a:"go"...
scheduler->b:"go"
space -3
b->merger:hi
space -3
b-->scheduler:scheduleOnce:1000:b:"go"...
scheduler->c:"go"
space -3
c->merger:hi
space -4
merger->d:hi
space -3
c->merger:unjoin
space -3
scheduler->a:"go"
space -3
a->merger:hi2
space -3
scheduler->b:"go"
space -3
b->merger:hi2
space -4
merger->d:hi2
```

https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGMHsB2A3SAnA5pWBDAzsAFCEC2Ow6IO4AJpHiBoiAontAGICMArAAzQ8sABaQaAVyhpCABxxpQsEHMTBoOWfMXKcq6ACNNCljr2wj2lWpLos0ucaVXoNYjgC0APhuZ0ALgAreBBEQjw5WBh3AGZCDy8hUQkpP0SxSUgAeURIvy4+Ar8cPwAiDHgSgDpqwn0vHztA4NDwnEjoGNr3BJF0lLTkrJzIPIK+P31S8qqa2HrbfyCQsIio2LmepIy0VN7B7Nz8wtgpiurKsL3tr2KyipW2tbj53x3hEAf2zvjPAe3drZQA4jI7jW7Tc6XQHoLyTO4lT5POreBZvD6tL6xOqbPr+P5A4ajQpwiE1fEwzwneGIjrrF6Nd409wAFlIqK8ND8jIxTzmKNefnEiCWLVWtKhuLQN1OCJ54o8-IZIAATEzYuSpZ4Sfc5Z1kQ1-O9VbrWQbNZyjUA
