public class PullThread extends Thread {
    private CommandsBuffer commandsBuffer;

    public PullThread(CommandsBuffer commandsBuffer) {
        this.commandsBuffer = commandsBuffer;
    }

    public void run() {
        for (int i = 0; i < 100; i++) {
            String cmd = commandsBuffer.pop();
            System.out.println("POP : "+cmd);
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();// TODO: handle exception
            }
        }
    }
}