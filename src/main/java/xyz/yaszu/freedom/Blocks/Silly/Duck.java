package xyz.yaszu.freedom.Blocks.Silly;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Duck extends Util implements BaseBlock, BaseItem, Listener {
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
        return Behavior.Interface;
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
    public InventoryHolder inventoryHolder() {
        return new Ducky();
    }

    @Override
    public ItemStack item() {
        return block();
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        //none
    }

    @EventHandler
    public void onInteract(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Ducky ducky) {
            if (event.getCurrentItem() == null) {
                if (event.getCurrentItem() == block()) {
                    Player player = (Player) event.getWhoClicked();
                    player.playSound(player.getLocation(), (String) placeSound(), 1f, 1f);
                }
            }
            event.setCancelled(true);
        }
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






    public class Ducky implements InventoryHolder {

        Inventory inventory = Bukkit.createInventory(this, 27, dess("Ducky"));
        public void setInventory() {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, emptyItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)));
            }
            //set center to duck
            inventory.setItem(13, block());
        }

        @Override
        public @NotNull Inventory getInventory() {
            setInventory();
            return inventory;
        }
    }
}
