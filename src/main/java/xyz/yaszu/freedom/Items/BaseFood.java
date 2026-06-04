package xyz.yaszu.freedom.Items;

import org.bukkit.entity.Player;

public interface BaseFood extends BaseItem {
    public int hunger();
    public float saturation();
    public default void eat(Player player) {
        player.setFoodLevel(player.getFoodLevel() + hunger());
        player.setSaturation(player.getSaturation() + saturation());
        effect(player);
    };
    public void effect(Player player);
    public void bakeTime();
}
