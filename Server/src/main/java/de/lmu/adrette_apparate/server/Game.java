package de.lmu.adrette_apparate.server;

import de.lmu.adrette_apparate.server.cards.Card;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The type Game.
 */
public class Game {
    private final String[] nameOfActivePlayers;
    private final PlayerThread[] nameOfActiveThreads;
    private final ServerApplication server;
    private final Card[] discardPileForTwoPlayers = new Card[3];
    private PlayerThread[] threadsActiveInRound;
    private int roundThread;
    private Card discardedCard;
    private Queue <PlayerThread> queue = new ConcurrentLinkedQueue<>();
    private PlayerThread currentActivePlayer;


    /**
     * Instantiates a new Game.
     *
     * @param nameOfActivePlayers the name of active players
     * @param nameOfActiveThreads the name of active threads
     * @param server              the server
     */
    public Game(String[] nameOfActivePlayers, PlayerThread[] nameOfActiveThreads, ServerApplication server) {
        this.nameOfActivePlayers = nameOfActivePlayers;
        this.nameOfActiveThreads = nameOfActiveThreads;
        this.server = server;

    }

    /**
     * Pre game: gets everything ready for a new game.
     */
    public void preGame() {
        String messageToEveryone = "[Server:] You are now playing!";
        server.broadcastGameMessage(messageToEveryone, null);
        server.broadcastMessage("New Game has started!", null);

        for (int i = 0; i < nameOfActiveThreads.length; i++) {
            nameOfActiveThreads[i].setPlayerInRound(true);
        }
        server.serverLog(server.getPlayerOrder());
        server.serverLog(server.getHighscore());
    }

    /**
     * Starts the pre-round preparations.
     */
    public void preRoundStart() {
        System.out.println("New Round!");

        for (int i = 0; i < nameOfActiveThreads.length; i++) {
            nameOfActiveThreads[i].setPlayerInRound(true);
            server.serverLog("Checking isInRound: " + i + " " + nameOfActiveThreads[i].isPlayerInRound());
        }
        server.serverLog("I added all Threads in the queue "+ queue.addAll(List.of(nameOfActiveThreads)));


        server.serverLog(server.getPlayerOrder());
        server.serverLog(server.getHighscore());
        server.broadcastGameMessage(server.getHighscore(), null);
        server.broadcastGameMessage(server.getPlayerOrder(), null);

        CardDeck.getInstance().shuffle();
        discardedCard = CardDeck.getInstance().getTopCard();         //oberste Karte wird weggelegt.


        int index = 16 - (CardDeck.getInstance().getCurrentTop() + 1);
        System.out.println("I have  " + index + " Cards left on my Pile");

        //special case for 2 players: top three cards get discarded and both players get message
        if (nameOfActiveThreads.length == 2) {
            for (int i = 0; i < discardPileForTwoPlayers.length; i++) {
                discardPileForTwoPlayers[i] = CardDeck.getInstance().getTopCard();
                server.broadcastGameMessage("Card added to discard pile: " + discardPileForTwoPlayers[i].getCardName(),
                        null);
            }
        }

        //Next Step - Deal cards to each player


        for (int i = 0; i < nameOfActiveThreads.length; i++) {
            server.broadcastSingleMessage("Your first card is: ", "[Server]", nameOfActiveThreads[i]);
            server.broadcastCard(CardDeck.getInstance().getTopCard(), nameOfActiveThreads[i]);
            index = 16 - CardDeck.getInstance().getCurrentTop() + 1;
            nameOfActiveThreads[i].setCard0(CardDeck.getInstance().getCurrentTopCard());
        }
    }

