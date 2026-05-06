package xyz.yaszu.freedom.Util;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A flexible and efficient bullet system for all types of guns.
 * Supports configurable particles, damage, speed, charge mechanics, and callbacks.
 */
public class BulletSystem implements Listener {

    // Track charging players
    private static final Map<UUID, ChargeData> CHARGING_PLAYERS = new HashMap<>();

    // Track ammo and reload state per player
    private static final Map<UUID, ReloadData> RELOAD_PLAYERS = new HashMap<>();

    /**
     * Internal class to track charge state per player
     */
    private static class ChargeData {
        UUID playerId;
        Location chargeStartPosition;
        ItemStack chargeItem;
        int chargeTicksRemaining;
        Consumer<Integer> chargeCallback;
        BukkitRunnable chargeTask;

        ChargeData(UUID playerId, Location pos, ItemStack item, int ticks, Consumer<Integer> callback) {
            this.playerId = playerId;
            this.chargeStartPosition = pos.clone();
            this.chargeItem = item.clone();
            this.chargeTicksRemaining = ticks;
            this.chargeCallback = callback;
        }
    }

    /**
     * Internal class to track reload state per player
     */
    private static class ReloadData {
        UUID playerId;
        int currentAmmo;
        int maxAmmo;
        long reloadStartTime;
        int reloadDurationTicks;
        boolean isReloading;
        Consumer<Integer> reloadCallback;

        ReloadData(UUID playerId, int maxAmmo) {
            this.playerId = playerId;
            this.currentAmmo = maxAmmo;
            this.maxAmmo = maxAmmo;
            this.reloadStartTime = 0;
            this.reloadDurationTicks = 0;
            this.isReloading = false;
            this.reloadCallback = null;
        }
    }

    /**
     * Configuration class for bullet behavior
     */
    public static class BulletConfig {
        public double damage = 10.0;
        public double speed = 0.5;
        public double collisionRadius = 0.5;
        public double maxRange = 100.0;
        public Particle particle = Particle.CRIT;
        public Particle.DustOptions dustOptions = null;
        public int particleCount = 1;
        public Sound shootSound = null;
        public float shootSoundVolume = 1.0f;
        public float shootSoundPitch = 1.0f;
        public Sound chargeSound = null;
        public float chargeSoundVolume = 0.5f;
        public float chargeSoundPitch = 1.0f;
        public Sound hitSound = Sound.ENTITY_PLAYER_HURT;
        public float hitSoundVolume = 1.0f;
        public float hitSoundPitch = 1.0f;
        public boolean breaksBlocks = false;
        public Material blockMaterial = Material.AIR;
        public int charge = 0;
        public Consumer<Integer> chargeCallback = null;
        public int ammoPerShot = 1;
        public int maxAmmo = 30;
        public int reloadTimeTicks = 40;
        public Consumer<Integer> reloadCallback = null;
        public Consumer<LivingEntity> onEntityHit = null;
        public Consumer<Location> onBlockHit = null;
        public Consumer<Location> onMaxRangeReached = null;

        public BulletConfig() {}

        public BulletConfig damage(double damage) { this.damage = damage; return this; }
        public BulletConfig speed(double speed) { this.speed = speed; return this; }
        public BulletConfig collisionRadius(double radius) { this.collisionRadius = radius; return this; }
        public BulletConfig maxRange(double range) { this.maxRange = range; return this; }
        public BulletConfig particle(Particle particle) { this.particle = particle; return this; }
        public BulletConfig dustOptions(Particle.DustOptions options) { this.dustOptions = options; return this; }
        public BulletConfig particleCount(int count) { this.particleCount = count; return this; }
        public BulletConfig shootSound(Sound sound, float volume, float pitch) {
            this.shootSound = sound;
            this.shootSoundVolume = volume;
            this.shootSoundPitch = pitch;
            return this;
        }
        public BulletConfig chargeSound(Sound sound, float volume, float pitch) {
            this.chargeSound = sound;
            this.chargeSoundVolume = volume;
            this.chargeSoundPitch = pitch;
            return this;
        }
        public BulletConfig hitSound(Sound sound, float volume, float pitch) {
            this.hitSound = sound;
            this.hitSoundVolume = volume;
            this.hitSoundPitch = pitch;
            return this;
        }
        public BulletConfig breaksBlocks(boolean breaks) { this.breaksBlocks = breaks; return this; }
        public BulletConfig blockMaterial(Material material) { this.blockMaterial = material; return this; }
        public BulletConfig charge(int chargeTicksDuration) { this.charge = chargeTicksDuration; return this; }
        public BulletConfig chargeCallback(Consumer<Integer> callback) { this.chargeCallback = callback; return this; }
        public BulletConfig ammoPerShot(int ammo) { this.ammoPerShot = ammo; return this; }
        public BulletConfig maxAmmo(int max) { this.maxAmmo = max; return this; }
        public BulletConfig reloadTimeTicks(int ticks) { this.reloadTimeTicks = ticks; return this; }
        public BulletConfig reloadCallback(Consumer<Integer> callback) { this.reloadCallback = callback; return this; }
        public BulletConfig onEntityHit(Consumer<LivingEntity> callback) { this.onEntityHit = callback; return this; }
        public BulletConfig onBlockHit(Consumer<Location> callback) { this.onBlockHit = callback; return this; }
        public BulletConfig onMaxRangeReached(Consumer<Location> callback) { this.onMaxRangeReached = callback; return this; }
    }

