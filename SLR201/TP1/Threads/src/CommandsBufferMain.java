import java.util.Arrays;

public class CommandsBufferMain {
    public static void main(String[] args) {
        CommandsBuffer commandBuffer = new CommandsBuffer();
        PushThread pushThread = new PushThread(commandBuffer);
        PullThread pullThread = new PullThread(commandBuffer);

        pushThread.start();
        pullThread.start();

        try {
            pushThread.join();
            pullThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(Arrays.toString(commandBuffer.getCommands()));

    }
}