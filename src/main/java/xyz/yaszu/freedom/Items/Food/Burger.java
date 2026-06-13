package xyz.yaszu.freedom.Items.Food;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Burger extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack item  = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b><yellow>Biggie</yellow></b> Burger"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"burger");
        meta.setItemModel(NamespacedKey.minecraft("burger"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        if (player.getFoodLevel() >= 20) {
            player.sendMessage(dess("You are already full!"));
            return;
        }
        player.setSaturation(player.getSaturation()+5);
        player.setFoodLevel(player.getFoodLevel()+5);
        player.getWorld().spawnParticle(Particle.EGG_CRACK,player.getLocation(),10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP,10f,2f);
        item.subtract();
    }

    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(FreedomKeys.itemId(),item());
        recipe.addIngredient(ItemStack.of(Material.COOKED_BEEF));
        recipe.addIngredient(ItemStack.of(Material.BREAD));
        recipe.addIngredient(ItemStack.of(Material.GOLD_INGOT));
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.FOOD;
    }
}
