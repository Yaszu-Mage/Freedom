package xyz.yaszu.freedom.Items.Swords.Items;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Items.Swords.Sword;
import xyz.yaszu.freedom.Subsystems.TrustManager;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

public class Venomshank implements BaseItem, Sword {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.DIAMOND_SWORD);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b><i><gradient:#3d431e:#7a863c>Venomshank</gradient></i></b>"));
        meta.setItemModel(NamespacedKey.minecraft("venomshank"));
        meta.getPersistentDataContainer().set(keygen("sword"), PersistentDataType.STRING, "venomshank");
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        if (Sword.canUse(player,this)) {
            Location location = player.getLocation();
            location.getNearbyPlayers(10).forEach(p -> {
                if (!TrustManager.isTrusted(player.getUniqueId(),p.getUniqueId())) {
                    if (!TrustManager.isTrusted(p.getUniqueId(),player.getUniqueId())) {
                        p.addPotionEffect(PotionEffectType.POISON.createEffect(200,3));
                    }
                }
            });
            Sword.StartCooldown(player,SwordType.Venomshank);
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
                dess("<gradient:#3d431e:#7a863c>You will fail.</gradient>"),
                dess("<gradient:#3d431e:#7a863c>Death is inevitable.</gradient>"),
                dess("Kill them, it is the only option to salvation")
        );
    }

    @Override
    public int Cooldown() {
        return 30000;
    }

    @Override
    public SwordType SwordType() {
        return SwordType.Venomshank;
    }
}
