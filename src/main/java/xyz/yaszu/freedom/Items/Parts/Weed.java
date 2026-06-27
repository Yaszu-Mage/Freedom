package xyz.yaszu.freedom.Items.Parts;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Subsystems.AlcoholManager;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

public class Weed implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.FERN);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b><yellow>Weed</yellow></b>"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"weed");
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        AlcoholManager.addAlcohol(player,0,0,4);
    }

    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(keygen("weed"), item());
        recipe.addIngredient(ItemStack.of(Material.FERN));
        recipe.addIngredient(ItemStack.of(Material.SUGAR));
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }
}
