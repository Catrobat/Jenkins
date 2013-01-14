#!/bin/bash

customScriptPath=$(echo $1 | sed 's/\/$//g')
androidSDKPath=$(echo $2 | sed 's/\/$//g')
bluetoothServerPath=$(echo $3 | sed 's/\/$//g')

$customScriptPath/onCatroidBuildStart.sh $customScriptPath $bluetoothServerPath
sudo $androidSDKPath/platform-tools/adb uninstall -d at.tugraz.ist.catroid
sudo $androidSDKPath/platform-tools/adb uninstall -d at.tugraz.ist.catroid.test
sudo $androidSDKPath/platform-tools/adb uninstall -d at.tugraz.ist.catroid.uitest
sudo $androidSDKPath/platform-tools/adb uninstall -d at.tugraz.ist.catroid.nativetest
sudo $androidSDKPath/platform-tools/adb uninstall -d org.catrobat.catroid
sudo $androidSDKPath/platform-tools/adb uninstall -d org.catrobat.catroid.test
sudo $androidSDKPath/platform-tools/adb uninstall -d org.catrobat.catroid.uitest
sudo $androidSDKPath/platform-tools/adb uninstall -d org.catrobat.catroid.nativetest
sudo $androidSDKPath/platform-tools/adb uninstall -d at.tugraz.ist.paintroid
sudo $androidSDKPath/platform-tools/adb uninstall -d at.tugraz.ist.paintroid.test
sudo $androidSDKPath/platform-tools/adb uninstall -d org.catrobat.paintroid
sudo $androidSDKPath/platform-tools/adb uninstall -d org.catrobat.paintroid.test
sudo $androidSDKPath/platform-tools/adb shell rm -r /sdcard/catroid
sudo $androidSDKPath/platform-tools/adb shell rm -r /sdcard/testresults/*

