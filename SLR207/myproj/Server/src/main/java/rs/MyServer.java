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
    public static int PRT_CNT = 100000;

    private static String usr = "jkang-23";
    private static String pwd = "8888";
    private static int ftpPort = 8423;
    private static int socketPort = 9009;

    private static String homeDirPath = "/dev/shm/";

    public static int index;
    public static int serversNum;
    private static ThreadListerner[] threadListerners;
    private static ServerSocket listerner = null;
    private static Socket socketOfServer;
    private static Socket [] sockets;
    private static BufferedReader is;
    private static BufferedWriter os;
    private static BufferedWriter[] oss = null;

    private static HashMap<Integer, String> svrIdx_svrAddr_map; // server info : <serverIdx, serverAddress>
    private static HashMap<Integer, List<String>> toSvrIdx_wList_map; // words info: each words should go to which server, <serverIdx, wordsList>
    private static HashMap<Integer, Integer> range_svrIdx_map; // range map for all servers in Calculate, <count_range(count include), serverIdx>

    private static HashMap<String, Integer> words_cnt_pre_map; // words count map respective to this server in Pershuffle, <words, count>
    private static HashMap<Integer, HashMap<String, Integer>> words_cnt_map; //merge all threads' maps into one map, <serverIdx, <words, count>>
    private static HashMap<String, List<Integer>> words_cList_map; // merge all maps(words_cnt_pre_map and words_cnt_map) into one map, <words, countList>
    private static HashMap<Integer, List<String>> cnt_wList_map; // merge all maps into one map to build count wordsList map, <count, wordsList>
    private static HashMap<Integer, List<String>> cnt_wList_final_map; // final count wordsList map in Preshuffle2 and Shuffle2, <count, wordsList>

    public static void main(String[] args) {

        MyServer myServer = new MyServer();

        // start FTP server to receive files
        myServer.openFTPServer(ftpPort, usr, pwd, homeDirPath);

        // start socket server
        String line;

        svrIdx_svrAddr_map = new HashMap<Integer, String>();
        toSvrIdx_wList_map = new HashMap<Integer, List<String>>();
        words_cnt_pre_map = new HashMap<String, Integer>();

        words_cnt_map = new HashMap<Integer, HashMap<String, Integer>>();
        range_svrIdx_map = new HashMap<Integer, Integer>();

        words_cList_map = new HashMap<String, List<Integer>>();
        cnt_wList_map = new HashMap<Integer, List<String>>();

        cnt_wList_final_map = new HashMap<Integer, List<String>>();


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
                        svrIdx_svrAddr_map.put(Integer.parseInt(token[0]), token[1]);
                        if (!(token[2].isEmpty()) && token[2].equals("thisserver")) {
                            index = Integer.parseInt(token[0]);
                        }
                    }
                    msgINFO = true;
                    serversNum = svrIdx_svrAddr_map.size();
                    // System.out.println("[INFO][MyServer][main] serversNum: " + serversNum + " index: " + index);
                    // // open listener threads
                    // threadListerners = openThreadListerners(serversNum, socketPort, index);
                    // // open socket client threads
                    // oss = openThreadSockets(serversNum, socketPort, index, svrIdx_svrAddr_map);
                }

                // get msg
                line = is.readLine();

                ////////// receive SPLIT msg //////////                
                if(line.equals("SPLIT")){
                    SPLITHandler();
                }
                ////////// receive PRESHUFFLE msg //////////
                else if (line.equals("PRESHUFFLE")) {
                    PRESHUFFLEHandler();
                }
                ////////// receive WAITREADY msg //////////
                else if (line.equals("WAITREADY")) {
                    WAITREADYHandler();
                }
                ////////// receive SHUFFLE msg //////////
                else if (line.equals("SHUFFLE")) {
                    SHUFFLEHandler();
                }
                ////////// receive CALCULATE msg //////////
                else if (line.equals("CALCULATE")) {
                    CALCULATEHandler();
                }
                ////////// receive PRESHUFFLE2 msg //////////
                else if(line.equals("PRESHUFFLE2")){
                    PRESHUFFLE2Handler();
                }
                ////////// receive WAITREADY2 msg //////////
                else if (line.equals("WAITREADY2")) {
                    WAITREADY2Handler();
                }
                ////////// receive SHUFFLE2 msg //////////
                else if (line.equals("SHUFFLE2")) {
                    SHUFFLE2Handler();
                }
                ////////// receive QUIT msg //////////
                else if (line.equals("QUIT")) {
                    QUITHandler();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][main] An error occurred about socket msg receive.");
            e.printStackTrace();
        }
    }

    private static void waitForThreadsReady(ThreadListerner[] threadListerners) {
        for (int i = 0; i < serversNum; i++) {
            if (i == index) {
                continue;
            }
            try {
                threadListerners[i].join();
            } catch (InterruptedException e) {
                System.err.println("[ERROR][MyServer][waitForThreadsReady] Thread " + i + " was interrupted.");
                e.printStackTrace();
            }
        }
        System.out.println("[INFO][MyServer][waitForThreadsReady][" + index +"] at time [" + System.currentTimeMillis() + "] all threads are ready.");
        
        // close threadListerners
        for (int i = 0; i < serversNum; i++) {
            if (i == index) {
                continue;
            }
            threadListerners[i].closeThread();
        }

    }

    private static void SPLITHandler () {
        System.out.println("[INFO][MyServer][SPLIT][" + index +"]");
        File localDir = new File(homeDirPath + usr + "/");
        File[] files = localDir.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().contains(".wet")) {
                System.out.println("[DEBUG][MyServer][SPLIT][" + index +"] file: " + file.getName());
                try {
                    Scanner scanner = new Scanner(file);
                    while (scanner.hasNextLine()) {
                        String txtline = scanner.nextLine();
                        String[] txtTokens = txtline.split("\\s+");
                        // System.out.println("[DEBUG][MyServer][SPLIT][" + index +"] " + txtTokens.length);
                        for (String txtToken : txtTokens) {
                            // System.out.println("[DEBUG][MyServer][SPLIT][" + index +"] " + txtToken);
                            int toWhichServer = txtToken.hashCode() % serversNum;
                            if (toSvrIdx_wList_map.containsKey(toWhichServer)) {
                                toSvrIdx_wList_map.get(toWhichServer).add(txtToken);
                            } else {
                                List<String> wordsList = new ArrayList<String>();
                                wordsList.add(txtToken);
                                toSvrIdx_wList_map.put(toWhichServer, wordsList);
                            }
                        }
                    }
                    scanner.close();
                } catch (FileNotFoundException e) {
                    System.out.println("[ERROR][MyServer][SPLIT] An error occurred about build toSvrIdx_wList_map.");
                    e.printStackTrace();
                }
                
            }
        }
        // send back
        try {
            os.write("SPLIT_OK");
            os.newLine();
            os.flush();
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][SPLIT][" + index +"] bufferedwrite os error.");
            e.printStackTrace();
        }
    }

    private static void PRESHUFFLEHandler() {
        System.out.println("[INFO][MyServer][PRESHUFFLE][" + index +"]");

        // open listener threads
        threadListerners = openThreadListerners(serversNum, socketPort, index);
        // open socket client threads
        oss = openThreadSockets(serversNum, socketPort, index, svrIdx_svrAddr_map);

        for (Map.Entry<Integer, List<String> > entry : toSvrIdx_wList_map.entrySet()) {
            if (svrIdx_svrAddr_map.containsKey(entry.getKey()) && entry.getKey() != index) {
                // String msg = "$PRESHUFFLE_THREAD$;";
                try {
                    oss[entry.getKey()].write("$PRESHUFFLE_THREAD$;");
                } catch (IOException e) {
                    System.err.println("[ERROR][MyServer][PRESHUFFLE][" + index +"] bufferedwrite oss error.");
                    e.printStackTrace();
                }
                int wc = 0;

                for (String word : entry.getValue()) {
                    // msg += word + ";";
                    try {
                        oss[entry.getKey()].write(word + ";");
                        wc += 1;
                        if (wc % PRT_CNT == 0) {
                            System.out.println("[INFO][MyServer][PRESHUFFLE][" + index +"] send to server[" + entry.getKey() + "] at time [" + System.currentTimeMillis() + "] progress: " + wc + "/" + entry.getValue().size());
                        }
                    } catch (IOException e) {
                        System.err.println("[ERROR][MyServer][PRESHUFFLE][" + index +"] bufferedwrite oss error.");
                        e.printStackTrace();
                    }
                    // System.out.println("[INFO][MyServer][PRESHUFFLE][" + index +"] send to Thread[" + entry.getKey() + "] word: " + word);
                }
                try {
                    // oss[entry.getKey()].write(msg);
                    oss[entry.getKey()].newLine();
                    oss[entry.getKey()].flush();
                    // System.out.println("[INFO][MyServer][PRESHUFFLE][" + index +"] send to thread[" + entry.getKey() + "] msg: " + msg);
                } catch (IOException e) {
                    System.err.println("[ERROR][MyServer][PRESHUFFLE][" + index +"] bufferedwrite oss error.");
                    e.printStackTrace();
                }
            } else {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    if (i % PRT_CNT == 0) {
                        System.out.println("[INFO][MyServer][PRESHUFFLE][" + index +"] at time [" + System.currentTimeMillis() + "] build <words_cnt_pre_map> progress: " + i + "/" + entry.getValue().size());
                    }
                    if (words_cnt_pre_map.containsKey(entry.getValue().get(i))) {
                        int count = words_cnt_pre_map.get(entry.getValue().get(i));
                        words_cnt_pre_map.put(entry.getValue().get(i), count + 1);
                    } else {
                        words_cnt_pre_map.put(entry.getValue().get(i), 1);
                    }
                }
            }
        }

        // need to send END PRESHUFFLE_THREAD to end the conversation of threads
        for (int i = 0; i < serversNum; i++) {
            if (i == index) {
                continue;
            }
            try {
                oss[i].write("$TERMINATE_WHILE$");
                oss[i].newLine();
                oss[i].flush();
            } catch (IOException e) {
                System.err.println("[ERROR][MyServer][PRESHUFFLE][" + index +"] bufferedwrite oss error.");
                e.printStackTrace();
            }
        }

        // // print words_cnt_pre_map
        // System.out.println("[INFO][MyServer][main][" + index +"] //////////words_cnt_pre_map//////////");
        // for (Map.Entry<String, Integer> entry : words_cnt_pre_map.entrySet()) {
        //     System.out.println("[INFO][MyServer][main][" + index +"] " + entry.getKey() + " " + entry.getValue());
        // }

        // send back
        try {
            os.write("PRESHUFFLE_OK");
            os.newLine();
            os.flush();
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][PRESHUFFLE][" + index +"] bufferedwrite os error.");
            e.printStackTrace();
        }
    }

    private static void WAITREADYHandler () {
        // wait for all threads are ready
        System.out.println("[INFO][MyServer][WAITREADY][" + index +"] at time [" + System.currentTimeMillis() + "] wait for all threads are ready ...");
        waitForThreadsReady(threadListerners);

        // send back
        try {
            os.write("WAITREADY_OK");
            os.newLine();
            os.flush();
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][WAITREADY][" + index +"] bufferedwrite os error.");
            e.printStackTrace();
        }
    }

    private static void SHUFFLEHandler () {
        // // wait for all threads are ready
        // System.out.println("[INFO][MyServer][SHUFFLE][" + index +"] at time [" + System.currentTimeMillis() + "] wait for all threads are ready ...");
        // waitForThreadsReady(threadListerners);
        // try {
        //     Thread.sleep(500);
        // } catch (InterruptedException e) {
        //     System.err.println("[ERROR][MyServer][SHUFFLE][" + index +"] An error occurred about sleep.");
        //     e.printStackTrace();
        // }

        System.out.println("[INFO][MyServer][SHUFFLE][" + index +"] at time [" + System.currentTimeMillis() + "] start to shuffle ...");

        // start threadlisterners
        // for (ThreadListerner thread : threadListerners) {
        //     if (thread != null) {
        //         thread.start();
        //     }
        // }

        
        // put all threads' maps into one hashmap, i.e. words_cnt_map
        int cnt = 0;
        for (ThreadListerner thread : threadListerners) {
            if (thread != null) {
                HashMap<String, Integer> map = thread.get_words_cnt_thread_map();
                words_cnt_map.put(cnt, map);
            }
            cnt += 1;
        }

        // merge all maps into one map, i.e. words_cList_map
        for (Map.Entry<String, Integer> entry : words_cnt_pre_map.entrySet()) {
            if (words_cList_map.containsKey(entry.getKey())) {
                words_cList_map.get(entry.getKey()).add(entry.getValue());
            } else {
                List<Integer> list = new ArrayList<Integer>();
                list.add(entry.getValue());
                words_cList_map.put(entry.getKey(), list);
            }
        }
        for (Map.Entry<Integer, HashMap<String, Integer> > entry : words_cnt_map.entrySet()) {
            for (Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
                if (words_cList_map.containsKey(entry2.getKey())) {
                    words_cList_map.get(entry2.getKey()).add(entry2.getValue());
                } else {
                    List<Integer> list = new ArrayList<Integer>();
                    list.add(entry2.getValue());
                    words_cList_map.put(entry2.getKey(), list);
                }
            }
        }
        // // print words_cList_map
        // System.out.println("[INFO][MyServer][main][" + index +"] //////////words_cList_map//////////");
        // for (Map.Entry<String, List<Integer> > entry : words_cList_map.entrySet()) {
        //     System.out.println("[INFO][MyServer][main][" + index +"] " + entry.getKey() + " " + entry.getValue());
        // }
        
        // send back
        try {
            os.write("SHUFFLE_OK");
            os.newLine();
            os.flush();
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][SHUFFLE][" + index +"] bufferedwrite os error.");
            e.printStackTrace();
        }          
    }

    private static void CALCULATEHandler () {
        System.out.println("[INFO][MyServer][CALCULATE][" + index +"]");
        // calculate the max and min count of each word
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (Map.Entry<String, List<Integer> > entry : words_cList_map.entrySet()) {
            int sum = 0;
            for (int i = 0; i < entry.getValue().size(); i++) {
               sum += entry.getValue().get(i);
            }
            if (sum > max) {
                max = sum;
            }
            if (sum < min) {
                min = sum;
            }
            // merge all words and counts into one map, i.e. cnt_wList_map
            if (cnt_wList_map.containsKey(sum)) {
                cnt_wList_map.get(sum).add(entry.getKey());
            } else {
                List<String> list = new ArrayList<String>();
                list.add(entry.getKey());
                cnt_wList_map.put(sum, list);
            }
        }
        if (max == Integer.MIN_VALUE || min == Integer.MAX_VALUE) {
            max = 0;
            min = 0;
        }

        // send back
        try {
            os.write("CALCULATE_OK");
            os.newLine();
            os.flush();
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][CALCULATE][" + index +"] bufferedwrite os error.");
            e.printStackTrace();
        }

        // send fmax fmin
        try {
            os.write(max + ";" + min);
            os.newLine();
            os.flush();
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][CALCULATE][" + index +"] bufferedwrite os error.");
            e.printStackTrace();
        }

        // receive fmax fmin
        
        try {
            String line = is.readLine();
            String [] tokens = line.split(";");
            // String rangePrint = "";
            for (int i = 0; i < tokens.length; i++) {
                String [] token = tokens[i].split(",");
                for (int rg = Integer.parseInt(token[2]); rg <= Integer.parseInt(token[1]); rg++) {
                    if (rg == 0) {
                        continue;
                    }
                    range_svrIdx_map.put(rg, Integer.parseInt(token[0]));
                    // if (token[0].equals(String.valueOf(index))) {
                    //     rangePrint += rg + " ";
                    // }
                }
            }
            // System.out.println("[INFO][MyServer][RANGE][" + index +"] range: " + rangePrint);
            // System.out.println("[INFO][MyServer][CALCULATE][" + index +"] <range_svrIdx_map> ");
            // for (Map.Entry<Integer, Integer> entry : range_svrIdx_map.entrySet()) {
            //     System.out.println("[INFO][MyServer][CALCULATE][" + index +"] " + entry.getKey() + " " + entry.getValue());
            // }
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][RANGE][" + index +"] bufferedread is error.");
            e.printStackTrace();
        }
        
        // send back
        try {
            os.write("RANGE_OK");
            os.newLine();
            os.flush();
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][RANGE][" + index +"] bufferedwrite os error.");
            e.printStackTrace();
        }
    }

    private static void PRESHUFFLE2Handler () {
        System.out.println("[INFO][MyServer][PRESHUFFLE2][" + index +"]");

        // open listener threads
        threadListerners = openThreadListerners(serversNum, socketPort, index);
        // open socket client threads
        oss = openThreadSockets(serversNum, socketPort, index, svrIdx_svrAddr_map);

        for (Map.Entry<Integer, List<String>> entry : cnt_wList_map.entrySet()) {
            if (range_svrIdx_map.get(entry.getKey()) != index) {
                // String msg = "$PRESHUFFLE2_THREAD$;" + entry.getKey() + ";";
                try {
                    oss[range_svrIdx_map.get(entry.getKey())].write("$PRESHUFFLE2_THREAD$;" + entry.getKey() + ";");
                } catch (IOException e) {
                    System.err.println("[ERROR][MyServer][PRESHUFFLE2][" + index +"] bufferedwrite oss error.");
                    e.printStackTrace();
                }
                for (String word : entry.getValue()) {
                    // msg += word + ";";
                    try {
                        oss[range_svrIdx_map.get(entry.getKey())].write(word + ";");
                    } catch (IOException e) {
                        System.err.println("[ERROR][MyServer][PRESHUFFLE2][" + index +"] bufferedwrite oss error.");
                        e.printStackTrace();
                    }
                }
                try {
                    // oss[range_svrIdx_map.get(entry.getKey())].write(msg);
                    oss[range_svrIdx_map.get(entry.getKey())].newLine();
                    oss[range_svrIdx_map.get(entry.getKey())].flush();
                    // System.out.println("[INFO][MyServer][main][" + index +"][PRESHUFFLE2] send to server " + range_svrIdx_map.get(entry.getKey()) + " msg: " + msg);
                } catch (IOException e) {
                    System.err.println("[ERROR][MyServer][PRESHUFFLE2][" + index +"] bufferedwrite oss error.");
                    e.printStackTrace();
                }
                // System.out.println("[DEBUG][MyServer][main][" + index +"][PRESHUFFLE2] send to server " + range_svrIdx_map.get(entry.getKey()) + " finished.");
            } else {
                if (cnt_wList_final_map.containsKey(entry.getKey())) {
                    for (String word : entry.getValue()) {
                        cnt_wList_final_map.get(entry.getKey()).add(word);
                    }
                } else {
                    cnt_wList_final_map.put(entry.getKey(), entry.getValue());
                }
                // System.out.println("[DEBUG][MyServer][main][" + index +"][PRESHUFFLE2] locally process finished. ");

            }
        }
        // need to send END PRESHUFFLE2_THREAD to end the conversation of threads
        for (int i = 0; i < serversNum; i++) {
            if (i == index) {
                continue;
            }
            try {
                oss[i].write("$TERMINATE_WHILE$");
                oss[i].newLine();
                oss[i].flush();
            } catch (IOException e) {
                System.err.println("[ERROR][MyServer][PRESHUFFLE2][" + index +"] bufferedwrite oss error.");
                e.printStackTrace();
            }
        }

        // send back
        try {
            os.write("PRESHUFFLE2_OK");
            os.newLine();
            os.flush();
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][PRESHUFFLE2][" + index +"] bufferedwrite os error.");
            e.printStackTrace();
        }
    }

    private static void WAITREADY2Handler () {
        // wait for all threads are ready
        System.out.println("[INFO][MyServer][WAITREADY2][" + index +"] at time [" + System.currentTimeMillis() + "] wait for all threads are ready ...");
        waitForThreadsReady(threadListerners);

        // send back
        try {
            os.write("WAITREADY2_OK");
            os.newLine();
            os.flush();
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][WAITREADY2][" + index +"] bufferedwrite os error.");
            e.printStackTrace();
        }
    }

    private static void SHUFFLE2Handler () {
        System.out.println("[INFO][MyServer][SHUFFLE2][" + index +"] at time [" + System.currentTimeMillis() + "] start to shuffle ...");

        for (ThreadListerner thread : threadListerners) {
            if (thread != null) {
                HashMap<Integer, List<String>> cnt_words_thread_map = thread.get_cnt_words_thread_map();
                for (Map.Entry<Integer, List<String>> entry : cnt_words_thread_map.entrySet()) {
                    if (cnt_wList_final_map.containsKey(entry.getKey())) {
                        for (String word : entry.getValue()) {
                            cnt_wList_final_map.get(entry.getKey()).add(word);
                        }
                    } else {
                        cnt_wList_final_map.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        // send back
        try {
            os.write("SHUFFLE2_OK");
            os.newLine();
            os.flush();
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][SHUFFLE2][" + index +"] bufferedwrite os error.");
            e.printStackTrace();
        }        
    }

    private static void QUITHandler () {
        // print
        // System.out.println("[INFO][MyServer][" + index +"] //////////Words Count//////////");
        // for (Map.Entry<Integer, List<String> > entry : cnt_wList_final_map.entrySet()) {
        //     System.out.println("[INFO][MyServer][" + index +"] " + entry.getKey() + " " + entry.getValue());
        // }
        System.out.println("[INFO][MyServer][QUIT][" + index +"] All done! Ready to quit.");
        try {
            os.write("QUIT_OK");
            os.newLine();
            os.flush();
        } catch(IOException e) {
            System.err.println("[ERROR][MyServer][QUIT][" + index +"] bufferedwrite os error.");
            e.printStackTrace();
        }
        try {
            os.close();
            is.close();
            socketOfServer.close();
            for (int i = 0; i < serversNum; i++) {
                if (i == index) {
                    continue;
                }
                sockets[i].close();
                oss[i].close();
                threadListerners[i].closeThread();
            }
        } catch (IOException e) {
            System.err.println("[ERROR][MyServer][QUIT][" + index +"] close error.");
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
    /// @param svrIdx_svrAddr_map servers information map
    /// @return oss BufferedWriter array
    private static BufferedWriter[] openThreadSockets(int serversNum, int socketport, int index, HashMap<Integer, String> svrIdx_svrAddr_map) {
        BufferedWriter[] oss = new BufferedWriter[serversNum];
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.err.println("[ERROR][MyServer][main] An error occurred about sleep.");
            e.printStackTrace();
        }
        
        sockets = new Socket[serversNum];
        try {
            for (int i = 0; i < serversNum; i++) {
                if (i == index) {
                    continue;
                }
                sockets[i] = new Socket(svrIdx_svrAddr_map.get(i), socketPort + index + 1);
                System.out.println("[INFO][MyServer][openThreadSocket][" + index + "] connect to server: " + svrIdx_svrAddr_map.get(i) + " success!");
                oss[i] = new BufferedWriter(new OutputStreamWriter(sockets[i].getOutputStream()));
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
    private static ThreadListerner[] openThreadListerners(int serversNum, int socketport, int index) {
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
    private BufferedReader is;
    private InputStreamReader isReader;
    private HashMap<String, Integer> words_cnt_thread_map = new HashMap<String, Integer>(); // words count map respective to other server in Pershuffle, execute by thread, <words, count>
    private HashMap<Integer, List<String>> cnt_words_thread_map = new HashMap<Integer, List<String>>(); // count words map respective to other server in Pershuffle2, execute by thread, <count, wordsList>

    // flag to indicate whether the thread is ready, true means ready
    public boolean PRESHUFFLE_THREAD_READY = true;

    public ThreadListerner(ServerSocket listerner) {
        this.listerner = listerner;
    }

    public HashMap<String, Integer> get_words_cnt_thread_map() {
        return words_cnt_thread_map;
    }

    public HashMap<Integer, List<String>> get_cnt_words_thread_map() {
        return cnt_words_thread_map;
    }

    public void closeThread() {
        try {
            is.close();
            isReader.close();
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
            isReader = new InputStreamReader(socketOfServer.getInputStream());
            is = new BufferedReader(isReader);

            while (true) {
                String line = is.readLine();
                // System.out.println("[DEBUG][ThreadListerner][idx="+ MyServer.index +"][run] msg:" + line);
                String [] tokens = line.split(";");
                if(tokens[0].contains("$PRESHUFFLE_THREAD$")) {
                    System.out.println("[DEBUG][ThreadListerner][idx="+ MyServer.index +"][run] at time [" + System.currentTimeMillis() + "] $PRESHUFFLE_THREAD$");
                    // System.out.println("[DEBUG][ThreadListerner][idx="+ MyServer.index +"][run] msg:" + line);
                    PRESHUFFLE_THREAD_READY = false;
                    for (int i = 1; i < tokens.length; i++) {
                        if (i % MyServer.PRT_CNT == 0) {
                            System.out.println("[INFO][ThreadListerner][idx="+ MyServer.index +"][run] at time [" + System.currentTimeMillis() + "][$PRESHUFFLE_THREAD$] progress: " + i + "/" + tokens.length);
                        }
                        if (words_cnt_thread_map.containsKey(tokens[i])) {
                            int count = words_cnt_thread_map.get(tokens[i]);
                            words_cnt_thread_map.put(tokens[i], count + 1);
                        } else {
                            words_cnt_thread_map.put(tokens[i], 1);
                        }
                        // System.out.println("[DEBUG][ThreadListerner][idx="+ MyServer.index +"][i="+ i +"][run] PRESHUFFLE_THREAD_READY:" + PRESHUFFLE_THREAD_READY);
                    }
                    System.out.println("[INFO][ThreadListerner][idx="+ MyServer.index +"][run] at time [" + System.currentTimeMillis() + "][$PRESHUFFLE_THREAD$] progress: " + tokens.length + "/" + tokens.length);
                    PRESHUFFLE_THREAD_READY = true;
                    // break;
                } else if (tokens[0].contains("$PRESHUFFLE2_THREAD$")) {
                    // System.out.println("[DEBUG][ThreadListerner][idx="+ MyServer.index +"][run] at time [" + System.currentTimeMillis() + "] $PRESHUFFLE2_THREAD$");
                    PRESHUFFLE_THREAD_READY = false;
                    int count = Integer.parseInt(tokens[1]);
                    for (int i = 2; i < tokens.length; i++) {
                        if (cnt_words_thread_map.containsKey(count)) {
                            cnt_words_thread_map.get(count).add(tokens[i]);
                        } else {
                            List<String> list = new ArrayList<String>();
                            list.add(tokens[i]);
                            cnt_words_thread_map.put(count, list);
                        }
                    }
                    PRESHUFFLE_THREAD_READY = true;
                    // System.out.println("[INFO][ThreadListerner][idx="+ MyServer.index +"][run] at time [" + System.currentTimeMillis() + "][$PRESHUFFLE_THREAD2$] progress: " + tokens.length + "/" + tokens.length);
                    // break;
                } else if (tokens[0].contains("$TERMINATE_WHILE$")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR][ThreadListerner][run]");
            e.printStackTrace();
        }
    }
}