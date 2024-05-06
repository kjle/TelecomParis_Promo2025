package rs;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MyClient {

    // private static String [] servers = {"tp-3a107-10", "tp-3a107-11", "tp-3a107-12"};
    private static String [] servers = {"localhost"};
    private static String usr = "jkang-23";
    private static String pwd = "8888";
    private static int ftpPort = 8423;
    private static int socketPort = 9999;
    // local directory path
    private static String localDirPath = "./dataset";

    public static void main(String[] args) {
        MyClient myClient = new MyClient();
        FTPClient ftpClient = new FTPClient();

        File localDir = new File(localDirPath);
        File[] files = localDir.listFiles();
        int fileAssign = 0;
        int serverNum = servers.length;

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileAssign = (fileAssign + 1) % serverNum;
                    String server = servers[fileAssign];
                    try {
                        myClient.startFTPClient(ftpClient, server, ftpPort, usr, pwd);

                        // check if the file exists on the ftp server
                        FTPFile[] remoteFiles = ftpClient.listFiles();
                        boolean fileExists = false;
                        for (FTPFile remoteFile : remoteFiles) {
                            if (remoteFile.getName().equals(file.getName())) {
                                fileExists = true;
                                break;
                            }
                        }

                        // if not exists, upload the file to the ftp server
                        // else only read the file content from the ftp server
                        if (!fileExists) {
                            FileInputStream inputStream = new FileInputStream(file);
                            ftpClient.storeFile(file.getName(), inputStream);
                            inputStream.close();
                            System.out.println("[info] " + file.getName() + " uploaded successfully.");
                        } else {
                            InputStream inputStream = ftpClient.retrieveFileStream(file.getName());
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
                    // System.out.println(file.getName());
                }
            }
        }
        // for (String server : servers) {
        //     try {
        //         myClient.startFTPClient(ftpClient, server, ftpPort, usr, pwd);
        //         // send files to the server logic
        //         ftpClient.logout();
        //         ftpClient.disconnect();
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        //     // myClient.startFTPClient(ftpClient, server, ftpPort, usr, pwd);
        //     // myClient.startSocketClient(server, socketPort);
        // }
    
    }

    public void startFTPClient(FTPClient ftpClient, String server, int port, String username, String password) {
        try {
            // Connect to the server
            ftpClient.connect(server, port);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (IOException e) {
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

            os.write("QUIT");
            os.newLine();
            os.flush();

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
