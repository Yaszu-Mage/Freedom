package xyz.yaszu.freedom.Alchemy;

import org.bukkit.Material;

/**
 * Configuration class for maze generation parameters.
 */
public class MazeConfig {

    private int cellSize;
    private int wallHeight;
    private int roofOffset;
    private Material wallMaterial;
    private Material floorMaterial;
    private Material roofMaterial;
    private Material doorMaterial;
    private int baseHeight;
    private int lightLevel;
    private boolean fillWithLight;
    private double doorChance;
    private long seedOffset;

    public MazeConfig() {
        this.cellSize = 8;
        this.wallHeight = 4;
        this.roofOffset = 5;
        this.wallMaterial = Material.SMOOTH_SANDSTONE;
        this.floorMaterial = Material.STRIPPED_BIRCH_WOOD;
        this.roofMaterial = Material.SMOOTH_STONE;
        this.doorMaterial = Material.SMOOTH_SANDSTONE_STAIRS;
        this.baseHeight = 64;
        this.lightLevel = 15;
        this.fillWithLight = true;
        this.doorChance = 0.2;
        this.seedOffset = 12345L;
    }

    public int getCellSize() { return cellSize; }
    public void setCellSize(int cellSize) { this.cellSize = cellSize; }

    public int getWallHeight() { return wallHeight; }
    public void setWallHeight(int wallHeight) { this.wallHeight = wallHeight; }

    public int getRoofOffset() { return roofOffset; }
    public void setRoofOffset(int roofOffset) { this.roofOffset = roofOffset; }

    public Material getWallMaterial() { return wallMaterial; }
    public void setWallMaterial(Material material) { this.wallMaterial = material; }

    public Material getFloorMaterial() { return floorMaterial; }
    public void setFloorMaterial(Material material) { this.floorMaterial = material; }

    public Material getRoofMaterial() { return roofMaterial; }
    public void setRoofMaterial(Material material) { this.roofMaterial = material; }

    public Material getDoorMaterial() { return doorMaterial; }
    public void setDoorMaterial(Material material) { this.doorMaterial = material; }

    public int getBaseHeight() { return baseHeight; }
    public void setBaseHeight(int baseHeight) { this.baseHeight = baseHeight; }

    public int getLightLevel() { return lightLevel; }
    public void setLightLevel(int level) { this.lightLevel = Math.max(0, Math.min(15, level)); }

    public boolean isFillWithLight() { return fillWithLight; }
    public void setFillWithLight(boolean fill) { this.fillWithLight = fill; }

    public double getDoorChance() { return doorChance; }
    public void setDoorChance(double chance) { this.doorChance = chance; }

    public long getSeedOffset() { return seedOffset; }
    public void setSeedOffset(long offset) { this.seedOffset = offset; }

    /**
     * Creates a builder for fluent configuration
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for MazeConfig
     */
    public static class Builder {
        private final MazeConfig config = new MazeConfig();

        public Builder cellSize(int size) {
            config.cellSize = size;
            return this;
        }

        public Builder wallHeight(int height) {
            config.wallHeight = height;
            return this;
        }

        public Builder roofOffset(int offset) {
            config.roofOffset = offset;
            return this;
        }

        public Builder wallMaterial(Material material) {
            config.wallMaterial = material;
            return this;
        }

        public Builder floorMaterial(Material material) {
            config.floorMaterial = material;
            return this;
        }

        public Builder roofMaterial(Material material) {
            config.roofMaterial = material;
            return this;
        }

        public Builder doorMaterial(Material material) {
            config.doorMaterial = material;
            return this;
        }

        public Builder baseHeight(int height) {
            config.baseHeight = height;
            return this;
        }

        public Builder lightLevel(int level) {
            config.lightLevel = Math.max(0, Math.min(15, level));
            return this;
        }

        public Builder fillWithLight(boolean fill) {
            config.fillWithLight = fill;
            return this;
        }

        public Builder doorChance(double chance) {
            config.doorChance = chance;
            return this;
        }

        public Builder seedOffset(long offset) {
            config.seedOffset = offset;
            return this;
        }

        public MazeConfig build() {
            return config;
        }
    }
}

