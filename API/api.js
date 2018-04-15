'use strict';
const express = require('express');
const fs = require('fs');
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

function generateNNResponse(picture) {
    return "Possibly accessible";
}

router.post('/upload', function(req, res) {
    req.pipe(req.busboy);
    req.busboy.on('file', function (fieldname, file, filename) {
        let fstream = fs.createWriteStream(__dirname + '/temp/' + filename);
        file.pipe(fstream);
        fstream.on('close', function () {
            fs.unlink(fstream.path);    // just a test
            if (file.truncated) {
                console.log('Rejected: ' + filename + ' (too large)');
                return res.status(413).send('The file is too large!');
            }
            console.log('Accepted: ' + filename);
            let response = generateNNResponse("File data");
            res.send(response);
        });
    });
});

module.exports = router
