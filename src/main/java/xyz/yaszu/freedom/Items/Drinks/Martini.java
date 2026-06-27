package xyz.yaszu.freedom.Items.Drinks;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Items.Parts.DrugJuice;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

import static xyz.yaszu.freedom.Util.Util.constructColoredBottle;

public class Martini implements BaseItem,BaseDrink {
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

    @Override
    public ItemStack result() {
        return item();
    }

    @Override
    public ItemStack ingredient() {
        return new DrugJuice().item();
    }

    @Override
    public int inbetweenBrewTime() {
        return 40;
    }

    @Override
    public ItemStack stir() {
        return ItemStack.of(Material.SWEET_BERRIES);
    }
}
