#!/bin/bash
login="jkang-23"
localFolder="."
clientFolder="Client"
targetFolder="$clientFolder/target"
machinePath="$localFolder/machines.txt"

remoteFolder="~/Desktop/SLR207/myproj"
remoteClientFolder="$remoteFolder/$clientFolder"
largeDataNames=("CC-MAIN-20230320083513-20230320113513-00000.warc.wet")
dataFolder="$remoteFolder/dataset"

nameOfTheJarToExecute="myclient-1-jar-with-dependencies.jar"

computer="tp-3a107-09"

command0=("ssh" "$login@$computer" "lsof -ti | xargs kill -9 2>/dev/null; rm -rf $remoteFolder/;mkdir -p $remoteClientFolder;mkdir $dataFolder;")
command1=("scp" "-r" "$targetFolder" "$login@$computer:$remoteClientFolder")
command2=("scp" "$machinePath" "$login@$computer:$remoteFolder/")
command3=("ssh" "-tt" "$login@$computer" "cd $remoteFolder/;sleep 3; java -jar $targetFolder/$nameOfTheJarToExecute")

# echo ${command0[*]}
"${command0[@]}"
for largeDataName in ${largeDataNames[@]}; do
    commandsplit=("ssh" "$login@$computer" "cp /cal/commoncrawl/$largeDataName $dataFolder/;split -n 10 $dataFolder/$largeDataName -d -a 2 $dataFolder/$largeDataName-;rm $dataFolder/$largeDataName;")
    # echo ${commandsplit[*]}
    "${commandsplit[@]}"
done
# echo ${command1[*]}
"${command1[@]}"
# echo ${command2[*]}
"${command2[@]}"
# echo ${command3[*]}
"${command3[@]}"
