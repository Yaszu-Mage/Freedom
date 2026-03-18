package xyz.yaszu.freedom.GUI.SelectionGUI;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.GUI.BaseItems;
import xyz.yaszu.freedom.Soul.Ultra.Red;
import xyz.yaszu.freedom.Soul.SoulTypes;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class selectionGui implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        // Check if the holder is our MyInventory,
        // if yes, use instanceof pattern matching to store it in a variable immediately.
        if (!(inventory.getHolder(false) instanceof selectionGui_Inventory selectionGuiInventory)) {
            // It's not our inventory, ignore it.
            return;
        }
        // Do what we need in the event.
        if (event.getCurrentItem() != null) {
            //Do what you need to do for selection
            event.setCancelled(true);
            return;
        }
    }
    public static void openGUI(Player player) {
        selectionGui_Inventory selectionGuiInventory = new selectionGui_Inventory();
        player.openInventory(selectionGuiInventory.getInventoryforPlayer(player));

    }

    public static class selectionGui_Inventory extends BaseItems implements InventoryHolder {
        public static Inventory big_inventory;
        public static HashMap<UUID,Inventory> playerToInventory = new HashMap<UUID, Inventory>();
        public Inventory getInventoryforPlayer(Player player) {
            Inventory inventory = playerToInventory.getOrDefault(player.getUniqueId(), Bukkit.createInventory(this,9));
            big_inventory = inventory;
            if (inventory.getItem(4) == null) {
                setInventory(SoulTypes.Red);
            }
            return inventory;
        }

        public static void setInventory(SoulTypes soulType) {
            switch (soulType) {
                case Red:
                    //Fill out inventory for Red SoulType
                    // Make every item a blank item
                    for (int iteration = 0; big_inventory.getSize() > iteration; iteration = iteration + 1) {
                        big_inventory.setItem(iteration,emptyItem(ItemStack.of(Material.GRAY_STAINED_GLASS_PANE)));
                    }
                    //Make a new iteration of the soul type
                    Red red = new Red();
                    //Set the item in the center to the icon
                    big_inventory.setItem(4,red.Icon());
                    //Star item?
                    // on slots 2 and 6
                    big_inventory.setItem(2,description_to_Item(true,red.AbilityOneDescription()));
                    big_inventory.setItem(6,description_to_Item(false,red.AbilityTwoDescription()));
                    break;
            }
        }

        public static ItemStack description_to_Item(Boolean isAbilityOne, Component description) {
            ItemStack workingItem = ItemStack.of(Material.REDSTONE);
            ItemMeta workingMeta = workingItem.getItemMeta();
            if (isAbilityOne) {
                workingMeta.displayName(dess("<red>Ability One</red>"));
            } else {
                workingMeta.displayName(dess("<red>Ability Two</red>"));
            }
            workingMeta.lore(List.of(description));
            workingItem.setItemMeta(workingMeta);
            return workingItem;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return big_inventory;
        }
    }
}
