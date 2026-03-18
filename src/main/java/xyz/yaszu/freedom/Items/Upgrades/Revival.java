package xyz.yaszu.freedom.Items.Upgrades;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Util.Util;

public class Revival extends Util implements BaseItem {

    @Override
    public ItemStack item() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(keygen("item_id"), PersistentDataType.STRING,"revivalstone");
        meta.setItemModel(NamespacedKey.minecraft("revivalstone"));
        meta.displayName(dess("<shadow:#000000FF><b><Blue>Revival Stone</Blue>"));
        meta.setRarity(ItemRarity.EPIC);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player) {

    }



    @Override
    public Recipe recipe() {
        return null;
    }
}
