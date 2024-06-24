# SLR 207 - Hadoop MapReduce

## Overview
This project demonstrates the implementation of a Hadoop MapReduce, which is used for word counters.

## Project Structure
Here is the structure of the project and the purpose of each folder and file:

```
Myproj/
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

### 1. Use `run.sh` to execute simply 

#### Configurations

Move input data files into `./dataset`.

Modify parameters for `./run.sh`:
```bash
N=20    # how many nodes want to use? / how many iterations?
step=1  # increase of the numbers of nodes uses in this iteration
```

If use the computers in Télécom Paris, modify the node list by filling with ID of computer
```bash
computers=(01 03 04 05 06 07 12 13 14 15 16 17 18 19 20 22 23 25 26 28 30 31 33 34)
```

else, it should modify the code below to generate a correct node list which included IP address:
```bash
# write machine list
for ((j=0;j<k;j++)); do
    echo "tp-1a252-${computers[$j]}" >> machines.txt
done
```

#### Run

Use bash command 
```bash
bash run.sh
```
to execute `run.sh`.

It will generate `result.csv`, `summary/*.txt`, `figure/*.png` automatically.

### 1. JAVA Configuration

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

Make sure that all the common `params` should be set a same value!

Configuration for `server nodes`: modify `machines.txt` file by filling with server's IP address.

### 2. Build the Project
Navigate to the project directory and use Maven to build the project:

```bash
mvn clean install
```