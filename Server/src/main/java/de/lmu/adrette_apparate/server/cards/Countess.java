package de.lmu.adrette_apparate.server.cards;

import de.lmu.adrette_apparate.server.PlayerThread;
import de.lmu.adrette_apparate.server.ServerApplication;

import java.io.IOException;

/**
 * The card type Countess.
 */
public class Countess extends Card {

    /**
     * Instantiates a new Countess.
     */
    public Countess() {
        super(7, "Countess", "Unlike other cards, which take effect when discarded, the text on the " +
                "Countess applies while she is in your hand. In fact, the only time it doesn’t apply is when " +
                "you discard her. If you ever have the Countess and either the King or Prince in your hand, " +
                "you must discard the Countess. You do not have to reveal the other card in your hand. " +
                "Of course, you can also discard the Countess even if you do not have a royal family member " +
                "in your hand. The Countess likes to play mind games....");
    }

    /**
     * Card function of the card countess.
     *
     * @param server              the server
     * @param cardSender          the card sender
     * @param cardInputFromPlayer the card input from player
     * @throws IOException
     */
    @Override
    public void cardFunction(ServerApplication server, PlayerThread cardSender,
                             String cardInputFromPlayer) throws IOException {
        server.broadcastSingleMessage("You have played the Countess", "Countess", cardSender);
        server.serverLog("Countess function");
        server.broadcastGameMessage(cardSender.getPlayerName() + " played Countess ", cardSender);
    }

}