package xyz.yaszu.freedom.Subsystems;

import it.unimi.dsi.fastutil.Hash;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.Drinks.BaseDrink;
import xyz.yaszu.freedom.Items.Drinks.Martini;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static xyz.yaszu.freedom.Util.Util.isItemNull;

public class CustomBrewListener implements Listener {

    public HashMap<ItemStack, BaseDrink> customDrinks = new HashMap<>();
    public static ArrayList<Location> activeBrewingStands = new ArrayList<>();

    public void register() {
        Freedom.get_plugin().getServer().getPluginManager().registerEvents(this, Freedom.get_plugin());
        Freedom.get_plugin().getLogger().info("Registered CustomBrewListener");
        registerDrink(new Martini());
    }

    public void registerDrink(BaseDrink drink) {
        customDrinks.put(drink.ingredient(), drink);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.BREWING) return;
        if (activeBrewingStands.contains(event.getInventory().getLocation())) {
            event.setCancelled(true);
            return;
        }
        BrewerInventory inventory = (BrewerInventory) event.getInventory();
        if (event.getRawSlot() != 3) return;
        ItemStack clickedItem = event.getCurrentItem();
        if (isItemNull(clickedItem)) return;
        if (isItemNull(event.getInventory().getItem(0)) ||isItemNull(event.getInventory().getItem(1)) || isItemNull(event.getInventory().getItem(3)) ) return;
        if (event.getInventory().getItem(3).isSimilar(event.getInventory().getItem(1))) return;
        BaseDrink drink = getDrink(event.getInventory().getItem(0), event.getInventory().getItem(3));
        activeBrewingStands.add(event.getInventory().getLocation());
        Random random = new Random();
        new BukkitRunnable() {
            int tick = 0;
            final double waitTime = random.nextDouble(drink.inbetweenBrewTime() - (drink.inbetweenBrewTime() * 0.5),drink.inbetweenBrewTime());
            final Player player = (Player) event.getWhoClicked();
            final Location location = player.getLocation();
            @Override
            public void run() {
                if (tick >= waitTime) {
                    Bukkit.getScheduler().runTask(Freedom.get_plugin(), () -> {
                        event.getInventory().setItem(2, drink.result());
                        event.getInventory().setItem(0, drink.result());
                        event.getInventory().setItem(1, drink.result());
                        event.getInventory().setItem(3, ItemStack.of(Material.AIR));
                    });
                    this.cancel();
                } else {
                    if (player.getLocation().distance(location) > 5) {
                        Bukkit.getScheduler().runTask(Freedom.get_plugin(), () -> {
                            player.getWorld().spawnParticle(Particle.FLAME, location, 10, 0.5, 0.5, 0.5, 0.05);
                            player.playSound(location, Sound.BLOCK_BREWING_STAND_BREW, 1f,random.nextFloat(0.5f,1.5f));
                        });
                    }
                }
                tick++;
            }
        }.runTaskTimerAsynchronously(Freedom.get_plugin(),0,20);
    }



    public BaseDrink getDrink(ItemStack base, ItemStack addon) {
        for (BaseDrink drink : customDrinks.values()) {
            if (drink.ingredient().isSimilar(base) && drink.stir().isSimilar(addon)) {
                return drink;
            }
        }
        return null;
    }

}
