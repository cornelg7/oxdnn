'use strict';
const express = require('express');
const app = express();

app.set('port', process.env.PORT || 80);
app.use(express.static('WEB/HTML'));
app.use(express.static('WEB'));
app.use(require('./API/api'));


const server = app.listen(app.get('port'), function() {
    console.log('Listening on port ' + app.get('port'));
});