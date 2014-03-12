#!/bin/bash      

if [ "$#" -ne 2 ] 
then
  echo "Usage: ./runLint.sh androidSdkPath workspace"
  exit 1
fi

androidSdkPath=$(echo $1 | sed 's/\/$//g')
workspace=$(echo $2 | sed 's/\/$//g')

echo "------ LINT CHECKER ------"

$androidSdkPath/tools/lint --config $workspace/catroid/lint.xml --showall --xml $workspace/reports/catroid_lint_results.xml --classpath catroid/build/classes/debug/ catroid

