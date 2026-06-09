package xyz.yaszu.freedom.Items.Instruments;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.Util;

public class Guitar extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack item = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Util.dess("<shadow:#000000FF><b><yellow>Guitar</yellow></b>"));
        meta.setItemModel(NamespacedKey.minecraft("guitar"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    @Override
    public Recipe recipe() {
        ShapedRecipe recipe = new ShapedRecipe(keygen("guitar"), item());
        recipe.shape(
                "SS ",
                "ST ",
                "  T"
        );
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('T', Material.STICK);
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }
}
