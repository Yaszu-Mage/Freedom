package xyz.yaszu.freedom.Blocks.Silly;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Duck extends Util implements BaseBlock, BaseItem {
    @Override
    public ItemStack block() {
        ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("Ducky"));
        meta.getPersistentDataContainer().set(keygen("customBlock"), PersistentDataType.STRING,"duck");
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(),PersistentDataType.STRING,"duck");
        meta.setItemModel(NamespacedKey.minecraft("duck"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public CollisionSize collisionSize() {
        return CollisionSize.Teeny;
    }

    @Override
    public Behavior behavior() {
        return Behavior.Building;
    }

    @Override
    public int toolValue() {
        return 1;
    }

    @Override
    public boolean waterNeeded() {
        return false;
    }

    @Override
    public double scale() {
        return 1.4;
    }

    @Override
    public Object placeSound() {
        return "custom.ducky";
    }

    @Override
    public ItemStack item() {
        return block();
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        //none
    }

    @Override
    public Recipe recipe() {
        ShapedRecipe recipe = new ShapedRecipe(keygen("duck"), block());
        recipe.shape("WWW","WGW","WWW");
        recipe.setIngredient('W', Material.YELLOW_WOOL);
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.BLOCK;
    }
}
