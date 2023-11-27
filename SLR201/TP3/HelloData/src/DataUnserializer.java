import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class DataUnserializer {
    public static HelloData unserializeData () {
        HelloData data = null;
        try {
            FileInputStream fin = new FileInputStream("hellodata.ser");
            ObjectInputStream in = new ObjectInputStream(fin);
            data = (HelloData)in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
