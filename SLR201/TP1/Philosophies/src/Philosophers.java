import java.util.Random;

public class Philosophers extends Thread {
    private final int id;
    private final Fork leftFork;
    private final Fork rightFork;
    private final Random random = new Random();

    public Philosophers(int id, Fork leftFork, Fork rightFork) {
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    public void thinking() {
        System.out.println("Philosopher " + id + " is thinking.");
        try {
            Thread.sleep(random.nextInt(256)); // 随机思考时间
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void takeForks() {
        synchronized (leftFork) {
            leftFork.takeFork();
            System.out.println("Philosopher " + id + " picks up left fork.");
            synchronized (rightFork) {
                rightFork.takeFork();
                System.out.println("Philosopher " + id + " picks up right fork and starts eating.");
                eating();
            }
        }
    }

    public void eating() {
        System.out.println("Philosopher " + id + " is eating.");
        try {
            Thread.sleep(random.nextInt(256)); // 随机进餐时间
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void putForks() {
        System.out.println("Philosopher " + id + " puts down both forks.");
        leftFork.releaseFork();
        rightFork.releaseFork();
    }

    @Override
    public void run() {
        while (true) {
            thinking();
            takeForks();
            putForks();
        }
    }
}
