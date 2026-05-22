package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class RandomChestGenerator implements Listener {

    @EventHandler
    public void onChestGenerate() {

    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        //check chunk for chests
        if (event.isNewChunk()) {
            Chunk chunk = event.getChunk();
            if (chunk.contains(Material.CHEST.createBlockData())) {
                //iterate through all blocks to find chest

            }
        }
    }
}
