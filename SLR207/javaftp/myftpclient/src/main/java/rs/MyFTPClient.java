package rs;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MyFTPClient {

    public static void main(String[] args) {
        String [] servers = {"tp-3a107-14", "tp-3a107-17", "tp-3a107-16"};
        int port = 8423;
        String username = "jkang-23";
        String password = "8888";

        for (String server : servers) {
            FTPClient ftpClient = new FTPClient();
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
                    String content = "bonjour" + server;
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
        // String server = "tp-3a107-15";
        // int port = 8423;
        // String username = "jkang-23";
        // String password = "8888";

        // FTPClient ftpClient = new FTPClient();
        // try {
        //     ftpClient.connect(server, port);
        //     ftpClient.login(username, password);
        //     ftpClient.enterLocalPassiveMode();
        //     ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        //     // Code to display files
        //     FTPFile[] files = ftpClient.listFiles();
        //     boolean fileExists = false;
        //     for (FTPFile file : files) {
        //         if (file.getName().equals("bonjour.txt")) {
        //             fileExists = true;
        //             break;
        //         }
        //     }

        //     if (!fileExists) {
        //         String content = "bonjour toto";
        //         ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        //         ftpClient.storeFile("bonjour.txt", inputStream);
        //         int errorCode = ftpClient.getReplyCode();
        //         if (errorCode != 226) {
        //             System.out.println("File upload failed. FTP Error code: " + errorCode);
        //         } else {
        //             System.out.println("File uploaded successfully.");
        //         }
        //     } else {
        //         // Code to retrieve and display file content
            
        //             InputStream inputStream = ftpClient.retrieveFileStream("bonjour.txt");
        //             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        //             String line;
        //             while ((line = reader.readLine()) != null) {
        //                 System.out.println(line);
        //             }
        //             reader.close();
        //             ftpClient.completePendingCommand();
                
        //     }

        //     ftpClient.logout();
        //     ftpClient.disconnect();
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }
}
