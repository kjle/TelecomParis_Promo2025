#!/bin/bash

N=20
step=1
computers=(01 03 04 05 06 07 12 13 14 15 16 17 18 19 20 22 23 25 26 28 30 31 33 34)

k=1

for ((i=1; i<=N; i++)); do
    echo "------------Running iteration $i------------"
    echo "------------write machines------------"
    # clean up the machines.txt file
    > machines.txt

    # write machine list
    for ((j=0;j<k;j++)); do
        echo "tp-1a252-${computers[$j]}" >> machines.txt
    done
    k=$((k+step))

    echo "------------compile java file------------"
    # use mvn to compile the project
    cd Server
    mvn clean compile assembly:single
    cd ..

    cd Client
    mvn clean compile assembly:single
    cd ..

    echo "------------run deploy_new.sh------------"
    # run deploy_new.sh
    bash deploy_new.sh

    echo "------------run deploy_client.sh------------"
    # run client jar file
    bash deploy_client.sh > summary/output.txt

    wait

    echo "------------save output------------"
    # save the output of the client
    mv summary/output.txt summary/output$i.txt

done

echo "------------Finished running all iterations------------"

