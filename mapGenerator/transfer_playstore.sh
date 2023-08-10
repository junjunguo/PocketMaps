#!/bin/bash

##############################################################################
##
##  Starcommander@github.com
##  Pauls script to transfer android app!
##  This is necessary because of different app-name on playstore.
##  (sign key was lost by playstore user)
##  Also it is useful for creating a beta app with different name.
##
##  For example:
##  Moves the package name from com.junjunguo.pocketmaps to com.starcom.pocketmaps or com.junjunguo.pocketbetamaps
##
##############################################################################

RELEASE_KEY="/home/ppp/workspace/Android-SDK/key_google/google-release-key.keystore"
MY_ANDROID_SDK="/home/ppp/workspace/Android-SDK/android-sdk-linux/"

do_set_vars() # Args: pack_name proj_dir
{
  PROJ_PATH="$2/PocketMaps/"
  if [ ! -d "$PROJ_PATH" ]; then
    PROJ_PATH="$2/" # Is already sub-dir
  fi
  NEW_NAME="$1"
  NEW_DIR=$(echo $NEW_NAME | cut -d'.' -s -f 2)
  NEW_DIR_SUB=$(echo $NEW_NAME | cut -d'.' -s -f 3)
  NEW_DIR_SUP=$(echo $NEW_NAME | cut -d'.' -s -f 1)
}

do_transfer()
{
  cd "$PROJ_PATH"
  sed -i -e "s#com.junjunguo.pocketmaps#$NEW_NAME#g" app/build.gradle
  sed -i -e "s#com.junjunguo.pocketmaps#$NEW_NAME#g" app/src/main/AndroidManifest.xml
  if [ "$NEW_DIR" != "junjunguo" ]; then
    mv app/src/main/java/com/junjunguo app/src/main/java/com/$NEW_DIR
  fi
  if [ "$NEW_DIR_SUB" != "pocketmaps" ]; then
    mv app/src/main/java/com/$NEW_DIR/pocketmaps app/src/main/java/com/$NEW_DIR/$NEW_DIR_SUB
    find app/src/main/res/ -name "strings.xml" -print0 | xargs -0 --replace sed -i -e 's#>PocketMaps<#>PocketBetaMaps<#g' '{}'
  fi
  if [ "$NEW_DIR_SUP" != "com" ]; then
    mv app/src/main/java/com app/src/main/java/$NEW_DIR_SUP
  fi
  find app/src/main/java/ -name "*.java" -print0 | xargs -0 --replace="{}" sed -i -e "s/com.junjunguo.pocketmaps/$NEW_NAME/g" "{}"
}

do_transfer_back()
{
  cd "$PROJ_PATH"
  sed -i -e "s#$NEW_NAME#com.junjunguo.pocketmaps#g" app/build.gradle
  sed -i -e "s#$NEW_NAME#com.junjunguo.pocketmaps#g" app/src/main/AndroidManifest.xml
  if [ "$NEW_DIR" != "junjunguo" ]; then
    mv app/src/main/java/com/$NEW_DIR app/src/main/java/com/junjunguo
  fi
  if [ "$NEW_DIR_SUB" != "pocketmaps" ]; then
    mv app/src/main/java/com/$NEW_DIR/$NEW_DIR_SUB app/src/main/java/com/$NEW_DIR/pocketmaps
    find app/src/main/res/ -name "strings.xml" -print0 | xargs -0 --replace sed -i -e 's#>PocketBetaMaps<#>PocketMaps<#g' '{}'
  fi
  if [ "$NEW_DIR_SUP" != "com" ]; then
    mv app/src/main/java/$NEW_DIR_SUP app/src/main/java/com
  fi
  find app/src/main/java/ -name "*.java" -print0 | xargs -0 --replace="{}" sed -i -e "s/$NEW_NAME/com.junjunguo.pocketmaps/g" "{}"
}

do_sign()
{
  jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore "$RELEASE_KEY" $PROJ_PATH/app/build/outputs/apk/release/app-release-unsigned.apk starcommander
  if [ "$?" != "0" ]; then
    echo "Signing error!" 1>&2
    exit 2
  elif [ -d "$MY_ANDROID_SDK" ]; then
    $MY_ANDROID_SDK/build-tools/27.0.1/zipalign -v -p 4 $PROJ_PATH/app/build/outputs/apk/release/app-release-unsigned.apk $PROJ_PATH/app/build/outputs/apk/release/app-release.apk
  elif [ -d "$ANDROID_SDK" ]; then
    local za_tool=$(find "$ANDROID_SDK/build-tools/" -name zipalign | head -n 1)
    if [ -z "$za_tool" ]; then
      echo "Error, no zipalign-tool found in android sdk."
      echo "Maybe no target installed there?"
    else
      "$za_tool" -v -p 4 $PROJ_PATH/app/build/outputs/apk/release/app-release-unsigned.apk $PROJ_PATH/app/build/outputs/apk/release/app-release.apk
    fi
  else
    echo "Error, Android SDK not fount. Please set the environment-variable correctly: ANDROID_SDK"
  fi
}

if [ -z "$1" ]; then
  echo "No arguments used."
  echo "=================================="
  echo "Arguments for modify package name:"
  echo "  t <new.package.name> <target.dir>"
  echo "Example args:"
  echo "  t com.starcom.pocketmaps ./"
  echo "  t com.junjunguo.pocketbetamaps ./"
  echo "Arguments for revert modification:"
  echo "  b <new.package.name> <target.dir>"
  echo "Example args:"
  echo "  b com.starcom.pocketmaps ./"
  echo "  b com.junjunguo.pocketbetamaps ./"
  echo "Arguments for sign the apk"
  echo "  s <target.dir>"
  echo "=================================="
elif [ "$1" = "t" ]; then
  do_set_vars "$2" "$3"
  do_transfer
  echo "Finish! Now build again."
elif [ "$1" = "b" ]; then
  do_set_vars "$2" "$3"
  do_transfer_back
  echo "Finish! Now build again."
elif [ "$1" = "s" ]; then
  do_set_vars "com.junjunguo.pocketmaps" "$2"
  do_sign
  echo "Finish! Created file: app-release.apk"
else
  echo "Wrong argument!"
fi
