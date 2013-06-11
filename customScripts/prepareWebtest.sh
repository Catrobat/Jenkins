#!/bin/bash

if [ "$#" -ne 1 ] 
then
  echo "Usage: ./prepareWebtest.sh [master|customBranch]"
  exit 1
fi

if [ "$1" == "master" ]
then
  echo "hello"
elif [ "$1" == "customBranch" ]
then
  echo "goodbye"
fi  
