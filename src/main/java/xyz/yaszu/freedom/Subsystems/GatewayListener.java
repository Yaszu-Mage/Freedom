package xyz.yaszu.freedom.Subsystems;

import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * --unused--
 */
public class GatewayListener implements Listener {
    @EventHandler
    public void onGatewayEnter(EntityTeleportEndGatewayEvent event) {
        if (event.getGateway().getLocation().getWorld().getName().toLowerCase().contains("end")){
            event.setCancelled(true);
        }
    }
}
