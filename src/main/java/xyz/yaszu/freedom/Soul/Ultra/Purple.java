package xyz.yaszu.freedom.Soul.Ultra;

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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base.BasePurple;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Subsystems.TrustManager;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static xyz.yaszu.freedom.Util.Util.*;


public class Purple implements Base_Soul {




    @Override
    public String Name_For_Container() {
        return "Purple";
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

    public void drawCircle(Location center, double radius, World world, int points) {
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ() + (radius * Math.sin(angle)); // Calculate Z coordinate
            double y = center.getY(); // Y remains constant for a flat circle

            // Create a new location for the point
            Location pointLocation = new Location(world, x, y, z);

            // 2. Spawn particles
            world.spawnParticle(Particle.REVERSE_PORTAL, pointLocation, 1);
        }
    }
    BasePurple purple = new BasePurple();
    @Override
 public void AbilityOne(Player player) {
        AbilityOne(player, false);
    }

    @Override
 public void AbilityOne(Player player, boolean is_imbue) {
        purple.AbilityOne(player);
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
        if (can_ability(AbilityTwo_Cooldown(), abilityTwoCooldowns, player.getUniqueId())) {

            player.setVelocity(player.getLocation().getDirection().multiply(-1.2));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 10f, 0f);
//      player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation().add(player.getLocation().getDirection()), 1);
            drawPlayerTintedDisplay(true,2,player,10,null,player.getEyeLocation().clone().add(player.getLocation().getDirection().multiply(1.5)),Color.PURPLE,4,6);
            drawPlayerTintedDisplay(true,2,player,10,null,player.getEyeLocation().clone().add(player.getLocation().getDirection().multiply(1.5)),Color.YELLOW,2,1);
            handleSnipe(player).runTaskTimer(Bukkit.getPluginManager().getPlugin("Freedom"), 0, 0);
            abilityTwoCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            double seconds = (double) (effective_cooldown(AbilityTwo_Cooldown(), player.getUniqueId()) - (System.currentTimeMillis() - abilityTwoCooldowns.get(player.getUniqueId()))) / 1000;
            player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
        }
    }
    public BukkitRunnable handleSnipe(Player player) {
        return new BukkitRunnable() {
            final Vector direction = player.getLocation().getDirection();
            final World world = player.getWorld();
            int piercing = 0;
            final int piercing_max = 5;
            @Override
            public void run() {
                if (snipeLocation == null) {
                    snipeLocation = player.getLocation().add(0,1,0);
                }
                if (snipeLocation != player.getLocation()) {
                    world.spawnParticle(Particle.REVERSE_PORTAL,snipeLocation,30);
                }

                snipeLocation.add(direction);
                for (Entity inst : snipeLocation.getNearbyEntities(4,4,4)) {
                    if (inst instanceof Player) {
                        if (inst != player) {
                            if (inst.getLocation().distanceSquared(snipeLocation) <= 4) {
                                LivingEntity entity = (LivingEntity) inst;
                                dealSnipeDamage(entity);
                                this.cancel();
                            }
                        }
                    } else {
                        if (inst instanceof LivingEntity entity) {
                        if (inst.getLocation().distanceSquared(snipeLocation) <= 4) {
                            if (inst instanceof Tameable tameable) {
                                if (tameable.isTamed() && (tameable.getOwnerUniqueId() == player.getUniqueId() || TrustManager.isMutual(tameable.getOwnerUniqueId(),player.getUniqueId()))) {
                                    return;
                                }
                            }
                            dealSnipeDamage(entity);
//                            entity.damage(16 + player.getLocation().distance(snipeLocation) * 2.75, player);
                            this.cancel();
                        }
                        }
                    }
                }
                if (snipeLocation.isBlock()) {
                    snipeLocation.getBlock().setType(Material.AIR);
                    this.cancel();
                }
            }

            public Location snipeLocation;
            private void dealSnipeDamage(LivingEntity entity) {
                //old
                entity.damage(16 + player.getLocation().distance(snipeLocation) * 2.75, player);
                // GHOST REVAMP
//                if (entity instanceof Player) {
//                    dealTrueDamage(entity,10);
//                } else {
//                    dealTrueDamage(entity,20);
//                }
                //VFX
                entity.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, entity.getLocation().add(0, 1, 0), 1);
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 1.2f);
                //piercing++;
                //if (piercing > piercing_max) {
                    this.cancel();
                //}
            }

        };

    }

    @Override
    public Component Passive_Description() {
        return dess("You gain more XP");
    }

    @Override
    public void Passive(Player player,Object Event) {
// null bc xp gain is kinda already added
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
        return purple.AbilityOne_Cooldown(obj);
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


