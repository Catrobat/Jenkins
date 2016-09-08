var _ = require('underscore');
var Dict = require("collections/dict");

var express = require('express');
var app = express();

var adb = require('adbkit');
var client = adb.createClient();

// Global Variables
const TIMEOUT_IN_SEC = 35;
const AQUIRE_RETRY_IN_SEC = 11;

// Device States
const DEVICE_CONNECTED = 'DEVICE_CONNECTED';
const DEVICE_IN_USE = 'DEVICE_IN_USE';
const DEVICE_DISCONNECTED = 'DEVICE_DISCONNECTED';
const DEVICE_TIMEOUT = 'DEVICE_TIMEOUT';

// Device Map
// Returns DEVICE_DISCONNECTED if device is not mapped
var devices = new Dict({}, k => DEVICE_DISCONNECTED);

client.trackDevices()
    .then(function (tracker) {
        tracker.on('add', function (device) { //Called on Startup for each connected device
            if (device.type != 'device')
                return;

            console.log('Device %s was plugged in', device.id);

            if(devices.get(device.id) != DEVICE_DISCONNECTED)
                resetDevice(device.id);

            devices.set(device.id, DEVICE_CONNECTED);

            //console.log(devices);
        });
        tracker.on('remove', function (device) {
            if (device.type != 'device')
                return;

            console.log('Device %s was unplugged', device.id);
            devices.set(device.id, DEVICE_DISCONNECTED);
        });
        tracker.on('end', function () {
            console.log('Tracking stopped');
        })
    })
    .catch(function (err) {
        console.error('Something went wrong:', err.stack);
    });

app.get('/devices', function (req, res) {
    res.end(JSON.stringify(devices));
});

app.get('/timeout', function (req, res) {
    res.end(JSON.stringify(devices.filter(v => v == DEVICE_TIMEOUT)));
});

app.get('/aquire', function (req, res) {
    aquireDevice(res);
});

app.get('/release', function (req, res) {
    // TODO read Request Parameters
});


// Callback Functions
// adb device reset function
function resetDevice(deviceId){
    console.log(deviceId, 'reseted');
}

// called after timeout
function timeoutDevice(deviceId){
    console.log(deviceId, 'timeout detected');
    devices.set(deviceId, DEVICE_TIMEOUT);
}

//
function handleTimeout(deviceId){
    resetDevice(deviceId);
    devices.set(deviceId, DEVICE_CONNECTED);
}

// Handler for aquire request
function aquireDevice(response){
    // handle timeout for devices in timeout state
    Array.from(devices.filter(v => v == DEVICE_TIMEOUT).keys()).forEach(handleTimeout);

    // Get all connected(not in use) devices
    const openDevices = Array.from(devices.filter(v => v == DEVICE_CONNECTED).keys());

    // Block request and retry again
    if(openDevices.length <= 0) {
        setTimeout(() => aquireDevice(response), AQUIRE_RETRY_IN_SEC);
        return;
    }

    const rndIdx = Math.floor(Math.random() * openDevices.length);
    const deviceId = openDevices[rndIdx];

    setTimeout(() => timeoutDevice(deviceId), TIMEOUT_IN_SEC * 1000);
    devices.set(deviceId, DEVICE_IN_USE);

    console.log(deviceId, 'acquired');

    response.end(deviceId);
}

// Startup App
var server = app.listen(8081, function () {

    var host = server.address().address;
    var port = server.address().port;

    console.log("Hardware-Pooling service listening at http://%s:%s", host, port);

});