    /**
     * Initializes a new Round.
     *
     * @param roundThread the round thread
     * @throws IOException the io exception
     */
    public void round(int roundThread) throws IOException {
        threadsActiveInRound = server.selectablePlayerThreads();
        currentActivePlayer = queue.poll();

        server.serverLog("roundthread " + roundThread);
        server.serverLog("roundTest" + currentActivePlayer);

        currentActivePlayer.setIsOnTurn(true);
        //Handmaid: Check if the player has played in his/her last round a Handmaid - and reset the value
        if (currentActivePlayer.getPlayedHandmaid() == true) {
            currentActivePlayer.setPlayedHandmaid(false);
        }

        server.broadcastGameMessage("Next turn: " + currentActivePlayer.getPlayerName() + " is playing.",
                currentActivePlayer);
        server.broadcastSingleMessage("It's your turn!", "Server", currentActivePlayer);
        server.serverLog("It is your turn: " + currentActivePlayer.getPlayerName());
        int inWhatPlace;

        if (currentActivePlayer.getCard0() == null) {
            inWhatPlace= 0;
            currentActivePlayer.setCard0(CardDeck.getInstance().getTopCard());
        } else {
            inWhatPlace =1;
            currentActivePlayer.setCard1(CardDeck.getInstance().getTopCard());
        }
        server.broadcastSingleMessage("You drew (to your "+ inWhatPlace+ " Place) the card: ", "Server", currentActivePlayer);
        server.broadcastCard(CardDeck.getInstance().getCurrentTopCard(), currentActivePlayer);

        server.broadcastSingleMessage("Now choose a Card", "[Server]", currentActivePlayer);
        String printCard0 =
                currentActivePlayer.getCard0().getCardName() + " " + currentActivePlayer.getCard0().getCardValue() +
                        "\n " + currentActivePlayer.getCard0().getCardDescription();
        String printCard1 =
                currentActivePlayer.getCard1().getCardName() + " " + currentActivePlayer.getCard1().getCardValue() +
                        "\n " + currentActivePlayer.getCard1().getCardDescription();

        server.broadcastSingleMessage("This is your current hand: ", "[Server]", currentActivePlayer);
        //Card1
        String card1= "Card 1: " + printCard0;
        server.broadcastSingleMessage(card1, "[Server]", currentActivePlayer);


        //Card2
        String card2 = "Card 2: "+ printCard1;
        server.broadcastSingleMessage(card2, "[Server]", currentActivePlayer);


        //Check Countess + Prince or King -> Player must play the countess
        if ((currentActivePlayer.getCard0().getCardValue() == 7) ||
                (currentActivePlayer.getCard1().getCardValue() == 7)) {
            if (((currentActivePlayer.getCard0().getCardValue() == 5) ||
                    (currentActivePlayer.getCard1().getCardValue() == 5)) ||
                    ((currentActivePlayer.getCard0().getCardValue() == 6) ||
                            (currentActivePlayer.getCard1().getCardValue() == 6))) {
                server.broadcastSingleMessage(
                        "You have a Countess and a Prince/King on your Hand. You must play the Countess", "[Server]",
                        currentActivePlayer);
                if (currentActivePlayer.getCard0().getCardValue() == 7) {
                    currentActivePlayer.setActivePlayedCard(currentActivePlayer.getCard0());
                } else {
                    currentActivePlayer.setActivePlayedCard(currentActivePlayer.getCard1());
                }
                currentActivePlayer.getActivePlayedCard().cardFunction(server, currentActivePlayer, " ");
                playerFinishCardFunction(currentActivePlayer);
            } else {
                server.broadcastSingleMessage("To choose a card simply type '/game card1'  OR '/game card2' ", "[Server]", currentActivePlayer);
            }

        } else {
            server.broadcastSingleMessage("To choose a card simply type '/game card1'  OR '/game card2' ", "[Server]", currentActivePlayer);
        }


    }


    /**
     * Calls the card function of the chosen card.
     *
     * @param card        the card
     * @param roundThread the round thread
     * @param cardInput   the card input
     * @throws IOException the io exception
     */
    public void cardFunction(Card card, PlayerThread roundThread, String cardInput) throws IOException {
        card.cardFunction(server, roundThread, cardInput);
        //roundThread.addToDiscardPile(card);
    }

