package xyz.yaszu.freedom.Enchantments.Enchant;

import com.arakelian.core.feature.Nullable;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import xyz.yaszu.freedom.Enchantments.BaseEnchant;

import static xyz.yaszu.freedom.Util.Util.getSmeltingResult;
import static xyz.yaszu.freedom.Util.Util.isSmeltable;

public class SmeltingEnchant implements BaseEnchant {
    @Override
    public Class<? extends Event> eventType() {
        return BlockBreakEvent.class;
    }
    @Override
    public String id() {
        return "smelter";
    }

    @Override
    public String name() {
        return "smelter";
    }

    @Override
    public String description() {
        return "smelter";
    }

    @Override
    public int weight() {
        return 3;
    }

    @Override
    public int anvilCost() {
        return 1;
    }

    @Override
    public int maxLevel() {
        return 1;
    }

    @Override
    public int minEnchantCost() {
        return 1;
    }

    @Override
    public int maxEnchantCost() {
        return 3;
    }

    @Override
    public TagKey<ItemType> supportedItems() {
        return ItemTypeTagKeys.PICKAXES;
    }

    @Override
    public EquipmentSlotGroup activeSlots() {
        return null;
    }

    @Override
    public void effect(@Nullable ItemStack itemStack, @Nullable Entity entity,int level) {
        if (isSmeltable(itemStack.getType())) {
            if (entity instanceof Player player) {
                player.give(getSmeltingResult(itemStack));
            }
            itemStack.subtract();
        }
    }
}
