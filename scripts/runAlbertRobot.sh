#!/bin/bash
if [ "$#" -ne 1 ] 
then
  echo "Usage: ./runAlbertRobot.sh bluetoothAlbertServer"
  exit 1
fi

java -jar $1/bluetoothDummyServerAlbertAndSharedVariables.jar
