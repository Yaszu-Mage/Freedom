package xyz.yaszu.freedom.Items;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.ColorSpecific.Rifle;
import xyz.yaszu.freedom.Items.Upgrades.Evolve;
import xyz.yaszu.freedom.Items.Upgrades.Revival;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;
import java.util.Random;

public class ItemListener extends Util implements Listener {
    static Evolve evolve = new Evolve();
    static Revival revive = new Revival();
    static Rifle rifle = new Rifle();
    @EventHandler
    public void playerinteractevent(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null) {
            if (item.getPersistentDataContainer().has(keygen("item_id"))) {
                String itemid = item.getPersistentDataContainer().get(keygen("item_id"), PersistentDataType.STRING);
                Freedom.get_plugin().getLogger().info(itemid);
                switch (itemid) {
                    case "evolutionstone":
                        evolve.effect(event.getPlayer(),event);
                        break;
                    case "rifle":
                        rifle.effect(event.getPlayer(),event);
                        break;
                    case "revival":
                        revive.effect(event.getPlayer(), event);
                        break;


                }
            }
        }
    }

    public int sus_amount = 1;
    @EventHandler
    public void BlockBreakEvent(BlockDropItemEvent event) {
        if (event.getBlock().getType().equals(BlockType.SUSPICIOUS_GRAVEL) || event.getBlock().getType().equals(BlockType.SUSPICIOUS_SAND)) {
            Random random = new Random();
            int chance = random.nextInt(101);
            if (chance > 80) {
                List<Item>  stack = event.getItems();
                int chancer = random.nextInt(sus_amount+1);
                switch (chancer) {
                    case 1:
                        //drop revive item
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), revive.item());
                }
            }
        }
    }

    public static void registeritems(){
        Bukkit.addRecipe(evolve.recipe());
        Bukkit.addRecipe(rifle.recipe());
        Bukkit.addRecipe(revive.recipe());
    }

}
