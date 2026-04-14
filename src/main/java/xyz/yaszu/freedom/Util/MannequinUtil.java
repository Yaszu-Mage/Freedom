package xyz.yaszu.freedom.Util;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

/**
 * Utility class to control Mannequin entities.
 */
public class MannequinUtil {

    /**
     * Makes the mannequin pathfind to a specific vector (location).
     *
     * @param mannequin The mannequin to control.
     * @param target    The target vector to pathfind to.
     */
    public static void pathfindTo(Mannequin mannequin, Vector target) {
        if (mannequin instanceof Mob mob) {
            Location loc = target.toLocation(mannequin.getWorld());
            mob.getPathfinder().moveTo(loc);
        } else {
            // Fallback: move via velocity or teleport if it's not a Mob (though Paper's Mannequin usually is)
            mannequin.teleport(target.toLocation(mannequin.getWorld()));
        }
    }

    /**
     * Makes the mannequin look at a specific location.
     *
     * @param mannequin The mannequin to control.
     * @param target    The location to look at.
     */
    public static void lookAt(Mannequin mannequin, Location target) {
        Location mannequinLoc = mannequin.getLocation();
        // If the mannequin has an eye location, use it as the source for the direction vector
        Location source = mannequin.getEyeLocation();
        Vector direction = target.toVector().subtract(source.toVector());
        
        // Only normalize if the vector has length > 0
        if (direction.lengthSquared() > 0) {
            direction.normalize();
        } else {
            return; // Target is same as source
        }
        
        mannequinLoc.setDirection(direction);
        mannequin.teleport(mannequinLoc);
    }

    /**
     * Makes the mannequin look at a specific entity.
     *
     * @param mannequin The mannequin to control.
     * @param target    The entity to look at.
     */
    public static void lookAt(Mannequin mannequin, org.bukkit.entity.Entity target) {
        lookAt(mannequin, target instanceof LivingEntity le ? le.getEyeLocation() : target.getLocation());
    }

    /**
     * Makes the mannequin look at a specific vector.
     *
     * @param mannequin The mannequin to control.
     * @param target    The vector to look at.
     */
    public static void lookAt(Mannequin mannequin, Vector target) {
        lookAt(mannequin, target.toLocation(mannequin.getWorld()));
    }

    /**
     * Makes the mannequin attack a living entity.
     *
     * @param mannequin The mannequin that attacks.
     * @param target    The entity to attack.
     */
    public static void attack(Mannequin mannequin, LivingEntity target) {
        if (mannequin instanceof Mob mob) {
            mob.setTarget(target);
        }
        mannequin.attack(target);
    }

    /**
     * Makes the mannequin "say" something in the chat.
     *
     * @param mannequin The mannequin that speaks.
     * @param message   The message to say.
     */
    public static void say(Mannequin mannequin, String message) {
        Component component = Component.text("<" + mannequin.getName() + "> " + message);
        mannequin.getWorld().getPlayers().forEach(player -> player.sendMessage(component));
    }

    /**
     * Sets the mannequin's velocity using a vector.
     *
     * @param mannequin The mannequin to control.
     * @param velocity  The velocity vector.
     */
    public static void setVelocity(Mannequin mannequin, Vector velocity) {
        mannequin.setVelocity(velocity);
    }

    /**
     * Moves the mannequin to a specific vector immediately.
     *
     * @param mannequin The mannequin to control.
     * @param target    The target vector.
     */
    public static void moveTo(Mannequin mannequin, Vector target) {
        mannequin.teleport(target.toLocation(mannequin.getWorld()));
    }
}
