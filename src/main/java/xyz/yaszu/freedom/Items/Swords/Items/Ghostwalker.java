package xyz.yaszu.freedom.Items.Swords.Items;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Items.Swords.Sword;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;
import java.util.Objects;

public class Ghostwalker extends Util implements BaseItem, Sword {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.DIAMOND_SWORD);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(keygen("sword"), PersistentDataType.STRING, "ghostwalker");
        meta.setItemModel(NamespacedKey.minecraft("ghostwalker"));
        meta.displayName(dess("<shadow:#000000FF><b><i><gradient:#555755:#fbfffa>Ghostwalker</gradient></i></b>"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        Location location = player.getLocation();
        if (Sword.canUse(player,this)) {
            try {
                if (!location.getNearbyPlayers(5).isEmpty()) {
                    Player target = location.getNearbyPlayers(5).stream().findFirst().orElse(null);
                    if (target != null && target != player) {
                        if (!Life_and_Death.is_alive(target)) {
                            Life_and_Death.revive_player(target,location);
                            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).addModifier(new AttributeModifier(keygen("ghostwalker"),-1, AttributeModifier.Operation.ADD_NUMBER));
                            pulseCircle(target.getLocation(),2,16, Particle.SNOWFLAKE,1,2,3, Sound.ENTITY_WITCH_DRINK,new Particle.DustOptions(Color.fromRGB(255,255,255),0.5f));
                            //VFX
                        }

                    }
                }
            } catch (Exception ignored) {}
            Sword.StartCooldown(player,SwordType());
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
                dess("<shadow:#000BBB><dark_red><b>S E A R C H I N G F O R A W A Y O U T.\n" +
                        "A N D F O U N D N O T H I N G .</b></dark_red>"),
                dess("<shadow:#000BBB><dark_red><b>H A T R E D F O R M S I N T H E M O U T H O F T H E C O R R U P T E D</b></dark_red>")
        );
    }

    @Override
    public int Cooldown() {
        return 100000;
    }

    @Override
    public SwordType SwordType() {
        return SwordType.Ghostwalker;
    }
}
