package xyz.yaszu.freedom.Blocks.Decorations;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
import xyz.yaszu.freedom.Util.CustomEvents;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.UUID;

public class GrandfatherClock extends Util implements BaseBlock, BaseItem, Listener {
    @Override
    public ItemStack block() {
        ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("Grandfather Clock"));
        meta.getPersistentDataContainer().set(keygen("customBlock"), PersistentDataType.STRING,"grandfatherclock");
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(),PersistentDataType.STRING,"grandfatherclock");
        meta.setItemModel(NamespacedKey.minecraft("grandfatherclock_a"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public CollisionSize collisionSize() {
        return CollisionSize.Large;
    }

    @Override
    public Behavior behavior() {
        return Behavior.Updatable;
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
        return Sound.ITEM_LODESTONE_COMPASS_LOCK;
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
        //
    }

    @Override
    public Recipe recipe() {
        return null;
    }

    public ItemStack grandFatherTick(boolean tick) {
        if (tick) {
            ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
            ItemMeta meta = stack.getItemMeta();
            meta.displayName(dess("Grandfather Clock"));
            meta.getPersistentDataContainer().set(keygen("customBlock"), PersistentDataType.STRING,"grandfatherclock");
            meta.getPersistentDataContainer().set(FreedomKeys.itemId(),PersistentDataType.STRING,"grandfatherclock");
            meta.setItemModel(NamespacedKey.minecraft("grandfatherclock_b"));
            stack.setItemMeta(meta);
            return stack;
        } else {
            return block();
        }
    }


    @EventHandler
    public void onTimeChange(CustomEvents.TimeChangeEvent event) {
        BlockHandler.currentCustomData.keySet().forEach(blockPos -> {
           BaseBlock block = BlockHandler.currentCustomData.get(blockPos);
           if (!(block.behavior() == Behavior.Updatable && block.block().getItemMeta().getPersistentDataContainer().getOrDefault(FreedomKeys.itemId(), PersistentDataType.STRING,"").equals("grandfatherclock"))) {
               // we know grandfather clock
               UUID uuid = BlockHandler.currentCustomBlocks.get(blockPos);
               if (uuid != null) {
                   Entity e = Bukkit.getEntity(uuid);
                   if (e != null) {
                       if (e instanceof ItemDisplay display) {
                           if (display.getItemStack().getType() == Material.CLOCK) return;
                           // now we actually fucking update
                           if (display.getItemStack().getItemMeta().getItemModel() == NamespacedKey.minecraft("grandfatherclock_a")) {
                               display.setItemStack(grandFatherTick(true));
                               Location location = new Location(display.getWorld(), display.getLocation().getX(), display.getLocation().getY(), display.getLocation().getZ());
                               location.getWorld().playSound(location, Sound.ITEM_LODESTONE_COMPASS_LOCK, 0.5f, 1);
                           } else {
                               display.setItemStack(grandFatherTick(false));
                               Location location = new Location(display.getWorld(), display.getLocation().getX(), display.getLocation().getY(), display.getLocation().getZ());
                               location.getWorld().playSound(location, Sound.ITEM_LODESTONE_COMPASS_LOCK, 0.5f, 1.5f);
                           }

                       }
                   }
               }
           }
        });
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.BLOCK;
    }
    @Override
    public Location mountLocation() {
        return null;
    }
}
