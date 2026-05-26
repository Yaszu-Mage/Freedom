package xyz.yaszu.freedom.Items.Relics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class SoulGlass extends Util implements BaseItem, Listener {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Util.dess("<shadow:#000000FF><b><yellow>Soul Glass</yellow></b>"));
        meta.setItemModel(NamespacedKey.minecraft("soulglass"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"soulglass");
        meta.getPersistentDataContainer().set(keygen("soulglass"), PersistentDataType.BOOLEAN, true);
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        // Trigger an immediate visibility update to show auras/ghosts
        Life_and_Death.updateAllVisibility(player);
    }

    private void refreshVisibility(Player player) {
        if (player == null) return;
        Bukkit.getScheduler().runTaskLater(Freedom.get_plugin(), () -> {
            if (player.isOnline()) {
                Life_and_Death.updateAllVisibility(player);
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Trigger visibility update on join
        refreshVisibility(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            // Trigger visibility update when inventory changes
            refreshVisibility(player);
        }
    }

    @EventHandler
    public void onPlayerPickup(PlayerAttemptPickupItemEvent event) {
        // Trigger visibility update when picking up items
        refreshVisibility(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        refreshVisibility(event.getPlayer());
    }

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        refreshVisibility(event.getPlayer());
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        refreshVisibility(event.getPlayer());
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
