package de.lmu.adrette_apparate.server;

import de.lmu.adrette_apparate.server.cards.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The type Server application.
 *
 * @author Maxi, Anna
 * @see PlayerThread
 */
public class ServerApplication {

    final int MAX_PLAYERS = 4;
    private final int port;
    //private Set<WrapperUser> wrapperUserSet = new LinkedHashSet<>();
    private final Set<String> nameSet = new LinkedHashSet<>();
    private final Set<PlayerThread> threadSet = new LinkedHashSet<>();
    private final Set<String> ListOfActivePlayers = new LinkedHashSet<>();
    //keine wirkliche Liste, Namen ändern?
    private final Set<PlayerThread> ListOfActiveThreads = new LinkedHashSet<>();
    Card cardForFunction;
    private PlayerThread[] threadsOfActivePlayers;
    private String[] nameOfActivePlayers;
    private boolean activeGame = false;
    private int joinedPlayers = 0;
    private Game game;

    /**
     * Instantiates a new Server application.
     *
     * @param port the port
     */
    public ServerApplication(int port) {
        this.port = port;

    }

    /**
     * The entry point of the server application.
     *
     * @param args The parameter that the user specifies when starting the server.
     */
    public static void main(String[] args) {
        /*
         * The default value for port is 44444.
         * The server will start.
         */
        int port;

        if (args.length < 1) {
            System.out.println("Client runs on localhost and predefined port");
            port = 44444;
        } else {
            port = Integer.parseInt(args[0]);
        }

        System.out.println("Starting server, listening on port " + port);

        ServerApplication serverapplication = new ServerApplication(port);
        serverapplication.startServer();
    }

    /**
     * Gets game.
     *
     * @return the game
     */
    public Game getGame() {
        return game;
    }

    /**
     * Starts the server.s
     */
    public void startServer() {
        /*
         * A ServerSocket is created on the port.
         */
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server ready!");
            InetAddress address = InetAddress.getLocalHost();
            System.out.println("IP address local host: " + address.getHostAddress());
            /*
             * Socket accepts new connection.
             * A new thread is added for each new user.
             * Adds newPlayerThread to the threadSet.
             */
            while (true) {
                Socket clientOnServerSocket = serverSocket.accept();
                PlayerThread newPlayerThread = new PlayerThread(clientOnServerSocket, this);
                threadSet.add(newPlayerThread);
                newPlayerThread.start();
            }
        }
        /*
         * @exception IOException In the event of a server error, an IOException is thrown.
         */ catch (IOException ex) {
            System.out.println("Server error: " + ex.getMessage());
        }


    }

    /**
     * Broadcasts message.
     *
     * @param message        Contains the message to be sent.
     * @param excludedPlayer The author of the message is the excludedPlayer                       Sends message to everyone except yourself
     */
    public void broadcastMessage(String message, PlayerThread excludedPlayer) {
        serverLog(message);
        for (PlayerThread aPlayer : threadSet) {
            if (aPlayer != excludedPlayer) {
                aPlayer.sendMessage(message);
            }
        }
    }

    /**
     * Broadcasts game message.
     *
     * @param message        the message
     * @param excludedPlayer the excluded player
     */
    public void broadcastGameMessage(String message, PlayerThread excludedPlayer) {
        for (PlayerThread aPlayer : threadsOfActivePlayers) {
            if (aPlayer != excludedPlayer) {
                aPlayer.sendMessage(message);
            }
        }
    }

    /**
     * Broadcast private message.
     *
     * @param messageToRecipient Contains the message for the recipient
     * @param senderName         Contains the name who is sending the message
     * @param recipientThread    used by server to send message to specific client                           Sends message to specific user
     */
    public void broadcastSingleMessage(String messageToRecipient, String senderName, PlayerThread recipientThread) {
        recipientThread.sendMessage("<private> " + "[" + senderName + "]: " + messageToRecipient);
    }

    /**
     * Broadcasts card.
     *
     * @param card            the card
     * @param recipientThread the recipient thread
     */
    void broadcastCard(Card card, PlayerThread recipientThread) {
        recipientThread.sendMessage(card.getCardName() + "\n ==> " + card.getCardDescription());
    }

    /**
     * Gets recipient index.
     * Tells us where in the list the player is.
     *
     * @param recipientName Receiver of the private message
     * @return index : Location in the nameSet-Array Return the location of the recipient in the nameSet-Array
     */
    public int getRecipientIndex(String recipientName) {
        String[] nameSetArray = nameSet.toArray(new String[nameSet.size()]);
        int returnValue = 0;
        for (int i = 0; i < nameSetArray.length; i++) {
            if (nameSetArray[i].equals(recipientName)) {
                returnValue = i;
            }
        }
        return returnValue;
    }

    /**
     * Gets recipient thread.
     *
     * @param index Location in the nameSet-Array
     * @return Thread of specific User: used by server to send message to specific client Return the thread of the recipient user
     */
    public PlayerThread getRecipientThread(int index) {
        PlayerThread[] threadSetArray = threadSet.toArray(new PlayerThread[threadSet.size()]);
        return threadSetArray[index];
    }

