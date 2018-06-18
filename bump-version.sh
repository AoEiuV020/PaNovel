#!/bin/sh
# ./bump-version 下个版本，
# 改版本号和版本名，然后提交，
set -e
old=$PWD
cd $(dirname $0)
project=$(pwd)
buildGradleFile="$project/build.gradle"

versionCode=$(sed -n 's/\s*version_code\s*=\s*\(\S*\)/\1/p' $buildGradleFile)
versionCode=$(expr $versionCode + 1)

sed -i "s/version_code\\s*=\\s*\\d*/version_code = $versionCode/" $buildGradleFile

versionName=$1
sed -i "s/version_name\\s*=\\s*\".*\"/version_name = \"$versionName\"/" $buildGradleFile
changeLogFile=app/src/main/assets/ChangeLog.txt
sed -i "1G" $changeLogFile
sed -i "2i$versionName:" $changeLogFile

git add $buildGradleFile
git add $changeLogFile
git commit -m "Bumped version number to $versionName"

cd $old
