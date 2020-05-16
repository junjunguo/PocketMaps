#!/bin/bash

WORK_PATH=~/workspace/NetBeansProjects/PocketMaps_git/
WORK_PATH_NB=~/workspace/NetBeansProjects/PocketMaps/
TMP_DIR=$(mktemp -d)

cd "$TMP_DIR"
mkdir -p com/junjunguo/pocketmaps/
cp $WORK_PATH/PocketMaps/app/build/generated/source/r/debug/com/junjunguo/pocketmaps/R.java com/junjunguo/pocketmaps/
javac com/junjunguo/pocketmaps/R.java
zip -r $WORK_PATH_NB/lib/com.junjunguo.pocketmaps.R.jar com
cd

rm -r "$TMP_DIR"
