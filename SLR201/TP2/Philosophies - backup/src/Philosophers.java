import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Random;

public class Philosophers extends Thread {

    private final int id;
    private final Random random = new Random();
    private int mealsEaten = 0;
    // private final String outputFileName;
    private BufferedWriter bout;

    // variables for socket
    // private final int PORT = 8888;
    private Socket socket;
    BufferedWriter bufferedWriter;
    BufferedReader bufferedReader;    

    public Philosophers(int id, int PORT, BufferedWriter bout) {
        this.id = id;
        this.bout = bout;
        // initialization for socket
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            SocketAddress socketAddress = new InetSocketAddress(inetAddress, PORT);
            socket = new Socket();
            socket.connect(socketAddress, 1000);

            // OutputStream outputStream = socket.getOutputStream();
            // OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            // bufferedWriter = new BufferedWriter(outputStreamWriter);

            // InputStream inputStream = socket.getInputStream();
            // InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            // bufferedReader = new BufferedReader(inputStreamReader);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // methodes for socket
    public void sendMessage(String msg) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            bufferedWriter.write(msg+"\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Client " + this.id + " send message " + msg);
    }

    public String receiveMessage() {
        String msg = null;
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            msg = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }
    // methodes end

    public void increasementMealsEaten() {
        mealsEaten ++;
    }

    public void thinking() {
        // System.out.println("Philosopher " + id + " ----> thinking");
        printLogs("thinking");
        try {
            Thread.sleep(random.nextInt(256)); // 随机思考时间
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void takeForks() {
        sendMessage(this.id + "t");
        String msg = receiveMessage();
        System.out.println("reveive: " + msg);
        if("take successful".equals(msg)) {
        // System.out.println("Philosopher " + id + " takes left fork");
            printLogs("takes left fork");
        // System.out.println("Philosopher " + id + " takes right fork");
            printLogs("takes right fork");
        } else {
            try {
                wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // eating();
    }

    public void eating() {
        increasementMealsEaten();
        printLogs("eating");
        // System.out.println("Philosopher " + id + " ----> eating");
        try {
            Thread.sleep(random.nextInt(256)); // 随机进餐时间
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void putForks() {
        // System.out.println("Philosopher " + id + " releases forks");
        sendMessage(this.id + "r");
        String msg = receiveMessage();
        System.out.println(msg);
        if("release successful".equals(msg)) {
            printLogs("releases forks");
            notifyAll();
        }
    }

    public void printLogs(String actionsOfPhilosopher) {
        String msg;
        if(actionsOfPhilosopher == "eating") {
            msg = ("Philosopher " + id + " " + actionsOfPhilosopher + " times:" + mealsEaten);
        } else {
            msg = ("Philosopher " + id + " " + actionsOfPhilosopher);
        }

        try {
            bout.write(msg);
            bout.newLine();
            bout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

    public void run() {
        
        while (true) {
            thinking();
            takeForks();
            eating();
            putForks();
        }
    }

}



