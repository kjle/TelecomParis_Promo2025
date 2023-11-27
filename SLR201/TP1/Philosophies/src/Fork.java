public class Fork {
    private boolean inUse;

    public Fork() {
        this.inUse = false;
    }

    public synchronized void takeFork() {
        while (inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        inUse = true;
    }

    public synchronized void releaseFork() {
        inUse = false;
        notify();
    }
}
