package xyz.yaszu.freedom.Blocks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Util.BlockMapPersistentDataType;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class BlockHandler extends Util implements Listener {
    public BlockHandler() {

    }

    public ItemDisplay getItemDisplay(Location location) {
        AtomicReference<ItemDisplay> entity = new AtomicReference<ItemDisplay>();
        location.getNearbyEntitiesByType(ItemDisplay.class,1).forEach(e -> {
            if (e.getLocation().distance(location) < 0.5) {
                entity.set(e);
            }
        });
        return entity.get();
    }

    public void restore(World world) {
        if (world.getPersistentDataContainer().has(keygen("customBlocks"))) {
            Map<Location,BaseBlock> blockMap = world.getPersistentDataContainer().get(keygen("customBlocks"), BlockMapPersistentDataType.INSTANCE);
            blockMap.forEach((location, baseBlock) -> {
                if (getItemDisplay(location) == null) {


                }
            });
        }
    }

    public void restoreLocation(Location location, BaseBlock baseBlock) {

    }

    public boolean canPlace(BaseBlock baseBlock, Location location) {
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

    public void Place(BaseBlock baseBlock, Location location) {
        if (canPlace(baseBlock, location)) {
            switch (baseBlock.collisionSize()) {
                case Large -> {
                    //depends
                    switch (baseBlock.toolValue()) {
                        case 0 -> {
                            //oak leaf
                            location.getBlock().setType(Material.OAK_LEAVES);
                        }
                        case 1 -> {
                            //Oak Planks
                            location.getBlock().setType(Material.OAK_PLANKS);
                        }
                        case 2 -> {
                            //stone
                            location.getBlock().setType(Material.STONE);
                        }
                        case 3 -> {
                            location.getBlock().setType(Material.IRON_BLOCK);
                        }
                        default -> {
                            location.getBlock().setType(Material.OBSIDIAN);
                        }
                    }
                }
                case Small -> {
                    if (baseBlock.toolValue() == 0) {
                        //oak trapdoor
                        location.getBlock().setType(Material.OAK_TRAPDOOR);
                    } else {
                        //iron trapdoor
                        location.getBlock().setType(Material.IRON_TRAPDOOR);
                    }
                }
                case Medium -> {
                    //Enchantment table
                    if (baseBlock.toolValue() == 0) {
                        location.getBlock().setType(Material.OAK_SLAB);
                    } else {
                        location.getBlock().setType(Material.STONE_SLAB);
                    }
                }
            }
            //then let's store it
            StoreBlock(baseBlock, location);
            //then let's spawn the item display
            ItemStack blockitem = baseBlock.block();
            ItemDisplay display = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
            display.teleport(location);
        }
    }
    public void StoreBlock(BaseBlock baseBlock, Location location) {
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
