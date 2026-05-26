package xyz.yaszu.freedom.Items.Swords.Items;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Items.Swords.Sword;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.List;

import static xyz.yaszu.freedom.Util.Util.getGroundLocation;

public class Firebrand extends Util implements BaseItem, Sword, Listener {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.DIAMOND_SWORD);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b><i><gradient:#E82A03:#FE8500>Firebrand</gradient></i></b>"));
        meta.getPersistentDataContainer().set(keygen("sword"), PersistentDataType.STRING, "firebrand");
        meta.setItemModel(NamespacedKey.minecraft("firebrand"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    public boolean isHittingWithSword(Player player) {
        try {
            return player.getInventory().getItemInMainHand().getPersistentDataContainer().get(keygen("sword"), PersistentDataType.STRING).equals("firebrand");
        } catch (Exception e) {
            return false;
        }
    }


    @EventHandler
    public void OnHit(PrePlayerAttackEntityEvent event) {
        if (!isHittingWithSword(event.getPlayer())) return;
        new BukkitRunnable() {
            int currentradius = 1;
            Location center = event.getPlayer().getLocation();
            @Override
            public void run() {
                if (currentradius > 10) {
                    List<Location> lastPoints = circlePoints(64, center, currentradius - 1);
                    lastPoints.forEach(point -> {
                        Block block = point.getBlock();
                        if (block.getType() == Material.FIRE) {
                            block.setType(Material.AIR);
                        }
                    });
                    this.cancel();
                } else {
                    List<Location> circlePoints = circlePoints(64, center, currentradius);
                    drawCircle(center, currentradius, center.getWorld(), 16, Particle.FLAME);
                    circlePoints.forEach(point -> {
                        Block block = point.getBlock();
                        if (!block.getType().isSolid()) {
                            block.setType(Material.FIRE);
                        }
                    });
                    if (currentradius != 1) {
                        List<Location> lastPoints = circlePoints(32, center, currentradius - 1);
                        lastPoints.forEach(point -> {
                            Block block = point.getBlock();
                            if (block.getType() == Material.FIRE) {
                                block.setType(Material.AIR);
                            }
                        });
                    }
                    currentradius++;
                }
            }
        }.runTaskTimer(Freedom.get_plugin(),0,1);

    }



    public List<Location> circlePoints(int points, Location center, double radius) {
        List<Location> pointsList = new ArrayList<>();
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ() + (radius * Math.sin(angle)); // Calculate Z coordinate
            double y = center.getY(); // Y remains constant for a flat circle

            // Create a new location for the point
            pointsList.add(new Location(center.getWorld(), x, y, z));
        }
        return pointsList;
    }

    @Override
    public Recipe recipe() {
        return null;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.SWORD;
    }

    @Override
    public List<Component> visions() {
        return List.of();
    }

    @Override
    public int Cooldown() {
        return 0;
    }

    @Override
    public SwordType SwordType() {
        return Firebrand.SwordType.Firebrand;
    }
}
