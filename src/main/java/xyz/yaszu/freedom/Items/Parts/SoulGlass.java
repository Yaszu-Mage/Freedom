package xyz.yaszu.freedom.Items.Parts;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.UUID;

public class SoulGlass extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.SPYGLASS);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Util.dess("<shadow:#000000FF><b><yellow>Soul Glass</yellow></b>"));
        meta.setItemModel(NamespacedKey.minecraft("soulglass"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"soulglass");
        meta.getPersistentDataContainer().set(keygen("soulglass"), PersistentDataType.BOOLEAN, true);
        stack.setItemMeta(meta);
        return stack;
    }

    public static HashMap<UUID, BukkitRunnable> currentChecks = new HashMap<>();
    public static HashMap<UUID, Long> currentExtensions = new HashMap<>();
    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        if (!currentChecks.containsKey(player.getUniqueId())) {
            BukkitRunnable check = checkSoul(player,20,item);
            currentChecks.put(player.getUniqueId(),check);
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        cheatme(event.getPlayer(),0,item());
    }
    @EventHandler
    public void onPlayerQuit(PlayerJoinEvent event) {
        try {
            cheatme(event.getPlayer(),0,item());
        } catch (Exception ignored) {}
    }
    @EventHandler
    public void onItemMove(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            cheatme(player,0,item());
        }
    }
    @EventHandler
    public void onPlayerPickup(PlayerAttemptPickupItemEvent event) {
        cheatme(event.getPlayer(),0,item());
    }
    public void cheatme(Player player,int ticktill,ItemStack stack) {
        BukkitRunnable check = checkSoul(player,ticktill,stack);
        currentChecks.put(player.getUniqueId(),check);
        check.runTaskTimer(Freedom.get_plugin(),0,0);
    }

    public BukkitRunnable checkSoul(Player player,int ticktill,ItemStack stack) {
        return new BukkitRunnable() {
            public static int ticksTillEnd = 0;
            public int ticks = 0;
            @Override
            public void run() {
                if (ticks == 0) {
                    ticksTillEnd = ticktill;
                }
                ticks = ticks + 1;
                if (ticksTillEnd >= ticks) {
                    player.getInventory().forEach(stacker -> {
                        if (stacker.getPersistentDataContainer().has(keygen("soulglass"))) {
                            ItemMeta meta = stacker.getItemMeta();
                            meta.getPersistentDataContainer().set(keygen("soulglass"), PersistentDataType.BOOLEAN, false);
                            stacker.setItemMeta(meta);
                        }
                    });
                    currentChecks.remove(player.getUniqueId());
                    this.cancel();
                }

            }
        };
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
