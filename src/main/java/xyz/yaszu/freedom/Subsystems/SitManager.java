package xyz.yaszu.freedom.Subsystems;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SitManager implements Listener {

    private static final Map<UUID, UUID> sittingPlayers = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (sittingPlayers.containsKey(event.getPlayer().getUniqueId())) {
            Bukkit.getEntity(sittingPlayers.get(event.getPlayer().getUniqueId())).teleport(event.getPlayer().getLocation());
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getPlayer().isSneaking()) return;
        if (event.getPlayer().getVehicle() != null) return;
        
        // Don't sit if holding a block to allow building
        if (event.getItem() != null && event.getItem().getType().isBlock()) return;

        Block block = event.getClickedBlock();
        BlockData data = block.getBlockData();

        if (data instanceof Stairs || data instanceof Slab) {
            double yOffset = 0.5;
            if (data instanceof Slab slab) {
                if (slab.getType() == Slab.Type.TOP) yOffset = 1.0;
                else if (slab.getType() == Slab.Type.DOUBLE) yOffset = 1.0;
            } else if (data instanceof Stairs stairs) {
                if (stairs.getHalf() == Stairs.Half.TOP) yOffset = 1.0;
            }
            
            Location sitLoc = block.getLocation().add(0.5, yOffset, 0.5);
            sit(event.getPlayer(), sitLoc);
            event.setCancelled(true);
        }
    }

    public static void sit(Player player, Location loc) {
        if (sittingPlayers.containsKey(player.getUniqueId())) return;
        
        // Check for space above to prevent phasing
        if (!loc.clone().add(0, 1, 0).getBlock().isPassable()) {
            player.sendMessage(Util.dess("<red>Not enough space to sit here!"));
            return;
        }

        // Offset to place player's bottom at loc.getY()
        // Standard ArmorStand seat point is approximately 1.7 blocks above base
        Location seatLoc = loc.clone().add(0, 0, 0);
        ArmorStand seat = (ArmorStand) loc.getWorld().spawnEntity(seatLoc, EntityType.ARMOR_STAND);
        
        seat.setInvisible(true);
        seat.setGravity(false);
        seat.setMarker(true);
        seat.setBasePlate(false);
        seat.setCanPickupItems(false);
        seat.setCustomName("seat");
        seat.setCustomNameVisible(false);
        
        seat.addPassenger(player);
        sittingPlayers.put(player.getUniqueId(), seat.getUniqueId());
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID seatUUID = sittingPlayers.remove(player.getUniqueId());
            if (seatUUID != null) {
                Entity seat = org.bukkit.Bukkit.getEntity(seatUUID);
                if (seat != null) {
                    seat.remove();
                }
                
                // Teleport to a safe location to avoid phasing
                Location safeLoc = findSafeLocation(player.getLocation());
                player.teleport(safeLoc.add(0, 1, 0));
            }
        }
    }

    private Location findSafeLocation(Location loc) {
        // If current head space is clear, it's safe
        if (loc.clone().add(0, -1, 0).getBlock().isPassable()) {
            return loc;
        }
        
        // Otherwise, try to find a safe height nearby (up to 1 block)
        for (double y = 0.1; y <= 1.0; y += 0.1) {
            Location trial = loc.clone().add(0, y, 0);
            if (trial.clone().add(0, 1, 0).getBlock().isPassable()) {
                return trial;
            }
        }
        
        return loc;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID seatUUID = sittingPlayers.remove(event.getPlayer().getUniqueId());
        if (seatUUID != null) {
            Entity seat = org.bukkit.Bukkit.getEntity(seatUUID);
            if (seat != null) {
                seat.remove();
            }
        }
    }

    public static LiteralCommandNode<CommandSourceStack> sitCommand() {
        return Commands.literal("sit")
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        sit(player, player.getLocation());
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
