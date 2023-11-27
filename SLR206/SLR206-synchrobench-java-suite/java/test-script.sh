#!/bin/bash

case $1 in
	"1" )	echo CoarseGrainedListBasedSet
			OUTPUT="CoarseGrainedListBasedSet"
	;;
	"2" )	echo HandOverHandListIntSet
			OUTPUT="HandOverHandListIntSet"
	;;
	"3" )	echo LazyLinkedListSortedSet
			OUTPUT="LazyLinkedListSortedSet"
	;;
	*)		echo "Specify algorithm"
			exit 0
esac

echo "Who I am: $OUTPUT on `uname -n`"
echo "started on" `date`

for i in 1 4 6 8 10 12
do
	for j in 0 10 100
	do
		for k in 100 1000 10000
		do
			h=`expr 2 \* $k`
			echo "→	$OUTPUT	$i	$j	$k	$h"
			java -cp BenchBin contention.benchmark.Test -b linkedlists.lockbased.$OUTPUT -d 2000 -t $i -u $j -i $k -r $h -W 0 | grep Throughput
#			echo "→ $OUTPUT	$i	$j	without -W 0"
#			java -cp bin contention.benchmark.Test -b linkedlists.lockbased.$OUTPUT -d 3000 -t $i -u $j -i 1024 -r 2048 | grep Throughput
		done

	done 
	# wait
done
echo "finished on" `date`
echo "DONE \o/"

