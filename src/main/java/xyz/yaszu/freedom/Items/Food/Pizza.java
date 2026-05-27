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

public class Pizza extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack item  = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b> Pizza"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"pizza");
        meta.setItemModel(NamespacedKey.minecraft("pizza"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        if (player.getFoodLevel() >= 20) {
            player.sendMessage(dess("You are already full!"));
            return;
        }
        player.setSaturation(player.getSaturation()+6);
        player.setFoodLevel(player.getFoodLevel()+4);
        player.getWorld().spawnParticle(Particle.EGG_CRACK,player.getLocation(),10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP,10f,2f);
        item.subtract();
    }

    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(keygen("pizza"),item());
        recipe.addIngredient(ItemStack.of(Material.MILK_BUCKET));
        recipe.addIngredient(ItemStack.of(Material.BREAD));
        recipe.addIngredient(ItemStack.of(Material.PORKCHOP));
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.FOOD;
    }
}
