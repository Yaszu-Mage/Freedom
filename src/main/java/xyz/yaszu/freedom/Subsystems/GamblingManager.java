package xyz.yaszu.freedom.Subsystems;

import com.arakelian.core.feature.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.yaszu.freedom.Items.Gambling.Card;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static xyz.yaszu.freedom.Util.Util.*;

/**
 * a system for GAMBLING YAY
 */
public class GamblingManager implements Listener {
    public static HashMap<Location, Gambling> gamblingLocations = new HashMap<>();
    @EventHandler
    public void InventoryInteract(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof GamblingInventory gamblingInventory) {
            event.setCancelled(true);
        }
    }

    /**
     * inventory of gambling menu
     */
    public static class GamblingInventory implements InventoryHolder {
        private final Inventory inventory;
        public void setInventory(Location location) {
            try {
                for (int x = 0; x < inventory.getSize(); x++) {
                    inventory.setItem(x,emptyItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)));
                }
            } catch (Exception ignored) {}

            //set all inventory data
            //let's format
            if (gamblingLocations.getOrDefault(location,null) != null) {
                Gambling gambling = gamblingLocations.get(location);
            } else {
                Gambling gambling = new Gambling(location, inventory);
                gamblingLocations.put(location, gambling);

            }

        }

        /**
         * creates inventory for gambking menu
         *
         * @param inventory inventory
         */
        public GamblingInventory(Inventory inventory) {
            this.inventory = Bukkit.createInventory(this, 27, dess("Gambling Inventory"));
        }
        @Override
        public Inventory getInventory() {
            return inventory;
        }

        /**
         * selection icon
         * @return item stack for item
         */
        public ItemStack selectIcon() {
            ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
            ItemMeta meta = stack.getItemMeta();
            meta.displayName(dess("Select Game"));
            meta.setItemModel(NamespacedKey.minecraft("selectIcon"));
            stack.setItemMeta(meta);
            return stack;
        }

        /**
         * generate random card
         *
         * @param name name
         * @return item stack of card
         */
        public ItemStack randomStandardCard(String name) {
            String suit = "";
            switch (random.nextInt(0,4)) {
                case 0 -> suit = "S";
                case 1 -> suit = "C";
                case 2 -> suit = "H";
                case 3 -> suit = "D";
            }
            Card card = new Card("card"+suit+random.nextInt(1,14), Gambling.Deck.DeckTypes.Standard);
            ItemStack cardItem = card.getCardItem();
            ItemMeta meta = cardItem.getItemMeta();
            meta.displayName(dess(name));
            cardItem.setItemMeta(meta);
            return cardItem;
        }

        /**
         * --unused--
         * @param name --unused--
         * @return --unused--
         */
        public ItemStack randomUnoCard(String name) {
            String suit = "";
            switch (random.nextInt(0,4)) {
                case 0 -> suit = "R";
                case 1 -> suit = "W";
                case 2 -> suit = "B";
                case 3 -> suit = "G";
                case 4 -> suit = "Y";
            }
            Card card = new Card("card"+suit+random.nextInt(1,10), Gambling.Deck.DeckTypes.UNO);
            ItemStack cardItem = card.getCardItem();
            ItemMeta meta = cardItem.getItemMeta();
            meta.displayName(dess(name));
            cardItem.setItemMeta(meta);
            return cardItem;
        }


        /**
         * updates inventory of menu
         * @param state menu state
         * @param hand card hand
         * @param extraData extra data
         */
        public void updateInventory(MenuState state, @Nullable List<Card> hand, @Nullable Object extraData) {
            switch (state) {
                case MainMenu -> {
                    try {
                        for (int x = 0; x < inventory.getSize(); x++) {
                            inventory.setItem(x,emptyItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)));
                        }
                    } catch (Exception ignored) {}
                    inventory.setItem(13,selectIcon());
                }
                case GameSelect -> {
                    try {
                        for (int x = 0; x < inventory.getSize(); x++) {
                            inventory.setItem(x,emptyItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)));
                        }

                    } catch (Exception ignored) {}
                    inventory.setItem(13,randomStandardCard("Poker"));
                }
                case Poker -> {
                    //on the bottom row set cards, slowly reveal "extra data"
                    List<Card> revealed = (List<Card>) extraData;
                    for (int x = 0; x < revealed.size(); x++) {
                        inventory.setItem(1+x,revealed.get(x).getCardItem());
                    }
                    inventory.setItem(20,hand.get(0).getCardItem());
                    inventory.setItem(21,hand.get(1).getCardItem());
                    inventory.setItem(22,hand.get(2).getCardItem());
                    inventory.setItem(23,hand.get(3).getCardItem());
                    inventory.setItem(24,hand.get(4).getCardItem());
                }
            }
        }

        /**
         * menu states
         */
        public enum MenuState {
            MainMenu,
            GameSelect,
            Poker,
            Uno
        }
        public static void cashOut(Player player, int balance) {
            CurrencyManager.addCurrency(balance,player);
        }
    }

    public static HashMap<Location, Game> activeGames = new HashMap<>();
    public class Uno implements Game {
        boolean isActive = false;
        byte handSize = 14;
        public HashMap<UUID, GamblingInventory> displays = new HashMap<>();
        public List<Card> revealed = new ArrayList<>();
        @Override
        public boolean isGameActive() {
            return isActive;
        }


        public Uno(Location location,Player player) {
            activeGames.put(location,this);
            GamblingInventory inventory = new GamblingInventory(Bukkit.createInventory(null, 27, dess("Poker Game")));
            displays.put(player.getUniqueId(),inventory);
        }
        @Override
        public void startGame() {
            isActive = true;
            deck.DeckType = Gambling.Deck.DeckTypes.UNO;
            defaultStart();
        }

        @Override
        public void endGame() {
            deck.reset();

        }

        @Override
        public void updateGame() {

        }

        @Override
        public void loop() {

        }

        @Override
        public void resetGame() {

        }
    }

    public class Poker implements Game {
        byte handSize = 5;
        public HashMap<UUID, GamblingInventory> displays = new HashMap<>();
        public List<Card> revealed = new ArrayList<>();
        public Poker(Location location,Player player) {
            activeGames.put(location,this);
            GamblingInventory inventory = new GamblingInventory(Bukkit.createInventory(null, 27, dess("Poker Game")));
            displays.put(player.getUniqueId(),inventory);
        }

        /**
         * boolean checking for active games
         * @return active / inactive
         */
        @Override
        public boolean isGameActive() {
            return isActive;
        }
        boolean isActive = false;

        /**
         * starts the game
         */
        @Override
        public void startGame() {
            isActive = true;
            defaultStart();

        }

        /**
         * ends game
         */
        @Override
        public void endGame() {
            isActive = false;
            //calculate winner
            UUID winner = null;
            Gambling.HandType bestHand = null;
            for (UUID uuid : playerHands.keySet()) {
                List<Card> hand = playerHands.get(uuid);
                Gambling.HandType handType = Gambling.HandType.HighCard.getHandType(hand);
                if (bestHand == null || handType.value > bestHand.value) {
                    bestHand = handType;
                    winner = uuid;
                }
            }
            //give winner money
            AtomicInteger balance = new AtomicInteger(playerBalance.getOrDefault(winner, 0));
            if (balance.get() >= 0) {
                playerBalance.forEach((uuid, molah) -> {
                    balance.addAndGet(molah);
                });
            }

            if (winner != null) {
                //get sumofall bets placed
                playerBalance.put(winner, balance.get());
            }
            //return to mainMenu
            displays.forEach((uuid, inventory) -> {
                inventory.updateInventory(GamblingInventory.MenuState.MainMenu,null,null);
            });
        }

        /**
         * updates game
         */
        @Override
        public void updateGame() {
            if (revealed.size() < handSize) {
                revealed.add(deck.getRandomCard());
                displays.forEach((uuid, inventory) -> {;
                    List<Card> hand = playerHands.get(uuid);
                    inventory.updateInventory(GamblingInventory.MenuState.Poker, hand,revealed);
                });
            } else {
                //calculate
                endGame();
            }
        }

        @Override
        public void loop() {

        }

        @Override
        public void resetGame() {
            deck.reset();
            revealed.clear();
            playerHands.clear();
            playerBalance.clear();
            players.clear();
            playersInQueue.clear();
        }
    }

    /**
     * game interface
     */
    public interface Game {
        public HashMap<UUID, Integer> playerBalance = new HashMap<>();
        public HashMap<UUID, List<Card>> playerHands = new HashMap<>();
        public Gambling.Deck deck = new Gambling.Deck();
        boolean isGameActive();
        ArrayList<Player> players = new ArrayList<>();
        ArrayList<Player> playersInQueue = new ArrayList<>();
        public void startGame();
        public void endGame();
        public void updateGame();
        public void loop();
        public void resetGame();
        byte handSize = 5;
        public default void defaultStart() {
            deck.reset();
            players.addAll(playersInQueue);
            playersInQueue.clear();
            for (Player player : players) {
                List<Card> hand = deck.createHand(handSize);
                playerHands.put(player.getUniqueId(), hand);
            }
        }
        public default void addPlayer(Player player) {
            if (isGameActive()) {
                playersInQueue.add(player);
            } else {
                players.add(player);
            }
        }
    }

    /**
     * gambling data
     */
    public static class Gambling {
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
            final int value;

            HandType(int value) {
                this.value = value;
            }
            public HandType getHandType(List<Card> cards) {
                //order it by ranking Value
                cards.sort(Comparator.comparingInt(o -> o.rank));
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


        public static class Deck {
            int DeckSize = 52;
            public List<Card> cards = new ArrayList<>();
            public enum DeckTypes {
                Standard,
                UNO
            }
            DeckTypes DeckType = DeckTypes.Standard;
            public void reset() {
                cards.clear();
                switch (DeckType) {
                   case Standard -> {
                       for (int x = 0; x < 14; x++) {
                           cards.add(new Card("card"+"S"+x,DeckType));
                       }
                       for (int x = 0; x < 14; x++) {
                           cards.add(new Card("card"+"D"+x,DeckType));
                       }
                       for (int x = 0; x < 14; x++) {
                           cards.add(new Card("card"+"H"+x,DeckType));
                       }
                       for (int x = 0; x < 14; x++) {
                           cards.add(new Card("card"+"C"+x,DeckType));
                       }
                   }
                   case UNO -> {
                        for (int x = 0; x < 10; x++) {
                            cards.add(new Card("card"+"R"+x,DeckType));
                        }
                        for (int x = 0; x < 4; x++) {
                            for (int z = 0; z < 4; z++) {
                                cards.add(new Card("card"+"W"+x,DeckType));
                            }
                            for (int z = 0; z < 4; z++) {
                                cards.add(new Card("card"+"W"+x,DeckType));
                            }
                        }

                        for (int x = 0; x < 10; x++) {
                            cards.add(new Card("card"+"B"+x,DeckType));
                        }
                   }
                }
            }

            /**
             * gets random card
             * @return card gotten
             */
            public Card getRandomCard() {
                int rand = random.nextInt(0,cards.size());
                Card card = cards.get(rand);
                cards.remove(rand);
                return card;
            }

            /**
             * --unused--
             * @param hand --unused--
             * @param amount --unused--
             * @return --unused--
             */
            public List<Card> drawCard(List<Card> hand, int amount) {
                List<Card> cards = new ArrayList<>();
                for (int x = 0; x < amount; x++) {
                    cards.add(getRandomCard());
                }
                hand.addAll(cards);
                return cards;
            }

            /**
             * creates a hand
             * @param handSize size of created hand
             * @return hand created
             */
            public List<Card> createHand(byte handSize) {
                List<Card> hand = new ArrayList<>();
                for (int x = 0; x < handSize; x++) {
                    if (cards.isEmpty()) {
                        return null;
                    }
                    int cardIndex = random.nextInt(0,cards.size());
                    hand.add(cards.get(cardIndex));
                    cards.remove(cardIndex);
                }
                return hand;
            }
        }

    }

}
