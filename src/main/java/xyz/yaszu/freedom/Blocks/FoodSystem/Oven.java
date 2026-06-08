package xyz.yaszu.freedom.Blocks.FoodSystem;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseFood;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Subsystems.GamblingManager;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Oven extends Util implements BaseBlock, BaseItem {
    @Override
    public ItemStack block() {
        ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("oven"));
        meta.getPersistentDataContainer().set(keygen("customBlock"), PersistentDataType.STRING,"oven");
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING, "oven");
        stack.setItemMeta(meta);
        return stack;
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

        return new GamblingManager.GamblingInventory(Bukkit.createInventory(new OvenMiniGame(), 27, dess("Oven Mini-Game")));
    }

    @Override
    public ItemStack item() {
        return block();
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        //none
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
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getInventory().getHolder() != this) return;
            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem() == stack) {
                if (hasReacted.containsKey(event.getWhoClicked().getUniqueId())) {
                    return;
                }
                hasReacted.put(event.getWhoClicked().getUniqueId(), true);
            }
            event.setCancelled(true);
        }


        public enum GameState {
            WAITING,
            BAKING,
            DONE
        }
        Random random = new Random();
        public ItemStack stack = ItemStack.of(Material.FLINT);
        public void placeRandomItemForReaction() {
            //this should be instantiated so perhaps no tripping?
            inventory.setItem(random.nextInt(27), stack);
        }
        public static HashMap<UUID, Boolean> hasReacted = new HashMap<>();
        public void startGame(Player player, BaseFood food) {
            new BukkitRunnable() {
                GameState state = GameState.WAITING;
                int ticks = 0;
                int waitTicks = 0;
                double randomValue = secondsToTicks(random.nextInt(food.avgCookTime()));
                public int reactionTime = 0;
                @Override
                public void run() {
                    if (gameDone) {
                        cancel();
                        return;
                    }
                    switch (food.gameType()) {
                        case QuickTime -> {
                            switch (state) {
                                case WAITING -> {
                                    waitTicks++;
                                    if (waitTicks >= randomValue) {
                                        state = GameState.BAKING;
                                        reactionTime = ticks;
                                        waitTicks = 0;
                                        randomValue = secondsToTicks(random.nextInt(food.avgCookTime()));
                                        placeRandomItemForReaction();
                                    }
                                }
                                case BAKING -> {
                                    if (ticks % 20 == 0) {
                                        player.playSound(player.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1f, 1f);
                                    }
                                    //let's place random item with a time left to click
                                    if (ticks + food.avgCookTime() >= reactionTime) {
                                        if (hasReacted.containsKey(player.getUniqueId())) {
                                            if (ticks >= secondsToTicks(food.avgCookTime())) {
                                                gameDone = true;
                                                state = GameState.DONE;
                                            } else {
                                                state = GameState.WAITING;
                                            }
                                        }
                                        reactionTime++;
                                    }
                                }
                            }
                        }
                        case Tetris -> {

                        }
                        case Dropper -> {

                        }
                    }
                    ticks = ticks + 1;

                }
            }.runTaskTimer(Freedom.get_plugin(), 0,1);
        }
        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }

        public int getInventoryRow(int slot) {
            return slot / 9;
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
