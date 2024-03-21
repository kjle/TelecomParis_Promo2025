const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');
const fs = require('fs');

const app = express();
app.use(bodyParser.json());
app.use(express.static(path.join(__dirname, 'public')));

app.get('/', function(req, res) {
    res.sendFile(path.join(__dirname, 'public', 'ex4.html'));
});

app.listen(8080);
console.log('Server started at http://localhost:8080');
