package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Information.BaseInformation;
import xyz.yaszu.freedom.Information.Information_Handler;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChunkLootManager implements Listener {
    private final Random random = new Random();

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return;
        int chance = random.nextInt(0, 100);
        boolean chance_bool = chance > 95;
        if (chance_bool) {
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
            block.setType(Material.CHEST);

            if (block.getState() instanceof Chest chest) {
                // 25% chance for the chest to contain information books
                addRandomInformationBooks(chest);
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
