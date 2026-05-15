package xyz.yaszu.freedom.Subsystems;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Alchemy.voidGenerator;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.StructureUtil;

import java.io.IOException;
import java.util.*;

public class WorldManager implements Listener {

    public record StructureInfo(float weight) {}

    private static final Map<String, Map<String, StructureInfo>> WORLD_STRUCTURE_REGISTRY = new HashMap<>();
    private static final Random random = new Random();

    static {
        // Initialize with default structures for compatibility
        registerStructure("world", "ritual.schem", 1.0f);
        registerStructure("void", "voidisland.schem", 10.0f);
        registerStructure("void", "voidisland2.schem", 10.0f);
        registerStructure("void", "voidisland3.schem", 10.0f);
        registerStructure("void", "voidisland4.schem", 10.0f);
        registerStructure("world", "kfc.schematic", 0.01f);
        registerStructure("world", "evilcampfire.schem", 1f);
    }

    /**
     * Creates a uniquely generated world with void generation.
     * @param worldName The name of the world to create.
     * @return The created world.
     */
    public static World createInfiniteWorld(String worldName) {
        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.generator(new voidGenerator());
        World world = worldCreator.createWorld();
        if (world != null) {
            world.setStorm(false);
            world.setThundering(false);
        }
        return world;
    }

    /**
     * Adds a structure to a specific world's registry.
     * @param worldName The name of the world.
     * @param schematic Name of the schematic file.
     * @param weight Probability weight.
     */
    public static void registerStructure(String worldName, String schematic, float weight) {
        WORLD_STRUCTURE_REGISTRY.computeIfAbsent(worldName, k -> new HashMap<>()).put(schematic, new StructureInfo(weight));
    }

    /**
     * Registers a structure to multiple worlds.
     * @param worlds List of world names.
     * @param schematic Name of the schematic file.
     * @param weight Probability weight.
     */
    public static void registerStructure(List<String> worlds, String schematic, float weight) {
        for (String world : worlds) {
            registerStructure(world, schematic, weight);
        }
    }

    /**
     * Gets the structures registered for a specific world.
     * @param worldName The name of the world.
     * @return A map of schematic names to structure info.
     */
    public static Map<String, StructureInfo> getStructuresForWorld(String worldName) {
        return WORLD_STRUCTURE_REGISTRY.get(worldName);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        String worldName = world.getName();

        if (worldName.startsWith("redcastle")) {
            RedCastleManager.generateCastle(world, event.getChunk().getX(), event.getChunk().getZ());
            return;
        }

        // Get structures specifically for this world
        Map<String, StructureInfo> applicableStructures = WORLD_STRUCTURE_REGISTRY.get(worldName);

        if (applicableStructures == null || applicableStructures.isEmpty()) return;

        // Use PDC to ensure we only spawn structures once per chunk
        if (event.getChunk().getPersistentDataContainer().has(FreedomKeys.key("generated_structures"), PersistentDataType.BOOLEAN)) {
            return;
        }
        event.getChunk().getPersistentDataContainer().set(FreedomKeys.key("generated_structures"), PersistentDataType.BOOLEAN, true);

        // Randomly decide which structure to spawn based on weight
        if (random.nextInt(1000) != 0) return;

        // Validation: skip if on water (for specific worlds like "world")
        if (worldName.equals("world") && isOnWater(world, event.getChunk().getX() * 16, event.getChunk().getZ() * 16)) return;

        // Normalize weights for relative selection among applicable structures
        float totalWeight = 0;
        for (StructureInfo info : applicableStructures.values()) {
            totalWeight += info.weight();
        }

        if (totalWeight <= 0) return;

        float target = random.nextFloat() * totalWeight;
        float current = 0;
        for (Map.Entry<String, StructureInfo> entry : applicableStructures.entrySet()) {
            current += entry.getValue().weight();
            if (current >= target) {
                spawnStructure(world, event.getChunk().getX(), event.getChunk().getZ(), entry.getKey());
                break;
            }
        }
    }

    public static void spawnStructureStatic(World world, int chunkX, int chunkZ, int y, String resourceName, int rotation) {
        try {
            long startTime = System.currentTimeMillis();
            int x = (chunkX * 16);
            int z = (chunkZ * 16);

            performSpawnStatic(world, x, y, z, resourceName, rotation);

            if (RedCastleManager.verbose) {
                Freedom.get_plugin().getLogger().info(String.format("[RedCastle] Pasted %s at [%d, %d, %d] in %dms",
                        resourceName, x, y, z, System.currentTimeMillis() - startTime));
            }
        } catch (Exception e) {
            Freedom.get_plugin().getLogger().severe("Failed to spawn structure " + resourceName + ": " + e.getMessage());
        }
    }

