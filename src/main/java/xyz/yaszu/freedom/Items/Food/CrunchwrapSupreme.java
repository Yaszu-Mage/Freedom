package xyz.yaszu.freedom.Items.Food;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.Random;

public class CrunchwrapSupreme extends Util implements BaseItem {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"crunchwrapsupreme");
        meta.displayName(dess("<shadow:#000000FF><b><yellow>Crunchwrap Supreme</yellow></b>"));
        meta.setItemModel(NamespacedKey.minecraft("crunchwrapsupreme"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        player.setSaturation(player.getSaturation()+0.2f);
        player.setFoodLevel(player.getFoodLevel()-3);
        player.getWorld().spawnParticle(Particle.EGG_CRACK,player.getLocation(),10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP,10f,2f);
        ShitYourself(player, 2);
        item.subtract();
    }


    public static void ShitYourself(Player player,int shittime){
        new BukkitRunnable() {
            public static int ticks = 0;
            public Vector vector = new Vector(0,1,0);
            Random random = new Random();
            @Override
            public void run() {

                player.setVelocity(vector);

                player.getWorld().spawnParticle(Particle.DUST,
                        player.getLocation(),
                        4,
                        new Particle.DustOptions(
                                Color.fromRGB(196, 164, 132),
                                random.nextInt(0,2)
                        )
                );
                player.getWorld().spawnParticle(
                        Particle.DUST,
                        player.getLocation(),
                        4,
                        new Particle.DustOptions(
                                Color.fromRGB(101, 67, 33),
                                random.nextInt(0,4)
                        )
                );
                if (ticks >= (shittime * 20)) {
                    cancel();
                }
                ticks = ticks + 1;
            }
        }.runTaskTimer(Freedom.get_plugin(),0,0);
    }
    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(keygen("crunchwrapsupreme"),item());
        recipe.addIngredient(ItemStack.of(Material.BREAD));
        recipe.addIngredient(ItemStack.of(Material.BEEF));
        recipe.addIngredient(ItemStack.of(Material.BEETROOT));
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.FOOD;
    }
}
