package xyz.yaszu.freedom.Soul.Base;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

public class BaseCafe extends Util implements Base_Soul {
    @Override
    public String Name_For_Container() {
        return "BaseYellow";
    }

    @Override
    public Component Name() {
        return dess("<Yellow>Yellow</Yellow>");
    }

    @Override
    public Component Description() {
        return dess("What is it but time?");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.YELLOW_DYE);
    }

    @Override
    public Component AbilityOneName() {
        return dess("<Yellow>Selective TP</Yellow>");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("You can selectively swap with the Blue you are bonded with");
    }
    BaseMocha blue = new BaseMocha();
    @Override
    public void AbilityOne(Player player) {
        blue.AbilityOne(player);
    }

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.CLOCK);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("<yellow> AOE </yellow>");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("Damage people within a 5 block radius with an AOE attack");
    }
    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        Freedom.get_plugin().getLogger().info("Baller");
        if (can_ability(AbilityTwo_Cooldown(), abilityTwoCooldowns,player.getUniqueId())) {
            Freedom.get_plugin().getLogger().info("Baller");
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
                                Freedom.get_plugin().getLogger().info("inst");
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
                    drawCircle(player.getLocation(),1,player.getLocation().getWorld(),16, Particle.LANDING_HONEY);
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
            // TODO no no ability + time
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
    public long AbilityOne_Cooldown() {
        return 0;
    }

    @Override
    public void ActivePassive(Player player) {
        blue.ActivePassive(player);
    }
}
