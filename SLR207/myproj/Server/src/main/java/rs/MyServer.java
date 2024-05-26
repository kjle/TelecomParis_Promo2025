package rs;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
    private static int socketPort = 9009;

    private static String homeDirPath = "/dev/shm/";

    private static HashMap<Integer, String> serversINFOMap;
    public static int index;
    public static int serversNum;
    private static ThreadListerner[] threadListerners;


    public static void main(String[] args) {

        MyServer myServer = new MyServer();

        // start FTP server to receive files
        myServer.openFTPServer(ftpPort, usr, pwd, homeDirPath);

        // start socket server
        ServerSocket listerner = null;
        String line;
        Socket socketOfServer;
        BufferedReader is;
        BufferedWriter os;
        BufferedWriter[] oss = null;
        serversINFOMap = new HashMap<Integer, String>();

        HashMap<Integer, List<String>> wordsMap = new HashMap<Integer, List<String>>();
        HashMap<String, Integer> wordsPreshuffleMap = new HashMap<String, Integer>();

        HashMap<String, List<Integer>> wordsShuffMap = new HashMap<String, List<Integer>>();
        HashMap<Integer, List<String>> wordsCountMap = new HashMap<Integer, List<String>>();

        try {
            listerner = new ServerSocket(socketPort);
            System.out.println("[INFO][MyServer][main] ServerSocket listerner on port: " + socketPort + " success!");
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][main] An error occurred about ServerSocket listerner.");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            socketOfServer = listerner.accept();
            System.out.println("[INFO][MyServer][main] ServerSocket accept a client!");

            // open I/O streams
            is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));

            boolean msgINFO = false; // flag to receive INFO message

            while (true) {
                if (!msgINFO) {
                    String msgline = is.readLine();
                    System.out.println("[INFO][MyServer][main][INFO msg] Server receive: " + msgline);
                    // from INFO message to get servers indexes and addresses
                    // INFO msg example: 0 tp-3a107-12 thisserver;1 tp-3a107-13;2 tp-3a107-14;
                    // thisserver is the flag of this server
                    String [] tokens = msgline.split(";");
                    for (int i = 0; i < tokens.length; i++) {
                        String[] token = tokens[i].split(" ");
                        serversINFOMap.put(Integer.parseInt(token[0]), token[1]);
                        if (!(token[2].isEmpty()) && token[2].equals("thisserver")) {
                            index = Integer.parseInt(token[0]);
                        }
                    }
                    msgINFO = true;
                    serversNum = serversINFOMap.size();
                    // open listener threads
                    threadListerners = openThreadListerners(serversNum, socketPort, index);
                    // open socket client threads
                    oss = openThreadSockets(serversNum, socketPort, index, serversINFOMap);
                }

                // get msg
                line = is.readLine();

                ////////// receive QUIT msg //////////
                if (line.equals("QUIT")) {
                    // print local map
                    System.out.println("[INFO][MyServer][main][" + index +"] //////////Local map//////////");
                    for (Map.Entry<Integer, List<String> > entry : wordsCountMap.entrySet()) {
                        System.out.println("[INFO][MyServer][main][" + index +"] " + entry.getKey() + " " + entry.getValue());
                    }
                    os.write("QUIT_OK");
                    os.newLine();
                    os.flush();

                    os.close();
                    is.close();
                    socketOfServer.close();
                    for (int i = 0; i < serversNum; i++) {
                        if (i == index) {
                            continue;
                        }
                        oss[i].close();
                        threadListerners[i].closeThread();
                    }
                    break;
                }
                ////////// receive PRESHUFFLE msg //////////
                else if (line.equals("PRESHUFFLE")) {
                    // send SHUFFLE msg to other servers
                    // System.out.println("[INFO][MyServer][main][" + index +"][PRESHUFFLE]");
                    // // print wordsMap
                    // System.out.println("[INFO][MyServer][main][" + index +"] //////////wordsMap//////////");
                    // for (Map.Entry<Integer, List<String> > entry : wordsMap.entrySet()) {
                    //     System.out.println("[INFO][MyServer][main][" + index +"] " + entry.getKey() + " " + entry.getValue());
                    // }
                    for (Map.Entry<Integer, List<String> > entry : wordsMap.entrySet()) {
                        if (serversINFOMap.containsKey(entry.getKey()) && entry.getKey() != index) {
                            String msg = "$PRESHUFFLE_THREAD$;";
                            for (String word : entry.getValue()) {
                                msg += word + ";";
                            }
                            oss[entry.getKey()].write(msg);
                            oss[entry.getKey()].newLine();
                            oss[entry.getKey()].flush();
                        } else {
                            for (int i = 0; i < entry.getValue().size(); i++) {
                                if (wordsPreshuffleMap.containsKey(entry.getValue().get(i))) {
                                    int count = wordsPreshuffleMap.get(entry.getValue().get(i));
                                    wordsPreshuffleMap.put(entry.getValue().get(i), count + 1);
                                } else {
                                    wordsPreshuffleMap.put(entry.getValue().get(i), 1);
                                }
                            }
                        }
                    }
                    os.write("PRESHUFFLE_OK");
                    os.newLine();
                    os.flush();
                }
                ////////// build hashmap of words i.e. wordsMap//////////
                else {
                    File localDir = new File(homeDirPath + usr + "/");
                    File[] files = localDir.listFiles();
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".txt")) {
                            // read file
                            try {
                                Scanner scanner = new Scanner(file);
                                while (scanner.hasNextLine()) {
                                    String txtline = scanner.nextLine();
                                    String[] txtTokens = txtline.split("\\s+");
                                    for (String txtToken : txtTokens) {
                                        int toWhichServer = Math.abs(txtToken.hashCode()) % serversNum;
                                        if (wordsMap.containsKey(toWhichServer)) {
                                            wordsMap.get(toWhichServer).add(txtToken);
                                        } else {
                                            List<String> wordsList = new ArrayList<String>();
                                            wordsList.add(txtToken);
                                            wordsMap.put(toWhichServer, wordsList);
                                        }
                                    }
                                }
                                scanner.close();
                            } catch (FileNotFoundException e) {
                                System.out.println("[ERROR][MyServer][main] An error occurred about build wordsMap.");
                                e.printStackTrace();
                            }
                        }
                    }
                    // send back SPLIT_OK msg
                    os.write("SPLIT_OK");
                    os.newLine();
                    os.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][main] An error occurred about socket msg receive.");
            e.printStackTrace();
        }

    }

    /// @brief open FTP server to receive files
    /// @param port port number
    /// @param username username
    /// @param password password
    /// @param homeDirPath home directory path
    private void openFTPServer (int port, String username, String password, String homeDirPath) {
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
                    System.out.println("[INFO][MyServer][openFTPServer] File created: " + userFile.getName());
                } else {
                    System.out.println("[INFO][MyServer][openFTPServer] " + userFile.getName() + " already exists.");
                }
            } catch (IOException e) {
                System.err.println("[ERROR][MyServer][openFTPServer] An error occurred.");
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
                System.out.println("[INFO][MyServer][openFTPServer] Directory created: " + directory.getAbsolutePath());
            } else {
                System.err.println("[ERROR][MyServer][openFTPServer] Failed to create directory.");
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
            System.err.println("[ERROR][MyServer][openFTPServer] An error occurred about save user.");
            e.printStackTrace();
        }
        // Set the user manager on the server context
        serverFactory.setUserManager(userManager);

        FtpServer server = serverFactory.createServer();

        // start the server
        try {
            server.start();
            System.out.println("[INFO][MyServer][openFTPServer] FTP Server started on port " + port);
            
        } catch (FtpException e) {
            System.err.println("[ERROR][MyServer][openFTPServer] An error occurred about start server.");
            e.printStackTrace();
        }
    }

    /// @brief open socket client threads
    /// @param serversNum number of servers
    /// @param socketport base port number
    /// @param index index of this server
    /// @param serversINFOMap servers information map
    /// @return oss BufferedWriter array
    public static BufferedWriter[] openThreadSockets(int serversNum, int socketport, int index, HashMap<Integer, String> serversINFOMap) {
        BufferedWriter[] oss = new BufferedWriter[serversNum];
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.err.println("[ERROR][MyServer][main] An error occurred about sleep.");
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < serversNum; i++) {
                if (i == index) {
                    continue;
                }
                Socket socket = new Socket(serversINFOMap.get(i), socketPort + index + 1);
                System.out.println("[INFO][MyServer][openThreadSocket][" + index + "] connect to server: " + serversINFOMap.get(i) + " success!");
                oss[i] = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            }
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][main] An error occurred about open socket threads.");
            e.printStackTrace();
        }
        return oss;
    }

    /// @brief each server thread will listen to the other servers
    /// @param serversNum number of servers
    /// @param socketport base port number
    /// @param index index of this server
    /// @return threadListerners
    public static ThreadListerner[] openThreadListerners(int serversNum, int socketport, int index) {
        ThreadListerner[] threadListerners = new ThreadListerner[serversNum];
        try {
            for (int i = 0; i < serversNum; i++) {
                if (i == index) {
                    continue;
                }
                ServerSocket listener = new ServerSocket(socketport + i + 1);
                System.out.println("[INFO][MyServer][openThreadListerners][" + index + "] listers on port: " + (socketport + i + 1) + " success!");
                threadListerners[i] = new ThreadListerner(listener);
                threadListerners[i].start();
            }
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][openThreadListerners][" + index + "]");
            e.printStackTrace();
        }
        return threadListerners;
    }


}

