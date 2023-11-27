import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Main {
    public static void main(String[] args) {
        String outputFileName = "philosophers_out.txt";
        String outputTarget = "Console"; // "Console" or "File"
        int numPhilosophers = 5;
        Philosophers[] philosophers = new Philosophers[numPhilosophers];
        BufferedWriter bout = null;

        if ("File".equals(outputTarget)) {
            try {
                bout = new BufferedWriter(new FileWriter(outputFileName, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                bout = new BufferedWriter(new OutputStreamWriter(System.out));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        philosophers[0] = new Philosophers(0, 8888, bout);
        philosophers[1] = new Philosophers(1, 8888, bout);
        philosophers[2] = new Philosophers(2, 8888, bout);
        philosophers[3] = new Philosophers(3, 8888, bout);
        philosophers[4] = new Philosophers(4, 8888, bout);

        philosophers[0].start();
        philosophers[1].start();
        philosophers[2].start();
        philosophers[3].start();
        philosophers[4].start();
        
    }
}
