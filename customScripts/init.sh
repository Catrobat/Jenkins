/home/jenkins/jenkins/customScripts/onCatroidBuildStart.sh
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d at.tugraz.ist.catroid
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d at.tugraz.ist.catroid.test
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d at.tugraz.ist.catroid.uitest
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d at.tugraz.ist.catroid.nativetest
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d org.catrobat.catroid
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d org.catrobat.catroid.test
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d org.catrobat.catroid.uitest
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d org.catrobat.catroid.nativetest
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d at.tugraz.ist.paintroid
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d at.tugraz.ist.paintroid.test
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d org.catrobat.paintroid
sudo /home/catroid/android-sdk-linux/platform-tools/adb uninstall -d org.catrobat.paintroid.test
sudo /home/catroid/android-sdk-linux/platform-tools/adb shell rm -r /sdcard/catroid
sudo /home/catroid/android-sdk-linux/platform-tools/adb shell rm -r /sdcard/testresults/*

