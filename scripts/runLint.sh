#!/bin/bash      

if [ "$#" -ne 2 ] 
then
  echo "Usage: ./runLint.sh androidSdkPath workspace"
  exit 1
fi

androidSdkPath=$(echo $1 | sed 's/\/$//g')
workspace=$(echo $2 | sed 's/\/$//g')

echo "------ LINT CHECKER ------"

$androidSdkPath/tools/lint --config $workspace/catroid/lint.xml --xml $workspace/reports/catroid_lint_results.xml catroid
$androidSdkPath/tools/lint --config $workspace/catroidTest/lint.xml --xml $workspace/reports/catroidTest_lint_results.xml catroidTest
$androidSdkPath/tools/lint --config $workspace/catroidUiTest/lint.xml --xml $workspace/reports/catroidUiTest_lint_results.xml catroidUiTest
