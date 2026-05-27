package xyz.yaszu.freedom.Blocks.Decorations;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Subsystems.SitManager;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Couch extends Util implements BaseBlock, BaseItem, Listener {
    @Override
    public ItemStack block() {
        ItemStack stack = ItemStack.of(org.bukkit.Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("Couch"));
        meta.getPersistentDataContainer().set(keygen("customBlock"), PersistentDataType.STRING,"couch");
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(),PersistentDataType.STRING,"couch");
        meta.setItemModel(org.bukkit.NamespacedKey.minecraft("couch"));
        stack.setItemMeta(meta);
        return stack;
    }
    @Override
    public Location mountLocation() {
        return new Location(null,0,0,0);
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
        return Sound.BLOCK_WOOL_PLACE;
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
        SitManager.sit(player,event.getInteractionPoint().clone().add(mountLocation().clone()));
    }

    @Override
    public Recipe recipe() {
        return null;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }
}
