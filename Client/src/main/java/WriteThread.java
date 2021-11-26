import java.io.*;


/**
 * The type Write thread.
 */
public class WriteThread extends Thread {
    private final ClientApplication client;
    private PrintWriter writer;
    private BufferedReader bufferedReader;

    /**
     * Method for creating a Writer object
     *
     * @param client the client
     */
    public WriteThread(ClientApplication client) {
        this.client = client;
        try {
            OutputStream output = client.getClientSocket().getOutputStream();
            writer = new PrintWriter(output, true);
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException ex) {
            System.out.println("Error while connecting: " + ex.getMessage());
        }
    }

    /**
     * Initializes a new WriteThread, this allows the client to send any number of messages to the server.
     * Including the possibility to close the socket again.
     */
    public void run() {
        while (!client.isClientWillShutdown()) {
            try {
                String s = bufferedReader.readLine();
                writer.println(s);
                if (s.equals("bye")) client.exitGame();
            } catch (IOException ex) {
                System.out.println("Connection error:\n" + ex.getMessage());
            }
        }
    }


}
