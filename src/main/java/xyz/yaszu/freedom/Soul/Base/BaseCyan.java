package xyz.yaszu.freedom.Soul.Base;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.Util;

import java.util.Random;

public class BaseCyan extends Util implements Base_Soul, Listener {
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
            if (teleportAmount > 5) {
                abilityOneCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + AbilityOne_Cooldown(player));
                return;
            }
            Random random = new Random();
            World world = player.getWorld();
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 1f);
            Location teleportLocation = player.getLocation().add(player.getLocation().getDirection().multiply(1.25));
            Location midPoint = getMidpoint(player.getLocation(), teleportLocation);
            midPoint.add(random.nextDouble(-8,8),0,random.nextDouble(-8,8));
            drawLine(player.getLocation(),midPoint,world,16, Particle.ELECTRIC_SPARK,0,0,0,0,Color.BLUE);
            drawLine(teleportLocation,midPoint,world,16, Particle.ELECTRIC_SPARK,0,0,0,0,Color.BLUE);
            player.getWorld().playSound(teleportLocation, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 1f);
            player.teleport(teleportLocation);
            teleportAmount++;
            if (teleportAmount > 5) {

                abilityOneCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + AbilityOne_Cooldown(player));
            }
            player.getPersistentDataContainer().set(keygen("cyanTeleport".toLowerCase()), PersistentDataType.INTEGER, teleportAmount);
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

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        if (can_ability(AbilityTwo_Cooldown(),abilityOneCooldowns,player.getUniqueId())) {

        }
    }

    @EventHandler
    public void PreplayerAttack(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        SoulTypes soulType = getSoulType(player);
        if (soulType == SoulTypes.Cyan && player.getPersistentDataContainer().getOrDefault(keygen("wombocombo"),PersistentDataType.BOOLEAN,false)) {
            player.getPersistentDataContainer().set(keygen("wombocombokept"),PersistentDataType.LONG,System.currentTimeMillis());
        }

    }


    @Override
    public Component Passive_Description() {
        return null;
    }

    @Override
    public void Passive(Player player, Object event) {

    }

    @Override
    public Component ActivePassive_Description() {
        return null;
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 0;
    }

    @Override
    public long AbilityOne_Cooldown(Object given) {
        return 0;
    }

    @Override
    public void ActivePassive(Player player) {

    }
}
