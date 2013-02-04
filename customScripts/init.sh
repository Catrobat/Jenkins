#!/bin/bash
if [ "$#" -ne 3 ] 
then
  echo "Usage: ./init.sh customScriptsPath androidSdkPath bluetoothServerPath"
  exit 1
fi
customScriptsPath=$(echo $1 | sed 's/\/$//g')
androidSdkPath=$(echo $2 | sed 's/\/$//g')
bluetoothServerPath=$(echo $3 | sed 's/\/$//g')

$customScriptsPath/onCatroidBuildStart.sh $customScriptsPath $bluetoothServerPath
sudo $androidSdkPath/platform-tools/adb uninstall -d at.tugraz.ist.catroid
sudo $androidSdkPath/platform-tools/adb uninstall -d at.tugraz.ist.catroid.test
sudo $androidSdkPath/platform-tools/adb uninstall -d at.tugraz.ist.catroid.uitest
sudo $androidSdkPath/platform-tools/adb uninstall -d at.tugraz.ist.catroid.nativetest
sudo $androidSdkPath/platform-tools/adb uninstall -d org.catrobat.catroid
sudo $androidSdkPath/platform-tools/adb uninstall -d org.catrobat.catroid.test
sudo $androidSdkPath/platform-tools/adb uninstall -d org.catrobat.catroid.uitest
sudo $androidSdkPath/platform-tools/adb uninstall -d org.catrobat.catroid.nativetest
sudo $androidSdkPath/platform-tools/adb uninstall -d at.tugraz.ist.paintroid
sudo $androidSdkPath/platform-tools/adb uninstall -d at.tugraz.ist.paintroid.test
sudo $androidSdkPath/platform-tools/adb uninstall -d org.catrobat.paintroid
sudo $androidSdkPath/platform-tools/adb uninstall -d org.catrobat.paintroid.test
sudo $androidSdkPath/platform-tools/adb shell rm -r /sdcard/catroid
sudo $androidSdkPath/platform-tools/adb shell rm -r /sdcard/testresults/*

