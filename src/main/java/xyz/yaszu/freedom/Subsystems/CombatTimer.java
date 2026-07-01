package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.UUID;

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

/**
 * a system that keeps track of if a player is in combat.
 * functions as a timer of 30 seconds and is reset until the end of combat.
 */
public class CombatTimer implements Listener {
    public static HashMap<UUID,Long> combatTimer = new HashMap<>();
    public static long combatTime = 30000;

    @EventHandler
    public void PlayerLeaveEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(keygen("combattimer"))) {
            player.getPersistentDataContainer().remove(keygen("combattimer"));
            ItemStack[] stack = player.getInventory().getContents();
            player.getInventory().clear();
            for (ItemStack item : stack) {
                if (item != null) {
                    player.getWorld().dropItem(player.getLocation(),item);
                }

            }
            player.damage(player.getHealth() * 10);
            player.getWorld().sendMessage(dess(player.getName() +" combat logged."));

        }
    }

    /**
     * creates a combat timer for player when in combat
     *
     * @param player player
     * @return combat timer for player
     */
    public static boolean isCombat(Player player) {
        return combatTimer.containsKey(player.getUniqueId());
    }
    @EventHandler
    public void PlayerDamagePlayer (EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player && event.getDamager() instanceof Player damager) {
            combatcheck(player);
            combatcheck(damager);
        }

    }

    /**
     * removes combat timer on join
     *
     * @param event player join event
     */
    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        if (event.getPlayer().getPersistentDataContainer().has(keygen("combattimer"))) {
            event.getPlayer().getPersistentDataContainer().remove(keygen("combattimer"));
        }
    }

    /**
     * checks for a combat timer on player
     *
     * @param player player
     */
    public void combatcheck(Player player) {
        if (xyz.yaszu.freedom.Subsystems.AdminManager.isSudo(player)) return;
        if (!player.getPersistentDataContainer().has(keygen("combattimer"))) {
            player.getPersistentDataContainer().set(keygen("combattimer"), PersistentDataType.BOOLEAN,true);
            combatTimer.put(player.getUniqueId(),System.currentTimeMillis());
            futureCheck(player).runTaskLater(Freedom.get_plugin(),60);
        } else {
            combatTimer.put(player.getUniqueId(),System.currentTimeMillis());
        }
    }

    /**
     * does something, probably
     *
     * @param player player
     * @return honestly don't know, its 4am and i haven't slept in days
     */
    public BukkitRunnable futureCheck(Player player) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (combatTimer.get(player.getUniqueId()) + combatTime <= System.currentTimeMillis()) {
                    if (player.getPersistentDataContainer().has(keygen("combattimer"))) {
                        player.getPersistentDataContainer().remove(keygen("combattimer"));
                        combatTimer.remove(player.getUniqueId());
                        this.cancel();
                    } else {
                        this.cancel();
                    }
                } else {
                    futureCheck(player).runTaskLater(Freedom.get_plugin(),60);
                    this.cancel();
                }
            }
        };
    }


}