    /**
     * Initializes actions after a Player has finished their card function.
     *
     * @param cardSenderThread the card sender thread
     * @throws IOException the io exception
     */
    public void playerFinishCardFunction(PlayerThread cardSenderThread) throws IOException {
        discardPlayedCard();
        cardSenderThread.cleanHandCard();
        cardSenderThread.getActivePlayedCard().setCounter(0);
        cardSenderThread.setActivePlayedCard(null);
        cardSenderThread.setIsOnTurn(false);
        roundThread++;
        List <PlayerThread> queueArray = new ArrayList<>();
        for (PlayerThread item: queue) {
            if (item.isPlayerInRound()==true && (item != cardSenderThread)) {
                queueArray.add(item);
            }
        }
        for (PlayerThread item: queue) {
            queue.remove();
        }
        for (PlayerThread i : queueArray) {
            queue.add(i);
        }
        queue.add(cardSenderThread);

        startNextRound();

    }

    /**
     * Starts next round.
     *
     * @throws IOException the io exception
     */
    public void startNextRound() throws IOException {
        if (checkIfNewRoundCanStart()) {
            round(getRoundThread());
        } else {
            if(checkIfPrickingCardsIsNecessary());{ //CardDeck is empty and there a more than one player left.
                server.broadcastGameMessage("The card deck is empty! Prick your cards.", null);
                prickingCards();
                server.broadcastGameMessage("The player " + prickingCards().getPlayerName() + " won with highest card!", null);
            }
            server.serverLog("checkIfPrickingCardsIsNecessary" + checkIfPrickingCardsIsNecessary());
            spendTokenToWinner();
            endPreRound();
            if (!existWinnerOfCompleteGame(nameOfActiveThreads)) {
                preRoundStart();
                round(0);
            } else {
                endGame();
            }
        }
    }

    /**
     * Winner of round gets appointed a new token.
     */
    public void spendTokenToWinner() {
        PlayerThread winningPlayer = server.selectablePlayerThreads()[0];
        winningPlayer.setTokens(winningPlayer.getTokens() + 1);
    }

    /**
     * Checks if new round can start.
     * Conditions: more than 1 Player and Carddeck ist not empty
     *
     * @return the boolean
     */
    public boolean checkIfNewRoundCanStart() {
        return (server.selectablePlayerThreads().length > 1 && CardDeck.getInstance().getCurrentTop() < 15);

    }

    /**
     * Checks if a Player have enough Token to win the complete Game
     *
     * @param threadsOfActivePlayers the threads of active players
     * @return the boolean
     */
    public boolean existWinnerOfCompleteGame(PlayerThread[] threadsOfActivePlayers) {
        int necessaryTokensAmount = 7;

        if (threadsOfActivePlayers.length == 3) {
            necessaryTokensAmount = 5;
        } else if (threadsOfActivePlayers.length == 4) {
            necessaryTokensAmount = 4;
        }
        for (int i = 0; i < threadsOfActivePlayers.length; i++) {
            if (threadsOfActivePlayers[i].getTokens() == necessaryTokensAmount) {
                System.out.println(threadsOfActivePlayers[i].getTokens() == necessaryTokensAmount);
                return true;
            }

        }
        return false;
    }

    /**
     * Returns the playerThread of the player, who wins the complete Game
     *
     * @param threadsOfActivePlayers the threads of active players
     * @return the player thread
     */
    public PlayerThread threadWinnerCompletedGame(PlayerThread[] threadsOfActivePlayers) {
        int necessaryTokensAmount = 7;

        if (threadsOfActivePlayers.length == 3) {
            necessaryTokensAmount = 5;
        } else if (threadsOfActivePlayers.length == 4) {
            necessaryTokensAmount = 4;
        }
        for (int i = 0; i < threadsOfActivePlayers.length; i++) {
            if (threadsOfActivePlayers[i].getTokens() == necessaryTokensAmount) {
                System.out.println(threadsOfActivePlayers[i].getTokens() == necessaryTokensAmount);
                return threadsOfActivePlayers[i];
            }

        }
        return null;

    }

