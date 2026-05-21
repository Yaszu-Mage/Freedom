package xyz.yaszu.freedom.Blocks;

import org.bukkit.inventory.ItemStack;

public interface BaseBlock {
    public ItemStack block();
    public CollisionSize collisionSize();
    public Behavior behavior();
    public int toolValue();
    public boolean waterNeeded();
    public enum Behavior {
        Farm,
        Building,
        Interface
    }
    public enum CollisionSize {
        Small,
        Medium,
        Large
    }

}
