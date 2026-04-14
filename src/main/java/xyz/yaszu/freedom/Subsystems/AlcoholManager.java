package xyz.yaszu.freedom.Subsystems;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class AlcoholManager extends Util implements Listener {

    public static int minSeconds = 4;
    public static int maxSeconds = 120;


    /**
     * Adds alcohol to a player.
     *
     * @param player    Player to add alcohol.
     * @param saturation     Saturation how much saturation to add.
     * @param foodLevel    Food to add. Food level should be under 5.
     * @param alcoholpotency     Alcohol Potency.
     */
    public static void addAlcohol(Player player,int saturation,int foodLevel,int alcoholpotency) {
        player.setSaturation(player.getSaturation()+saturation);
        player.setFoodLevel(player.getFoodLevel()+foodLevel);
        player.getPersistentDataContainer().set(FreedomKeys.alcohol(), PersistentDataType.INTEGER,alcoholpotency);
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    int alcoholvalue = player.getPersistentDataContainer().get(FreedomKeys.alcohol(), PersistentDataType.INTEGER);
                    if (alcoholvalue-1 < 1) {
                        player.getPersistentDataContainer().remove(FreedomKeys.alcohol());
                        return;
                    }
                    player.getPersistentDataContainer().set(FreedomKeys.alcohol(), PersistentDataType.INTEGER,alcoholvalue-1);
                } catch (Exception ignored) {}

            }
        }.runTaskLater(Freedom.get_plugin(),random.nextInt(minSeconds * 20, maxSeconds * 20));
    }

    @EventHandler
    public void playerdrunkenmove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(FreedomKeys.alcohol())) {
            int alcohollevel = 0;
            try {
                alcohollevel = player.getPersistentDataContainer().get(FreedomKeys.alcohol(), PersistentDataType.INTEGER);
            }catch (Exception ignored) {}
            switch (alcohollevel) {
                case 1 -> {
                    //sway with value of 0.1
                    Location loc = event.getTo();
                    event.setTo(loc.add(loc.clone().multiply(0.1)));
                }
                case 2 -> {
                    //sway with value of 0.2
                    Location loc = event.getTo();
                    event.setTo(loc.add(loc.clone().multiply(0.2)));
                }
                case 3 -> {
                    //sway with value of 0.3
                    Location loc = event.getTo();
                    event.setTo(loc.add(loc.clone().multiply(0.3)));

                }
                case 4 -> {
                    Location loc = event.getTo();
                    event.setTo(loc.add(loc.clone().multiply(0.4)));
                    player.addPotionEffect(PotionEffectType.NAUSEA.createEffect(20,0));
                    //sway with value of 0.4
                    //nausea 1
                    //drunken fist
                }
                case 5 -> {
                    Location loc = event.getTo();
                    event.setTo(loc.add(loc.clone().multiply(0.5)));
                    player.addPotionEffect(PotionEffectType.NAUSEA.createEffect(20,1));
                    //sway with value of 0.5
                    //nausea 2
                    //stronger drunken fist
                }

            }
        }

    }

    @EventHandler
    public void PrePlayerAttackEvent(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(FreedomKeys.alcohol())) {
            try {
                int alcohollevel = player.getPersistentDataContainer().get(FreedomKeys.alcohol(), PersistentDataType.INTEGER);
                if (alcohollevel >= 4) {
                    if (event.getPlayer().getInventory().getItemInMainHand() == null
                            &&
                        event.getPlayer().getInventory().getItemInOffHand() == null) {
                        player.getAttribute(Attribute.ATTACK_DAMAGE).removeModifier(FreedomKeys.alcohol());
                        player.getAttribute(Attribute.ATTACK_DAMAGE).addModifier(new AttributeModifier(FreedomKeys.alcohol(),8 + (alcohollevel*2), AttributeModifier.Operation.ADD_NUMBER));

                    } else {
                        player.getAttribute(Attribute.ATTACK_DAMAGE).removeModifier(FreedomKeys.alcohol());
                    }

                }
            }catch (Exception ignored) {}
        }
    }

}
