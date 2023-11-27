import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class DataSerializer {
    public static void serializeData (HelloData data) {
        try {
            FileOutputStream fout = new FileOutputStream("hellodata.ser");
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(data);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
