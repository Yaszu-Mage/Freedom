package xyz.yaszu.freedom.Subsystems;

import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Alchemy.SpellCompiler.getItemValue;

public class CurrencyManager extends Util {

    public static void addCurrency(int amount, Player player) {
        player.getPersistentDataContainer().set(keygen("currency"), PersistentDataType.INTEGER,amount + getCurrency(player));

    }

    public static void removeCurrency(int amount, Player player) {
        player.getPersistentDataContainer().set(keygen("currency"), PersistentDataType.INTEGER,getCurrency(player) - amount);
        if (getCurrency(player) < 0) {
            player.getPersistentDataContainer().set(keygen("currency"), PersistentDataType.INTEGER,0);
        }
    }
    public static int getCurrency(Player player) {
        if (player.getPersistentDataContainer().has(keygen("currency"), PersistentDataType.INTEGER)) {
            try {
                return player.getPersistentDataContainer().get(keygen("currency"), PersistentDataType.INTEGER);
            } catch (Exception ignored) {}

        }
        player.getPersistentDataContainer().set(keygen("currency"), PersistentDataType.INTEGER, 0);
        return 0;

    }
    public static void setCurrency(int amount, Player player) {
        player.getPersistentDataContainer().set(keygen("currency"), PersistentDataType.INTEGER,amount);

    }
    public static void resetCurrency(Player player) {
        player.getPersistentDataContainer().remove(keygen("currency"));
        getCurrency(player);
    }
    public static enum SellType {
        Inventory,
        Hand
    }
    public static void sell(Player player, SellType type) {
        switch (type) {
            case Inventory -> {
                //sell all items in inventory excluding hotbar
                for (int iteration = 0; iteration < player.getInventory().getSize(); iteration++) {
                    if (iteration >= 9) {


                    if (player.getInventory().getItem(iteration) != null) {
                        try {
                            player.getInventory().removeItem(player.getInventory().getItem(iteration));
                            int value = getItemValue(player.getInventory().getItem(iteration).getType());
                            addCurrency(value, player);
                        } catch (Exception ignored) {}
                    }
                }
                }

            }
            case Hand -> {
                //sell item in hand
                if (player.getInventory().getItemInMainHand() != null) {
                    try {
                        player.getInventory().removeItem(player.getInventory().getItemInMainHand());
                        int value = getItemValue(player.getInventory().getItemInMainHand().getType());
                        addCurrency(value, player);
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
