package xyz.yaszu.freedom.Items.Tools;

import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

public class Hammer implements BaseItem, Listener {
    @Override
    public ItemStack item() {
        ItemStack item = ItemStack.of(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b><i><gradient:#1956C0:#65caf6>Hammer</gradient></i></b>"));
        meta.setItemModel(org.bukkit.NamespacedKey.minecraft("hammer"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), org.bukkit.persistence.PersistentDataType.STRING,"hammer");
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }


    @EventHandler
    public void BreakBlockEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.getPersistentDataContainer().has(FreedomKeys.itemId())) {
                if (mainHand.getPersistentDataContainer().get(FreedomKeys.itemId(), org.bukkit.persistence.PersistentDataType.STRING).equals("hammer")) {
                    //break surrounding
                    //center 2 blocks first
                    Block centerBlock = event.getBlock();
                    Vector dir = player.getLocation().getDirection().setY(0).normalize();
                    Vector leftDir = new Vector(-dir.getZ(),0,dir.getX());
                    Vector rightDir = new Vector(dir.getZ(),0,dir.getX());
                    Location leftLoc = centerBlock.getLocation().add(leftDir);
                    Location rightLoc = centerBlock.getLocation().add(rightDir);
                    Location centerLoc = centerBlock.getLocation();
                    //now get below+above blocks
                    leftLoc.add(0,1,0);
                    rightLoc.add(0,1,0);
                    centerLoc.add(0,1,0);
                    for (int iteration = 0; iteration < 2; iteration++) {
                        Block leftBlock = leftLoc.getBlock();
                        Block rightBlock = rightLoc.getBlock();
                        centerBlock = centerLoc.getBlock();
                        if (leftBlock.getType() != Material.AIR) {
                            leftBlock.breakNaturally(mainHand);
                        }
                        if (rightBlock.getType() != Material.AIR) {
                            rightBlock.breakNaturally(mainHand);
                        }
                        if (centerBlock.getType() != Material.AIR) {
                            centerBlock.breakNaturally(mainHand);
                        }
                        centerLoc.add(0,-1,0);
                        leftLoc.add(0,-1,0);
                        rightLoc.add(0,-1,0);
                    }
                }
            }
    }

    @Override
    public Recipe recipe() {
        ShapedRecipe recipe = new ShapedRecipe(keygen("hammer"), item());
        recipe.shape(
                "DDD",
                "DSD",
                " S "
        );
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('S', Material.STICK);
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.TOOL;
    }
}
