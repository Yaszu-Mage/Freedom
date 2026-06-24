package xyz.yaszu.freedom.Enchantments;

import com.arakelian.core.feature.Nullable;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.HashMap;
import java.util.Map;

import static xyz.yaszu.freedom.Util.Util.getEnchantment;

public interface BaseEnchant {
    public Class<? extends Event> eventType();
    public String id();
    public String name();
    public String description();
    public int weight();
    public int anvilCost();
    public int maxLevel();
    public int minEnchantCost();
    public int maxEnchantCost();
    public TagKey<ItemType> supportedItems();
    public EquipmentSlotGroup activeSlots();
    public void effect(@Nullable ItemStack itemStack, @Nullable Entity entity, int level);
}
