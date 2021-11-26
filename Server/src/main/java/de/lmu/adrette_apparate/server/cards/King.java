package de.lmu.adrette_apparate.server.cards;

import de.lmu.adrette_apparate.server.PlayerThread;
import de.lmu.adrette_apparate.server.ServerApplication;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The card type King.
 */
public class King extends Card {

    private int kingCounter;

    /**
     * Instantiates a new King.
     */
    public King() {
        super(6, "King", "When you discard the King, trade the card in your hand with the card held by " +
                "another player of your choice. You cannot trade with a player who is out of the round.");
        kingCounter = 0;
    }

    /**
     * Gets the king counter.
     *
     * @return kind counter
     */
    private int getKingCounter() {
        return this.kingCounter;
    }

    /**
     * Sets the kind counter.
     *
     * @param kingCounter
     */
    private void setKingCounter(int kingCounter) {
        this.kingCounter = kingCounter;
    }

    /**
     * Card function of the card king.
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
            kingCounter = 2;
        }

        if (getKingCounter() == 0) {
            server.broadcastSingleMessage("You're playing the function of the King", "King", cardSender);
            server.serverLog("Test: King function");
            server.broadcastGameMessage(cardSender.getPlayerName() + " played King ", cardSender);


            server.broadcastSingleMessage("You can choose these players \n " + selectablePlayerNamesString, "King", cardSender);
            server.broadcastSingleMessage("Choose a Player, e.g. 'Sina'", "King", cardSender);

            setKingCounter(1);

        } else if (getKingCounter() == 1) {

            String[] input = cardInputFromPlayer.split(" ");
            String chosenPlayer = input[0];

            //check if chosenPlayer is a valid input
            if (selectablePlayers.contains(chosenPlayer)) {
                server.broadcastGameMessage(cardSender.getPlayerName() + " chooses " + chosenPlayer, cardSender);
                PlayerThread chosenPersonThread = server.getRecipientThread(server.getRecipientIndex(chosenPlayer));

                //cleans hand of both players -> sets the cards held in hand to first position of Array
                cardSender.cleanHandCard();
                chosenPersonThread.cleanHandCard();

                //switches card with opponent
                if (cardSender.getCard0().getCardValue() == 6) {
                    Card tempCard = cardSender.getCard0();
                    cardSender.setCard0(cardSender.getCard1());
                    cardSender.setCard1(tempCard);
                }
                Card senderCard = cardSender.getCard0();
                cardSender.setCard0(chosenPersonThread.getCard0());
                chosenPersonThread.setCard0(senderCard);
                server.broadcastGameMessage(cardSender.getPlayerName() + " switched cards with " + chosenPlayer, null);
                server.broadcastSingleMessage("You got the card: "+ cardSender.getCard0().getCardName(),"Server", cardSender);
                server.broadcastSingleMessage("You got the card: "+ chosenPersonThread.getCard0().getCardName(),"Server", chosenPersonThread);
                this.setCounter(3);
                setKingCounter(0);
            } else {
                server.broadcastSingleMessage("Invalid name-input! Try again.", "Server", cardSender);
                server.broadcastSingleMessage("You can choose these players \n " + selectablePlayerNamesString, "King",
                        cardSender);

            }
        }
        if (this.getCounter() == 2) {
            server.broadcastSingleMessage("No player available", "King", cardSender);
            server.broadcastGameMessage("King was played without any effect!", cardSender);
            setCounter(0);
        }
    }
}