    private static void performSpawnStatic(World world, int x, int y, int z, String resourceName, int rotation) throws WorldEditException {
        Clipboard load = StructureUtil.loadSchematicFromResource(resourceName);
        if (load != null) {
            com.sk89q.worldedit.world.World adapter = BukkitAdapter.adapt(world);
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(adapter)) {
                // The center of the schematic relative to its origin (for rotation)
                BlockVector3 dimensions = load.getDimensions();
                BlockVector3 min = load.getRegion().getMinimumPoint().subtract(load.getOrigin());

                double width = dimensions.x();
                double length = dimensions.z();
                if (rotation == 90 || rotation == 270 || rotation == -90 || rotation == -270) {
                    double temp = width;
                    width = length;
                    length = temp;
                }

                // CHECK FOR BLEED: If the dimensions (after rotation) are > 16, it WILL bleed.
                if (width > 16 || length > 16) {
                    Freedom.get_plugin().getLogger().warning(String.format(
                            "[RedCastle] Bleed detected for %s: Dimensions %.1fx%.1f exceed 16x16 chunk boundary. Skipping or clamping might be needed.",
                            resourceName, width, length));
                    // Depending on preference, we could return here to PREVENT bleed.
                    // For now, let's just warn as per "add checks" request.
                }

                ClipboardHolder holder = new ClipboardHolder(load);
                com.sk89q.worldedit.math.transform.AffineTransform transform = new com.sk89q.worldedit.math.transform.AffineTransform();

                // 1. First, move the schematic so its internal min point is at (0,0,0)
                transform = transform.translate(-min.x(), 0, -min.z());

                // 2. Apply rotation around its own center (width/2, length/2)
                // Wait, it's easier to rotate around (8,8) if we know it's meant to fit in 16x16.
                
                // If we want it to NOT bleed, we align it to (0,0) first.
                // Then rotate.
                if (rotation != 0) {
                    // Rotate around the center of the 16x16 area to keep it aligned if it's 16x16
                    transform = transform
                            .translate(8.0, 0, 8.0)
                            .rotateY(-rotation)
                            .translate(-8.0, 0, -8.0);
                }

                // Finally, align to bottom-right (16,16) using the helper
                com.sk89q.worldedit.math.Vector3 adjust = StructureUtil.getBottomRightOffset(load, rotation);
                
                // Ensure no negative shift if we want to stay within [0, 16]
                // but if it's > 16, it will bleed anyway.
                transform = transform.translate(Math.max(0, adjust.x()), 0, Math.max(0, adjust.z()));

                holder.setTransform(transform);

                Operation operation = holder
                        .createPaste(editSession)
                        .to(BlockVector3.at(x, y, z))
                        .ignoreAirBlocks(false)
                        .copyEntities(true)
                        .copyBiomes(false)
                        .build();
                Operations.complete(operation);
            }
        }
    }

    private void spawnStructure(World world, int chunkX, int chunkZ, String resourceName) {
        try {
            int x = (chunkX * 16) + random.nextInt(0, 16);
            int z = (chunkZ * 16) + random.nextInt(0, 16);

            // Validation rules
            if (world.getName().equals("world")) {
                // Spawning on water check
                if (isOnWater(world, x, z)) {
                    return;
                }

                int highestY = world.getHighestBlockYAt(x, z);
                // Average ground level check (variance <= 2)
                int sumHeight = 0;
                int minH = highestY;
                int maxH = highestY;
                int count = 0;
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        int h = world.getHighestBlockYAt(x + dx, z + dz);
                        sumHeight += h;
                        if (h < minH) minH = h;
                        if (h > maxH) maxH = h;
                        count++;
                    }
                }
                double avg = (double) sumHeight / count;
                if (maxH - minH > 2) {
                    return; // Too uneven
                }

                int y = highestY + 1;
                // Spawning in blocks check (ensure enough air space)
                if (world.getBlockAt(x, y, z).getType() != Material.AIR ||
                    world.getBlockAt(x, y + 1, z).getType() != Material.AIR) {
                    return;
                }

                performSpawn(world, x, y, z, resourceName);
            } else {
                // Default void world behavior
                int y = 60 + random.nextInt(-5, 20);
                if (y < world.getMinHeight()) y = 60;
                performSpawn(world, x, y, z, resourceName);
            }
        } catch (Exception e) {
            Freedom.get_plugin().getLogger().severe("Failed to spawn structure " + resourceName + ": " + e.getMessage());
        }
    }

    private void performSpawn(World world, int x, int y, int z, String resourceName) throws WorldEditException {
        Freedom.get_plugin().getLogger().info("WorldManager: Spawning " + resourceName + " at " + x + ", " + y + ", " + z + " in world " + world.getName());

        Clipboard load = StructureUtil.loadSchematicFromResource(resourceName);
        if (load != null) {
            com.sk89q.worldedit.world.World adapter = BukkitAdapter.adapt(world);
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(adapter)) {
                Operation operation = new ClipboardHolder(load)
                        .createPaste(editSession)
                        .to(BlockVector3.at(x, y, z))
                        .build();
                Operations.complete(operation);
            }
        }
    }

    private boolean isOnWater(World world, int x, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int highestY = world.getHighestBlockYAt(x + dx, z + dz);
                Block highestBlock = world.getBlockAt(x + dx, highestY, z + dz);
                Material type = highestBlock.getType();
                if (type == Material.WATER || type == Material.LAVA || type == Material.SEAGRASS || type == Material.TALL_SEAGRASS || type == Material.KELP || type == Material.KELP_PLANT) {
                    return true;
                }
            }
        }
        return false;
    }
}
