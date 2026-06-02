package xyz.yaszu.freedom.Soul.Base;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

public class BaseCyan extends Util implements Base_Soul, Listener {
    @Override
    public String Name_For_Container() {
        return "BaseCyan";
    }

    @Override
    public Component Name() {
        return dess("<color:#00ffff>Cyan</color>");
    }

    @Override
    public Component Description() {
        return dess("Your ambition flows like a river");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.WATER_BUCKET);
    }

    @Override
    public Component AbilityOneName() {
        return dess("Ability One - Drown");
    }

    @Override
    public Component AbilityOneDescription() {
        return null;
    }

    public static HashMap<UUID,Integer> offset = new HashMap<>();


    @EventHandler
    public void onInventorySlotChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        int oldSlot = event.getPreviousSlot();
        int newSlot = event.getNewSlot();
        if (getSoulType(player) == SoulTypes.BaseCyan || getSoulType(player) == SoulTypes.Cyan && player.getPersistentDataContainer().getOrDefault(activeAbilityOne, PersistentDataType.BOOLEAN, false)) {
            if (oldSlot >= newSlot) {
                offset.put(player.getUniqueId(), Math.clamp(offset.getOrDefault(player.getUniqueId(), 0) - 1, 0, 15));
            } else {
                offset.put(player.getUniqueId(), Math.clamp(offset.getOrDefault(player.getUniqueId(), 0) + 1,0,15));
            }
            event.setCancelled(true);
        }


    }

    public NamespacedKey activeAbilityOne = keygen("ActiveAbilityOne");
    Predicate<Entity> filter(Player player) {
        return entity -> !(entity instanceof ItemDisplay || entity == player);
    }



    @Override
    public void AbilityOne(Player player) {
        // get player that is being looked at
        Vector vector = player.getLocation().getDirection();
        // check if vector is pointing at a nearby player
        Entity entity;
        RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(),player.getEyeLocation().getDirection(),10,0.5,filter(player));
        if (result != null) {
            if (result.getHitBlock() != null) {
                entity = null;
                // Player is looking at a block
            } else if (result.getHitEntity() != null) {
                entity = result.getHitEntity();
                Freedom.get_plugin().getLogger().info("Player is looking at entity: " + entity.getName());
            } else {
                entity = null;
            }
        } else {
            entity = null;
        }
        if (entity == null) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.5f);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);
            player.sendMessage(dess("<b><Red>ERROR</Red></b>You are not looking at something!!"));
            return;
        }
        player.getPersistentDataContainer().set(keygen("ActiveAbilityOne"), PersistentDataType.BOOLEAN,true);
        offset.put(player.getUniqueId(), (int) entity.getLocation().distance(player.getLocation()));
        new BukkitRunnable() {
            public final ArrayList<Block> blocksAffected = new ArrayList<>();
            int noSneak = 0;
            @Override
            public void run() {
                //let's fill in like a 3x3 around them and move them to the players look location plus an offset
                // first we get offset
                if (!player.isSneaking()) {
                    noSneak++;
                }
                blocksAffected.forEach(block -> block.setType(Material.AIR));
                blocksAffected.clear();
                offset.putIfAbsent(player.getUniqueId(), 10);
                Vector vector = player.getEyeLocation().getDirection();
                //let's get offset from that vector
                int offsetValue = offset.get(player.getUniqueId());
                Location targetLocation = player.getLocation().add(vector.multiply(offsetValue));
                double distance = entity.getLocation().distance(targetLocation);
                if (distance <= 0.5) {
                    entity.setVelocity(new Vector(0,0,0));
                    return;
                }
                //now let's calc the velocity to the target location
                Vector velocity = targetLocation.toVector()
                        .subtract(entity.getLocation().toVector())
                        .normalize()
                        .multiply(0.5);
                //now let's create the bubble
                Location corner = entity.getLocation().clone().subtract(1, -1, 1);
                entity.setVelocity(velocity);
                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        for (int z = 0; z < 3; z++) {
                            Block block = corner.clone().add(x, y, z).getBlock();
                            if (block.getType() == Material.AIR) {
                                block.setType(Material.WATER);
                                blocksAffected.add(block);
                            }
                        }
                    }

                }
                if (entity.isDead() || player.isDead() || noSneak >= 10) {
                    player.getPersistentDataContainer().remove(activeAbilityOne);
                    cancel();
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 10);
    }

    @Override
    public ItemStack Related_Item() {
        return null;
    }

    @Override
    public Component AbilityTwoName() {
        return null;
    }

    @Override
    public Component AbilityTwoDescription() {
        return null;
    }

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        player.setRiptiding(true);
        player.setVelocity(player.getLocation().getDirection().multiply(2));
    }

    @Override
    public Component Passive_Description() {
        return null;
    }

    @Override
    public void Passive(Player player, Object event) {
        player.addPotionEffect(PotionEffectType.WATER_BREATHING.createEffect(PotionEffect.INFINITE_DURATION,0));
    }

    @Override
    public Component ActivePassive_Description() {
        return null;
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 1000;
    }

    @Override
    public long AbilityOne_Cooldown(Object given) {
        return 80000;
    }

    @Override
    public void ActivePassive(Player player) {
        player.addPotionEffect(PotionEffectType.CONDUIT_POWER.createEffect(100,0));
        player.getPersistentDataContainer().set(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE,(double) getSoulPoints(player) - 5);
    }
}
