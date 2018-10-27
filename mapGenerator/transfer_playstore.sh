#!/bin/bash

##############################################################################
##
##  Starcommander@github.com
##  Pauls script to transfer android app!
##  This is necessary because of different app-name on playstore.
##  (sign key was lost by playstore user)
##
##  Moves the package name from com.junjunguo.pocketmaps to com.starcom.pocketmaps
##
##############################################################################

PROJ_PATH="/home/ppp/Desktop/Programmieren/map/PocketMaps/PocketMaps/"

do_transfer()
{
  cd "$PROJ_PATH"
  sed -i -e "s#com.junjunguo.pocketmaps#com.starcom.pocketmaps#g" app/build.gradle
  sed -i -e "s#com.junjunguo.pocketmaps#com.starcom.pocketmaps#g" app/src/main/AndroidManifest.xml
  mv app/src/main/java/com/junjunguo app/src/main/java/com/starcom
  find app/src/main/java/ -name "*.java" -print0 | xargs -0 --max-args 1 --replace="{}" sed -i -e "s/com.junjunguo.pocketmaps/com.starcom.pocketmaps/g" "{}"
}

do_transfer_back()
{
  cd "$PROJ_PATH"
  sed -i -e "s#com.starcom.pocketmaps#com.junjunguo.pocketmaps#g" app/build.gradle
  sed -i -e "s#com.starcom.pocketmaps#com.junjunguo.pocketmaps#g" app/src/main/AndroidManifest.xml
  mv app/src/main/java/com/starcom app/src/main/java/com/junjunguo
  find app/src/main/java/ -name "*.java" -print0 | xargs -0 --max-args 1 --replace="{}" sed -i -e "s/com.starcom.pocketmaps/com.junjunguo.pocketmaps/g" "{}"
}

if [ -z "$1" ]; then
  echo "Use arg t=transfer to transfer for playstore"
  echo "Use arg b=back to transfer back"
elif [ "$1" = "t" ]; then
  do_transfer
  echo "Finish! Now clear tmp data, and build again."
elif [ "$1" = "b" ]; then
  do_transfer_back
  echo "Finish! Now clear tmp data, and build again."
else
  echo "Wrong argument!"
fi
