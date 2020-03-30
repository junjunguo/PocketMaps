#!/bin/bash

##############################################################################
##
##  Starcommander@github.com
##  Pauls script to build android app!
##
##  Adapt the first 2 vars below (PROJ_PATH and ANDROID_HOME)
##  Enable debug options on mobile phone and ensure adb is working well.
##  Start the script to build and install app automatically.
##
##############################################################################

PROJ_PATH="/home/ppp/workspace/NetBeansProjects/PocketMaps_git/PocketMaps/"
export ANDROID_HOME="/home/ppp/workspace/Android-SDK/android-sdk-linux/"
PACK_NAME=$(cat "$PROJ_PATH/app/src/main/AndroidManifest.xml" | grep "package=" | cut -d'"' -s -f 2)
CUR_ARGS="build"
TMP_COPY="false"

check_exists() # Args: dir|file
{
  if [ -e "$1" ]; then
    echo "" > /dev/null
  else
    echo "Error, Path does not exist: $1"
    exit 1
  fi
}

check_exists_var() # Args: var
{
  if [ ! -z "$1" ]; then
    echo "" > /dev/null
  else
    echo "Error, Var does not exist: $1"
    exit 1
  fi
}

copy_project_tmp()
{
  if [ "$TMP_COPY" != "true" ]; then
    return
  fi
  if [ -e /tmp/TmpPocketMaps/app/src ]; then
    rm -r /tmp/TmpPocketMaps/app/src
    cp -r "$PROJ_PATH/app/src" /tmp/TmpPocketMaps/app/src
    cp "$PROJ_PATH/app/src/main/AndroidManifest.xml" /tmp/TmpPocketMaps/app/src/main/AndroidManifest.xml
    cp "$PROJ_PATH/app/build.gradle" /tmp/TmpPocketMaps/app/build.gradle
  else
    if [ -e /tmp/TmpPocketMaps ]; then
      rm -r /tmp/TmpPocketMaps
    fi
    cp -r "$PROJ_PATH" /tmp/TmpPocketMaps/
  fi
  PROJ_PATH="/tmp/TmpPocketMaps/"
}


if [ ! -z "$*" ]; then
  CUR_ARGS=$*
fi

check_exists "$ANDROID_HOME"
check_exists_var "$PACK_NAME"
check_exists "$PROJ_PATH"

copy_project_tmp

cd "$PROJ_PATH"
#./gradlew clean build
./gradlew $CUR_ARGS

echo "Install on device? [y/n]"
read -e -p ">>>" cur_input

if [ "$cur_input" = "y" ]; then
  cd "$ANDROID_HOME"
  cd platform-tools
  APK_FILE="$PROJ_PATH/app/build/outputs/apk/debug/app-debug.apk"
  check_exists "$APK_FILE"
  ./adb uninstall "$PACK_NAME"
  ./adb install "$APK_FILE"
fi

echo "LogCat on device? [y/n]"
read -e -p ">>>" cur_input

if [ "$cur_input" = "y" ]; then
  cd "$ANDROID_HOME"
  cd platform-tools
  ./adb logcat | grep -i "com.graphhopper\|exception\|---GH\|junjunguo"
fi
