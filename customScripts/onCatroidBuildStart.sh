#!/bin/bash      

if [ "$#" -ne 2 ] 
then
  echo "Usage: ./onCatroidBuildStart.sh androidSdkPath bluetoothServerPath"
  exit 1
fi   

androidSdkPath=$(echo $1 | sed 's/\/$//g')
bluetoothServerPath=$(echo $2 | sed 's/\/$//g')


echo "------BTSERVER------"

echo "Killing BTSERVER"

killall startbtserver.sh
kill -9 $(ps aux | grep BTServer | grep -v grep | awk '{print $2}')

# search for the btserver process except the one from the current grep
btProcess=`ps aux | grep btserver | grep -v grep`

# if no btserver process is found, start it
if [ "$btProcess" = "" ]
  then echo "btserver not running, going to start it"
       $bluetoothServerPath/startbtserver.sh &
       #wait for btserver to start
       sleep 1

       #check again if its running
       btProcess=`ps aux | grep btserver | grep -v grep`
       if [ "$btProcess" = "" ]
          then echo "btserver NOT running, after starting it -> FAIL"
               exit 1
          else echo "btserver start successful - OK"
       fi
  else echo "btserver is running -> OK"
fi

echo "------ADB------"
adbProcess=`ps aux | grep "adb fork-server server" | grep -v grep`
adbProcessAsRoot=`ps aux | grep "adb fork-server server" | grep root | grep -v grep`
if [ "$adbProcess" =  "" ]
  then echo "adb not running -> starting it"
       sudo ./restartADB.sh $androidSdkPath
elif [ "$adbProcessAsRoot" = "" ]
  then echo "adb does NOT run as root -> restarting adb"
       sudo ./restartADB.sh $androidSdkPath
  else echo "adb does run as root -> OK" 
fi

#success
exit 0

