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
import java.util.concurrent.atomic.AtomicReference;

import static xyz.yaszu.freedom.Util.Util.keygen;

public class BlockHandler extends Util implements Listener {
    public BlockHandler() {

        register(new Duck(), "duck");
        register(new Broker(), "broker");
        register(new ZanePlush(), "zaneplush");
        register(new YaszuPlush(), "yaszuplush");
        register(new SylPlush(),"sylplush");
        register(new NitroPlush(), "nitroplush");
        register(new GhostPlush(), "ghostplush");
        register(new AugurmovPlush(), "augurmovplush");

    }

    int blockplaceCooldown = 100;
    public static HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static void storeDisplayUUID(Location location, UUID uuid) {
        World world = location.getWorld();
        String key = "blockDisplay." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();
        world.getPersistentDataContainer().set(keygen(key), PersistentDataType.STRING, uuid.toString());
    }

    private static UUID getStoredDisplayUUID(Location location) {
        World world = location.getWorld();
        String key = "blockDisplay." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();
        if (world.getPersistentDataContainer().has(keygen(key))) {
            return UUID.fromString(world.getPersistentDataContainer().get(keygen(key), PersistentDataType.STRING));
        }
        return null;
    }

    private static void removeDisplayUUID(Location location) {
        World world = location.getWorld();
        String key = "blockDisplay." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();
        if (world.getPersistentDataContainer().has(keygen(key))) {
            world.getPersistentDataContainer().remove(keygen(key));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location below = player.getLocation().subtract(0, 1, 0);
        Location blockLocation = below.getBlock().getLocation();

        if (!currentCustomBlocks.containsKey(blockLocation)) return;

        World world = blockLocation.getWorld();
        Map<Location, BaseBlock> blockMap = world.getPersistentDataContainer()
                .get(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE);

        if (blockMap == null || !blockMap.containsKey(blockLocation)) return;

        BaseBlock baseBlock = blockMap.get(blockLocation);

        if (baseBlock.behavior() != BaseBlock.Behavior.Farm) return;

        // Only trample if falling onto the block
        if (event.getFrom().getY() > event.getTo().getY()) {
            trample(baseBlock, blockLocation, player);
        }
    }

    private void trample(BaseBlock baseBlock, Location location, Player player) {
        // Remove the display
        UUID displayUUID = currentCustomBlocks.get(location);
        if (displayUUID != null) {
            Entity entity = Bukkit.getEntity(displayUUID);
            if (entity != null) entity.remove();
        }

        // Remove from storage
        World world = location.getWorld();
        Map<Location, BaseBlock> blockMap = world.getPersistentDataContainer()
                .get(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE);
        if (blockMap != null) {
            blockMap.remove(location);
            world.getPersistentDataContainer()
                    .set(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE, blockMap);
        }

        removeRotation(location);
        currentCustomBlocks.remove(location);

        // Revert underlying block to dirt, play effect
        world.playSound(location, Sound.BLOCK_GRASS_BREAK, 1f, 1f);
    }


    public HashMap<Location, BaseBlock> currentDrops = new HashMap<>();

    public static HashMap<Location, UUID> currentCustomBlocks = new HashMap<>();
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        restore(event.getChunk().getWorld());
    }
    public static final Map<String, BaseBlock> ITEMS = new HashMap<>();
    public ItemDisplay getItemDisplay(Location location) {
        return location.getNearbyEntitiesByType(ItemDisplay.class,0.1).stream().findFirst().orElse(null);
    }


    private static float snapTo90(float yaw) {
        // Normalize yaw to 0-360
        yaw = ((-yaw % 360) + 360) % 360;
        // Round to nearest 90
        return Math.round(yaw / 90.0f) * 90f;
    }

    public void restore(World world) {
        if (world.getPersistentDataContainer().has(keygen("customBlocks"))) {
            Map<Location, BaseBlock> blockMap = world.getPersistentDataContainer()
                    .get(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE);
            assert blockMap != null;
            blockMap.forEach((location, baseBlock) -> {
                UUID storedUUID = getStoredDisplayUUID(location);
                if (storedUUID != null) {
                    Entity entity = Bukkit.getEntity(storedUUID);
                    if (entity instanceof ItemDisplay) {
                        // Already exists, just retrack
                        currentCustomBlocks.put(location, storedUUID);
                        return;
                    }
                }
                // Gone or never stored, spawn fresh
                ConstructBlock(baseBlock, location, null);
            });
        }
    }


    public void register(BaseBlock baseBlock, String id) {
        ITEMS.put(id, baseBlock);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        if (location.getWorld().getPersistentDataContainer().has(keygen("customBlocks"))) {
            Map<Location, BaseBlock> blockMap = location.getWorld()
                    .getPersistentDataContainer()
                    .get(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE);
            assert blockMap != null;
            if (blockMap.containsKey(location)) {
                BaseBlock baseBlock = blockMap.get(location);
                blockMap.remove(location);
                location.getWorld().getPersistentDataContainer()
                        .set(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE, blockMap);

                // Remove display by stored UUID
                UUID displayUUID = getStoredDisplayUUID(location);
                if (displayUUID != null) {
                    Entity entity = Bukkit.getEntity(displayUUID);
                    if (entity != null) entity.remove();
                }

                removeDisplayUUID(location);
                removeRotation(location);
                currentCustomBlocks.remove(location);
                event.setDropItems(false);
                location.getWorld().dropItemNaturally(location, baseBlock.block());
            }
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Freedom.get_plugin().getLogger().info("INTERACT");
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (hasBlockInHand(player) && cooldowns.getOrDefault(player.getUniqueId(), 0L) + blockplaceCooldown < System.currentTimeMillis()) {
                ItemStack item = player.getInventory().getItemInMainHand();
                String id = item.getPersistentDataContainer().get(keygen("customBlock"), PersistentDataType.STRING);
                BaseBlock baseBlock = ITEMS.get(id);
                if (baseBlock != null) {
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

                Freedom.get_plugin().getLogger().info("BLOCK" + baseBlock);
                Place(baseBlock, event.getClickedBlock().getLocation().add(0, 1, 0), player);
                player.getInventory().getItemInMainHand().subtract(1);
                if (baseBlock.placeSound() instanceof String sound) {
                    player.getWorld().playSound(player.getLocation(), sound, 1, 1);
                }
                if (baseBlock.placeSound() instanceof Sound sound) {
                    player.getWorld().playSound(player.getLocation(), sound, 1, 1);
                }}
                Freedom.get_plugin().getLogger().info("PLACING");
                event.setCancelled(true);
            }
        }
        }



