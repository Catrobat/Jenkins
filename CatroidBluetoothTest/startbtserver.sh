#!/bin/sh
BASEDIR=$(dirname $0)
echo $BASEDIR
java -cp $BASEDIR/bluecove-2.1.1-SNAPSHOT.jar:$BASEDIR/bluecove-gpl-2.1.1.jar:$BASEDIR/BTTestNOGUI.jar at.tugraz.ist.catroid.LegoNXTBTTestServer.BTServer $BASEDIR/log.txt 
