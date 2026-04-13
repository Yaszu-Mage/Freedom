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
            handleSnipe(player,15).runTaskTimer(Freedom.get_plugin(),0,1);
            event.setCancelled(true);
        } else {
            handleSnipe(player,11).runTaskTimer(Freedom.get_plugin(),0,1);
        }
        player.setCooldown(item,100);
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

    public BukkitRunnable handleSnipe(Player player,int damage) {
        return new BukkitRunnable() {
            Vector direction = player.getLocation().getDirection();
            World world = player.getWorld();
            @Override
            public void run() {
                if (snipeLocation == null) {
                    snipeLocation = player.getLocation().add(0,1,0);
                }
                if (snipeLocation != player.getLocation()) {
                    world.spawnParticle(Particle.CRIT,snipeLocation,30);
                } else {
                    createVerticleMinMagicCircle(snipeLocation.clone().add(player.getLocation().getDirection()),15, SoulTypes.Purple,player.getLocation().getYaw(),player.getLocation(),100,0.25);
                }

                snipeLocation.add(direction);
                for (Entity inst : snipeLocation.getNearbyEntities(4,4,4)) {
                    if (inst instanceof Player) {
                        if (inst != player) {
                            if (inst.getLocation().distanceSquared(snipeLocation) <= 4) {
                                LivingEntity entity = (LivingEntity) inst;
                                entity.damage(damage, player);
                                this.cancel();
                            }
                        }
                    } else {
                        if (inst.getLocation().distanceSquared(snipeLocation) <= 4) {
                            LivingEntity entity = (LivingEntity) inst;
                            entity.damage(damage, player);
                            this.cancel();
                        }
                    }
                }

                if (snipeLocation.isBlock()) {
                    snipeLocation.getBlock().setType(Material.AIR);
                    this.cancel();
                }
            }

            public Location snipeLocation;


        };
    }
}
