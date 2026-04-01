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

import static xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi.open_UI;

public class Reset extends Util implements BaseItem {

    @Override
    public ItemStack item() {
        ItemStack evolve = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = evolve.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("evolve"));
        meta.displayName(dess("<shadow:#000000FF><b><rainbow>Memory Stone</rainbow>"));
        meta.getPersistentDataContainer().set(keygen("item_id"), PersistentDataType.STRING,"resetstone");
        meta.setRarity(ItemRarity.EPIC);
        evolve.setItemMeta(meta);
        return evolve;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        Base_Soul red = new Red();
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        open_UI(player,red);
        item.subtract();

    }

    @Override
    public Recipe recipe() {
        RecipeChoice template = new RecipeChoice.MaterialChoice(Material.TOTEM_OF_UNDYING);
        RecipeChoice base = new RecipeChoice.MaterialChoice(Material.GOLDEN_APPLE);
        RecipeChoice addition = new RecipeChoice.MaterialChoice(Material.DIAMOND);
        ItemStack result = item();

        return new SmithingTransformRecipe(keygen("reset"), result, template, base, addition);
    }


}
