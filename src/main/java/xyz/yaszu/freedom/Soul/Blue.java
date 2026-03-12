package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class Blue extends Util implements Base_Soul {
    @Override
    public String Name_For_Container() {
        return "Blue";
    }

    @Override
    public Component Name() {
        return dess("<blue>Blue</blue>");
    }

    @Override
    public Component Description() {
        return dess("The reverse, to go backwards, that is your meaning");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.SHIELD);
    }

    @Override
    public Component AbilityOneName() {
        return dess("<blue> Ability One </blue> - Wait up!");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("Slow down people in a 10 block radius");
    }


    public long abilityOneCooldownTime = 8100;
    public HashMap<UUID, Long> abilityOneCooldown = new HashMap<>();
    @Override
    public void AbilityOne(Player player) {
        //The clocks... what?
        //Slows down people in a 10 block radius
        if (can_ability(abilityOneCooldownTime, abilityOneCooldown, player.getUniqueId())) {
            Location location = player.getLocation();
            location.getNearbyEntitiesByType(Player.class, 10).forEach(entity -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location location = entity.getLocation();
                        if (location.getNearbyEntitiesByType(Player.class, 10).contains(player) && player.isOnline()) {
                            //default movement speed attribute 0.1
                            //default gravity 0.08
                            entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.025);
                            entity.getAttribute(Attribute.GRAVITY).setBaseValue(0.01);
                        } else {
                            entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);
                            entity.getAttribute(Attribute.GRAVITY).setBaseValue(0.08);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(Freedom.get_plugin(), 0, 0);
            });
            abilityOneCooldown.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            player.sendActionBar(dess("You can't use this ability yet wait, " + (abilityOneCooldownTime - (System.currentTimeMillis() - abilityOneCooldown.get(player.getUniqueId()))) / 1000 + " seconds."));
        }
    }



    @Override
    public ItemStack Related_Item() {
        //TODO make clock blue
        return ItemStack.of(Material.CLOCK);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("<blue> Ability Two </blue> - Rewind");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) {
        new BukkitRunnable() {
            int tick = 0;
            ArrayList<velocityTime> velocityTimes = new ArrayList<>();
            @Override
            public void run() {
                if (tick >= 100) {
                    velocityTimes.add(new velocityTime(player.getVelocity().multiply(-1), 100));
                } else {
                    if (tick == 101) {
                        Collections.reverse(velocityTimes);
                    }
                    if (tick >= 200) {
                        this.cancel();
                        return;
                    }
                    player.setVelocity(velocityTimes.get(100-tick).vector);
                }
                tick = tick + 1;
            }
        }.runTaskTimer(Freedom.get_plugin(), 0,0);
    }

    public class velocityTime {

        public Vector vector;
        public double time;
        velocityTime(Vector Vector, long Time) {
           this.time = Time;
           this.vector = Vector;
        }
    }

    @Override
    public Component Passive_Description() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void Passive(Player player, Object event) {
        //idk bro
    }

    @Override
    public Component ActivePassive_Description() {
        return dess("You can grant yourself resistance for 9 Soul Points");
    }

    @Override
    public void ActivePassive(Player player) {
        player.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(2000,5));
    }
}
