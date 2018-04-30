'use strict';
const busboy = require('connect-busboy');
const express = require('express');
const fs = require('fs');
const request = require('request');
const uuidV4 = require('uuid/v4');
const zerorpc = require('zerorpc');

// const googleMapsClient = require('@google/maps').createClient({
//   key: 'AIzaSyCjNr4oNHdPz49y_I4yVTm18R8NlMVJxOM'
// });

const router = express.Router();

const SIZE_LIMIT = 10 * 1024 * 1024;

router.use(busboy({
    limits: {
        fileSize: SIZE_LIMIT,
        files: 1,
        fields: 10,
    },
}));

const mime = {
    // html: 'text/html',
    // txt: 'text/plain',
    // css: 'text/css',
    // gif: 'image/gif',
    jpg: 'image/jpeg',
    png: 'image/png',
    // svg: 'image/svg+xml',
    // js: 'application/javascript'
};

const py_temp = __dirname + '/../ML/temp/';

const client = new zerorpc.Client();
client.connect('tcp://127.0.0.1:44224');

router.post('/upload', function(req, res) {
    try {
        req.pipe(req.busboy);
        req.busboy.on('file', function (fieldname, file, filename) {
            let uuid = uuidV4();
            let ext = filename.split('.').pop();

            if (ext === 'jpeg') ext = 'jpg';
            if (ext !== 'png' && ext !== 'jpg') {
                return res.status(415).send('Illegal file type!');
            }

            let u_filename = uuid + '.' + ext;

            let fstream = fs.createWriteStream(py_temp + u_filename);
            file.pipe(fstream);

            fstream.on('close', function () {
                if (file.truncated) {
                    console.log('Rejected: ' + filename + ' (too large)');
                    deleteFile(py_temp + u_filename);
                    return res.status(413).send('The file is too large!');
                }

                console.log('Accepted: ' + filename);
                    
                client.invoke("evaluate", u_filename, function(error, nn_res) {                    
                    if (error) {
                        console.log('Neural network error: ' + error);
                        return res.status(500).send('Something went wrong!');
                    }

                    sendImage(py_temp + u_filename, ext, res, true);
                });
            });
        });
    } catch (e) {
        console.log(e);
        console.log(req);
        return res.status(500).send('Something went wrong!');
    }
});

router.post('/upload-url', function(req, res) {
    try {
        let u_filename = uuidV4() + '.jpg';
        let fstream = fs.createWriteStream(py_temp + u_filename);
        console.log(req.body);
        request(dummy_url).pipe(fstream);

        fstream.on('close', function () {
            if (file.truncated) {
                console.log('Rejected: ' + filename + ' (too large)');
                deleteFile(py_temp + u_filename);
                return res.status(413).send('The file is too large!');
            }

            console.log('Accepted: ' + filename);
                
            client.invoke("evaluate", u_filename, function(error, nn_res) {                    
                if (error) {
                    console.log('Neural network error: ' + error);
                    return res.status(500).send('Something went wrong!');
                }

                sendImage(py_temp + u_filename, ext, res, true);
            });
        });
    } catch (e) {
        console.log(e);
        console.log(req);
        return res.status(500).send('Something went wrong!');
    }
});

var dummy_url = 'https://maps.googleapis.com/maps/api/streetview?size=600x300&location=46.414382,10.013988&heading=151.78&pitch=-0.76';


function deleteFile(path) {
    fs.unlink(path, (err) => {
        if (err) return console.log(err);
    });
}

function sendImage(filepath, ext, res, erase) {
    let type = mime[ext] || 'text/plain';
    let s = fs.createReadStream(filepath);
    s.on('open', function () {
        console.log('Sanity check - content type: ' + type);
        res.set('Content-Type', type);
        s.pipe(res);
    });
    if (erase) {
        s.on('end', function() {
            deleteFile(filepath);
        });
    }
}

module.exports = router