    /**
     * Check if pricking cards is necessary.
     * Performed when the deck is empty, but there are at least 2 players at round end.
     *
     * @return the boolean
     */
    public boolean checkIfPrickingCardsIsNecessary() {
        return (server.selectablePlayerThreads().length > 1 && CardDeck.getInstance().getCurrentTop() >= 15);
    }

    /**
     * Pricking cards: card values are compared to determine a winner for the round.
     *
     * @return the player thread
     */
    public PlayerThread prickingCards() {
        PlayerThread playerWithHighestCardValue = null;
        for (PlayerThread p: server.selectablePlayerThreads()) {
            if (playerWithHighestCardValue == null) {
                playerWithHighestCardValue = p;
            } else if (p.getCard0().getCardValue() > playerWithHighestCardValue.getCard0().getCardValue()) {
                playerWithHighestCardValue.setPlayerInRound(false);
                playerWithHighestCardValue = p;
            }
        }
        return playerWithHighestCardValue;
    }

    /**
     * Discards played card.
     */
    public void discardPlayedCard() {
        Card playedCard = currentActivePlayer.getActivePlayedCard();
        Card[] handCards = (currentActivePlayer.getCardArray());
        int whichHand = inWhichHandIsTheCard(playedCard);

        if (whichHand == 0) {
            currentActivePlayer.addToDiscardPile(currentActivePlayer.getCard0());
            currentActivePlayer.setCard0(null);

        } else if (whichHand == 1) {
            currentActivePlayer.addToDiscardPile(currentActivePlayer.getCard1());
            currentActivePlayer.setCard1(null);
        }
    }

    /**
     * Determines In which hand the card is in.
     *
     * @param card the card
     * @return the int
     */
    public int inWhichHandIsTheCard(Card card) {
        if (currentActivePlayer.getCard0().equals(card)) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Starts a game.
     *
     * @throws IOException the io exception
     */
    public void start() throws IOException {
        preGame();
        preRoundStart();
        round(0);
    }

    /**
     * Ends a game.
     */
    public void endGame() {
        server.broadcastGameMessage("The winner is: " + threadWinnerCompletedGame(nameOfActiveThreads).getPlayerName(),
                null);
        for (int i = 0; i < nameOfActiveThreads.length; i++) {
            nameOfActiveThreads[i].setPlayerInRound(false);
            nameOfActiveThreads[i].setGameHasStarted(false);
            nameOfActiveThreads[i].setTokens(0);
            nameOfActiveThreads[i].setJoinedGame(false);
            nameOfActiveThreads[i].setPlayerInRound(false);
        }
        server.setActiveGame(false);
    }

    /**
     * Ends pre-round.
     */
    public void endPreRound() {
        server.serverLog(server.getHighscore());
        server.broadcastGameMessage(
                "New Round and token won by  " + server.selectablePlayerThreads()[0].getPlayerName(), null);

        PlayerThread playerThreadWinner = server.selectablePlayerThreads()[0];
        playerThreadWinner.setWonLastRound(true);

        Random random = ThreadLocalRandom.current();
        for (int i = nameOfActiveThreads.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);

            PlayerThread a = nameOfActiveThreads[index];
            nameOfActiveThreads[index] = nameOfActiveThreads[i];
            nameOfActiveThreads[i] = a;
            String b = nameOfActivePlayers[index];
            nameOfActivePlayers[index] = nameOfActivePlayers[i];
            nameOfActivePlayers[i] = b;
        }

        int winnerIndex = 0;
        for (int i = 0; i < nameOfActiveThreads.length; i++) {
            if (nameOfActiveThreads[i].isWonLastRound() == true) {
                winnerIndex = i;
            }
        }

        PlayerThread temp = nameOfActiveThreads[0];
        nameOfActiveThreads[0] = playerThreadWinner;//nameOfActiveThreads[winnerIndex]
        nameOfActiveThreads[winnerIndex] = temp;

        String temp1 = nameOfActivePlayers[0];
        nameOfActivePlayers[0] = playerThreadWinner.getPlayerName();
        nameOfActivePlayers[winnerIndex] = temp1;

        server.setThreadsOfActivePlayers(nameOfActiveThreads);
        server.setNameOfActivePlayers(nameOfActivePlayers);
        playerThreadWinner.setWonLastRound(false);
    }





