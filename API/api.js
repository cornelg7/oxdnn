'use strict';
const express = require('express');
const fs = require('fs');
const zerorpc = require("zerorpc");
const busboy = require('connect-busboy');
const router = express.Router();

const SIZE_LIMIT = 10 * 1024 * 1024;

router.use(busboy({
    limits: {
        fileSize: SIZE_LIMIT,
        files: 1,
        fields: 10,
    },
}));

const client = new zerorpc.Client();
client.connect("tcp://127.0.0.1:44224");

router.post('/upload', function(req, res) {
    req.pipe(req.busboy);
    req.busboy.on('file', function (fieldname, file, filename) {
        let fstream = fs.createWriteStream(__dirname + '/temp/' + filename);
        file.pipe(fstream);
        fstream.on('close', function () {
            fs.unlink(fstream.path, (err) => {
                if (err) return console.log(err);
            });
            if (file.truncated) {
                console.log('Rejected: ' + filename + ' (too large)');
                return res.status(413).send('The file is too large!');
            }
            console.log('Accepted: ' + filename);
            client.invoke("evaluate", file, function(error, response) {
                res.send(response);
            });
        });
    });
});

module.exports = router
