package xyz.yaszu.freedom.Blocks.Crops;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Blocks.BlockHandler;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Rice extends Util implements BaseBlock, BaseItem {
    @Override
    public ItemStack block() {
        ItemStack stack = ItemStack.of(org.bukkit.Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("Rice"));
        meta.getPersistentDataContainer().set(keygen("customBlock"), PersistentDataType.STRING,"rice");
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(),PersistentDataType.STRING,"rice");
        meta.setItemModel(NamespacedKey.minecraft("rice"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public CollisionSize collisionSize() {
        return CollisionSize.Itsy;
    }

    @Override
    public Behavior behavior() {
        return Behavior.Farm;
    }
    @Override
    public int toolValue() {
        return 0;
    }

    @Override
    public boolean waterNeeded() {
        return true;
    }

    @Override
    public double scale() {
        return 1;
    }

    @Override
    public Object placeSound() {
        return Sound.BLOCK_WET_GRASS_PLACE;
    }

    @Override
    public InventoryHolder inventoryHolder() {
        return null;
    }

    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(org.bukkit.Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("Rice"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(),PersistentDataType.STRING,"riceitem");
        meta.setItemModel(NamespacedKey.minecraft("rice"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        BlockHandler.Place(this,event.getInteractionPoint(),player);
        player.getInventory().getItemInMainHand().subtract(1);
    }


    @Override
    public Recipe recipe() {
        return null;
    }

    @Override
    public CustomItemType getType() {
        return null;
    }
}
