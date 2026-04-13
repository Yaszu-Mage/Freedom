package xyz.yaszu.freedom.Subsystems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class AdminManager {

    public record AdminProfile(String sudoName, String skinSource, String skinSignature) {}

    // Hardcoded admin profiles - UUID based as requested
    private static final Map<UUID, AdminProfile> ADMINS = Map.of(
            // Example: TheMoonLady (Source can be a skin name or a URL)
            UUID.fromString("83849cc9-677d-4599-8a2d-09d1c0469038"), new AdminProfile("TheGrandMage", "https://s.namemc.com/i/7c2b160450e0839c.png", null),
            // Example with a URL skin
            UUID.fromString("7b39a2df-ead1-4f91-910e-f1542fa8c333"), new AdminProfile("TheCarp", "https://s.namemc.com/i/f7998f18d571ff92.png", null),
            UUID.fromString("7d86f8b4-d11f-4735-ba6f-829f2d63401a"), new AdminProfile("AntiClock","https://s.namemc.com/i/76b47e0741aefbbd.png",null)

    );

    // Track sudo players within the current server session to differentiate across restarts
    private static final Set<UUID> sessionSudoPlayers = Collections.synchronizedSet(new HashSet<>());

    public static AdminProfile getProfile(UUID uuid) {
        return ADMINS.get(uuid);
    }

    public static boolean isSystemAdmin(UUID uuid) {
        return ADMINS.containsKey(uuid);
    }

    public static boolean isSudo(Player player) {
        return player.getPersistentDataContainer().has(FreedomKeys.isSudo(), PersistentDataType.BOOLEAN);
    }

    public static void toggleSudo(Player player) {
        UUID uuid = player.getUniqueId();
        if (!isSystemAdmin(uuid)) {
            player.sendRichMessage("<red>Access denied. You are not a recognized system admin.</red>");
            return;
        }

        if (isSudo(player)) {
            disableSudo(player, true);
        } else {
            enableSudo(player, ADMINS.get(uuid));
        }
    }

    private static void enableSudo(Player player, AdminProfile profile) {
        UUID uuid = player.getUniqueId();

        // Save normal state before enabling sudo
        savePlayerState(player, FreedomKeys.originalState());

        player.getPersistentDataContainer().set(FreedomKeys.isSudo(), PersistentDataType.BOOLEAN, true);
        sessionSudoPlayers.add(uuid);

        // Load sudo state (if any)
        loadPlayerState(player, FreedomKeys.sudoState());

        // Apply sudo identity
        applySudoIdentity(player, profile);

        // Hide nametag via packets
        PacketManager.updateSudoStatus(player, true);

        Bukkit.broadcast(Component.text(profile.sudoName() + " has joined", NamedTextColor.YELLOW));

        player.sendRichMessage("<green>Sudo mode ENABLED. You are now " + profile.sudoName() + ".</green>");
    }

    private static void disableSudo(Player player, boolean broadcast) {
        UUID uuid = player.getUniqueId();

        if (broadcast) {
            AdminProfile profile = ADMINS.get(uuid);
            String sudoName = (profile != null) ? profile.sudoName() : player.getName();
            Bukkit.broadcast(Component.text(sudoName + " has left", NamedTextColor.YELLOW));
        }

        // Save current sudo state before disabling
        savePlayerState(player, FreedomKeys.sudoState());

        player.getPersistentDataContainer().remove(FreedomKeys.isSudo());
        sessionSudoPlayers.remove(uuid);

        // Load original state
        loadPlayerState(player, FreedomKeys.originalState());

        // Restore names to default
        player.displayName(Component.text(player.getName()));
        player.playerListName(Component.text(player.getName()));
        player.customName(null);
        player.setCustomNameVisible(false);
        player.setOp(false);

        // Show nametag via packets
        PacketManager.updateSudoStatus(player, false);

        try {
            // Restore original skin
            Util.setSkinByName(player, player.getName());
        } catch (Exception e) {
            player.sendRichMessage("<red>Failed to restore skin: " + e.getMessage() + "</red>");
        }

        player.sendRichMessage("<yellow>Sudo mode DISABLED. Identity restored.</yellow>");
    }

    private static void applySudoIdentity(Player player, AdminProfile profile) {
        player.displayName(Component.text(profile.sudoName()));
        player.playerListName(Component.text(profile.sudoName()));
        player.customName(Component.text(profile.sudoName()));
        player.setCustomNameVisible(false); // Task said hide the nametag
        player.setOp(true);

        try {
            if (profile.skinSignature() != null && !profile.skinSignature().isEmpty()) {
                // Hardcoded Base64 + Signature
                Util.setSkinByProperties(player, profile.skinSource(), profile.skinSignature());
            } else if (profile.skinSource().startsWith("http")) {
                // Hardcoded URL
                Util.setSkinByUrl(player, profile.skinSource());
            } else {
                // Hardcoded Name
                Util.setSkinByName(player, profile.skinSource());
            }
        } catch (Exception e) {
            player.sendRichMessage("<red>Failed to apply sudo identity: " + e.getMessage() + "</red>");
        }
    }
    
    public static void handleJoin(Player player) {
        UUID uuid = player.getUniqueId();
        if (isSudo(player)) {
            if (sessionSudoPlayers.contains(uuid)) {
                // Relog in the same session - maintain sudo
                applySudoIdentity(player, ADMINS.get(uuid));
                PacketManager.updateSudoStatus(player, true);
                player.sendRichMessage("<green>Sudo mode maintained.</green>");
            } else {
                // Join after server start - revert to normal
                disableSudo(player, false);
                player.sendRichMessage("<yellow>Sudo mode reverted due to server restart.</yellow>");
            }
        } else {
            // Not in sudo mode - ensure deopped
            player.setOp(false);
        }
        // Send all sudo teams to any joining player
        PacketManager.sendAllSudoTeams(player);
    }

    public static void handleQuit(Player player) {
        // We don't necessarily need to clear on quit if we use PDC for persistence.
    }

    private static void savePlayerState(Player player, NamespacedKey key) {
        try {
            Map<String, Object> state = new HashMap<>();
            state.put("inventory", player.getInventory().getContents());
            state.put("armor", player.getInventory().getArmorContents());
            state.put("extra", player.getInventory().getExtraContents());
            state.put("enderchest", player.getEnderChest().getContents());
            state.put("location", player.getLocation());
            state.put("health", player.getHealth());
            state.put("food", player.getFoodLevel());
            state.put("saturation", (double) player.getSaturation());
            state.put("level", player.getLevel());
            state.put("xp", (double) player.getExp());
            state.put("gamemode", player.getGameMode().name());
            state.put("potions", new ArrayList<>(player.getActivePotionEffects()));
            state.put("fireTicks", player.getFireTicks());
            state.put("remainingAir", player.getRemainingAir());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(state);
            dataOutput.close();

            player.getPersistentDataContainer().set(key, PersistentDataType.BYTE_ARRAY, outputStream.toByteArray());
        } catch (IOException e) {
            player.sendRichMessage("<red>Error saving state: " + e.getMessage() + "</red>");
        }
    }

    private static void loadPlayerState(Player player, NamespacedKey key) {
        if (!player.getPersistentDataContainer().has(key, PersistentDataType.BYTE_ARRAY)) {
            // If no state saved (first time sudo), just clear for sudo
            if (key.equals(FreedomKeys.sudoState())) {
                player.getInventory().clear();
                player.getEnderChest().clear();
                player.setExp(0);
                player.setLevel(0);
                player.setHealth(20);
                player.setFoodLevel(20);
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
            }
            return;
        }

        try {
            byte[] bytes = player.getPersistentDataContainer().get(key, PersistentDataType.BYTE_ARRAY);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Map<String, Object> state = (Map<String, Object>) dataInput.readObject();
            dataInput.close();

            player.getInventory().setContents((ItemStack[]) state.get("inventory"));
            player.getInventory().setArmorContents((ItemStack[]) state.get("armor"));
            player.getInventory().setExtraContents((ItemStack[]) state.get("extra"));
            player.getEnderChest().setContents((ItemStack[]) state.get("enderchest"));
            player.teleport((Location) state.get("location"));
            player.setHealth((double) state.get("health"));
            player.setFoodLevel((int) state.get("food"));
            player.setSaturation(((Double) state.get("saturation")).floatValue());
            player.setLevel((int) state.get("level"));
            player.setExp(((Double) state.get("xp")).floatValue());
            player.setGameMode(GameMode.valueOf((String) state.get("gamemode")));
            player.setFireTicks((int) state.get("fireTicks"));
            player.setRemainingAir((int) state.get("remainingAir"));

            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            for (PotionEffect effect : (List<PotionEffect>) state.get("potions")) {
                player.addPotionEffect(effect);
            }

        } catch (Exception e) {
            player.sendRichMessage("<red>Error loading state: " + e.getMessage() + "</red>");
        }
    }
}
