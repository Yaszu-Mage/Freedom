package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class Purple extends Util implements Base_Soul{

    public long ability_one_cooldown = 1000;
    public long ability_two_cooldown = 60000;

    public static HashMap<UUID, Long> ability_two_cooldowns = new HashMap<>();

    public static HashMap<UUID,Long> ability_one_cooldowns = new HashMap<UUID,Long>();
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

    @Override
    public void AbilityOne(Player player) {
        //
        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1f, 0f);
        if (can_ability(ability_one_cooldown,ability_one_cooldowns,player.getUniqueId())) {
            Vector velocity = player.getVelocity();
        drawCircle(player.getLocation().add(0,1,0), 1, player.getWorld(), 100);
        Location location = player.getLocation().add(player.getLocation().getDirection().multiply(5));
        //Location location = player.getLocation().add(player.getEyeLocation().getDirection().multiply(5));
        while (!location.getBlock().isEmpty() && !location.add(0,1,0).getBlock().isEmpty()) {

            location = location.add(0, 1, 0);
        }
        if (!location.add(0,1,0).getBlock().isEmpty()) {
            if (location.add(0,1,0).getBlock().getBreakSpeed(player) < 25) {
                location.add(0, 1, 0).getBlock().breakNaturally();
            }
        }

        player.teleport(location);
            world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1f, 0f);
        drawCircle(player.getLocation().add(0,1,0), 1, player.getWorld(), 100);
        ability_one_cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    } else {
            player.sendActionBar(dess("You can't use this ability yet"));
            double seconds = (double) (ability_one_cooldown - (System.currentTimeMillis() - ability_one_cooldowns.get(player.getUniqueId()))) / 1000;
            player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
        }
    }



    @Override
    public ItemStack Related_Item() {
        ItemStack workingItem = ItemStack.of(Material.CROSSBOW);

        CrossbowMeta workingMeta = (CrossbowMeta) workingItem.getItemMeta();
        workingMeta.setItemModel(NamespacedKey.minecraft("rifle"));
        workingMeta.getPersistentDataContainer().set(keygen("rifle"), PersistentDataType.BOOLEAN, true);
        workingMeta.setChargedProjectiles(List.of(ItemStack.of(Material.ARROW)));
        workingMeta.displayName(dess("<color:#3700ff>My</color> rifle"));
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
        if (can_ability(ability_two_cooldown,ability_two_cooldowns,player.getUniqueId())) {

        player.setVelocity(player.getLocation().getDirection().multiply(-1.2));
        player.getWorld().playSound(player.getLocation(),Sound.ENTITY_WARDEN_SONIC_BOOM,10f,0f);

        handleSnipe(player).runTaskTimer(Bukkit.getPluginManager().getPlugin("Freedom"), 0, 1);
        ability_two_cooldowns.put(player.getUniqueId(),System.currentTimeMillis());
    }
    else {
        double seconds = (double) (ability_two_cooldown - (System.currentTimeMillis() - ability_two_cooldowns.get(player.getUniqueId()))) / 1000;
        player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
    }
    }

    public BukkitRunnable handleSnipe(Player player) {
        return new BukkitRunnable() {
            Vector direction = player.getLocation().getDirection();
            World world = player.getWorld();
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
                    Freedom.get_plugin().getLogger().info(String.valueOf(inst.getLocation().distance(snipeLocation)));
                    if (inst instanceof Player) {
                        if (inst != player) {
                            if (inst.getLocation().distanceSquared(snipeLocation) <= 4) {
                                LivingEntity entity = (LivingEntity) inst;
                                entity.damage(14 + Math.sqrt(player.getLocation().distance(snipeLocation) / 6) * 2.75, player);
                                this.cancel();
                            }
                        }
                    } else {
                        if (inst.getLocation().distanceSquared(snipeLocation) <= 4) {
                            LivingEntity entity = (LivingEntity) inst;
                            entity.damage(14 + Math.sqrt(player.getLocation().distance(snipeLocation) / 6) * 2.75, player);
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
    public void ActivePassive(Player player) {
        Double soulpoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        if (soulpoints >= 5) {
            player.addPotionEffect(PotionEffectType.SLOW_FALLING.createEffect(60,1));
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, soulpoints - 5);
        }
    }
}
