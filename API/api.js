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

const temp_dir = __dirname + '/../temp/';

const client = new zerorpc.Client();
client.connect('tcp://127.0.0.1:44224');

router.post('/upload-:inf-:outf', function(req, res) {
    if ((req.params.inf !== 'pic' && req.params.inf !== 'url') ||
        (req.params.outf !== 'pic' && req.params.outf !== 'list')) {
        return res.status(400).end();
    }
    try {
        let resFun;
        let uuid = uuidV4();
        let ext;
        let u_filename = function() {
            return uuid + '.' + ext;
        }
        if (req.params.outf === 'pic') {
            resFun = function(error, nn_res) {                    
                if (error) {
                    console.log('Neural network error: ' + error);
                    res.status(500).send('Something went wrong!');
                    deleteFile(temp_dir + u_filename());
                    return;
                }

                sendImage(temp_dir + u_filename(), ext, res, true);
            };
        }
        else {
            resFun = function(error, nn_res) {                    
                deleteFile(temp_dir + u_filename());

                if (error) {
                    console.log('Neural network error: ' + error);
                    res.status(500).send('Something went wrong!');
                    return;
                }

                res.send(nn_res);
            };
        }
        if (req.params.inf === 'pic') {
            req.pipe(req.busboy);
            req.busboy.on('file', function (fieldname, file, filename) {
                ext = filename.split('.').pop();
                if (ext === 'jpeg') ext = 'jpg';
                if (ext !== 'png' && ext !== 'jpg') {
                    res.status(415).send('Illegal file type!');
                    return;
                }

                let fstream = fs.createWriteStream(temp_dir + u_filename());

                file.pipe(fstream);
                fstream.on('close', function () {
                    if (file.truncated) {
                        console.log('Rejected: ' + filename + ' (too large)');
                        deleteFile(temp_dir + u_filename());
                        res.status(413).send('The file is too large!');
                        return;
                    }

                    console.log('Accepted: ' + filename);
                        
                    client.invoke('evaluate', u_filename(), resFun);
                });
            });
        }
        else {
            request('https://lh3.googleusercontent.com/p/' + req.body)
            .on('response', function(response) {
                if(response.statusCode != 200) {
                    res.status(400).send('Malformed url suffix!');
                    return;
                }
                else {
                    ext = 'jpg';
                    let fstream = fs.createWriteStream(temp_dir + u_filename());
                    response.pipe(fstream);
                    fstream.on('close', function () {
                        console.log('Accepted: ' + req.body);

                        client.invoke('evaluate', u_filename(), resFun);
                    });
                }
            });
        }
    } catch (e) {
        console.log(e);
    }
});

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
