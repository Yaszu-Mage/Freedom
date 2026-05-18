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
import xyz.yaszu.freedom.Soul.Alchemy.Astral;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.Ultra.*;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class AstralItem implements BaseItem {

    @Override
    public ItemStack item() {
        ItemStack evolve = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = evolve.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("astral"));
        meta.displayName(Util.dess("<shadow:#000000FF><b><rainbow>Astral Color</rainbow>"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"astral");
        meta.setRarity(ItemRarity.EPIC);
        evolve.setItemMeta(meta);
        return evolve;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        Base_Soul soul = new Red();
        boolean cancel = false;
        SoulTypes soulType = Util.getSoulType(player);
        if (soulType != SoulTypes.Astral) {
            soul = new Astral();
            cancel = true;
        }
        if (cancel) {
            player.sendMessage(Util.dess("You are already an Astral!"));
        } else {
            item.subtract();
            UltraselectionUi.open_UI(player,soul);
        }

    }

    @Override
    public Recipe recipe() {
        RecipeChoice template = new RecipeChoice.MaterialChoice(Material.DRAGON_EGG);
        RecipeChoice base = new RecipeChoice.MaterialChoice(Material.DRAGON_EGG);
        RecipeChoice addition = new RecipeChoice.MaterialChoice(Material.DRAGON_EGG);
        ItemStack result = item();
        return new SmithingTransformRecipe(FreedomKeys.key("astral"), result, template, base, addition);
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.UPGRADE;
    }
}
