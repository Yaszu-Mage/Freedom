package xyz.yaszu.freedom.Items;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.ColorSpecific.Rifle;
import xyz.yaszu.freedom.Items.ColorSpecific.TimePiece;
import xyz.yaszu.freedom.Items.Parts.Burger;
import xyz.yaszu.freedom.Items.Relics.Glock;
import xyz.yaszu.freedom.Items.Relics.PainScythe;
import xyz.yaszu.freedom.Items.Upgrades.Evolve;
import xyz.yaszu.freedom.Items.Upgrades.Reset;
import xyz.yaszu.freedom.Items.Upgrades.Revival;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ItemListener implements Listener {
    private static final Map<String, BaseItem> ITEMS = new HashMap<>();

    public static void registerItems() {
        register(new Evolve(), "evolutionstone");
        register(new Revival(), "revival");
        register(new Rifle(), "rifle");
        register(new TimePiece(), "timepiece");
        register(new Reset(), "resetstone");
        register(new Burger(),"burger");
        register(new PainScythe(),"painscythe");
        register(new Glock(), "glock");
    }

    private static void register(BaseItem item, String id) {
        ITEMS.put(id, item);
        Bukkit.addRecipe(item.recipe());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta()) {
            String itemId = item.getItemMeta().getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING);
            if (itemId != null) {
                BaseItem baseItem = ITEMS.get(itemId);
                if (baseItem != null) {
                    baseItem.effect(event.getPlayer(), event, item);
                }
            }
        }
    }

    private final Random random = new Random();
    private static final int SUS_AMOUNT = 2;

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (event.getBlock().getType().equals(BlockType.SUSPICIOUS_GRAVEL) || event.getBlock().getType().equals(BlockType.SUSPICIOUS_SAND)) {
            if (random.nextInt(101) > 80) {
                int chancer = random.nextInt(SUS_AMOUNT + 1);
                switch (chancer) {
                    case 1 -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), ITEMS.get("revival").item());
                    case 2 -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), ITEMS.get("resetstone").item());
                }
            }
        }
    }
}
