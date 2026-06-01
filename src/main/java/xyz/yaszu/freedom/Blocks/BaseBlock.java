package xyz.yaszu.freedom.Blocks;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface BaseBlock {
    public ItemStack block();
    // IDK //todo
    public CollisionSize collisionSize();
    // size of collision, small, medium, large, teeny, itsy
    public Behavior behavior();
    // behavior of block, farm, building, interface, interactable, updatable
    public int toolValue();
    // dificulty to break block
    public boolean waterNeeded();
    // if water is needed
    public double scale();
    // scale of block
    public Location mountLocation();
    // place to sit on chairs
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
