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
            Straight(7),
            FullHouse(6),
            Pair(2),
            TwoPair(3),
            ThreeOfAKind(5),
            FourOfAKind(6),
            Flush(4),
            StraightFlush(8),
            HighCard(1),
            RoyalFlush(9);
            int value;

            HandType(int value) {
                this.value = value;
            }
            public HandType getHandType(List<Card> cards) {
                //order it by ranking Value
                cards.sort((o1, o2) -> o1.rank - o2.rank);
                if (isRoyalFlush(cards)) return HandType.RoyalFlush;
                if (isStraightFlush(cards)) return HandType.StraightFlush;
                if (isFullHouse(cards)) return HandType.FullHouse;
                if (isFlush(cards)) return HandType.Flush;
                if (isStraight(cards)) return HandType.Straight;
                if (isFourOfAKind(cards)) return HandType.FourOfAKind;
                if (isThreeOfAKind(cards)) return HandType.ThreeOfAKind;
                if (isTwoPair(cards)) return HandType.TwoPair;
                if (isPair(cards)) return HandType.Pair;
                return HandType.HighCard;
            }
            public boolean isFlush(List<Card> cards) {
                Card.Suit suit = cards.get(0).suit;
                for (Card card : cards) {
                    if (card.suit != suit) return false;
                }
                return true;
            }
            public boolean isStraightFlush(List<Card> cards) {
                return isFlush(cards) && isStraight(cards);
            }

            public boolean isRoyalFlush(List<Card> cards) {
                return isStraightFlush(cards) && cards.getFirst().rank == 14;
            }
            public boolean isFourOfAKind(List<Card> cards) {
                return cards.stream().distinct().count() == 4;
            }
            public boolean isThreeOfAKind(List<Card> cards) {
                return cards.stream().distinct().count() == 3;
            }
            public boolean isTwoPair(List<Card> cards) {
                return cards.stream().distinct().count() == 2 && cards.stream().filter(card -> cards.stream().filter(card2 -> card2.rank == card.rank).count() == 2).count() == 2;
            }
            public boolean isPair(List<Card> cards) {
                return cards.stream().distinct().count() == 2;
            }
            public boolean isFullHouse(List<Card> cards) {
                return isPair(cards) && isThreeOfAKind(cards);
            }
            public boolean isStraight(List<Card> cards) {
                //get distance between rank of cards and if average > 1 then not straight
                int average = 0;
                for (int x = 0; x < cards.size()-1; x++) {
                    average += cards.get(x).rank;
                }
                average /= cards.size();
                return average <= 1;
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
