# JoramMQ and Docker

There are different ways to deploy JoramMQ in Docker:

- The first solution is to use a predefined Java image and to mount the JoramMQ installation directory in this image. This solution makes it possible to keep JoramMQ data external to the container, so the documentation and log files are directly accessible.
- The second solution is to create a minimal Docker image for JoramMQ. As in the previous solution, we keep the JoramMQ data outside the container. This solution simplifies the order to create the container.
- The last solution consists in creating a specific Docker image for JoramMQ, this image contains all the data necessary for the server.

These three solutions are detailed in the following sections.

Note: In these different examples we use the openjdk: 11 Docker image for Java, this image can easily be replaced according to your needs.

## 1. Launching a JoramMQ container from a Java image

In this solution we just use a Java image (openjdk:11 by example) and "mount" the installation directory of JoramMQ in the container. This is quite simple and it has the advantage of keeping JoramMQ data out of the container (logs and documentation). This also allows you to easily change the Java version if you want to test different implementations.

### Container creation
We create the container by specifying the chosen Java image and the JoramMQ start command (jorammq-server).

Command:

```
docker run --env JORAMMQ_HOME=<JoramMQ mount point> --env JORAMMQ_IN_DOCKER=true -p 1883:1883/tcp -p 18090:18090/tcp -v <path of JoramMQ installation>:<JoramMQ mount point> <Docker Java image> <JoramMQ mount point>/bin/jorammq-server
```

- <path of JoramMQ installation> corresponds to the host's directory containing the JoramMQ installation with bin, conf, doc, .. directories. In the example below 'C:\Temp\jorammq-mqtt-1.13.0-SNAPSHOT' (decompressing a 1.13.0 SNAPSHOT version of JoramMQ in the C:\Temp directory).
- <JoramMQ mount point> corresponds to the container's directory in which the installation of JoramMQ will be accessible, '/home/jorammq' in the example below.
- The JORAMMQ_HOME environment variable should correspond to the mount point of the JoramMQ installation in the created container.  
- The JORAMMQ\_IN\_DOCKER environment variable specifies that the JoramMQ must not end. This avoids the early termination of the container created.
- The container's ports 1883 (MQTT) and 18090 (SSH administration) are published to the host (Depending of your configuration you can have to publish other ports).
- <Docker Java image> corresponds to the Java Docker image you have chosen, 'openjdk:11' in the example below.

Example:

```
docker run --env JORAMMQ_HOME=/home/jorammq --env JORAMMQ_IN_DOCKER=true -p 1883:1883/tcp -p 18090:18090/tcp -v C:\Temp\jorammq-mqtt-1.13.0-SNAPSHOT:/home/jorammq openjdk:11 /home/jorammq/bin/jorammq-server
```

### Container life cycle
We can stop JoramMQ (and the container) properly using the administration command (jorammq-admin -stop), or a little more brutally by stopping the container (docker stop).

```
docker exec <containerId> <JoramMQ mount point>/bin/jorammq-admin -stop
docker stop <containerId>
```

Normally stopping the container results in clean stop of JoramMQ. However an error can be raised by the server on restart if the lock file has not been deleted (see the documentation in section 2.6 and 6.1.15):

```
TxLogTransaction.init : TxLog.init(): Either TxLog is already running, or you have to remove the lock file: /home/jorammq/data/jorammq/s0/lock
```

In that case, you must remove the lock file manually:

```
docker exec rm /home/jorammq/data/jorammq/s0/lock
```

In all cases, we restart JoramMQ by starting the container (docker start).

```
docker start <containerId>
```

## 2. Creating a simple JoramMQ image

In this solution we create a Docker image dedicated to JoramMQ, at first we can just add the command to launch JoramMQ. In absolute terms,
this simplifies the command to create the container.

### Image creation:

The content of the script file (DockerFile) allowing the creation of such an image is:

```
FROM openjdk:11

#############
# Expose ports
#############

EXPOSE 1883/tcp 18090/tcp

#########
# RUN
#########

ENV JORAMMQ_HOME=/home/jorammq
ENV PATH=$JORAMMQ_HOME/bin:$PATH
# This environment variable below allows to launch the JoramMQ server in foreground, it avoids the Docker to exit.
ENV JORAMMQ_IN_DOCKER=true
# Using exec allows to pass the SIGTERM to Java process (pid 1) during container shutdown
WORKDIR $JORAMMQ_HOME
CMD exec /bin/bash bin/jorammq-server

HEALTHCHECK --interval=1m --timeout=10s --retries=3 CMD bin/jorammq-admin -exec "getStatusInfo tcp-1883" | grep "RUNNING"
```

Assuming that the previous DockerFile is in a file named 'JDK11DockerFile1', the command below build a corresponding image named 'jmq1.13-openjdk11' :

