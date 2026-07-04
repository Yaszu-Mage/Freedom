package xyz.yaszu.freedom.Soul.Base;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.BetterModelPlatform;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.platform.PlatformEntity;
import kr.toxicity.model.api.tracker.EntityTracker;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Subsystems.SoulImbueManager;
import xyz.yaszu.freedom.Subsystems.TrustManager;
import xyz.yaszu.freedom.Util.Util;

import javax.annotation.Nullable;
import java.util.*;

import static xyz.yaszu.freedom.Util.Util.*;

public class BaseGreen implements Base_Soul, Listener {

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
        AbilityOne(player, false);
    }

    /**
     * Executes the first ability of the player, applying various effects based on
     * the provided parameters. This method redirects to {@link #AbilityOneRedo(Player, boolean, int)}
     * for additional logic and processing.
     *
     * @param player    The player who is using the ability.
     * @param is_imbue  Indicates whether the ability is being imbued with additional effects.
     */
    @Override
 public void AbilityOne(Player player, boolean is_imbue) {
        AbilityOneRedo(player, is_imbue,0);
//        if (player.getPersistentDataContainer().has(keygen("sprite_active"))) {
//            if (Boolean.FALSE.equals(player.getPersistentDataContainer().get(keygen("sprite_active"), PersistentDataType.BOOLEAN))) {
//            RayTraceResult ray = player.getWorld().rayTraceEntities(player.getLocation(), player.getEyeLocation().toVector(), 5d);
//            if (ray != null) {
//                if (ray.getHitEntity() != null) {
//                    Entity looking_at = ray.getHitEntity();
//                    if (looking_at instanceof Player target && target != player) {
//
//          if (target.getPersistentDataContainer().get(keygen("trustedby"), PersistentDataType.STRING) != null) {
//                            if (target.getPersistentDataContainer().get(keygen("trustedby"), PersistentDataType.STRING).contains(player.getName())) {
//                                registerSprite(target, player);
//                            }
//                        }
//                    } else {
//                        registerSprite(player, player);
//                    }
//                } else {
//                    registerSprite(player, player);
//                }
//            } else {
//                registerSprite(player, player);
//            }
//        }
//    } else {
//        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
//    }
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

    /**
     * Recreation of the original ability one for green because the models were not done
     * @param player Player to apply ability to
     * @param is_imbue if it is an imbuement or not
     * @param modifier modifier to add
     */
    public void AbilityOneRedo(Player player, boolean is_imbue, @Nullable int modifier) {
        //--------------- VFX -----------------\\
        Location playerLocation = player.getLocation();
        Location location = player.getLocation();
        World world = player.getWorld();
        location.addRotation(0,0);
        location.add(0,2,0);
        Location endingLocation = location.clone().add(0,1,0);
        drawTintedDisplay(10,location,10,null,endingLocation,Color.GREEN,2);
        drawTintedDisplay(10,endingLocation.clone().add(0,3,0),10,null,endingLocation.add(0,-1,0),Color.GREEN,4);
        drawSpiral(playerLocation,2,4,playerLocation.getWorld(),32,Particle.DUST,new Particle.DustOptions(Color.GREEN,1f));
        //------------- Logic -------------------\\
        if (!is_imbue) {
            world.playSound(playerLocation, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1, 1);
            world.playSound(playerLocation, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            Collection<Player> playersNear = playerLocation.getNearbyPlayers(4);
            UUID casterId = player.getUniqueId();
            playersNear.forEach(target -> {
                UUID targetId = target.getUniqueId();
                if (TrustManager.isMutual(casterId, targetId)) {
                    target.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(300, 2));
                    target.addPotionEffect(PotionEffectType.SPEED.createEffect(80, 2));
                    target.addPotionEffect(PotionEffectType.STRENGTH.createEffect(300, 0));
                    target.sendMessage(dess("You have been buffed by ").append(player.displayName()));
                }
            });
            if (modifier == 0) return;
            playersNear.forEach(target -> {
                UUID targetId = target.getUniqueId();
                if (TrustManager.isMutual(casterId, targetId)) {
                    target.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(400, 2));
                    target.addPotionEffect(PotionEffectType.SPEED.createEffect(160, 2));
                    target.addPotionEffect(PotionEffectType.STRENGTH.createEffect(400, 1));

                }
            });
        } else {
            //TODO add logic
        }
    }

    /**
     * Registers a sprite for the target player by applying a health boost effect,
     * displaying particles and sounds, spawning a custom entity, and setting up
     * entity tracking and behavior as a follower for the target.
     *
     * @param target The player who is the target of the sprite registration.
     *               Effects, sounds, particles, and a tamed wolf are applied/spawned for this player.
     * @param player The player who owns the sprite. The wolf entity is named after this player,
     *               and their name is displayed above the sprite.
     */
    public void registerSprite(Player target, Player player) {
        target.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(80, 2));
        Location location = target.getLocation();
        target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        target.getWorld().playSound(location, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 1.5f);
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

    /**
     * A Random object used to generate pseudo-random numbers in the class.
     * This field provides utility for various randomized operations
     * within the functionality of the containing class.
     */
    Random random = new Random();

    /**
     * Creates a BukkitRunnable responsible for tracking and managing the behavior of an entity
     * that acts as a follower for a player. The follower's behavior includes periodic particle
     * and sound effects, ensuring the entity follows its owner, and applying status effects.
     * The runnable also monitors certain conditions to cancel the task and despawn the follower
     * when required.
     *
     * @param tracker  The {@link EntityTracker} object responsible for tracking and managing the follower entity.
     * @param entity   The {@link Entity} acting as the follower. Typically, this is a tamed {@link Wolf}.
     * @param player   The {@link Player} that*/
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

    /**
     * Provides the related item associated with this class or functionality.
     * This method is overridden to define the specific ItemStack that represents
     * the corresponding item, which is meant to symbolize or identify certain
     * behavior or characteristics within the context of the system.
     *
     * @return An {@link ItemStack} instance of type {@link Material#STICK},
     *         representing the related item defined for the implementing class.
     */
    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.STICK);
    }

    /**
     * Handles the EntityDeath event and performs specific operations when certain conditions are met.
     * This method checks if the entity that died has a persistent data tag with the key "sprite" and,
     * if so, updates relevant data for the player associated with the entity.
     *
     * @param event The EntityDeathEvent triggered when an entity dies. Provides access
     *              to the entity and other event-related data.
     */
    @EventHandler
    public void EntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity.getPersistentDataContainer().has(keygen("sprite"))) {
            Player player = Bukkit.getPlayer(entity.getName().replace("'s Sprite",""));
            player.getPersistentDataContainer().set(keygen("sprite_active"),PersistentDataType.BOOLEAN,false);
        }
    }

    /**
     * Provides the name of the second ability associated with this class or functionality.
     * The returned name is styled using MiniMessage formatting and includes details about
     * the ability's nature, indicating it is a short-range healing ability.
     *
     * @return A {@link Component} representing the formatted name of Ability Two.
     */
    @Override
    public Component AbilityTwoName() {
        return dess("<green>Ability Two</green> - Short Range Healing");
    }

    /**
     * Provides the description of the second ability associated with this class or functionality.
     * The description highlights the ability's feature of quickly healing trusted individuals.
     *
     * @return A {@link Component} representing the formatted description of Ability Two.
     */
    @Override
    public Component AbilityTwoDescription() {
        return dess("Heal those you trust quickly");
    }


    /**
     * Ability Two - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability Two for
     * @param ability_item ItemStack used in ability usage
     * @throws MineSkinException if there's an error with MineSkin
     * @throws DataRequestException if there's an error with the data request
     * links back to ability two
     */
    @Override
 public void AbilityTwo(Player player, ItemStack ability_item) {
        AbilityTwo(player, ability_item, false);
    }

    /**
     * Ability Two - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability Two for
     * @param ability_item ItemStack used in ability usage
     * @param is_imbue checking if it's imbued
     */
    @Override
 public void AbilityTwo(Player player, ItemStack ability_item, boolean is_imbue) {
        //
        if (can_ability(AbilityTwo_Cooldown(),abilityTwoCooldowns,player.getUniqueId())) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.8f);
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                player.getWorld().spawnParticle(Particle.COMPOSTER, player.getLocation().add(0, 1, 0), 30, 2.0, 1.0, 2.0, 0.1);
                player.getLocation().getNearbyEntitiesByType(Player.class, 10).forEach(iterator -> {
                        if ((TrustManager.isTrustedBy(iterator, player) || TrustManager.isTrustedByName(iterator, player.getName())) && iterator.getLocation().distanceSquared(player.getLocation()) <= 100) {
                            iterator.addPotionEffect(PotionEffectType.INSTANT_HEALTH.createEffect(1, 0));
                            iterator.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, iterator.getLocation(),15, 0.3, 0.5, 0.3, 0.05);
                            iterator.getWorld().playSound(iterator.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
                            if (!player.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                                player.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(120, 0));
                            } else {
                                int amplifier = player.getPotionEffect(PotionEffectType.SLOWNESS).getAmplifier();
                                player.removePotionEffect(PotionEffectType.SLOWNESS);
                                player.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(120, amplifier + 1));
                            }

                        }
                });
                tick++;
                if (tick == 2) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Freedom.get_plugin(),0,80);
        abilityTwoCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    } else {
        double seconds = (double) (effective_cooldown(AbilityTwo_Cooldown(), player.getUniqueId()) - (System.currentTimeMillis() - abilityTwoCooldowns.get(player.getUniqueId()))) / 1000;
        player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }
    }

    /**
     * Passive Description - Provides a description of the passive ability associated with this class or functionality.
     * @return A {@link Component} representing the formatted description of the Passive.
     */
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
            final World world = player.getWorld();
            @Override
            public void run() {
                if (soulListener.canAbility == false) {
                    this.cancel();
                }
              for (Player iterator : world.getPlayers()) {
                  if (iterator != player) {
                      if ((TrustManager.isTrustedBy(iterator, player) || TrustManager.isTrustedByName(iterator, player.getName())) && iterator.getLocation().distanceSquared(player.getLocation()) <= 100) {
                          iterator.addPotionEffect(PotionEffectType.REGENERATION.createEffect(80, 0));
                      }
                  }
              }
            }
        };
    }

    @Override
    public Component ActivePassive_Description() {
        return dess("Regeneration for yourself for a brief period");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 30000;
    }

    @Override
    public long AbilityOne_Cooldown(Object obj) {
        return 30000;
    }

    @Override
    public void ActivePassive(Player player) {
        if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 3000, 0));
            double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, SoulPoints - 5);
        }
    }
}


