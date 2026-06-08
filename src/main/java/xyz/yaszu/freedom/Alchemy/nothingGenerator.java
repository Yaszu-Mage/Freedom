package xyz.yaszu.freedom.Alchemy;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.Random;

public class nothingGenerator extends ChunkGenerator implements Listener {

    public static Util util = new Util();

    @Override
    public ChunkGenerator.ChunkData generateChunkData(World world, Random random, int x, int z, ChunkGenerator.BiomeGrid biome) {
        ChunkGenerator.ChunkData chunk = Bukkit.createChunkData(world);
        // Set the very bottom layer to Bedrock
        // In newer versions (1.18+), the minimum height is -64
        for (int bX = 0; bX < 16; bX++) {
            for (int bZ = 0; bZ < 16; bZ++) {
                chunk.setBlock(bX, world.getMinHeight(), bZ, Material.AIR);
            }
        }
        return chunk;
    }
    public static HashMap<Chunk,Boolean> activeChunks = new HashMap<>();
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.getChunk().getWorld().getGenerator() instanceof nothingGenerator || event.getChunk().getWorld().getGenerator() instanceof voidGenerator) {
            // Ensure the chunk is generated with our custom generator
            if (!activeChunks.containsKey(event.getChunk())) {
                activeChunks.put(event.getChunk(),true);
                shimmer(event.getChunk()).runTaskTimer(Freedom.get_plugin(),0,1);
            }

        }
    }
    public BukkitRunnable shimmer(Chunk chunk) {
        return new BukkitRunnable() {
            public boolean pausedBecauseLight = false;
            public int pauseCounter = 0;
            public Random random = new Random();
            public int higherY = -100;
            public int lowerY = -100;
            @Override
            public void run() {
                if (random.nextInt(32) == 0 || pausedBecauseLight) {
                    int x = random.nextInt(16);
                    int y = random.nextInt(-64,320);
                    if (lowerY != -100) {
                        y = getRandomOutsideRange(320,lowerY,higherY);
                    }
                    int z = random.nextInt(16);
                    Block block = chunk.getBlock(x,y,z);
                    if (pauseCounter >= 128) {
                        for (x = 0; x < 16; x++) {
                            for (y = -64; y < 320; y++) {
                                for (z = 0; z < 16; z++) {
                                    if (block.getLightLevel() > 4) {
                                        if (lowerY == -100) {
                                            lowerY = y - 1;
                                        } else {
                                            higherY = y + 1;
                                        }
                                    }
                                }
                            }
                        }
                        if (lowerY == -100 && higherY == -100) {
                            this.cancel();
                        }
                    }
                    if (block.getLightLevel() < 4) {
                        Location location = block.getLocation();
                        location.getWorld().spawnParticle(Particle.END_ROD,location,16);
                    } else {
                        pauseCounter++;
                        pausedBecauseLight = true;
                    }
                }
            }
        };
    }


    @EventHandler
    public void onPlayerGoIntoNetherPortal(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getWorld() != Bukkit.getWorld("personalVoid")) return;
        if (player.getLocation().getBlock().getType() == Material.NETHER_PORTAL || player.getLocation().add(0,1,0).getBlock().getType() == Material.NETHER_PORTAL) {
            if (!player.getPersistentDataContainer().has(util.keygen("personalVoid"))) {
                player.teleport(Bukkit.getWorld("world").getSpawnLocation());
            } else {
                int x = player.getPersistentDataContainer().getOrDefault(util.keygen("personalVoidX"), PersistentDataType.INTEGER,0);
                int y = player.getPersistentDataContainer().getOrDefault(util.keygen("personalVoidY"), PersistentDataType.INTEGER,0);
                int z = player.getPersistentDataContainer().getOrDefault(util.keygen("personalVoidZ"), PersistentDataType.INTEGER,0);
                player.teleport(new Location(Bukkit.getWorld("world"),x,y,z));
            }
        }
    }
    public static int getRandomOutsideRange(int totalMax, int excludeMin, int excludeMax) {
        Random rand = new Random();

        // Calculate how many numbers are inside the forbidden zone
        int excludedSize = (excludeMax - excludeMin) + 1;

        // Pick a number from the compressed range (0 to totalMax - excludedSize)
        int randomNumber = rand.nextInt(totalMax - excludedSize + 1);

        // If the number hits or passes the forbidden zone, shift it past it
        if (randomNumber >= excludeMin) {
            randomNumber += excludedSize;
        }

        return randomNumber;
    }
}
