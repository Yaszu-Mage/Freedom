package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Items.Gambling.Card;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GamblingManager extends Util implements Listener {
    public HashMap<Location, Gambling> gamblingLocations = new HashMap<>();
    @EventHandler
    public void InventoryInteract(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof GamblingInventory gamblingInventory) {
            event.setCancelled(true);
        }
    }

    public class GamblingInventory implements InventoryHolder {

        private final Inventory inventory;


        public void setInventory(Location location) {

            inventory.forEach(item -> item = emptyItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)));
            //set all inventory data
            //let's format
            if (gamblingLocations.getOrDefault(location,null) != null) {

            } else {
                Gambling gambling = new Gambling(location, inventory);
                gamblingLocations.put(location, gambling);

            }
        }

        public GamblingInventory(Inventory inventory) {
            this.inventory = Bukkit.createInventory(this, 27, dess("Gambling Inventory"));
        }
        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }


    public class Gambling {


        public Gambling(Location location,Inventory inventory) {
            deck.reset();
        }

        Location position;
        public HashMap<UUID,Inventory> playerInventories = new HashMap<>();
        List<UUID> playersInGame = new ArrayList<>();
        HashMap<UUID, Integer> playerBalance = new HashMap<>();
        public HashMap<UUID, List<Card>> playerCards = new HashMap<>();
        public Deck deck = new Deck();
        public boolean GamblingActive = false;

        public enum HandType {
            Straight,
            FullHouse,
            Pair,
            TwoPair,
            ThreeOfAKind,
            FourOfAKind,
            Flush,
            StraightFlush,
            HighCard,
            RoyalFlush;
            public static HandType getHandType(List<Card> cards) {

                return HandType.HighCard;
            }
            public boolean isFlush(List<Card> cards) {
                Card.Suit suit = cards.get(0).suit;
                for (Card card : cards) {
                    if (card.suit != suit) return false;
                }
                return true;
            }
            public boolean isStraight(List<Card> cards) {
                return false;
            }
        }


        public class Deck {
            int DeckSize = 52;
            public List<Card> cards = new ArrayList<>();
            public void reset() {
                cards.clear();
                for (int x = 0; x < 14; x++) {
                    cards.add(new Card("card"+"S"+x));
                }
                for (int x = 0; x < 14; x++) {
                    cards.add(new Card("card"+"D"+x));
                }
                for (int x = 0; x < 14; x++) {
                    cards.add(new Card("card"+"H"+x));
                }
                for (int x = 0; x < 14; x++) {
                    cards.add(new Card("card"+"C"+x));
                }
            }
            public List<Card> createHand(byte handSize) {
                List<Card> hand = new ArrayList<>();
                for (int x = 0; x < handSize; x++) {
                    hand.add(cards.get(random.nextInt(0,cards.size())));
                }
                return hand;
            }
        }

    }

}
