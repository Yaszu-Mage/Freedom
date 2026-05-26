package xyz.yaszu.freedom.Subsystems;


import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.ConfigManager;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;
import xyz.yaszu.freedom.Util.player_util;


import java.util.Random;

import static xyz.yaszu.freedom.Freedom.clearPlayerPersistentData;
import static xyz.yaszu.freedom.Util.Util.*;


public class Life_and_Death extends Util implements org.bukkit.event.Listener{
    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        if (!event.getPlayer().getPersistentDataContainer().has(keygen("life"))) {
            player_util.set_type_value(event.getPlayer(),"life",9, PersistentDataType.INTEGER);
        }
    }




    public static Util util = new Util();


    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event) {
        if (DuelManager.playerInDuelArena.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        Integer lives = getCachedPdcValue(event.getPlayer(), "life", PersistentDataType.INTEGER);
        if (lives != null && lives <= 0) {
            return;
        }
        Player victim = event.getEntity();
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent damageByEntity) {
            Entity damager = damageByEntity.getDamager();

            if (damager instanceof Player killer) {
                String trustedby = getCachedPdcValue(killer, "trustedby", PersistentDataType.STRING);
                String trustedby2 = getCachedPdcValue(victim, "trustedby", PersistentDataType.STRING);

                if (trustedby != null && trustedby2 != null) {
                    if (trustedby.contains(killer.getName()) && trustedby2.contains(damager.getName())) {
                        victim.setHealth(1);
                        victim.setFireTicks(0);
                        return;
                    }
                }
                player_util.set_type_value(
                        event.getPlayer(),
                        "life",
                        (lives != null ? lives : 9) - 1,
                        PersistentDataType.INTEGER
                );
            }
        }
        Integer newLives = getCachedPdcValue(event.getPlayer(), "life", PersistentDataType.INTEGER);
        if (newLives != null && newLives <= 0) {
            player_util.set_type_value(event.getPlayer(),"ghost",true,PersistentDataType.BOOLEAN);
            event.getPlayer().setAllowFlight(true);
            updateVisibility(event.getPlayer());
            // Ensure the dead player's aura is visible to ghost viewers
            updateAllVisibility(event.getPlayer());
            // Reschedule visibility updates for all viewers after a short delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateAllVisibility(event.getPlayer());
                    for (Player viewer : Bukkit.getOnlinePlayers()) {
                        updateAllVisibility(viewer);
                    }
                }
            }.runTaskLater(Freedom.get_plugin(), 2);
        }
        Player player = event.getPlayer();
    }



    @EventHandler
    public void Player_Join_Event(PlayerJoinEvent event) {
        updateVisibility(event.getPlayer());
        updateAllVisibility(event.getPlayer());
    }

    public static void updateVisibility(Player target) {
        Freedom.get_plugin().getLogger().info("Updating visibility for " + target.getName() + "grrr");
        boolean isGhost = target.getPersistentDataContainer().has(keygen("ghost"));
        boolean targetInDoubleVoid = target.getWorld().getName().equals("doublevoid");
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) continue;

            if (isGhost) {
                if (targetInDoubleVoid || viewer.getWorld().getName().equals("doublevoid") || player_util.does_player_have_tag(viewer, "ghost")) {
                    viewer.showPlayer(Freedom.get_plugin(), target);
                } else {
                    Freedom.get_plugin().getLogger().info("Ghost: " + viewer.getName() + " hidden: " + target.getName() + "");
                    viewer.hidePlayer(Freedom.get_plugin(), target);
                }
            } else {
                viewer.showPlayer(Freedom.get_plugin(), target);
            }
            updateAllVisibility(viewer);
        }
    }

    public static void updateAllVisibility(Player viewer) {
         boolean viewerInDoubleVoid = viewer.getWorld().getName().equals("doublevoid");
         boolean viewerCanSeeGhosts = viewerInDoubleVoid || player_util.does_player_have_tag(viewer, "ghost");
         Entity display = (Freedom.soulAuras.getOrDefault(viewer.getUniqueId(),null) != null) ? Freedom.soulAuras.get(viewer.getUniqueId()) : null;

         // Check BOTH main hand and off-hand for soulglass item
         ItemStack mainHand = viewer.getInventory().getItemInMainHand();
         ItemStack offHand = viewer.getInventory().getItemInOffHand();

         boolean hasSoulGlass = false;

         // Check main hand
         if (mainHand != null && mainHand.getType() != Material.AIR) {
             try {
                 String itemId = mainHand.getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING);
                 if ("soulglass".equals(itemId)) {
                     hasSoulGlass = true;
                 }
             } catch (Exception ignored) {}
         }

         // Check off-hand if not found
         if (!hasSoulGlass && offHand != null && offHand.getType() != Material.AIR) {
             try {
                 String itemId = offHand.getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING);
                 if ("soulglass".equals(itemId)) {
                     hasSoulGlass = true;
                 }
             } catch (Exception ignored) {}
         }

         // Enable visibility if holding soulglass
         if (hasSoulGlass) {
             viewerCanSeeGhosts = true;
         }
        if (display == null) {
            return;}
        if (viewerCanSeeGhosts) {
            showEntityToPlayer(viewer,display);
        } else {
            hideEntityFromPlayer(viewer,display);
        }
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) continue;

            boolean targetIsGhost = target.getPersistentDataContainer().has(keygen("ghost"));
            Entity targetAura = Freedom.soulAuras.get(target.getUniqueId());

            if (targetIsGhost) {
                if (viewerCanSeeGhosts || target.getWorld().getName().equals("doublevoid") || viewerInDoubleVoid) {
                    viewer.showPlayer(Freedom.get_plugin(), target);
                    // Show target's aura if it exists
                    if (targetAura != null) {
                        showEntityToPlayer(viewer, targetAura);
                    }
                } else {
                    viewer.hidePlayer(Freedom.get_plugin(), target);
                    // Hide target's aura if it exists
                    if (targetAura != null) {
                        hideEntityFromPlayer(viewer, targetAura);
                    }
                }
            } else {
                viewer.showPlayer(Freedom.get_plugin(), target);
                if (viewerCanSeeGhosts) {
                    // Show target's aura if it exists
                    if (targetAura != null) {
                        showEntityToPlayer(viewer, targetAura);
                    }
                } else {
                    // Hide target's aura if it exists
                    if (targetAura != null) {
                        hideEntityFromPlayer(viewer, targetAura);
                    }
                }

            }
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        updateVisibility(event.getPlayer());
        updateAllVisibility(event.getPlayer());
    }

    ConfigManager config = new ConfigManager(Freedom.get_plugin().getConfig());
    @EventHandler
    public void Can_See_Ghost(PlayerMoveEvent event) {
        if (event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY() == event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ()) {
            return;
        }
        updateAllVisibility(event.getPlayer());
        if (event.getPlayer().getPersistentDataContainer().get(keygen("life"),PersistentDataType.INTEGER) <= 0) {
            if (!event.getPlayer().getPersistentDataContainer().has(keygen("ghost"))) {
                event.getPlayer().getPersistentDataContainer().set(keygen("ghost"), PersistentDataType.BOOLEAN,true);
                event.getPlayer().setAllowFlight(true);
                updateAllVisibility(event.getPlayer());
            }
        }
        if (event.getPlayer().getPersistentDataContainer().has(keygen("life"))) {
            if (event.getPlayer().getPersistentDataContainer().has(keygen("ghost")) && event.getPlayer().getPersistentDataContainer().get(keygen("life"),PersistentDataType.INTEGER) > 0) {
                event.getPlayer().getPersistentDataContainer().remove(keygen("ghost"));
                updateAllVisibility(event.getPlayer());
            }
        } else {
            player_util.set_type_value(event.getPlayer(),"life",9, PersistentDataType.INTEGER);
        }
    }

    @EventHandler
    public void playerdropitemevent(PlayerDropItemEvent event) {
        if (event.getPlayer().getPersistentDataContainer().has(keygen("ghost"))) {
            event.setCancelled(true);
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
        updateVisibility(player);
        player.getInventory().clear();
        SoulImbueManager.unimbuePlayer(player);
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