#!/bin/sh
cur="$(dirname $0)"
cd $cur
changeLogFile=app/src/main/assets/ChangeLog.txt
cat $changeLogFile |head -2 |tail -1 |sed  's/\(.*\):/\1/'
