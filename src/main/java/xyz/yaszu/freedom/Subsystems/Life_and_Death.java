package xyz.yaszu.freedom.Subsystems;


import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.ConfigManager;
import xyz.yaszu.freedom.Util.Util;
import xyz.yaszu.freedom.Util.player_util;


import java.util.Random;

import static xyz.yaszu.freedom.Freedom.clearPlayerPersistentData;
import static xyz.yaszu.freedom.Util.Util.*;


public class Life_and_Death implements org.bukkit.event.Listener{
    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        if (!player_util.does_player_have_tag(event.getPlayer(),"life")) {
            player_util.set_type_value(event.getPlayer(),"life",9, PersistentDataType.INTEGER);
        }
    }




    public static Util util = new Util();


    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event) {
        if ((Integer) player_util.get_type_value(event.getPlayer(), "life",PersistentDataType.INTEGER) <= 0) {
            return;
        }
        Player victim = event.getEntity();
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent damageByEntity) {
            Entity damager = damageByEntity.getDamager();

            if (damager instanceof Player killer) {
                player_util.set_type_value(
                        event.getPlayer(),
                        "life",
                        (Integer) player_util.get_type_value(event.getPlayer(), "life",PersistentDataType.INTEGER) - 1,
                        PersistentDataType.INTEGER
                );
            }
        }
        if ((Integer) player_util.get_type_value(event.getPlayer(), "life",PersistentDataType.INTEGER) <= 0) {
            player_util.set_type_value(event.getPlayer(),"ghost",true,PersistentDataType.BOOLEAN);
            event.getPlayer().setAllowFlight(true);
        }
        Player player = event.getPlayer();
    }



    @EventHandler
    public void Player_Join_Event(PlayerJoinEvent event) {
        if (event.getPlayer().getPersistentDataContainer().has(keygen("ghost"))) {
            for (Player player : Bukkit.getOnlinePlayers() ) {
                if (!player_util.does_player_have_tag(player,"ghost") && player != event.getPlayer() ) {

                    event.getPlayer().setAllowFlight(true);
                    player.hidePlayer(Bukkit.getPluginManager().getPlugin("Freedom"), event.getPlayer());
                } else {

                    player.showPlayer(Bukkit.getPluginManager().getPlugin("Freedom"), event.getPlayer());
                }
            }
        }
    }
    ConfigManager config = new ConfigManager(Bukkit.getPluginManager().getPlugin("Freedom").getConfig());
    @EventHandler
    public void Can_See_Ghost(PlayerMoveEvent event) {
        if (event.getPlayer().getPersistentDataContainer().has(keygen("ghost"))) {
            for (Player player : Bukkit.getOnlinePlayers() ) {
                if (!player_util.does_player_have_tag(player,"ghost") && player != event.getPlayer() ) {
                    player.hidePlayer(Bukkit.getPluginManager().getPlugin("Freedom"), event.getPlayer());
                } else {
                    player.showPlayer(Bukkit.getPluginManager().getPlugin("Freedom"), event.getPlayer());
                }
            }
        }
    }

    public static boolean is_alive(Player player) {
        return !player.getPersistentDataContainer().has(keygen("ghost"));
    }

    public static void revive_player(Player player,Location location){
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setFireTicks(0);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.teleport(location);
        clearPlayerPersistentData(player);
        player.getInventory().clear();
        Random random = new Random();
        int random_int = random.nextInt(1,4);
        switch (random_int) {
            case 1:
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<b><dark_red>It was all just a bad dream...</dark_red></b>"));
            case 2:
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<dark_red><b>Yet, time marches on, though from the start.</b></dark_red>"));
            case 3:

                player.sendActionBar(MiniMessage.miniMessage().deserialize("Your hear a voice whisper into your ear softly, <dark_red>\"Proceed.\"</dark_red>"));
            case 4:
                int rarer = random.nextInt(1,1000);
                int rarer2 = random.nextInt(1,2500);
                if (rarer2 == 2500) {
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("You found a glowing white substance on the ground before 'accidently' ingesting it... Oh yeah, you now have stage 4 brain cancer. Well, fictional Brain cancer atleast or was it dementia hell knows you just damn woken up again."));
                } else if (rarer == 500){
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("Hey, it's me again..."));
                }else {
                    int players = 0;
                    for (Player online_player : Bukkit.getOnlinePlayers()) {
                        players += 1;
                    }
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<dark_red>* "+ String.valueOf(players - 1) + " left.</dark_red>"));
                }
    }
    }


    @EventHandler
    public void Hostile_Target_Event(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() != null) {
            if (event.getTarget().getPersistentDataContainer().has(keygen("ghost"))) {
                event.setCancelled(true);
            }}
    }

    @EventHandler
    public void DamageEvent(EntityDamageEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(keygen("ghost"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void HungerEvent(FoodLevelChangeEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(keygen("ghost"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void BlockBreakEvent(org.bukkit.event.block.BlockBreakEvent event) {

        if (event.getPlayer().getPersistentDataContainer().has(keygen("ghost"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void Target(EntityTargetEvent event) {
        if (event.getTarget() != null) {
            if (event.getTarget().getPersistentDataContainer().has(keygen("ghost"))) {
                event.setCancelled(true);
            }}
    }


    @EventHandler
    public void GhostPickupEvent(PlayerAttemptPickupItemEvent event){
        if (event.getPlayer().getPersistentDataContainer().has(keygen("ghost"))) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void GhostInteractEvent(PlayerInteractEntityEvent event){
        if (event.getPlayer().getPersistentDataContainer().has(keygen("ghost"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void GhostBlockInteract(PlayerInteractEntityEvent event){
        if (event.getPlayer().getPersistentDataContainer().has(keygen("ghost"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void GhostMoveEvent(PlayerArmorStandManipulateEvent event){
        if (event.getPlayer().getPersistentDataContainer().has(keygen("ghost"))) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void GhostMoveEvent(PrePlayerAttackEntityEvent event){
        if (event.getPlayer().getPersistentDataContainer().has(keygen("ghost"))) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void GhostChestEvent(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        if (player.getPersistentDataContainer().has(keygen("ghost"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void GhostChestEvent3(InventoryInteractEvent event){
        Player player = (Player) event.getWhoClicked();
        if (player.getPersistentDataContainer().has(keygen("ghost"))) {
            event.setCancelled(true);
        }
    }


}