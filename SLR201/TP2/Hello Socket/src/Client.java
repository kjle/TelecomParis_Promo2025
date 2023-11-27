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

public class Client {
    private final int PORT = 8888;
    private Socket socket;
    BufferedWriter bufferedWriter;
    BufferedReader bufferedReader;
    
    public Client () {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            SocketAddress socketAddress = new InetSocketAddress(inetAddress, PORT);
            socket = new Socket();
            socket.connect(socketAddress, 1000);

            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

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
        Client client = new Client();
        client.sendMessage(" world\n");
        String msg = client.receiveMessage();
        System.out.println("client receive: " + msg);
        try {
            client.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
