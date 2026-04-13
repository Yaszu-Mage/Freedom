package xyz.yaszu.freedom.Information;

import net.kyori.adventure.inventory.Book;
import xyz.yaszu.freedom.Items.CustomItemType;
import org.bukkit.inventory.ItemStack;

public interface BaseInformation {
    public ItemStack information();
    public CustomItemType getType();
}
