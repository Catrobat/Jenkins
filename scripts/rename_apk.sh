#!/bin/bash

if [ "$#" -ne 4 ] 
then
  echo "Usage: ./rename_apk.sh pathToBin apkTempName apkPrefix androidSdkPath"
  exit 1
fi
pathToBin=$1
apkTempName=$2
apkPrefix=$3
androidSdkPath=$4
apkName=$apkPrefix-$($androidSdkPath/platform-tools/aapt dump badging $pathToBin$apkTempName | sed -En "s/.*versionName='(.*)'.*/\1/p").apk
echo $apkName
mv $pathToBin$apkTempName $pathToBin$apkName
