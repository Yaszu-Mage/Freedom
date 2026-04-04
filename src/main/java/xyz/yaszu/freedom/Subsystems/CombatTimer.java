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

public class CombatTimer extends Util implements Listener {
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

    @EventHandler
    public void PlayerDamagePlayer (EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player && event.getDamager() instanceof Player damager) {
            combatcheck(player);
            combatcheck(damager);
        }

    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        if (event.getPlayer().getPersistentDataContainer().has(keygen("combattimer"))) {
            event.getPlayer().getPersistentDataContainer().remove(keygen("combattimer"));
        }
    }


    public void combatcheck(Player player) {
        if (!player.getPersistentDataContainer().has(keygen("combattimer"))) {
            player.getPersistentDataContainer().set(keygen("combattimer"), PersistentDataType.BOOLEAN,true);
            combatTimer.put(player.getUniqueId(),System.currentTimeMillis());
            futureCheck(player).runTaskLater(Freedom.get_plugin(),60);
        } else {
            combatTimer.put(player.getUniqueId(),System.currentTimeMillis());
        }
    }

    public BukkitRunnable futureCheck(Player player) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (combatTimer.get(player.getUniqueId()) + combatTime <= System.currentTimeMillis()) {
                    if (player.getPersistentDataContainer().has(keygen("combattimer"))) {
                        player.getPersistentDataContainer().remove(keygen("combattimer"));
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
