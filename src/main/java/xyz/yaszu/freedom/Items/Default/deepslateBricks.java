package xyz.yaszu.freedom.Items.Default;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.keygen;

public class deepslateBricks implements BaseItem {
    @Override
    public ItemStack item() {
        return ItemStack.of(Material.DEEPSLATE_BRICKS);
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    @Override
    public Recipe recipe() {
        ShapedRecipe recipe = new ShapedRecipe(keygen("deepslate_bricks"), item());
        recipe.shape("DD ","DD ","   ");
        recipe.setIngredient('D', Material.DEEPSLATE);
        return recipe;
    }



    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }
}
