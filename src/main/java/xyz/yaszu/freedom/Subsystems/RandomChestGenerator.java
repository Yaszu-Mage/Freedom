package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Information.BaseInformation;
import xyz.yaszu.freedom.Information.Information_Handler;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.ItemListener;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static xyz.yaszu.freedom.Util.Util.keygen;

/**
 * system for generating chests
 */
public class RandomChestGenerator implements Listener {

    private static final double CHEST_FILL_CHANCE = 0.35;
    private static final double RELIC_CHANCE = 0.03;
    // How many loot items to place in a filled chest (min and max, inclusive)
    private static final int LOOT_MIN = 3;
    private static final int LOOT_MAX = 8;

    private final Random random = new Random();

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) {
            return;
        }
        Chunk chunk = event.getChunk();
        Bukkit.getScheduler().runTaskLater(Freedom.get_plugin(), () -> processChunk(chunk), 1L);
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof Chest chest)) {
            return;
        }

        Player player = event.getPlayer();
        if (!AdminManager.isSudo(player)) {
            return;
        }

        processChest(chest, true);
    }

    /**
     * processes if chunk has chest to generate
     * @param chunk
     */
    private void processChunk(Chunk chunk) {
        if (chunk == null || !chunk.isLoaded()) {
            return;
        }

        var states = chunk.getTileEntities();
        int chestCount = 0;
        for (var state : states) {
            if (state instanceof Chest) chestCount++;
        }

        for (var state : states) {
            if (state instanceof Chest chest) {
                processChest(chest, false);
            }
        }
    }

    /**
     * processes chest for if generated loot yet
     * @param chest chest checked
     * @param forceGenerate if force generating
     */
    private void processChest(Chest chest, boolean forceGenerate) {
        if (chest == null) {
            return;
        }

        boolean alreadyGenerated = chest.getPersistentDataContainer()
                .has(keygen("randomChestGenerated"), PersistentDataType.BYTE);

        // Skip if already processed, unless an admin is forcing a regeneration
        if (alreadyGenerated && !forceGenerate) {
            return;
        }

        // On force-generate, wipe existing contents so fresh loot can be placed
        Inventory inventory = chest.getBlockInventory();
        if (forceGenerate) {
            inventory.clear();
        }

        double roll = random.nextDouble();

        // Always mark as generated so this chest is not re-rolled on the next chunk reload
        chest.getPersistentDataContainer()
                .set(keygen("randomChestGenerated"), PersistentDataType.BYTE, (byte) 1);

        if (!forceGenerate && roll > CHEST_FILL_CHANCE) {
            chest.update(true, false);
            return;
        }

        // Place several loot items in random empty slots
        int lootCount = LOOT_MIN + random.nextInt(LOOT_MAX - LOOT_MIN + 1);

        int placed = 0;
        for (int i = 0; i < lootCount; i++) {
            int emptySlot = inventory.firstEmpty();
            if (emptySlot < 0) {
                break;
            }

            ItemStack loot = nextLoot();
            if (loot == null || loot.getType() == Material.AIR) {
                continue;
            }

            inventory.setItem(emptySlot, loot);
            placed++;
        }

        chest.update(true, false);
    }

    /**
     * checks if loot is added
     *
     * @return what loot is generated
     */
    private ItemStack nextLoot() {
        ItemStack relic = nextRelic();
        if (relic != null) {
            return relic;
        }

        List<ItemStack> lootPool = new ArrayList<>();

        for (Map.Entry<String, BaseItem> entry : ItemListener.ITEMS.entrySet()) {
            if (ItemListener.RELICS.containsKey(entry.getKey())) {
                continue;
            }
            ItemStack stack = entry.getValue().item();
            if (stack != null && stack.getType() != Material.AIR) {
                lootPool.add(stack.clone());
            }
        }

        for (BaseInformation information : Information_Handler.ITEMS.values()) {
            ItemStack stack = information.information();
            if (stack != null && stack.getType() != Material.AIR) {
                lootPool.add(stack.clone());
            }
        }

        if (lootPool.isEmpty()) {
            return null;
        }

        return lootPool.get(random.nextInt(lootPool.size())).clone();
    }

    /**
     * checks if relic is added to loot
     *
     * @return if relic is generated
     */
    private ItemStack nextRelic() {
        if (ItemListener.RELICS.isEmpty() || random.nextDouble() > RELIC_CHANCE) {
            return null;
        }

        List<Map.Entry<String, BaseItem>> availableRelics = new ArrayList<>();
        for (Map.Entry<String, BaseItem> entry : ItemListener.RELICS.entrySet()) {
            if (!isRelicSpawned(entry.getKey())) {
                availableRelics.add(entry);
            }
        }

        if (availableRelics.isEmpty()) {
            return null;
        }

        Map.Entry<String, BaseItem> chosen = availableRelics.get(random.nextInt(availableRelics.size()));
        ItemStack relic = chosen.getValue().item();
        if (relic == null || relic.getType() == Material.AIR) {
            return null;
        }

        markRelicSpawned(chosen.getKey());
        return relic.clone();
    }

    /**
     * checks if a relic is spawned
     *
     * @param id relic id
     * @return
     */
    private boolean isRelicSpawned(String id) {
        return ItemListener.getTrackedWorld()
                .getPersistentDataContainer()
                .has(keygen(ItemListener.RELIC_SPAWNED_PREFIX + id), PersistentDataType.BYTE);
    }

    /**
     * marks a relic as spawned
     * @param id id of spawned relic
     */
    private void markRelicSpawned(String id) {
        ItemListener.getTrackedWorld()
                .getPersistentDataContainer()
                .set(keygen(ItemListener.RELIC_SPAWNED_PREFIX + id), PersistentDataType.BYTE, (byte) 1);
        Freedom.get_plugin().getLogger().info("RandomChestGenerator: relic '" + id + "' marked as spawned");
    }
}