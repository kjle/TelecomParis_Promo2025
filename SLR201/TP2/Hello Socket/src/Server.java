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
    private final int PORT = 8888;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private ServerSocket serverSocket;

    public Server () {
        try {
            serverSocket = new ServerSocket(PORT);
            Socket socket = serverSocket.accept();

            // Output Config
            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            // Input Config
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            bufferedWriter.write(msg);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receiveMessage() {
        String msg = null;
        try {
            msg = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public static void main(String[] args) {
        Server server = new Server();
        String receiveMsg = server.receiveMessage();
        System.out.println("server receive: " + receiveMsg);
        server.sendMessage("hello" + receiveMsg);
        try {
            server.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
