package xyz.yaszu.freedom.Items.Parts;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Soul.Alchemy.Astral;
import xyz.yaszu.freedom.Util.FreedomKeys;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Util.Util;

public class Grapple_Hook extends Util implements Listener, BaseItem {

    @Override
    public ItemStack item() {
        ItemStack item = ItemStack.of(Material.FISHING_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"grapplehook");
        meta.displayName(Util.dess("<shadow:#000000FF><b><yellow>Grapple Hook</yellow></b>"));
        meta.setItemModel(NamespacedKey.minecraft("grapple_hook"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    @Override
    public Recipe recipe() {
        ShapedRecipe recipe = new ShapedRecipe(keygen("grapple_hook"), item());
        recipe.shape(
                " IS",
                " TS",
                "TG "
        );
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('T', Material.STICK);
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }


    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        FishHook hook = event.getHook();
        // Check if the bobber has successfully latched onto a block or landed
        if (
                event.getPlayer().getInventory().getItemInMainHand().isSimilar(item()) ||
                event.getPlayer().getInventory().getItemInOffHand().isSimilar(item()) ||
                event.getPlayer().getInventory().getItemInMainHand().isSimilar(Astral.GrappleItem())
        ){


        if (event.getState() == PlayerFishEvent.State.IN_GROUND ||
                event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {

            // Start the physics pulling task
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || !hook.isValid() || hook.isOnGround()) {
                        cancel();
                        return;
                    }

                    // Physics: Calculate pull vector
                    Location playerLoc = player.getLocation();
                    Location hookLoc = hook.getLocation();

                    Vector direction = hookLoc.toVector().subtract(playerLoc.toVector());
                    double distance = direction.length();

                    if (distance > 0.5) {
                        // Normalize the vector to get the direction and multiply by pull speed (e.g., 0.5)
                        Vector velocity = direction.normalize().multiply(0.5);
                        player.setVelocity(velocity);
                    } else {
                        cancel(); // Stop pulling when close to the hook
                    }
                }
            }.runTaskTimer(Freedom.get_plugin(), 0L, 1L); // Run every 1 tick
        }
    }
  }
}
