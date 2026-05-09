package xyz.yaszu.freedom.Items.ColorSpecific;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Soul.Ultra.Purple;
import xyz.yaszu.freedom.Util.BulletSystem;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;


public class Railgun extends Util implements BaseItem {
    private final Purple purple = new Purple();

    @Override
    public ItemStack item() {
        ItemStack item = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Util.dess("<shadow:#000000FF><b>Railgun</b>"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"railgun");
        meta.getPersistentDataContainer().set(keygen("railgun"), PersistentDataType.BOOLEAN, false);
        meta.setItemModel(NamespacedKey.minecraft("railgun"));

        item.setItemMeta(meta);
        return item;
    }


    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        // Initialize reload system on first use

        // Prevent firing while charging
        if (BulletSystem.isCharging(player)) {
            return;
        }

        // Display ammo to player
        int currentAmmo = BulletSystem.getAmmo(player,item);
        int maxAmmo = BulletSystem.getMaxAmmo(player,item);
        player.sendActionBar(Util.dess("<color:#ffff00>Ammo: " + currentAmmo + "/" + maxAmmo));
        BulletSystem.initializeAmmo(player, item,1); // 1 round magazine
        // Fire the railgun with charge mechanics and reload
        BulletSystem.fireBullet(player, new BulletSystem.BulletConfig()
                .damage(25.0)
                .speed(2.0)
                .maxRange(100.0)
                .collisionRadius(0.4)
                .particle(Particle.END_ROD)
                .shootSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.8f)
                .chargeSound(Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 1.5f)
                .hitSound(Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f)
                .charge(300)  // 0.75 second charge time
                .ammoPerShot(1)           // Uses 1 ammo per shot
                .maxAmmo(20)              // 20 bullets per magazine
                .reloadTimeTicks(40)       // 2 second reload
                .chargeCallback(chargeLevel -> {
                    // Show charging feedback
                    player.sendActionBar(Util.dess("<color:#00ffff>⚡ Charging: " + chargeLevel + "/300"));
                })
                .reloadCallback(reloadLevel -> {
                    // Optional: Show reload feedback
                    player.sendActionBar(Util.dess("<color:#ff00ff>Reloading..."));
                }));
    }

    @Override
    public Recipe recipe() {
        RecipeChoice template = new RecipeChoice.MaterialChoice(Material.DIAMOND);
        RecipeChoice base = new RecipeChoice.MaterialChoice(Material.IRON_INGOT);
        RecipeChoice addition = new RecipeChoice.MaterialChoice(Material.DRAGON_BREATH);
        ItemStack result = item();
        return new SmithingTransformRecipe(FreedomKeys.key("railgun"), result, template, base, addition);
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.COLOR_SPECIFIC;
    }


}
