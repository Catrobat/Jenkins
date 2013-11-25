#!/bin/bash
if [ "$#" -lt 2 ] 
then
  echo "Usage: ./cleanup.sh androidSdkPath deviceSerialNumber"
  exit 1
fi

androidSdkPath=$(echo $1 | sed 's/\/$//g')
deviceSerialNumber=$2

if [ $3 ]; then
  export ANDROID_ADB_SERVER_PORT=$3
fi

$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.catroid
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.catroid.test
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.catroid.uitest
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.catroid.nativetest
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.test
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.uitest
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.nativetest
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.pocketcode
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.pocketcode.test
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.pocketcode.uitest
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.pocketcode.nativetest
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.pocketcode
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.pocketcode.test
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.pocketcode.uitest
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.pocketcode.nativetest
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.paintroid
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.paintroid.test
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.paintroid
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.paintroid.test
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber shell rm -r /sdcard/catroid # Legacy
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber shell rm -r /sdcard/pocketcode
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber shell rm -r "/sdcard/Pocket Code"
$androidSdkPath/platform-tools/adb -s $deviceSerialNumber shell rm -r /sdcard/testresults/*