    /**
     * Fires a bullet from a player in the direction they're looking
     * @param player The player firing the bullet
     * @param config The bullet configuration
     * @return The BukkitRunnable task (already scheduled)
     */
    public static BukkitRunnable fireBullet(Player player, BulletConfig config) {
        // Check if mainhand is empty
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            return new BukkitRunnable() { @Override public void run() {} };
        }

        // Check if offhand is NOT empty (if it has something, prevent firing)
        if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
            return new BukkitRunnable() { @Override public void run() {} };
        }

        // Check ammo system
        UUID playerId = player.getUniqueId();
        ReloadData reloadData = getOrCreateReloadData(playerId, config.maxAmmo);

        // Check if reloading
        if (reloadData.isReloading) {
            long reloadElapsed = System.currentTimeMillis() - reloadData.reloadStartTime;
            long reloadTotal = reloadData.reloadDurationTicks * 50L; // 50ms per tick

            if (reloadElapsed < reloadTotal) {
                long secondsRemaining = (reloadTotal - reloadElapsed + 999) / 1000;
                player.sendActionBar(Util.dess("<color:#ff0000>Reloading... " + secondsRemaining + "s"));
                return new BukkitRunnable() { @Override public void run() {} };
            } else {
                // Reload complete
                reloadData.isReloading = false;
                reloadData.currentAmmo = reloadData.maxAmmo;
            }
        }

        // Check if out of ammo
        if (reloadData.currentAmmo <= 0) {
            startReload(player, config);
            player.sendActionBar(Util.dess("<color:#ff0000>Out of ammo! Reloading..."));
            return new BukkitRunnable() { @Override public void run() {} };
        }

        // Handle charging
        if (config.charge > 0) {
            return startChargeSequence(player, config);
        }

        // Fire and consume ammo
        BukkitRunnable result = fireBullet(player, player.getEyeLocation(), player.getLocation().getDirection(), config);
        reloadData.currentAmmo -= config.ammoPerShot;

        if (reloadData.currentAmmo < 0) {
            reloadData.currentAmmo = 0;
        }

        return result;
    }

    /**
     * Starts the charge sequence, returns a runnable that completes when charge is done
     */
    private static BukkitRunnable startChargeSequence(Player player, BulletConfig config) {
        UUID playerId = player.getUniqueId();

        // Cancel any existing charge
        if (CHARGING_PLAYERS.containsKey(playerId)) {
            ChargeData data = CHARGING_PLAYERS.get(playerId);
            if (data.chargeTask != null) {
                data.chargeTask.cancel();
            }
            CHARGING_PLAYERS.remove(playerId);
        }

        ChargeData chargeData = new ChargeData(playerId, player.getLocation(), player.getInventory().getItemInMainHand(), config.charge, config.chargeCallback);
        CHARGING_PLAYERS.put(playerId, chargeData);

        BukkitRunnable chargeRunnable = new BukkitRunnable() {
            boolean firstRun = true;
            
            @Override
            public void run() {
                ChargeData data = CHARGING_PLAYERS.get(playerId);
                if (data == null) {
                    this.cancel();
                    Player p = Bukkit.getPlayer(playerId);
                    if (p != null && p.isOnline()) {
                        p.removePotionEffect(PotionEffectType.SLOWNESS);
                    }
                    return;
                }

                Player p = Bukkit.getPlayer(playerId);
                if (p == null || !p.isOnline()) {
                    CHARGING_PLAYERS.remove(playerId);
                    this.cancel();
                    return;
                }
                
                // Play charge sound on first tick
                if (firstRun) {
                    firstRun = false;
                    if (config.chargeSound != null) {
                        p.getWorld().playSound(p.getLocation(), config.chargeSound, config.chargeSoundVolume, config.chargeSoundPitch);
                    }
                    // Apply slowness effect while charging
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1, false, false));
                }

                // Check if offhand now has something
                if (p.getInventory().getItemInOffHand().getType() != Material.AIR) {
                    CHARGING_PLAYERS.remove(playerId);
                    this.cancel();
                    p.removePotionEffect(PotionEffectType.SLOWNESS);
                    return;
                }

                // Check if mainhand item changed
                if (!itemsEqual(p.getInventory().getItemInMainHand(), data.chargeItem)) {
                    CHARGING_PLAYERS.remove(playerId);
                    this.cancel();
                    p.removePotionEffect(PotionEffectType.SLOWNESS);
                    return;
                }

                // Decrease charge counter
                data.chargeTicksRemaining--;

                // Call charge callback
                if (data.chargeCallback != null) {
                    data.chargeCallback.accept(config.charge - data.chargeTicksRemaining);
                }

                // When charge is complete, fire the bullet
                if (data.chargeTicksRemaining <= 0) {
                    CHARGING_PLAYERS.remove(playerId);
                    this.cancel();
                    
                    // Remove slowness effect
                    p.removePotionEffect(PotionEffectType.SLOWNESS);

                    // Fire the bullet with the charged damage
                    fireBullet(p, p.getEyeLocation(), p.getLocation().getDirection(), config);
                    return;
                }
            }
        };

        chargeData.chargeTask = chargeRunnable;
        chargeRunnable.runTaskTimer(Bukkit.getPluginManager().getPlugin("Freedom"), 0, 1);

        return chargeRunnable;
    }

    /**
     * Checks if two ItemStacks are equal (ignoring amount)
     */
    private static boolean itemsEqual(ItemStack item1, ItemStack item2) {
        if (item1 == null && item2 == null) return true;
        if (item1 == null || item2 == null) return false;
        return item1.getType() == item2.getType();
    }

    /**
     * Fires a bullet from a specific location in a specific direction
     * @param firer The player who fired the bullet (for damage and collision checks)
     * @param startLocation The starting location of the bullet
     * @param direction The direction the bullet travels
     * @param config The bullet configuration
     * @return The BukkitRunnable task (already scheduled)
     */
    public static BukkitRunnable fireBullet(Player firer, Location startLocation, Vector direction, BulletConfig config) {
        BukkitRunnable runnable = new BukkitRunnable() {
            Location bulletLocation = startLocation.clone();
            double distanceTraveled = 0;
            boolean hasHit = false;
            boolean firstRun = true;

            @Override
            public void run() {
                // Play shoot sound on first run
                if (firstRun) {
                    firstRun = false;
                    Bukkit.getLogger().info("[BulletSystem] Firing bullet from " + firer.getName() +
                            " with collision radius: " + config.collisionRadius + "m, damage: " + config.damage);
                    if (config.shootSound != null) {
                        firer.getWorld().playSound(bulletLocation, config.shootSound, config.shootSoundVolume, config.shootSoundPitch);
                    }
                }

                if (hasHit || distanceTraveled >= config.maxRange) {
                    if (!hasHit && config.onMaxRangeReached != null) {
                        config.onMaxRangeReached.accept(bulletLocation);
                    }
                    this.cancel();
                    return;
                }

                // Move bullet forward
                bulletLocation.add(direction.clone().multiply(config.speed));
                distanceTraveled += config.speed;

                // Spawn particles
                spawnBulletParticle(bulletLocation, config);

                // Check for entity collisions
                java.util.Collection<Entity> nearbyEntities = bulletLocation.getNearbyEntities(config.collisionRadius, config.collisionRadius, config.collisionRadius);
                if (!nearbyEntities.isEmpty()) {
                    Bukkit.getLogger().info("[BulletSystem] Found " + nearbyEntities.size() + " nearby entities at " +
                            bulletLocation.getBlockX() + "," + bulletLocation.getBlockY() + "," + bulletLocation.getBlockZ());
                }
                for (Entity entity : nearbyEntities) {

                    if (entity instanceof LivingEntity && entity != firer) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply distance check consistently for all entities (convert AABB to sphere)
                        double distanceSquared = bulletLocation.distanceSquared(entity.getLocation());
                        double maxDistanceSquared = config.collisionRadius * config.collisionRadius;

                        // Verbose logging
                        Bukkit.getLogger().info("[BulletSystem] Hit detection for " + entity.getName() +
                                " | Distance: " + String.format("%.2f", Math.sqrt(distanceSquared)) + "m | " +
                                "Max Distance: " + config.collisionRadius + "m | " +
                                "Bullet Loc: " + bulletLocation.getBlockX() + "," + bulletLocation.getBlockY() + "," + bulletLocation.getBlockZ() + " | " +
                                "Entity Loc: " + entity.getLocation().getBlockX() + "," + entity.getLocation().getBlockY() + "," + entity.getLocation().getBlockZ());

                        if (distanceSquared > maxDistanceSquared) {
                            Bukkit.getLogger().info("[BulletSystem] MISSED - Distance too far: " + String.format("%.2f", Math.sqrt(distanceSquared)) + "m > " + config.collisionRadius + "m");
                            continue;
                        }

                        // Apply damage
                        target.damage(config.damage, firer);
                        Bukkit.getLogger().info("[BulletSystem] HIT! Dealt " + config.damage + " damage to " + target.getName());

                        // Play sound
                        if (config.hitSound != null) {
                            target.getWorld().playSound(target.getLocation(), config.hitSound, config.hitSoundVolume, config.hitSoundPitch);
                        }

                        // Call custom callback
                        if (config.onEntityHit != null) {
                            config.onEntityHit.accept(target);
                        }

                        hasHit = true;
                        this.cancel();
                        return;
                    }
                }

                // Check for block collisions
                if (bulletLocation.getBlock().getType() != Material.AIR) {
                    if (config.breaksBlocks && bulletLocation.getBlock().getBreakSpeed(firer) < 25) {
                        bulletLocation.getBlock().breakNaturally();
                    }

                    // Call custom callback
                    if (config.onBlockHit != null) {
                        config.onBlockHit.accept(bulletLocation);
                    }

                    hasHit = true;
                    this.cancel();
                }
            }
        };

        // Schedule the task
        runnable.runTaskTimer(Bukkit.getPluginManager().getPlugin("Freedom"), 0, 1);
        return runnable;
    }

    /**
     * Spawns a particle at the bullet location
     */
    private static void spawnBulletParticle(Location location, BulletConfig config) {
        if (config.particle == Particle.DUST && config.dustOptions != null) {
            location.getWorld().spawnParticle(config.particle, location, config.particleCount, 0, 0, 0, 0, config.dustOptions);
        } else {
            location.getWorld().spawnParticle(config.particle, location, config.particleCount);
        }
    }

    /**
     * Checks if a player is currently charging a weapon
     * @param player The player to check
     * @return true if the player is charging, false otherwise
     */
    public static boolean isCharging(Player player) {
        return CHARGING_PLAYERS.containsKey(player.getUniqueId());
    }

    /**
     * Stops charging for a player and removes all slowness effects
     * @param player The player to stop charging
     */
    public static void stopCharging(Player player) {
        UUID playerId = player.getUniqueId();
        if (CHARGING_PLAYERS.containsKey(playerId)) {
            ChargeData data = CHARGING_PLAYERS.get(playerId);
            if (data.chargeTask != null) {
                data.chargeTask.cancel();
            }
            CHARGING_PLAYERS.remove(playerId);
            
            // Remove slowness effect
            player.removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }

    // ========== AMMO & RELOAD SYSTEM ==========

    /**
     * Gets or creates reload data for a player
     */
    private static ReloadData getOrCreateReloadData(UUID playerId, int maxAmmo) {
        if (!RELOAD_PLAYERS.containsKey(playerId)) {
            RELOAD_PLAYERS.put(playerId, new ReloadData(playerId, maxAmmo));
        }
        return RELOAD_PLAYERS.get(playerId);
    }

    /**
     * Initializes the ammo system for a player
     * @param player The player
     * @param maxAmmo The maximum ammo capacity
     */
    public static void initializeAmmo(Player player, int maxAmmo) {
        getOrCreateReloadData(player.getUniqueId(), maxAmmo);
    }

    /**
     * Gets the current ammo for a player
     * @param player The player
     * @return Current ammo count
     */
    public static int getAmmo(Player player) {
        ReloadData data = RELOAD_PLAYERS.get(player.getUniqueId());
        return data != null ? data.currentAmmo : 0;
    }

    /**
     * Sets the ammo for a player
     * @param player The player
     * @param ammo The new ammo count
     */
    public static void setAmmo(Player player, int ammo) {
        ReloadData data = RELOAD_PLAYERS.get(player.getUniqueId());
        if (data != null) {
            data.currentAmmo = Math.min(ammo, data.maxAmmo);
        }
    }

    /**
     * Resets ammo to max for a player
     * @param player The player
     */
    public static void resetAmmo(Player player) {
        ReloadData data = RELOAD_PLAYERS.get(player.getUniqueId());
        if (data != null) {
            data.currentAmmo = data.maxAmmo;
        }
    }

    /**
     * Checks if a player is currently reloading
     * @param player The player
     * @return true if reloading, false otherwise
     */
    public static boolean isReloading(Player player) {
        ReloadData data = RELOAD_PLAYERS.get(player.getUniqueId());
        return data != null && data.isReloading;
    }

    /**
     * Starts a manual reload for a player
     * @param player The player
     * @param reloadTickDuration Duration of reload in ticks
     */
    public static void startReload(Player player, int reloadTickDuration) {
        BulletSystem.BulletConfig config = new BulletSystem.BulletConfig();
        config.reloadTimeTicks = reloadTickDuration;
        startReload(player, config);
    }

    /**
     * Starts a reload based on config
     */
    private static void startReload(Player player, BulletConfig config) {
        UUID playerId = player.getUniqueId();
        ReloadData data = RELOAD_PLAYERS.get(playerId);

        if (data == null) {
            data = new ReloadData(playerId, config.maxAmmo);
            RELOAD_PLAYERS.put(playerId, data);
        }

        data.isReloading = true;
        data.reloadStartTime = System.currentTimeMillis();
        data.reloadDurationTicks = config.reloadTimeTicks;
        data.reloadCallback = config.reloadCallback;
    }

    /**
     * Gets the max ammo for a player
     * @param player The player
     * @return Maximum ammo capacity
     */
    public static int getMaxAmmo(Player player) {
        ReloadData data = RELOAD_PLAYERS.get(player.getUniqueId());
        return data != null ? data.maxAmmo : 0;
    }

    /**
     * Checks if player can fire (has ammo and not reloading)
     * @param player The player
     * @return true if can fire, false otherwise
     */
    public static boolean canFire(Player player) {
        ReloadData data = RELOAD_PLAYERS.get(player.getUniqueId());
        return data != null && data.currentAmmo > 0 && !data.isReloading;
    }

    /**
     * Quick fire method with default configuration
     * @param player The player firing
     * @return The BukkitRunnable task (already scheduled)
     */
    public static BukkitRunnable quickFire(Player player) {
        return fireBullet(player, new BulletConfig());
    }

    /**
     * Convenience method for rapid-fire with custom damage
     * @param player The player firing
     * @param damage The damage to deal
     * @return The BukkitRunnable task (already scheduled)
     */
    public static BukkitRunnable quickFireDamage(Player player, double damage) {
        return fireBullet(player, new BulletConfig().damage(damage));
    }

    // ========== EVENT LISTENERS ==========

    /**
     * Handles player movement - cancels charge if position changes significantly
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (!CHARGING_PLAYERS.containsKey(playerId)) return;

        ChargeData data = CHARGING_PLAYERS.get(playerId);

        // Only cancel if position (block position) changes, not rotation
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {

            // Position changed, cancel charge
            if (data.chargeTask != null) {
                data.chargeTask.cancel();
            }
            CHARGING_PLAYERS.remove(playerId);
            event.getPlayer().removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }

    /**
     * Handles item held change - cancels charge if mainhand item changes
     */
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (!CHARGING_PLAYERS.containsKey(playerId)) return;

        ChargeData data = CHARGING_PLAYERS.get(playerId);

        // Check if the new held item is different from the charge item
        ItemStack newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (!itemsEqual(newItem, data.chargeItem)) {
            if (data.chargeTask != null) {
                data.chargeTask.cancel();
            }
            CHARGING_PLAYERS.remove(playerId);
            event.getPlayer().removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }

    /**
     * Prevents using offhand while charging or holding mainhand gun
     */
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        // Block swap if currently charging
        if (CHARGING_PLAYERS.containsKey(playerId)) {
            event.setCancelled(true);
            return;
        }


    }
}

