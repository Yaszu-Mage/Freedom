package xyz.yaszu.freedom.Items.Drinks;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Subsystems.AlcoholManager;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

public class Wine extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack itemStack = constructColoredBottle(List.of(FreedomKeys.itemId()),List.of("ale"), Color.fromRGB(150,75,0));
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Util.dess("<shadow:#000000FF><b><Red>Wine</Red></b>"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"wine");
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        AlcoholManager.addAlcohol(player,1,1,1);
        player.getWorld().spawnParticle(Particle.EGG_CRACK,player.getLocation(),10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK,10f,1f);
        item.subtract();
    }

    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(keygen("wine"),item());
        recipe.addIngredient(ItemStack.of(Material.WATER_BUCKET));
        recipe.addIngredient(ItemStack.of(Material.SWEET_BERRIES));
        recipe.addIngredient(ItemStack.of(Material.HONEY_BOTTLE));
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.FOOD;
    }
}
