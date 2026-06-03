package xyz.yaszu.freedom.Items.Swords.Items;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Items.Swords.Sword;
import xyz.yaszu.freedom.Subsystems.TrustManager;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

public class Windforce extends Util implements BaseItem, Sword {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.DIAMOND_SWORD);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING, "windforce");
        meta.getPersistentDataContainer().set(keygen("sword"), PersistentDataType.STRING, "Windforce");
        meta.setItemModel(NamespacedKey.minecraft("windforce"));
        meta.displayName(dess("<shadow:#000000FF><b><i><gradient:#abedff:#fff2ab>Windforce</gradient></i></b>"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        if (Sword.canUse(player,this)) {
            // do Windforce
            Location location = player.getLocation();
            drawCircle(location,10,location.getWorld(),32, Particle.GUST_EMITTER_LARGE);
            location.getNearbyPlayers(10).forEach(p -> {
               if (!TrustManager.isMutual(p.getUniqueId(),player.getUniqueId())) {
                   p.setVelocity(directionTo(player.getLocation(),p.getLocation()).multiply(-4));
               }
            });
            location.getWorld().playSound(location, Sound.ENTITY_WIND_CHARGE_WIND_BURST,1,1);
            Sword.StartCooldown(player,SwordType.Windforce);
        }
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
                dess("Everything you built will crumble")
        );
    }

    @Override
    public int Cooldown() {
        return 0;
    }

    @Override
    public SwordType SwordType() {
        return SwordType.Windforce;
    }
}
