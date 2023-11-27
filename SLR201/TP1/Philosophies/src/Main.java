public class Main {
    public static void main(String[] args) {
        int numPhilosophers = 5;
        Fork[] forks = new Fork[numPhilosophers];
        Philosophers[] philosophers = new Philosophers[numPhilosophers];

        for (int i = 0; i < numPhilosophers; i++) {
            forks[i] = new Fork();
        }

        for (int i = 0; i < numPhilosophers; i++) {
            if (i == numPhilosophers -1)
                        philosophers[i] = new Philosophers(i, forks[(i + 1) % numPhilosophers], forks[i]);
            else
                        philosophers[i] = new Philosophers(i, forks[i], forks[(i + 1) % numPhilosophers]);
            philosophers[i].start();
        }
    }
}
