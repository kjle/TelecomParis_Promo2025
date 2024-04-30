package rs;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MyFTPClient {

    public static void main(String[] args) {
        String [] servers = {"tp-3a107-14", "tp-3a107-17", "tp-3a107-16"};
        // String [] servers = {"localhost"};
        int port = 8423;
        String username = "jkang-23";
        String password = "8888";
        MyFTPClient myftpClient = new MyFTPClient();
        FTPClient ftpClient = new FTPClient();
        for (String server : servers) {
            myftpClient.startFTPClient(ftpClient, server, port, username, password);
            myftpClient.startSocketClient(server, 9999);
        }
    
    }

    public void startFTPClient(FTPClient ftpClient, String server, int port, String username, String password) {
        // FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Code to display files
            FTPFile[] files = ftpClient.listFiles();
            boolean fileExists = false;
            for (FTPFile file : files) {
                if (file.getName().equals("bonjour.txt")) {
                    fileExists = true;
                    break;
                }
            }

            if (!fileExists) {
                String content = "bonjour " + server;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
                ftpClient.storeFile("bonjour.txt", inputStream);
                int errorCode = ftpClient.getReplyCode();
                if (errorCode != 226) {
                    System.out.println("File upload failed. FTP Error code: " + errorCode);
                } else {
                    System.out.println("File uploaded successfully.");
                }
            } else {
                // Code to retrieve and display file content
                InputStream inputStream = ftpClient.retrieveFileStream("bonjour.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                reader.close();
                ftpClient.completePendingCommand();
            }

            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSocketClient(String serverHost, int port) {
        // Server Host
        // final String serverHost = "tp-1a226-01.enst.fr";
        // final String serverHost = "localhost";

        Socket socketOfClient = null;
        BufferedWriter os = null;
        BufferedReader is = null;

        try {
            // Send a request to connect to the server is listening
            // on machine 'localhost' port 9999.
            socketOfClient = new Socket(serverHost, port);

            // Create output stream at the client (to send data to the server)
            os = new BufferedWriter(new OutputStreamWriter(socketOfClient.getOutputStream()));

            // Input stream at Client (Receive data from the server).
            is = new BufferedReader(new InputStreamReader(socketOfClient.getInputStream()));

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + serverHost);
            return;
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + serverHost);
            return;
        }

        try {
            // Write data to the output stream of the Client Socket.
            os.write("START");
            os.newLine();
            os.flush();  

            // os.write("I am Tom Cat");
            // os.newLine();
            // os.flush();

            // os.write("QUIT");
            // os.newLine();
            // os.flush();

            // Read data sent from the server.
            // By reading the input stream of the Client Socket.
            String responseLine;
            while ((responseLine = is.readLine()) != null) {
                System.out.println("Client receive: " + responseLine);
                if (responseLine.indexOf("OK") != -1) {
                    break;
                }
            }

            os.close();
            is.close();
            socketOfClient.close();
        } catch (UnknownHostException e) {
            System.err.println("Trying to connect to unknown host: " + e);
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }

}
