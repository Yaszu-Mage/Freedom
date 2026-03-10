package xyz.yaszu.freedom.Util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;

public class Util {
    public static NamespacedKey keygen(String key) {
        return new NamespacedKey(Bukkit.getPluginManager().getPlugin("Freedom"),key);
    }

    public static Component dess(String minimessage) {
        return MiniMessage.miniMessage().deserialize(minimessage);
    }
    public void drawCircle(Location center, double radius, World world, int points,Particle particle) {
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ() + (radius * Math.sin(angle)); // Calculate Z coordinate
            double y = center.getY(); // Y remains constant for a flat circle

            // Create a new location for the point
            Location pointLocation = new Location(world, x, y, z);

            // 2. Spawn particles
            world.spawnParticle(particle, pointLocation, 1);
        }
    }
}
