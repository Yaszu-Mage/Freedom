package xyz.yaszu.freedom.Items;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public interface BaseItem {
    public ItemStack item();
    // idk
    public void effect(Player player, PlayerInteractEvent event,ItemStack item);
    // event on right click
    public Recipe recipe();
    // recipe of item, can be null
    public CustomItemType getType();
    // type of item
}
