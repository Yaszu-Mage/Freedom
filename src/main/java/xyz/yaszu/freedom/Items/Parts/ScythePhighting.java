package xyz.yaszu.freedom.Items.Parts;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
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

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

public class ScythePhighting implements BaseItem, Listener {
    @Override
    public ItemStack item() {
        ItemStack item = ItemStack.of(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b>Scythe</b>"));
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
                // Gun mode - initialize ammo system on first use
                BulletSystem.initializeAmmo(player, item, 30);
                // Fire the bullet using BulletSystem's ammo management
                BulletSystem.fireBullet(player, new BulletSystem.BulletConfig()
                        .maxAmmo(30)
                        .reloadTimeTicks(40)
                        .shootSound(Sound.ENTITY_GENERIC_EXPLODE, 0.1f, 1.9f)
                        .hitSound(org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f)
                        .charge(0)
                        .damage(0.5)
                        .speed(1)
                        .maxRange(60.0)
                        .collisionRadius(1.78)
                        .particle(Particle.SMOKE));

                // Send ammo status to player
                player.sendActionBar(dess("<color:#ffff00>Ammo: " + BulletSystem.getAmmo(player, item) + "/30"));
            }
            // Scythe mode: handled elsewhere or add logic here
        }
    }
    AttributeModifier gunModeAttack = new AttributeModifier(keygen("scythephighting"),-15, AttributeModifier.Operation.ADD_NUMBER);
    AttributeModifier scytheModeAttack = new AttributeModifier(keygen("scythephighting"),-2, AttributeModifier.Operation.ADD_NUMBER);
    AttributeModifier SPEED = new AttributeModifier(keygen("scythephighting"),2, AttributeModifier.Operation.ADD_NUMBER);
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
                meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE,scytheModeAttack);
                meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,gunModeAttack);
            } else {
                // Currently in gun mode, switch to scythe mode
                meta.getPersistentDataContainer().set(keygen("scythephighting"), PersistentDataType.BOOLEAN, true);
                Freedom.get_plugin().getLogger().info("SWITCHED TO SCYTHEMODE");
                meta.setItemModel(NamespacedKey.minecraft("scythephightingb"));
                meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE,gunModeAttack);
                meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,scytheModeAttack);
            }
            meta.removeAttributeModifier(Attribute.ATTACK_SPEED,SPEED);
            meta.addAttributeModifier(Attribute.ATTACK_SPEED,SPEED);
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
