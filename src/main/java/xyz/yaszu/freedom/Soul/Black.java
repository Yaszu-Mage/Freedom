package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.Audiences;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

public class Black extends Util implements Base_Soul{

    @Override
    public String Name_For_Container() {
        return "Black";
    }

    @Override
    public Component Name() {
        return dess("<shadow:#000000FF><b><yellow><gradient:#0f000f:#555555:#aa00aa>Black</gradient>");
    }

    @Override
    public Component Description() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.WOODEN_SHOVEL);
    }

    @Override
    public Component AbilityOneName() {
        return dess("Trickster");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("Teleport to a saved location, it will take you 5 seconds.");
    }

    @Override
    public void AbilityOne(Player player) {
        if (!player.getPersistentDataContainer().has(keygen("black_save"), PersistentDataType.BOOLEAN)) {
            save(player);
            return;
        }
        if (player.getPersistentDataContainer().get(keygen("black_save"), PersistentDataType.BOOLEAN)) {
            load(player);
        }
    }

    public void load(Player player) {
        //VFX
        new BukkitRunnable() {

            @Override
            public void run() {
                if (!player.getPersistentDataContainer().has(keygen("blacksave"))) {
                    this.cancel();
                }
                Location loadingLocation = new Location(
                        Bukkit.getWorld(player.getPersistentDataContainer().get(keygen("blackworld"),PersistentDataType.STRING)),
                        player.getPersistentDataContainer().get(keygen("blacksaveX"),PersistentDataType.DOUBLE),
                        player.getPersistentDataContainer().get(keygen("blacksaveY"),PersistentDataType.DOUBLE),
                        player.getPersistentDataContainer().get(keygen("blacksaveZ"),PersistentDataType.DOUBLE)

                );
                drawCircle(player.getLocation(),1,player.getWorld(),16,Particle.SMOKE);
                drawCircle(loadingLocation,1,player.getWorld(),16,Particle.SMOKE);
            }
        }.runTaskTimer(Freedom.get_plugin(),20,20);
        new BukkitRunnable() {
            public int tick = 0;
            public double last_health = player.getHealth();
            @Override
            public void run() {
                Location loadingLocation = new Location(
                    Bukkit.getWorld(player.getPersistentDataContainer().get(keygen("blackworld"),PersistentDataType.STRING)),
                    player.getPersistentDataContainer().get(keygen("blacksaveX"),PersistentDataType.DOUBLE),
                    player.getPersistentDataContainer().get(keygen("blacksaveY"),PersistentDataType.DOUBLE),
                    player.getPersistentDataContainer().get(keygen("blacksaveZ"),PersistentDataType.DOUBLE)
                );
                if (player.getHealth() > last_health || !player.isSneaking() || player.isDead()) {
                    this.cancel();
                    player.sendActionBar(dess("Teleport Cancelled."));
                }
                if (tick >= 4) {
                    drawCircle(player.getLocation(),1,player.getWorld(),32,Particle.GUST);
                    player.playSound(player.getLocation(),Sound.ENTITY_WIND_CHARGE_WIND_BURST,1,1);
                    player.teleport(loadingLocation);
                    drawCircle(loadingLocation,1,loadingLocation.getWorld(),32,Particle.REVERSE_PORTAL);
                    player.playSound(player.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1,1);
                    this.cancel();
                } else {
                    if (threes(tick) == 3) {
                        player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds..."));
                    } else if (threes(tick) == 2) {
                        player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds.."));
                    } else {
                        player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds."));
                    }
                    drawCircle(player.getLocation(),1.5,player.getWorld(),32,Particle.SOUL_FIRE_FLAME);
                    drawCircle(player.getLocation(),1,player.getWorld(),32,Particle.VAULT_CONNECTION);
                    drawCircle(loadingLocation,1,player.getWorld(),32,Particle.VAULT_CONNECTION);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO,SoundCategory.PLAYERS,1,tick);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,SoundCategory.PLAYERS,1,tick-1);
                }
                tick = tick + 1;
            }
            @Override
            public synchronized void cancel() throws IllegalStateException {
                player.getPersistentDataContainer().remove(keygen("black_save"));
                player.getPersistentDataContainer().remove(keygen("blacksaveX"));
                player.getPersistentDataContainer().remove(keygen("blacksaveY"));
                player.getPersistentDataContainer().remove(keygen("blacksaveZ"));
                player.getPersistentDataContainer().remove(keygen("blackworld"));
                Bukkit.getScheduler().cancelTask(getTaskId());
            }
        }.runTaskTimer(Freedom.get_plugin(),0,20);


    }
    public static double threes(int num) {
        if (num % 3 == 0) {
            return 3;
        }
        if (num % 2 == 0) {
            return 2;
        }
        return 1;
    }

    public static boolean isMultipleofTwenty(int num) {
        // The condition (num % 10 == 0) is true if the remainder is 0.
        return (num % 20 == 0);
    }
    public void save(Player player) {
        player.getPersistentDataContainer().set(keygen("black_save"), PersistentDataType.BOOLEAN, true);
        player.getPersistentDataContainer().set(keygen("blacksaveX"), PersistentDataType.DOUBLE, player.getLocation().getX());
        player.getPersistentDataContainer().set(keygen("blacksaveY"), PersistentDataType.DOUBLE, player.getLocation().getY());
        player.getPersistentDataContainer().set(keygen("blacksaveZ"), PersistentDataType.DOUBLE, player.getLocation().getZ());
        player.getPersistentDataContainer().set(keygen("blackworld"), PersistentDataType.STRING, player.getWorld().getName());
        player.sendActionBar(dess("Saved Location at (" + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + ")"));
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick > 8) {
                    this.cancel();
                }
                if (tick == 0) {
                    player.getWorld().playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_GUITAR,1,1 + Math.min((tick/10),0));
                }
                if (tick % 5 == 0) {
                    player.getWorld().playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_GUITAR,1,1 + Math.min((tick/10),0));
                }

                tick = tick + 1;
            }
        }.runTaskTimer(Freedom.get_plugin(),0,1);
    }
    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.ACACIA_HANGING_SIGN);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) {

    }

    @Override
    public Component Passive_Description() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void Passive(Player player, Object event) {

    }

    @Override
    public Component ActivePassive_Description() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void ActivePassive(Player player) {

    }
}
