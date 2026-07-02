package xyz.yaszu.freedom.Soul.Base;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import jdk.jfr.Experimental;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.Util;

import java.util.Random;

import static xyz.yaszu.freedom.Util.Util.*;
@Experimental
public class BaseCyan implements Base_Soul, Listener {
    @Override
    public String Name_For_Container() {
        return "BaseCyan";
    }

    @Override
    public Component Name() {
        return dess("<cyan>Cyan</cyan>");
    }

    @Override
    public Component Description() {
        //TODO fix description
        return dess("ZAP ZAP ZAP ZAP");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.LIGHTNING_ROD);
    }

    @Override
    public Component AbilityOneName() {
        return dess("Ability One: <cyan>Zap</cyan>");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("Zap between 5 locations quickly");
    }

    @Override
    public void AbilityOne(Player player) {
        int teleportAmount = player.getPersistentDataContainer().getOrDefault(keygen("cyanTeleport".toLowerCase()), PersistentDataType.INTEGER, 0);
        if (can_ability(AbilityOne_Cooldown(player),abilityOneCooldowns,player.getUniqueId())) {
            Random random = new Random();
            World world = player.getWorld();
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1f);
            Location teleportLocation = player.getLocation().add(player.getLocation().getDirection().multiply(4.5));
            Location midPoint = getMidpoint(player.getLocation(), teleportLocation);
            midPoint.add(random.nextDouble(-1,1),0,random.nextDouble(-1,1));
            drawLine(player.getLocation().clone().add(0,1,0),midPoint.add(0,1,0),world,16, Particle.ELECTRIC_SPARK);
            drawLine(teleportLocation.add(0,1,0),midPoint.add(0,1,0),world,16, Particle.ELECTRIC_SPARK);
            player.getWorld().playSound(teleportLocation, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1f);
            player.teleport(teleportLocation);
            teleportAmount++;
            player.getPersistentDataContainer().set(keygen("cyanTeleport".toLowerCase()), PersistentDataType.INTEGER, teleportAmount);
            if (teleportAmount >= 5) {
                Freedom.get_plugin().getLogger().info("resetting!");
                player.getPersistentDataContainer().set(keygen("cyanTeleport".toLowerCase()), PersistentDataType.INTEGER, 0);
                abilityOneCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            } else {
                player.getPersistentDataContainer().set(keygen("cyanTeleport".toLowerCase()), PersistentDataType.INTEGER, teleportAmount);
            }
        }
    }

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.LIGHTNING_ROD);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("Ability Two: <cyan>Zap</cyan>");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("Zap between 5 locations quickly");
    }
    double comboTime = 5.5;
    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        if (can_ability(AbilityTwo_Cooldown(),abilityOneCooldowns,player.getUniqueId())) {
            womboCombo(player).runTaskTimer(Freedom.get_plugin(), 0L, (long) secondsToTicks(comboTime));
        }
    }

    public static BukkitRunnable womboCombo(Player player) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                Long lastHit = player.getPersistentDataContainer().getOrDefault(keygen("wombocombokept"),PersistentDataType.LONG, 0L);
                long Timer = lastHit * 1000;
                long currentTime = System.currentTimeMillis();
                if (currentTime + 5500 <= currentTime + Timer) {
                    player.getPersistentDataContainer().set(keygen("wombocombo"),PersistentDataType.BOOLEAN,true);
                } else {
                    abilityTwoCooldowns.put(player.getUniqueId(),currentTime);
                    this.cancel();
                }
            }
        };
    }

    @EventHandler
    public void PreplayerAttack(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        SoulTypes soulType = getSoulType(player);
        if (soulType == SoulTypes.Cyan && player.getPersistentDataContainer().getOrDefault(keygen("wombocombo"),PersistentDataType.BOOLEAN,false)) {
            if (event.getAttacked() instanceof Player attacked) {
                player.getPersistentDataContainer().set(keygen("wombocombokept"),PersistentDataType.LONG,System.currentTimeMillis());
                silenceFor(attacked, 5);
            }
            dealTrueDamage((LivingEntity) event.getAttacked(),1);
        }
    }


    @Override
    public Component Passive_Description() {
        return dess("Voltage Transient: Reach 2 blocks Farther");
    }

    @Override
    public void Passive(Player player, Object event) {

    }

    @Override
    public Component ActivePassive_Description() {
        return dess("Joule Surge: Haste 3 for 300 ticks");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 40000;
    }

    @Override
    public long AbilityOne_Cooldown(Object given) {
        return 10000;
    }

    @Override
    public void ActivePassive(Player player) {
        Double soulpoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        if (soulpoints >= 5) {
            player.addPotionEffect(PotionEffectType.HASTE.createEffect(300,2));
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, soulpoints - 5);
        }
    }
}
