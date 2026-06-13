package xyz.yaszu.freedom.Items.Tools;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Hammer extends Util implements BaseItem, Listener {
    @Override
    public ItemStack item() {
        ItemStack item = ItemStack.of(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b><i><gradient:#1956C0:#65caf6>Hammer</gradient></i></b>"));
        meta.setItemModel(org.bukkit.NamespacedKey.minecraft("hammer"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), org.bukkit.persistence.PersistentDataType.STRING,"hammer");
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

    @Override
    public CustomItemType getType() {
        return null;
    }
}
