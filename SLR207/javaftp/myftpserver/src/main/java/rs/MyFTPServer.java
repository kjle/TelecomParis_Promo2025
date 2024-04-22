package rs;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class MyFTPServer {

    public static void main(String[] args) {
    PropertyConfigurator.configure(MyFTPServer.class.getResource("/log4J.properties"));
    FtpServerFactory serverFactory = new FtpServerFactory();
    int port = 8423; // Replace 3456 with the desired port number

    ListenerFactory listenerFactory = new ListenerFactory();
    listenerFactory.setPort(port);

    serverFactory.addListener("default", listenerFactory.createListener());

    // Create a UserManager instance
    PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
    File userFile = new File("users.properties");
    if (!userFile.exists()) {
        try {
            if (userFile.createNewFile()) {
                System.out.println("File created: " + userFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    
    userManagerFactory.setFile(userFile); // Specify the file to store user details
    userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor()); // Store plain text passwords
    UserManager userManager = userManagerFactory.createUserManager();
    // Create a user
    BaseUser user = new BaseUser();
    user.setName("jkang-23"); // Replace "username" with the desired username
    user.setPassword("8888"); // Replace "password" with the desired password
    String username = user.getName();
    String homeDirectory = System.getProperty("java.io.tmpdir")  + "/" + username; 
    File directory = new File(homeDirectory); // Convert the string to a File object
    if (!directory.exists()) { // Check if the directory exists
        if (directory.mkdirs()) {
            System.out.println("Directory created: " + directory.getAbsolutePath());
        } else {
            System.out.println("Failed to create directory.");
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
        System.out.println("FTP Server started on port " + port);
        
    } catch (FtpException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    }
}