#!/bin/bash

if [ "$#" -lt 2 ] 
then
  echo "Usage: ./captureContiuusEmulatorScreenshots.sh emulatorUserPort path/name.png"
  echo ""
  echo "       @emulatorUserPort: The adb avd port the emulator uses."
  echo "              If you use the jenkins android emulator plugin this value can be read from the ANDROID_AVD_USER_PORT variable" 
  exit 1	
fi

SLEEP_TIME=6

EMULATOR_SEARCH_STRING="$1:"
SCREENSHOT_FILENAME=$2

echo "Starting continuus screenshots, press [CTRL+C] to stop"

window_not_found=0

while true
do
	sleep $SLEEP_TIME
	
	if [ "$window_not_found" -ge 3 ] ; then
  	echo "Emulator window not found for some time and jenkins didn't kill us, exiting"
  	exit 0
	fi
	
	emulator_window_id=$(wmctrl -l | grep $EMULATOR_SEARCH_STRING | awk '{print $1}')
	if [ ! -n "$emulator_window_id" ]; then
    window_not_found=$(( window_not_found + 1 ))
    echo "Emulator window not found #$window_not_found"
    continue
	fi
	
	window_not_found=0
	
	import -window "$emulator_window_id" $SCREENSHOT_FILENAME
	convert $SCREENSHOT_FILENAME -thumbnail 150x150^ $SCREENSHOT_FILENAME-thumb.png
	
done


