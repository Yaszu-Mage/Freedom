package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Util.Util;

public class GamblingManager extends Util implements Listener {

    @EventHandler
    public void InventoryInteract(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof GamblingInventory gamblingInventory) {
            
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnInteract(PlayerInteractEntityEvent event) {
        // MAKE SURE TO CHECK ENTITY PERSISTENTDATA FOR IF ITS A GAMBLING TABLE
        GamblingInventory gamblingInventory = new GamblingInventory(null);
        gamblingInventory.setInventory();
        event.getPlayer().openInventory(gamblingInventory.getInventory());
    }
    public class GamblingInventory implements InventoryHolder {

        private final Inventory inventory;


        public void setInventory() {

            inventory.forEach(item -> item = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE));
            //set all inventory data
        }

        public GamblingInventory(Inventory inventory) {
            this.inventory = Bukkit.createInventory(this, 27, dess("Gambling Inventory"));
        }
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

}
