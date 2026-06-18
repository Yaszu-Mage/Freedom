package xyz.yaszu.freedom.Items.Drinks;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

public class BajaBlast extends Util implements BaseItem {

    @Override
    public ItemStack item() {
        ItemStack stack = constructColoredBottle(List.of(FreedomKeys.itemId()),List.of("bajablast"), Color.GREEN);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING, "bajablast");
        meta.displayName(dess("<shadow:#000000FF><b><green>Baja Blast</green></b>"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        player.setSaturation(player.getSaturation()+0);
        player.setFoodLevel(player.getFoodLevel());
        player.getWorld().spawnParticle(Particle.EGG_CRACK,player.getLocation(),10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP,10f,2f);
        player.addPotionEffect(PotionEffectType.NAUSEA.createEffect(100,5));
        player.addPotionEffect(PotionEffectType.SPEED.createEffect(20,3));
        item.subtract();
    }

    @Override
    public Recipe recipe() {
        ShapedRecipe recipe = new ShapedRecipe(keygen("bajablast"), item());
        recipe.shape(
                "SIS",
                "IGI",
                "SIS"
        );
        recipe.setIngredient('I', Material.ICE);
        recipe.setIngredient('S', Material.SUGAR);
        recipe.setIngredient('G', Material.GREEN_DYE);
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.DRINK;
    }
}
