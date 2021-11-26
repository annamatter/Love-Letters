package de.lmu.adrette_apparate.server;

import de.lmu.adrette_apparate.server.cards.Card;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * The type Player thread.
 */
public class PlayerThread extends Thread {
    private final Socket serverSidedSocket;
    private final ServerApplication server;
    private final ArrayList<Card> discardPile = new ArrayList<>();
    private final Card[] cardArray = new Card[2];
    private final BufferedReader reader;
    private PrintWriter writer;
    private boolean gameStart = false;
    private boolean joinedGame = false;
    private String playerName;
    private int tokens;
    private boolean playerInRound;
    private boolean playedHandmaid;
    private Card hasCountess;
    private Game game;
    private boolean wonLastRound;
    private boolean isOnTurn;
    private Card activePlayedCard;

    /**
     * Instantiates a new Player thread.
     *
     * @param serverSidedSocket the server sided socket
     * @param server            the server
     * @throws IOException the io exception
     */
    public PlayerThread(Socket serverSidedSocket, ServerApplication server) throws IOException {
        this.serverSidedSocket = serverSidedSocket;
        this.server = server;
        this.reader = new BufferedReader(new InputStreamReader(serverSidedSocket.getInputStream()));
    }

    /**
     * Gets card array.
     *
     * @return the card [ ]
     */
    public Card[] getCardArray() {
        return this.cardArray;
    }

    /**
     * Gets card 0.
     *
     * @return the card 0
     */
    public Card getCard0() {
        return this.cardArray[0];
    }

    /**
     * Sets card 0.
     *
     * @param card the card
     */
    public void setCard0(Card card) {
        this.cardArray[0] = card;
    }

    /**
     * Gets card 1.
     *
     * @return the card 1
     */
    public Card getCard1() {
        return this.cardArray[1];
    }

    /**
     * Sets card 1.
     *
     * @param card the card
     */
    public void setCard1(Card card) {
        this.cardArray[1] = card;
    }

    /**
     * Gets player name.
     *
     * @return the player name
     */
    public String getPlayerName() {
        return this.playerName;
    }

    /**
     * Gets active played card.
     *
     * @return the active played card
     */
    public Card getActivePlayedCard() {
        return this.activePlayedCard;
    }

    /**
     * Sets active played card.
     *
     * @param activePlayedCard the active played card
     */
    public void setActivePlayedCard(Card activePlayedCard) {
        this.activePlayedCard = activePlayedCard;
    }

    /**
     * Gets if player has the countess in their hand.
     *
     * @return the has countess
     */
    public Card getHasCountess() {
        return this.hasCountess;
    }

    /**
     * Sets if player has the countess in their hand.
     *
     * @param countess the countess
     */
    public void setHasCountess(Card countess) {
        this.hasCountess = countess;
    }


    /**
     * Starting the thread for the sequence of a client
     */
    public void run() {
        /*
         * Reader: Is used to read data from the "serverSidedSocket"
         * Writer: Is used to write data to the "serverSidedSocket"
         * As soon as the client has connected, notification of joining is sent
         * Sends chat messages until the client replies with 'bye'
         *
         */
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(serverSidedSocket.getInputStream()));

            OutputStream output = serverSidedSocket.getOutputStream();
            writer = new PrintWriter(output, true);

            do {
                sendMessage("Please enter your nickname.");
                playerName = reader.readLine();
            } while (!server.addPlayerName(playerName, this));


            String messageFromClient;
            String serverExe = "Server";

