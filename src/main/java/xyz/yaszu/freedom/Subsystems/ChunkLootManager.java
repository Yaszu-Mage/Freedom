package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Information.BaseInformation;
import xyz.yaszu.freedom.Information.Information_Handler;
import xyz.yaszu.freedom.Util.StructureUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChunkLootManager implements Listener {
    private final Random random = new Random();

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return;
        // Chance to spawn a random structure
        int structureChance = random.nextInt(0,100);
        boolean structureChanceBool = structureChance > 95 && ((!event.getChunk().contains(Biome.OCEAN)) || !event.getChunk().contains(Biome.COLD_OCEAN) || !event.getChunk().contains(Biome.DEEP_OCEAN) || !event.getChunk().contains(Biome.DEEP_COLD_OCEAN) || !event.getChunk().contains(Biome.DEEP_FROZEN_OCEAN) || !event.getChunk().contains(Biome.FROZEN_OCEAN) || !event.getChunk().contains(Biome.DEEP_LUKEWARM_OCEAN)|| !event.getChunk().contains(Biome.LUKEWARM_OCEAN) || !event.getChunk().contains(Biome.WARM_OCEAN) || !event.getChunk().contains(Biome.RIVER) );
        if (!structureChanceBool) {
            return;
        } else {
            if (structureChanceBool) { // 10% chance for structure
                String worldName = event.getWorld().getName();
                Map<String, Freedom.StructureInfo> allStructures = Freedom.STRUCTURES;

                // Filter structures by world and calculate total weight
                float totalWeight = 0;
                List<Map.Entry<String, Freedom.StructureInfo>> validStructures = new ArrayList<>();
                for (Map.Entry<String, Freedom.StructureInfo> entry : allStructures.entrySet()) {
                    if (entry.getValue().worlds().contains(worldName)) {
                        validStructures.add(entry);
                        totalWeight += entry.getValue().weight();
                    }
                }

                if (!validStructures.isEmpty()) {
                    float rand = random.nextFloat() * totalWeight;
                    float currentWeight = 0;
                    String structureName = null;
                    for (Map.Entry<String, Freedom.StructureInfo> entry : validStructures) {
                        currentWeight += entry.getValue().weight();
                        if (rand < currentWeight) {
                            structureName = entry.getKey();
                            break;
                        }
                    }

                    if (structureName == null) return;

                    Chunk chunk = event.getChunk();
                    int rx = random.nextInt(16);
                    int rz = random.nextInt(16);
                    int x = (chunk.getX() << 4) + rx;
                    int z = (chunk.getZ() << 4) + rz;
                    int y = chunk.getWorld().getHighestBlockYAt(x, z);
                    Location loc = new Location(chunk.getWorld(), x, y, z);
                    if (loc.getBlock().getType() == Material.WATER && loc.getBlock().getType() != Material.ICE) {
                        Freedom.get_plugin().getLogger().info("Structure not placed in water");
                        return;
                    }
                    loc.add(0, 1, 0);
                    if (structureName.endsWith(".schem") || structureName.endsWith(".schematic")) {
                        Clipboard clipboard = StructureUtil.loadSchematicFromResource(structureName);
                        if (clipboard != null) {
                            StructureUtil.spawnSchematic(clipboard, loc);
                        }
                    } else if (structureName.endsWith(".nbt")) {
                        StructureUtil.spawnVanillaStructureFromResource(structureName, loc);
                    } else {
                        // Try .schem by default
                        Clipboard clipboard = StructureUtil.loadSchematicFromResource(structureName + ".schem");
                        if (clipboard != null) {
                            StructureUtil.spawnSchematic(clipboard, loc);
                        }
                    }

                    // Fill chests in the chunk after spawning structure
                    processChunk(chunk);
                }
            }

            int chance = random.nextInt(0, 100);
            boolean chance_bool = (chance > 95) && !(
                    event.getChunk().contains(Biome.OCEAN)
                            || event.getChunk().contains(Biome.COLD_OCEAN)
                            || event.getChunk().contains(Biome.DEEP_OCEAN)
                            || event.getChunk().contains(Biome.DEEP_COLD_OCEAN)
                            || event.getChunk().contains(Biome.DEEP_FROZEN_OCEAN)
                            || event.getChunk().contains(Biome.FROZEN_OCEAN)
                            || event.getChunk().contains(Biome.DEEP_LUKEWARM_OCEAN)
                            || event.getChunk().contains(Biome.LUKEWARM_OCEAN)
                            || event.getChunk().contains(Biome.BEACH));
            if (!chance_bool){
                return;
            }
            if (chance_bool){
                // Ensure we have items to add
                if (Information_Handler.ITEMS.isEmpty()) return;

                Chunk chunk = event.getChunk();

                // Randomly choose a location within the chunk (0-15)
                int rx = random.nextInt(16);
                int rz = random.nextInt(16);

                int x = (chunk.getX() << 4) + rx;
                int z = (chunk.getZ() << 4) + rz;
                int y = chunk.getWorld().getHighestBlockYAt(x, z);

                // Place a chest at y + 1 (on top of the highest block)
                Block block = chunk.getWorld().getBlockAt(x, y + 1, z);
                Freedom.get_plugin().getLogger().info("Placing chest at " + x + ", " + y + ", " + z);
                block.setType(Material.CHEST);

                if (block.getState() instanceof Chest chest) {
                    // 25% chance for the chest to contain information books
                    addRandomInformationBooks(chest);
                }
            }
        }

    }

    public int processChunks(Chunk centerChunk, int radius) {
        int chestCount = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Chunk chunk = centerChunk.getWorld().getChunkAt(centerChunk.getX() + dx, centerChunk.getZ() + dz);
                chestCount += processChunk(chunk);
            }
        }
        return chestCount;
    }

    public int processChunk(Chunk chunk) {
        int chestCount = 0;
        for (var state : chunk.getTileEntities()) {
            if (state instanceof Chest chest) {
                addRandomInformationBooks(chest);
                chestCount++;
            }
        }
        return chestCount;
    }

    private void addRandomInformationBooks(Chest chest) {
        List<BaseInformation> infoItems = new ArrayList<>(Information_Handler.ITEMS.values());
        if (infoItems.isEmpty()) return;

        Inventory inv = chest.getInventory();

        // Guaranteed at least one book
        int firstIndex = random.nextInt(infoItems.size());
        inv.addItem(infoItems.get(firstIndex).information());

        // Potential for additional books
        for (int i = 0; i < infoItems.size(); i++) {
            if (i == firstIndex) continue;
            if (random.nextInt(100) > 95) { // 10% chance for each other book
                inv.addItem(infoItems.get(i).information());
            }
        }
    }
}
