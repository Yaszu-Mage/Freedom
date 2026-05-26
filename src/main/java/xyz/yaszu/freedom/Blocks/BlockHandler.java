package xyz.yaszu.freedom.Blocks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.yaszu.freedom.Blocks.AdminPlush.*;
import xyz.yaszu.freedom.Blocks.Silly.Broker;
import xyz.yaszu.freedom.Blocks.Silly.Duck;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.BlockMapPersistentDataType;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

import static xyz.yaszu.freedom.Util.Util.keygen;

public class BlockHandler extends Util implements Listener {

    // --- Coordinate key record for fast, exact block-position lookups ---
    private record BlockPos(UUID worldId, int x, int y, int z) {
        static BlockPos of(Location loc) {
            return new BlockPos(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    // Runtime maps keyed by BlockPos — no yaw/pitch confusion, O(1) lookup
    public static final Map<BlockPos, UUID>      currentCustomBlocks = new HashMap<>();
    public static final Map<BlockPos, BaseBlock> currentCustomData   = new HashMap<>();  // avoid re-reading PDC on every move

    public static final Map<String, BaseBlock> ITEMS = new HashMap<>();
    public static final Map<UUID, Long>        cooldowns = new HashMap<>();

    private static final int BLOCK_PLACE_COOLDOWN_MS = 100;

    public BlockHandler() {
        register(new Duck(),          "duck");
        register(new Broker(),        "broker");
        register(new ZanePlush(),     "zaneplush");
        register(new YaszuPlush(),    "yaszuplush");
        register(new SylPlush(),      "sylplush");
        register(new NitroPlush(),    "nitroplush");
        register(new GhostPlush(),    "ghostplush");
        register(new AugurmovPlush(), "augurmovplush");
    }

    // -------------------------------------------------------------------------
    // PDC helpers
    // -------------------------------------------------------------------------

    private static void storeDisplayUUID(Location loc, UUID uuid) {
        String key = pdcKey(loc);
        loc.getWorld().getPersistentDataContainer()
                .set(keygen(key), PersistentDataType.STRING, uuid.toString());
    }

    private static UUID getStoredDisplayUUID(Location loc) {
        String key = pdcKey(loc);
        var pdc = loc.getWorld().getPersistentDataContainer();
        if (pdc.has(keygen(key))) {
            return UUID.fromString(pdc.get(keygen(key), PersistentDataType.STRING));
        }
        return null;
    }

    private static void removeDisplayUUID(Location loc) {
        String key = pdcKey(loc);
        var pdc = loc.getWorld().getPersistentDataContainer();
        if (pdc.has(keygen(key))) pdc.remove(keygen(key));
    }

    private static void storeRotation(Location loc, float yaw) {
        loc.getWorld().getPersistentDataContainer()
                .set(keygen(rotKey(loc)), PersistentDataType.FLOAT, yaw);
    }

    private static float restoreRotation(Location loc) {
        var pdc = loc.getWorld().getPersistentDataContainer();
        var k   = keygen(rotKey(loc));
        return pdc.has(k) ? pdc.get(k, PersistentDataType.FLOAT) : 0f;
    }

    private static void removeRotation(Location loc) {
        var pdc = loc.getWorld().getPersistentDataContainer();
        var k   = keygen(rotKey(loc));
        if (pdc.has(k)) pdc.remove(k);
    }

    private static String pdcKey(Location loc) {
        return "blockDisplay." + loc.getBlockX() + "." + loc.getBlockY() + "." + loc.getBlockZ();
    }

    private static String rotKey(Location loc) {
        return "blockRot." + loc.getBlockX() + "." + loc.getBlockY() + "." + loc.getBlockZ();
    }

    // -------------------------------------------------------------------------
    // Registration & restore
    // -------------------------------------------------------------------------

    public void register(BaseBlock block, String id) {
        ITEMS.put(id, block);
        if (block.behavior() == BaseBlock.Behavior.Interface) {
            Bukkit.getPluginManager().registerEvents((Listener) block, Freedom.get_plugin());
        }
    }


    private static final Set<UUID> restoredWorlds = new HashSet<>();
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getChunk().getWorld();
        // Only restore once per world per server session
        if (!restoredWorlds.contains(world.getUID())) {
            restoredWorlds.add(world.getUID());
            // Delay slightly so the world is fully loaded before we scan
            Bukkit.getScheduler().runTaskLater(Freedom.get_plugin(), () -> restore(world), 20L);
        }
    }

    public void restore(World world) {
        var pdc = world.getPersistentDataContainer();

        // --- Optimized Orphan Purge ---
        // 1. Collect all display UUIDs from the world that are marked as custom blocks.
        Set<UUID> allDisplayUuidsInWorld = new HashSet<>();
        for (ItemDisplay display : world.getEntitiesByClass(ItemDisplay.class)) {
            if (display.getPersistentDataContainer().has(keygen("customBlock"), PersistentDataType.STRING)) {
                allDisplayUuidsInWorld.add(display.getUniqueId());
            }
        }

        // 2. If there are no custom blocks in PDC, remove all custom block displays and exit.
        if (!pdc.has(keygen("customBlocks"))) {
            for (UUID displayUuid : allDisplayUuidsInWorld) {
                Entity e = Bukkit.getEntity(displayUuid);
                if (e != null) e.remove();
            }
            return;
        }

        Map<Location, BaseBlock> blockMap = pdc.get(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE);
        if (blockMap == null || blockMap.isEmpty()) {
            for (UUID displayUuid : allDisplayUuidsInWorld) {
                Entity e = Bukkit.getEntity(displayUuid);
                if (e != null) e.remove();
            }
            return;
        }

        // 3. Collect all KNOWN display UUIDs from the blockMap's PDC entries.
        Set<UUID> knownDisplayUuids = new HashSet<>();
        for (Location loc : blockMap.keySet()) {
            UUID storedUuid = getStoredDisplayUUID(loc);
            if (storedUuid != null) {
                knownDisplayUuids.add(storedUuid);
            }
        }

        // 4. Iterate through all found display UUIDs and remove any that are NOT in our known set.
        for (UUID displayUuid : allDisplayUuidsInWorld) {
            if (!knownDisplayUuids.contains(displayUuid)) {
                Entity e = Bukkit.getEntity(displayUuid);
                if (e != null) {
                    e.remove();
                }
            }
        }

        // Now restore only what's in the block map
        blockMap.forEach((location, baseBlock) -> {
            BlockPos pos = BlockPos.of(location);

            // Already tracked in runtime map with a live entity — skip
            if (currentCustomBlocks.containsKey(pos)) {
                Entity e = Bukkit.getEntity(currentCustomBlocks.get(pos));
                if (e instanceof ItemDisplay && !e.isDead()) {
                    currentCustomData.put(pos, baseBlock);
                    return; // Already live and tracked, do nothing.
                }
            }

            // Check persisted UUID
            UUID stored = getStoredDisplayUUID(location);
            if (stored != null) {
                Entity e = Bukkit.getEntity(stored);
                if (e instanceof ItemDisplay && !e.isDead()) {
                    // Found a live entity, let's start tracking it.
                    currentCustomBlocks.put(pos, stored);
                    currentCustomData.put(pos, baseBlock);
                    return; // Found, do nothing more.
                }
            }

            // Nothing alive — spawn fresh
            ConstructBlock(baseBlock, location, null);
        });
    }

    // -------------------------------------------------------------------------
    // Events
    // -------------------------------------------------------------------------

    /**
     * PlayerMoveEvent fires every tick. Keep it as cheap as possible:
     *  - Only check when Y decreases (falling).
     *  - Use the fast BlockPos map — no PDC read here.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Bail fast if the player isn't descending
        if (event.getFrom().getY() <= event.getTo().getY()) return;

        Player   player        = event.getPlayer();
        Location below         = player.getLocation().subtract(0, 1, 0);
        BlockPos pos           = BlockPos.of(below.getBlock().getLocation());

        UUID blockDisplayUUID  = currentCustomBlocks.get(pos);
        if (blockDisplayUUID == null) return;

        BaseBlock baseBlock = currentCustomData.get(pos);
        if (baseBlock == null || baseBlock.behavior() != BaseBlock.Behavior.Farm) return;

        trample(baseBlock, below.getBlock().getLocation(), player);
    }

    private void trample(BaseBlock baseBlock, Location location, Player player) {
        BlockPos pos = BlockPos.of(location);

        UUID displayUUID = currentCustomBlocks.get(pos);
        if (displayUUID != null) {
            Entity e = Bukkit.getEntity(displayUUID);
            if (e != null) e.remove();
        }

        // Update PDC blockMap
        var pdc = location.getWorld().getPersistentDataContainer();
        Map<Location, BaseBlock> blockMap = pdc.get(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE);
        if (blockMap != null) {
            blockMap.remove(location);
            pdc.set(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE, blockMap);
        }

        removeDisplayUUID(location);
        removeRotation(location);
        currentCustomBlocks.remove(pos);
        currentCustomData.remove(pos);

        location.getWorld().playSound(location, Sound.BLOCK_GRASS_BREAK, 1f, 1f);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        BlockPos pos      = BlockPos.of(location);

        // Fast check against in-memory map first
        if (!currentCustomBlocks.containsKey(pos)) return;

        var   world    = location.getWorld();
        var   pdc      = world.getPersistentDataContainer();
        Map<Location, BaseBlock> blockMap = pdc.get(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE);
        if (blockMap == null) return;

        // Find the stored key (Location equality is fragile — match by coords)
        Location matchedKey = null;
        BaseBlock baseBlock  = null;
        for (var entry : blockMap.entrySet()) {
            Location k = entry.getKey();
            if (k.getWorld() != null
                    && k.getWorld().getUID().equals(world.getUID())
                    && k.getBlockX() == location.getBlockX()
                    && k.getBlockY() == location.getBlockY()
                    && k.getBlockZ() == location.getBlockZ()) {
                matchedKey = k;
                baseBlock  = entry.getValue();
                break;
            }
        }
        if (matchedKey == null) return;

        // Remove from PDC blockMap
        blockMap.remove(matchedKey);
        pdc.set(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE, blockMap);

        // Remove display entity — prefer stored UUID (fast path), no getNearbyEntities scanning
        UUID displayUUID = currentCustomBlocks.get(pos);
        if (displayUUID == null) displayUUID = getStoredDisplayUUID(matchedKey);

        if (displayUUID != null) {
            Entity e = Bukkit.getEntity(displayUUID);
            if (e != null) e.remove();
        } else {
            Freedom.get_plugin().getLogger().warning(
                    "BlockHandler: no UUID found for custom block at "
                            + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
                            + " — display may be orphaned.");
        }

        // Clean up all state
        removeDisplayUUID(matchedKey);
        removeRotation(location);
        currentCustomBlocks.remove(pos);
        currentCustomData.remove(pos);

        event.setDropItems(false);
        world.dropItemNaturally(location, baseBlock.block());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        if (hasBlockInHand(player)) {
            long now = System.currentTimeMillis();
            if (cooldowns.getOrDefault(player.getUniqueId(), 0L) + BLOCK_PLACE_COOLDOWN_MS > now) return;

            ItemStack item = player.getInventory().getItemInMainHand();
            String    id   = item.getPersistentDataContainer().get(keygen("customBlock"), PersistentDataType.STRING);
            BaseBlock baseBlock = ITEMS.get(id);
            if (baseBlock == null) return;

            cooldowns.put(player.getUniqueId(), now);
            Place(baseBlock, event.getClickedBlock().getLocation().add(0, 1, 0), player);
            player.getInventory().getItemInMainHand().subtract(1);

            Object sound = baseBlock.placeSound();
            if (sound instanceof String s)  player.getWorld().playSound(player.getLocation(), s, 1, 1);
            if (sound instanceof Sound  s)  player.getWorld().playSound(player.getLocation(), s, 1, 1);

            event.setCancelled(true);

        } else {
            // Bare hand — check for Interface block
            if (event.getClickedBlock() == null) return;
            BlockPos pos = BlockPos.of(event.getClickedBlock().getLocation());
            if (!currentCustomBlocks.containsKey(pos)) return;

            BaseBlock baseBlock = currentCustomData.get(pos);
            if (baseBlock != null && baseBlock.behavior() == BaseBlock.Behavior.Interface) {
                player.openInventory(baseBlock.inventoryHolder().getInventory());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Placement helpers
    // -------------------------------------------------------------------------

    public boolean hasBlockInHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item != null && item.getPersistentDataContainer().has(keygen("customBlock"));
    }

    public static boolean canPlace(BaseBlock baseBlock, Location location) {
        if (!location.isBlock()) {
            if (baseBlock.waterNeeded()) {
                Block block = location.getBlock();
                return block != null && block.isLiquid();
            }
            return location.getBlock() == null || location.getBlock().getType() == Material.AIR;
        }
        return false;
    }

    public static void Place(BaseBlock baseBlock, Location location, Player player) {
        if (!canPlace(baseBlock, location)) return;

        switch (baseBlock.collisionSize()) {
            case Itsy  -> location.getBlock().setType(Material.STRUCTURE_VOID);
            case Teeny -> location.getBlock().setType(Material.FLOWER_POT);
            case Large -> {
                location.getBlock().setType(switch (baseBlock.toolValue()) {
                    case 0  -> Material.OAK_LEAVES;
                    case 1  -> Material.OAK_PLANKS;
                    case 2  -> Material.STONE;
                    case 3  -> Material.IRON_BLOCK;
                    default -> Material.OBSIDIAN;
                });
            }
            case Small -> location.getBlock().setType(
                    baseBlock.toolValue() == 0 ? Material.OAK_TRAPDOOR : Material.IRON_TRAPDOOR);
            case Medium -> location.getBlock().setType(
                    baseBlock.toolValue() == 0 ? Material.OAK_SLAB : Material.STONE_SLAB);
        }

        StoreBlock(baseBlock, location);
        ConstructBlock(baseBlock, location, player);
    }

    private static void ConstructBlock(BaseBlock baseBlock, Location location, Player player) {
        Location spawnLocation = location.clone().add(0.5, baseBlock.scale() / 2.0, 0.5);

        float yaw;
        if (player != null) {
            yaw = snapTo90(-player.getLocation().getYaw());
            storeRotation(location, yaw);
        } else {
            yaw = restoreRotation(location);
        }

        AxisAngle4f rotation = new AxisAngle4f((float) Math.toRadians(yaw), 0, -1, 0);

        ItemDisplay display = (ItemDisplay) location.getWorld().spawnEntity(spawnLocation, EntityType.ITEM_DISPLAY);
        display.setPersistent(true);
        display.setItemStack(baseBlock.block());
        float s = (float) baseBlock.scale();
        display.setTransformation(new Transformation(
                display.getTransformation().getTranslation(),
                new Quaternionf(rotation),
                new Vector3f(s, s, s),
                display.getTransformation().getRightRotation()
        ));

        storeDisplayUUID(location, display.getUniqueId());
        BlockPos pos = BlockPos.of(location);
        currentCustomBlocks.put(pos, display.getUniqueId());
        currentCustomData.put(pos, baseBlock);
    }

    private static float snapTo90(float yaw) {
        yaw = ((yaw % 360) + 360) % 360;
        return Math.round(yaw / 90.0f) * 90f;
    }

    public static void StoreBlock(BaseBlock baseBlock, Location location) {
        World world = location.getWorld();
        var   pdc   = world.getPersistentDataContainer();
        Map<Location, BaseBlock> blockMap;
        if (pdc.has(keygen("customBlocks"))) {
            blockMap = pdc.get(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE);
        } else {
            blockMap = new HashMap<>();
        }
        blockMap.put(location, baseBlock);
        pdc.set(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE, blockMap);
    }
}