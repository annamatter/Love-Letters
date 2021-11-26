package de.lmu.adrette_apparate.server.cards;

import de.lmu.adrette_apparate.server.PlayerThread;
import de.lmu.adrette_apparate.server.ServerApplication;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The abstract type Card.
 */
public abstract class Card {


    private final int cardValue;
    private final String cardName;
    private final String cardDescription;
    private int counter;

    /**
     * Instantiates a new Card.
     *
     * @param cardValue       the card value
     * @param cardName        the card name
     * @param cardDescription the card description
     */
    public Card(int cardValue, String cardName, String cardDescription) {
        this.cardValue = cardValue;
        this.cardName = cardName;
        this.cardDescription = cardDescription;
        this.counter = 0;
    }

    /**
     * Choose other player and return its name.
     *
     * @param cardSender the card sender
     * @param server     the server
     * @return the name of the chosen other player
     * @throws IOException the io exception
     */
    static String chooseOtherPlayer(PlayerThread cardSender, ServerApplication server) throws IOException {
        //Get the list of all players, make a copy and then remove player that is currently playing (should only be able to choose other players)
        ArrayList otherPlayers = (ArrayList) server.selectablePlayerNames().clone();
        otherPlayers.remove(cardSender.getPlayerName());

        String name;
        do {
            server.broadcastSingleMessage("These are selectable players: " + String.join(", ", otherPlayers),
                    "[Server]", cardSender);
            server.broadcastSingleMessage("Choose player for comparison", "Server", cardSender);
            String chosenPlayer = cardSender.readLine();
            name = chosenPlayer;
        } while (!otherPlayers.contains(name));

        return name;
    }

    /**
     * Get card value int.
     *
     * @return the int of the card value
     */
    public int getCardValue() {
        return cardValue;
    }

    /**
     * Get card name string.
     *
     * @return the string
     */
    public String getCardName() {

        return cardName;

    }

    /**
     * Gets card description.
     *
     * @return the card description
     */
    public String getCardDescription() {
        return cardDescription;
    }

    /**
     * Gets counter.
     *
     * @return the counter
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Sets counter.
     *
     * @param counter the counter
     */
    public void setCounter(int counter) {
        this.counter = counter;
    }

    /**
     * Card function.
     *
     * @param server              the server
     * @param cardSender          the card sender
     * @param cardInputFromPlayer the card input from player
     * @throws IOException the io exception
     */
    public abstract void cardFunction(ServerApplication server, PlayerThread cardSender,
                                      String cardInputFromPlayer) throws IOException;
}
