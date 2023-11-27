public class MyThread extends Thread {
    private String threadName ;

    public MyThread(String threadName)
    {
        this.threadName = threadName ; 
    }

    public void run() {
        for (int i = 1; i <= 100; i++) {
            System.out.println(threadName + ":" + i);
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();// TODO: handle exception
            }
        }
        System.out.println(threadName + ": END");
    }

}