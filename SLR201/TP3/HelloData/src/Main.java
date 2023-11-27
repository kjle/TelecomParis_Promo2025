public class Main {
    public static void main(String[] args) {
        HelloData data = new HelloData("message","transient message");
        DataSerializer.serializeData(data);
        HelloData undata = DataUnserializer.unserializeData();
        System.out.println(undata);
    }
}
