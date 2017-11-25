#!/bin/sh
old=$PWD
cd $(dirname $0)
project=$(pwd)
buildGradleFile="$project/app/build.gradle"

versionCode=$(cat $buildGradleFile |grep versionCode |awk '{print $2'})
versionCode=$(expr $versionCode + 1)
sed -i "s/versionCode\\s*[0-9]*/versionCode $versionCode/" $buildGradleFile

versionName=$1
sed -i "s/versionName\\s*\".*\"/versionName \"$versionName\"/" $buildGradleFile
changeLogFile=app/src/main/assets/ChangeLog.txt
sed -i "2i$versionName:" $changeLogFile

git add $buildGradleFile
git add $changeLogFile
git commit -m "Bumped version number to $versionName"

cd $old
