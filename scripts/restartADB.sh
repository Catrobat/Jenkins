#!/bin/bash

if [ "$#" -ne 1 ] 
then
  echo "Usage: ./restartADB.sh androidSdkPath"
  exit 1
fi
androidSdkPath=$(echo $1 | sed 's/\/$//g')

$androidSdkPath/platform-tools/adb kill-server
$androidSdkPath/platform-tools/adb start-server

