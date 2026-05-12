package xyz.yaszu.freedom.Subsystems;

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Alchemy.Arcanus;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.Ultra.Black;
import xyz.yaszu.freedom.Util.Util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager extends Util implements Listener {

    private static final Map<UUID, UUID> echoMap = new ConcurrentHashMap<>();
    public Arcanus arcanus = new Arcanus();

    @EventHandler
    public void PlayerChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (getSoulType(player) == SoulTypes.Arcanus) {
            arcanus.Passive(player,event);
        }
        // Check for echo
        UUID sourceUUID = echoMap.remove(player.getUniqueId());
        Player sourcePlayer = (sourceUUID != null) ? Bukkit.getPlayer(sourceUUID) : null;
        boolean isEcho = sourcePlayer != null;
        
        Player realTypist = isEcho ? sourcePlayer : player;
        String message = event.getMessage();

        // If not an echo, process message and check for disguise
        if (!isEcho) {
            // Apply CurseManager modifications
            String processedMessage = CurseManager.handleChat(player, message);
            event.setMessage(processedMessage);
            boolean isDisguised = player.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN);
            if (isDisguised) {
                PlayerDisguise disguise = Black.originalProfiles.get(player.getUniqueId());
                if (disguise != null) {
                    Player target = Bukkit.getPlayer(disguise.getName());
                    if (target != null && target.isOnline()) {
                        // Echo through online target
                        event.setCancelled(true);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                echoMap.put(target.getUniqueId(), player.getUniqueId());
                                target.chat(event.getMessage());
                            }
                        }.runTask(Freedom.get_plugin());
                        return;
                    } else {
                        // Target is offline, substitute name in current event format
                        String targetName = disguise.getName();
                        // Temporarily set names if plugins use them
                        player.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(targetName));
                        player.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(targetName));
                    }
                }
            }
        }

        // Filtering logic (runs for non-disguised OR for the echoed event OR for offline disguise)
        String processedMessage = event.getMessage();
        
        // Use the disguise target's status if disguised and offline
        boolean is_alive;
        boolean isDisguised = player.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN);
        if (!isEcho && isDisguised) {
            PlayerDisguise disguise = Black.originalProfiles.get(player.getUniqueId());
            Player target = (disguise != null) ? Bukkit.getPlayer(disguise.getName()) : null;
            if (target != null && target.isOnline()) {
                is_alive = Life_and_Death.is_alive(target);
            } else {
                // If offline, we assume they are alive or use the typist's status
                is_alive = Life_and_Death.is_alive(player);
            }
        } else {
            is_alive = Life_and_Death.is_alive(player); // Status of the identity chatting
        }

        // Filter recipients and play sounds
        event.getRecipients().clear();
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            // Check if recipient can see the sender (realTypist)
            if (recipient.canSee(realTypist)) {
                if (is_alive) {
                    // Alive chat: only to those who are alive
                    if (Life_and_Death.is_alive(recipient)) {
                        event.getRecipients().add(recipient);
                        playProximitySound(recipient, processedMessage);
                    }
                } else {
                    // Dead chat (ghost): only to other dead players
                    if (!Life_and_Death.is_alive(recipient) || (recipient.getWorld() == Bukkit.getWorld("doublevoid") && recipient.getLocation().distanceSquared(player.getLocation()) <= 100)) {
                        event.getRecipients().add(recipient);
                        playProximitySound(recipient, processedMessage);
                    }
                }
            }
        }
        
        // Ensure the real typist sees their own message
        if (!event.getRecipients().contains(realTypist)) {
            event.getRecipients().add(realTypist);
        }
        // Ensure the identity player sees it too (if it's an echo)
        if (isEcho && !event.getRecipients().contains(player)) {
            event.getRecipients().add(player);
        }
    }

    private void playProximitySound(Player recipient, String message) {
        if (message.contains("Meow")) {
            recipient.playSound(recipient.getLocation(), Sound.ENTITY_CAT_PURREOW, 1, 1);
        } else if (message.contains("Ribbit")) {
            recipient.playSound(recipient.getLocation(), Sound.ENTITY_FROG_AMBIENT, 1, 1);
        } else if (message.contains("Tnt")) {
            recipient.playSound(recipient.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        } else {
            recipient.playSound(recipient.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1, 1);
        }
    }
}
