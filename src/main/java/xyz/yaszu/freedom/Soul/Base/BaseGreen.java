package xyz.yaszu.freedom.Soul.Base;

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
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class BaseGreen extends Util implements Base_Soul {

    @Override
    public String Name_For_Container() {
        return "BaseGreen";
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
    public void AbilityOne(Player player) {
        if (player.getPersistentDataContainer().has(keygen("sprite_active"))) {
            if (Boolean.FALSE.equals(player.getPersistentDataContainer().get(keygen("sprite_active"), PersistentDataType.BOOLEAN))) {
            RayTraceResult ray = player.getWorld().rayTraceEntities(player.getLocation(), player.getEyeLocation().toVector(), 5d);
            if (ray != null) {
                if (ray.getHitEntity() != null) {
                    Entity looking_at = ray.getHitEntity();
                    if (looking_at instanceof Player target && target != player) {
                        if (target.getPersistentDataContainer().get(keygen("trustedby"), PersistentDataType.STRING) != null) {
                            if (target.getPersistentDataContainer().get(keygen("trustedby"), PersistentDataType.STRING).contains(player.getName())) {
                                registerSprite(target, player);
                            }
                        }
                    } else {
                        registerSprite(player, player);
                    }
                } else {
                    registerSprite(player, player);
                }
            } else {
                registerSprite(player, player);
            }
        }
    }
}
    public static void removeOldFollowers() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getPersistentDataContainer().has(keygen("sprite"))) {
                    if (entity.getPersistentDataContainer().get(keygen("sprite"),PersistentDataType.INTEGER) != Freedom.version) {
                        entity.remove();
                    }
                }
            }
        }
    }


    public void registerSprite(Player target, Player player) {
        target.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(80, 2));
        Location location = target.getLocation();
        Entity entity = target.getWorld().spawnEntity(location, EntityType.WOLF);
        entity.setCustomName(player.getName() + "'s Sprite");
        entity.setCustomNameVisible(true);
        Wolf wolf = (Wolf) entity;
        wolf.setTamed(true);
        wolf.setOwner(target);
        wolf.setSilent(true);
        wolf.getPersistentDataContainer().set(keygen("sprite"),PersistentDataType.INTEGER,Freedom.version);
        BetterModelPlatform platform = BetterModel.platform();
        PlatformEntity platwolf = BukkitAdapter.adapt(wolf);
        EntityTracker tracker = BetterModel.model("sillything")
                .map(r -> r.getOrCreate(platwolf))
                .orElse(null);
        follower(tracker,wolf,target,player).runTaskTimer(Bukkit.getPluginManager().getPlugin("Freedom"),0,40);
    }

    Random random = new Random();
    public BukkitRunnable follower(EntityTracker tracker, Entity entity,Player player, Player summoner) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if ((!player.isOnline() || entity.isDead() || Bukkit.getServer().isStopping()) || (!summoner.isOnline())) {
                    Freedom.get_plugin().getLogger().info("THIS WAS CANCELLED");
                    this.cancel();
                }
                if (random.nextInt(101) == 0) {
                    World world = entity.getWorld();
                    world.spawnParticle(Particle.HAPPY_VILLAGER, entity.getLocation(), 18);
                    world.playSound(entity.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1f, 0.5f);
                }
                Wolf wolf = (Wolf) entity;
                wolf.setSitting(false);
                wolf.setCustomNameVisible(true);
                wolf.setOwner(player);

                player.getPersistentDataContainer().set(keygen("sprite_active"),PersistentDataType.BOOLEAN,true);
                player.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(80, 1));
            }
            @Override
            public synchronized void cancel() throws IllegalStateException {
                player.getPersistentDataContainer().set(keygen("sprite_active"),PersistentDataType.BOOLEAN,false);
                tracker.despawn();
                tracker.close();
                entity.remove();

                Bukkit.getScheduler().cancelTask(getTaskId());
            }
        };
    }

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.STICK);
    }
    @EventHandler
    public void EntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity.getPersistentDataContainer().has(keygen("sprite"))) {
            Player player = Bukkit.getPlayer(entity.getName().replace("'s Sprite",""));
            player.getPersistentDataContainer().set(keygen("sprite_active"),PersistentDataType.BOOLEAN,false);
        }
    }
    @Override
    public Component AbilityTwoName() {
        return dess("Ability Two - Short Range Healing");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("Heal those you trust quickly");
    }


    public static HashMap<UUID,Long> abilityTwoCooldownTime = new HashMap<>();
    public long AbilityTwo_Cooldown = 30000;


    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) {
        //
        if (can_ability(AbilityTwo_Cooldown,abilityTwoCooldownTime,player.getUniqueId())) {
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                player.getLocation().getNearbyEntitiesByType(Player.class, 10).forEach(iterator -> {
                        String trusted;
                        if (iterator.getPersistentDataContainer().has(keygen("trustedby"), PersistentDataType.STRING)) {

                            trusted = iterator.getPersistentDataContainer().get(keygen("trustedby"), PersistentDataType.STRING);
                            if (trusted.contains(player.getName()) && iterator.getLocation().distanceSquared(player.getLocation()) <= 10) {
                                iterator.addPotionEffect(PotionEffectType.INSTANT_HEALTH.createEffect(1, 0));
                                iterator.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, iterator.getLocation(),8);
                                if (!player.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                                    player.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(120, 0));
                                } else {
                                    int amplifier = player.getPotionEffect(PotionEffectType.SLOWNESS).getAmplifier();
                                    player.removePotionEffect(PotionEffectType.SLOWNESS);
                                    player.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(120, amplifier + 1));
                                }

                            }
                        }

                });
                tick++;
                if (tick == 2) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Freedom.get_plugin(),0,80);

    }
    }

    @Override
    public Component Passive_Description() {
        return dess("You heal all people you trust in a 10 block radius");
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
                      if (iterator.getPersistentDataContainer().has(keygen("trustedby"), PersistentDataType.STRING)) {
                          trusted = iterator.getPersistentDataContainer().get(keygen("trustedby"), PersistentDataType.STRING);
                          if (trusted.contains(player.getName()) && iterator.getLocation().distanceSquared(player.getLocation()) <= 100) {
                              iterator.addPotionEffect(PotionEffectType.REGENERATION.createEffect(80, 0));
                          }
                      }

                  }
              }
            }
        };
    }

    @Override
    public Component ActivePassive_Description() {
        return dess("Regeneration for yourself for 30s");
    }

    @Override
    public void ActivePassive(Player player) {
        if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 6000, 0));
            double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, SoulPoints - 5);
        }
    }
}