    public boolean hasBlockInHand(Player player) {
        if (player.getInventory().getItemInMainHand() != null) {
            if (player.getInventory().getItemInMainHand().getPersistentDataContainer().has(keygen("customBlock"))) {
                return true;
            }
        }
        return false;
    }


    public static boolean canPlace(BaseBlock baseBlock, Location location) {
        if (!location.isBlock()){
            if (baseBlock.waterNeeded()) {
                Block block = (location.getBlock() != null) ? location.getBlock() : location.getWorld().getBlockAt(new Location(Bukkit.getWorld("world"),0,-64,0));
                if (block.isLiquid()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return location.getBlock() == null || location.getBlock().getType() == Material.AIR;
            }
        }
        return false;
    }

    public static void Place(BaseBlock baseBlock, Location location, Player player) {
        if (canPlace(baseBlock, location)) {
            switch (baseBlock.collisionSize()) {
                case Itsy -> location.getBlock().setType(Material.STRUCTURE_VOID);
                case Teeny -> location.getBlock().setType(Material.FLOWER_POT);
                case Large -> {
                    switch (baseBlock.toolValue()) {
                        case 0 -> location.getBlock().setType(Material.OAK_LEAVES);
                        case 1 -> location.getBlock().setType(Material.OAK_PLANKS);
                        case 2 -> location.getBlock().setType(Material.STONE);
                        case 3 -> location.getBlock().setType(Material.IRON_BLOCK);
                        default -> location.getBlock().setType(Material.OBSIDIAN);
                    }
                }
                case Small -> {
                    if (baseBlock.toolValue() == 0) {
                        location.getBlock().setType(Material.OAK_TRAPDOOR);
                    } else {
                        location.getBlock().setType(Material.IRON_TRAPDOOR);
                    }
                }
                case Medium -> {
                    if (baseBlock.toolValue() == 0) {
                        location.getBlock().setType(Material.OAK_SLAB);
                    } else {
                        location.getBlock().setType(Material.STONE_SLAB);
                    }
                }
            }

            StoreBlock(baseBlock, location);
            ConstructBlock(baseBlock, location, player);
        }
    }

    private static void ConstructBlock(BaseBlock baseBlock, Location location, Player player) {
        // Compute final position upfront, no teleport needed
        Location spawnLocation = location.clone().add(0.5, baseBlock.scale() / 2, 0.5);

        float yaw = 0f;
        if (player != null) {
            yaw = BlockHandler.snapTo90(player.getLocation().getYaw());
            storeRotation(location, yaw);
        } else {
            yaw = restoreRotation(location);
        }

        AxisAngle4f rotation = new AxisAngle4f(
                (float) Math.toRadians(yaw),
                0, -1, 0
        );

        ItemDisplay display = (ItemDisplay) location.getWorld().spawnEntity(spawnLocation, EntityType.ITEM_DISPLAY);
        display.setPersistent(true);
        display.setItemStack(baseBlock.block());
        display.setTransformation(new Transformation(
                display.getTransformation().getTranslation(),
                new Quaternionf(rotation),
                new Vector3f((float) baseBlock.scale(), (float) baseBlock.scale(), (float) baseBlock.scale()),
                display.getTransformation().getRightRotation()
        ));

        // Store UUID in both the runtime map and world PDC
        storeDisplayUUID(location, display.getUniqueId());
        BlockHandler.currentCustomBlocks.put(location, display.getUniqueId());
    }

    private static void storeRotation(Location location, float yaw) {
        World world = location.getWorld();
        // Key per location so each block has its own rotation entry
        String key = "blockRot." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();
        world.getPersistentDataContainer().set(
                keygen(key),
                PersistentDataType.FLOAT,
                yaw
        );
    }

    private static float restoreRotation(Location location) {
        World world = location.getWorld();
        String key = "blockRot." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();
        if (world.getPersistentDataContainer().has(keygen(key))) {
            return world.getPersistentDataContainer().get(keygen(key), PersistentDataType.FLOAT);
        }
        return 0f;
    }

    // Call this in onBlockBreak to clean up the rotation entry
    private static void removeRotation(Location location) {
        World world = location.getWorld();
        String key = "blockRot." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();
        if (world.getPersistentDataContainer().has(keygen(key))) {
            world.getPersistentDataContainer().remove(keygen(key));
        }
    }

    public static void StoreBlock(BaseBlock baseBlock, Location location) {
        if (location.getWorld().getPersistentDataContainer().has(keygen("customBlocks"))) {
            World world = location.getWorld();
            Map<Location,BaseBlock> blockMap = world.getPersistentDataContainer().get(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE);
            blockMap.put(location,baseBlock);
            world.getPersistentDataContainer().set(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE, blockMap);
        } else {
            World world = location.getWorld();
            Map<Location,BaseBlock> blockMap = new HashMap<>();
            blockMap.put(location,baseBlock);
            world.getPersistentDataContainer().set(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE, blockMap);
        }
    }
}
