package xyz.yaszu.freedom.Subsystems;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.FrogWatcher;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.UUID;

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

/**
 * a system allowing players to apply curses to others.
 * curses last for 3 minutes or until uncursed.
 * curse makes players turn into an animal of choice restricting their chat messages.
 * the messages will also play sounds when attempting to speak in chat.
 */
public class CurseManager implements Listener {
    private static final HashMap<UUID, MobDisguise> activeCurses = new HashMap<>();
    private static final long CURSE_DURATION_TICKS = 3600; // 3 minutes = 3600 ticks

    /**
     * apply curse to player
     *
     * @param victim player cursed
     * @param curser player aplieing curse
     */
    public static void curse(Player victim, Player curser) {
        victim.getPersistentDataContainer().set(keygen("cursed"), PersistentDataType.STRING, "Frog");
        victim.getPersistentDataContainer().set(keygen("cursedby"), PersistentDataType.STRING, curser.getName());
        victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 1, true, false));

        applyDisguise(victim);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (victim.isOnline() && isCursed(victim)) {
                    String type = victim.getPersistentDataContainer().get(keygen("cursed"), PersistentDataType.STRING);
                    if ("Frog".equalsIgnoreCase(type)) {
                        uncurse(victim);
                    }
                }
            }
        }.runTaskLater(Freedom.get_plugin(), CURSE_DURATION_TICKS);
    }

    /**
     * removes curse from player
     *
     * @param victim player uncursed
     */
    public static void uncurse(Player victim) {
        if (victim.isOnline()) {
            removeDisguise(victim);
            victim.removePotionEffect(PotionEffectType.WEAKNESS);

            if (!isCursed(victim)) {
                return;
            }

            String curserName = victim.getPersistentDataContainer().get(keygen("cursedby"), PersistentDataType.STRING);
            victim.getPersistentDataContainer().remove(keygen("cursed"));
            victim.getPersistentDataContainer().remove(keygen("cursedby"));

            if (curserName != null) {
                Player curser = Bukkit.getPlayer(curserName);
                if (curser != null) {
                    curser.getPersistentDataContainer().set(keygen("cancurse"), PersistentDataType.BOOLEAN, true);
                    curser.sendMessage(dess("<green>Your curse on " + victim.getName() + " has ended!</green>"));
                } else {
                    String recursors = Bukkit.getWorld("world").getPersistentDataContainer().getOrDefault(keygen("recursor"), PersistentDataType.STRING, "");
                    if (!recursors.contains(curserName)) {
                        Bukkit.getWorld("world").getPersistentDataContainer().set(keygen("recursor"), PersistentDataType.STRING, recursors + curserName + ",");
                    }
                }
            }

            removeDisguise(victim);
        } else {
            String uncursors = Bukkit.getWorld("world").getPersistentDataContainer().getOrDefault(keygen("uncursor"), PersistentDataType.STRING, "");
            if (!uncursors.contains(victim.getName())) {
                Bukkit.getWorld("world").getPersistentDataContainer().set(keygen("uncursor"), PersistentDataType.STRING, uncursors + victim.getName() + ",");
            }
        }
    }

    /**
     * boolean for if player is cursed
     *
     * @param player player checked
     * @return if or if not cursed
     */
    public static boolean isCursed(Player player) {
        return player.getPersistentDataContainer().has(keygen("cursed"));
    }

    /**
     * changes what player says acording to curse type
     *
     * @param player player sending message
     * @param message message attempted to be sent
     * @return message sent
     */
    public static String handleChat(Player player, String message) {
        if (player.getName().equals("TheAntiClock")) return new StringBuilder(message).reverse().toString().toLowerCase();
        if (!isCursed(player)) return message;
        String curseType = player.getPersistentDataContainer().get(keygen("cursed"), PersistentDataType.STRING);
        if (curseType == null) return message;

        String[] msg = message.split(" ");
        StringBuilder newmsg = new StringBuilder();

        if (curseType.equalsIgnoreCase("Cat")) {
            for (int i = 0; i < msg.length; i++) {
                newmsg.append("Meow ");
            }
        } else if (curseType.equalsIgnoreCase("Frog")) {
            for (int i = 0; i < msg.length; i++) {
                newmsg.append("Ribbit ");
            }
        } else {
            return message;
        }
        return newmsg.toString().trim();
    }

    /**
     * changes players apearance to a player or entity
     *
     * @param player player disguising
     */
    public static void applyDisguise(Player player) {
        if (!activeCurses.containsKey(player.getUniqueId())) {
            MobDisguise mobDisguise = new MobDisguise(DisguiseType.FROG);
            mobDisguise.addPlayer(player);
            mobDisguise.setEntity(player);
            mobDisguise.startDisguise();
            FrogWatcher watcher = (FrogWatcher) mobDisguise.getWatcher();
            watcher.setVariant(Frog.Variant.COLD);
            activeCurses.put(player.getUniqueId(), mobDisguise);
        }
    }

    /**
     * removes disguise from player
     *
     * @param player player no longer disguised
     */
    public static void removeDisguise(Player player) {
        MobDisguise disguise = activeCurses.remove(player.getUniqueId());
        if (disguise != null) {
            disguise.removeDisguise();
        }
    }

    /**
     * --unused--
     * @param event --unused--
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Handle if player was uncursed while offline
        String uncursors = Bukkit.getWorld("world").getPersistentDataContainer().getOrDefault(keygen("uncursor"), PersistentDataType.STRING, "");
        if (uncursors.contains(player.getName())) {
            uncurse(player);
            String updatedUncursors = uncursors.replace(player.getName() + ",", "");
            if (updatedUncursors.isEmpty()) {
                Bukkit.getWorld("world").getPersistentDataContainer().remove(keygen("uncursor"));
            } else {
                Bukkit.getWorld("world").getPersistentDataContainer().set(keygen("uncursor"), PersistentDataType.STRING, updatedUncursors);
            }
        }

        // Handle if player is still cursed, re-apply disguise
        if (isCursed(player)) {
            applyDisguise(player);
        }

        // Handle if player's curser ability needs to be restored
        String recursors = Bukkit.getWorld("world").getPersistentDataContainer().getOrDefault(keygen("recursor"), PersistentDataType.STRING, "");
        if (recursors.contains(player.getName())) {
            player.getPersistentDataContainer().set(keygen("cancurse"), PersistentDataType.BOOLEAN, true);
            String updatedRecursors = recursors.replace(player.getName() + ",", "");
            if (updatedRecursors.isEmpty()) {
                Bukkit.getWorld("world").getPersistentDataContainer().remove(keygen("recursor"));
            } else {
                Bukkit.getWorld("world").getPersistentDataContainer().set(keygen("recursor"), PersistentDataType.STRING, updatedRecursors);
            }
        }
    }

    /**
     * --unused--
     * @param event --unused--
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();

        // 1. If the dead player was CURSED as a Frog, uncurse them (existing logic)
        if (isCursed(deadPlayer)) {
            String type = deadPlayer.getPersistentDataContainer().get(keygen("cursed"), PersistentDataType.STRING);
            if ("Frog".equalsIgnoreCase(type)) {
                uncurse(deadPlayer);
            }
        }

        // 2. If the dead player was a CURSER, uncurse their victims
        String deadPlayerName = deadPlayer.getName();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (isCursed(onlinePlayer)) {
                String type = onlinePlayer.getPersistentDataContainer().get(keygen("cursed"), PersistentDataType.STRING);
                if (!"Frog".equalsIgnoreCase(type)) continue;

                String curserName = onlinePlayer.getPersistentDataContainer().get(keygen("cursedby"), PersistentDataType.STRING);
                if (deadPlayerName.equals(curserName)) {
                    uncurse(onlinePlayer);
                    onlinePlayer.sendMessage(dess("<green>Your curser has died, and you are free!</green>"));
                }
            }
        }
    }
}
