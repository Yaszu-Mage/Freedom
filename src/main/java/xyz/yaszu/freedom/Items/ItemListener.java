package xyz.yaszu.freedom.Items;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Util.Util;

public class ItemListener extends Util implements Listener {
    static Evolve evolve = new Evolve();
    @EventHandler
    public void playerinteractevent(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null) {
            if (item.getPersistentDataContainer().has(keygen("item_id"))) {
                String itemid = item.getPersistentDataContainer().get(keygen("item_id"), PersistentDataType.STRING);
                switch (itemid) {
                    case "evolutionstone":
                        evolve.effect(event.getPlayer());
                        break;


                }
            }
        }
    }


    public static void registeritems(){
        Bukkit.addRecipe(evolve.recipe());
    }

}
