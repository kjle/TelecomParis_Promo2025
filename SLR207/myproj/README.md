# SLR 207 - Hadoop MapReduce

## Overview
This project demonstrates the implementation of a Hadoop MapReduce, which is used for word counters.

## Project Structure
Here is the structure of the project and the purpose of each folder and file:

```
myproj/
|--- Client/    # client directory
|   |--- src/main/java/rs/MyClient.java   # java src code
|   |--- tarjet/    # target directory for .jar and dependencies
|   |   |--- myclient-1-jar-with-dependencies.jar   # executable java file
|   |   |--- ...
|   |--- pom.xml    # MAVEN pom file
|--- Server
|   |--- src/main/
|   |   |--- java/rs/MyServer.java   # java src code
|   |   |--- resources/log4J.properties # style for logs
|   |--- tarjet/    # target directory for .jar and dependencies
|   |   |--- myserver-1-jar-with-dependencies.jar
|   |   |--- ...
|   |--- pom.xml    # MAVEN pom file
|--- dataset/   # test dataset directory
|   |--- *.warc.wet # test input files
|--- figure/    # output figures directory
|   |--- *.png
|--- summary/   # client log files directory
|   |--- *.txt
|--- result.csv # time excuted for each period
|--- machines.txt   # IPs for distributed servers
|--- deploy_client.sh   # script for deploy Client java files
|--- deploy_new.sh  # script for deploy Server java file to all the server nodes in machines.txt
|--- processing.py  # python script for data collection and draw figures
|--- run.sh    # simply star test
|--- README.md # project documentation
```

## Prerequisites

For JAVA part:
- Java JDK 8 or higher
- Apache Maven

For Python uses for data processing:
- re
- os
- pandas
- matplotlib

## Setup

I provide two methods to run this project. If you don't want to do a lot of configurations, you could just follow [this](#1-use-runsh-to-execute-simply "run.sh"), or follow [this](#2-other-configurations "complex config") to set an entire configurations.

### 1. Use `run.sh` to execute simply 

#### 1.1 Configurations

Move input data files into `./dataset/`.

Modify parameters in `./run.sh`:
```bash
N=20    # how many nodes want to use? / how many iterations?
step=1  # increase of the numbers of nodes uses in this iteration
```

If you use the computers in Télécom Paris, modify the node list by filling with ID of computer
```bash
computers=(01 03 04 05 06 07 12 13 14 15 16 17 18 19 20 22 23 25 26 28 30 31 33 34)
```

else, you should modify the code below to generate a correct node list which included IP address:
```bash
# write machine list
for ((j=0;j<k;j++)); do
    echo "tp-1a252-${computers[$j]}" >> machines.txt
done
```

#### 1.2 Run

Use bash command 
```bash
bash run.sh
```
to execute `run.sh`.

It will generate `result.csv`, `summary/*.txt`, `figure/*.png` automatically.

### 2. Other Configurations

#### 2.1 JAVA Configuration

Configurations for Client: modify these `params` in `Client/src/main/java/rs/MyClient.java`:

```java
private static String usr = "jkang-23";
private static String pwd = "8888";
private static int ftpPort = 8423;
private static int socketPort = 9009;
private static int fileNum = 0; // load how many files? 0 -> all files; x (> 0) -> only x files
private static String localDirPath = "./dataset";   // directory for load test input data
```

Configurations for Server: modify these `params` in `Server/src/main/java/rs/MyServer.java`:
```java
private static String usr = "jkang-23";
private static String pwd = "8888";
private static int ftpPort = 8423;
private static int socketPort = 9009;
```

Make sure that all the common `params` should be defined by a same value!

Configuration for `server nodes`: modify `machines.txt` file by filling with server's IP address. (Each IP address in a line!)

#### 2.2 Build JAVA files

First, you need to build `Client`.

Navigate to the project directory (`myproj/Client/`) and use Maven to build the project:
```bash
mvn clean
mvn compile
mvn assembly:single
```
The last command is uesd to creat a `myclient-1-jar-with-dependencies.jar` file in `myproj/Client/target/`. Make sure that this file include a directory `/rs`. If not, try to run these command together (`mvn clean; mvn compile; mvn assembly:single`).

Then you need to build `Server`. Do the same steps above.

Navigate to the project directory (`myproj/Server/`) and use Maven to build the project:
```bash
mvn clean
mvn compile
mvn assembly:single
```
The last command is uesd to creat a `myserver-1-jar-with-dependencies.jar` file in `myproj/Server/target/`. Make sure that this file include a directory `/rs`. If not, try to run these command together (`mvn clean; mvn compile; mvn assembly:single`).

#### 2.3 Bash Configuration and Run

If you don't want to use bash to deploy the client and server automatically, you could skip this step!

Pre-request: `ssh` should be password free to login to the computers uesd in this project.

First, you need to config `deploy_client.sh` file. This file is uesd to `scp` all files about client to the target computer, and execute `myclient-1-jar-with-dependencies.jar`.
```bash
login="jkang-23"    # usr name to login by using ssh
computer="tp-3a107-09"  # target computer IP address
remoteFolder="~/Desktop/SLR207/myproj"  # target folder of this project in target computer
largeDataNames=("CC-MAIN-20230320083513-20230320113513-00000.warc.wet") # input files
```

Attention: `commandsplit=("ssh" "$login@$computer" "cp /cal/commoncrawl/$largeDataName $dataFolder/;split -n 10 $dataFolder/$largeDataName -d -a 2 $dataFolder/$largeDataName-;rm $dataFolder/$largeDataName;")` This command enables that split all the input files into 10 small files in advance. If You don't need to do this, just remove `split -n 10 $dataFolder/$largeDataName -d -a 2 $dataFolder/$largeDataName-;rm $dataFolder/$largeDataName;`! And if your test input files is not in `/cal/commoncrawl/`, just change it to your folder.

Then, config `deploy_new.sh` file. This file is used to `scp` all files about server to the computers in the `machines.txt`, and execute `myserver-1-jar-with-dependencies.jar`.
If you are a user of Télécom Paris, just modify
```bash
login="jkang-23"    # usr name to login by using ssh
```
else, you should also modify
```bash
remoteFolder="/dev/shm/$login/" # target folder of servers' programmes
```
Make sure that all servers' files are move to the correct folder.

Finally, use bash command
```bash
bash deploy_new.sh
bash deploy_client.sh
```
to execute both server and client progammes.