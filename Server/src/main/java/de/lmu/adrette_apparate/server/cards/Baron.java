package de.lmu.adrette_apparate.server.cards;

import de.lmu.adrette_apparate.server.PlayerThread;
import de.lmu.adrette_apparate.server.ServerApplication;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The type Baron.
 */
public class Baron extends Card {

    private final int cardValue = 3;
    private final String cardName = "Baron";
    private final String cardDescription = "When you discard the Baron, choose another player still in the round. " +
            "You and that player secretly compare your hands. The player with the lower number is knocked " +
            "out of the round. In case of a tie, nothing happens.";
    private int baronCounter;


    /**
     * Instantiates a new Baron.
     */
    public Baron() {
        super(3, "Baron", "When you discard the Baron, choose another player still in the round. " +
                "You and that player secretly compare your hands. The player with the lower number is knocked " +
                "out of the round. In case of a tie, nothing happens.");
        baronCounter = 0;
    }

    /**
     * Gets the baron counter.
     * @return
     */
    private int getBaronCounter() {
        return baronCounter;
    }

    /**
     * Sets the baron counter.
     *
     * @param baronCounter
     */
    private void setBaronCounter(int baronCounter) {
        this.baronCounter = baronCounter;
    }

    /**
     * Card function of the card baron.
     *
     * @param server              the server
     * @param cardSender          the card sender
     * @param cardInputFromPlayer the card input from player
     * @throws IOException
     */
    @Override
    public void cardFunction(ServerApplication server, PlayerThread cardSender,
                             String cardInputFromPlayer) throws IOException {
        server.broadcastSingleMessage("You now play the Baron function!", "Baron", cardSender);
        server.serverLog("Baron function");
        server.broadcastGameMessage(cardSender.getPlayerName() + " played Baron ", cardSender);
        ArrayList selectablePlayers = (ArrayList) server.selectablePlayerNames().clone();
        selectablePlayers.remove(cardSender.getPlayerName());
        String selectablePlayerNamesString = String.join("\n", selectablePlayers);
        if (selectablePlayers.isEmpty()) {
            this.setCounter(2);
            baronCounter = 2;
        }


        if (getBaronCounter() == 0) {
            server.broadcastSingleMessage("You can choose these players \n " + selectablePlayerNamesString, "Baron", cardSender);
            server.broadcastSingleMessage("Choose a Player to compare your card with, e.g. 'Sina'", "Baron", cardSender);
            server.broadcastSingleMessage("Choose a Player, e.g. 'Sina'", "Baron", cardSender);

            setBaronCounter(1);
        } else if (getBaronCounter() == 1) {
            String[] input = cardInputFromPlayer.split(" ");
            String chosenPlayer = input[0];

            if (selectablePlayers.contains(chosenPlayer)) {
                server.broadcastGameMessage(cardSender.getPlayerName() + " choose " + chosenPlayer, cardSender);
                PlayerThread chosenPersonThread = server.getRecipientThread(server.getRecipientIndex(chosenPlayer));
                chosenPersonThread.cleanHandCard();
                int valueOfChosen = chosenPersonThread.getCard0().getCardValue();
                int valueOfPlayer;
                if (cardSender.getCard0().getCardValue() == 3) {
                    valueOfPlayer = cardSender.getCard1().getCardValue();
                } else {
                    valueOfPlayer = cardSender.getCard0().getCardValue();
                }
                if (valueOfChosen > valueOfPlayer) {
                    cardSender.setPlayerInRound(false);
                    server.broadcastSingleMessage("You are out of the round. Sorry!", "server", cardSender);
                    server.broadcastGameMessage(chosenPersonThread.getPlayerName() + " wins in comparison with " + cardSender , null);
                    this.setCounter(3);
                    setBaronCounter(0);

                } else if (valueOfChosen < valueOfPlayer) {
                    chosenPersonThread.setPlayerInRound(false);
                    server.broadcastSingleMessage("You are out of the round. Sorry!", "server", chosenPersonThread);
                    server.broadcastGameMessage(cardSender.getPlayerName() + " wins in comparison with " + chosenPlayer, null);
                    this.setCounter(3);
                    setBaronCounter(0);

                } else if (valueOfChosen == valueOfPlayer) {
                    server.broadcastSingleMessage("Nothing happened! You both have the same Card", "server", chosenPersonThread);
                    server.broadcastSingleMessage("Nothing happened! You both have the same Card", "server", cardSender);
                    server.broadcastGameMessage(cardSender.getPlayerName() + " and " + chosenPlayer + " have the same Card. Nothing Happened!", null);
                    this.setCounter(3);
                    setBaronCounter(0);
                }


            } else {
                server.broadcastSingleMessage("Invalid name-input! Try again.", "Server", cardSender);
                server.broadcastSingleMessage("You can choose these players \n " + selectablePlayerNamesString,
                        "Priest", cardSender);
            }
        }

        if (this.getCounter() == 2) {
            server.broadcastSingleMessage("No player available", "Baron", cardSender);
            server.broadcastGameMessage("Baron was played without any effect!", cardSender);
            setCounter(0);
        }






       /* //choose opponent and output the chosen player to all
        String name = chooseOtherPlayer(cardSender, server);
        server.broadcastGameMessage("I played Baron and I choose " + name + " as my opponent.", cardSender);

        PlayerThread chosenPlayer = server.getRecipientThread(server.getRecipientIndex(name));

        //cleans hand of both players -> sets the cards held in hand to first position of Array
        cardSender.cleanHandCard();
        chosenPlayer.cleanHandCard();

        //saves the card value of both players
        int valueSender = (cardSender.getCard0().getClass() == Baron.class) ? cardSender.getCard1().getCardValue() : cardSender.getCard0().getCardValue();
        int valueOther = chosenPlayer.getCard0().getCardValue();

        //checks if card values of players is equal (players have same card in hand) -> nothing happens
        if (valueSender == valueOther) {
            server.broadcastGameMessage("It's a tie!", null);
            return;
        }

        //finds out who loses and saves value in eliminatedPlayer -> Loser no longer in round
        PlayerThread eliminatedPlayer = (valueSender > valueOther) ? chosenPlayer : cardSender;
        eliminatedPlayer.setPlayerInRound(false);
        server.broadcastGameMessage("Player" + eliminatedPlayer.getPlayerName() + "has been eliminated!", null);*/
    }
}
