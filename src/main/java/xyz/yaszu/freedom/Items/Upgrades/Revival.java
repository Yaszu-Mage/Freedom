package xyz.yaszu.freedom.Items.Upgrades;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class Revival implements BaseItem {

    @Override
    public ItemStack item() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"revival");
        meta.setItemModel(NamespacedKey.minecraft("revival"));
        meta.displayName(Util.dess("<shadow:#000000FF><b><Blue>Revival Stone</Blue>"));
        meta.setRarity(ItemRarity.EPIC);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        for (Entity entity : player.getNearbyEntities(2,2,2)) {
            if (entity instanceof Player player_nearby) {
                if (player_nearby.getPersistentDataContainer().has(FreedomKeys.trustedBy())) {
                    String trustedby = player_nearby.getPersistentDataContainer().get(FreedomKeys.trustedBy(),PersistentDataType.STRING);
                    if (trustedby != null && trustedby.contains(player.getName()) && !Life_and_Death.is_alive(player)) {
                        Life_and_Death.revive_player(player_nearby,player.getLocation());
                        item.subtract();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public Recipe recipe() {
        ShapedRecipe rec = new ShapedRecipe(FreedomKeys.key("revival"), item());
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
