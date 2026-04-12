package xyz.yaszu.freedom.GUI.SettingsGui;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
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
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.UUID;

public class SettingsMenu extends Util implements Listener{
    public static LiteralCommandNode<CommandSourceStack> settingsCommand() {
        return Commands.literal("settings").executes(
                ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        openSettingsMenu(player);
                    }
                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof SettingsMenuInventory holder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() != null && event.getWhoClicked() instanceof Player player) {
            ItemStack item = event.getCurrentItem();
            int slot = event.getSlot();

            if (slot == 0) {
                if (item.getType() == Material.REDSTONE_BLOCK) {
                    player.getPersistentDataContainer().set(keygen("ComorAction"), PersistentDataType.BOOLEAN, true);
                    player.sendRichMessage("You have selected to use Actions Instead of Commands");
                    Boolean val = player.getPersistentDataContainer().get(keygen("ComorAction"), PersistentDataType.BOOLEAN);
                    boolean actionsOnly = val != null && !val;
                    ItemStack toggleItem = ItemStack.of(actionsOnly ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK);
                    ItemMeta toggleMeta = toggleItem.getItemMeta();
                    toggleMeta.displayName(dess(actionsOnly ? "Actions Only" : "Commands Only"));
                    toggleItem.setItemMeta(toggleMeta);
                    event.getInventory().setItem(0, toggleItem);

                } else if (item.getType() == Material.EMERALD_BLOCK) {
                    player.getPersistentDataContainer().set(keygen("ComorAction"), PersistentDataType.BOOLEAN, false);
                    player.sendRichMessage("You have selected to use Commands Instead of Actions");
                    Boolean val = player.getPersistentDataContainer().get(keygen("ComorAction"), PersistentDataType.BOOLEAN);
                    boolean actionsOnly = val != null && !val;
                    ItemStack toggleItem = ItemStack.of(actionsOnly ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK);
                    ItemMeta toggleMeta = toggleItem.getItemMeta();
                    toggleMeta.displayName(dess(actionsOnly ? "Actions Only" : "Commands Only"));
                    toggleItem.setItemMeta(toggleMeta);
                    event.getInventory().setItem(0, toggleItem);
                }
            } else if (slot == 1) {
                boolean current = player.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockBreak(), PersistentDataType.BOOLEAN, true);
                player.getPersistentDataContainer().set(xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockBreak(), PersistentDataType.BOOLEAN, !current);
                xyz.yaszu.freedom.Subsystems.ProvinceManager.updatePlayerSettings(player.getUniqueId(), xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockBreak(), !current);
                player.sendRichMessage("Province Block Break Protection: " + (!current ? "<green>Enabled" : "<red>Disabled"));
                openSettingsMenu(player);
            } else if (slot == 2) {
                boolean current = player.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockPlace(), PersistentDataType.BOOLEAN, true);
                player.getPersistentDataContainer().set(xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockPlace(), PersistentDataType.BOOLEAN, !current);
                xyz.yaszu.freedom.Subsystems.ProvinceManager.updatePlayerSettings(player.getUniqueId(), xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockPlace(), !current);
                player.sendRichMessage("Province Block Place Protection: " + (!current ? "<green>Enabled" : "<red>Disabled"));
                openSettingsMenu(player);
            } else if (slot == 3) {
                boolean current = player.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceFireSpread(), PersistentDataType.BOOLEAN, false);
                player.getPersistentDataContainer().set(xyz.yaszu.freedom.Util.FreedomKeys.provinceFireSpread(), PersistentDataType.BOOLEAN, !current);
                xyz.yaszu.freedom.Subsystems.ProvinceManager.updatePlayerSettings(player.getUniqueId(), xyz.yaszu.freedom.Util.FreedomKeys.provinceFireSpread(), !current);
                player.sendRichMessage("Province Fire Spread: " + (!current ? "<green>Allowed" : "<red>Blocked"));
                openSettingsMenu(player);
            } else if (slot == 4) {
                TrustMenu.openTrustMenu(player);
            } else if (slot == 5) {
                boolean current = player.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceExplosions(), PersistentDataType.BOOLEAN, false);
                player.getPersistentDataContainer().set(xyz.yaszu.freedom.Util.FreedomKeys.provinceExplosions(), PersistentDataType.BOOLEAN, !current);
                xyz.yaszu.freedom.Subsystems.ProvinceManager.updatePlayerSettings(player.getUniqueId(), xyz.yaszu.freedom.Util.FreedomKeys.provinceExplosions(), !current);
                player.sendRichMessage("Province Explosions: " + (!current ? "<green>Allowed" : "<red>Blocked"));
                openSettingsMenu(player);
            }
        }
    }

    public static void openSettingsMenu(Player player) {
        player.openInventory(new SettingsMenuInventory(player).getInventory());
    }

    public static class SettingsMenuInventory implements InventoryHolder {
        private final Inventory inventory;

        public SettingsMenuInventory(Player player) {
            this.inventory = Bukkit.createInventory(this, 9, dess("<shadow:#000000FF><b><gradient:gold:gold:#a64000>Settings"));
            setupInventory(player);
        }

        private void setupInventory(Player player) {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, emptyItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)));
            }

            // Commands/Actions toggle
            Boolean val = player.getPersistentDataContainer().get(keygen("ComorAction"), PersistentDataType.BOOLEAN);
            boolean actionsOnly = val != null && !val;
            ItemStack toggleItem = ItemStack.of(actionsOnly ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK);
            ItemMeta toggleMeta = toggleItem.getItemMeta();
            toggleMeta.displayName(dess(actionsOnly ? "Actions Only" : "Commands Only"));
            toggleItem.setItemMeta(toggleMeta);
            inventory.setItem(0, toggleItem);

            // Province Settings
            // Slot 1: Block Break
            boolean breakProt = player.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockBreak(), PersistentDataType.BOOLEAN, true);
            ItemStack breakItem = ItemStack.of(Material.IRON_PICKAXE);
            ItemMeta breakMeta = breakItem.getItemMeta();
            breakMeta.displayName(dess("<gold>Block Break Protection: " + (breakProt ? "<green>Enabled" : "<red>Disabled")));
            breakItem.setItemMeta(breakMeta);
            inventory.setItem(1, breakItem);

            // Slot 2: Block Place
            boolean placeProt = player.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockPlace(), PersistentDataType.BOOLEAN, true);
            ItemStack placeItem = ItemStack.of(Material.GRASS_BLOCK);
            ItemMeta placeMeta = placeItem.getItemMeta();
            placeMeta.displayName(dess("<gold>Block Place Protection: " + (placeProt ? "<green>Enabled" : "<red>Disabled")));
            placeItem.setItemMeta(placeMeta);
            inventory.setItem(2, placeItem);

            // Slot 3: Fire Spread
            boolean fireAllowed = player.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceFireSpread(), PersistentDataType.BOOLEAN, false);
            ItemStack fireItem = ItemStack.of(Material.FLINT_AND_STEEL);
            ItemMeta fireMeta = fireItem.getItemMeta();
            fireMeta.displayName(dess("<gold>Fire Spread: " + (fireAllowed ? "<green>Allowed" : "<red>Blocked")));
            fireItem.setItemMeta(fireMeta);
            inventory.setItem(3, fireItem);

            // Trust Management
            ItemStack trustItem = ItemStack.of(Material.PLAYER_HEAD);
            ItemMeta trustMeta = trustItem.getItemMeta();
            trustMeta.displayName(dess("<green>Trust Management"));
            trustItem.setItemMeta(trustMeta);
            inventory.setItem(4, trustItem);

            // Slot 5: Explosions
            boolean expAllowed = player.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceExplosions(), PersistentDataType.BOOLEAN, false);
            ItemStack expItem = ItemStack.of(Material.TNT);
            ItemMeta expMeta = expItem.getItemMeta();
            expMeta.displayName(dess("<gold>Explosions: " + (expAllowed ? "<green>Allowed" : "<red>Blocked")));
            expItem.setItemMeta(expMeta);
            inventory.setItem(5, expItem);
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }


}
