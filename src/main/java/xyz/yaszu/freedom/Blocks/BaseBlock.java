package xyz.yaszu.freedom.Blocks;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface BaseBlock {
    public ItemStack block();
    public CollisionSize collisionSize();
    public Behavior behavior();
    public int toolValue();
    public boolean waterNeeded();
    public double scale();
    public Location mountLocation();
    public enum Behavior {
        Farm,
        Building,
        Interface,
        Interactable,
        Updatable
    }

    public enum CollisionSize {
        Small,
        Medium,
        Large,
        Teeny,
        Itsy
    }
    public Object placeSound();

    public InventoryHolder inventoryHolder();

}
