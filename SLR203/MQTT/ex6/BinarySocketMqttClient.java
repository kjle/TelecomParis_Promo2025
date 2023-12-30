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

public class BinarySocketMqttClient  {

    private int PORT = 1883;
    Socket socket; 
    OutputStream outputStream = null;
    InputStream inputStream = null;

    public void initSocket() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            SocketAddress socketAddress = new InetSocketAddress(inetAddress, PORT);
            socket = new Socket();
            socket.connect(socketAddress, 1000);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(byte[] msg) {
        try {
            outputStream.write(msg);
            outputStream.flush();
            System.out.print("Message sent: ");
            for (byte b : msg) {
                System.out.print(String.format("0x%02X ", b));
            }
            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage() {
        try {
            byte[] buffer = new byte[4];
            inputStream.read(buffer);
            System.out.print("Message received: ");
            for (byte b : buffer) {
                System.out.print(String.format("0x%02X ", b));
            }
            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        // System.out.println("Hello World!");
        BinarySocketMqttClient binarySocketMqttClient = new BinarySocketMqttClient();
        binarySocketMqttClient.initSocket();

        // // CONNECT message packet
        byte[] connectPacket = {
            0x10,                   // Control Header: CONNECT (Message Type)
            0x18,                   // Remaining Length
            0x00, 0x04,             // Variable Header: Protocol Name Length
            0x4d, 0x51, 0x54, 0x54, // Variable Header: MQTT (Protocol Name in UTF-8)
            0x04,                   // Variable Header: Protocol Level (4)
            0x02,                   // Variable Header: Connect Flags
                                    //                  User Name Flag = 0
                                    //                  Password Flag = 0
                                    //                  Will Retain = 0
                                    //                  Will QoS = 0
                                    //                  Will Flag = 0
                                    //                  Clean Session = 1
                                    //                  Reserved = 0 
            0x00, 0x3C,             // Variable Header: Keep Alive = 60
            0x00, 0x0C,             // Payload: Length
            'C', 'l', 'i', 'e', 'n', 't', 'S', 'o', 'c', 'k', 'e', 't'// Client Identifier
        };

        
        // send the CONNECT message
        binarySocketMqttClient.sendMessage(connectPacket);

        // receive the CONNACK message
        binarySocketMqttClient.receiveMessage();

        // PUBLISH message packet
        byte[] publishPacket = {
            0x32,                   // Control Header: PUBLISH (Message Type)
            0x14,                   // Remaining Length
            0x00, 0x03,             // Variable Header: Topic Length
            'l', 'a', 'b',          // Variable Header: Topic Name
            0x12, 0x34,             // Packet Identifier
            0x0C,                   // Payload: Length
            'H', 'e', 'l', 'l', 'o', ',', 'w', 'o', 'r', 'l', 'd', '!' // Payload: Message
        };
            
        // send the PUBLISH message
        binarySocketMqttClient.sendMessage(publishPacket);

        // receive the PUBACK message (PUBACK)
        binarySocketMqttClient.receiveMessage();
    }
}
