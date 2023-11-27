import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    int numPhilosophers = 5;
    Fork[] forks = new Fork[numPhilosophers];

    public Server () {
        for (int i = 0; i < numPhilosophers; i++) {
            forks[i] = new Fork();
        }
    }

    public Server (int numPhilosophers) {
        this.numPhilosophers = numPhilosophers;
        for (int i = 0; i < numPhilosophers; i++) {
            forks[i] = new Fork();
        }
    }

    public static void main(String[] args) {
        int PORT = 8010;
        int numPhilosophers = 5;

        Server forkServer = new Server(numPhilosophers);
        Socket socket = null;
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while(true) {
                socket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(socket, forkServer.forks, numPhilosophers);
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
    private int numPhilosophers;

    public ServerThread (Socket socket, Fork[] forks, int numPhilosophers) {
        this.socket = socket;
        this.forks = forks;
        this.numPhilosophers = numPhilosophers;
    }

    @Override
    public void run() {
        // System.out.println("run");
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
                msg = bufferedReader.readLine();
                // System.out.println("receive:" + msg);

                outputStream = socket.getOutputStream();
                outputStreamWriter = new OutputStreamWriter(outputStream);
                bufferedWriter = new BufferedWriter(outputStreamWriter);
                if (msg == null) {
                    continue;
                } else {
                    int philosopherID = Integer.parseInt(msg.substring(0, 1));
                    // System.out.println("ID:" + philosopherID);
                    switch (msg.substring(1, 2)) {
                        case "t":
                            synchronized(forks) {
                                forks[philosopherID].takeFork();
                                forks[(philosopherID+1)%5].takeFork();
                                bufferedWriter.write("take successful\n");
                                // System.out.println(philosopherID + "take successful");
                                bufferedWriter.flush(); 
                            }                   
                            break;

                        case "r":
                            forks[philosopherID].releaseFork();
                            forks[(philosopherID+1)%numPhilosophers].releaseFork();
                            bufferedWriter.write("release successful\n");
                            bufferedWriter.flush();
                            break;
                    
                        default:
                            break;
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Fork {
    private boolean inUse;

    public Fork() {
        this.inUse = false;
    }

    public synchronized void takeFork() {
        while (inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        inUse = true;
    }

    public synchronized void releaseFork() {
        inUse = false;
        notifyAll();
    }
}

