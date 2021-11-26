import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The type Read thread.
 */
public class ReadThread extends Thread {
    private final ClientApplication client;
    /**
     * @param clientSocket This socket is the communication endpoint of the client and is thus responsible for the connection
     * Responsible between server and client (client side). Responsible for receiving messages.
     * @param client       instance of ClientApplication.
     */
    private BufferedReader reader;

    /**
     * Method for creating a new reader object
     *
     * @param client the client
     */
    public ReadThread(ClientApplication client) {
        this.client = client;

        try {
            InputStream input = client.getClientSocket().getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error due to InputStream: " + ex.getMessage());
        }
    }

    /**
     * Initializes a new ReadThread, which allows the client to receive and output any number of messages from the server.
     */
    public void run() {
        while (!client.isClientWillShutdown()) {
            try {
                String s = reader.readLine();
                if (s == null) break;
                System.out.println(s);
            } catch (IOException ex) {
                if (!client.isClientWillShutdown()) {
                    System.out.println("Connection error:\n" + ex.getMessage());
                }
            }
        }
    }
}

