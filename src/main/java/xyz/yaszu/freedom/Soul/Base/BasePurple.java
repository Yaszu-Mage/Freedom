package xyz.yaszu.freedom.Soul.Base;

import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Subsystems.CombatTimer;
import xyz.yaszu.freedom.Subsystems.TrustManager;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static xyz.yaszu.freedom.Util.Util.*;


public class BasePurple implements Base_Soul {
    @Override
    public String Name_For_Container() {
        return "BasePurple";
    }

    @Override
    public Component Name() {
        return dess("<color:#3700ff>Purple</color>");
    }

    @Override
    public Component Description() {
        return dess("Your ideals grow like a library");
    }
    @Override
    public ItemStack Icon() {
        ItemStack workingItem = ItemStack.of(Material.DIAMOND);
        ItemMeta workingMeta = workingItem.getItemMeta();
        workingMeta.setItemModel(NamespacedKey.minecraft("purpleicon"));
        // fly perfect wings
        workingItem.setItemMeta(workingMeta);
        return workingItem;
    }
    @Override
    public Component AbilityOneName() {
        return dess("<color:#3700ff>Ability One</color> - Teleportation");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("Teleport 5 blocks in the direction you are looking at.");
    }

    @Override
 public void AbilityOne(Player player) {
        AbilityOne(player, false);
    }
    public static double dragTime = 600;

    @Override
 public void AbilityOne(Player player, boolean is_imbue) {
        if (can_ability(AbilityOne_Cooldown(player),abilityOneCooldowns,player.getUniqueId())) {
            World world = player.getWorld();
            world.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
            world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1f, 0f);
            Vector velocity = player.getVelocity();
            drawStar(player.getLocation().add(0,1,0), 1.4, player.getWorld(), 20, Particle.DUST, new Particle.DustOptions(Color.PURPLE, 3));
            drawStar(player.getLocation().add(0,1,0), 2, player.getWorld(), 20, Particle.DUST, new Particle.DustOptions(Color.YELLOW, 2));
            drawStar(player.getLocation().add(0,1,0), 0.5, player.getWorld(), 20, Particle.DUST, new Particle.DustOptions(Color.BLACK, 8));
        Location location = player.getLocation().add(player.getLocation().getDirection().multiply(5));
        List<Player> trustedNearby = getNearbyTrusted(player,1);

        //Location location = player.getLocation().add(player.getEyeLocation().getDirection().multiply(5));
        //Location location = player.getLocation().add(player.getEyeLocation().getDirection().multiply(5));
//        while (!location.getBlock().isEmpty() && !location.add(0,1,0).getBlock().isEmpty()) {
//
//            location = location.add(0, 1, 0);
//        }
        if (!location.add(0,1,0).getBlock().isEmpty()) {
            if (location.add(0,1,0).getBlock().getBreakSpeed(player) < 25) {
                location.add(0, 1, 0).getBlock().breakNaturally();
            }
        }
            if (!trustedNearby.isEmpty()) {
                trustedNearby.forEach(iterated -> {
                    iterated.teleport(location);
                    iterated.setVelocity(velocity.multiply(1.1).add(iterated.getLocation().getDirection()));
                });
                player.getPersistentDataContainer().set(keygen("purpledrag"), PersistentDataType.BOOLEAN, true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.getPersistentDataContainer().remove(keygen("purpledrag"));
                    }
                }.runTaskLater(Bukkit.getPluginManager().getPlugin("Freedom"), (long) dragTime);
            }
            if (!location.getWorld().getWorldBorder().isInside(location)) {
                player.sendMessage(dess("You can't teleport there"));
                return;
            }
        player.teleport(location);
        try {
            if (player.getNearbyEntities(2,5,2).size() == 1) {
                player.lookAt(player.getNearbyEntities(1, 1, 1).get(0).getLocation(), LookAnchor.EYES);
            }
        } catch (IndexOutOfBoundsException ignored) {

        }