/*
    public String[] getNameOfActivePlayers() {

        return nameOfActivePlayers;
    }
*/

    /**
     * Gets the thread of active players.
     *
     * @return threads of active players
     */
    public PlayerThread[] getThreadsOfActivePlayers() {
        return threadsOfActivePlayers;
    }

    /**
     * Gets if game is active.
     *
     * @return the game is active
     */
    public boolean getGameIsActive() {
        return activeGame;
    }

    /**
     * Logs messages to the serverlog.
     *
     * @param message the message
     */
    public void serverLog(String message) {
        System.out.println(message);
    }

    /**
     * Adds player name.
     *
     * @param playerName Playername.
     * @param aPlayer    Instance of the PlayerThread.
     * @return true if player was added, false otherwise
     */
    public boolean addPlayerName(String playerName, PlayerThread aPlayer) {
        /*
         * If the player name already exists, the user will be asked again to enter a name.
         * Otherwise it will be added directly to the nameSet.
         */

        if (nameSet.contains(playerName)) {
            aPlayer.sendMessage("Nickname already exists! Please enter a new name");
            return false;
        }

        if (!playerName.matches("^\\w{3,25}$")) {
            aPlayer.sendMessage("Nickname is invalid! Please enter a new name (3-25 chars, no special characters)");
            return false;
        }

        aPlayer.sendMessage("Welcome " + playerName);
        nameSet.add(playerName);

        broadcastMessage(playerName + " joined the room", aPlayer);
        return true;
    }

    /**
     * Checks if recipient is available.
     *
     * @param recipientName Receiver of the private message
     * @return if recipient is in nameSet (online)
     */
    boolean isRecipientAvailable(String recipientName) {
        return nameSet.contains(recipientName);
    }

    /**
     * Removes player from list if not available and writes internal server message.
     *
     * @param playerName Playername.
     * @param aPlayer    Instance of the PlayerThread.
     */
    public void removePlayer(String playerName, PlayerThread aPlayer) {
        boolean removed = nameSet.remove(playerName);
        if (removed) {
            threadSet.remove(aPlayer);
            System.out.println("Player " + playerName + " left the server.");
        }

    }

    /**
     * Actual players int.
     *
     * @return the int of how many players are part of the game.
     */
    public int actualPlayers() {
        if (nameSet.size() < MAX_PLAYERS) {
            return nameSet.size();
        } else {
            return MAX_PLAYERS;
        }
    }

    /**
     * Joins game.
     *
     * @param joinedPlayer the joined player
     * @param playerName   the player name
     */
    public void joinGame(PlayerThread joinedPlayer, String playerName) {
        joinedPlayers++;
        String messageToEveryone;
        if (activeGame == false) { //Checks, if Game has already stared
            if (joinedPlayers < 5) {
                joinedPlayer.sendMessage("[Server]: You have joined the game!");
                messageToEveryone = "[" + playerName + "]: " + "joined the Game and is waiting.";
                this.broadcastMessage(messageToEveryone, joinedPlayer);
                ListOfActivePlayers.add(playerName);
                ListOfActiveThreads.add(joinedPlayer);
                joinedPlayer.setJoinedGame(true);
            } else {
                joinedPlayer.sendMessage("[Server]: Game is already full. You can't join the game.");
            }
        } else {
            joinedPlayer.sendMessage("[Server]: Sorry, the game has already started!");
        }
    }

    /**
     * Checks if already joined.
     *
     * @param recipientName the recipient name
     * @return the boolean
     */
    boolean isAlreadyJoined(String recipientName) {
        return nameSet.contains(recipientName);
    }

    /**
     * Update thread set and name set.
     */
    public void updateThreadSetAndNameSet() {
        nameOfActivePlayers = ListOfActivePlayers.toArray(String[]::new);
        threadsOfActivePlayers = ListOfActiveThreads.toArray(PlayerThread[]::new);

    }

    public void setThreadsOfActivePlayers(PlayerThread[] threadsOfActivePlayers) {
        this.threadsOfActivePlayers = threadsOfActivePlayers;
    }

    public String[] getNameOfActivePlayers() {
        return nameOfActivePlayers;
    }

    public void setNameOfActivePlayers(String[] nameOfActivePlayers) {
        this.nameOfActivePlayers = nameOfActivePlayers;
    }

    /**
     * Start game.
     */
