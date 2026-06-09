package xyz.yaszu.freedom.Items.Default;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.Util;

public class endRod extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        return ItemStack.of(Material.END_ROD);
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(keygen("end_rod"), item());
        recipe.addIngredient(ItemStack.of(Material.BLAZE_ROD));
        recipe.addIngredient(ItemStack.of(Material.ENDER_PEARL));
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }
}
