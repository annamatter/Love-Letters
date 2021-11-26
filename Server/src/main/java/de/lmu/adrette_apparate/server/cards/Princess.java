package de.lmu.adrette_apparate.server.cards;

import de.lmu.adrette_apparate.server.PlayerThread;
import de.lmu.adrette_apparate.server.ServerApplication;

import java.io.IOException;

/**
 * The type Princess.
 */
public class Princess extends Card {


    /**
     * Instantiates a new Princess.
     */
    public Princess() {
        super(8, "Princess", "If you discard the Princess—no matter how or why—she has tossed your letter " +
                "into the fire. You are immediately knocked out of the round. If the Princess was discarded by " +
                "a card effect, any remaining effects of that card do not apply (you do not draw a card from the " +
                "Prince, for example). Effects tied to being knocked out the round still apply, however.");
    }

    /**
     * Card function of the card princess.
     *
     * @param server              the server
     * @param cardSender          the card sender
     * @param cardInputFromPlayer the card input from player
     * @throws IOException
     */
    @Override
    public void cardFunction(ServerApplication server, PlayerThread cardSender,
                             String cardInputFromPlayer) throws IOException {
        server.broadcastSingleMessage("I played Princess and am kicked out of the round.", "Princess", cardSender);
        server.broadcastGameMessage(cardSender.getPlayerName() + " played Princess and was kicked out of the round.",
                cardSender);
        cardSender.setPlayerInRound(false);
    }
}
