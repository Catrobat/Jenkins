#!/bin/bash
if [ "$#" -ne 3 ] 
then
  echo "Usage: ./init.sh androidSdkPath bluetoothServerPath deviceSerialNumber"
  exit 1
fi
customScriptsPath=$(dirname $0)
androidSdkPath=$(echo $1 | sed 's/\/$//g')
bluetoothServerPath=$(echo $2 | sed 's/\/$//g')
deviceSerialNumber=$3

$customScriptsPath/onCatroidBuildStart.sh $androidSdkPath $bluetoothServerPath
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.catroid
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.catroid.test
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.catroid.uitest
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.catroid.nativetest
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.test
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.uitest
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.nativetest
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.pocketcode
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.pocketcode.test
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.pocketcode.uitest
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.pocketcode.nativetest
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.pocketcode
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.pocketcode.test
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.pocketcode.uitest
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.catroid.pocketcode.nativetest
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.paintroid
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall at.tugraz.ist.paintroid.test
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.paintroid
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber uninstall org.catrobat.paintroid.test
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber shell rm -r /sdcard/catroid # Legacy
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber shell rm -r /sdcard/pocketcode
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber shell rm -r "/sdcard/Pocket Code"
sudo $androidSdkPath/platform-tools/adb -s $deviceSerialNumber shell rm -r /sdcard/testresults/*