//    public boolean enoughPlayer () {
//        return nameOfActivePlayers.length >= 2;
//    }


    /**
     * Checks if game is active.
     *
     * @return the boolean if game is active
     */
    public boolean isActiveGame() {
        return activeGame;
    }

    /**
     * Sets activeGame on true or false
     * @param activeGame
     */

    public void setActiveGame(boolean activeGame) {
        this.activeGame = activeGame;
    }

    /**
     * Starts game.
     *
     * @param clientOnServerSocket the client on server socket
     * @param startingPlayer       the starting player
     */
    public void startGame(Socket clientOnServerSocket, PlayerThread startingPlayer) {

        this.updateThreadSetAndNameSet();
        try {
            if (nameOfActivePlayers.length >= 2) {
                activeGame = true;

                System.out.println("Game is starting...");

                for (int index = 0; index < nameOfActivePlayers.length; index++) {
                    System.out.println(nameOfActivePlayers[index]);
                } // Array

                Game game = new Game(nameOfActivePlayers, threadsOfActivePlayers, this);


                //Object [] discardArray = discardPile.getDiscardPileArray();
                //for (int index = 0; index < discardArray.length; index++) {
                //  System.out.println(discardArray[index]);
                //}

                for (int index = 0; index < threadsOfActivePlayers.length; index++) {
                    //nur die ersten 4 Leute sollen spielen dürfen
                    threadsOfActivePlayers[index].setGameHasStarted(true);
                    threadsOfActivePlayers[index].setGame(game);
                }
                game.start();
            } else {
                System.out.println(
                        "Could not start the game. Either game has already started or not enough players to start a game. YOU NOOB");
                startingPlayer.sendMessage(
                        "Could not start the game. Either game has already started or not enough players to start a game.");
            }
        } catch (NullPointerException | IOException ex) {
            startingPlayer.sendMessage("There is not a single Player ready to play:  " + ex);
            System.out.println("There is not a single Player ready to play: " + ex);
        }
    }


    /**
     * Gets player order.
     *
     * @return the player order
     */
    public String getPlayerOrder() {
        String returnValue = "Player order for this round:" +
                "\nPlayer 1: " + this.nameOfActivePlayers[0] +
                "\nPlayer 2: " + this.nameOfActivePlayers[1];
        if (this.nameOfActivePlayers.length >= 3) {
            returnValue = (returnValue + "\nPlayer 3: " + this.nameOfActivePlayers[2]);
        }
        if (this.nameOfActivePlayers.length >= 4) {
            returnValue = (returnValue + "\nPlayer 4: " + this.nameOfActivePlayers[3]);
        }
        return returnValue;
    }

    /**
     * Gets highscore.
     *
     * @return the highscore
     */
    public String getHighscore() {
        String returnValue = "Highscores in this round:" +
                "\nPlayer 1: " + this.nameOfActivePlayers[0] + " " + this.threadsOfActivePlayers[0].getTokens() +
                "\nPlayer 2: " + this.nameOfActivePlayers[1] + " " + this.threadsOfActivePlayers[1].getTokens();
        if (this.nameOfActivePlayers.length >= 3) {
            returnValue = (returnValue + "\nPlayer 3: " + this.nameOfActivePlayers[2]) + " " +
                    this.threadsOfActivePlayers[2].getTokens();
        }
        if (this.nameOfActivePlayers.length >= 4) {
            returnValue = (returnValue + "\nPlayer 4: " + this.nameOfActivePlayers[3] + " " +
                    this.threadsOfActivePlayers[3].getTokens());
        }
        return returnValue;
    }
    /* isPlayerInRound() doesnt work yet. May also work with setters in PlayerThread --> used in Game.Java

     */

    /**
     * Gets card from wrapper.
     *
     * @param i the
     * @return the card from wrapper
     */
    public Card getCardFromWrapper(int i) {


        switch (i) {
            case 1:
                Guard guard = new Guard();
                return guard;
            case 2:
                Priest priest = new Priest();
                return priest;
            case 3:
                Baron baron = new Baron();
                return baron;
            case 4:
                Handmaid handmaid = new Handmaid();
                return handmaid;
            case 5:
                Prince prince = new Prince();
                return prince;
            case 6:
                King king = new King();
                return king;
            case 7:
                Countess countess = new Countess();
                return countess;
            case 8:
                Princess princess = new Princess();
                return princess;
        }
        return null;
    }

    /**
     * Selectable player names array list.
     *
     * @return the array list of names that can be selected
     */
    public ArrayList selectablePlayerNames() {
        //benötigt Name/Thread vom Spieler, der aktuell dran ist
        ArrayList<String> listSelectablePlayer = new ArrayList<String>();
        for (int i = 0; i < threadsOfActivePlayers.length; i++) {
            if ((threadsOfActivePlayers[i].isPlayerInRound()) &&
                    (threadsOfActivePlayers[i].getPlayedHandmaid() == false)) {
                // to do: aktueller Spieler soll nicht in der Liste aufgeführt werden
                listSelectablePlayer.add(nameOfActivePlayers[i]);
                this.serverLog("Anzeige Spieler in Runde" + nameOfActivePlayers[i]);
            }
        }
        return listSelectablePlayer;
    }


    /**
     * Selectable player threads player thread [ ].
     *
     * @return the player thread [ ] that can be selected
     */
    public PlayerThread[] selectablePlayerThreads() { //benötigt Name/Thread vom Spieler, der aktuell dran ist
        Set<PlayerThread> setOfRoundThreads = new LinkedHashSet<>();
        for (int i = 0; i < threadsOfActivePlayers.length; i++) {
            if (threadsOfActivePlayers[i].isPlayerInRound()) {
                setOfRoundThreads.add(threadsOfActivePlayers[i]);
            }
        }
        PlayerThread[] arrayOfRoundThreads = setOfRoundThreads.toArray(PlayerThread[]::new);
        return arrayOfRoundThreads;
    }

}








