package de.lmu.adrette_apparate.server.cards;

import de.lmu.adrette_apparate.server.PlayerThread;
import de.lmu.adrette_apparate.server.ServerApplication;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The card type Guard.
 */
public class Guard extends Card {

    private final int cardValue = 1;
    private final String cardName = "Guard";
    private final String cardDescription = "When you discard the Guard, choose a player and name a number " +
            "(other than 1). If that player has that number in their hand, that player is " +
            "knocked out of the round. If all other players still in the round cannot be " +
            "chosen (eg. due to Handmaid), this card is discarded without effect.";
    private int guardCounter;

    /**
     * Instantiates a new Guard.
     */
    public Guard() {
        super(1, "Guard", "When you discard the Guard, choose a player and name a number " +
                "(other than 1). If that player has that number in their hand, that player is " +
                "knocked out of the round. If all other players still in the round cannot be " +
                "chosen (eg. due to Handmaid), this card is discarded without effect.");
        guardCounter = 0;

    }

    /**
     * Gets the guard counter.
     *
     * @return guard counter
     */
    private int getGuardCounter() {
        return guardCounter;
    }

    /**
     * Sets the guard counter.
     *
     * @param guardCounter
     */
    private void setGuardCounter(int guardCounter) {
        this.guardCounter = guardCounter;
    }

    /**
     * Card function of the card guard.
     *
     * @param server              the server
     * @param cardSender          the card sender
     * @param cardInputFromPlayer the card input from player
     * @throws IOException
     */
    @Override
    public void cardFunction(ServerApplication server, PlayerThread cardSender,
                             String cardInputFromPlayer) throws IOException { //game message mit übergeben?

        ArrayList selectablePlayers = (ArrayList) server.selectablePlayerNames().clone();
        selectablePlayers.remove(cardSender.getPlayerName());
        String selectablePlayerNamesString = String.join("\n", selectablePlayers);
        if (selectablePlayers.isEmpty()) {
            this.setCounter(2);
            guardCounter = 2;
        }

        if (getGuardCounter() == 0) {
            server.broadcastSingleMessage("You are now play the function of the Guard", "Guard", cardSender);
            server.serverLog("Guard function");
            server.broadcastGameMessage(cardSender.getPlayerName() + " played Guard ", cardSender);

            //Ausgabe: Welche aktiven Spieler stehen zur Auswahl

            server.broadcastSingleMessage("You can choose these players \n " + selectablePlayerNamesString, "Guard",
                    cardSender);
            server.broadcastSingleMessage(
                    "Choose a Player and the value of your guessed card (2, 3, 4, 5, 6, 7, 8), e.g. 'Sina 2'", "Guard",
                    cardSender);
            setGuardCounter(1);

        } else if (getGuardCounter() == 1) {


            String[] input = cardInputFromPlayer.split(" ");
            String chosenPlayer = input[0];
            //need to check
            int invalidInput = 0;
            int chosenCardValue;
            if (input.length < 1) {
                chosenCardValue = invalidInput;
            } else {
                try{
                    chosenCardValue = Integer.parseInt(input[1]);
                }catch (NumberFormatException ex) {
                    chosenCardValue = invalidInput;
                }
            }
            if ((chosenCardValue >= 2 && chosenCardValue <= 8) && server.selectablePlayerNames().contains(chosenPlayer)) {
                // nun Input da: Namen und einen Kartenwert
                server.broadcastGameMessage(
                        cardSender.getPlayerName() + " choose " + input[0] + " and card-value " + input[1], cardSender);
                PlayerThread chosenPersonThread = server.getRecipientThread(server.getRecipientIndex(chosenPlayer));
                server.serverLog(chosenPersonThread.getCard0().getCardName());
                chosenPersonThread.cleanHandCard();
                if ((chosenPersonThread.getCard0().getCardValue() == chosenCardValue)) {
                    chosenPersonThread.setPlayerInRound(false);
                    server.broadcastSingleMessage("You are out of the round. Sorry!", "server", chosenPersonThread);
                    server.broadcastGameMessage(cardSender.getPlayerName() + " good guess!", null);
                    server.broadcastGameMessage(chosenPlayer + " you have lost. You are eliminated from the round!",
                            null);

                } else {
                    //Spieler fliegt nicht raus, nichts passiert
                    server.broadcastGameMessage(cardSender.getPlayerName() + " good try, but wrong guess.", null);
                }
                this.setCounter(3);
                setGuardCounter(0);

            } else {
                server.broadcastSingleMessage("Invalid name-input! Try again.", "Server", cardSender);
                server.broadcastSingleMessage("You can choose these players \n " + selectablePlayerNamesString, "Priest", cardSender);
                server.broadcastSingleMessage("Choose a Player and the value of your guessed card (2, 3, 4, 5, 6, 7, 8), e.g. 'Sina 2'", "Guard", cardSender);
            }
        }

        else if (this.getCounter() == 2){
            server.broadcastSingleMessage("No player available", "Guard", cardSender);
            server.broadcastGameMessage("Guard was played without any effect!", cardSender);
            setCounter(0);
        }

    }
}