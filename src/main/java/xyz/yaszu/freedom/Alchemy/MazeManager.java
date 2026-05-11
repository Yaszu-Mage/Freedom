package xyz.yaszu.freedom.Alchemy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager for maze world creation and configuration.
 * Handles creation of infinite maze worlds with custom parameters.
 */
public class MazeManager {

    private static final Map<String, MazeConfig> MAZE_CONFIGS = new HashMap<>();
    private static final Map<String, MazeGenerator> MAZE_GENERATORS = new HashMap<>();

    /**
     * Creates a maze world with the specified name using default configuration
     */
    public static World createMazeWorld(String worldName) {
        return createMazeWorld(worldName, new MazeConfig());
    }

    /**
     * Creates a maze world with the specified name and custom configuration
     */
    public static World createMazeWorld(String worldName, MazeConfig config) {
        // Store the configuration
        MAZE_CONFIGS.put(worldName, config);

        // Create the generator
        MazeGenerator generator = new MazeGenerator(config);
        MAZE_GENERATORS.put(worldName, generator);

        // Create the world
        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.generator(generator);

        World world = worldCreator.createWorld();

        if (world != null) {
            world.setStorm(false);
            world.setThundering(false);
            world.setDifficulty(org.bukkit.Difficulty.HARD);
            world.setSpawnLocation(world.getSpawnLocation().getBlockX(), world.getSpawnLocation().getBlockY() - 3, world.getSpawnLocation().getBlockZ());
        }

        return world;
    }

    /**
     * Creates a maze world with builder pattern configuration
     */
    public static World createMazeWorld(String worldName, MazeConfig.Builder builder) {
        return createMazeWorld(worldName, builder.build());
    }

    /**
     * Gets the configuration for a maze world
     */
    public static MazeConfig getMazeConfig(String worldName) {
        return MAZE_CONFIGS.get(worldName);
    }

    /**
     * Gets the generator for a maze world
     */
    public static MazeGenerator getMazeGenerator(String worldName) {
        return MAZE_GENERATORS.get(worldName);
    }

    /**
     * Creates a preset maze world configuration
     */
    public static World createPresetMaze(String worldName, MazePreset preset) {
        return createMazeWorld(worldName, preset.getConfig());
    }

    /**
     * Enum for preset maze configurations
     */
    public enum MazePreset {
        DEFAULT(new MazeConfig()),

        LARGE_CELLS(MazeConfig.builder()
            .cellSize(8)
            .wallHeight(6)
            .roofOffset(7)
            .build()),

        TIGHT_MAZE(MazeConfig.builder()
            .cellSize(2)
            .wallHeight(3)
            .roofOffset(4)
            .build()),

        STONE_MAZE(MazeConfig.builder()
            .wallMaterial(Material.STONE)
            .floorMaterial(Material.STONE)
            .roofMaterial(Material.SMOOTH_STONE)
            .build()),

        DIORITE_MAZE(MazeConfig.builder()
            .wallMaterial(Material.DIORITE)
            .floorMaterial(Material.DIORITE)
            .roofMaterial(Material.POLISHED_DIORITE)
            .build()),

        DARK_MAZE(MazeConfig.builder()
            .wallMaterial(Material.BLACKSTONE)
            .floorMaterial(Material.BLACKSTONE)
            .roofMaterial(Material.POLISHED_BLACKSTONE)
            .lightLevel(15)
            .build()),

        TALL_MAZE(MazeConfig.builder()
            .wallHeight(10)
            .roofOffset(12)
            .cellSize(6)
            .build()),

        DEEP_MAZE(MazeConfig.builder()
            .baseHeight(20)
            .wallHeight(8)
            .roofOffset(10)
            .build());

        private final MazeConfig config;

        MazePreset(MazeConfig config) {
            this.config = config;
        }

        public MazeConfig getConfig() {
            return config;
        }
    }
}

