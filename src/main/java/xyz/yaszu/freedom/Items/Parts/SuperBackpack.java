package xyz.yaszu.freedom.Items.Parts;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Subsystems.BackpackManager;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.InventoryPersistentDataType;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

public class SuperBackpack implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"superbackpack");
        meta.displayName(dess("<shadow:#000000FF><b><yellow>Super Backpack</yellow></b>"));
        meta.setItemModel(NamespacedKey.minecraft("superbackpack"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        // Only open inventory on right-click actions, not on drops or other interactions
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!item.getItemMeta().getPersistentDataContainer().has(keygen("backpack"))) {
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(keygen("backpack"), InventoryPersistentDataType.get(), BackpackManager.BackpackGui.create(54));
            item.setItemMeta(meta);

        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                player.openInventory(BackpackManager.BackpackGui.open(item));
            }
        }.runTaskLater(Freedom.get_plugin(),10);

    }

    public static DoubleBackpack doubleBackpack = new DoubleBackpack();
    @Override
    public Recipe recipe() {
        ShapedRecipe recipe = new ShapedRecipe(keygen("backpack"), item());
        recipe.shape(
                "SSS",
                "SCS",
                "SSS"
        );
        recipe.setIngredient('S', Material.NETHERITE_INGOT);
        recipe.setIngredient('C', doubleBackpack.item());
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }
}
