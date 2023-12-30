Same as before, use the scheduler to do the right timing for this pattern. Here the scheduler is not displayed for simplicity reasons.

![](img.png?raw=true)
```
title mutlicast

participant sender
participant multicaster
participant receiver1
participant receiver2
participant receiver3

sender->multicaster:group1:receiver1,receiver2
sender->multicaster:group2:receiver2,receiver3
sender->multicaster:hello to group1
multicaster->receiver1:hello
multicaster->receiver2:hello
sender->multicaster:world to group2
multicaster->receiver2:world
multicaster->receiver3:world
```
http://sequencediagram.org/index.html#initialData=C4S2BsFMAIFsFcIgMYEMDOwBQWAOqAnUZEfAO2GnUjIBNIC9DjTUK55xiNgGmiUrdgUjJIIAG4MAjPxblKIsZIYAmOYIXQl4qQQDMOanQYBaAHwIuKHgwBcAcwIB7eLml2dKgtIA0XvXVjegILK25MeydXXFVPUV01fwTvQ2CzS04I3gI7AAtIcHBnaGAS6LdZcJtI0PMAmXzC4qxqtFqLBoI4gqLnLHS6tttcgHdnAnBaUvKXN3VhjvqUwLtxydpWrJqczpWGfTWJqaA
