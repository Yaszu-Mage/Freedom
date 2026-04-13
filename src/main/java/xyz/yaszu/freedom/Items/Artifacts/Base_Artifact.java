package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

public class Base_Artifact extends Util implements Listener, BaseItem {

    private final String id;
    private final Component name;
    private final Component description;
    private final Material material;
    private final NamespacedKey customModelData;
    private final List<PotionEffect> buffs;

    public Base_Artifact(String id, Component name, Component description, Material material, NamespacedKey customModelData, List<PotionEffect> buffs) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.material = material;
        this.customModelData = customModelData;
        this.buffs = buffs;
    }

    public Component Name() { return name; }
    public Component Description() { return description; }
    public List<PotionEffect> getBuffs() { return buffs; }
    public String getID() { return id; }
    public Material getMaterial() { return material; }
    public NamespacedKey getCustomModelData() { return customModelData; }

    @Override
    public ItemStack item() {
        ItemStack item = ItemStack.of(getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING, getID());
            meta.getPersistentDataContainer().set(keygen(getID()), PersistentDataType.BOOLEAN, true);
            meta.displayName(Name());
            List<Component> lore = new ArrayList<>();
            lore.add(Description());
            lore.add(dess("<gray>Sleep with this in your inventory to get a buff!</gray>"));
            meta.lore(lore);
            if (getCustomModelData() != null) {
                meta.setItemModel(customModelData);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        // Artifacts have passive effects when sleeping, no active effect
    }

    @Override
    public Recipe recipe() {
        return null;
    }

    public boolean hasArtifact(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;
            String id = item.getItemMeta().getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING);
            if (getID().equals(id)) return true;
        }
        return false;
    }
}