class ThreadListerner extends Thread {
    private ServerSocket listerner;
    private HashMap<String, Integer> mapThread = new HashMap<String, Integer>();
    private HashMap<Integer, List<String>> mapThread2 = new HashMap<Integer, List<String>>();

    public boolean PRESHUFFLE_THREAD_READY = false;

    public ThreadListerner(ServerSocket listerner) {
        this.listerner = listerner;
    }

    public void closeThread() {
        try {
            listerner.close();
            interrupt();
        } catch (IOException e) {
            System.err.println("[ERROR][ThreadListerner][closeThread]");
            e.printStackTrace();
        }
    }

    @Override
    public void run () {
        try {
            Socket socketOfServer = listerner.accept();
            BufferedReader is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));

            while (true) {
                String line = is.readLine();
                String [] tokens = line.split(";");
                if(tokens[0].contains("$PRESHUFFLE_THREAD$")) {
                    PRESHUFFLE_THREAD_READY = false;
                    for (int i = 1; i < tokens.length; i++) {
                        if (mapThread.containsKey(tokens[i])) {
                            int count = mapThread.get(tokens[i]);
                            mapThread.put(tokens[i], count + 1);
                        } else {
                            mapThread.put(tokens[i], 1);
                        }
                    }
                    PRESHUFFLE_THREAD_READY = true;
                }
                // check commands
            }
        } catch (IOException e) {
            System.err.println("[ERROR][ThreadListerner][run]");
            e.printStackTrace();
        }
    }
}