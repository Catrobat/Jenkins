#!/bin/bash

#kill any running emulator processes
runningEmulators=`ps aux | grep -i emulator64-x86  | grep -v grep`
echo "$runningEmulators"
if [ -z "$runningEmulators" ] 
then
  echo "no emulators running - nothing to do"
else
  echo "killing all running emulator processes"
  kill -9 $(ps aux | grep -i emulator64-x86 | grep -v grep | awk '{print $2}')
fi
