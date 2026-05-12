package xyz.yaszu.freedom.Soul.Alchemy;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.BetterModelPlatform;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.platform.PlatformEntity;
import kr.toxicity.model.api.tracker.EntityTracker;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Subsystems.TrustManager;
import xyz.yaszu.freedom.Util.Util;

public class Leaf extends Util implements Base_Soul {
    @Override
    public String Name_For_Container() {
        return "leaf";
    }

    @Override
    public Component Name() {
        return dess("<green>Leaf</green>");
    }

    @Override
    public Component Description() {
        //TODO real description
        return dess("I'll leaf you alone");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.OAK_LEAVES);
    }

    @Override
    public Component AbilityOneName() {
        return dess("Ability One: Foliage");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("Teleport 5 blocks in the direction you are looking at.");
    }

    @Override
    public void AbilityOne(Player player) {
        if (can_ability(AbilityOne_Cooldown(player), abilityOneCooldowns, player.getUniqueId())) {
            //Calc teleport direction
            Location playerLocation = player.getLocation();
            Vector dir = player.getLocation().getDirection();
            Location teleportLocation = player.getLocation().add(dir.multiply(5));
            double distance = playerLocation.distance(teleportLocation);
            //Display Visual Effects
            drawLine(playerLocation,teleportLocation,playerLocation.getWorld(), (int) Math.round(distance/2), Particle.TINTED_LEAVES,10,1,1,1, Color.GREEN);
            //Play Sounds
            player.getWorld().playSound(playerLocation, Sound.BLOCK_LEAF_LITTER_STEP, 1f, 1f);
            player.getWorld().playSound(teleportLocation, Sound.BLOCK_LEAF_LITTER_BREAK, 1f, 1f);
            player.getWorld().playSound(playerLocation, "custom.windy", 1f, 1f);
            //Teleport Player
            player.teleport(teleportLocation);
        }
    }

    @Override
    public ItemStack Related_Item() {
        ItemStack item = ItemStack.of(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(dess("Leaf Blade"));
        meta.setItemModel(NamespacedKey.minecraft("leafblade"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Component AbilityTwoName() {
        return dess("Leaf Blade");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("Create a circular blade that damages people withing a 5 block radius.");
    }

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        Location center = player.getLocation();
        //visual and audio effects
        drawCircle(center,1.3,center.getWorld(),32,Particle.PALE_OAK_LEAVES);
        player.getWorld().playSound(center, Sound.BLOCK_LEAF_LITTER_STEP, 1f, 1f);
        player.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
        //damage
        center.getNearbyEntitiesByType(Player.class, 2).forEach(entity -> {
           if (player != entity) {
               if (!TrustManager.isTrustedBy(entity, player) && !TrustManager.isTrustedBy(player, entity)) {
                   int bonus = 0;
                   if (player.getPotionEffect(PotionEffectType.SLOW_FALLING) != null) {
                       bonus = player.getPotionEffect(PotionEffectType.SLOW_FALLING).getAmplifier();
                   }
                   entity.damage(6);
                   entity.addPotionEffect(PotionEffectType.SLOW_FALLING.createEffect(100,bonus));
               }
           }
        });
        center.getNearbyEntities(5,5,5).forEach(entity -> {
            if (!(entity instanceof Player) && entity instanceof LivingEntity living) {
                int bonus = 0;
                if (living.getPotionEffect(PotionEffectType.SLOW_FALLING) != null) {
                    bonus = living.getPotionEffect(PotionEffectType.SLOW_FALLING).getAmplifier();
                }
                living.damage(6);
                living.addPotionEffect(PotionEffectType.SLOW_FALLING.createEffect(100,bonus));
            }

        });

    }

    @Override
    public Component Passive_Description() {
        return dess("Photosynthesis: Regenerate 1 food point every 5 seconds while in sunlight.");
    }

    @Override
    public void Passive(Player player, Object event) {
        if (player.getLocation().getBlock().getLightFromSky() > 0) {
            player.setFoodLevel(Math.min(player.getFoodLevel() + 1, 20));
        }
    }

    @Override
    public Component ActivePassive_Description() {
        return dess("Root yourself into the ground");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 0;
    }

    @Override
    public long AbilityOne_Cooldown(Object obj) {
        return 0;
    }

    @Override
    public void ActivePassive(Player player) {
        player.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(100,4));
        player.addPotionEffect(PotionEffectType.SLOW_FALLING.createEffect(100,4));
        //do vfx
        Entity entity = player.getWorld().spawnEntity(player.getLocation(), EntityType.MANNEQUIN);
        BetterModelPlatform platform = BetterModel.platform();
        PlatformEntity platwolf = BukkitAdapter.adapt(entity);
        EntityTracker tracker = BetterModel.model("roots")
                .map(r -> r.getOrCreate(platwolf))
                .orElse(null);
        new BukkitRunnable() {
            @Override
            public void run() {
                assert tracker != null;
                tracker.despawn();
                entity.remove();

            }
        }.runTaskLater(Freedom.get_plugin(),100);
    }
}
