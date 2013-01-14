#!/bin/bash
androidSdkPath=$(echo $1 | sed 's/\/$//g')

$androidSdkPath/platform-tools/adb kill-server
$androidSdkPath/platform-tools/adb start-server

