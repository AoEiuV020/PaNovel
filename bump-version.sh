#!/bin/sh
# ./bump-version 下个版本，
# 自动建个分支合并到master，
# 然后改版本号和版本名，
set -e
old=$PWD
cd $(dirname $0)
project=$(pwd)
buildGradleFile="$project/app/build.gradle"

versionCode=$(cat $buildGradleFile |grep versionCode |awk '{print $2'})
versionCode=$(expr $versionCode + 1)
versionName=$(sed -n 's/\s*versionName "\(\S*\)"/\1/p' app/build.gradle)

branch=release-$versionName
git checkout -b $branch
git checkout master
git merge --no-ff $branch
git tag -a $versionName
git checkout dev
git merge master

sed -i "s/versionCode\\s*[0-9]*/versionCode $versionCode/" $buildGradleFile

versionName=$1
sed -i "s/versionName\\s*\".*\"/versionName \"$versionName\"/" $buildGradleFile
changeLogFile=app/src/main/assets/ChangeLog.txt
sed -i "2i$versionName:" $changeLogFile

git add $buildGradleFile
git add $changeLogFile
git commit -m "Bumped version number to $versionName"

cd $old
