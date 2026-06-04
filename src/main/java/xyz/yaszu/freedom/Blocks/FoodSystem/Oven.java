package xyz.yaszu.freedom.Blocks.FoodSystem;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseFood;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.Util;

public class Oven extends Util implements BaseBlock, BaseItem {
    @Override
    public ItemStack block() {
        return null;
    }

    @Override
    public CollisionSize collisionSize() {
        return CollisionSize.Teeny;
    }

    @Override
    public Behavior behavior() {
        return Behavior.Interface;
    }

    @Override
    public int toolValue() {
        return 0;
    }

    @Override
    public boolean waterNeeded() {
        return false;
    }

    @Override
    public double scale() {
        return 1;
    }

    @Override
    public Location mountLocation() {
        return null;
    }

    @Override
    public Object placeSound() {
        return Sound.BLOCK_IRON_PLACE;
    }

    @Override
    public InventoryHolder inventoryHolder() {
        Bukkit.getPluginManager().registerEvents(new OvenMiniGame(), Freedom.get_plugin());
        return null;
    }

    @Override
    public ItemStack item() {
        return block();
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    @Override
    public Recipe recipe() {
        ShapedRecipe recipe = new ShapedRecipe(keygen("oven"), block());
        recipe.shape("WWW","WGW","WBW");
        recipe.setIngredient('W', Material.IRON_INGOT);
        recipe.setIngredient('G', Material.FURNACE);
        recipe.setIngredient('B', Material.BLAZE_POWDER);
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.BLOCK;
    }


    public static class OvenMiniGame implements Listener,   InventoryHolder {
        Inventory inventory;
        boolean gameDone = false;
        public OvenMiniGame() {
            Inventory inv = Bukkit.createInventory(this, 27, dess("Oven Mini-Game"));
        }



        public enum GameState {
            WAITING,
            BAKING,
            DONE
        }
        public void startGame(Player player, BaseFood food) {

            new BukkitRunnable() {
                GameState state = GameState.WAITING;
                int ticks = 0;
                @Override
                public void run() {
                    if (gameDone) {
                        cancel();
                        return;
                    }
                    ticks = ticks + 1;

                }
            }.runTaskTimer(Freedom.get_plugin(), 0,1);
        }
        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }

        @EventHandler
        public void onPlayerCloseInventory(InventoryCloseEvent event) {
            if (inventory.getHolder() != this) return;
            if (event.getInventory() == inventory && gameDone) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().openInventory(inventory);
                    }
                }.runTaskLater(Freedom.get_plugin(), 10);

            }
        }
    }
}