            /*
        first player draws (getTopCard)


        checks if (countess == true)
            if also King || Prince --> Discard Countess

        wait for player choosing card to discard... -> publicmsg for discarded Card // should also be documented and callable for allPlayers // discard Pile from Player is always public

        server checks effect and applies // suggestion maybe case switch for card effect application

        check if player is eliminated (PlayerCounter[before] == PlayerCounter[after] .. maybe?) if player is eliminated - set playerinRound (Client) false

        boolean playerInRound (Client) set true for all players after one player gets token

        next in line --> second player to join

             */

    /**
     * Get threads of players that are active in the round.
     *
     * @return the player thread [ ]
     */
    public PlayerThread[] getThreadsActiveInRound() {
        return threadsActiveInRound;
    }

    /**
     * Gets round thread.
     *
     * @return the round thread
     */
    public int getRoundThread() {
        return roundThread;
    }

    /**
     * Sets round thread.
     *
     * @param roundThread the round thread
     */
    public void setRoundThread(int roundThread) {
        this.roundThread = roundThread;
    }

    /**
     * Gets discarded card.
     *
     * @return the discarded card
     */
    public Card getDiscardedCard() {
        return discardedCard;
    }
}





    /*
    public void start() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientOnServerSocket.getInputStream()));
            OutputStream output = clientOnServerSocket.getOutputStream();
            writer = new PrintWriter(output, true);
            String messageToEveryone = "[Server:] You are now playing!";
            server.broadcastGameMessage(messageToEveryone, null);
            server.broadcastMessage("New Game has started!", null);

            for (int i = 0; i < nameOfActiveThreads.length; i++ ){
                nameOfActiveThreads[i].setPlayerInRound(true);
            }


            // Display player order

            server.serverLog(server.getPlayerOrder());
            server.serverLog(server.getHighscore());
            server.broadcastGameMessage(server.getPlayerOrder(), null);
            //TokenCheck
            while (server.existWinnerOfCompleteGame(this.nameOfActiveThreads) == false) {

                System.out.println("New Round!");
                CardDeck.getInstance().shuffle();
                Card discardedDeck = CardDeck.getInstance().getTopCard();         //oberste Karte wird weggelegt.
                int index = 16 - (CardDeck.getInstance().getCurrentTop() + 1);
                System.out.println("I have  " + index + " Cards left on my Pile");

                if (nameOfActiveThreads.length ==2) {
                    for (int i = 0; i < discardPileForTwoPlayers.length; i++) {
                        discardPileForTwoPlayers[i] = CardDeck.getInstance().getTopCard();
                    }
                }

                //Next Step - Deal cards to each player


                for (int i = 0; i < nameOfActiveThreads.length; i++) {
                    server.broadcastSingleMessage("Your first card is: ", "[Server]",nameOfActiveThreads[i]);
                    server.broadcastCard(CardDeck.getInstance().getTopCard(), nameOfActivePlayers[i], nameOfActiveThreads[i]);
                    index = 16 - CardDeck.getInstance().getCurrentTop() + 1;
                    nameOfActiveThreads[i].setCard0(CardDeck.getInstance().getCurrentTopCard());
                    System.out.println("I have  " + index + " Cards left on my Pile");
                }



                //Rundenstart

                //Shuffeln der PlayerOrder!!!!!!!!!!!!
                int roundThread = 0;
                do {

                    threadsActiveInRound = server.selectablePlayerThreads();
                    if (roundThread >= threadsActiveInRound.length){
                      roundThread =0;
                    }
                    server.serverLog("It is your turn: " + threadsActiveInRound[roundThread].getPlayerName());

                    if (threadsActiveInRound[roundThread].getCard0() == null){
                        threadsActiveInRound[roundThread].setCard0(CardDeck.getInstance().getTopCard());
                        server.broadcastSingleMessage("You drew the card: ", "[Server]",threadsActiveInRound[roundThread]);
                        server.broadcastCard(CardDeck.getInstance().getCurrentTopCard(), "", threadsActiveInRound[roundThread]);
                    } else {
                        threadsActiveInRound[roundThread].setCard1(CardDeck.getInstance().getTopCard());
                        server.broadcastSingleMessage("You drew the card: ", "[Server]",threadsActiveInRound[roundThread]);
                        server.broadcastCard(CardDeck.getInstance().getCurrentTopCard(), "", threadsActiveInRound[roundThread]);
                    }

                    server.broadcastSingleMessage("Now choose a Card", "[Server]",threadsActiveInRound[roundThread]);
                    String printCard0 = threadsActiveInRound[roundThread].getCard0().getCardName() + " " + threadsActiveInRound[roundThread].getCard0().getCardValue() + "\n " + threadsActiveInRound[roundThread].getCard0().getCardDescription();
                    String printCard1 = threadsActiveInRound[roundThread].getCard1().getCardName() + " " + threadsActiveInRound[roundThread].getCard1().getCardValue() + "\n " + threadsActiveInRound[roundThread].getCard1().getCardDescription();

                    server.broadcastSingleMessage("This is your current hand: ", "[Server]",threadsActiveInRound[roundThread]);
                    server.broadcastSingleMessage("Card 1", "[Server]",threadsActiveInRound[roundThread]);

                    server.broadcastSingleMessage(printCard0, "[Server]",threadsActiveInRound[roundThread]);


                    server.broadcastSingleMessage("Card 2", "[Server]",threadsActiveInRound[roundThread]);

                    server.broadcastSingleMessage(printCard1, "[Server]",threadsActiveInRound[roundThread]);
                    server.broadcastSingleMessage("To choose a card simply type '/card1'  OR '/card2' ", "[Server]",threadsActiveInRound[roundThread]);
                    server.setGameIsWaiting(true);
                    server.serverLog("TEST0");





                    //threadsActiveInRound[roundThread].addToDiscardPile(wrapperCard.getCard());
                    roundThread++;
                    server.serverLog("TEST2");

                } while (threadsActiveInRound.length > 1 && CardDeck.getInstance().getCurrentTopCard() != null); // && should be ||

                threadsActiveInRound[0].setTokens(threadsActiveInRound[0].getTokens()+1);




                first player draws (getTopCard)


                checks if (countess == true)
                 if also King || Prince --> Discard Countess

                wait for player choosing card to discard... -> publicmsg for discarded Card // should also be documented and callable for allPlayers // discard Pile from Player is always public

                server checks effect and applies // suggestion maybe case switch for card effect application

                check if player is eliminated (PlayerCounter[before] == PlayerCounter[after] .. maybe?) if player is eliminated - set playerinRound (Client) false

                boolean playerInRound (Client) set true for all players after one player gets token

                next in line --> second player to join





            }


        } catch (IOException ex) {
            System.out.println("GameStart Error: " + ex.getMessage());
        }


    }
    */


//private String[] nameOfActivePlayers;
//private PlayerThread[] nameOfActiveThreads;






