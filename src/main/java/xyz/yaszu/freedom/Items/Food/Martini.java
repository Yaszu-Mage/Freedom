package xyz.yaszu.freedom.Items.Food;

import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

public class Martini extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack itemStack = constructColoredBottle(List.of(FreedomKeys.itemId()),List.of("martini"), Color.LIME);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Util.dess("<shadow:#000000FF><b><yellow>Martini</yellow></b>"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"beer");
        itemStack.setItemMeta(meta);
        return itemStack;
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
        return CustomItemType.DRINK;
    }
}
