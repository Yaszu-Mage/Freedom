package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.Audiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
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
        return null;
    }

    @Override
    public ItemStack Icon() {
        return null;
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
        Location loadingLocation = new Location(Bukkit.getWorld(player.getPersistentDataContainer().get(keygen("blackworld"),PersistentDataType.STRING)),player.getPersistentDataContainer().get(keygen("blacksaveX"),PersistentDataType.DOUBLE),player.getPersistentDataContainer().get(keygen("blacksaveY"),PersistentDataType.DOUBLE),player.getPersistentDataContainer().get(keygen("blacksaveZ"),PersistentDataType.DOUBLE));
        new BukkitRunnable() {
            public int tick = 0;
            public double last_health = player.getHealth();
            @Override
            public void run() {
                if (player.getHealth() > last_health || !player.isSneaking() || player.isDead()) {
                    this.cancel();
                    player.sendActionBar(dess("Teleport Cancelled."));
                }
                if (tick >= 80) {
                    player.teleport(loadingLocation);
                    this.cancel();
                } else {
                    if (threes(tick) == 3) {
                        player.sendActionBar(dess("Teleporting in" + tick/20 + " seconds..."));
                    } else if (threes(tick) == 2) {
                        player.sendActionBar(dess("Teleporting in" + tick/20 + " seconds.."));
                    } else {
                        player.sendActionBar(dess("Teleporting in" + tick/20 + " seconds."));
                    }
                    drawCircle(player.getLocation(),1,player.getWorld(),16,Particle.SMOKE);
                    drawCircle(loadingLocation,1,player.getWorld(),16,Particle.SMOKE);
                    if (isMultipleofTwenty(tick)) {
                        for (double iteration = 0; iteration < player.getLocation().distance(player.getEyeLocation()); iteration = iteration + 0.1) {
                            drawCircle(player.getLocation().add(player.getLocation().add(0,iteration,0)),1,player.getWorld(),16,Particle.SMOKE);
                            drawCircle(loadingLocation.add(loadingLocation.add(0,iteration,0)),1,player.getWorld(),16,Particle.SMOKE);
                        }
                    }
                }
                tick++;
            }
            @Override
            public synchronized void cancel() throws IllegalStateException {
                player.getPersistentDataContainer().remove(keygen("black_save"));
                Bukkit.getScheduler().cancelTask(getTaskId());
            }
        }.runTaskTimer(Freedom.get_plugin(),0,1);


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
        player.sendActionBar(dess("Saved Location at (" + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ()) + ")");
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
    public void AbilityTwo(Player player, ItemStack ability_item) {

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
    public void ActivePassive(Player player) {

    }
}
