"use strict";

import { createServer } from "http";
import { parse } from "url";
import { readFile, writeFile, existsSync } from "fs";
import { resolve, join } from "path";
import {lookup} from "mime-types";
import { unescape } from "querystring";
import { sep } from 'path';

const port = process.argv[2] || 8000;
console.log("Server is running on port " + port);

// process requests
function webserver( request, response ) {
    const requestURL = parse(request.url, true);
    const baseDirectory = resolve(".");
    try {
        if (requestURL.pathname.startsWith("/Files/")) {
            const requestedFile = join(baseDirectory, requestURL.pathname.substring(6));
            // console.log("Requested file: " + requestedFile);
            if (!requestedFile.startsWith(baseDirectory + sep) || !existsSync(requestedFile)) {
                response.statusCode = 404;
                return response.end("File not found");
            }
    
            readFile(requestedFile, (err, data) => {
                if (err) {
                    response.statusCode = 500;
                    return response.end("Error reading the file");
                }
    
                const mimeType = lookup(requestedFile) || "application/octet-stream";
                response.setHeader("Content-Type", mimeType);
                response.end(data);
            });
        } else if (requestURL.pathname.startsWith("/Items")) {
            if (!existsSync("storage.json")) {
                response.statusCode = 404;
                return response.end("File not found");
            } else {
                readFile("storage.json", (err, data) => {
                    if (err) {
                        response.statusCode = 500;
                        return response.end("Error reading the file");
                    }
                    response.setHeader("Content-Type", "application/json");
                    response.end(data);
                });
            }
        } else if (requestURL.pathname.startsWith("/add")) {
            const title = unescape(requestURL.query.title);
            const value = parseInt(unescape(requestURL.query.value), 10);
            const color = unescape(requestURL.query.color);

            var newdata = {"title": title, "color": color, "value": value};

            if (!existsSync("storage.json")) {
                response.statusCode = 404;
                return response.end("File not found");
            } else {
                readFile("storage.json", 'utf8', (err, data) => {
                    if (err) {
                        response.statusCode = 500;
                        return response.end("Error reading the file");
                    } else {
                        let json = JSON.parse(data);
                        json.push(newdata);
                        writeFile("storage.json", JSON.stringify(json, null, 2), 'utf8', (err) => {
                            if (err) {
                                response.statusCode = 500;
                                return response.end("Error writing to the file");
                            } else {
                                response.statusCode = 200;
                                return response.end("Data added successfully");
                            }
                        });
                    }
                });
            }
        } else if (requestURL.pathname.startsWith("/remove")) {
            const index = parseInt(unescape(requestURL.query.index), 10);

            if (!existsSync("storage.json")) {
                response.statusCode = 404;
                return response.end("File not found");
            } else {
                readFile("storage.json", 'utf8', (err, data) => {
                    if (err) {
                        response.statusCode = 500;
                        return response.end("Error reading the file");
                    } else {
                        let json = JSON.parse(data);
                        if (index >= 0 && index < json.length) {
                            json.splice(index, 1);
                            writeFile("storage.json", JSON.stringify(json, null, 2), 'utf8', (err) => {
                                if (err) {
                                    response.statusCode = 500;
                                    return response.end("Error writing to the file");
                                } else {
                                    response.statusCode = 200;
                                    return response.end("Data removed successfully");
                                }
                            });
                        } else {
                            response.statusCode = 400;
                            return response.end("Invalid index");
                        }
                    }
                });
            }
        } else if (requestURL.pathname.startsWith("/clear")) {
            if (!existsSync("storage.json")) {
                response.statusCode = 404;
                return response.end("File not found");
            } else {
                const newData = [{ "title": "empty", "color": "red", "value": 1 }];
                writeFile("storage.json", JSON.stringify(newData, null, 2), 'utf8', (err) => {
                    if (err) {
                        response.statusCode = 500;
                        return response.end("Error writing to the file");
                    } else {
                        response.statusCode = 200;
                        return response.end("Data cleared successfully");
                    }
                });
            }
        } else if (requestURL.pathname.startsWith("/restore")) {
            if (!existsSync("storage.json")) {
                response.statusCode = 404;
                return response.end("File not found");
            } else {
                const restoreData = [
                    { "title": "slice1", "color": "red", "value": 1 },
                    { "title": "slice2", "color": "green", "value": 2 },
                    { "title": "slice3", "color": "blue", "value": 3 }
                ];
                writeFile("storage.json", JSON.stringify(restoreData, null, 2), 'utf8', (err) => {
                    if (err) {
                        response.statusCode = 500;
                        return response.end("Error writing to the file");
                    } else {
                        response.statusCode = 200;
                        return response.end("Data restored successfully");
                    }
                });
            }
        } else if (requestURL.pathname.startsWith("/PChart")) {
            if (!existsSync("storage.json")) {
                response.statusCode = 404;
                return response.end("File not found");
            } else {
                readFile("storage.json", 'utf8', (err, data) => {
                    if (err) {
                        response.statusCode = 500;
                        return response.end("Error reading the file");
                    } else {
                        let json = JSON.parse(data);
                        let sum = json.reduce((total, item) => total + item.value, 0);
                        let currentAngle = 0;
                        let svgData = json.map((item, i) => {
                            let percentage = (item.value / sum) * 100;
                            let largeArcFlag = percentage > 50 ? 1 : 0;
                            let x = Math.cos(2 * Math.PI * currentAngle / 100);
                            let y = Math.sin(2 * Math.PI * currentAngle / 100);
                            let xEnd = Math.cos(2 * Math.PI * (currentAngle + percentage) / 100);
                            let yEnd = Math.sin(2 * Math.PI * (currentAngle + percentage) / 100);
                            currentAngle += percentage;
                            return `
                                <path fill="${item.color}" d="M ${x} ${y} A 1 1 0 ${largeArcFlag} 1 ${xEnd} ${yEnd} L 0 0 Z">
                                    <text x="${(x + xEnd) / 2}" y="${(y + yEnd) / 2}" fill="#000">${item.title}</text>
                                </path>
                            `;
                        }).join('');
                        
                        let svg = `
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="-1 -1 2 2">
                                ${svgData}
                            </svg>
                        `;
                        
                        response.setHeader("Content-Type", "image/svg+xml");
                        response.end(svg);
                    }
                });
            }
        } else if (requestURL.pathname === "/stop") {
            response.setHeader("Content-Type", "text/html; charset=utf-8");
            response.end("<!doctype html><html><body>The server will stop now.</body></html>");
            process.exit(0);
        } else {
            response.setHeader("Content-Type", "text/html; charset=utf-8");  
            response.end("<!doctype html><html><body>Server is working</body></html>");
        }
    } catch(err) {
        console.error(err);
        if (err.message === '404') {
            response.writeHeader(404, { 'Content-Type': 'text/html' });
            response.write('404 Not Found', 'utf8');
            response.end();
            return;
        }
        else if (err.message == '403') {
            response.writeHeader(403, { 'Content-Type': 'text/html' });
            response.write('Forbidden', 'utf8');
            response.end();
            return;
        }
        else if (err.message === '400') {
            response.writeHeader(400, { 'Content-Type': 'text/html' });
            response.write('Bad Request', 'utf8');
            response.end();
            return;
        }
    }
    
}

// server object creation
const server = createServer(webserver);

// start listening
server.listen(port, (err) => {});
