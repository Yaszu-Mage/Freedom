package xyz.yaszu.freedom.Blocks.AdminPlush;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Blocks.BlockHandler;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.Objects;
import java.util.UUID;

import static xyz.yaszu.freedom.Blocks.BlockHandler.*;

public class AugurmovPlush extends Util implements BaseBlock, BaseItem {
    @Override
    public ItemStack block() {
        ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("<shadow:#000000FF><b>Augurmov Plush"));
        meta.getPersistentDataContainer().set(keygen("customBlock"), PersistentDataType.STRING,"augurmovplush");
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(),PersistentDataType.STRING,"augurmovplush");
        meta.setItemModel(NamespacedKey.minecraft("augurmovplush"));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public CollisionSize collisionSize() {
        return CollisionSize.Itsy;
    }
    @Override
    public Behavior behavior() {
        return Behavior.Building;
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
    public Object placeSound() {
        return "custom.squeak";
    }

    @Override
    public InventoryHolder inventoryHolder() {
        return null;
    }

    @Override
    public ItemStack item() {
        return block();
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), (String) placeSound(), 10f, 1f);
        UUID uuid = BlockHandler.currentCustomBlocks.get(event.getInteractionPoint());
        if (uuid != null) {
            ItemDisplay display = (ItemDisplay) Bukkit.getEntity(uuid);
            //lets do an animation
            float yaw;
            yaw = restoreRotation(Objects.requireNonNull(event.getInteractionPoint()));
            AxisAngle4f rotation = new AxisAngle4f((float) Math.toRadians(yaw), 0, -1, 0);
            new BukkitRunnable() {
                int tick = 1;
                int secondtick = 50;
                @Override
                public void run() {
                    if (secondtick == 0) {
                        this.cancel();
                    }
                    if (tick >= 50) {
                        display.setTransformation(new Transformation(
                                display.getTransformation().getTranslation(),
                                new Quaternionf(rotation),
                                new Vector3f(1,tick/50,1),
                                display.getTransformation().getRightRotation()
                        ));
                        tick++;
                    } else {
                        display.setTransformation(new Transformation(
                                display.getTransformation().getTranslation(),
                                new Quaternionf(rotation),
                                new Vector3f(1,secondtick/50,1),
                                display.getTransformation().getRightRotation()
                        ));
                        secondtick--;
                    }

                    if (tick == 100) {
                        float s = (float) 1;
                        display.setTransformation(new Transformation(
                                display.getTransformation().getTranslation(),
                                new Quaternionf(rotation),
                                new Vector3f(s, s, s),
                                display.getTransformation().getRightRotation()
                        ));
                    }

                }
            }.runTaskTimer(Freedom.get_plugin(),0,2);
        }
    }

    @Override
    public Recipe recipe() {
        return null;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.BLOCK;
    }
}