        player.setVelocity(velocity.multiply(1.1).add(player.getLocation().getDirection()));
            world.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.8f);
            world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1f, 0f);
            drawIsoscelesTriangle(player.getLocation(), 0.8, player.getWorld(), 16, Particle.DUST, new Particle.DustOptions(Color.BLACK, 8));
        drawIsoscelesTriangle(player.getLocation(), 1.5, player.getWorld(), 16, Particle.DUST, new Particle.DustOptions(Color.PURPLE, 4));
            drawIsoscelesTriangle(player.getLocation(), 2.3, player.getWorld(), 16, Particle.DUST, new Particle.DustOptions(Color.YELLOW, 3));
        abilityOneCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            double seconds = (double) (effective_cooldown(AbilityOne_Cooldown(player), player.getUniqueId()) - (System.currentTimeMillis() - abilityOneCooldowns.get(player.getUniqueId()))) / 1000;
            player.sendActionBar(dess("You can't use this ability yet, wait " + Math.round(seconds) + " seconds"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }
    @Override
    public ItemStack Related_Item() {
        ItemStack workingItem = ItemStack.of(Material.CROSSBOW);
        CrossbowMeta workingMeta = (CrossbowMeta) workingItem.getItemMeta();
        workingMeta.setItemModel(NamespacedKey.minecraft("rifle"));
        workingMeta.getPersistentDataContainer().set(keygen("rifle"), PersistentDataType.BOOLEAN, true);
        workingMeta.setChargedProjectiles(List.of(ItemStack.of(Material.ARROW)));
        workingMeta.displayName(dess("<color:#3700ff>Rifle</color>"));
        workingItem.setItemMeta(workingMeta);
        return workingItem;
    }

    @Override
    public Component AbilityTwoName()
    {
        return dess("<color:#3700ff>Ability Two</color> - BANG ‼ ");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("Fire a small particle that can, at distances deal massive damage");
    }

    @Override
 public void AbilityTwo(Player player, ItemStack ability_item) {
        AbilityTwo(player, ability_item, false);
    }

    @Override
 public void AbilityTwo(Player player, ItemStack ability_item, boolean is_imbue) {
        if (can_ability(AbilityTwo_Cooldown(),abilityTwoCooldowns,player.getUniqueId())) {

        player.setVelocity(player.getLocation().getDirection().multiply(-1.2));
        player.getWorld().playSound(player.getLocation(),Sound.ENTITY_WARDEN_SONIC_BOOM,10f,0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
//      player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation().add(player.getLocation().getDirection()), 1);
        drawPlayerTintedDisplay(true,2,player,10,null,player.getEyeLocation().clone().add(player.getLocation().getDirection().multiply(1.5)),Color.PURPLE,4,6);
        drawPlayerTintedDisplay(true,2,player,10,null,player.getEyeLocation().clone().add(player.getLocation().getDirection().multiply(1.5)),Color.YELLOW,2,1);
        handleSnipe(player).runTaskTimer(Bukkit.getPluginManager().getPlugin("Freedom"), 0, 0);
        abilityTwoCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
        } else {
            double seconds = (double) (effective_cooldown(AbilityTwo_Cooldown(), player.getUniqueId()) - (System.currentTimeMillis() - abilityTwoCooldowns.get(player.getUniqueId()))) / 1000;
            player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
}




    public BukkitRunnable handleSnipe(Player player) {
        return new BukkitRunnable() {
            Vector direction = player.getLocation().getDirection();
            World world = player.getWorld();
            int piercing = 0;
            int piercing_max = 5;
            @Override
            public void run() {
                if (snipeLocation == null) {
                    snipeLocation = player.getLocation().add(0,1,0);
                }
                if (snipeLocation != player.getLocation()) {
                    world.spawnParticle(Particle.REVERSE_PORTAL,snipeLocation,30, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.WITCH, snipeLocation, 5, 0.05, 0.05, 0.05, 0.02);
                } else {
                    createVerticleMinMagicCircle(snipeLocation.clone().add(player.getLocation().getDirection()),15, SoulTypes.Purple,player.getLocation().getYaw(),player.getLocation(),100,0.25);
                }
                snipeLocation.add(direction);
                for (Entity inst : snipeLocation.getNearbyEntities(4,4,4)) {
                    if (inst instanceof Player) {
                        if (inst != player) {
                            if (inst.getLocation().distanceSquared(snipeLocation) <= 4) {
                                LivingEntity entity = (LivingEntity) inst;
                                dealSnipeDamage(entity);
                            }
                        }
                    } else {
                        if (inst.getLocation().distanceSquared(snipeLocation) <= 4) {
                            if (inst instanceof Tameable tameable) {
                                if (tameable.isTamed() && (tameable.getOwnerUniqueId() == player.getUniqueId() || TrustManager.isMutual(tameable.getOwnerUniqueId(),player.getUniqueId()))) {
                                    return;
                                }
                            }
                            if (inst instanceof LivingEntity entity) {
                                dealSnipeDamage(entity);
                            }

                        }
                    }
                }

                if (snipeLocation.isBlock()) {
                    snipeLocation.getBlock().setType(Material.AIR);
                    this.cancel();
                }
            }

            private void dealSnipeDamage(LivingEntity entity) {
                //old
                 entity.damage(4.5 + player.getLocation().distance(snipeLocation)/2, player);
                // GHOST REVAMP
//                if (entity instanceof Player) {
//                    dealTrueDamage(entity,5);
//                } else {
//                    dealTrueDamage(entity,10);
//                }
                //VFX
                entity.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, entity.getLocation().add(0, 1, 0), 1);
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 1.2f);
//                piercing++;
//                if (piercing > piercing_max) {
                    this.cancel();
//                }
            }

            public Location snipeLocation;


        };
    }

    @Override
    public Component Passive_Description() {
        return dess("You gain more XP");
    }

    @Override
    public void Passive(Player player,Object Event) {

    }

    @Override
    public Component ActivePassive_Description() {
        return dess("You can remove Fall Damage at will");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 30000;
    }

    @Override
    public long AbilityOne_Cooldown(Object obj) {
        if (obj instanceof Player player) {
            PersistentDataContainer data = player.getPersistentDataContainer();
            Boolean hasTransportedAnother = data.get(keygen("purpledrag"), PersistentDataType.BOOLEAN);
            if (hasTransportedAnother == null) {
                if (CombatTimer.isCombat(player)) {
                    return 15000;
                }
                return 2500;
            } else if (hasTransportedAnother) {
                if (CombatTimer.isCombat(player)) {
                    return 25000;
                }
                return 10000;
            }
        }
            return 2500;
    }

    @Override
    public void ActivePassive(Player player) {
        Double soulpoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        if (soulpoints >= 5) {
            player.addPotionEffect(PotionEffectType.SLOW_FALLING.createEffect(60,1));
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, soulpoints - 5);
        }
    }
}


