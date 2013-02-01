#!/bin/bash         

if [ "$#" -ne 1 ] 
then
  echo "Usage: ./onPaintroidBuildStart.sh customScriptPath"
  exit 1
fi 
customScriptPath=$(echo $1 | sed 's/\/$//g')

echo "------ADB------"
adbProcess=`ps aux | grep "adb fork-server server" | grep -v grep`
adbProcessAsRoot=`ps aux | grep "adb fork-server server" | grep root | grep -v grep`
if [ "$adbProcess" =  "" ]
  then echo "adb not running -> starting it"
       sudo customScriptPath/restartADB.sh
elif [ "$adbProcessAsRoot" = "" ]
  then echo "adb does NOT run as root -> restarting adb"
       sudo customScriptPath/restartADB.sh
  else echo "adb does run as root -> OK" 
fi

#success
exit 0

