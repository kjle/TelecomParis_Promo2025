import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServerProgram {

   public static void main(String args[]) {

       ServerSocket listener = null;
       String line;
       BufferedReader is;
       BufferedWriter os;
       Socket socketOfServer = null;

       // Try to open a server socket on port 9999
       // Note that we can't choose a port less than 1023 if we are not
       // privileged users (root)

 
       try {
           listener = new ServerSocket(9999);
       } catch (IOException e) {
           System.out.println(e);
           System.exit(1);
       }

       try {
           System.out.println("Server is waiting to accept user...");

           // Accept client connection request
           // Get new Socket at Server.    
           socketOfServer = listener.accept();
           System.out.println("Accept a client!");

           // Open input and output streams
           is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
           os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));


           while (true) {
               // Read data to the server (sent from client).
               line = is.readLine();
               
               // Write to socket of Server
               // (Send to client)
               os.write(">> " + line);
               // End of line
               os.newLine();
               // Flush data.
               os.flush();  
                System.out.println("Client sent: " + line);


               // If users send QUIT (To end conversation).
               if (line.equals("QUIT")) {
                   os.write(">> OK");
                   os.newLine();
                   os.flush();
                   break;
               }
           }

       } catch (IOException e) {
           System.out.println(e);
           e.printStackTrace();
       }
       System.out.println("Sever stopped!");
   }
}