package xyz.yaszu.freedom.Items.Swords.Items;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
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
import xyz.yaszu.freedom.Subsystems.TrustManager;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

public class Icedagger extends Util implements BaseItem, Sword {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.DIAMOND_SWORD);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(keygen("sword"), PersistentDataType.STRING, "icedagger");
        meta.setItemModel(NamespacedKey.minecraft("icedagger"));
        meta.displayName(dess("<shadow:#000000FF><b><i><gradient:#1956C0:#65caf6>Icedagger</gradient></i></b>"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        Location location = player.getLocation();
        location.getNearbyPlayers(5).forEach(p -> {
            if (!TrustManager.isMutual(player.getUniqueId(), p.getUniqueId())) {
                new BukkitRunnable() {
                    int tick = 0;
                    final Location initial = p.getLocation();
                    @Override
                    public void run() {
                        if (tick % 20 == 0) {
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1, 1);
                            p.spawnParticle(Particle.SNOWFLAKE, p.getLocation().add(0,1,0), 40, 0.5, 0.5, 0.5, 0.1);
                        }
                        p.teleportAsync(initial);
                        if (tick >= 200) {
                            this.cancel();
                        }
                        tick++;
                    }
                }.runTaskTimerAsynchronously(Freedom.get_plugin(),0,0);
            }
        });
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
        return List.of(
                dess("They will overthrow you")
        );
    }

    @Override
    public int Cooldown() {
        return 0;
    }

    @Override
    public SwordType SwordType() {
        return Icedagger.SwordType.Icedagger;
    }
}
