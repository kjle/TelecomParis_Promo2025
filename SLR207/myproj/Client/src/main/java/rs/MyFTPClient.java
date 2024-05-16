package rs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class MyFTPClient {
    
    private FTPClient ftpClient;
    private String server;
    private int ftpPort;
    private String username;
    private String password;
    private Logger logger = Logger.getLogger(MyFTPClient.class.getName());

    MyFTPClient(FTPClient ftpClient, int ftpPort, String server, String username, String password) {
        this.ftpClient = ftpClient;
        this.ftpPort = ftpPort;
        this.server = server;
        this.username = username;
        this.password = password;
    }

    public void connectFTPClientToServer() {
        try {
            // Connect to the server
            ftpClient.connect(server, ftpPort);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            logger.info("[info][MyFTPClient] Connect Success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadFileToServer(File file) {
        try {
            // check if the connection is established
            if (!ftpClient.isConnected()) {
                logger.warning("[warning][MyFTPClient][uploadFileToServer] Connection closed! Reconnect!");
                connectFTPClientToServer();
            }

            // check if the file exists on the ftp server
            FTPFile[] remoteFiles = ftpClient.listFiles();
            boolean fileExists = false;
            for (FTPFile remoteFile : remoteFiles) {
                if (remoteFile.getName().equals(file.getName())) {
                    fileExists = true;
                    break;
                }
            }
            logger.info("[info][MyFTPClient][uploadFileToServer] " + file.getName() + " exists? " + fileExists);

            // if not exists, upload the file to the ftp server
            // else only read the file content from the ftp server
            if (!fileExists) {
                FileInputStream inputStream = new FileInputStream(file);
                ftpClient.storeFile(file.getName(), inputStream);
                inputStream.close();
                logger.info("[info][MyFTPClient][uploadFileToServer] " + file.getName() + " uploaded successfully.");
                // System.out.println("[info] " + file.getName() + " uploaded successfully.");
            } else {
                InputStream inputStream = ftpClient.retrieveFileStream(file.getName());
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                System.out.println(file.getName());
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                reader.close();
                ftpClient.completePendingCommand();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectFTPClientToServer() {
        try {
            ftpClient.logout();
            ftpClient.disconnect();
            logger.info("[info][MyFTPClient] Disconnect Success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}