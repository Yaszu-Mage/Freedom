package xyz.yaszu.freedom.Items.Food;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

public class Shawarma implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack item  = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b> Shawarma"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"shawarma");
        meta.setItemModel(NamespacedKey.minecraft("shawarma"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        if (player.getFoodLevel() >= 20) {
            player.sendMessage(dess("You are already full!"));
            return;
        }
        player.setSaturation(player.getSaturation()+9);
        player.setFoodLevel(player.getFoodLevel()+6);
        player.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(1800,2));
        player.getWorld().spawnParticle(Particle.EGG_CRACK,player.getLocation(),10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP,10f,2f);
        item.subtract();
    }

    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(keygen("shawarma"),item());
        recipe.addIngredient(ItemStack.of(Material.BREAD));
        recipe.addIngredient(RecipeChoice.itemType(ItemType.BEEF,ItemType.COOKED_BEEF));
        recipe.addIngredient(ItemStack.of(Material.CARROT));
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.FOOD;
    }
}
