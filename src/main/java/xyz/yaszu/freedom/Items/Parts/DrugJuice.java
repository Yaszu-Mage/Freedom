package xyz.yaszu.freedom.Items.Parts;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Items.Drinks.BaseDrink;
import xyz.yaszu.freedom.Subsystems.AlcoholManager;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

public class DrugJuice extends Util implements BaseItem, BaseDrink {
    @Override
    public ItemStack item() {
        ItemStack stack = constructColoredBottle(List.of(FreedomKeys.itemId()),List.of("drugjuice"), Color.WHITE);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"drugjuice");
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        AlcoholManager.addAlcohol(player,0,0,10);
        player.addPotionEffect(PotionEffectType.POISON.createEffect(100,8));
    }

    @Override
    public Recipe recipe() {

        return null;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.DRINK;
    }

    @Override
    public ItemStack result() {
        return item();
    }

    @Override
    public ItemStack ingredient() {
        return ItemStack.of(Material.GLASS_BOTTLE);
    }

    @Override
    public int inbetweenBrewTime() {
        return 30;
    }

    @Override
    public ItemStack stir() {
        return ItemStack.of(Material.FERN);
    }
}
