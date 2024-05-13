package rs;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.log4j.PropertyConfigurator;

public class MyServer {

    private static String usr = "jkang-23";
    private static String pwd = "8888";
    private static int ftpPort = 8423;
    private static int socketPort = 9999;

    private static String homeDirPath = "/dev/shm/";

    public static Map<String, Integer> map = new HashMap<>();

    public static void main(String[] args) {
        MyServer myServer = new MyServer();
        Thread serverFtpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                myServer.startFTPServer(ftpPort, usr, pwd, homeDirPath);
            }
        });
        Thread serverSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                myServer.startSocketServer(socketPort);
            }
        });
        // myServer.startFTPServer(ftpPort, usr, pwd, homeDirPath);
        // myServer.startSocketServer(socketPort);
        serverFtpThread.start();
        serverSocketThread.start();
    }

    public void startFTPServer(int port, String username, String password, String homeDirPath) {
        PropertyConfigurator.configure(MyServer.class.getResource("/log4J.properties"));
        FtpServerFactory serverFactory = new FtpServerFactory();

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(port);

        serverFactory.addListener("default", listenerFactory.createListener());

        // Create a UserManager instance
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        File userFile = new File("users.properties");
        if (!userFile.exists()) {
            try {
                if (userFile.createNewFile()) {
                    System.out.println("[FTP] File created: " + userFile.getName());
                } else {
                    System.out.println("[FTP] File already exists.");
                }
            } catch (IOException e) {
                System.out.println("[FTP] An error occurred.");
                e.printStackTrace();
            }
        }
        
        userManagerFactory.setFile(userFile); // Specify the file to store user details
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor()); // Store plain text passwords
        UserManager userManager = userManagerFactory.createUserManager();
        // Create a user
        BaseUser user = new BaseUser();
        user.setName(username);
        user.setPassword(password);
        String homeDirectory = homeDirPath + user.getName();
        File directory = new File(homeDirectory); // Convert the string to a File object
        if (!directory.exists()) { // Check if the directory exists
            if (directory.mkdirs()) {
                System.out.println("[FTP] Directory created: " + directory.getAbsolutePath());
            } else {
                System.out.println("[FTP] Failed to create directory.");
            }
        }
        user.setHomeDirectory(homeDirectory);
        // Set write permissions for the user
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        user.setHomeDirectory(homeDirectory);

        // Add the user to the user manager
        try {
            userManager.save(user);
        } catch (FtpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Set the user manager on the server context
        serverFactory.setUserManager(userManager);

        FtpServer server = serverFactory.createServer();

        // start the server
        try {
            server.start();
            System.out.println("[FTP] FTP Server started on port " + port);
            
        } catch (FtpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void startSocketServer(int port) {
        ServerSocket listener = null;
        String line;
        BufferedReader is;
        BufferedWriter os;
        Socket socketOfServer = null;

        try {
            listener = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
        try {
            System.out.println("[socket] Server is waiting to accept user...");
 
            // Accept client connection request
            // Get new Socket at Server.    
            socketOfServer = listener.accept();
            System.out.println("[socket] Accept a client!");
 
            // Open input and output streams
            is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
 
 
            while (true) {
                // Read data to the server (sent from client).
                line = is.readLine();
                
                // Write to socket of Server
                // (Send to client)
                os.write(">> " + line);
                // End of line
                os.newLine();
                // Flush data.
                os.flush();  
                System.out.println("[socket] Server receive: " + line);

                if (line.equals("MAP")) {
                    os.write(">> MAP OK");
                    os.newLine();
                    os.flush();
                    CallbakMAP();
                }

                if (line.equals("START")) {
                    os.write(">> START OK");
                    os.newLine();
                    os.flush();
                    CallbakSTART();
                }
 
 
                // If users send QUIT (To end conversation).
                if (line.equals("QUIT")) {
                    os.write(">> OK");
                    os.newLine();
                    os.flush();
                    break;
                }
            }
 
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        System.out.println("[socket] Sever stopped!");
    }

    public static void CallbakMAP() {
        System.out.println("[MAP] Callback MAP");
        String fileDir = homeDirPath + usr;
        File directory = new File(fileDir);
        if (!directory.exists()) {
            System.out.println("[MAP] Directory does not exist.");
        } else {
            File[] files = directory.listFiles();
            for (File file : files) {
                String content = "";
                if (file.isFile()) {
                    content += file.getName() + "\n";
                }
                String[] words = content.split(" ");
                for (String word : words) {
                    int hash = word.hashCode();


                }
            }
        }

    }

    public static void CallbakSTART() {
        System.out.println("[START] Callback START");
    }
}