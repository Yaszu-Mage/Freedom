package xyz.yaszu.freedom.Items.Upgrades;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.GUI.SelectionGUI.UltraselectionUi;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.Ultra.*;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Evolve implements BaseItem {

    @Override
    public ItemStack item() {
        ItemStack evolve = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = evolve.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("evolve"));
        meta.displayName(Util.dess("<shadow:#000000FF><b><rainbow>Evolution Stone</rainbow>"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"evolutionstone");
        meta.setRarity(ItemRarity.EPIC);
        evolve.setItemMeta(meta);
        return evolve;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        Base_Soul soul = new Red();
        boolean cancel = false;
        SoulTypes soulType = Util.getSoulType(player);
        switch (soulType) {
            case BaseRed -> soul = new Red();
            case BaseBlack -> soul = new Black();
            case BaseMocha -> soul = new Mocha();
            case BaseGreen -> soul = new Green();
            case BaseNone -> soul = new None();
            case BaseOrange -> soul = new Orange();
            case BasePurple -> soul = new Purple();
            case BaseCafe -> soul = new Cafe();
            default -> cancel = true;
        }
        if (cancel) {
            player.sendMessage(Util.dess("You cannot evolve twice!"));
        } else {
            item.subtract();
            UltraselectionUi.open_UI(player,soul);
        }

    }

    @Override
    public Recipe recipe() {
        RecipeChoice template = new RecipeChoice.MaterialChoice(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        RecipeChoice base = new RecipeChoice.MaterialChoice(Material.GOLDEN_APPLE);
        RecipeChoice addition = new RecipeChoice.MaterialChoice(Material.NETHERITE_INGOT);
        ItemStack result = item();

        return new SmithingTransformRecipe(FreedomKeys.key("evolve"), result, template, base, addition);
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.UPGRADE;
    }
}
