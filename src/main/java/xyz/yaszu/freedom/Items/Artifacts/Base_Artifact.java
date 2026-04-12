package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.List;

public interface Base_Artifact extends Listener, BaseItem {

    Util util = new Util();

    Component Name();
    Component Description();
    List<PotionEffect> getBuffs();
    String getID();
    Material getMaterial();

    @Override
    default ItemStack item() {
        ItemStack item = ItemStack.of(getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING, getID());
            meta.getPersistentDataContainer().set(util.keygen(getID()), PersistentDataType.BOOLEAN, true);
            meta.displayName(Name());
            List<Component> lore = new ArrayList<>();
            lore.add(Description());
            lore.add(util.dess("<gray>Sleep with this in your inventory to get a buff!</gray>"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    default void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        // Artifacts have passive effects when sleeping, no active effect
    }

    @Override
    default Recipe recipe() {
        return null;
    }

    default boolean hasArtifact(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;
            String id = item.getItemMeta().getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING);
            if (getID().equals(id)) return true;
        }
        return false;
    }
}
