# Device Pooling Service
REST service for handling device ids for build jobs.

## Install
### Requirements
* NodeJS - ^v6.5.0
* Node Package Manager (npm) - ^3.10.6

Use ```npm update``` to download and all necessary dependencies to the
'node_modules' directory

## Usage
```node service.js```

this starts up a service which listens to localhost:8081

## Rest API
* /devices - list of all known devices
* /aquire - returns a device id for Use
* /timeout - lists all diveces timed out
