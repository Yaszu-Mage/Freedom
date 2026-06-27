package xyz.yaszu.freedom.Items.Default;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.keygen;

public class leather implements BaseItem {
    @Override
    public ItemStack item() {
        return ItemStack.of(Material.LEATHER);
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    @Override
    public Recipe recipe() {
        return new FurnaceRecipe(keygen("leather"),item(),Material.ROTTEN_FLESH,0.5f,200);
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }
}
