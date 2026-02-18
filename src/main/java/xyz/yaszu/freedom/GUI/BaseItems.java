package xyz.yaszu.freedom.GUI;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.yaszu.freedom.Util.Util;

public class BaseItems extends Util {

    public static ItemStack emptyItem(ItemStack item) {
        ItemMeta workingMeta = item.getItemMeta();
        workingMeta.displayName(dess("\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33"));
        item.setItemMeta(workingMeta);
        return item;
    }

}
