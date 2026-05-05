package xyz.yaszu.freedom.Items.ColorSpecific;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.Ultra.Purple;
import xyz.yaszu.freedom.Util.BulletSystem;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.createVerticleMinMagicCircle;

public class Rifle implements BaseItem {
    private final Purple purple = new Purple();

    @Override
    public ItemStack item() {
        return purple.Related_Item();
    }


    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        SoulTypes soul = Util.getSoulType(player);
        player.setVelocity(player.getLocation().getDirection().multiply(-0.5));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE,1,10);
        if (soul != SoulTypes.Purple && soul != SoulTypes.BasePurple) {
            //do stuff
            BulletSystem.fireBullet(player,new BulletSystem.BulletConfig()
                    .damage(15.0)
                    .speed(1.0)
                    .maxRange(50.0)
                    .collisionRadius(0.25)
                    .particle(Particle.CRIT));
            event.setCancelled(true);
        } else {
            BulletSystem.fireBullet(player,new BulletSystem.BulletConfig()
                    .damage(11.0)
                    .speed(1.0)
                    .maxRange(50.0)
                    .collisionRadius(0.25)
                    .particle(Particle.CRIT));
        }
        if (!xyz.yaszu.freedom.Subsystems.AdminManager.isSudo(player)) {
            player.setCooldown(item, 100);
        }
    }

    @Override
    public Recipe recipe() {
        RecipeChoice template = new RecipeChoice.MaterialChoice(Material.DIAMOND);
        RecipeChoice base = new RecipeChoice.MaterialChoice(Material.IRON_INGOT);
        RecipeChoice addition = new RecipeChoice.MaterialChoice(Material.GUNPOWDER);
        ItemStack result = item();
        return new SmithingTransformRecipe(FreedomKeys.key("rifle"), result, template, base, addition);
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.COLOR_SPECIFIC;
    }


}
