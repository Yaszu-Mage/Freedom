package xyz.yaszu.freedom.Subsystems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;

import java.util.Random;

/**
 * Manages Backrooms specific features: protection and ambience.
 * the backrooms is a custom dimension used as a form of punishment to players.
 * the ambiance is a collection of vanilla sounds that play randomly.
 */
public class BackroomsManager implements Listener {

    private static final String BACKROOMS_WORLD_NAME = "backrooms";
    private final Random random = new Random();

    public BackroomsManager(Freedom plugin) {
        startAmbienceTask(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase(BACKROOMS_WORLD_NAME)) {
            if (!event.getPlayer().isOp() && (event.getBlock().getType() == Material.SMOOTH_SANDSTONE || event.getBlock().getType() == Material.SMOOTH_STONE) ) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase(BACKROOMS_WORLD_NAME)) {
            if (!event.getPlayer().isOp() && (event.getBlock().getType() == Material.SMOOTH_SANDSTONE || event.getBlock().getType() == Material.SMOOTH_STONE) ) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * plays scary ambiance at a random chance every 10 seconds
     *
     * @param plugin runs timer on plugin
     */
    private void startAmbienceTask(Freedom plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                World backrooms = Bukkit.getWorld(BACKROOMS_WORLD_NAME);
                if (backrooms == null) return;

                for (Player player : backrooms.getPlayers()) {
                    if (random.nextDouble() < 0.15) { // 15% chance every 10 seconds for a sound
                        playScarySound(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 200L); // Every 10 seconds (200 ticks)
    }

    /**
     * list of scary sounds to play
     *
     * @param player player that hears the sound
     */
    private void playScarySound(Player player) {
        Sound[] sounds = {
            Sound.AMBIENT_CAVE,
            Sound.ENTITY_ENDERMAN_STARE,
            Sound.ENTITY_ENDERMAN_SCREAM,
            Sound.ENTITY_CREEPER_PRIMED,
            Sound.BLOCK_CHEST_OPEN,
            Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
            Sound.ENTITY_GHAST_SCREAM,
            Sound.ENTITY_PHANTOM_SWOOP,
            Sound.BLOCK_GRASS_STEP,
            Sound.ENTITY_PLAYER_BREATH
        };

        Sound sound = sounds[random.nextInt(sounds.length)];
        float pitch = 0.5f + random.nextFloat();
        float volume = 0.2f + random.nextFloat() * 0.5f;

        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
