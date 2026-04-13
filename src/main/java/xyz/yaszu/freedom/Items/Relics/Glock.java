package xyz.yaszu.freedom.Items.Relics;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

public class Glock extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack hoe = ItemStack.of(Material.STONE_HOE);
        ItemMeta meta = hoe.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("glock"));
        meta.displayName(dess("<shadow:#000000FF><b><yellow>Glock</yellow></b>"));
        meta.lore(List.of(dess("<color:#F9EBDE>A relic of the ancient gods"),dess("<color:#F9EBDE>It is said to be the most powerful"),dess("Glock")));
        ((Damageable) meta).setDamage(99);
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"glock");
        hoe.setItemMeta(meta);
        return hoe;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        Damageable damage = (Damageable) item.getItemMeta();
        damage.setDamage(damage.getDamage()+1);
        item.subtract();
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK,1,1);
        player.playSound(player.getLocation(),"custom.chicken",1,1);
    }

    @Override
    public Recipe recipe() {
        RecipeChoice template = new RecipeChoice.MaterialChoice(Material.GUNPOWDER);
        RecipeChoice base = new RecipeChoice.MaterialChoice(Material.DRAGON_BREATH);
        RecipeChoice addition = new RecipeChoice.MaterialChoice(Material.IRON_INGOT);
        SmithingTransformRecipe recipe = new SmithingTransformRecipe(keygen("glock"),item(),template,base,addition);
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.RELIC;
    }
}
