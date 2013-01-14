#!/bin/bash         

echo "------ADB------"
adbProcess=`ps aux | grep "adb fork-server server" | grep -v grep`
adbProcessAsRoot=`ps aux | grep "adb fork-server server" | grep root | grep -v grep`
if [ "$adbProcess" =  "" ]
  then echo "adb not running -> starting it"
       sudo /home/jenkins/jenkins/customScripts/restartADB.sh
elif [ "$adbProcessAsRoot" = "" ]
  then echo "adb does NOT run as root -> restarting adb"
       sudo /home/jenkins/jenkins/customScripts/restartADB.sh
  else echo "adb does run as root -> OK" 
fi

#success
exit 0

