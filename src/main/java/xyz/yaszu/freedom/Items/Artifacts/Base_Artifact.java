package xyz.yaszu.freedom.Items.Artifacts;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Util.Util;

public interface Base_Artifact extends Listener {


    //Util Class
    public Util util = new Util();

    //Name of the Artifact
    public Component Name();
    //Description
    public Component Description();
    //Effect
    public void effect();
    //ID
    public String ID = "Base_Artifact";
    //Item
    public default ItemStack Item() {
        ItemStack portaitem = ItemStack.of(Material.WOODEN_SHOVEL);
        ItemMeta meta = portaitem.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(util.keygen(ID), PersistentDataType.BOOLEAN, true);
        meta.displayName(util.dess(ID));
        portaitem.setItemMeta(meta);
        return portaitem;
    }



    @EventHandler
    public default void WhenHeld(PlayerSetSpawnEvent event) {
        Player player = event.getPlayer();
        NamespacedKey Key = util.keygen(ID);
        if (InventoryCheck(player,Key,PersistentDataType.BOOLEAN)) {

        }

    }

    public default boolean InventoryCheck(Player player, NamespacedKey key,PersistentDataType dataType) {
        Inventory inventory = player.getInventory();
        boolean has_item = false;
        for (ItemStack iterated_item : inventory.getContents()) {
            if (iterated_item.getPersistentDataContainer().get(key,dataType) != null) {
                   has_item = true;
            }
        }
        return has_item;
    }

}
