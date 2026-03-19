package xyz.yaszu.freedom.Soul;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.bossbar.BossBar;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base.*;
import xyz.yaszu.freedom.Soul.Ultra.*;
import xyz.yaszu.freedom.Util.Util;

import java.util.Objects;

public class soulListener extends Util implements Listener {
    public static Red red = new Red();
    public static Yellow yellow =  new Yellow();
    public static Green green = new Green();
    public static Black black = new Black();
    public static Purple purple = new Purple();
    public static Blue blue = new Blue();
    public static Orange orange = new Orange();
    public static BaseRed basered = new BaseRed();
    public static BaseYellow baseyellow =  new BaseYellow();
    public static BaseGreen basegreen = new BaseGreen();
    public static BaseBlack baseblack = new BaseBlack();
    public static BasePurple basepurple = new BasePurple();
    public static BaseBlue baseblue = new BaseBlue();
    public static BaseOrange baseorange = new BaseOrange();
    public void Passive(Player player) {
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        switch (soulType) {
            case Orange:
                orange.Passive(player,null);
                break;
            case BaseOrange:
                baseorange.Passive(player,null);
                break;
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setResourcePack("https://www.dropbox.com/scl/fo/h91az0ps0s36ovw1u12wh/AP97wFsQ1gIsBwMdoeu5CLU?rlkey=5unbjew1tualfdv84l94m7yte&st=uan1lu5e&dl=1");
        if (!player.getPersistentDataContainer().has(keygen("ComorAction"))) {
            player.getPersistentDataContainer().set(keygen("ComorAction"), PersistentDataType.BOOLEAN, true);
        }
        if (!player.getPersistentDataContainer().has(keygen("SoulPoint"))) {
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, 0.0);
            return;
        }
        if (player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE) > 10) {
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, 10.0);
        }


    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getPersistentDataContainer().get(keygen("SoulPoint"),PersistentDataType.DOUBLE) < 10 && !player.isInsideVehicle()) {
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE) + 1);
            showSoulPoints(player);
        }
        }
    }




    @EventHandler
    public void onPlayerDamagedEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (player.getPersistentDataContainer().get(keygen("SoulPoint"),PersistentDataType.DOUBLE) < 10 && !player.isInsideVehicle()) {
                player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE) + 1);
                showSoulPoints(player);
            }
        }
    }



    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
            if (player.getPersistentDataContainer().has(keygen("soul"))) {
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
            } else {
                player.performCommand("openGui");
                player.setWalkSpeed(0);
                player.setFlySpeed(0);
        }
        if (player.getPersistentDataContainer().has(keygen("cursed"))) {
            if (Objects.equals(player.getPersistentDataContainer().get(keygen("cursed"), PersistentDataType.STRING), "Frog")) {
                Orange.recurse(player);
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,PotionEffect.INFINITE_DURATION,1,true,false));
            }
        }
    }





    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        showSoulPoints(event.getPlayer());
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().get(keygen("soul"),PersistentDataType.STRING) == "Green") {

        }
    }

    public static void showSoulPoints(Player player) {
        if (!player.getPersistentDataContainer().has(keygen("SoulPoint"))) {
            player.getPersistentDataContainer().set(keygen("SoulPoint"),PersistentDataType.DOUBLE,0d);
        }
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        for (BossBar bossBar : player.activeBossBars() ) {
            bossBar.name().toString().contains("SoulPoints");
            player.hideBossBar(bossBar);
        }

        player.showBossBar(BossBar.bossBar(dess("SoulPoints"), (float) SoulPoints/10, BossBar.Color.BLUE, BossBar.Overlay.NOTCHED_10));
    }


    @EventHandler
    public void enableActivePassives(PlayerArmSwingEvent event){

        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().get(keygen("ComorAction"), PersistentDataType.BOOLEAN)) {


        if (!player.getPersistentDataContainer().has(keygen("soul")) || player.isSneaking() == false && event.getAnimationType() != PlayerAnimationType.ARM_SWING || !player.getPersistentDataContainer().has(keygen("SoulPoint"))) {
            return;
        }

        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);

        switch (soulType) {
            case Red:
                if (SoulPoints >= 5 && player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE) == false) {

                    red.ActivePassive(player);
                }
                break;
            case Black:
                if (SoulPoints >= 5 && player.isSneaking()) {
                    black.playerSneakEvent(player);
                }
            case Green:
                if (SoulPoints >= 5 && player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE) == false) {

                    green.ActivePassive(player);
                }
                break;
            case Blue:
                blue.ActivePassive(player);
                break;
            case Yellow:
                yellow.ActivePassive(player);
                break;
            case Orange:
                orange.ActivePassive(player);
                break;
            case BaseRed:
                if (SoulPoints >= 5 && player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE) == false) {

                    basered.ActivePassive(player);
                }
                break;
            case BaseBlack:
                if (SoulPoints >= 5 && player.isSneaking()) {
                    baseblack.playerSneakEvent(player);
                }
            case BaseGreen:
                if (SoulPoints >= 5 && player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE) == false) {

                    basegreen.ActivePassive(player);
                }
                break;
            case BaseBlue:
                baseblue.ActivePassive(player);
                break;
            case BaseYellow:
                baseyellow.ActivePassive(player);
                break;
            case BaseOrange:
                baseorange.ActivePassive(player);
                break;
        }
    }
    }

    public void AbilityOne(Player player) {
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        switch (soulType) {
            case Red:
                red.AbilityOne(player);
                break;
            case Purple:
                purple.AbilityOne(player);
                break;
            case Green:
                green.AbilityOne(player);
                break;
            case Black:
                black.AbilityOne(player);
                break;
            case Blue, Yellow:
                blue.AbilityOne(player);
                break;
            case Orange:
                orange.AbilityOne(player);
                break;
            case BaseRed:
                basered.AbilityOne(player);
                break;
            case BasePurple:
                basepurple.AbilityOne(player);
                break;
            case BaseGreen:
                basegreen.AbilityOne(player);
                break;
            case BaseBlack:
                baseblack.AbilityOne(player);
                break;
            case BaseBlue, BaseYellow:
                baseblue.AbilityOne(player);
                break;
            case BaseOrange:
                baseorange.AbilityOne(player);
                break;
        }
    }
    @EventHandler
    public void joinAndHeal(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        Freedom.get_plugin().getLogger().info(soulType.toString());
        switch (soulType) {
            case Green -> green.Passive(player,event);
            case BaseGreen -> basegreen.Passive(player,event);
        }
    }

    public void AbilityTwo(Player player) throws MineSkinException, DataRequestException {
        ItemStack drop = player.getInventory().getItemInMainHand();
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        player.sendActionBar(dess("<green>Ability Two</green>"));
        Freedom.get_plugin().getLogger().info(soulType.toString());
        switch (soulType) {
            case Red:
                if (drop.getPersistentDataContainer().has(keygen("timepiece"))) {
                    //Do stuff
                    red.AbilityTwo(player,drop);

                }
                break;
            case Purple:
                if (drop.getPersistentDataContainer().has(keygen("rifle"))) {
                    purple.AbilityTwo(player,drop);
                }
                break;
            case Black:
                black.AbilityTwo(player,player.getInventory().getItem(0));
                break;
            case Blue:
                Freedom.get_plugin().getLogger().info("Baller");
                blue.AbilityTwo(player,player.getInventory().getItem(0));
                break;
            case Green:
                green.AbilityTwo(player,player.getInventory().getItem(0));
                break;
            case Yellow:
                yellow.AbilityTwo(player,player.getInventory().getItem(0));
                break;
            case Orange:
                orange.AbilityTwo(player,player.getInventory().getItem(0));
                break;
            case BaseRed:
                if (drop.getPersistentDataContainer().has(keygen("timepiece"))) {
                    //Do stuff
                    basered.AbilityTwo(player,drop);

                }
                break;
            case BasePurple:
                if (drop.getPersistentDataContainer().has(keygen("rifle"))) {
                    basepurple.AbilityTwo(player,drop);
                }
                break;
            case BaseBlack:
                baseblack.AbilityTwo(player,player.getInventory().getItem(0));
                break;
            case BaseBlue:
                Freedom.get_plugin().getLogger().info("Baller");
                baseblue.AbilityTwo(player,player.getInventory().getItem(0));
                break;
            case BaseGreen:
                basegreen.AbilityTwo(player,player.getInventory().getItem(0));
                break;
            case BaseYellow:
                baseyellow.AbilityTwo(player,player.getInventory().getItem(0));
                break;
            case BaseOrange:
                baseorange.AbilityTwo(player,player.getInventory().getItem(0));
                break;
        }
    }

    public void ActivePassive(Player player) {
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        switch (soulType) {
            case Red:
                red.ActivePassive(player);
            case Purple:
                purple.ActivePassive(player);
            case Black:
                double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
                if (SoulPoints >= 5 && player.isSneaking()) {
                    black.playerSneakEvent(player);

                }
                break;
            case Blue:
                blue.ActivePassive(player);
                break;
            case Yellow:
                yellow.ActivePassive(player);
                break;
            case Orange:
                orange.ActivePassive(player);
                break;
            case BaseRed:
                basered.ActivePassive(player);
                break;
            case BasePurple:
                basepurple.ActivePassive(player);
                break;
            case BaseBlack:
                double SoulPoints2 = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
                if (SoulPoints2 >= 5 && player.isSneaking()) {
                    black.playerSneakEvent(player);

                }
                break;
            case BaseBlue:
                baseblue.ActivePassive(player);
                break;
            case BaseYellow:
                baseyellow.ActivePassive(player);
                break;
            case BaseOrange:
                baseorange.ActivePassive(player);
                break;
        }
    }



    @EventHandler
    public void onPlayerXPgain(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().get(keygen("soul"),PersistentDataType.STRING) == "Purple") {
            event.setAmount(event.getAmount() * 4);

        }
        if (player.getPersistentDataContainer().get(keygen("soul"),PersistentDataType.STRING) == "BasePurple") {
            event.setAmount(event.getAmount() * 2);

        }
    }

    @EventHandler
    public void AbilityOneListener(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().get(keygen("ComorAction"),PersistentDataType.BOOLEAN)) {


            if (!player.getPersistentDataContainer().has(keygen("soul"))) {
                return;
            }
            SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
            switch (soulType) {
                case Red:
                    if (player.isSneaking()) {
                        red.AbilityOne(player);
                    }
                    break;
                case Purple:
                    if (player.isSneaking()) {
                        purple.AbilityOne(player);
                    }
                    break;
                case Green:
                    if (player.isSneaking()) {
                        green.AbilityOne(player);
                    }
                    break;
                case Black:
                    if (player.isSneaking()) {
                        black.AbilityOne(player);
                    }
                    break;
                case Blue, Yellow:
                    if (player.isSneaking()) {
                        blue.AbilityOne(player);
                    }
                    break;
                case Orange:
                    orange.AbilityOne(player);
                    break;
                case BaseRed:
                    if (player.isSneaking()) {
                        basered.AbilityOne(player);
                    }
                    break;
                case BasePurple:
                    if (player.isSneaking()) {
                        basepurple.AbilityOne(player);
                    }
                    break;
                case BaseGreen:
                    if (player.isSneaking()) {
                        basegreen.AbilityOne(player);
                    }
                    break;
                case BaseBlack:
                    if (player.isSneaking()) {
                        baseblack.AbilityOne(player);
                    }
                    break;
                case BaseBlue, BaseYellow:
                    if (player.isSneaking()) {
                        baseblue.AbilityOne(player);
                    }
                    break;
                case BaseOrange:
                    baseorange.AbilityOne(player);
                    break;
            }
        }
    }


    @EventHandler
    public void AbilityTwoListener(PlayerDropItemEvent event) throws MineSkinException, DataRequestException {
        Player player = event.getPlayer();
        ItemStack drop = event.getItemDrop().getItemStack();
        if (player.getPersistentDataContainer().get(keygen("ComorAction"),PersistentDataType.BOOLEAN)) {
        if (!player.getPersistentDataContainer().has(keygen("soul"))) {
            return;
        }
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));

        switch (soulType) {
            case Red:
                if (drop.getPersistentDataContainer().has(keygen("timepiece"))) {
                        //Do stuff
                        red.AbilityTwo(player,drop);
                        event.setCancelled(true);

                }
                break;
            case Purple:
                if (drop.getPersistentDataContainer().has(keygen("rifle"))) {
                    purple.AbilityTwo(player,drop);
                    event.setCancelled(true);
                }
                break;
            case Green:
                green.AbilityTwo(player,player.getInventory().getItem(0));
                break;
            case Yellow:
                yellow.AbilityTwo(player,player.getInventory().getItem(0));
                break;
        }
    }
    }

@EventHandler
    public void activateAttackPassive(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        if (!player.getPersistentDataContainer().has(keygen("soul"))) {
            return;
        }
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        switch (soulType) {
            case Red:
                red.Passive(player,event);
                break;
            case BaseRed:
                basered.Passive(player,event);
                break;
        }

}



}