```
docker build -t jmq1.13-openjdk11 -f JDK11DockerFile1 .
```

### Creation and start-up of the container

We create the container by specifying the path of the JoramMQ installation to use.

Command:

```
docker run -p 1883:1883/tcp -p 18090:18090/tcp -v <path of JoramMQ installation>:/home/jorammq jmq1.13-openjdk11
```

- <path of JoramMQ installation> corresponds to the host's directory containing the JoramMQ installation with bin, conf, doc, .. directories. In the example below 'C:\Temp\jorammq-mqtt-1.13.0-SNAPSHOT' (decompressing a 1.13.0 SNAPSHOT version of JoramMQ in the C:\Temp directory).

Example:

```
docker run -p 1883:1883/tcp -p 18090:18090/tcp -v C:\Temp\jorammq-mqtt-1.13.0-SNAPSHOT:/home/jorammq jmq1.13-openjdk11
```

### Container life cycle

We can stop JoramMQ (and the container) properly using the administration command (jorammq-admin -stop), or a little more brutally by stopping the container (docker stop).

```
docker exec <containerId> jorammq-admin -stop
docker stop <containerId>
```

In all cases, we restart JoramMQ by starting the container (docker start).

```
docker start <containerId>
```

## 3. Creating a complete self content JoramMQ image

In this solution we create a Docker image dedicated to JoramMQ and containing the distribution. This makes JoramMQ a little more opaque (access to the logs mainly) but this is perhaps the objective sought. It also makes it possible to 'move' the container, or to deploy the image more easily, although those use or benefit are doubtful.

Compared to the previous solution only the image creation is different, the user's commands remain unchanged.

```
FROM openjdk:11

#############
# Get JoramMQ
#############

COPY target/jorammq-mqtt-trial-1.14.0-SNAPSHOT.zip /tmp/jorammq-mqtt-trial-1.14.0-SNAPSHOT.zip
RUN cd /tmp && unzip jorammq-mqtt-trial-1.14.0-SNAPSHOT.zip && mv jorammq-mqtt-1.14.0-SNAPSHOT /home/jorammq && rm -f /tmp/jorammq-mqtt-1.14.0-SNAPSHOT.zip

#############
# Expose ports
#############

EXPOSE 1883/tcp 18090/tcp

#########
# RUN
#########

ENV JORAMMQ_HOME=/home/jorammq
ENV PATH=$JORAMMQ_HOME/bin:$PATH
# This environment variable below allows to launch the JoramMQ server in foreground, it avoids the Docker to exit.
ENV JORAMMQ_IN_DOCKER=true
# Using exec allows to pass the SIGTERM to Java process (pid 1) during container shutdown
WORKDIR $JORAMMQ_HOME
CMD exec /bin/bash bin/jorammq-server

HEALTHCHECK --interval=1m --timeout=10s --retries=3 CMD bin/jorammq-admin -exec "getStatusInfo tcp-1883" | grep "RUNNING"
```

Assuming that the previous DockerFile is in a file named 'JDK11DockerFile2', the command below build a corresponding image named 'jmq1.13-openjdk11' :

```
docker build -t jmq1.13-openjdk11 -f JDK11DockerFile2 .
```

### Creation and start-up of the container

```
docker run -p 1883:1883/tcp -p 18090:18090/tcp jmq1.13-openjdk11
```

### Container life cycle

We can stop JoramMQ (and the container) properly using the administration command (jorammq-admin -stop), or a little more brutally by stopping the container (docker stop).

```
docker exec <containerId> jorammq-admin -stop
docker stop <containerId>
```

In all cases, we restart JoramMQ by starting the container (docker start).

```
docker start <containerId>
```

### Stateless container

It can be useful to build stateless containers, for that it is necessary to mount a volume in the container and to locate the persistence data of JoramMQ in this volume. To do this we will use the JORAMMQ\_DATA\_DIR environment variable (see section 2.3 in documentation).

```
docker run --env JORAMMQ_DATA_DIR=<JoramMQ mount point> -p 1883:1883/tcp -p 18090:18090/tcp -v <path of JoramMQ installation>:<JoramMQ mount point> jmq1.13-openjdk11
```

For example:

```
docker run --env JORAMMQ_DATA_DIR=/home/jorammq-data -p 1883:1883/tcp -p 18090:18090/tcp -v C:\Temp:/home/jorammq-data jmq1.13-openjdk11
```

## 4. Administration and monitoring

### Secure Shell commands

JoramMQ provides a secure shell (ssh) to monitor and control the MQTT server (see chapter 10 of the documentation). This shell can be reached on the exported port 18090:

```
ssh admin@localhost -p 18090
```

### JMX, Jolokia and HawtIo

As described in chapter 8 of the documentation there are multiples ways to monitor JoramMQ. In addition to the configuration operations described, you will obviously have to expose the ports used outside the container.

    
