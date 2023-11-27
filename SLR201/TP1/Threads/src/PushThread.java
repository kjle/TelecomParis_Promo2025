public class PushThread extends Thread {
    private CommandsBuffer commandsBuffer;

    public PushThread(CommandsBuffer commandsBuffer) {
        this.commandsBuffer = commandsBuffer;
    }

    public void run() {
        for (int i = 0; i < 100; i++) {
            commandsBuffer.push("command "+i);
            System.out.println("PUSH: command "+i);
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();// TODO: handle exception
            }
        }
    }
}