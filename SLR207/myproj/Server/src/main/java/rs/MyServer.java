package rs;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class MyServer {

    private static String usr = "jkang-23";
    private static String pwd = "8888";
    private static int ftpPort = 8423;
    public static int socketPort = 9999;
    public static Logger logger = Logger.getLogger(MyServer.class.getName());

    private static String homeDirPath = "/dev/shm/";

    public static Map<String, Integer> map = new HashMap<>();

    public static void main(String[] args) {
        MyFTPServer ftpServer = new MyFTPServer(0, MyServer.class.getResource("/log4J.properties"), "users.properties", ftpPort, usr, pwd, homeDirPath);

        SocketThread socketThread = new SocketThread();
        socketThread.start();
    }


    public static void CallbakMAP() {
        System.out.println("[MAP] Callback MAP");
        // String fileDir = homeDirPath + usr;
        // File directory = new File(fileDir);
        // if (!directory.exists()) {
        //     System.out.println("[MAP] Directory does not exist.");
        // } else {
        //     File[] files = directory.listFiles();
        //     for (File file : files) {
        //         String content = "";
        //         if (file.isFile()) {
        //             content += file.getName() + "\n";
        //         }
        //         String[] words = content.split(" ");
        //         for (String word : words) {
        //             int hash = word.hashCode();


        //         }
        //     }
        // }

    }

    public static void CallbakSTART() {
        System.out.println("[START] Callback START");
    }
}

class SocketThread extends Thread {
    public MySocketServer mySocketServer = null;
    private Logger logger = Logger.getLogger(SocketThread.class.getName());

    @Override
    public void run() {
        mySocketServer = new MySocketServer(MyServer.socketPort);
        String message = null;
        message = mySocketServer.socketServerReceive();

        if (message.equals("Connect_Established")) {
            
            while(true) {
                message = mySocketServer.socketServerReceive();
                logger.info("[info][SocketThread] receive message: " + message);
                if (message.equals("MAP")) {
                    MyServer.CallbakMAP();
                    mySocketServer.socketServerSend("OK");
                } else if (message.equals("START")) {
                    MyServer.CallbakSTART();
                    mySocketServer.socketServerSend("OK");
                } else if (message.equals("QUIT")) {
                    mySocketServer.closeSocketServer();
                    break;
                }
            }
        } else {
            logger.warning("[warning][SocketThread] Connection to server is closed!");
        }

    }

}