package xyz.yaszu.freedom.Worlds;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WaterWorld implements Listener {



    public class waterGenerator extends ChunkGenerator {

        @Override
        public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            // Define your custom blocks here using chunkData.setBlock(x, y, z, Material)
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // Set the bottom layer to bedrock
                    chunkData.setBlock(x, 0, z, Material.BEDROCK);
                    // Set the next layers to water
                    for (int y = 1; y < 64; y++) {
                        chunkData.setBlock(x, y, z, Material.WATER);
                    }
                }
            }
        }





    }

}
