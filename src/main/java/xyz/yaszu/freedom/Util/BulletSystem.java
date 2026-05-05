package xyz.yaszu.freedom.Util;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;

import java.util.function.Consumer;

/**
 * A flexible and efficient bullet system for all types of guns.
 * Supports configurable particles, damage, speed, and callbacks.
 */
public class BulletSystem {

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
        public Sound hitSound = Sound.ENTITY_PLAYER_HURT;
        public float hitSoundVolume = 1.0f;
        public float hitSoundPitch = 1.0f;
        public boolean breaksBlocks = false;
        public Material blockMaterial = Material.AIR;
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
        public BulletConfig hitSound(Sound sound, float volume, float pitch) {
            this.hitSound = sound;
            this.hitSoundVolume = volume;
            this.hitSoundPitch = pitch;
            return this;
        }
        public BulletConfig breaksBlocks(boolean breaks) { this.breaksBlocks = breaks; return this; }
        public BulletConfig blockMaterial(Material material) { this.blockMaterial = material; return this; }
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
        return fireBullet(player, player.getEyeLocation(), player.getLocation().getDirection(), config);
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
                for (Entity entity : bulletLocation.getNearbyEntities(config.collisionRadius, config.collisionRadius, config.collisionRadius)) {
                    if (entity instanceof LivingEntity && entity != firer) {
                        LivingEntity target = (LivingEntity) entity;

                        // Only hit players with distance check
                        if (entity instanceof Player && bulletLocation.distanceSquared(entity.getLocation()) > config.collisionRadius * config.collisionRadius) {
                            continue;
                        }

                        // Apply damage
                        target.damage(config.damage, firer);

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
}

