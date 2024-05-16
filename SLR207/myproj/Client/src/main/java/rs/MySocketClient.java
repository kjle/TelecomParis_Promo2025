package rs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class MySocketClient {

    private Socket socketClient;
    private String server;
    private int socketPort;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Logger logger = Logger.getLogger(MySocketClient.class.getName());
    
    MySocketClient(Socket socketClient, String server, int socketPort) {
        this.socketClient = socketClient;
        this.server = server;
        this.socketPort = socketPort;
        connectSocketClientToServer();
    }

    public void connectSocketClientToServer() {
        try {
            // Connect to the server
            InetSocketAddress socketAddress = new InetSocketAddress(server, socketPort);
            socketClient.connect(socketAddress, socketPort);
            logger.info("[info][MySocketClient] Connect Success");
            // Create a reader
            reader = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            // Create a writer
            writer = new BufferedWriter(new OutputStreamWriter(socketClient.getOutputStream()));

            // Send a message to the server
            socketClientSend("Connect_Established");
            
            
        } catch (UnknownHostException e) {
            logger.warning("[warning][MySocketClient] " + server + " is not found!");
            e.printStackTrace();
        } catch (IOException e) {
            logger.warning("[warning][MySocketClient] Failed to get IO to " + server + ":" + socketPort);
            e.printStackTrace();
        }
    }

    public void socketClientSend(String message) {
        if (!socketClient.isConnected()) {
            logger.warning("[warning][MySocketClient][socketClientSend] Connection to server is closed!");
            return;
        }

        try {
            // Send a message to the server
            writer.write(message);
            writer.newLine();
            writer.flush();
            logger.info("[info][MySocketClient][socketClientSend] Sent message: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String socketClientReceive () {
        String response = null;
        try {
            response = reader.readLine();
            logger.info("[info][MySocketClient][socketClientReceive] Received message: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public void disconnectSocketClientToServer() {
        try {
            reader.close();
            writer.close();
            socketClient.close();
            logger.info("[info][MySocketClient] Disconnect Success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
