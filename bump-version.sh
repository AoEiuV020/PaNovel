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

git add $buildGradleFile
git commit -m "Bumped version number to $versionName"

cd $old
