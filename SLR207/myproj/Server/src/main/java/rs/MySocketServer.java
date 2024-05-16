package rs;

import java.net.ServerSocket;

import java.net.Socket;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MySocketServer {
    private Socket socketServer = null;
    private ServerSocket listener = null;
    private int socketPort;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;
    private Logger logger = Logger.getLogger(MySocketServer.class.getName());
    
    MySocketServer (int socketPort) {
        this.socketPort = socketPort;
        startSocketServer();
    }

    public void startSocketServer() {
        try {
            listener = new ServerSocket(socketPort);
            logger.info("[info][MySocketServer] Server is running on port " + socketPort);  
        } catch (IOException e) {
            logger.warning("[warning][MySocketServer] Failed to start server on port " + socketPort);
            System.exit(1);
        }

        try {
            socketServer = listener.accept();
            logger.info("[info][MySocketServer] Connection accepted from " + socketServer.getInetAddress().getHostName());
            // Open input and output streams
            reader = new BufferedReader(new InputStreamReader(socketServer.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socketServer.getOutputStream()));
        } catch (IOException e) {
            logger.warning("[warning][MySocketServer] Failed to accept connection from client");
            System.exit(1);
        }
    }

    public void socketServerSend(String message) {
        try {
            // Send a message to the client
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String socketServerReceive () {
        String response = null;
        try {
            response = reader.readLine();
        } catch (IOException e) {
            logger.warning("[warning][MySocketServer] Connection to client is closed!");
            e.printStackTrace();
            // closeSocketServer();
        }
        return response;
    }

    public void closeSocketServer() {
        try {
            reader.close();
            writer.close();
            socketServer.close();
            listener.close();
            logger.info("[info][MySocketServer] Connection closed!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
