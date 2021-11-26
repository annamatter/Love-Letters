package de.lmu.adrette_apparate.server;

import de.lmu.adrette_apparate.server.cards.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The type Card deck.
 */
public class CardDeck {
    private static final CardDeck DECK = new CardDeck();
    private final Card[] cardDeck = new Card[16];
    private int currentTop = -1;

    /**
     * Instantiates CardDeck.
     */
    private CardDeck() {
        this.populate();
    }

    /**
     * Gets instance of card deck.
     *
     * @return the instance
     */
    public static CardDeck getInstance() {
        return DECK;
    }

    /**
     * Populates the card deck.
     */
    public void populate() {
        cardDeck[0] = new Princess();
        cardDeck[1] = new Countess();
        cardDeck[2] = new King();
        cardDeck[3] = new Prince();
        cardDeck[4] = new Prince();
        cardDeck[5] = new Handmaid();
        cardDeck[6] = new Handmaid();
        cardDeck[7] = new Baron();
        cardDeck[8] = new Baron();
        cardDeck[9] = new Priest();
        cardDeck[10] = new Priest();
        cardDeck[11] = new Guard();
        cardDeck[12] = new Guard();
        cardDeck[13] = new Guard();
        cardDeck[14] = new Guard();
        cardDeck[15] = new Guard();
    }

    /**
     * Shuffles the card deck.
     */
    public void shuffle() {
        currentTop = -1;

        List<Card> l = Arrays.asList(cardDeck);
        Collections.shuffle(l);
        l.toArray(cardDeck);
    }

    /**
     * Gets current top.
     *
     * @return the current top
     */
    public int getCurrentTop() {
        return currentTop;
    }

    /**
     * Gets current top card.
     *
     * @return the current top card
     */
    public Card getCurrentTopCard() {
        return cardDeck[currentTop];
    }

    /**
     * Gets top card.
     *
     * @return the top card
     */
    public Card getTopCard() {
        currentTop++;
        if (currentTop <= 15) {
            return cardDeck[currentTop];
        }
        return null;
    }

}
