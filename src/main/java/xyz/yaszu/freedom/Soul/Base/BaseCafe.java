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


/**
 * Older Blue Class, Reworked into "Cafe"
 * @author Yaszu
 */
public class BaseCafe implements Base_Soul {
    /**
     * Name Used in Components
     * @return Name used in Components
     */
    @Override
    public String Name_For_Container() {
        return "BaseCafe";
    }

    /**
     * Name of the Soul, used in GUI
     * @return Name of the Soul returned as a Component
     * @see Component
     */
    @Override
    public Component Name() {
        return dess("<color:#F9EBDE>Cafe</color>");
    }

    /**
     * Description of the Soul, used in GUI
     * @return Description of the Soul returned as a Component
     * @see Component
     */
    @Override
    public Component Description() {
        return dess("What is it but coffee?");
    }

    /**
     * Icon of the Soul, used in GUI
     * @return ItemStack of the Soul
     * @see ItemStack
     */
    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.YELLOW_DYE);
    }

    /**
     * Ability One Name, used in GUI
     * @return Component of the Ability One Name
     * @see Component
     */
    @Override
    public Component AbilityOneName() {
        return dess("<color:#F9EBDE>Ability One: Selective TP</color>");
    }

    /**
     * Ability One Description, used in GUI
     * @return Component of the Ability One Description
     * @see Component
     */
    @Override
    public Component AbilityOneDescription() {
        return dess("You can selectively swap with the Cafe you are bonded with");
    }

    /**
     * Creates a new BaseMocha instance to recycle passives
     * @see BaseMocha
     */
    BaseMocha mocha = new BaseMocha();

    /**
     * Ability One - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability One for
     */
    @Override
 public void AbilityOne(Player player) {
        AbilityOne(player, false);
    }

    /**
     * Ability One - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability One for
     * @param is_imbue checking if it's an imbued action
     * @see xyz.yaszu.freedom.Subsystems.SoulImbueManager
     */
    @Override
 public void AbilityOne(Player player, boolean is_imbue) {
        mocha.AbilityOne(player);
    }

    /**
     * Item that is required to use this ability or Just an Icon
     * @return ItemStack used in ability usage
     */
    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.CLOCK);
    }

    /**
     * Ability Two Name used in GUI
     * @return Component of the Ability Two Name
     * @see Component
     */
    @Override
    public Component AbilityTwoName() {
        return dess("Ability Two: <color:#F9EBDE> AOE </color>");
    }

    /**
     * Ability Two Description used in GUI
     * @return Component of the Ability Two Description
     * @see Component
     */
    @Override
    public Component AbilityTwoDescription() {
        return dess("Damage people within a 5 block radius with an AOE attack");
    }

    /**
     * Ability Two - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability Two for
     * @param ability_item ItemStack used in ability usage
     * @throws MineSkinException
     * @throws DataRequestException
     */
    @Override
 public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        AbilityTwo(player, ability_item, false);
    }

    /**
     * Ability Two - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability Two for
     * @param ability_item ItemStack used in ability usage
     * @param is_imbue checking if it's imbued
     * @throws MineSkinException
     * @throws DataRequestException
     * Gets nearby players and damages them with a damage amount based on distance from player "caster"
     */
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

    /**
     * Description of the Passive, used in GUI
      * @return Component of the Passive Description
     * @see Component
     */
    @Override
    public Component Passive_Description() {
        return dess("Shares your potion effects with your bonded half");
    }

    /**
     * Passive - A passive that is active no matter what
     * @param player Player to handle passive for
     * @param event Event that triggered the passive
     *
     */
    @Override
    public void Passive(Player player, Object event) {
        mocha.Passive(player,event);
    }

    /**
     * Active Passive Description
     * @return Component used in UI
     * @see Component
     */
    @Override
    public Component ActivePassive_Description() {
        return dess("Chose those to bond with");
    }

    /**
     * Ability Two Cooldown Time
     * @return Cooldown time in milliseconds
     */
    @Override
    public long AbilityTwo_Cooldown() {
        return 3000;
    }

    /**
     * Ability One Cooldown Time
     * @param obj Given object, depends on the moveset, could be a player
     * @return Cooldown time in milliseconds
     */
    @Override
    public long AbilityOne_Cooldown(Object obj) {
        return 0;
    }

    /**
     * Active Passive - A passive that requires a condition to activate (like sneaking or specific stats)
     * @param player Player to handle active passive for
     */
    @Override
    public void ActivePassive(Player player) {
        mocha.ActivePassive(player);
    }
}


