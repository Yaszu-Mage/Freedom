package xyz.yaszu.freedom.Items;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public interface BaseItem {
    public ItemStack item();
    public void effect(Player player, PlayerInteractEvent event);
    public Recipe recipe();
}
