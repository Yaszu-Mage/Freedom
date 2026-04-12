package xyz.yaszu.freedom.Items.Artifacts;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.Map;

public class ArtifactManager extends Util implements Listener {

    public static final Map<String, Base_Artifact> ARTIFACTS = new HashMap<>();

    public static void registerArtifacts() {
        register(new MinerArtifact());
        register(new WarriorArtifact());
        register(new ScholarArtifact());
        register(new FeatherArtifact());
        register(new OceanArtifact());
        register(new FireArtifact());
        register(new NightArtifact());
        register(new GuardianArtifact());
        register(new AthleteArtifact());
        register(new HealerArtifact());
    }

    private static void register(Base_Artifact artifact) {
        ARTIFACTS.put(artifact.getID(), artifact);
    }

    @EventHandler
    public void onSleep(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        // Check if they were in bed long enough or if it's morning
        // player.isDeeplySleeping() is true if they slept through the night
        if (player.isDeeplySleeping() || (player.getWorld().getTime() >= 0 && player.getWorld().getTime() <= 1000)) {
            for (Base_Artifact artifact : ARTIFACTS.values()) {
                if (artifact.hasArtifact(player)) {
                    player.getPersistentDataContainer().set(FreedomKeys.activeArtifact(), PersistentDataType.STRING, artifact.getID());
                    player.sendMessage(dess("<green>You feel well rested thanks to your </green>").append(artifact.Name()));
                    // Apply effects immediately
                    for (PotionEffect effect : artifact.getBuffs()) {
                        player.addPotionEffect(effect);
                    }
                    break; // Only one artifact buff at a time
                }
            }
        }
    }

    public void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Freedom.get_plugin().getServer().getOnlinePlayers()) {
                    String activeId = player.getPersistentDataContainer().get(FreedomKeys.activeArtifact(), PersistentDataType.STRING);
                    if (activeId != null) {
                        Base_Artifact artifact = ARTIFACTS.get(activeId);
                        if (artifact != null && artifact.hasArtifact(player)) {
                            // Maintain effects
                            for (PotionEffect buff : artifact.getBuffs()) {
                                PotionEffect current = player.getPotionEffect(buff.getType());
                                if (current == null || current.getDuration() < 100) {
                                    player.addPotionEffect(buff);
                                }
                            }
                        } else {
                            // Artifact lost, removed, or doesn't exist anymore
                            if (artifact != null) {
                                for (PotionEffect buff : artifact.getBuffs()) {
                                    player.removePotionEffect(buff.getType());
                                }
                            }
                            player.getPersistentDataContainer().remove(FreedomKeys.activeArtifact());
                            player.sendMessage(dess("<red>The artifact's blessing has faded as it is no longer with you.</red>"));
                        }
                    }
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 20L, 20L);
    }
}
