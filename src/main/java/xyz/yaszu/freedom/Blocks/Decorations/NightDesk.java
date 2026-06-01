package xyz.yaszu.freedom.Blocks.Decorations;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.InventoryPersistentDataType;
import xyz.yaszu.freedom.Util.Util;

public class NightDesk extends Util implements BaseBlock, BaseItem, Listener {
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof NightStand nightStand) {
            nightStand.saveInventory();
        }
    }




    @Override
    public ItemStack block() {
        ItemStack stack = ItemStack.of(org.bukkit.Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(keygen("customBlock"), PersistentDataType.STRING,"nightdesk");
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(),PersistentDataType.STRING,"nightdesk");
        meta.setItemModel(NamespacedKey.minecraft("nightdesk"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public CollisionSize collisionSize() {
        return CollisionSize.Itsy;
    }

    @Override
    public Behavior behavior() {
        return Behavior.Interface;
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
        return new NightStand();
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
        ShapedRecipe recipe = new ShapedRecipe(keygen("nightdesk"),block());
        recipe.shape(
                "WWW" +
                "FCF" +
                "F F");
        recipe.setIngredient('W', RecipeChoice.itemType(ItemType.OAK_PLANKS,ItemType.BIRCH_PLANKS,ItemType.JUNGLE_PLANKS,ItemType.ACACIA_PLANKS,ItemType.DARK_OAK_PLANKS,ItemType.CRIMSON_PLANKS,ItemType.WARPED_PLANKS));
        recipe.setIngredient('F', RecipeChoice.itemType(ItemType.OAK_FENCE,ItemType.ACACIA_FENCE,ItemType.BIRCH_FENCE,ItemType.DARK_OAK_FENCE,ItemType.JUNGLE_FENCE,ItemType.SPRUCE_FENCE,ItemType.CRIMSON_FENCE,ItemType.WARPED_FENCE));
        recipe.setIngredient('C', Material.CHEST);
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.BLOCK;
    }


    public static class NightStand implements InventoryHolder {
        Inventory inventory;
        Location location;
        public void setInventory(Location location) {
             if (location.getWorld().getPersistentDataContainer().has(keygen(location.toString()))) {
                 Inventory inv = location.getWorld().getPersistentDataContainer().get(keygen(location.toString()), InventoryPersistentDataType.get());
                 if (inv != null) {
                     inventory = inv;
                 } else {
                     inventory = Bukkit.createInventory(this, 27, "Night Stand");
                     location.getWorld().getPersistentDataContainer().set(keygen(location.toString()), InventoryPersistentDataType.get(), inventory);
                 }
             } else {
                 inventory = Bukkit.createInventory(this, 27, "Night Stand");
             }
        }
        public void saveInventory() {
            location.getWorld().getPersistentDataContainer().set(keygen(location.toString()), InventoryPersistentDataType.get(), inventory);
        }


        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
