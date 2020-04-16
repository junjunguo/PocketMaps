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

PROJ_PATH="/home/ppp/workspace/NetBeansProjects/PocketMaps_git/PocketMaps/"
RELEASE_KEY="/home/ppp/workspace/Android-SDK/key_google/google-release-key.keystore"
ANDROID_SDK="/home/ppp/workspace/Android-SDK/android-sdk-linux/"

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

do_sign()
{
  jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore "$RELEASE_KEY" $PROJ_PATH/app/build/outputs/apk/release/app-release-unsigned.apk starcommander
  $ANDROID_SDK/build-tools/27.0.1/zipalign -v -p 4 $PROJ_PATH/app/build/outputs/apk/release/app-release-unsigned.apk $PROJ_PATH/app/build/outputs/apk/release/app-release.apk
}

if [ -z "$1" ]; then
  echo "Use arg t=transfer to transfer for playstore"
  echo "Use arg b=back to transfer back"
  echo "Use arg s=sign to sign the apk"
elif [ "$1" = "t" ]; then
  do_transfer
  echo "Finish! Now build again."
elif [ "$1" = "b" ]; then
  do_transfer_back
  echo "Finish! Now build again."
elif [ "$1" = "s" ]; then
  do_sign
  echo "Finish! Created file: app-release.apk"
else
  echo "Wrong argument!"
fi
