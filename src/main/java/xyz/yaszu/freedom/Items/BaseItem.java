package xyz.yaszu.freedom.Items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public interface BaseItem {
    public ItemStack item();
    public void effect(Player player);
    public Recipe recipe();
}
