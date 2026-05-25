package xyz.yaszu.freedom.Items.Swords.Items;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Items.ItemListener;
import xyz.yaszu.freedom.Items.Swords.Sword;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.List;

public class Illumina extends Util implements BaseItem, Sword {
    @Override
    public ItemStack item() {
        ItemStack stack = ItemStack.of(Material.DIAMOND_SWORD);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING, "illumina");
        meta.getPersistentDataContainer().set(keygen("sword"), PersistentDataType.STRING, "Illumina");
        meta.setItemModel(NamespacedKey.minecraft("illumina"));
        meta.displayName(dess("<shadow:#000000FF><b><i><gradient:#c44dff:#ffdbfe>Illumina</gradient></i></b>"));
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        Freedom.get_plugin().getLogger().info("Illumina");
        if (Sword.canUse(player,this)) {
            Location center = player.getLocation();
            new BukkitRunnable() {
                int tick = 0;
                final int ticksTill = 3;
                @Override
                public void run() {
                    if (player.isSneaking() && center.distance(player.getLocation()) <= 1) {
                        pulseCircle(
                                center,
                                4,
                                32,
                                Particle.DUST,
                                2,
                                16,
                                4,
                                Sound.ENTITY_ELDER_GUARDIAN_HURT,
                                new Particle.DustOptions(DyeColor.MAGENTA.getColor(),2f)
                        );
                        if (tick >= ticksTill) {
                            // do stuff
                            center.getWorld().getNearbyPlayers(center,3.25,3.25,3.25).forEach(p -> {
                                if (p != player) {
                                    p.setVelocity(new Vector(0,5,20));
                                    p.damage(18);
                                }
                            });
                            drawCircle(
                                    center.clone().add(0,5,0),
                                    4,center.getWorld(),16,
                                    Particle.DUST,
                                    new Particle.DustOptions(DyeColor.MAGENTA.getColor(), 8f)
                            );
                            for (int i = 0; i < 10; i++) {
                                drawCircle(
                                        center.clone().add(0,i,0),
                                        4,center.getWorld(),32,
                                        Particle.DUST,
                                        new Particle.DustOptions(Color.PURPLE,8f)
                                );
                            }
                            center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE,1,1);
                            this.cancel();
                        } else {
                            center.getWorld().playSound(center, Sound.BLOCK_END_PORTAL_FRAME_FILL,1,1 + (tick/ticksTill));
                        }
                        tick = tick + 1;
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Freedom.get_plugin(),0,20);
            Sword.StartCooldown(player,SwordType());
        }
    }

    @Override
    public Recipe recipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(keygen("illumina"),item());
        recipe.addIngredient(ItemStack.of(Material.DIAMOND_SWORD));
        recipe.addIngredient(ItemStack.of(Material.GUNPOWDER));
        return recipe;
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
        return SwordType.Illumina;
    }
}
