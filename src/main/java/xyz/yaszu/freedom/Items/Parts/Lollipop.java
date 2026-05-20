package xyz.yaszu.freedom.Items.Parts;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Lollipop extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack item  = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b> Lollipop"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"lollipop");
        meta.setItemModel(NamespacedKey.minecraft("lollipop"));
        meta.addAttributeModifier(Attribute.ATTACK_KNOCKBACK,new AttributeModifier(keygen("lollipop"),8, AttributeModifier.Operation.ADD_NUMBER));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        if (player.getFoodLevel() >= 20) {
            player.sendMessage(dess("You are already full!"));
            return;
        }
        player.setSaturation(player.getSaturation()+2);
        player.setFoodLevel(player.getFoodLevel()+2);
        player.addPotionEffect(PotionEffectType.SPEED.createEffect(50,8));
        player.getWorld().spawnParticle(Particle.EGG_CRACK,player.getLocation(),10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP,10f,2f);
        item.subtract();
    }

    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(keygen("lollipop"),item());
        recipe.addIngredient(ItemStack.of(Material.SUGAR));
        recipe.addIngredient(ItemStack.of(Material.GUNPOWDER));
        recipe.addIngredient(ItemStack.of(Material.RED_DYE));
        recipe.addIngredient(ItemStack.of(Material.STICK));
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }
}
