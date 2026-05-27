package xyz.yaszu.freedom.Items.Gambling;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Util.Util;

import java.util.Random;

public class Card extends Util {
    String cardID = "cardS1";
    public static enum Suit {
        Spades, Clubs, Hearts, Diamonds
    }
    public int rank = 0;
    public Suit suit = Suit.Spades;

    public Card(String card) {
        cardID = card;
    }
    public ItemStack getCardItem() {
        ItemStack card = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta cardMeta = card.getItemMeta();
        // DECODE HERE
        //S1
        String color = "";
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
        // we need more for kings aces jacks, queens
        String rankString = "";
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
        cardMeta.getPersistentDataContainer().set(keygen(cardID), PersistentDataType.STRING, cardID);
        cardMeta.setItemModel(NamespacedKey.minecraft(cardID));
        card.setItemMeta(cardMeta);
        return card;
    }
}
