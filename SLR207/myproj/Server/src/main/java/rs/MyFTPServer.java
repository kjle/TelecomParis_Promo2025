package rs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.log4j.PropertyConfigurator;
import org.apache.ftpserver.listener.ListenerFactory;

public class MyFTPServer {

    private int index;
    private URL resourcePath;
    private String usrPropPath;
    private int ftpPort;
    private String username;
    private String password;
    private String homeDirPath;

    private FtpServerFactory serverFactory = null;
    private ListenerFactory listenerFactory = null;
    private PropertiesUserManagerFactory userManagerFactory = null;
    private File userFile = null;
    private BaseUser user = null;
    private UserManager userManager = null;
    private FtpServer server = null;
    
    private Logger logger = Logger.getLogger(MyFTPServer.class.getName());

    public MyFTPServer(int index, URL resourcePath, String usrPropPath, int ftpPort, String username, String password, String homeDirPath) {
        this.index = index;
        this.resourcePath = resourcePath;
        this.usrPropPath = usrPropPath;
        this.ftpPort = ftpPort;
        this.username = username;
        this.password = password;
        this.homeDirPath = homeDirPath;
        startFTPServer();
        
    }

    public void startFTPServer() {
        PropertyConfigurator.configure(this.resourcePath);
        serverFactory = new FtpServerFactory();
        listenerFactory = new ListenerFactory();
        listenerFactory.setPort(ftpPort);
        serverFactory.addListener("default", listenerFactory.createListener());
        
        // Create a UserManager instance
        userManagerFactory = new PropertiesUserManagerFactory();
        userFile = new File(usrPropPath);

        if (!userFile.exists()) {
            try {
                if (userFile.createNewFile()) {
                    logger.info("[info][MyFTPServer] File created: " + userFile.getName());
                } else {
                    logger.warning("[warning][MyFTPServer] File already exists.");
                }
            } catch (IOException e) {
                logger.warning("[warning][MyFTPServer] An error occurred.");
                e.printStackTrace();
            }
        }

        userManagerFactory.setFile(userFile); // Specify the file to store user details
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor()); // Store plain text passwords
        userManager = userManagerFactory.createUserManager();
        // Create a user
        user = new BaseUser();
        user.setName(username);
        user.setPassword(password);
        String homeDirectory = homeDirPath + user.getName();
        File directory = new File(homeDirectory); // Convert the string to a File object
        if (!directory.exists()) { // Check if the directory exists
            if (directory.mkdirs()) {
                logger.info("[info][MyFTPServer] Directory created: " + directory.getAbsolutePath());
            } else {
                logger.warning("[warning][MyFTPServer] Failed to create directory.");
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
            logger.warning("[warning][MyFTPServer] Failed to save user.");
            e.printStackTrace();
        }

        // Set the user manager on the server context
        serverFactory.setUserManager(userManager);

        server = serverFactory.createServer();
        // start the server
        try {
            server.start();
            logger.info("[info][MyFTPServer] FTP Server started on port " + ftpPort);
            
        } catch (FtpException e) {
            logger.warning("[warning][MyFTPServer] Failed to start FTP server.");
            e.printStackTrace();
        }
    }

}
