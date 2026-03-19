package xyz.yaszu.freedom.Items.Upgrades;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.Util;

public class Revival extends Util implements BaseItem {

    @Override
    public ItemStack item() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(keygen("item_id"), PersistentDataType.STRING,"revival");
        meta.setItemModel(NamespacedKey.minecraft("revival"));
        meta.displayName(dess("<shadow:#000000FF><b><Blue>Revival Stone</Blue>"));
        meta.setRarity(ItemRarity.EPIC);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event) {
        for (Entity entity : player.getNearbyEntities(2,2,2)) {
            if (entity instanceof Player player_nearby) {
                if (player_nearby.getPersistentDataContainer().has(keygen("trustedby"))) {
                    String trustedby = player_nearby.getPersistentDataContainer().get(keygen("trustedby"),PersistentDataType.STRING);
                    if (trustedby.contains(player.getName()) && !Life_and_Death.is_alive(player)) {
                        Life_and_Death.revive_player(player_nearby,player.getLocation());
                        return;
                    }
                }
            }
        }
    }



    @Override
    public Recipe recipe() {
        ShapedRecipe rec = new ShapedRecipe(keygen("revival"), item());
        rec.shape(
                "NGN",
                "TST",
                "NGN"
        );
        rec.setIngredient('N', Material.NETHERITE_INGOT);
        rec.setIngredient('G', Material.GOLDEN_APPLE);
        rec.setIngredient('T', Material.TOTEM_OF_UNDYING);
        rec.setIngredient('S', Material.NETHER_STAR);
        return rec;
    }
}
