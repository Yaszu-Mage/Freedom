package xyz.yaszu.freedom.Items.Parts;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Burger extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack item  = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(dess("<yellow> Biggie </yellow> Burger"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"burger");
        meta.setItemModel(NamespacedKey.minecraft("burger"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(FreedomKeys.itemId(),item());
        recipe.addIngredient(ItemStack.of(Material.BEEF));
        recipe.addIngredient(ItemStack.of(Material.BREAD));
        recipe.addIngredient(ItemStack.of(Material.GOLD_INGOT));
        recipe.addIngredient(ItemStack.of(Material.CLOCK));

        return recipe;
    }
}
