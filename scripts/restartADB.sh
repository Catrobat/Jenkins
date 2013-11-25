#!/bin/bash



if [ "$#" -lt 1 ] 
then
  echo "Usage: ./restartADB.sh androidSdkPath"
  exit 1
fi
androidSdkPath=$(echo $1 | sed 's/\/$//g')
if [ $2 ]; then
  export ANDROID_ADB_SERVER_PORT=$2
fi

$androidSdkPath/platform-tools/adb kill-server
$androidSdkPath/platform-tools/adb start-server

