package de.lmu.adrette_apparate.server.cards;

import de.lmu.adrette_apparate.server.PlayerThread;
import de.lmu.adrette_apparate.server.ServerApplication;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The card type Priest.
 */
public class Priest extends Card {

    private int priestCounter;

    /**
     * Instantiates a new Priest.
     */
    public Priest() {
        super(2, "Priest", "When you discard the Priest, you can look at another player’s " +
                "hand. Do not reveal the hand to any other players.");
        priestCounter = 0;

    }

    /**
     * Gets the priest counter.
     *
     * @return the priest counter.
     */
    private int getPriestCounter() {
        return this.priestCounter;
    }

    /**
     * Sets the priest counter.
     *
     * @param priestCounter
     */
    private void setPriestCounter(int priestCounter) {
        this.priestCounter = priestCounter;
    }

    /**
     * Card function of the card priest.
     *
     * @param server              the server
     * @param cardSender          the card sender
     * @param cardInputFromPlayer the card input from player
     * @throws IOException
     */
    @Override
    public void cardFunction(ServerApplication server, PlayerThread cardSender,
                             String cardInputFromPlayer) throws IOException {
        //choose opponent and output the chosen player to all

        ArrayList selectablePlayers = (ArrayList) server.selectablePlayerNames().clone();
        selectablePlayers.remove(cardSender.getPlayerName());
        String selectablePlayerNamesString = String.join("\n", selectablePlayers);
        if (selectablePlayers.isEmpty()) {
            this.setCounter(2);
            priestCounter = 2;
        }

        if (getPriestCounter() == 0) {
            server.broadcastSingleMessage("You're playing the function of the Priest", "Priest", cardSender);
            server.serverLog("Test: Priest function");
            server.broadcastGameMessage(cardSender.getPlayerName() + " played Priest ", cardSender);

            server.broadcastSingleMessage("You can choose these players \n " + selectablePlayerNamesString, "Priest",
                    cardSender);
            server.broadcastSingleMessage("Choose a Player to look at his/her hand cards, e.g. 'Sina'", "Priest", cardSender);


            setPriestCounter(1);

        } else if (getPriestCounter() == 1) {

            String[] input = cardInputFromPlayer.split(" ");
            String chosenPlayer = input[0];

            //check if chosenPlayer is a valid input
            if (selectablePlayers.contains(chosenPlayer)) {
                server.broadcastGameMessage(cardSender.getPlayerName() + " choose " + chosenPlayer, cardSender);
                PlayerThread chosenPersonThread = server.getRecipientThread(server.getRecipientIndex(chosenPlayer));
                chosenPersonThread.cleanHandCard();
                String revealedHand =
                        chosenPlayer + " had " + chosenPersonThread.getCard0().getCardName() + " with value " +
                                chosenPersonThread.getCard0().getCardValue();
                //not revealing to others using single message
                server.broadcastSingleMessage(revealedHand, "Server", cardSender);
                //server.getGame().playerFinishCardFunction(cardSender);
                this.setCounter(3);
                setPriestCounter(0);
            } else {
                server.broadcastSingleMessage("Invalid name-input! Try again.", "Server", cardSender);
                server.broadcastSingleMessage("You can choose these players \n " + selectablePlayerNamesString, "Priest", cardSender);

            }
        }

    if(this.getCounter() == 2){
        server.broadcastSingleMessage("No player available", "Priest", cardSender);
        server.broadcastGameMessage("Priest was played without any effect!", cardSender);
        this.setCounter(0);
    }


        // to do: In extra Methode packen für alle Karten (Game Klasse?)
        // folgende Sachen zurücksetzen
        // private Card activePlayedCard = null;
        // private boolean isOnTurn = false;
        // whoIsOnTurn (Game) erhöhen, abfrage ob if (game.getRoundThread() >= game.getThreadsActiveInRound().length){game.setRoundThread(0);}
        // discard card?


    }
}