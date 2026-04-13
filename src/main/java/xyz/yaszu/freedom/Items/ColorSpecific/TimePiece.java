package xyz.yaszu.freedom.Items.ColorSpecific;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.Ultra.Red;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class TimePiece implements BaseItem {
    private final Red red = new Red();

    @Override
    public ItemStack item() {
        return red.Related_Item();
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        SoulTypes soul = Util.getSoulType(player);
        if (soul != SoulTypes.Red && soul != SoulTypes.BaseRed) {
            //do stuff
            event.setCancelled(true);
        }
    }

    @Override
    public Recipe recipe() {
        RecipeChoice template = new RecipeChoice.MaterialChoice(Material.IRON_INGOT);
        RecipeChoice base = new RecipeChoice.MaterialChoice(Material.CLOCK);
        RecipeChoice addition = new RecipeChoice.MaterialChoice(Material.GUNPOWDER);
        ItemStack result = item();
        return new SmithingTransformRecipe(FreedomKeys.key("timepiece"), result, template, base, addition);
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.COLOR_SPECIFIC;
    }
}
