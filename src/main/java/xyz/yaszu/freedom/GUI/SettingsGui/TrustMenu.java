package xyz.yaszu.freedom.GUI.SettingsGui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Subsystems.TrustManager;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrustMenu extends Util implements Listener {

    public static void openTrustMenu(Player player) {
        player.openInventory(new TrustMenuInventory(player).getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof TrustMenuInventory holder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta.getOwningPlayer() != null) {
                UUID targetUuid = meta.getOwningPlayer().getUniqueId();
                if (event.isRightClick()) {
                    TrustMemberMenu.openTrustMemberMenu(player, targetUuid);
                } else {
                    Player target = Bukkit.getPlayer(targetUuid);
                    if (target != null) {
                        TrustManager.toggleTrust(player, target);
                        openTrustMenu(player);
                    }
                }
            }
        } else if (item.getType() == Material.ARROW) {
            SettingsMenu.openSettingsMenu(player);
        }
    }

    public static class TrustMenuInventory implements InventoryHolder {
        private final Inventory inventory;

        public TrustMenuInventory(Player player) {
            this.inventory = Bukkit.createInventory(this, 54, dess("<shadow:#000000FF><b><gradient:green:green:#006400>Trust Management"));
            setupInventory(player);
        }

        private void setupInventory(Player player) {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, emptyItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)));
            }

            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            players.remove(player);

            int slot = 0;
            for (Player p : players) {
                if (slot >= 45) break; // Keep bottom row for navigation

                ItemStack skull = getSkull(p);
                ItemMeta meta = skull.getItemMeta();
                boolean trusted = TrustManager.isTrustedBy(player, p);
                
                List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                lore.add(dess(trusted ? "<green>Status: TRUSTED" : "<red>Status: NOT TRUSTED"));
                lore.add(dess("<gray>Left-Click to toggle general trust"));
                lore.add(dess("<gray>Right-Click to manage province permissions"));
                lore.add(dess("<gray>General trust allows abilities to not explicitly target them"));
                meta.lore(lore);
                
                skull.setItemMeta(meta);
                inventory.setItem(slot++, skull);
            }

            // Back button
            ItemStack back = ItemStack.of(Material.ARROW);
            ItemMeta backMeta = back.getItemMeta();
            backMeta.displayName(dess("<yellow>Back to Settings"));
            back.setItemMeta(backMeta);
            inventory.setItem(49, back);
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }
}
