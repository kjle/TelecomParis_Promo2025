"use strict";

import { createServer } from "http";
import { parse } from "url";
import { readFile, existsSync } from "fs";
import { resolve, join } from "path";
import {lookup} from "mime-types";
import { unescape } from "querystring";

const port = process.argv[2] || 8000;
console.log("Server is running on port " + port);

let noms = [];

// process requests
function webserver( request, response ) {
    const requestURL = parse(request.url, true);
    const baseDirectory = resolve(".");
    if (requestURL.pathname.startsWith("/Files/")) {
        const requestedFile = join(baseDirectory, requestURL.pathname.substring(6));
        // console.log("Requested file: " + requestedFile);
        if (!requestedFile.startsWith(baseDirectory + "/") || !existsSync(requestedFile)) {
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
    } else if (requestURL.pathname.startsWith("/clear")) {
        noms = []; // Clear the noms array
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        response.end("<!doctype html><html><body>Memory cleared.</body></html>");
    }else if (requestURL.pathname.startsWith("/coucou")) {
        let nom = unescape(requestURL.query.nom || "");
        nom = nom.replace( /(<([^>]+)>)/ig, '');
        nom = nom.replace(/<script>(.*?)<\/script>/g, '$1');
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        response.writeHead(200);
        response.end(`<!doctype html><html><body>coucou ${nom}, the following users have already visited this page: ${noms.join(", ")}</body></html>`);
        noms.push(nom);
    }else if (requestURL.pathname.startsWith("/bonjour")) {
        const user = unescape(requestURL.query.user || "");
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        response.end(`<!doctype html><html><body>bonjour ${user}</body></html>`);
    } else if (requestURL.pathname === "/stop") {
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        response.end("<!doctype html><html><body>The server will stop now.</body></html>");
        process.exit(0);
    } else {
        response.setHeader("Content-Type", "text/html; charset=utf-8");  
        response.end("<!doctype html><html><body>Server is working</body></html>");
    }
}

// server object creation
const server = createServer(webserver);

// start listening
server.listen(port, (err) => {});
