package xyz.yaszu.freedom.Blocks.AdminPlush;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class ZanePlush extends Util implements BaseBlock, BaseItem {
    @Override
    public ItemStack block() {
        ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b>Zane Plush"));
        meta.getPersistentDataContainer().set(keygen("customBlock"), PersistentDataType.STRING,"zaneplush");
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(),PersistentDataType.STRING,"zaneplush");
        meta.setItemModel(NamespacedKey.minecraft("zaneplush"));
        stack.setItemMeta(meta);
        return stack;
    }
    @Override
    public CollisionSize collisionSize() {
        return CollisionSize.Itsy;
    }

    @Override
    public Behavior behavior() {
        return Behavior.Interactable;
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
    public Object placeSound() {
        return "custom.squeak";
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
        plush(player,event,this);
    }

    @Override
    public Recipe recipe() {
        return null;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.BLOCK;
    }
}
