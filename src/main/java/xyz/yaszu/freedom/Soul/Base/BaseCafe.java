package xyz.yaszu.freedom.Soul.Base;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

import static xyz.yaszu.freedom.Util.Util.*;

public class BaseCafe implements Base_Soul {
    @Override
    public String Name_For_Container() {
        return "BaseCafe";
    }

    @Override
    public Component Name() {
        return dess("<color:#F9EBDE>Cafe</color>");
    }

    @Override
    public Component Description() {
        return dess("What is it but coffee?");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.YELLOW_DYE);
    }

    @Override
    public Component AbilityOneName() {
        return dess("<color:#F9EBDE>Ability One: Selective TP</color>");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("You can selectively swap with the Cafe you are bonded with");
    }
    BaseMocha mocha = new BaseMocha();
    @Override
 public void AbilityOne(Player player) {
        AbilityOne(player, false);
    }

    @Override
 public void AbilityOne(Player player, boolean is_imbue) {
        mocha.AbilityOne(player);
    }

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.CLOCK);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("Ability Two: <color:#F9EBDE> AOE </color>");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("Damage people within a 5 block radius with an AOE attack");
    }
    @Override
 public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        AbilityTwo(player, ability_item, false);
    }

    @Override
 public void AbilityTwo(Player player, ItemStack ability_item, boolean is_imbue) throws MineSkinException, DataRequestException {
        if (can_ability(AbilityTwo_Cooldown(), abilityTwoCooldowns,player.getUniqueId())) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    List<Entity> nearguys = player.getNearbyEntities(5,5,5);
                    Player ignoreplayer = player;
                    if (player.getPersistentDataContainer().has(keygen("doubleclock"))) {
                        Player doubleclock = Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("doubleclock"), PersistentDataType.STRING));
                        if (doubleclock != null) {
                            ignoreplayer = Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("doubleclock"), PersistentDataType.STRING));
                        }
                    }
                    for (Entity entity : nearguys) {
                        if (entity instanceof Player instplayer) {
                            if (instplayer != ignoreplayer) {
                                double distance = Math.sqrt(instplayer.getLocation().distanceSquared(player.getLocation()));
                                double damage = 5.5 + ((5-distance)*tick);
                                instplayer.damage(damage,player);
                            }
                        }
                    }

                    if (tick >= 4) {
                        this.cancel();
                    }
                    player.getLocation().getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK,1,1);
                    player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 0.5f);
                    drawCircle(player.getLocation(),1,player.getLocation().getWorld(),16, Particle.LANDING_HONEY);
                    drawCircle(player.getLocation(),1,player.getLocation().getWorld(),8, Particle.DUST, new Particle.DustOptions(Color.MAROON, 1.0f));
                    drawCircle(player.getLocation(),5,player.getLocation().getWorld(),16,Particle.SWEEP_ATTACK);
                    tick++;
                }
                @Override
                public synchronized void cancel() throws IllegalStateException {
                    Bukkit.getScheduler().cancelTask(getTaskId());
                }

            }.runTaskTimer(Freedom.get_plugin(),0,20);

            abilityTwoCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
        } else {

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            if (abilityTwoCooldowns.get(player.getUniqueId()) != null) {
                double seconds = (double) (effective_cooldown(AbilityTwo_Cooldown(), player.getUniqueId()) - (System.currentTimeMillis() - abilityTwoCooldowns.get(player.getUniqueId()))) / 1000;
                player.sendActionBar(dess("You can't use this ability yet, wait " + Math.round(seconds) + " seconds"));
            }
        }
    }

    @Override
    public Component Passive_Description() {
        return dess("Shares your potion effects with your bonded half");
    }

    @Override
    public void Passive(Player player, Object event) {

    }

    @Override
    public Component ActivePassive_Description() {
        return dess("Chose those to bond with");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 3000;
    }

    @Override
    public long AbilityOne_Cooldown(Object obj) {
        return 0;
    }

    @Override
    public void ActivePassive(Player player) {
        mocha.ActivePassive(player);
    }
}


