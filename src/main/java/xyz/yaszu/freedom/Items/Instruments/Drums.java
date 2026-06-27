package xyz.yaszu.freedom.Items.Instruments;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.keygen;

public class Drums implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack item = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Util.dess("<shadow:#000000FF><b><yellow>Drums</yellow></b>"));
        meta.setItemModel(NamespacedKey.minecraft("drums"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    @Override
    public Recipe recipe() {
        ShapedRecipe recipe = new ShapedRecipe(keygen("drums"), item());
        recipe.shape(
                "GSG",
                "WSW",
                " I "
        );
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('W', RecipeChoice.itemType(ItemType.OAK_PLANKS,ItemType.SPRUCE_PLANKS,ItemType.BIRCH_PLANKS,ItemType.JUNGLE_PLANKS,ItemType.ACACIA_PLANKS,ItemType.DARK_OAK_PLANKS));
        recipe.setIngredient('I', Material.IRON_INGOT);
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }
}
