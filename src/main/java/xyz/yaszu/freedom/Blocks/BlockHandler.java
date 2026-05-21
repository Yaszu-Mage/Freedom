package xyz.yaszu.freedom.Blocks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import xyz.yaszu.freedom.Util.Util;

public class BlockHandler extends Util implements Listener {



    public BlockHandler() {

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
                }
                case Small -> {
                    if (baseBlock.toolValue() == 0) {
                        //oak trapdoor
                    } else {
                        //iron trapdoor
                    }
                }
                case Medium -> {
                    //Enchantment table
                    location.getBlock().setType(Material.ENCHANTING_TABLE);
                    //then let's store it

                }
            }
        }
    }


}
