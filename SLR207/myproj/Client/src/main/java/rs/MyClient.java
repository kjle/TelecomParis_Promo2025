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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MyClient {

    // define flag
    private static final boolean localFlag = false;
    public static final boolean debugFlag = false;

    public static ArrayList<String> servers = new ArrayList<>();
    // parameters for ftp and socket
    private static String usr = "jkang-23";
    private static String pwd = "8888";
    private static int ftpPort = 8423;
    private static int socketPort = 9009;
    // total number of files to be distributed
    private static int fileNum = 0; // 0 - all files; x > 0 - only x files
    // local directory path
    private static String localDirPath = "./dataset";
    // private static String localDirPath = "/cal/commoncrawl";

    public static HashMap<Integer, String> svrIdx_svrAddr_map = new HashMap<Integer, String>();

    public enum ResonseTypeFlag {
        SPLIT_END, // all servers have built hashmap of word, ready to send PRESHUFFLE
        PRESHUFFLE_END, // all servers have sent their WORDS to the respective server, ready to send WAITSHUFFLE
        WAITREADY_END, // all servers have received the PRESHUFFLE, ready to send SHUFFLE
        SHUFFLE_END, // all servers have built the shuffleMap, ready to send CALCULATE
        CALCULATE_END, // all servers have calculated their min and max and send them back
        RANGE_END, // all servers have built their rangeMap, contains <key, val>=<cntNum, servIdx>, ready to send PRESHUFFLE2
        PRESHUFFLE2_END, // PRESHUFFLE with max and min values, ready to send SHUFFLE2
        WAITREADY2_END, // all servers have received the PRESHUFFLE2, ready to send SHUFFLE2
        SHUFFLE2_END, // build wordsCountMap, ready to send QUIT
        END, // all processes are done, ready to print the result and quit
        DEFAULT
    }

    public static void main(String[] args) {
        if (localFlag) {
            servers.add("localhost");
        } else {
            servers = readMachine("machines.txt");
        }
        // // print machines
        // for (String server : servers) {
        //     System.out.println("[INFO][MyClient][main] server: " + server);
        // }
        System.out.println("[INFO][MyClient][main] servers number: " + servers.size());
////////////////////// SET TIME //////////////////////
        long timeCommunication = 0;
        long timeComputation = 0;
        long timeSynchronization = 0;

        // local time to measure the time of each step
        long lstartTime = 0;
        long lendTime = 0;

////////////////////// DISTRIBUTED DOCUMENTS TO SERVERS //////////////////////
        // time start [sending splits]
        long startTime = System.currentTimeMillis();
        lstartTime = System.currentTimeMillis();

        File localDir = new File(localDirPath);
        File[] files = localDir.listFiles();
        File[] fileList = null;
        if (files != null) {
            if (fileNum == 0) {
                fileList = files;
            } else {
                fileList = new File[fileNum];
                for (int i = 0; i < fileNum; i++) {
                    fileList[i] = files[i];
                }
            }
            
        }

        FTPThread [] ftpThreads = new FTPThread[servers.size()];

        for (int idx = 0; idx < servers.size(); idx++) {
            ftpThreads[idx] = new FTPThread(new FTPClient(), servers.get(idx), idx, servers.size(),usr, pwd, ftpPort, fileList);
            ftpThreads[idx].start();
        }

        // wait fot ftp close
        while(true) {
            boolean allClosed = true;
            for (int i = 0; i < servers.size(); i++) {
                if (ftpThreads[i].isAlive()) {
                    allClosed = false;
                    break;
                }
            }
            if (allClosed) {
                break;
            }
        }

        // time end [send splits]
        long endTime = System.currentTimeMillis();
        timeCommunication += endTime - startTime;

        lendTime = System.currentTimeMillis();
        System.out.println("[INFO][MyClient][main] Time for ftp sending splits: " + (lendTime - lstartTime) + " ms.");

////////////////////// SOCKET CLIENT //////////////////////
        // time start [send node info]
        startTime = System.currentTimeMillis();
        lstartTime = System.currentTimeMillis();

        // build the server info map
        for (int i = 0; i < servers.size(); i++) {
            svrIdx_svrAddr_map.put(i, servers.get(i));
        }

        Socket [] socketClients = new Socket[servers.size()];
        BufferedWriter [] oss = new BufferedWriter[servers.size()];
        BufferedReader [] iss = new BufferedReader[servers.size()];

        try {
            for (int i = 0; i < servers.size(); i++) {
                socketClients[i] = new Socket(servers.get(i), socketPort);
                if (debugFlag)  System.out.println("[DEBUG][MyClient][main] Connected to " + servers.get(i) + " at port " + socketPort + ".");
                oss[i] = new BufferedWriter(new OutputStreamWriter(socketClients[i].getOutputStream()));
                iss[i] = new BufferedReader(new InputStreamReader(socketClients[i].getInputStream()));
            }
        } catch (IOException e) {
            System.err.println("[ERRO][MyClient][main] Couldn't get I/O for the connection to the server.");
            e.printStackTrace();
            System.exit(1);
        }

        
        try {

            // send svrIdx and svrAddr to all servers
            for (int i = 0; i < servers.size(); i++) {
                String svrmsg = "";
                for (Map.Entry<Integer, String> entry : svrIdx_svrAddr_map.entrySet()) {
                    svrmsg += entry.getKey() + " " + entry.getValue();
                    if (entry.getKey() == i) {
                        svrmsg += " thisserver";
                    } else {
                        svrmsg += " otherserver";
                    }
                    if (entry.getKey() != svrIdx_svrAddr_map.size() - 1) {
                        svrmsg += ";";
                    }
                }
                if (debugFlag)  System.out.println("[DEBUG][MyClient][main] svrmsg: " + svrmsg);
                oss[i].write(svrmsg);
                oss[i].newLine();
                oss[i].flush();
            }
            // time end [send node info]
            endTime = System.currentTimeMillis();
            timeSynchronization += endTime - startTime;
            lendTime = System.currentTimeMillis();
            System.out.println("[INFO][MyClient][main] Time for sending node info: " + (lendTime - lstartTime) + " ms.");

            // time start [map1 computation]
            startTime = System.currentTimeMillis();
            lstartTime = System.currentTimeMillis();

            // SPLIT: servers build hashmap of word
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
                    if (debugFlag)  System.out.println("[DEBUG][MyClient][main] response[" + i + "]: " + responses[i]);
                }
                responseTypeFlag = checkResponseType(responses);
                if (responseTypeFlag == ResonseTypeFlag.SPLIT_END) {
                    // time end [map1 computation]
                    endTime = System.currentTimeMillis();
                    timeComputation += endTime - startTime;
                    lendTime = System.currentTimeMillis();
                    System.out.println("[INFO][MyClient][main] Time for SPLIT: " + (lendTime - lstartTime) + " ms.");

                    // time start [shuffle1 communincation]
                    startTime = System.currentTimeMillis();
                    lstartTime = System.currentTimeMillis();

                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("PRESHUFFLE");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.PRESHUFFLE_END) {
                    lendTime = System.currentTimeMillis();
                    System.out.println("[INFO][MyClient][main] Time for PRESHUFFLE: " + (lendTime - lstartTime) + " ms.");

                    lstartTime = System.currentTimeMillis();
                    
                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("WAITREADY");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.WAITREADY_END) {
                    // time end [shuffle1 communincation]
                    endTime = System.currentTimeMillis();
                    timeCommunication += endTime - startTime;
                    lendTime = System.currentTimeMillis();
                    System.out.println("[INFO][MyClient][main] Time for WAITREADY: " + (lendTime - lstartTime) + " ms.");

                    // time start [reduce1 computation]
                    startTime = System.currentTimeMillis();
                    lstartTime = System.currentTimeMillis();
                   
                    for (int i = 0 ; i < servers.size(); i ++) {
                        oss[i].write("SHUFFLE");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.SHUFFLE_END) {
                    lendTime = System.currentTimeMillis();
                    System.out.println("[INFO][MyClient][main] Time for SHUFFLE: " + (lendTime - lstartTime) + " ms.");

                    lstartTime = System.currentTimeMillis();

                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("CALCULATE");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                    // time end [reduce1 computation]
                    endTime = System.currentTimeMillis();
                    timeComputation += endTime - startTime;

                    // time start [send group synchronization]
                    startTime = System.currentTimeMillis();

                } else if (responseTypeFlag == ResonseTypeFlag.CALCULATE_END) {
                    lendTime = System.currentTimeMillis();
                    System.out.println("[INFO][MyClient][main] Time for CALCULATE: " + (lendTime - lstartTime) + " ms.");

                    lstartTime = System.currentTimeMillis();

                    for (int i = 0 ; i < servers.size(); i ++) {
                        responses[i] = iss[i].readLine();
                    }
                    int fmax = Integer.MIN_VALUE;
                    int fmin = Integer.MAX_VALUE;
                    for (int i = 0 ; i < servers.size(); i ++) {
                        // System.out.println("[INFO][MyClient][main] responses[" + i + "]: " + responses[i]);
                        String [] res = responses[i].split(";");
                        if (res[0].equals("0")) {
                            continue;
                        }
                        fmax = Math.max(fmax, Integer.parseInt(res[0]));
                        fmin = Math.min(fmin, Integer.parseInt(res[1]));
                    }
                    // calculate the range
                    double expval = (double)(fmax - fmin + 1) / (double)servers.size();
                    double floorval = Math.floor(expval);
                    double diff = expval - floorval;
                    // System.out.println("[INFO][MyClient][main] expval: " + expval + ", floorval: " + floorval);
                    int range = (int)((diff >= 0.5) ? (Math.ceil(expval)) : (floorval));
                    if (debugFlag)  System.out.println("[DEBUG][MyClient][main] fmax: " + fmax + ", fmin: " + fmin + ", range: " + range);
                   
                    // send the range respectively to all servers
                    int rmin = fmin;
                    String msg = ""; // range for each server, i.e. serverIdx,fmax,fmin;
                    for (int i = 0; i < servers.size() - 1; i++) {
                        fmin = fmax - range + 1;
                        if (fmin <= rmin || fmax <= rmin) {
                            msg += i + ",0,0;";
                            // System.out.println("[INFO][MyClient][main] send fmax: 0, fmin: 0 to server " + i);
                            continue;
                        }
                        msg += i + "," + fmax + "," + fmin + ";";
                        // System.out.println("[INFO][MyClient][main] send fmax: " + fmax + ", fmin: " + fmin + " to server " + i);
                        fmax = fmin - 1;
                    }
                    msg += servers.size() - 1 + "," + fmax + "," + rmin + ";";
                    // System.out.println("[INFO][MyClient][main] send fmax: " + fmax + ", fmin: " + rmin + " to server " + (servers.size() - 1));
                    if (debugFlag)  System.out.println("[DEBUG][MyClient][main] send range msg: " + msg);
                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write(msg);
                        oss[i].newLine();
                        oss[i].flush();
                    }
                    // time end [send group synchronization]
                    endTime = System.currentTimeMillis();
                    timeSynchronization += endTime - startTime;

                    // time start for [map2 computation]
                    startTime = System.currentTimeMillis();

                } else if (responseTypeFlag == ResonseTypeFlag.RANGE_END) {
                    // time end [map2 computation]
                    endTime = System.currentTimeMillis();
                    timeComputation += endTime - startTime;
                    lendTime = System.currentTimeMillis();
                    System.out.println("[INFO][MyClient][main] Time for RANGE: " + (lendTime - lstartTime) + " ms.");
                    
                    //time start [shuffle2 communication]
                    startTime = System.currentTimeMillis();
                    lstartTime = System.currentTimeMillis();

                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("PRESHUFFLE2");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.PRESHUFFLE2_END) {
                    lendTime = System.currentTimeMillis();
                    System.out.println("[INFO][MyClient][main] Time for PRESHUFFLE2: " + (lendTime - lstartTime) + " ms.");

                    lstartTime = System.currentTimeMillis();

                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("WAITREADY2");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.WAITREADY2_END) {
                    // time end [shuffle2 communication]
                    endTime = System.currentTimeMillis();
                    timeCommunication += endTime - startTime;
                    lendTime = System.currentTimeMillis();
                    System.out.println("[INFO][MyClient][main] Time for WAITREADY2: " + (lendTime - lstartTime) + " ms.");

                    // time start [reduce2 computation]
                    startTime = System.currentTimeMillis();
                    lstartTime = System.currentTimeMillis();

                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("SHUFFLE2");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.SHUFFLE2_END) {
                    // time end [reduce2 computation]
                    endTime = System.currentTimeMillis();
                    timeComputation += endTime - startTime;
                    lendTime = System.currentTimeMillis();
                    System.out.println("[INFO][MyClient][main] Time for SHUFFLE2: " + (lendTime - lstartTime) + " ms.");

                    for (int i = 0; i < servers.size(); i++) {
                        oss[i].write("QUIT");
                        oss[i].newLine();
                        oss[i].flush();
                    }
                } else if (responseTypeFlag == ResonseTypeFlag.END) {

                    System.out.println("[INFO][MyClient][main] All processes are done.");
                    System.out.println("[INFO][MyClient][main] Time for communication: " + timeCommunication + " ms.");
                    System.out.println("[INFO][MyClient][main] Time for computation: " + timeComputation + " ms.");
                    System.out.println("[INFO][MyClient][main] Time for synchronization: " + timeSynchronization + " ms.");
                    double ratio = (double)(timeCommunication + timeSynchronization) / timeComputation; 
                    System.out.println("[INFO][MyClient][main] Time ratio: " + ratio);
                    System.out.println("[INFO][MyClient][main] Time total: " + (timeCommunication + timeComputation + timeSynchronization) + " ms.");
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
            System.err.println("[ERROR][MyClient][main] An error occurred while sending msg.");
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
        else if (responses[0].equals("WAITREADY_OK"))
            return ResonseTypeFlag.WAITREADY_END;
        else if (responses[0].equals("SHUFFLE_OK"))
            return ResonseTypeFlag.SHUFFLE_END;
        else if (responses[0].equals("CALCULATE_OK"))
            return ResonseTypeFlag.CALCULATE_END;
        else if (responses[0].equals("RANGE_OK"))
            return ResonseTypeFlag.RANGE_END;
        else if (responses[0].equals("PRESHUFFLE2_OK"))
            return ResonseTypeFlag.PRESHUFFLE2_END;
        else if (responses[0].equals("WAITREADY2_OK"))
            return ResonseTypeFlag.WAITREADY2_END;
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
}

class FTPThread extends Thread {
    private FTPClient ftpClient;
    private int idx;
    private String server;
    private int serversNum;
    private String usr;
    private String pwd;
    private int ftpPort;
    private File[] files;
    private boolean connected = false;

    public FTPThread(FTPClient ftpClient, String server, int idx, int serversNum, String usr, String pwd, int ftpPort, File[] files) {
        this.ftpClient = ftpClient;
        this.server = server;
        this.idx = idx;
        this.serversNum = serversNum;
        this.usr = usr;
        this.pwd = pwd;
        this.ftpPort = ftpPort;
        this.files = files;
        // connect to the ftp server
        try {
            // Connect to the server
            ftpClient.connect(server, ftpPort);
            ftpClient.login(usr, pwd);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            if (MyClient.debugFlag)  System.out.println("[DEBUG][FTPThread] Connected to " + server + " successfully.");
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
                if (MyClient.debugFlag)   System.out.println("[DEBUG][FTPThread] Connected to " + server + " successfully.");
                this.connected = true;
            } catch (IOException e) {
                System.err.println("[ERRO][FTPThread] Failed to connect to " + server + ".");
                e.printStackTrace();
            }
        }

        // File localDir = new File(this.localDirPath);
        // File[] files = localDir.listFiles();
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
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (!fileExists) {
                            try {
                                FileInputStream inputStream = new FileInputStream(files[i]);
                                ftpClient.storeFile(files[i].getName(), inputStream);
                                inputStream.close();
                                if (MyClient.debugFlag)   System.out.println("[DEBUG][FTPThread][run] " + files[i].getName() + " uploaded successfully.");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        // } else {
                        //     // read files[i] from the server
                        //     try {
                        //         InputStream inputStream = ftpClient.retrieveFileStream(files[i].getName());
                        //         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        //         String line;
                        //         while ((line = reader.readLine()) != null) {
                        //             System.out.println(line);
                        //         }
                        //         reader.close();
                        //         ftpClient.completePendingCommand();
                        //     } catch (IOException e) {
                        //         e.printStackTrace();
                        //     }
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