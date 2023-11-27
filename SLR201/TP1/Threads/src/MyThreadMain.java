public class MyThreadMain {
    public static void main(String[] args) {
        MyThread myThreadA = new MyThread("myThreadA");
        MyThread myThreadB = new MyThread("myThreadB");
        MyThread myThreadC = new MyThread("myThreadC");

        myThreadA.start();
        myThreadB.start();
        myThreadC.start();

        try {
            myThreadA.join();
        } catch (Exception e) {
            e.printStackTrace();// TODO: handle exception
        }

        try {
            myThreadB.join();
        } catch (Exception e) {
            e.printStackTrace();// TODO: handle exception
        }

        try {
            myThreadC.join();
        } catch (Exception e) {
            e.printStackTrace();// TODO: handle exception
        }
    }
}