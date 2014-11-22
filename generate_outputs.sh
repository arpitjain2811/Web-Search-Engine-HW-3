#!/bin/bash
PRF=./prf/
OUTPUT=./Qsim/
PREFIX=prf-
RUNFLAGS =-classpath
RANKER="conjunctive"
SUFFIX=.tsv


rm -rf $PRF
mkdir $PRF
rm -rf $OUTPUT
mkdir $OUTPUT
rm -f prf*.tsv
i=0
while read q ; do
	i=$((i + 1));
	FILE=$PRF$PREFIX$i$SUFFIX;
	if [ ! -e $FILE ]
	then
    	echo "Creating file... $FILE"
    	touch $FILE
	fi
	curl "http://localhost:25808/prf?query=$q&ranker=$RANKER&numdocs=10&numterms=5" >> $FILE
done < queries.tsv
java -classpath "src:library/*" -Xmx512m edu.nyu.cs.cs2580.Bhattacharyya $PRF $OUTPUT
    	