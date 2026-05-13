package xyz.yaszu.freedom.Alchemy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

import static org.bukkit.Bukkit.createChunkData;

/**
 * Infinite connected backrooms-style space generator.
 * Creates corridors first (street grid), then fills spaces with connected rooms.
 */
public class MazeGenerator extends ChunkGenerator {

    private MazeConfig config;
    private long worldSeed;

    public MazeGenerator() {
        this(new MazeConfig());
    }

    public MazeGenerator(MazeConfig config) {
        this.config = config;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);
        
        worldSeed = world.getSeed();

        // Set bedrock at bottom
        for (int bX = 0; bX < 16; bX++) {
            for (int bZ = 0; bZ < 16; bZ++) {
                chunk.setBlock(bX, world.getMinHeight(), bZ, Material.BEDROCK);
            }
        }

        // Generate backrooms with corridor-first approach
        generateBackroomsChunk(chunk, chunkX, chunkZ);

        return chunk;
    }

    /**
     * Generates backrooms with explicit corridor grid + room fill approach
     */
    private void generateBackroomsChunk(ChunkData chunk, int chunkX, int chunkZ) {
        int baseHeight = config.getBaseHeight();
        int wallHeight = config.getWallHeight();
        int roofOffset = config.getRoofOffset();
        int cellSize = config.getCellSize();
        int mid = cellSize / 2;

        for (int blockX = 0; blockX < 16; blockX++) {
            for (int blockZ = 0; blockZ < 16; blockZ++) {
                int globalX = (chunkX << 4) | blockX;
                int globalZ = (chunkZ << 4) | blockZ;

                boolean isWall = isWallBlock(globalX, globalZ);

                if (isWall) {
                    // Create wall
                    for (int y = baseHeight; y < baseHeight + wallHeight + 1; y++) {
                        chunk.setBlock(blockX, y, blockZ, config.getWallMaterial());
                    }
                } else {
                    // Create passage
                    chunk.setBlock(blockX, baseHeight, blockZ, config.getFloorMaterial());
                    
                    // Add floor protection (bedrock underneath)
                    chunk.setBlock(blockX, baseHeight - 1, blockZ, Material.BEDROCK);
                    
                    // Check if this is a gate (opening in the middle of a wall segment)
                    int localX = Math.floorMod(globalX, cellSize);
                    int localZ = Math.floorMod(globalZ, cellSize);
                    
                    int cellX = Math.floorDiv(globalX, cellSize);
                    int cellZ = Math.floorDiv(globalZ, cellSize);

                    if (localX == 0) {
                        if (!shouldRemoveWall(cellX, cellZ, true)) {
                            int gateWidth = getGateWidth(cellX, cellZ, true);
                            if (isInGate(localZ, gateWidth)) {
                                generateGate(chunk, blockX, blockZ, true);
                            }
                        }
                    } else if (localZ == 0) {
                        if (!shouldRemoveWall(cellX, cellZ, false)) {
                            int gateWidth = getGateWidth(cellX, cellZ, false);
                            if (isInGate(localX, gateWidth)) {
                                generateGate(chunk, blockX, blockZ, false);
                            }
                        }
                    } else {
                        // Fill air with light
                        if (config.isFillWithLight()) {
                            for (int y = baseHeight + 1; y < baseHeight + roofOffset; y++) {
                                chunk.setBlock(blockX, y, blockZ, Material.LIGHT);
                            }
                        }
                    }
                }

                // Add roof
                chunk.setBlock(blockX, baseHeight + roofOffset, blockZ, config.getRoofMaterial());
                // Add roof protection (bedrock above)
                chunk.setBlock(blockX, baseHeight + roofOffset + 1, blockZ, Material.BEDROCK);
            }
        }
    }

    /**
     * Generates a gate structure with smooth sandstone stairs.
     */
    private void generateGate(ChunkData chunk, int x, int z, boolean vertical) {
        int baseHeight = config.getBaseHeight();
        
        // Clear the space for the gate
        for (int y = baseHeight + 1; y < baseHeight + config.getRoofOffset(); y++) {
            chunk.setBlock(x, y, z, Material.AIR);
        }
        
        Material stairs = config.getDoorMaterial();
        int gateHeight = baseHeight + 3;
        
        try {
            if (vertical) {
                // Gate in a vertical wall (X=0), so it faces East/West
                // Top arch

                chunk.setBlock(x, gateHeight + 1, z, Bukkit.createBlockData(stairs, "[half=top,facing=east]"));
            } else {
                // Gate in a horizontal wall (Z=0), so it faces North/South
                // Top arch
                chunk.setBlock(x, gateHeight + 1, z, Bukkit.createBlockData(stairs, "[half=top,facing=south]"));
            }
        } catch (Exception e) {
            // Fallback if BlockData string parsing fails or Bukkit not initialized correctly
            chunk.setBlock(x, gateHeight + 1, z, stairs);
        }

        // Add some light
        if (config.isFillWithLight()) {
            chunk.setBlock(x, baseHeight + 1, z, Material.LIGHT);
            chunk.setBlock(x, baseHeight + 2, z, Material.LIGHT);
        }
    }

    /**
     * Determines if a block should be a wall.
     * Generates a "true" maze using a deterministic cell-based approach.
     * Each cell is defined by config.getCellSize().
     */
    private boolean isWallBlock(int globalX, int globalZ) {
        int cellSize = config.getCellSize();
        
        // Use Math.floorDiv and Math.floorMod to handle negative coordinates correctly
        int cellX = Math.floorDiv(globalX, cellSize);
        int cellZ = Math.floorDiv(globalZ, cellSize);
        int localX = Math.floorMod(globalX, cellSize);
        int localZ = Math.floorMod(globalZ, cellSize);

        // Cell boundary walls (localX == 0 or localZ == 0)
        if (localX == 0 || localZ == 0) {
            // Corner is always a wall pillar
            if (localX == 0 && localZ == 0) return true;
            
            // Middle of the wall segment might be an opening
            int mid = cellSize / 2;
            
                // Vertical wall segment between cellX-1 and cellX
            if (localX == 0) {
                if (shouldRemoveWall(cellX, cellZ, true)) return false;
                int gateWidth = getGateWidth(cellX, cellZ, true);
                if (isInGate(localZ, gateWidth)) {
                    return !hasPassage(cellX - 1, cellZ, 1, 0);
                }
                return true;
            } else {
                // Horizontal wall segment between cellZ-1 and cellZ
                if (shouldRemoveWall(cellX, cellZ, false)) return false;
                int gateWidth = getGateWidth(cellX, cellZ, false);
                if (isInGate(localX, gateWidth)) {
                    return !hasPassage(cellX, cellZ - 1, 0, 1);
                }
                return true;
            }
        }

        // Cell interior is always air (passage)
        return false;
    }

    /**
     * Deterministically decides the width of a gate at a specific wall segment.
     */
    private int getGateWidth(int cellX, int cellZ, boolean vertical) {
        long seed;
        if (vertical) {
            seed = (long) cellX * 668265261L ^ (long) cellZ * 511891231L ^ worldSeed ^ 0xBEEFBEEFL;
        } else {
            seed = (long) cellX * 312151189L ^ (long) cellZ * 850327465L ^ worldSeed ^ 0xDEADBEEFL;
        }
        Random rand = new Random(seed);
        // Random width between 1 and 3, but not exceeding cellSize - 2 to keep corners
        int maxWidth = Math.max(1, config.getCellSize() - 2);
        return rand.nextInt(Math.min(3, maxWidth)) + 1;
    }

    /**
     * Checks if a local coordinate is within the gate range of a wall segment.
     */
    private boolean isInGate(int localPos, int gateWidth) {
        int mid = config.getCellSize() / 2;
        int halfWidth = gateWidth / 2;
        int start = mid - halfWidth;
        int end = start + gateWidth;
        return localPos >= start && localPos < end;
    }

    /**
     * Deterministically decides if a wall segment between cells should be completely removed
     * to create larger, randomly sized rooms.
     */
    private boolean shouldRemoveWall(int cellX, int cellZ, boolean vertical) {
        // Use a hash of the wall location
        long seed;
        if (vertical) {
            seed = (long) cellX * 668265261L ^ (long) cellZ * 511891231L ^ worldSeed ^ 0xFAFADADAL;
        } else {
            seed = (long) cellX * 312151189L ^ (long) cellZ * 850327465L ^ worldSeed ^ 0xDADAFADAL;
        }

        Random rand = new Random(seed);
        // 30% chance to remove the entire wall segment to merge rooms
        return rand.nextDouble() < 0.3;
    }

    /**
     * Deterministically decides if there is a passage between two adjacent cells.
     */
    private boolean hasPassage(int cellX, int cellZ, int dx, int dz) {
        // Use a hash of the cell coordinates and world seed to get a stable seed for this cell
        long seed = (long) cellX * 312151189L ^ (long) cellZ * 850327465L ^ worldSeed;
        Random rand = new Random(seed);
        
        // We use a modified Binary Tree approach that is deterministic for infinite generation.
        // For each cell, we decide if it connects to the EAST (dx=1, dz=0) 
        // or to the SOUTH (dx=0, dz=1). 
        // We ensure at least one connection (except maybe at world boundaries, but this is infinite).
        
        // To avoid the "highway" feel, we don't have long straight corridors.
        // We use the seed to pick exactly one direction to open by default, 
        // and occasionally open both to allow for loops and multiple paths.
        
        double choice = rand.nextDouble();
        
        if (dx == 1 && dz == 0) {
            // Open East if choice < 0.55 (slightly biased for some directionality, but random)
            // Or if it's a "both" cell (e.g. choice > 0.9)
            return choice < 0.55 || choice > 0.9;
        } else if (dx == 0 && dz == 1) {
            // Open South if choice >= 0.55
            // This ensures every cell has at least one exit (either East or South),
            // which guarantees global connectivity in an infinite grid.
            return choice >= 0.55;
        }
        
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }
}

