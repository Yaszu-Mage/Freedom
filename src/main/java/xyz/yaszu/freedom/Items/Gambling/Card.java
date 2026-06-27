package xyz.yaszu.freedom.Items.Gambling;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Subsystems.GamblingManager;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

public class Card {
    String cardID = "cardS1";
    public static enum Suit {
        Spades, Clubs, Hearts, Diamonds,Red,Green,Blue,Yellow, Wild
    }
    public int rank = 0;
    public Suit suit = Suit.Spades;
    public GamblingManager.Gambling.Deck.DeckTypes deckType = GamblingManager.Gambling.Deck.DeckTypes.Standard;

    public Card(String card, GamblingManager.Gambling.Deck.DeckTypes deckType) {
        cardID = card;
    }
    public ItemStack getCardItem() {
        ItemStack card = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta cardMeta = card.getItemMeta();
        // DECODE HERE
        //S1
        String color = "";
        switch (deckType) {
            case Standard -> {
                switch (cardID.charAt(5)) {
                    case 'S' -> {
                        //Spades
                        suit = Suit.Spades;
                        color = "<Blue>";
                    }
                    case 'C' -> {
                        color = "<Blue>";
                        suit = Suit.Clubs;
                    }
                    case 'H' -> {
                        color = "<Red>";
                        suit = Suit.Hearts;
                    }
                    case 'D' -> {
                        color = "<Red>";
                        suit = Suit.Diamonds;
                    }
                }
                rank = Integer.parseInt(String.valueOf(cardID.charAt(6)));
                String rankString = "";
                // we need more for kings aces jacks, queens
                if (rank >= 11) {
                    switch (rank) {
                        case 11 -> rankString = "Jack";
                        case 12 -> rankString = "Queen";
                        case 13 -> rankString = "King";
                        case 14 -> rankString = "Ace";
                    }
                    cardMeta.displayName(/* decode card id HERE*/dess(rankString + " of " + color + suit.toString()));
                } else {
                    cardMeta.displayName(/* decode card id HERE*/dess(rank + " of " + color + suit.toString()));
                }
            }
            case UNO -> {
                switch (cardID.charAt(5)) {
                    case 'R' -> {
                        //Spades
                        suit = Suit.Red;
                        color = "<Red>";
                    }
                    case 'G' -> {
                        color = "<Green>";
                        suit = Suit.Green;
                    }
                    case 'B' -> {
                        color = "<Blue>";
                        suit = Suit.Blue;
                    }
                    case 'Y' -> {
                        color = "<Yellow>";
                        suit = Suit.Yellow;
                    }
                    case 'W' -> {
                        color = "<rainbow>";
                        suit = Suit.Wild;
                    }
                }
                try {
                    rank = Integer.parseInt(cardID.charAt(6) + String.valueOf(cardID.charAt(7)));
                } catch (Exception e) {
                    rank = Integer.parseInt(String.valueOf(cardID.charAt(6)));
                }

                String rankString = "";
                if (suit == Suit.Wild) {
                    switch (rank) {
                        case 1 -> rankString = "Wild";
                        case 2 -> rankString = "Draw Four";
                        case 3 -> rankString = "Skip";
                        case 4 -> rankString = "Reverse";
                        case 5 -> rankString = "Draw Two";
                    }
                    cardMeta.displayName(/* decode card id HERE*/dess(rankString + " " + color + suit.toString()));
                } else {
                    cardMeta.displayName(dess(suit.toString() + rank));
                }
            }
        }
        cardMeta.getPersistentDataContainer().set(keygen(cardID), PersistentDataType.STRING, cardID);
        cardMeta.setItemModel(NamespacedKey.minecraft(cardID));
        card.setItemMeta(cardMeta);
        return card;
    }
}
