package rs;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MyClient {

    // define flag
    private static final boolean localFlag = false;

    public static ArrayList<String> servers = new ArrayList<>();
    private static String usr = "jkang-23";
    private static String pwd = "8888";
    private static int ftpPort = 8423;
    private static int socketPort = 9009;
    // local directory path
    private static String localDirPath = "./dataset";

    public static HashMap<Integer, String> serversINFOMap = new HashMap<Integer, String>();

    public enum ResonseTypeFlag {
        SPLIT_END, // all servers have built hashmap of word, ready to send PRESHUFFLE
        PRESHUFFLE_END, // all servers have sent their WORDS to the respective server, ready to send WAITSHUFFLE
        WAITSHUFFLE_END, // all servers have received the WORDS from others, ready to send SHUFFLE
        SHUFFLE_END, // all servers have built the shuffleMap, ready to send CALCULATE
        CALCULATE_END, // all servers have calculated their min and max and send them back, ready to send PRESHUFFLE2
        PRESHUFFLE2_END, // PRESHUFFLE with max and min values, ready to send SHUFFLE2
        SHUFFLE2_END, // build wordsCountMap, ready to send QUIT
        END, // all processes are done, ready to print the result and quit
        DEFAULT
    }

    public static void main(String[] args) {
        MyClient myClient = new MyClient();
        if (localFlag) {
            servers.add("localhost");
        } else {
            servers = readMachine("machines.txt");
        }
////////////////////// DISTRIBUTED DOCUMENTS TO SERVERS //////////////////////
        for (int idx = 0; idx < servers.size(); idx++) {
            FTPThread ftpThread = new FTPThread(new FTPClient(), servers.get(idx), idx, servers.size(),usr, pwd, ftpPort, localDirPath);
            ftpThread.start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

////////////////////// SOCKET CLIENT //////////////////////

        // build the server info map
        for (int i = 0; i < servers.size(); i++) {
            serversINFOMap.put(i, servers.get(i));
        }

        Socket [] socketClients = new Socket[servers.size()];
        BufferedWriter [] oss = new BufferedWriter[servers.size()];
        BufferedReader [] iss = new BufferedReader[servers.size()];

        try {
            for (int i = 0; i < servers.size(); i++) {
                socketClients[i] = new Socket(servers.get(i), socketPort);
                System.out.println("[INFO][MyClient][main] Connected to " + servers.get(i) + " at port " + socketPort + ".");
                oss[i] = new BufferedWriter(new OutputStreamWriter(socketClients[i].getOutputStream()));
                iss[i] = new BufferedReader(new InputStreamReader(socketClients[i].getInputStream()));
            }
        } catch (IOException e) {
            System.err.println("[ERRO][MyClient][main] Couldn't get I/O for the connection to the server.");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            // send serversINFOmsg to all servers
            for (int i = 0; i < servers.size(); i++) {
                String serversINFOmsg = "";
                for (Map.Entry<Integer, String> entry : serversINFOMap.entrySet()) {
                    serversINFOmsg += entry.getKey() + " " + entry.getValue();
                    if (entry.getKey() == i) {
                        serversINFOmsg += " thisserver";
                    } else {
                        serversINFOmsg += " otherserver";
                    }
                    if (entry.getKey() != serversINFOMap.size() - 1) {
                        serversINFOmsg += ";";
                    }
                }
                System.out.println("[INFO][MyClient][main] serversINFOmsg: " + serversINFOmsg);
                oss[i].write(serversINFOmsg);
                oss[i].newLine();
                oss[i].flush();
            }

            // start here: send other msgs to all servers 
            for (int i = 0; i < servers.size(); i++) {
                oss[i].write("SPLIT");
                oss[i].newLine();
                oss[i].flush();
            }

            String [] responses = new String[servers.size()];
            ResonseTypeFlag responseTypeFlag = ResonseTypeFlag.DEFAULT;
            while (true) {
                for (int i = 0 ; i < servers.size(); i ++) {
                    responses[i] = iss[i].readLine();
                }
                responseTypeFlag = checkResponseType(responses);
                if (responseTypeFlag == ResonseTypeFlag.SPLIT_END) {
                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("PRESHUFFLE");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.PRESHUFFLE_END) {
                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("WAITSHUFFLE");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.WAITSHUFFLE_END) {
                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("SHUFFLE");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.SHUFFLE_END) {
                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("CALCULATE");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.CALCULATE_END) {
                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("PRESHUFFLE2");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.PRESHUFFLE2_END) {
                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("SHUFFLE2");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.SHUFFLE2_END) {
                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("QUIT");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.END) {
                    // do something before END, i.e. calculate the tot time
                    break;
                }
            }
            // close all sockets
            for (int i = 0; i < servers.size(); i++) {
                oss[i].close();
                iss[i].close();
                socketClients[i].close();
            }

        } catch (Exception e) {
            System.err.println("[ERRO][MyClient][main] An error occurred while sending msg.");
            e.printStackTrace();
        }
        
    }

    /// @brief: check the response type
    /// @param responses: responses from all servers
    /// @return: response type flag
    public static ResonseTypeFlag checkResponseType(String[] responses) {
        for (int i = 1; i < responses.length; i++) {
            if (!responses[i].equals(responses[i - 1])) {
                return ResonseTypeFlag.DEFAULT;
            }
        }
        if (responses[0].equals("SPLIT_OK"))
            return ResonseTypeFlag.SPLIT_END;
        else if (responses[0].equals("PRESHUFFLE_OK"))
            return ResonseTypeFlag.PRESHUFFLE_END;
        else if (responses[0].equals("WAITSHUFFLE_OK"))
            return ResonseTypeFlag.WAITSHUFFLE_END;
        else if (responses[0].equals("SHUFFLE_OK"))
            return ResonseTypeFlag.SHUFFLE_END;
        else if (responses[0].equals("CALCULATE_OK"))
            return ResonseTypeFlag.CALCULATE_END;
        else if (responses[0].equals("PRESHUFFLE2_OK"))
            return ResonseTypeFlag.PRESHUFFLE2_END;
        else if (responses[0].equals("SHUFFLE2_OK"))
            return ResonseTypeFlag.SHUFFLE2_END;
        else if (responses[0].equals("QUIT_OK"))
            return ResonseTypeFlag.END;
        else   
            return ResonseTypeFlag.DEFAULT;
    }

    /// @brief: read machine list from file
    /// @param filePath: file path of the machine list
    /// @return: machine list
    public static ArrayList<String> readMachine(String filePath) {
        ArrayList<String> machines = new ArrayList<>();
        try {
            File myObj = new File(filePath);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                machines.add(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return machines;
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
            // os.write("START");
            // os.newLine();
            // os.flush();  

            // MAP msg
            os.write("MAP");
            os.newLine();
            os.flush();

            // 

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

class FTPThread extends Thread {
    private FTPClient ftpClient;
    private int idx;
    private String server;
    private int serversNum;
    private String usr;
    private String pwd;
    private int ftpPort;
    private String localDirPath;
    private boolean connected = false;

    public FTPThread(FTPClient ftpClient, String server, int idx, int serversNum, String usr, String pwd, int ftpPort, String localDirPath) {
        this.ftpClient = ftpClient;
        this.server = server;
        this.idx = idx;
        this.serversNum = serversNum;
        this.usr = usr;
        this.pwd = pwd;
        this.ftpPort = ftpPort;
        this.localDirPath = localDirPath;
        // connect to the ftp server
        try {
            // Connect to the server
            ftpClient.connect(server, ftpPort);
            ftpClient.login(usr, pwd);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            System.out.println("[INFO][FTPThread] Connected to " + server + " successfully.");
            this.connected = true;
        } catch (IOException e) {
            System.err.println("[ERRO][FTPThread] Failed to connect to " + server + ".");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // connect to the ftp server
        if(!connected || !ftpClient.isConnected()) {
            try {
                ftpClient.connect(this.server, this.ftpPort);
                ftpClient.login(this.usr, this.pwd);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                System.out.println("[INFO][FTPThread] Connected to " + server + " successfully.");
                this.connected = true;
            } catch (IOException e) {
                System.err.println("[ERRO][FTPThread] Failed to connect to " + server + ".");
                e.printStackTrace();
            }
        }

        File localDir = new File(this.localDirPath);
        File[] files = localDir.listFiles();
        if (files != null) {
            for (int i=0; i<files.length; i++) {
                if (files[i].isFile()) {
                    if (i % serversNum == idx) {
                        // upload the files[i] to the server

                        // check if the file exists on the ftp server
                        boolean fileExists = false;
                        try {
                            FTPFile[] remoteFiles = ftpClient.listFiles();
                            for (FTPFile remoteFile : remoteFiles) {
                                if (remoteFile.getName().equals(files[i].getName())) {
                                    fileExists = true;
                                    break;
                                }
                            }
                            System.out.println("[INFO][FTPThread][run] " + files[i].getName() + " exists on the server: " + fileExists);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (!fileExists) {
                            try {
                                FileInputStream inputStream = new FileInputStream(files[i]);
                                ftpClient.storeFile(files[i].getName(), inputStream);
                                inputStream.close();
                                System.out.println("[INFO][FTPThread][run] " + files[i].getName() + " uploaded successfully.");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // read files[i] from the server
                            try {
                                InputStream inputStream = ftpClient.retrieveFileStream(files[i].getName());
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println(line);
                                }
                                reader.close();
                                ftpClient.completePendingCommand();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        continue;
                    }
               
                }
            }
        }
        try {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}