package xyz.yaszu.freedom.Soul;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.BetterModelPlatform;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.platform.PlatformEntity;
import kr.toxicity.model.api.tracker.EntityTracker;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.Random;

public class Green extends Util implements Base_Soul{

    @Override
    public String Name_For_Container() {
        return "Green";
    }

    @Override
    public Component Name() {
        return dess("<green>Green</green>");
    }

    @Override
    public Component Description() {
        return dess("You want to help, you want to mend what has been broken");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.GOLDEN_APPLE);
    }

    @Override
    public Component AbilityOneName() {
        return dess("<green>Ability One</green> - Sprite");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("You can summon a sprite that helps you or your allies.");
    }

    @Override
    public void AbilityOne(Player player) throws AssertionError{
        RayTraceResult ray = player.getWorld().rayTraceEntities(player.getLocation(),player.getEyeLocation().toVector(),5d);
        if (ray != null) {
        if (ray.getHitEntity() != null) {
            Entity looking_at = ray.getHitEntity();
            if (looking_at instanceof Player target) {
                if (target.getPersistentDataContainer().get(keygen("trustedby"), PersistentDataType.STRING) != null) {
                    if (target.getPersistentDataContainer().get(keygen("trustedby"), PersistentDataType.STRING).contains(player.getName())) {
                        registerSprite(target,player);
                    }
                }
            } else {
                registerSprite(player,player);
            }
    } else {
            registerSprite(player,player);
        }
    } else {
            registerSprite(player,player);
        }
    }

    public void registerSprite(Player target, Player player) {
        target.addPotionEffect(PotionEffectType.REGENERATION.createEffect(80, 0));
        target.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(80, 2));
        Location location = target.getLocation();
        Entity entity = target.getWorld().spawnEntity(location, EntityType.WOLF);
        entity.setCustomName(player.getName() + "'s Sprite");
        entity.setCustomNameVisible(true);
        Wolf wolf = (Wolf) entity;
        wolf.setTamed(true);
        wolf.setOwner(target);
        wolf.setSilent(true);
        BetterModelPlatform platform = BetterModel.platform();
        PlatformEntity platwolf = BukkitAdapter.adapt(wolf);
        EntityTracker tracker = BetterModel.model("sillything")
                .map(r -> r.getOrCreate(platwolf))
                .orElse(null);
        follower(tracker,wolf,target).runTaskTimer(Bukkit.getPluginManager().getPlugin("Freedom"),0,40);
    }

    Random random = new Random();
    public BukkitRunnable follower(EntityTracker tracker, Entity entity,Player player) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || entity.isDead()) {
                    this.cancel();
                }
                if (random.nextInt(101) == 0) {
                    World world = entity.getWorld();
                    world.spawnParticle(Particle.HAPPY_VILLAGER, entity.getLocation(), 18);
                    world.playSound(entity.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1f, 0.5f);
                }
                Wolf wolf = (Wolf) entity;
                wolf.setSitting(false);
                player.addPotionEffect(PotionEffectType.REGENERATION.createEffect(80, 1));
                player.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(80, 1));
            }
            @Override
            public synchronized void cancel() throws IllegalStateException {
                tracker.despawn();
                tracker.close();
                Bukkit.getScheduler().cancelTask(getTaskId());
            }
        };
    }

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.STICK);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("Ability Two - ⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) {

    }

    @Override
    public Component Passive_Description() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void Passive(Player player, Object event) {
        heal_all_trusted(player).runTaskTimer(Bukkit.getPluginManager().getPlugin("Freedom"),0,60);
    }

    public BukkitRunnable heal_all_trusted(Player player) {
        return new BukkitRunnable() {

            @Override
            public void run() {
                World world = player.getWorld();
              for (Player iterator : world.getPlayers()) {
                  if (iterator != player) {
                      String trusted;
                      Freedom.get_plugin().getLogger().info("Iterating through" + iterator.getName());
                      if (iterator.getPersistentDataContainer().has(keygen("trustedby"), PersistentDataType.STRING)) {
                          trusted = iterator.getPersistentDataContainer().get(keygen("trustedby"), PersistentDataType.STRING);
                          if (trusted.contains(player.getName())) {
                              iterator.addPotionEffect(PotionEffectType.REGENERATION.createEffect(80, 0));
                              Freedom.get_plugin().getLogger().info("healed " + iterator.getName());
                          }
                      }

                  }
              }
            }
        };
    }

    @Override
    public Component ActivePassive_Description() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void ActivePassive(Player player) {

    }
}
