package de.lmu.adrette_apparate.server.cards;

import de.lmu.adrette_apparate.server.PlayerThread;
import de.lmu.adrette_apparate.server.ServerApplication;

import java.io.IOException;

/**
 * The card type Handmaid.
 */
public class Handmaid extends Card {

    /**
     * Instantiates a new Handmaid.
     */
    public Handmaid() {
        super(4, "Handmaid", "When you discard the Handmaid, you are immune to the effects of other players’ " +
                "cards until the start of your next turn. If all players other than the player whose turn it is " +
                "are protected by the Handmaid, the player must choose him- or herself for a card’s effects, " +
                "if possible.");

    }

    /**
     * Card function of the card handmaid.
     *
     * @param server              the server
     * @param cardSender          the card sender
     * @param cardInputFromPlayer the card input from player
     * @throws IOException
     */
    @Override
    public void cardFunction(ServerApplication server, PlayerThread cardSender,
                             String cardInputFromPlayer) throws IOException {

        cardSender.setPlayedHandmaid(true);
        server.broadcastSingleMessage("You have played the Handmaid. Until your next turn you are protected",
                "Handmaid", cardSender);
        server.serverLog("Countess function");
        server.serverLog("check flag: " + cardSender.getPlayedHandmaid());
        server.broadcastGameMessage(
                cardSender.getPlayerName() + " played Handmaid. Until his/her next turn the player ist protected ",
                cardSender);

    }
}