            do {
                messageFromClient = reader.readLine();

                if (messageFromClient == null) {
                    // might happen on exit
                    break;
                }

                if (messageFromClient.startsWith("@")) {
                    handleCommandPrivateChat(messageFromClient);
                } else if (messageFromClient.equals("/joinGame")) {
                    server.joinGame(this, playerName);
                } else if (messageFromClient.equals("/startGame")) {
                    if (this.joinedGame == true && (!server.isActiveGame())) {
                        server.startGame(serverSidedSocket, this);
                    } else {
                        server.broadcastSingleMessage("You didn't joined the game before! Can't start the game.",
                                serverExe, this);
                    }
                }

//Game message-------------------------------------------------------------------------------------------------------------------------------------------------

                else if (messageFromClient.startsWith("/game") && isOnTurn) {
                    handleCommandGame(messageFromClient);
                }

                else if (messageFromClient.equals("/highscore")){
                    server.broadcastSingleMessage(server.getHighscore(), "Server", this);
                }


                //Game logic
//-------------------------------------------------------------------------------------------------------------------------------------------------------------
                /*
                 * Deserialization and sending of public messages
                 */
                else {
                    server.broadcastMessage("[" + playerName + "]: " + messageFromClient, this);
                }

            } while (!messageFromClient.equals("bye"));

            server.removePlayer(playerName, this);
            serverSidedSocket.close();

