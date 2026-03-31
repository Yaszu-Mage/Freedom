package xyz.yaszu.freedom.Util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;
import org.bukkit.*;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.SoulTypes;

import java.util.*;

public class Util {
    public static SkinsRestorer skinsRestorerAPI;

    public static NamespacedKey keygen(String key) {
        return new NamespacedKey(Bukkit.getPluginManager().getPlugin("Freedom"), key);
    }

    public static Component dess(String minimessage) {
        return MiniMessage.miniMessage().deserialize(minimessage);
    }

    public static void drawElipse(Location center, double radius, int points, Particle particle, double linearscale, double horizontalscale) {
        World world = center.getWorld();
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = (center.getX() + (horizontalscale * (radius * Math.cos(angle)))); // Calculate X coordinate
            double z = (center.getZ() + (linearscale * (radius * Math.sin(angle)))); // Calculate Z coordinate
            double y = center.getY(); // Y remains constant for a flat circle

            // Create a new location for the point
            Location pointLocation = new Location(world, x, y, z);
            // 2. Spawn particles
            if (particle == Particle.DUST) {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 2.0f));
            } else {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
            }
        }
    }

    public static void drawEye(Location location, int scale) {
        drawElipse(location, 1 + scale, 128, Particle.DUST, -2.58, 6.6);
        drawElipse(location, 1 + scale, 128, Particle.DUST, -2.58, 3.3);
        drawCircle(location, 2 + 2 * scale, location.getWorld(), 128, Particle.DUST, new Particle.DustOptions(Color.RED, 2.0f));
    }


    public static void drawCircle(Location center, double radius, World world, int points, Particle particle, Particle.DustOptions options) {
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ() + (radius * Math.sin(angle)); // Calculate Z coordinate
            double y = center.getY(); // Y remains constant for a flat circle

            // Create a new location for the point
            Location pointLocation = new Location(world, x, y, z);

            // 2. Spawn particles
            if (particle == Particle.DUST) {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 1, options);
            } else {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
            }
        }
    }

    public static void drawCircle(Location center, double radius, World world, int points, Particle particle) {
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ() + (radius * Math.sin(angle)); // Calculate Z coordinate
            double y = center.getY(); // Y remains constant for a flat circle

            // Create a new location for the point
            Location pointLocation = new Location(world, x, y, z);

            // 2. Spawn particles
            if (particle == Particle.DUST) {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 1, new Particle.DustOptions(Color.PURPLE, 2.0f));
            } else {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
            }
        }
    }

    public void createMinMagicCircleAroundPlayer(Player target, int tickRate) {
        SoulTypes soulType = SoulTypes.valueOf(target.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        Color color = Color.PURPLE;
        switch (soulType) {
            case Green, BaseGreen -> color = Color.GREEN;
            case Red, BaseRed -> color = Color.RED;
            case Yellow, BaseYellow -> color = Color.YELLOW;
            case Orange, BaseOrange -> color = Color.ORANGE;
            case BaseBlue, Blue -> color = Color.BLUE;
            case Black, BaseBlack -> color = Color.BLACK;
        }
        Color finalColor = color;
        new BukkitRunnable() {
            int tick = 1;

            @Override
            public void run() {
                multisquare(target.getLocation(), tick, 4, Particle.DUST, 90, new Particle.DustOptions(finalColor.setBlue(0), 1.5f));
                multisquare(target.getLocation(), tick, 10, Particle.DUST, 30, new Particle.DustOptions(finalColor, 1.5f));
                drawCircle(target.getLocation(), 1, target.getWorld(), 30, Particle.DUST, new Particle.DustOptions(Color.PURPLE, 2.0f));
                tick = tick + tickRate;
                if (tick >= 1000) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 10);
    }

    public void createMinMagicCircle(Location center, int tickRate, SoulTypes soulType) {
        Color color = Color.PURPLE;
        switch (soulType) {
            case Green, BaseGreen -> color = Color.GREEN;
            case Red, BaseRed -> color = Color.RED;
            case Yellow, BaseYellow -> color = Color.YELLOW;
            case Orange, BaseOrange -> color = Color.ORANGE;
            case BaseBlue, Blue -> color = Color.BLUE;
            case Black, BaseBlack -> color = Color.BLACK;
        }
        Color finalColor = color;
        if (center.clone().add(0, 0, 0).getBlock().getType() == Material.LECTERN) {
            Freedom.get_plugin().getLogger().info("LECTURN");
            new BukkitRunnable() {
                int tick = 1;

                @Override
                public void run() {
                    multisquare(center, tick, 4, Particle.DUST, 90, new Particle.DustOptions(finalColor.setBlue(0), 2f));
                    multisquare(center, tick, 10, Particle.DUST, 30, new Particle.DustOptions(finalColor, 2f));
                    if (tick % 3 == 0 || tick % 8 == 0) {
                        // multiple of 10
                        randompointoncircle(center, 30, 1, Particle.DUST);
                    }
                    drawCircle(center, 1, center.getWorld(), 30, Particle.DUST);
                    tick = tick + tickRate;

                    if (center.clone().add(0, 0, 0).getBlock().getType() != Material.LECTERN) {
                        this.cancel();
                    }
                    if (center.clone().add(0, 0, 0).getBlock().getType() == Material.LECTERN) {
                        Lectern lectern = (Lectern) center.clone().add(0, 0, 0).getBlock().getState();
                        if (lectern.getInventory().getItem(0) == null && tick > 4) {
                            this.cancel();
                        }

                    }

                }
            }.runTaskTimer(Freedom.get_plugin(), 5, 10);
        } else {
            new BukkitRunnable() {
                int tick = 1;

                @Override
                public void run() {
                    multisquare(center, tick, 4, Particle.DUST, 90, new Particle.DustOptions(finalColor.setBlue(0), 2f));
                    multisquare(center, tick, 10, Particle.DUST, 30, new Particle.DustOptions(finalColor, 2f));
                    if (tick % 3 == 0 || tick % 8 == 0) {
                        // multiple of 10
                        randompointoncircle(center, 30, 1, Particle.DUST);
                    }
                    drawCircle(center, 1, center.getWorld(), 30, Particle.DUST);
                    tick = tick + tickRate;
                    if (tick >= 1000) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Freedom.get_plugin(), 5, 10);
        }

    }

    public void createMaxMagicCircle(Location center, int tickRate, int scale, SoulTypes soulType) {
        Color color = Color.PURPLE;
        switch (soulType) {
            case Green, BaseGreen -> color = Color.GREEN;
            case Red, BaseRed -> color = Color.RED;
            case Yellow, BaseYellow -> color = Color.YELLOW;
            case Orange, BaseOrange -> color = Color.ORANGE;
            case BaseBlue, Blue -> color = Color.BLUE;
            case Black, BaseBlack -> color = Color.BLACK;
        }
        Color finalColor = color;
        new BukkitRunnable() {
            int tick = 1;

            @Override
            public void run() {
                multisquare(center, tick, 4 + scale, Particle.DUST, 90, new Particle.DustOptions(finalColor, 1.5f));
                multisquare(center, tick, 10 + scale, Particle.DUST, 30, new Particle.DustOptions(finalColor, 1.5f));
                drawCircle(center, 1 + ((double) scale / 2), center.getWorld(), 30 + (scale * 10), Particle.DUST);
                multisquare(center, tick, 17 + scale, Particle.DUST, 90, new Particle.DustOptions(finalColor, 1.5f));
                tick = tick + tickRate;
                if (center.clone().add(0, 0, 0).getBlock().getType() != Material.LECTERN) {
                    this.cancel();
                }
                if (center.clone().add(0, 0, 0).getBlock().getType() == Material.LECTERN) {
                    Lectern lectern = (Lectern) center.clone().add(0, 0, 0).getBlock().getState();
                    if (lectern.getInventory().getItem(0) == null && tick > 4) {
                        this.cancel();
                    }

                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 12);
    }

    public static Random random = new Random();

    public static void createRemoteExplosionParticles(Location center/* Center of Explosion*/, int tickrate/* rate of rotation*/, int size/* Power given from spell compiler*/) {
        new BukkitRunnable() {
            double size_modifier = 1;

            @Override
            public void run() {


                Location c1 = center.clone().add(-((size + random.nextFloat(0, 1)) * size_modifier), 0, -((size + random.nextFloat(0, 1)) * size_modifier));
                Location c2 = center.clone().add(((size + random.nextFloat(0, 1)) * size_modifier), 0, -(size * size_modifier));
                Location c3 = center.clone().add(((size + random.nextFloat(0, 1)) * size_modifier), 0, ((size + random.nextFloat(0, 1)) * size_modifier));
                Location c4 = center.clone().add(-((size + random.nextFloat(0, 1)) * size_modifier), 0, ((size + random.nextFloat(0, 1)) * size_modifier));
                //add rot
                int rot = tick;
                c1 = rotpointX(center, rot, c1);
                c2 = rotpointX(center, rot, c2);
                c3 = rotpointX(center, rot, c3);
                c4 = rotpointX(center, rot, c4);

                Location c1u = c1.clone().add(0, 30, 0);
                Location c2u = c2.clone().add(0, 30, 0);
                Location c3u = c3.clone().add(0, 30, 0);
                Location c4u = c4.clone().add(0, 30, 0);
                drawParticleLine(c1, c1u, c1.getWorld(), Particle.SOUL_FIRE_FLAME, new Particle.DustOptions(Color.AQUA, 2.0f), 0, 8, 0);
                drawParticleLine(c2, c2u, c2.getWorld(), Particle.SOUL_FIRE_FLAME, new Particle.DustOptions(Color.AQUA, 2.0f), 0, 8, 0);
                drawParticleLine(c3, c3u, c3.getWorld(), Particle.SOUL_FIRE_FLAME, new Particle.DustOptions(Color.AQUA, 2.0f), 0, 8, 0);
                drawParticleLine(c4, c4u, c4.getWorld(), Particle.SOUL_FIRE_FLAME, new Particle.DustOptions(Color.AQUA, 2.0f), 0, 8, 0);
                center.getWorld().playSound(center, Sound.ITEM_FLINTANDSTEEL_USE, 4, soundtick);
                tick = tick + tickrate;
                if (tick % 80 == 0) {
                    soundtick++;
                }
                size_modifier = size_modifier * 0.9;
                if (tick >= 550) {
                    for (int x = 0; x < 30; x = x + 5) {
                        drawSquare(center.clone().add(0, x, 0), ((float) size / 3) + ((float) x / 5), tick, Particle.DUST, new Particle.DustOptions(Color.WHITE, 2.0f + ((float) x / 5)), 0, 8, 0);
                        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 4, 1);

                    }
                    center.getWorld().spawnParticle(Particle.EXPLOSION, center, 30);
                    center.getWorld().createExplosion(center, size, true, true);
                    this.cancel();
                }
            }

            int soundtick = 0;
            int tick = 0;

        }.runTaskTimer(Freedom.get_plugin(), 0, 4);
    }


    public void createMaxMagicCircleAroundPlayer(Player target, int tickRate, int scale) {
        SoulTypes soulType = SoulTypes.valueOf(target.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        Color color = Color.PURPLE;
        switch (soulType) {
            case Green, BaseGreen -> color = Color.GREEN;
            case Red, BaseRed -> color = Color.RED;
            case Yellow, BaseYellow -> color = Color.YELLOW;
            case Orange, BaseOrange -> color = Color.ORANGE;
            case BaseBlue, Blue -> color = Color.BLUE;
            case Black, BaseBlack -> color = Color.BLACK;
        }
        Color finalColor = color;
        new BukkitRunnable() {
            int tick = 1;

            @Override
            public void run() {
                multisquare(target.getLocation(), tick, 4 + scale, Particle.DUST, 90, new Particle.DustOptions(finalColor, 8f));
                multisquare(target.getLocation(), tick, 10 + scale, Particle.DUST, 30, new Particle.DustOptions(finalColor, 8f));
                drawCircle(target.getLocation(), 1 + ((double) scale / 2), target.getWorld(), 30 + (scale * 10), Particle.DUST);
                multisquare(target.getLocation(), tick, 17 + scale, Particle.DUST, 90, new Particle.DustOptions(finalColor, 1.5f));
                tick = tick + tickRate;
                if (tick >= 1000) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 12);
    }

    public void randompointoncircle(Location center, int points, int radius, Particle particle) {
        World world = center.getWorld();
        Random random = new Random();
        int i = random.nextInt(points);
        double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
        double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
        double z = center.getZ() + (radius * Math.sin(angle)); // Calculate Z coordinate
        double y = center.getY(); // Y remains constant for a flat circle

        // Create a new location for the point
        Location pointLocation = new Location(world, x, y, z);

        // 2. Spawn particles
        if (particle == Particle.DUST) {
            Location location = pointLocation.clone();
            for (double iter = -1.0; iter <= 1.0; iter += 0.25) {

                world.spawnParticle(particle, pointLocation.clone().add(0, iter, 0), 1, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 2.0f));
            }
        } else {
            world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
        }
    }

    public void multisquare(Location location, int tick, int size, Particle particle, int initialrot, Particle.DustOptions options) {
        Color optionscolor = options.getColor();
        optionscolor = optionscolor.mixColors(Color.RED);
        Particle.DustOptions options1 = new Particle.DustOptions(optionscolor, options.getSize());
        drawSquare(location, size, initialrot + tick, particle, options, 0, 8, 0);
        drawSquare(location, size, initialrot - 45 + tick, particle, options1, 0, 8, 0);
    }

    // Example: Draw a 5x5 square on the ground around the player
    public static void drawSquare(Location center, double size, double rot, Particle particle, Particle.DustOptions options, double xlimit, double ylimit, double zlimit) {
        size = size / 2; // Half-size for a 5x5

        // Four corners
        Location c1 = center.clone().add(-size, 0, -size);
        Location c2 = center.clone().add(size, 0, -size);
        Location c3 = center.clone().add(size, 0, size);
        Location c4 = center.clone().add(-size, 0, size);
        //add rot
        c1 = rotpointX(center, rot, c1);
        c2 = rotpointX(center, rot, c2);
        c3 = rotpointX(center, rot, c3);
        c4 = rotpointX(center, rot, c4);
        // Draw lines between corners (simplified)
        drawParticleLine(c1, c2, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c2, c3, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c3, c4, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c4, c1, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
    }

    public static void drawVerticleSquare(Location center, double size, double rot, Particle particle, Particle.DustOptions options, double xlimit, double ylimit, double zlimit) {
        size = size / 2; // Half-size for a 5x5

        // Four corners
        Location c1 = center.clone().add(-size, -size, 0);
        Location c2 = center.clone().add(size, -size, 0);
        Location c3 = center.clone().add(size, size, 0);
        Location c4 = center.clone().add(-size, size, 0);
        //add rot
        c1 = rotpointZ(center, rot, c1);
        c2 = rotpointZ(center, rot, c2);
        c3 = rotpointZ(center, rot, c3);
        c4 = rotpointZ(center, rot, c4);
        // Draw lines between corners (simplified)
        drawParticleLine(c1, c2, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c2, c3, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c3, c4, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c4, c1, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
    }

    public static Location rotpointX(Location pivot, double angleDegrees, Location toRotate) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        // 1. Get relative vector
        Vector v = toRotate.clone().subtract(pivot).toVector();

        // 2. Rotate Vector (X and Z)
        double x = v.getX() * cos - v.getZ() * sin;
        double z = v.getX() * sin + v.getZ() * cos;

        // 3. Add pivot back
        return pivot.clone().add(new Vector(x, v.getY(), z));
    }

    public static Location rotpointZ(Location pivot, double angleDegrees, Location toRotate) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        // 1. Get relative vector
        Vector v = toRotate.clone().subtract(pivot).toVector();

        // 2. Rotate Vector (X and Z)
        double x = v.getX() * cos - v.getZ() * sin;
        double Y = v.getX() * sin + v.getZ() * cos;

        // 3. Add pivot back
        return pivot.clone().add(new Vector(x, v.getY(), Y));
    }

    public static Location rotpointY(Location pivot, double angleDegrees, Location toRotate) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        // 1. Get relative vector
        Vector v = toRotate.clone().subtract(pivot).toVector();

        // 2. Rotate Vector (X and Z)
        double x = v.getX() * cos - v.getY() * sin;
        double Y = v.getX() * sin + v.getY() * cos;

        // 3. Add pivot back
        return pivot.clone().add(new Vector(x, Y, v.getZ()));
    }

    public static void drawParticleLine(Location start, Location end, World world, Particle particle, Particle.DustOptions optionsdouble, double xlimit, double ylimit, double zlimit) {
        double space = 0.2; // Density of particles
        double distance = start.distance(end);
        Vector p1 = start.toVector();
        Vector p2 = end.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);

        for (double length = 0; length < distance; length += space) {

            p1.add(vector);
            if (length > xlimit) {
                if (particle == Particle.DUST) {
                    world.spawnParticle(particle, p1.toLocation(world), 1, 0, 0, 0, 1, optionsdouble);
                } else {
                    world.spawnParticle(particle, p1.toLocation(world), 1, 0, 0, 0, 0);
                }
            }
        }
    }

    public static enum ParticlePortalState {
        Opening,
        Open,
        Closing
    }
    public static void PortalParticleLifespan(Location center, Location targetLocation) {

        new BukkitRunnable() {
            int tick = 0;
            double lifespantick = 0;
            ParticlePortalState state = ParticlePortalState.Opening;
            @Override
            public void run() {
                switch (state) {
                    case Opening: {
                        if (lifespantick >= 2) {
                            state = ParticlePortalState.Open;
                        }
                        createPortalParticles(center,2-lifespantick,lifespantick * 4,0);
                        lifespantick = lifespantick + 0.5;
                        break;
                    }
                    case Open: {
                        lifespantick = 0;
                        createPortalParticles(center,0,4,0);
                        if (tick >= 100) {
                            state = ParticlePortalState.Closing;
                        }
                        for (Player player : center.getNearbyEntitiesByType(Player.class,2,2,2)) {
                            player.teleport(targetLocation);

                        }
                        break;
                    }
                    case Closing:
                        createPortalParticles(center,2 + lifespantick,lifespantick,0);
                        lifespantick = lifespantick + 1;
                        break;
                }
                if (tick >= 1200) {
                    this.cancel();
                }
                tick ++;
            }
        }.runTaskTimer(Freedom.get_plugin(),0,10);
    }

    public static void PortalParticleLifespan(Location center, Location targetLocation, Particle.DustOptions options) {

        new BukkitRunnable() {
            int tick = 0;
            double lifespantick = 0;
            ParticlePortalState state = ParticlePortalState.Opening;
            @Override
            public void run() {
                switch (state) {
                    case Opening: {
                        if (lifespantick >= 2) {
                            state = ParticlePortalState.Open;
                        }
                        createPortalParticles(center,2-lifespantick,lifespantick * 4,0,options,new Particle.DustOptions(Color.BLACK,16f));
                        lifespantick = lifespantick + 0.5;
                        break;
                    }
                    case Open: {
                        lifespantick = 0;
                        createPortalParticles(center,0,4,0,options,new Particle.DustOptions(Color.BLACK,16f));
                        if (tick >= 100) {
                            state = ParticlePortalState.Closing;
                        }
                        for (Player player : center.getNearbyEntitiesByType(Player.class,2,2,2)) {
                            player.teleport(targetLocation);

                        }
                        break;
                    }
                    case Closing:
                        createPortalParticles(center,2 + lifespantick,lifespantick,0,options,new Particle.DustOptions(Color.BLACK,16f));
                        lifespantick = lifespantick + 1;
                        break;
                }
                if (tick >= 1200) {
                    this.cancel();
                }
                tick ++;
            }
        }.runTaskTimer(Freedom.get_plugin(),0,10);
    }
    public static void createPortalParticles(Location center, double xlimit, double ylimit, double zlimit) {
        boolean circle = false;
        if (circle) {
            drawverticleCircle(center.add(0,2,0),2.1,center.getWorld(),32, Particle.DUST,new Particle.DustOptions(Color.GREEN,16f));
            for (double iteration = 0; iteration <= 2; iteration = iteration + 0.1) {
                drawverticleCircle(center,0 + iteration,center.getWorld(), (int) (16 + Math.round(iteration)),Particle.DUST,new Particle.DustOptions(Color.BLACK,128f));
            }
        } else {
            double rot = center.getRotation().yaw();
            drawVerticleSquare(center,4,rot, Particle.DUST,new Particle.DustOptions(Color.GREEN, (float) ylimit * 4),xlimit,0,0);
            drawVerticleSquare(center,3,rot, Particle.DUST,new Particle.DustOptions(Color.BLACK,(float) ylimit),xlimit,0,0);
            drawVerticleSquare(center,2,rot, Particle.DUST,new Particle.DustOptions(Color.BLACK,(float) ylimit),xlimit,0,0);
            drawVerticleSquare(center,1,rot, Particle.DUST,new Particle.DustOptions(Color.BLACK,(float) ylimit),xlimit,0,0);
        }
    }
    public static void createPortalParticles(Location center, double xlimit, double ylimit, double zlimit, Particle.DustOptions ring, Particle.DustOptions inside) {
        boolean circle = false;
        if (circle) {
            drawverticleCircle(center.add(0,2,0),2.1,center.getWorld(),32, Particle.DUST,new Particle.DustOptions(Color.GREEN,16f));
            for (double iteration = 0; iteration <= 2; iteration = iteration + 0.1) {
                drawverticleCircle(center,0 + iteration,center.getWorld(), (int) (16 + Math.round(iteration)),Particle.DUST,new Particle.DustOptions(Color.BLACK,128f));
            }
        } else {
            double rot = center.getRotation().yaw();
            drawVerticleSquare(center,4,rot, Particle.DUST,ring,xlimit,0,0);
            drawVerticleSquare(center,3,rot, Particle.DUST,inside,xlimit,0,0);
            drawVerticleSquare(center,2,rot, Particle.DUST,inside,xlimit,0,0);
            drawVerticleSquare(center,1,rot, Particle.DUST,inside,xlimit,0,0);
        }
    }
    public static void drawverticleCircle(Location center, double radius, World world, int points, Particle particle, Particle.DustOptions options) {
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ(); // Calculate Z coordinate
            double y = center.getY() + (radius * Math.sin(angle));; // Y remains constant for a flat circle

            // Create a new location for the point
            Location pointLocation = new Location(world, x, y, z);

            // 2. Spawn particles
            if (particle == Particle.DUST) {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 1, options);
            } else {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
            }
        }
    }
    public static ItemStack getSkull(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack getCustomSkull(String base64) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (base64.isEmpty()) return head;

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = skullMeta.getPlayerProfile();
        profile = Bukkit.createProfile(UUID.randomUUID(),null);
        profile.getProperties().add(new ProfileProperty("textures", base64));
        skullMeta.setPlayerProfile(profile);
        head.setItemMeta(skullMeta);
        return head;
    }
    public SkinProperty getPlayerSkin(Player player) throws DataRequestException {
        PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
        Optional<SkinProperty> property = playerStorage.getSkinForPlayer(
                player.getUniqueId(),
                player.getName()
        );
        return property.orElse(null);
    }

    public SoulTypes getSoulType(Player player) {
        return SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
    }

    public static void setSkinByName(Player player, String skinName) throws MineSkinException, DataRequestException {
        SkinStorage skinStorage = skinsRestorerAPI.getSkinStorage();
        PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
        // Find or fetch the skin data
        Optional<InputDataResult> result = skinStorage.findOrCreateSkinData(skinName);
        if (result.isPresent()) {
            // Set the skin identifier for the player
            playerStorage.setSkinIdOfPlayer(
                    player.getUniqueId(),
                    result.get().getIdentifier()
            );
            // Apply the skin visually
            skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player);
        }
    }
}
