var express = require('express');
var router = express.Router();

router.post('/upload', function(req, res) {    
    res.send("Success - file uploaded");
});

module.exports = router