package xyz.yaszu.freedom.Items.Parts;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.BulletSystem;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class ScythePhighting extends Util implements BaseItem, Listener {
    @Override
    public ItemStack item() {
        ItemStack item = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Util.dess("<shadow:#000000FF><b>Scythe</b>"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"scythephighting");
        meta.getPersistentDataContainer().set(keygen("scythephighting"), PersistentDataType.BOOLEAN, false);
        meta.setItemModel(NamespacedKey.minecraft("scythephightinga"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        if (item.getPersistentDataContainer().has(keygen("scythephighting"))) {
            Boolean isScytheMode = item.getPersistentDataContainer().get(keygen("scythephighting"), PersistentDataType.BOOLEAN);
            if (isScytheMode != null && !isScytheMode) {
                // Gun mode

                // Get current ammo (0-30)
                Integer currentAmmo = item.getPersistentDataContainer().get(keygen("scythephighting_ammo"), PersistentDataType.INTEGER);
                if (currentAmmo == null) {
                    currentAmmo = 0;
                }

                // Check if in reload cooldown
                Long lastReloadTime = item.getPersistentDataContainer().get(keygen("scythephighting_reload_time"), PersistentDataType.LONG);
                if (lastReloadTime != null && System.currentTimeMillis() - lastReloadTime < 2000) {
                    // Still in cooldown
                    player.sendActionBar(dess("Reloading... " + (2000 - (System.currentTimeMillis() - lastReloadTime)) / 1000 + "s"));
                    return;
                } else if (lastReloadTime != null && System.currentTimeMillis() - lastReloadTime >= 2000) {
                    // Reload cooldown completed, reset ammo
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.getPersistentDataContainer().set(keygen("scythephighting_ammo"), PersistentDataType.INTEGER, 0);
                        meta.getPersistentDataContainer().remove(keygen("scythephighting_reload_time"));
                        item.setItemMeta(meta);
                    }
                    currentAmmo = 0;
                }

                // Check if ammo is depleted
                if (currentAmmo >= 30) {
                    // Out of ammo, start reload cooldown
                    player.sendActionBar(dess("<color:#ff0000>Out of ammo! Reloading..."));
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.getPersistentDataContainer().set(keygen("scythephighting_reload_time"), PersistentDataType.LONG, System.currentTimeMillis());
                        item.setItemMeta(meta);
                    }
                    return;
                }

                // Fire the bullet
                BulletSystem.fireBullet(player, new BulletSystem.BulletConfig()
                        .shootSound(Sound.ENTITY_GENERIC_EXPLODE, 0.1f, 1.9f)
                        .hitSound(org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f)
                        .damage(3.0)
                        .speed(1)
                        .maxRange(60.0)
                        .collisionRadius(0.35)
                        .particle(Particle.SMOKE));

                // Increment ammo counter and update item meta
                currentAmmo++;
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.getPersistentDataContainer().set(keygen("scythephighting_ammo"), PersistentDataType.INTEGER, currentAmmo);
                    item.setItemMeta(meta);
                }

                // Send ammo status to player
                player.sendActionBar(dess("<color:#ffff00>Ammo: " + currentAmmo + "/30"));
            }
            // Scythe mode: handled elsewhere or add logic here
        }
    }

    public void swap(ItemStack item) {
        if (item.getPersistentDataContainer().has(keygen("scythephighting"))) {
            Boolean isScytheMode = item.getPersistentDataContainer().get(keygen("scythephighting"), PersistentDataType.BOOLEAN);
            if (isScytheMode == null) return;

            Freedom.get_plugin().getLogger().info("BEFORE: " + isScytheMode);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            if (isScytheMode) {
                // Currently in scythe mode, switch to gun mode
                meta.getPersistentDataContainer().set(keygen("scythephighting"), PersistentDataType.BOOLEAN, false);
                Freedom.get_plugin().getLogger().info("SWITCHED TO GUNMODE");
                meta.setItemModel(NamespacedKey.minecraft("scythephightinga"));
            } else {
                // Currently in gun mode, switch to scythe mode
                meta.getPersistentDataContainer().set(keygen("scythephighting"), PersistentDataType.BOOLEAN, true);
                Freedom.get_plugin().getLogger().info("SWITCHED TO SCYTHEMODE");
                meta.setItemModel(NamespacedKey.minecraft("scythephightingb"));
            }
            item.setItemMeta(meta);
        }
    }
    @EventHandler
    public void swapmode(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (droppedItem.getPersistentDataContainer().has(FreedomKeys.itemId())) {
            Freedom.get_plugin().getLogger().info("STEPONE");
            String itemId = droppedItem.getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING);
            if (itemId != null && itemId.equals("scythephighting")) {
                Freedom.get_plugin().getLogger().info("STEPTWO");
                Player player = event.getPlayer();
                if (!player.isSneaking()) {
                    // Swap the actual dropped item
                    Freedom.get_plugin().getLogger().info("STEPTHREE");
                    swap(droppedItem);
                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    public Recipe recipe() {
        RecipeChoice template = new RecipeChoice.MaterialChoice(Material.NETHERITE_INGOT);
        RecipeChoice base = new RecipeChoice.MaterialChoice(Material.DIAMOND);
        RecipeChoice addition = new RecipeChoice.MaterialChoice(Material.GUNPOWDER);
        ItemStack result = item();
        return new SmithingTransformRecipe(FreedomKeys.key("scythephighting"), result, template, base, addition);
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }
}
