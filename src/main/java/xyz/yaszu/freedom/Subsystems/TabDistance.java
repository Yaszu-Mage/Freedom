package xyz.yaszu.freedom.Subsystems;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import xyz.yaszu.freedom.Freedom;

import java.util.ArrayList;

public class TabDistance implements Listener {
    int tabradius = 100;
    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!Life_and_Death.is_alive(player)) return;
        ArrayList<Player> players = new ArrayList<>();
        for (Player instancedplayer : player.getLocation().getNearbyEntitiesByType(Player.class, tabradius) ) {
            if (Life_and_Death.is_alive(instancedplayer)) {
                players.add(instancedplayer);
            }
        }
        for (Player instancedPlayer : Bukkit.getOnlinePlayers()) {
            if (players.contains(instancedPlayer)) {
                player.showPlayer(Freedom.get_plugin(),instancedPlayer);
            } else {
                player.hidePlayer(Freedom.get_plugin(),instancedPlayer);
            }
        }
    }

    @EventHandler
    public void PlayerChatEvent(AsyncPlayerChatEvent event) {
        event.getRecipients().clear();
        Player player = event.getPlayer();
        boolean is_alive = Life_and_Death.is_alive(player);
        if (is_alive) {
            for (Player instancedPlayer : Bukkit.getOnlinePlayers()) {
                if (Life_and_Death.is_alive(instancedPlayer)) {
                    return;
                } else {
                    if (player.getLocation().getNearbyEntitiesByType(Player.class, tabradius).contains(instancedPlayer)) {
                        event.getRecipients().add(instancedPlayer);
                    }
                }
            }
        } else {
            for (Player instancedPlayer : Bukkit.getOnlinePlayers()) {
                if (Life_and_Death.is_alive(instancedPlayer)) {
                    if (player.getLocation().getNearbyEntitiesByType(Player.class, tabradius).contains(instancedPlayer)) {
                        event.getRecipients().add(instancedPlayer);
                    }
                }
            }
        }

    }
}
