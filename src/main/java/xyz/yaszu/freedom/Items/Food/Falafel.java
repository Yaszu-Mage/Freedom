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

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

public class Falafel implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b> Falafel"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING, "falafel");
        meta.setItemModel(NamespacedKey.minecraft("falafel"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        player.setSaturation(player.getSaturation()+8f);
        player.setFoodLevel(player.getFoodLevel()+6);
        player.getWorld().spawnParticle(Particle.EGG_CRACK,player.getLocation(),10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP,10f,2f);
        item.subtract();
    }

    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(keygen("falafel"), item());
        recipe.addIngredient(ItemStack.of(Material.BROWN_MUSHROOM));
        recipe.addIngredient(ItemStack.of(Material.WHEAT));
        recipe.addIngredient(ItemStack.of(Material.SWEET_BERRIES));
        recipe.addIngredient(ItemStack.of(Material.BREAD));
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.FOOD;
    }
}
