package xyz.yaszu.freedom.Items.Parts;

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
import xyz.yaszu.freedom.Subsystems.AlcoholManager;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Beer extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack itemStack = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("beer"));
        meta.displayName(Util.dess("<shadow:#000000FF><b><yellow>Beer</yellow></b>"));
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"beer");
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        AlcoholManager.addAlcohol(player,1,1,2);
        player.getWorld().spawnParticle(Particle.EGG_CRACK,player.getLocation(),10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK,10f,2f);
        item.subtract();
    }

    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(keygen("beer"),item());
        recipe.addIngredient(ItemStack.of(Material.WATER_BUCKET));
        recipe.addIngredient(ItemStack.of(Material.WHEAT));
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }
}
