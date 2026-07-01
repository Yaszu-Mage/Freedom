package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Alchemy.SpellCompiler.getItemValue;
import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

/**
 * a system of currency that allows for easy transfer.
 * the currency is stored on the player and can be changed through transfer.
 * transfer of currency works through commands.
 */
public class CurrencyManager {

    /**
     * adds currency to a player
     *
     * @param amount amount of currency added
     * @param player player to add to
     */
    public static void addCurrency(int amount, Player player) {
        Freedom.get_plugin().getLogger().info("ADDING " + amount);
        player.getPersistentDataContainer().set(keygen("currency"), PersistentDataType.INTEGER,amount + getCurrency(player));

    }

    /**
     * removes currency from a player
     *
     * @param amount amount of currency removed
     * @param player player to remove from
     */
    public static void removeCurrency(int amount, Player player) {
        player.getPersistentDataContainer().set(keygen("currency"), PersistentDataType.INTEGER,getCurrency(player) - amount);
        if (getCurrency(player) < 0) {
            player.getPersistentDataContainer().set(keygen("currency"), PersistentDataType.INTEGER,0);
        }
    }

    /**
     * gets the currency of a player
     *
     * @param player player checked
     * @return amount of currency owned
     */
    public static int getCurrency(Player player) {
        if (player.getPersistentDataContainer().has(keygen("currency"), PersistentDataType.INTEGER)) {
            try {
                return player.getPersistentDataContainer().get(keygen("currency"), PersistentDataType.INTEGER);
            } catch (Exception ignored) {
                Freedom.get_plugin().getLogger().info("ERR " + ignored);
            }

        }
        player.getPersistentDataContainer().set(keygen("currency"), PersistentDataType.INTEGER, 0);
        return 0;

    }

    /**
     * sets a players currency
     *
     * @param amount amount set to
     * @param player player set
     */
    public static void setCurrency(int amount, Player player) {
        player.getPersistentDataContainer().set(keygen("currency"), PersistentDataType.INTEGER,amount);

    }

    /**
     * --unused--
     *
     * @param player --unused--
     */
    public static void resetCurrency(Player player) {
        player.getPersistentDataContainer().remove(keygen("currency"));
        getCurrency(player);
    }

    /**
     * weather selling from inventory or hand
     */
    public static enum SellType {
        Inventory,
        Hand
    }

    /**
     * transfer money from player 1 to player 2
     * @param p1 player 1 / sender
     * @param p2 player 2 / sender
     * @param value amount sent
     */
    public static void pay(Player p1, Player p2, int value) {
        if (getCurrency(p1) - value >= 0) {
            addCurrency(value,p2);
            p1.sendMessage(dess("PAID " + p2.getName() + " " + value));
            p2.sendMessage(dess("RECEIVED " + value + " from " + p1.getName()));
            removeCurrency(value,p1);
        }
    }

    /**
     * sells item
     * @param player player selling
     * @param type sell type between inventory and hand
     */
    public static void sell(Player player, SellType type) {
        switch (type) {
            case Inventory -> {
                //sell all items in inventory excluding hotbar
                for (int iteration = 0; iteration < player.getInventory().getSize(); iteration++) {
                    if (iteration >= 9) {


                    if (player.getInventory().getItem(iteration) != null) {
                        try {
                            int value = getItemValue(player.getInventory().getItem(iteration).getType());
                            value = value * player.getInventory().getItem(iteration).getAmount();
                            addCurrency(value, player);
                            player.getInventory().setItem(iteration, ItemStack.of(Material.AIR));
                        } catch (Exception ignored) {}
                    }
                }
                }

            }
            case Hand -> {
                //sell item in hand
                if (player.getInventory().getItemInMainHand() != null) {
                    try {

                        int value = getItemValue(player.getInventory().getItemInMainHand().getType());
                        value = value * player.getInventory().getItemInMainHand().getAmount();
                        player.getInventory().setItemInMainHand(ItemStack.of(Material.AIR));
                        addCurrency(value, player);

                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
