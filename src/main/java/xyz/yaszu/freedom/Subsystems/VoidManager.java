package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Material;
import org.bukkit.block.BlockType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class VoidManager implements Listener {

@EventHandler
    public void BlockBreakEvent(BlockBreakEvent event) {
    if (event.getBlock().getLocation().getWorld().getName().equals("doublevoid")) {
        if (event.getBlock().getType() == Material.BLACK_CONCRETE) {
            event.setCancelled(true);
        }
    }
}
}
