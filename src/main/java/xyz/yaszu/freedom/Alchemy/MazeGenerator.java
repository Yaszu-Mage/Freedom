package xyz.yaszu.freedom.Alchemy;

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
        for (int blockX = 0; blockX < 16; blockX++) {
            for (int blockZ = 0; blockZ < 16; blockZ++) {
                int globalX = (chunkX << 4) | blockX;
                int globalZ = (chunkZ << 4) | blockZ;

                boolean isWall = isWallBlock(globalX, globalZ);

                if (isWall) {
                    // Create wall
                    for (int y = config.getBaseHeight(); y < config.getBaseHeight() + config.getWallHeight() + 1; y++) {
                        chunk.setBlock(blockX, y, blockZ, config.getWallMaterial());
                    }
                    // Add roof
                    chunk.setBlock(blockX, config.getBaseHeight() + config.getRoofOffset(), blockZ, config.getRoofMaterial());
                    // Add roof protection (bedrock above)
                    chunk.setBlock(blockX, config.getBaseHeight() + config.getRoofOffset() + 1, blockZ, Material.BEDROCK);
                } else {
                    // Create passage
                    chunk.setBlock(blockX, config.getBaseHeight(), blockZ, config.getFloorMaterial());
                    
                    // Add floor protection (bedrock underneath)
                    chunk.setBlock(blockX, config.getBaseHeight() - 1, blockZ, Material.BEDROCK);
                    
                    // Fill air with light
                    if (config.isFillWithLight()) {
                        for (int y = config.getBaseHeight() + 1; y < config.getBaseHeight() + config.getRoofOffset(); y++) {
                            chunk.setBlock(blockX, y, blockZ, Material.LIGHT);
                        }
                    }
                    
                    // Add roof
                    chunk.setBlock(blockX, config.getBaseHeight() + config.getRoofOffset(), blockZ, config.getRoofMaterial());
                    // Add roof protection (bedrock above)
                    chunk.setBlock(blockX, config.getBaseHeight() + config.getRoofOffset() + 1, blockZ, Material.BEDROCK);
                }
            }
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
            
            if (localX == 0) {
                // Vertical wall segment between cellX-1 and cellX
                if (localZ == mid) {
                    return !hasPassage(cellX - 1, cellZ, 1, 0);
                }
                return true;
            } else {
                // Horizontal wall segment between cellZ-1 and cellZ
                if (localX == mid) {
                    return !hasPassage(cellX, cellZ - 1, 0, 1);
                }
                return true;
            }
        }

        // Cell interior is always air (passage)
        return false;
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

