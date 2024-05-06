create the JAR with 

mvn clean compile assembly:single

and run it with

java -jar .\target\myftpserver-1-jar-with-dependencies.jar

you can change the log level in the src/main/resources/log4J.properties file (by default I have set it up to INFO but you can change it to:

DEBUG 	Designates fine-grained informational events that are most useful to debug an application.
INFO 	Designates informational messages that highlight the progress of the application at coarse-grained level.
WARN 	Designates potentially harmful situations.
ERROR 	Designates error events that might still allow the application to continue running.
FATAL 	Designates very severe error events that will presumably lead the application to abort.
OFF 	The highest possible rank and is intended to turn off logging.