            server.broadcastMessage(playerName + " left the room", this);
        }
        /*
         * @exception IOException In the case of UserThread errors, an IOException is thrown.
         */ catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
        }
    }

    /**
     * Sends message.
     *
     * @param message The message to be sent
     */
    void sendMessage(String message) {
        writer.println(message);
    }

    /**
     * Reads the line.
     *
     * @return the string
     * @throws IOException the io exception
     */
    public String readLine() throws IOException {
        return reader.readLine();
    }

    /**
     * Handles command of private chat.
     * @param messageFromClient
     * @throws IOException
     */
    private void handleCommandPrivateChat(String messageFromClient) throws IOException {
        String[] msg = messageFromClient.split(" ", 2);

        if (msg.length != 2) {
            server.broadcastSingleMessage(
                    "Invalid request. Please write private messages by e.g. writing '@Bob My private Message'!",
                    "Server", this);
            return;
        }

        String recipientName = msg[0].substring(1);

        if (server.isRecipientAvailable(recipientName)) {
            int index = server.getRecipientIndex(recipientName);
            PlayerThread recipientThread = server.getRecipientThread(index);
            server.broadcastSingleMessage(msg[1], playerName, recipientThread);
            server.serverLog("<private to " + recipientName + "> " + "[" + playerName + "]: " + msg[1]);
        } else {
            server.broadcastSingleMessage("The selected recipient does not exist!", "Server", this);
        }
    }

    /**
     * Handles the commands of game.
     * @param messageFromClient
     * @throws IOException
     */
    private void handleCommandGame(String messageFromClient) throws IOException {
        // TODO Karteninput im Read/Write Thread muss noch geregelt werden
        //checks if it is the player's turn

        // TODO Am Ende, wenn Karteneffekt abgehandelt worden ist, muss dieser Wert wieder auf null gesetzt werden (Idee: eigene Methode in Game erstellen, am Ende der Karten aufrufen)
        //Has player already selected card?
        if (getActivePlayedCard() == null) {

            //card-selection if "/game card1" or "/game card2"
            if (messageFromClient.equals("/game card1")) {
                setActivePlayedCard(getCard0());
            } else if (messageFromClient.equals("/game card2")) {
                setActivePlayedCard(getCard1());
            }
            else {
                // TODO else Block reagiert noch nicht, bei falscher Eingabe - Write-Read-Thread auf Client Seite anschauen!
                //player type wrong input..
                server.broadcastSingleMessage("Please check your input: type in '/game card1' or '/game card2'",
                        "server", this);
                return;
            }

            server.serverLog(("ActivePlayedCard: " + getActivePlayedCard().getCardName()));
            server.serverLog("Client choose card: " + getActivePlayedCard().getCardName() + " " +
                    getActivePlayedCard().getCardValue());
        }

        if (getActivePlayedCard() != null) {
            // TODO Input vom Player muss String gespeichert und mit übergeben werden /Wenn welcher da ist If-Abfrage? Oder ohne If-Abfrage, weil Karten ohne benötigten Input nur 1mal aufgerufen werden müssen?)

            String cardInputFromPlayer = "No input yet";

            //Calling the method function of the actively played card.
            getActivePlayedCard().cardFunction(server, this, cardInputFromPlayer);
            if (!(getActivePlayedCard().getCardName().equals("Handmaid")
                    || getActivePlayedCard().getCardName().equals("Countess")
                    || getActivePlayedCard().getCardName().equals("Princess"))) {
                if (!(getActivePlayedCard().getCounter() == 2)) {
                    while (!(getActivePlayedCard().getCounter() == 3)) {
                        cardInputFromPlayer = reader.readLine();
                        getActivePlayedCard().cardFunction(server, this, cardInputFromPlayer);
                    }
                }
            }
            game.playerFinishCardFunction(this);
            return;
        }

        //false: Message - not your turn.
        server.broadcastSingleMessage("It is not your turn. Please wait", "server", this);


//-----------------------------------------------------------------------------------------------------------------------------------------------------------------

//                    if (game.getRoundThread() >= game.getThreadsActiveInRound().length){
//                        game.setRoundThread(0);
//                    }
/*                    server.serverLog("t:" + CardDeck.getInstance().getCurrentTop());
                    server.serverLog("s: round thread " + game.getRoundThread());

                    if(server.selectablePlayerThreads().length > 1 && CardDeck.getInstance().getCurrentTop() < 15){
                        game.round(game.getRoundThread());

                    }

                    else {
                        //If-Abfrage was passiert, wenn die Karten ausgehen -->Regeln
                        PlayerThread winningPlayer = server.selectablePlayerThreads()[0];
                        winningPlayer.setTokens(winningPlayer.getTokens() + 1);
                        game.endPreRound();
                        if (!server.existWinnerOfCompleteGame(game.getThreadsActiveInRound())) {
                            game.preRoundStart();
                            game.round(0);
                        } else {
                            game.endGame();
                        }
                    }*/
    }


    /**
     * Gets tokens.
     *
     * @return the tokens
     */
    public int getTokens() {
        return tokens;
    }

    /**
     * Sets tokens.
     *
     * @param tokens the tokens
     */
    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    /**
     * Is player in round boolean.
     *
     * @return the boolean
     */
    public boolean isPlayerInRound() {
        return playerInRound;
    }

    /**
     * Sets player in round.
     *
     * @param playerInRound the player in round
     */
    public void setPlayerInRound(boolean playerInRound) {
        this.playerInRound = playerInRound;
    }

    /**
     * Gets played handmaid.
     *
     * @return the played handmaid
     */
    public boolean getPlayedHandmaid() {
        return playedHandmaid;
    }

    /**
     * Sets played handmaid.
     *
     * @param value the value
     */
    public void setPlayedHandmaid(boolean value) {
        this.playedHandmaid = value;
    }


    /**
     * Clean hand card.
     */
    public void cleanHandCard() {
        if (cardArray[0] == null) {
            cardArray[0] = cardArray[1];
            cardArray[1] = null;
        }
    }


    /**
     * Game has started.
     *
     * @param value the value
     */
    public void setGameHasStarted(boolean value) {
        gameStart = value;
    }

    /**
     * Sets joined game.
     *
     * @param value the value
     */
    public void setJoinedGame(boolean value) {
        joinedGame = value;
    }

    /**
     * Add to discard pile.
     *
     * @param card the card
     */
    public void addToDiscardPile(Card card) {
        this.discardPile.add(card);
    }

    /**
     * Sets game.
     *
     * @param game the game
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Is won last round boolean.
     *
     * @return the boolean
     */
    public boolean isWonLastRound() {
        return wonLastRound;
    }

    /**
     * Sets won last round.
     *
     * @param wonLastRound the won last round
     */
    public void setWonLastRound(boolean wonLastRound) {
        this.wonLastRound = wonLastRound;
    }

    /**
     * Sets is on turn.
     *
     * @param onTurn the on turn
     */
    public void setIsOnTurn(boolean onTurn) {
        isOnTurn = onTurn;
    }
}
