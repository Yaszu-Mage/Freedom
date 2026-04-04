package xyz.yaszu.freedom.Items.Parts;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.GUI.BaseItems;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class SpellFocus implements Listener {


    public static class Orb extends Util implements BaseItem {

        @Override
        public ItemStack item() {
            ItemStack item  = ItemStack.of(Material.RECOVERY_COMPASS);
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"orb");
            meta.setItemModel(NamespacedKey.minecraft("orb"));
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

        }

        @Override
        public Recipe recipe() {
            return null;
        }
    }

    public static class Staff extends Util implements BaseItem {

        @Override
        public ItemStack item() {
            ItemStack item  = ItemStack.of(Material.RECOVERY_COMPASS);
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"staff");
            meta.setItemModel(NamespacedKey.minecraft("staff"));
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

        }

        @Override
        public Recipe recipe() {
            return null;
        }
    }
}
