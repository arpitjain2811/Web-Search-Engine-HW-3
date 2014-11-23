#!/bin/bash
PRF=./prf/
OUTPUT=qsim.tsv
PREFIX=prf-
RANKER="CONJUNCTIVE"
SUFFIX=.tsv
PRFFILE=prf.tsv

rm -rf $PRF
mkdir $PRF
rm $OUTPUT
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
	curl "http://localhost:25808/prf?query=$q&ranker=$RANKER&numdocs=30&numterms=500" >> $FILE
    if [ ! -e $PRFFILE ]
    then
        echo "Creating file... $PRFFILE"
        touch $PRFFILE
    fi
    echo $q:$FILE >> $PRFFILE
    done < queries.tsv
java -classpath "src:library/*" -Xmx512m edu.nyu.cs.cs2580.Bhattacharyya $PRFFILE $OUTPUT
    	