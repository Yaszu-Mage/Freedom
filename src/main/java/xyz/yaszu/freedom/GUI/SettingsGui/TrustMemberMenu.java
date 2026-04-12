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
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Subsystems.TrustManager;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrustMemberMenu extends Util implements Listener {

    public static void openTrustMemberMenu(Player owner, UUID guestUuid) {
        owner.openInventory(new TrustMemberMenuInventory(owner, guestUuid).getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof TrustMemberMenuInventory holder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        int slot = event.getSlot();

        if (slot >= 10 && slot <= 16) {
            TrustManager.ProvinceTrustFlag flag = holder.getFlagInSlot(slot);
            if (flag != null) {
                TrustManager.toggleTrustFlag(player.getUniqueId(), holder.guestUuid, flag);
                // Re-open to refresh
                openTrustMemberMenu(player, holder.guestUuid);
            }
        } else if (slot >= 18 && slot <= 20) {
            TrustManager.TrustTier tier = holder.getTierInSlot(slot);
            if (tier != null) {
                TrustManager.setTrustFlags(player.getUniqueId(), holder.guestUuid, tier.getFlags());
                // Re-open to refresh
                openTrustMemberMenu(player, holder.guestUuid);
            }
        } else if (item.getType() == Material.ARROW) {
            TrustMenu.openTrustMenu(player);
        }
    }

    public static class TrustMemberMenuInventory implements InventoryHolder {
        private final Inventory inventory;
        private final UUID guestUuid;
        private final List<TrustManager.ProvinceTrustFlag> flagsList = new ArrayList<>();
        private final List<TrustManager.TrustTier> tiersList = new ArrayList<>();

        public TrustMemberMenuInventory(Player owner, UUID guestUuid) {
            this.guestUuid = guestUuid;
            String guestName = Bukkit.getOfflinePlayer(guestUuid).getName();
            if (guestName == null) guestName = "Unknown";
            
            this.inventory = Bukkit.createInventory(this, 27, dess("<shadow:#000000FF><b><gradient:green:green:#006400>Trust: " + guestName));
            setupInventory(owner);
        }

        private void setupInventory(Player owner) {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, emptyItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)));
            }

            int slot = 10;
            for (TrustManager.ProvinceTrustFlag flag : TrustManager.ProvinceTrustFlag.values()) {
                if (slot > 16) break;
                
                boolean hasFlag = TrustManager.hasFlag(owner.getUniqueId(), guestUuid, flag);
                ItemStack item = ItemStack.of(flag.icon);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(dess((hasFlag ? "<green>" : "<red>") + flag.displayName));
                
                List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                lore.add(dess(hasFlag ? "<green>Enabled" : "<red>Disabled"));
                lore.add(dess("<gray>Click to toggle this permission"));
                meta.lore(lore);
                
                item.setItemMeta(meta);
                inventory.setItem(slot++, item);
                flagsList.add(flag);
            }

            // Tiers
            slot = 18;
            for (TrustManager.TrustTier tier : TrustManager.TrustTier.values()) {
                if (slot > 20) break;
                
                ItemStack item = ItemStack.of(tier.icon);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(dess("<yellow>Tier: " + tier.displayName));
                
                List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                lore.add(dess("<gray>Click to apply this tier's permissions"));
                meta.lore(lore);
                
                item.setItemMeta(meta);
                inventory.setItem(slot++, item);
                tiersList.add(tier);
            }

            // Back button
            ItemStack back = ItemStack.of(Material.ARROW);
            ItemMeta backMeta = back.getItemMeta();
            backMeta.displayName(dess("<yellow>Back to Trust List"));
            back.setItemMeta(backMeta);
            inventory.setItem(22, back);
        }

        public TrustManager.ProvinceTrustFlag getFlagInSlot(int slot) {
            int index = slot - 10;
            if (index >= 0 && index < flagsList.size()) {
                return flagsList.get(index);
            }
            return null;
        }

        public TrustManager.TrustTier getTierInSlot(int slot) {
            int index = slot - 18;
            if (index >= 0 && index < tiersList.size()) {
                return tiersList.get(index);
            }
            return null;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }
}
