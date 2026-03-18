package xyz.yaszu.freedom.Items.Upgrades;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.GUI.SelectionGUI.UltraselectionUi;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.Ultra.*;
import xyz.yaszu.freedom.Util.Util;

public class Evolve extends Util implements BaseItem {

    @Override
    public ItemStack item() {
        ItemStack evolve = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = evolve.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("evolve"));
        meta.displayName(dess("<shadow:#000000FF><b><rainbow>Evolution Stone</rainbow>"));
        meta.getPersistentDataContainer().set(keygen("item_id"), PersistentDataType.STRING,"evolutionstone");
        meta.setRarity(ItemRarity.EPIC);
        evolve.setItemMeta(meta);
        return evolve;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event) {
        Base_Soul red = new Red();
        boolean cancel = false;
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        switch (soulType) {
            case BaseRed -> red = new Red();
            case BaseBlack -> red = new Black();
            case BaseBlue -> red = new Blue();
            case BaseGreen -> red = new Green();
            case BaseNone -> red = new None();
            case BaseOrange -> red = new Orange();
            case BasePurple -> red = new Purple();
            case BaseYellow -> red = new Yellow();
            default -> cancel = true;
        }
        if (cancel) {
            player.sendMessage(dess("You cannot evolve twice!"));
        } else {
            UltraselectionUi.open_UI(player,red);
        }

    }

    @Override
    public Recipe recipe() {
        RecipeChoice template = new RecipeChoice.MaterialChoice(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        RecipeChoice base = new RecipeChoice.MaterialChoice(Material.GOLDEN_APPLE);
        RecipeChoice addition = new RecipeChoice.MaterialChoice(Material.NETHERITE_INGOT);
        ItemStack result = item();

        return new SmithingTransformRecipe(keygen("evolve"), result, template, base, addition);
    }


}
