package xyz.yaszu.freedom.Alchemy;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;

import java.util.HashMap;
import java.util.Random;

import static org.bukkit.Bukkit.createChunkData;

public class voidGenerator extends ChunkGenerator implements Listener {

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        ChunkData chunk = Bukkit.createChunkData(world);
        // Set the very bottom layer to Bedrock
        // In newer versions (1.18+), the minimum height is -64
        for (int bX = 0; bX < 16; bX++) {
            for (int bZ = 0; bZ < 16; bZ++) {
                chunk.setBlock(bX, world.getMinHeight(), bZ, Material.BEDROCK);
            }
        }
        return chunk;
    }
}