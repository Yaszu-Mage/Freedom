package xyz.yaszu.freedom.Blocks.Decorations;

import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Table extends Util implements BaseBlock, BaseItem, Listener {
    @Override
    public ItemStack block() {
        ItemStack stack = ItemStack.of(org.bukkit.Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("Table"));
        meta.getPersistentDataContainer().set(keygen("customBlock"), PersistentDataType.STRING,"table");
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(),PersistentDataType.STRING,"table");
        meta.setItemModel(NamespacedKey.minecraft("table"));
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
        return 0;
    }

    @Override
    public boolean waterNeeded() {
        return false;
    }

    @Override
    public double scale() {
        return 1;
    }

    @Override
    public Location mountLocation() {
        return null;
    }

    @Override
    public Object placeSound() {
        return Sound.BLOCK_WOOD_PLACE;
    }

    @Override
    public InventoryHolder inventoryHolder() {
        return null;
    }

    @Override
    public ItemStack item() {
        return block();
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    @Override
    public Recipe recipe() {
        ShapedRecipe recipe = new ShapedRecipe(keygen("table"),block());
        recipe.shape(
                "WWW",
                " W ",
                " W "
        );
        recipe.setIngredient('W', RecipeChoice.itemType(ItemType.OAK_PLANKS,ItemType.SPRUCE_PLANKS,ItemType.BIRCH_PLANKS,ItemType.JUNGLE_PLANKS,ItemType.ACACIA_PLANKS,ItemType.DARK_OAK_PLANKS,ItemType.CRIMSON_PLANKS,ItemType.WARPED_PLANKS));
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.BLOCK;
    }
}
