package xyz.yaszu.freedom.Enchantments;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;

import static xyz.yaszu.freedom.Util.Util.getEnchantment;
import static xyz.yaszu.freedom.Util.Util.isItemNull;

public class EnchantmentListener implements Listener {
    public static HashMap<String, BaseEnchant> ENCHANTS = Bootstrap.ENCHANTS;

    public static HashMap<Class<? extends Event>,ArrayList<BaseEnchant>> EVENTS = new HashMap<>();

    //
    public static void update() {
        ENCHANTS = Bootstrap.ENCHANTS;
        EVENTS.clear();
        ENCHANTS.values().forEach(b -> {
            ArrayList<BaseEnchant> enchants = EVENTS.getOrDefault(b.eventType(),new ArrayList<>());
            enchants.add(b);
            EVENTS.put(b.eventType(),enchants);
        });
    }
    //SUPPORTED EVENTS
    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event) {
        ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
        if (isItemNull(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        ArrayList<BaseEnchant> enchants = EVENTS.get(BlockBreakEvent.class);
        if (enchants == null) return;
        for (BaseEnchant enchant : enchants) {
            if (stack.getEnchantmentLevel(getEnchantment(enchant.id())) >= 1) {
                //yes yes?
                enchant.effect(stack,event.getPlayer(),stack.getEnchantmentLevel(getEnchantment(enchant.id())));
            }
        }
    }





}
