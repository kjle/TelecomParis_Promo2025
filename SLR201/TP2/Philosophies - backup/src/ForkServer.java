import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ForkServer {

    int numPhilosophers = 5;
    Fork[] forks = new Fork[numPhilosophers];

    public ForkServer () {
        for (int i = 0; i < numPhilosophers; i++) {
            forks[i] = new Fork();
        }
    }

    public static void main(String[] args) {
        ForkServer forkServer = new ForkServer();
        Socket socket = null;
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            while(true) {
                socket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(socket, forkServer.forks);
                serverThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

class ServerThread extends Thread {
    private Socket socket;
    private Fork[] forks;

    public ServerThread (Socket socket, Fork[] forks) {
        this.socket = socket;
        this.forks = forks;
    }

    @Override
    public void run() {
        System.out.println("run");
        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter bufferedWriter = null;

        InputStream inputStream = null;
        InputStreamReader inputStreamReader =  null;
        BufferedReader bufferedReader = null;

        while(true) {
            try {
                inputStream = socket.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                
                String msg = null;
                // while((msg = bufferedReader.readLine()) != null) {
                //     System.out.println("receive msg : " + msg);
                // }
                msg = bufferedReader.readLine();
                System.out.println("receive:" + msg);
                // socket.shutdownInput();

                outputStream = socket.getOutputStream();
                outputStreamWriter = new OutputStreamWriter(outputStream);
                bufferedWriter = new BufferedWriter(outputStreamWriter);

                int philosopherID = Integer.parseInt(msg.substring(0, 1));
                System.out.println("ID:" + philosopherID);
                switch (msg.substring(1, 2)) {
                    case "t":
                        forks[philosopherID].takeFork();
                        forks[(philosopherID+1)%5].takeFork();
                        bufferedWriter.write("take successful\n");
                        System.out.println(philosopherID + "take successful");
                        bufferedWriter.flush();                    
                        break;

                    case "r":
                        forks[philosopherID].releaseFork();
                        forks[(philosopherID+1)%5].releaseFork();
                        bufferedWriter.write("release successful\n");
                        bufferedWriter.flush();
                        break;
                
                    default:
                        break;
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }



}

        // // Output Config
        //         OutputStream outputStream = socket.getOutputStream();
        //         OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        //         bufferedWriter = new BufferedWriter(outputStreamWriter);

        //         // Input Config
        //         InputStream inputStream = socket.getInputStream();
        //         InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        //         bufferedReader = new BufferedReader(inputStreamReader);

        //         String msg = receiveMessage();
        //         System.out.println(msg);
        //         int philosopherID = Integer.parseInt(msg.substring(0, 1));
        //         switch (msg.substring(1, 2)) {
        //             case "t":
        //                 forks[philosopherID].takeFork();
        //                 forks[(philosopherID+1)%numPhilosophers].takeFork();
        //                 sendMessage("take successful");
        //                 System.out.println(philosopherID + " t");
        //                 break;

        //             case "r":
        //                 forks[philosopherID].releaseFork();
        //                 forks[(philosopherID+1)%numPhilosophers].releaseFork();
        //                 System.out.println(philosopherID + " r");
        //                 break;
                
        //             default:
        //                 break;
        //         }