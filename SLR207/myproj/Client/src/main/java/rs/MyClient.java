package rs;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;


public class MyClient {

    public static boolean localFlag = false;

    public static String [] servers;
    public static String usr = "jkang-23";
    public static String pwd = "8888";
    public static int ftpPort = 8423;
    public static int socketPort = 9999;
    // local directory path
    public static String localDirPath = "./dataset";
    public static Logger logger = Logger.getLogger(MyClient.class.getName());

    public static String[] readMachine(String filePath) {
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
        return machines.toArray(new String[0]);
    }

    public static void main(String[] args) {

        // read servers from machines.txt
        if (!localFlag) {
            servers = readMachine("machines.txt");
            logger.info("[info][MyClient] Change to distributed mode !");
        } else {
            servers = new String[] {"localhost"};
            logger.info("[info][MyClient] Change to local mode !");
        }

        // start ftp client and assign files to servers
        for (int i=0; i<servers.length; i++) {
            FTPThread ftpThread = new FTPThread(i);
            ftpThread.start();
            logger.info("[info][MyClient] FTPThread [" + i + "] started and connected to [" + servers[i] + "]:" + ftpPort);
        }

        // start socket client
        SocketThread[] socketThreads = new SocketThread[servers.length];
        for (int i=0; i<servers.length; i++) {
            socketThreads[i] = new SocketThread(i, servers[i], socketPort);
            socketThreads[i].start();
            logger.info("[info][MyClient] SocketThread  [" + i + "] started and connected to [" + servers[i] + "]: " + socketPort);
        }

        // make sure all socket servers are ready
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i=0; i<servers.length; i++) {
            socketThreads[i].mySocketClient.socketClientSend("MAP");
            String responseLine;
            while (true) {
                responseLine = socketThreads[i].mySocketClient.socketClientReceive();
                if (responseLine.equals("OK")) {
                    socketThreads[i].mySocketClient.socketClientSend("QUIT");
                    break;
                }
            }
            socketThreads[i].mySocketClient.disconnectSocketClientToServer();
        }

    }

}

class FTPThread extends Thread {
    private int index;
    private Logger logger = Logger.getLogger(FTPThread.class.getName());

    FTPThread (int index) {
        this.index = index;
    }

    @Override
    public void run() {
        MyFTPClient myFTPClient = new MyFTPClient(new FTPClient(), MyClient.ftpPort, MyClient.servers[index], MyClient.usr, MyClient.pwd);
        myFTPClient.connectFTPClientToServer();
        
        File localDir = new File(MyClient.localDirPath);
        File[] files = localDir.listFiles();
        int serverNum = MyClient.servers.length;
        if (files != null) {
            for (int i=0; i<files.length; i++) {
                if (files[i].isFile()) {
                    if (i % serverNum == index) {
                        myFTPClient.uploadFileToServer(files[i]);
                        logger.info("[info][FTPThread] " + files[i].getName() + " assigned to " + MyClient.servers[index]);
                    } else {
                        continue;
                    }
                }
            }
        }

        myFTPClient.disconnectFTPClientToServer();

    }
}

class SocketThread extends Thread {
    private int index;
    private String server;
    private int port;
    public MySocketClient mySocketClient = null;
    private Logger logger = Logger.getLogger(SocketThread.class.getName());

    SocketThread (int index, String server, int port) {
        this.index = index;
        this.server = server;
        this.port = port;
    }

    @Override
    public void run() {
        mySocketClient = new MySocketClient(new Socket(), server, port);
        logger.info("[info][SocketThread] Socket client [" + index + "] connected to " + server);

        // mySocketClient.socketClientSend("111");
        // mySocketClient.socketClientSend("QUIT");

    }
}