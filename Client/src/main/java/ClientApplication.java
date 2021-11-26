import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * The type Client application.
 */
public class ClientApplication {

    private final String hostname;
    private final int port;
    private Socket clientSocket;
    private boolean clientWillShutdown;

    /**
     * Instantiates a new Client application.
     *
     * @param hostname the hostname
     * @param port     the port
     */
    public ClientApplication(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.clientSocket = null;
        this.clientWillShutdown = false;
    }

    /**
     * Creates client object (default port 44444 and localhost)
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        int port;
        String hostname;
        if (args.length != 2) {
            port = 44444;
            hostname = "localhost";
        } else {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
        }

        ClientApplication client = new ClientApplication(hostname, port);
        client.start();
    }

    /**
     * Boolean if client will shut down.
     *
     * @return the boolean
     */
    public boolean isClientWillShutdown() {
        return clientWillShutdown;
    }

    /**
     * Gets client socket.
     *
     * @return the client socket
     */
    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * Startup method tries to connect to the host
     * creates socket object with hostname and port
     * uses socket object to start reader/writer thread
     *
     * @throws UnknownHostException throws exception if the host cannot be found or is invalid
     * @throws IOException
     */
    public void start() {
        System.out.println("Connecting to server at " + this.hostname + ", port " + this.port);
        try {
            clientSocket = new Socket(hostname, port);
            System.out.println("Connected to server successfully!");

            new ReadThread(this).start();
            new WriteThread(this).start();

        } catch (UnknownHostException ex) {
            System.out.println("Server could not be found.\n" + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error:\n" + ex.getMessage());
        }
    }

    /**
     * Exits game and closes client socket.
     */
    public void exitGame() {
        this.clientWillShutdown = true;
        if (clientSocket == null || !clientSocket.isConnected()) return;
        System.out.println("Closing connection to server ...");
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Could not close connection:");
            e.printStackTrace();
        }
    }
}