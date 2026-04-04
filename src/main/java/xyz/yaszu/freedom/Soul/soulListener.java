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
import xyz.yaszu.freedom.Subsystems.CurseManager;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class soulListener extends Util implements Listener {
    public static final Map<SoulTypes, Base_Soul> SOULS = new EnumMap<>(SoulTypes.class);

    public static void registerSouls() {
        SOULS.put(SoulTypes.Red, new Red());
        SOULS.put(SoulTypes.Yellow, new Yellow());
        SOULS.put(SoulTypes.Green, new Green());
        SOULS.put(SoulTypes.Black, new Black());
        SOULS.put(SoulTypes.Purple, new Purple());
        SOULS.put(SoulTypes.Blue, new Blue());
        SOULS.put(SoulTypes.Orange, new Orange());
        SOULS.put(SoulTypes.BaseRed, new BaseRed());
        SOULS.put(SoulTypes.BaseYellow, new BaseYellow());
        SOULS.put(SoulTypes.BaseGreen, new BaseGreen());
        SOULS.put(SoulTypes.BaseBlack, new BaseBlack());
        SOULS.put(SoulTypes.BasePurple, new BasePurple());
        SOULS.put(SoulTypes.BaseBlue, new BaseBlue());
        SOULS.put(SoulTypes.BaseOrange, new BaseOrange());
    }

    private Base_Soul getSoul(Player player) {
        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
        if (soulName == null) return null;
        try {
            return SOULS.get(SoulTypes.valueOf(soulName));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void Passive(Player player) {
        if (!Life_and_Death.is_alive(player)) return;
        Base_Soul soul = getSoul(player);
        if (soul != null) {
            soul.Passive(player, null);
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
        if (!player.getPersistentDataContainer().has(keygen("ComorAction"))) {
            player.getPersistentDataContainer().set(keygen("ComorAction"), PersistentDataType.BOOLEAN, true);
        }
    }





    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Integer life = event.getPlayer().getPersistentDataContainer().get(keygen("life"),PersistentDataType.INTEGER);
        event.getPlayer().sendActionBar(dess("<green>Life</green> " + life+ "/9"));
        showSoulPoints(event.getPlayer());
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
    public void enableActivePassives(PlayerArmSwingEvent event) {
        if (!Life_and_Death.is_alive(event.getPlayer())) return;
        Player player = event.getPlayer();
        if (!player.getPersistentDataContainer().getOrDefault(FreedomKeys.comorAction(), PersistentDataType.BOOLEAN, true)) {
            return;
        }
        if (!player.getPersistentDataContainer().has(FreedomKeys.soul()) ||
                (!player.isSneaking()) ||
                !player.getPersistentDataContainer().has(FreedomKeys.soulPoint())) {
            return;
        }

        Base_Soul soul = getSoul(player);
        if (soul == null) return;

        double soulPoints = player.getPersistentDataContainer().getOrDefault(FreedomKeys.soulPoint(), PersistentDataType.DOUBLE, 0.0);

        // Special handling for some souls in enableActivePassives
        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
        if (soulName != null) {
            if (soulName.contains("Red") || soulName.contains("Green")) {
                if (soulPoints >= 5 && !player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                    soul.ActivePassive(player);
                }
                return;
            } else if (soulName.contains("Black")) {
                if (soulPoints >= 5 && player.isSneaking()) {
                    soul.playerSneakEvent(player);
                }
                return;
            }
        }

        // Default behavior for other souls
        soul.ActivePassive(player);
    }

    public void AbilityOne(Player player) {
        Base_Soul soul = getSoul(player);
        if (soul != null) {
            soul.AbilityOne(player);
        }
    }

    @EventHandler
    public void joinAndHeal(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Base_Soul soul = getSoul(player);
        if (soul != null) {
            String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
            if (soulName != null && soulName.contains("Green")) {
                soul.Passive(player, event);
            }
        }
    }

    public void AbilityTwo(Player player) throws MineSkinException, DataRequestException {
        ItemStack drop = player.getInventory().getItemInMainHand();
        if (!Life_and_Death.is_alive(player)) return;
        Base_Soul soul = getSoul(player);
        if (soul == null) return;

        player.sendActionBar(dess("<green>Ability Two</green>"));

        String soulName = soul.Name_For_Container();
        if (soulName != null) {
            if (soulName.contains("Red")) {
                if (drop.getPersistentDataContainer().has(keygen("timepiece"))) {
                    player.sendMessage(dess("ABILITY"));
                    soul.AbilityTwo(player, drop);
                }

                return;
            } else if (soulName.contains("Purple")) {
                if (drop.getPersistentDataContainer().has(keygen("rifle"))) {
                    soul.AbilityTwo(player, drop);
                }
                return;
            }
        }

        // For others, use first item slot as per original logic
        soul.AbilityTwo(player, player.getInventory().getItem(0));
    }
    public void ActivePassive(Player player) {
        Base_Soul soul = getSoul(player);
        if (soul == null) return;
        if (!Life_and_Death.is_alive(player)) return;
        double soulPoints = player.getPersistentDataContainer().getOrDefault(FreedomKeys.soulPoint(), PersistentDataType.DOUBLE, 0.0);
        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);

        if (soulName != null && soulName.contains("Black")) {
            if (soulPoints >= 5 && player.isSneaking()) {
                soul.playerSneakEvent(player);
            }
        } else {
            soul.ActivePassive(player);
        }
    }

    @EventHandler
    public void onPlayerXPgain(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
        if ("Purple".equals(soulName)) {
            event.setAmount(event.getAmount() * 4);
        } else if ("BasePurple".equals(soulName)) {
            event.setAmount(event.getAmount() * 2);
        }
    }

    @EventHandler
    public void AbilityOneListener(PlayerJumpEvent event) {
        if (!Life_and_Death.is_alive(event.getPlayer())) return;
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().getOrDefault(FreedomKeys.comorAction(), PersistentDataType.BOOLEAN, true)) {
            Base_Soul soul = getSoul(player);
            if (soul == null) return;

            String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
            if (soulName != null && soulName.contains("Orange")) {
                soul.AbilityOne(player);
            } else if (player.isSneaking()) {
                soul.AbilityOne(player);
            }
        }
    }

    @EventHandler
    public void AbilityTwoListener(PlayerDropItemEvent event) throws MineSkinException, DataRequestException {
        Player player = event.getPlayer();
        ItemStack drop = event.getItemDrop().getItemStack();
        if (!Life_and_Death.is_alive(event.getPlayer())) return;
        if (player.getPersistentDataContainer().getOrDefault(FreedomKeys.comorAction(), PersistentDataType.BOOLEAN, true)) {
            Base_Soul soul = getSoul(player);
            if (soul == null) return;

            String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
            if (soulName != null) {
                if (soulName.contains("Red") && drop.getPersistentDataContainer().has(keygen("timepiece"))) {
                    soul.AbilityTwo(player, drop);
                    event.setCancelled(true);
                    return;
                } else if (soulName.contains("Purple") && drop.getPersistentDataContainer().has(keygen("rifle"))) {
                    soul.AbilityTwo(player, drop);
                    event.setCancelled(true);
                    return;
                }
            }

            soul.AbilityTwo(player, player.getInventory().getItem(0));
        }
    }

    @EventHandler
    public void activateAttackPassive(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        if (!Life_and_Death.is_alive(event.getPlayer())) return;
        Base_Soul soul = getSoul(player);
        if (soul == null) return;

        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
        if (soulName != null && soulName.contains("Red")) {
            soul.Passive(player, event);
        }
    }
}
