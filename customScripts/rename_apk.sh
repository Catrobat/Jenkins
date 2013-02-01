#!/bin/bash
#!/bin/bash
if [ "$#" -ne 4 ] 
then
  echo "Usage: ./rename_apk.sh path_to_bin apk_temp_name apk_prefix android_sdk_path"
  exit 1
fi
PATH_TO_BIN=$1
APK_TEMP_NAME=$2
APK_PREFIX=$3
ANDROID_SDK_PATH=$4
APK_NAME=$APK_PREFIX-$($ANDROID_SDK_PATH/platform-tools/aapt dump badging $PATH_TO_BIN$APK_TEMP_NAME | sed -En "s/.*versionName='(.*)'.*/\1/p").apk
echo $APK_NAME
mv $PATH_TO_BIN$APK_TEMP_NAME $PATH_TO_BIN$APK_NAME
