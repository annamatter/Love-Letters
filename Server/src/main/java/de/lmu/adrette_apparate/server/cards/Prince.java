package de.lmu.adrette_apparate.server.cards;

import de.lmu.adrette_apparate.server.CardDeck;
import de.lmu.adrette_apparate.server.PlayerThread;
import de.lmu.adrette_apparate.server.ServerApplication;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The card type Prince.
 */
public class Prince extends Card {

    private int princeCounter;

    /**
     * Instantiates a new Prince.
     */
    public Prince() {
        super(5, "Prince", "When you discard Prince Arnaud, choose one player still in the round " +
                "(including yourself). That player discards his or her hand (but doesn’t apply its effect, " +
                "unless it is the Princess, see page 8) and draws a new one. If the deck is empty and the player " +
                "cannot draw a card, that player draws the card that was removed at the start of the round. " +
                "If all other players are protected by the Handmaid, you must choose yourself.");
        princeCounter = 0;

    }

    /**
     * Gets the prince counter.
     *
     * @return the counter of prince.
     */
    private int getPrinceCounter() {
        return this.princeCounter;
    }

    /**
     * Sets the prince counter.
     *
     * @param princeCounter
     */
    private void setPrinceCounter(int princeCounter) {
        this.princeCounter = princeCounter;
    }

    /**
     * Card function of the card prince.
     *
     * @param server              the server
     * @param cardSender          the card sender
     * @param cardInputFromPlayer the card input from player
     * @throws IOException
     */
    @Override
    public void cardFunction(ServerApplication server, PlayerThread cardSender,
                             String cardInputFromPlayer) throws IOException {
        //get list of all players (including yourself)
        ArrayList selectablePlayers = (ArrayList) server.selectablePlayerNames().clone();
        String selectablePlayerNamesString = String.join("\n", selectablePlayers);
        if (selectablePlayers.isEmpty()) {
            this.setCounter(2);
            princeCounter = 2;
        }

        if (getPrinceCounter() == 0) {
            server.broadcastSingleMessage("You're playing the function of the Prince", "Prince", cardSender);
            server.serverLog("Test: Prince function");
            server.broadcastGameMessage(cardSender.getPlayerName() + " played Prince ", cardSender);


            server.broadcastSingleMessage("You can choose these players \n " + selectablePlayerNamesString, "Prince", cardSender);
            server.broadcastSingleMessage("Choose a Player, including yourself! e.g. '"+ cardSender.getPlayerName() + "'", "Prince", cardSender);

            setPrinceCounter(1);

        } else if (getPrinceCounter() == 1) {

            String[] input = cardInputFromPlayer.split(" ");
            String chosenPlayer = input[0];

            //check if chosenPlayer is a valid input
            if (selectablePlayers.contains(chosenPlayer)) {
                server.broadcastGameMessage(
                        cardSender.getPlayerName() + " chooses " + chosenPlayer + " to take up a new card.",
                        cardSender);
                PlayerThread chosenPersonThread = server.getRecipientThread(server.getRecipientIndex(chosenPlayer));

                //cleans hand of chosen players -> sets the cards held in hand to first position of Array
                chosenPersonThread.cleanHandCard();
                if(cardSender.getCard0().getCardValue() == 5){
                    Card tempCard = cardSender.getCard0();
                    cardSender.setCard0(cardSender.getCard1());
                    cardSender.setCard1(tempCard);
                }

                //discards card onto pile, if it is not the princess
                if (chosenPersonThread.getCard0().getCardValue() != 8) {
                    //adds card to discard pile of player
                    chosenPersonThread.addToDiscardPile(cardSender.getCard0());
                    //get new card
                    if (CardDeck.getInstance().getCurrentTop() > 15 ) {
                        chosenPersonThread.setCard0(server.getGame().getDiscardedCard());
                        server.broadcastSingleMessage("You got the first discarded card of the deck: " + chosenPersonThread.getCard0().getCardName(), "Server", chosenPersonThread);
                    } else {
                        chosenPersonThread.setCard0(CardDeck.getInstance().getTopCard());
                        server.broadcastSingleMessage("You drew the card: " + chosenPersonThread.getCard0().getCardName(), "Server", chosenPersonThread);
                    }
                } else {
                    chosenPersonThread.setPlayerInRound(false);
                }
                this.setCounter(3);
                setPrinceCounter(0);
            } else {
                server.broadcastSingleMessage("Invalid name-input! Try again.", "Server", cardSender);
                server.broadcastSingleMessage("You can choose these players \n " + selectablePlayerNamesString,
                        "Prince", cardSender);

            }

        }
        if(this.getCounter() == 2){
            server.broadcastSingleMessage("No player available", "Prince", cardSender);
            server.broadcastGameMessage("Prince was played without any effect!", cardSender);
            setCounter(0);
        }
    }
}