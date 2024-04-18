"use strict";
import express from 'express';
import path from 'path';
import fs from 'fs';
import morgan from 'morgan';

const port = process.argv[2] || 8000;

const server = express();
const logger = morgan('combined');

var db = JSON.parse(fs.readFileSync("db.json"));

server.use(logger);
server.use(express.json());

server.get('/', function(req, res) {
    res.send("Hi");
});

server.get('/stop', (req, res) => {
    res.type("text/plain");
    res.send('The Server will stop now.');
    process.exit(0);
});

server.get('/reload', (req, res) => {
    try {
        db = JSON.parse(fs.readFileSync("db.json"));
        res.type("text/plain");
        res.send("db.json reloaded");
    } catch (err) {
        console.error(err);
        res.status(500).send("Error reloading db.json");
    }
});

server.get('/countpapers', (req, res) => {
    const count = db.length;
    res.type("text/plain");
    res.send(`${count}`);
});

server.get('/authoredby/:author', (req, res) => {
    const author = req.params.author.toLowerCase();
    const papers = db.filter(paper => paper.authors.some(a => a.toLowerCase().includes(author)));
    const count = papers.length;
    res.type("text/plain");
    res.send(`${count}`);
});

server.get('/descriptors/:name', (req, res) => {
    const name = req.params.name.toLowerCase();
    const descriptors = db.filter(paper => paper.authors.some(a => a.toLowerCase().includes(name)));
    res.json(descriptors);
});

server.get('/tt/:name', (req, res) => {
    const name = req.params.name.toLowerCase();
    const titles = db.filter(paper => paper.authors.some(a => a.toLowerCase().includes(name)))
                     .map(paper => paper.title);
    res.json(titles);
});

server.get('/reference/:key', (req, res) => {
    const key = req.params.key;
    const descriptor = db.find(paper => paper.key === key);
    if (descriptor) {
        res.json(descriptor);
    } else {
        res.status(404).send('Publication not found');
    }
});

server.delete('/reference/:key', (req, res) => {
    const key = req.params.key;
    const index = db.findIndex(paper => paper.key === key);
    if (index !== -1) {
        db.splice(index, 1);
        res.send(`Publication with key ${key} deleted`);
    } else {
        res.status(404).send('Publication not found');
    }
});

server.post('/reference', (req, res) => {
    const newPublication = req.body;
    newPublication.key = 'imaginary';
    db.push(newPublication);
    res.send('New publication added');
});

server.put('/reference/:key', (req, res) => {
    const key = req.params.key;
    const index = db.findIndex(paper => paper.key === key);
    if (index !== -1) {
        const updatedPublication = { ...db[index], ...req.body };
        db[index] = updatedPublication;
        res.send(`Publication with key ${key} updated`);
    } else {
        res.status(404).send('Publication not found');
    }
});

server.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});
