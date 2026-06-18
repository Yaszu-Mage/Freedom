package xyz.yaszu.freedom.Items.Drinks;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface BaseDrink {
    ItemStack result();
    ItemStack ingredient();
    int inbetweenBrewTime();
    ItemStack stir();
}
