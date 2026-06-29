package xyz.yaszu.freedom.Items.Bakery;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import xyz.yaszu.freedom.Items.BaseFood;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;

public class Coffee implements BaseItem, BaseFood {
    @Override
    public ItemStack item() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING, "coffee");
        meta.setItemModel(NamespacedKey.minecraft("coffee"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        eat(player);
        player.addPotionEffect(PotionEffectType.SPEED.createEffect(100,8));
    }

    @Override
    public Recipe recipe() {
        return null;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.FOOD;
    }

    @Override
    public int hunger() {

        return 4;
    }
    @Override
    public float saturation() {
        return 6;
    }

    @Override
    public void effect(Player player) {

    }

    @Override
    public int bakeTime() {
        return 16000;
    }

    @Override
    public int avgCookTime() {
        return 200;
    }

    @Override
    public GameTypes gameType() {
        return GameTypes.QuickTime;
    }
}
