#!/bin/bash

if [ "$#" -ne 1 ] 
then
  echo "Usage: ./prepareWebtest.sh [master|customBranch]"
  exit 1
fi

if [ "$1" == "master" ]
then
  ln -s /etc/apache2/sites-enabled/catroweb-master 001-catroweb
elif [ "$1" == "customBranch" ]
then
  ln -s /etc/apache2/sites-enabled/catroweb-customBranch 001-catroweb
fi  
