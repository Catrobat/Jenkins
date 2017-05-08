#!/bin/bash
#
# Creates backups of jenkins in /home/jenkins/jenkins_created_backups.
#
# The backup directories are named in the form yyyy-MM-dd
# Backups that were created more than 7 days ago are removed.
#

JENKINS_USER_HOME="/home/jenkins"
JENKINS_DIR="$JENKINS_USER_HOME/jenkins"
BACKUP_PARENT_DIR="$JENKINS_USER_HOME/jenkins_created_backups"
BACKUP_DIR="$BACKUP_PARENT_DIR/$(date -u '+%F')"
NOW=$(date -u '+%s')
SEVEN_DAYS=$(expr 7 \* 24 \* 3600)
TOO_OLD_BACKUPS=$(expr $NOW - $SEVEN_DAYS)

echo "==== Creating backup to '$BACKUP_DIR' ..."
mkdir -p $BACKUP_PARENT_DIR
rsync -av $JENKINS_DIR $BACKUP_DIR

if [ $? -ne 0 ]; then
    echo "==== Encountered errors during backup creation" >&2
    exit 1
fi

echo "==== Looking for old backups and removing them"
for BACKUP in $BACKUP_PARENT_DIR/20*/; do
    BACKUP_DATE=$(date -u -d $(basename $BACKUP) '+%s')
    if [ "$BACKUP_DATE" -lt "$TOO_OLD_BACKUPS" ]; then
        echo "==== Backup '$BACKUP' is too old, removing ..."
        rm -rf $BACKUP
    fi
done

echo "==== Finished"
exit 0
