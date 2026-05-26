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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static xyz.yaszu.freedom.Util.Util.getGroundLocation;

public class Firebrand implements BaseItem, Sword, Listener {
    private final Map<UUID, BukkitRunnable> activeTasks = new ConcurrentHashMap<>();
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.DIAMOND_SWORD);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Util.dess("<shadow:#000000FF><b><i><gradient:#E82A03:#FE8500>Firebrand</gradient></i></b>"));
        meta.getPersistentDataContainer().set(Util.keygen("sword"), PersistentDataType.STRING, "firebrand");
        meta.setItemModel(NamespacedKey.minecraft("firebrand"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    public boolean isHittingWithSword(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        String swordType = meta.getPersistentDataContainer().get(Util.keygen("sword"), PersistentDataType.STRING);
        return "firebrand".equals(swordType);
    }


    @EventHandler
    public void OnHit(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        if (!isHittingWithSword(player)) return;

        if (activeTasks.containsKey(player.getUniqueId())) {
            return;
        }

        BukkitRunnable task = new BukkitRunnable() {
            int currentRadius = 1;
            final Location center = player.getLocation().clone();
            {
                center.setY(getGroundLocation(player.getLocation()));
            }

            @Override
            public void run() {
                if (currentRadius > 10) {
                    clearPreviousCircle(center, currentRadius - 1);
                    this.cancel();
                    activeTasks.remove(player.getUniqueId());
                } else {
                    Util.drawCircle(center, currentRadius, center.getWorld(), 16, Particle.FLAME);
                    List<Location> circlePoints = circlePoints(64, center, currentRadius);
                    circlePoints.forEach(point -> {
                        Block block = point.getBlock();
                        if (!block.getType().isSolid()) {
                            block.setType(Material.FIRE);
                        }
                    });

                    if (currentRadius > 1) {
                        clearPreviousCircle(center, currentRadius - 1);
                    }
                    currentRadius++;
                }
            }
        };

        activeTasks.put(player.getUniqueId(), task);
        task.runTaskTimer(Freedom.get_plugin(), 0, 1);
    }

    private void clearPreviousCircle(Location center, double radius) {
        List<Location> lastPoints = circlePoints(32, center, radius);
        lastPoints.forEach(point -> {
            Block block = point.getBlock();
            if (block.getType() == Material.FIRE) {
                block.setType(Material.AIR);
            }
        });
    }


    public List<Location> circlePoints(int points, Location center, double radius) {
        List<Location> pointsList = new ArrayList<>();
        double angleIncrement = 2 * Math.PI / points;
        for (int i = 0; i < points; i++) {
            double angle = i * angleIncrement;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            pointsList.add(new Location(center.getWorld(), x, center.getY(), z));
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
