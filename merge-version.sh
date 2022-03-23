#!/bin/sh
# 自动建个release分支合并到master，
# 然后删除这个分支，
set -e
old=$PWD
cd $(dirname $0)
project=$(pwd)
buildGradleFile="$project/build.gradle"
versionFile="$project/version.properties"

versionName=$(sed -n 's/\s*version_name\s*=\s*\(\S*\)/\1/p' $versionFile)
branch=release-$versionName
changeLog=$(./latest-changelog.sh $versionName)
mergeLog=$(echo "Merge branch '$branch'\n\n$changeLog")
tagLog=$(echo "$changeLog" |sed '1s/[，,]$//;1s/^\(.*\)$/\1\n/')

git checkout -b $branch
git checkout master
git merge --no-ff $branch -m "$mergeLog"
git branch -d $branch
git tag -a $versionName -m "$tagLog"
git checkout dev
git merge master

