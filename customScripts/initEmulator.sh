#!/bin/bash

if [ "$#" -ne 2 ] 
then
  echo "Usage: ./initEmulator.sh androidSdkPath port"
  echo ""
  echo "       @port: The port the emulator should run on."
  echo "              The avd image and the serial number will depend on this value" 
  echo "              e.g. port \"1234\" will result in serial \"emulator-1234\""
  echo "              and will start avd image NexusS_1234"	
  exit 1	
fi

customScriptsPath=$(dirname $0)
androidSdkPath=$(echo $1 | sed 's/\/$//g')
port=$2
port2=$((port + 1))

# Kill emulator if running. Prevents starting the same AVD multiple times.
running=$($androidSdkPath/platform-tools/adb -s emulator-$port get-state)
if [ "$running" != "unknown" ]; then
  $androidSdkPath/platform-tools/adb -s emulator-$port emu kill
fi

# Start emulator window on Slave2 display
export DISPLAY=:0
$androidSdkPath/tools/emulator -ports $port,$port2 -avd NexusS_$port -gpu on -qemu -m 512 -enable-kvm &

# Wait for emulator to finish booting
while [ ! "$($androidSdkPath/platform-tools/adb -s emulator-$port shell getprop dev.bootcomplete)" -a "1" ]
do
sleep 1
done

# Unlock screen
$androidSdkPath/platform-tools/adb -s emulator-$port shell input keyevent 82

# Uninstall packages and cleanup sdcard
$customScriptsPath/cleanup.sh $androidSdkPath emulator-$port
