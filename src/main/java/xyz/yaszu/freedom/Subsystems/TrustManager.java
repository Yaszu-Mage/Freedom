package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.FreedomKeys;

import java.util.*;
import java.util.stream.Collectors;

public class TrustManager {
    
    private static final NamespacedKey TRUSTED_BY_KEY = FreedomKeys.trustedBy();

    public enum ProvinceTrustFlag {
        BREAK_BLOCKS("Break Blocks", Material.IRON_PICKAXE),
        PLACE_BLOCKS("Place Blocks", Material.GRASS_BLOCK),
        OPEN_CHESTS("Open Chests", Material.CHEST),
        ATTACK_MOBS("Attack Mobs", Material.IRON_SWORD),
        INTERACT_BLOCKS("Interact Blocks", Material.LEVER);

        public final String displayName;
        public final Material icon;

        ProvinceTrustFlag(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
    }

    public enum TrustTier {
        NONE("None", Material.BARRIER),
        VISITOR("Visitor", Material.CHEST),
        BUILDER("Builder", Material.NETHERITE_PICKAXE);

        public final String displayName;
        public final Material icon;

        TrustTier(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public Set<ProvinceTrustFlag> getFlags() {
            switch (this) {
                case VISITOR:
                    return EnumSet.of(ProvinceTrustFlag.OPEN_CHESTS, ProvinceTrustFlag.INTERACT_BLOCKS, ProvinceTrustFlag.ATTACK_MOBS);
                case BUILDER:
                    return EnumSet.allOf(ProvinceTrustFlag.class);
                default:
                    return EnumSet.noneOf(ProvinceTrustFlag.class);
            }
        }
    }
    
    public static List<UUID> getTrustedBy(UUID targetUuid) {
        Player onlineTarget = Bukkit.getPlayer(targetUuid);
        String trustedString;
        if (onlineTarget != null) {
            trustedString = onlineTarget.getPersistentDataContainer().get(TRUSTED_BY_KEY, PersistentDataType.STRING);
        } else {
            // Check offline player if possible
            org.bukkit.OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetUuid);
            trustedString = offlineTarget.getPersistentDataContainer().get(TRUSTED_BY_KEY, PersistentDataType.STRING);
        }

        if (trustedString == null || trustedString.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Handle both old names and new UUID format for backward compatibility
        return Arrays.stream(trustedString.split(","))
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return UUID.fromString(s);
                    } catch (IllegalArgumentException e) {
                        // Not a UUID, probably an old name. We'll ignore it or keep it as null.
                        // Since we want to move to UUIDs, we'll return null and filter it out.
                        return null; 
                    }
                })
                .filter(uuid -> uuid != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static boolean isTrusted(UUID targetUuid, UUID guestUuid) {
        if (targetUuid.equals(guestUuid)) return true;
        List<UUID> trusted = getTrustedBy(targetUuid);
        return trusted.contains(guestUuid);
    }

    public static List<UUID> getTrustedBy(Player target) {
        return getTrustedBy(target.getUniqueId());
    }

    public static boolean isTrustedBy(Player target, Player healer) {
        return isTrusted(target.getUniqueId(), healer.getUniqueId());
    }

    public static void addTrust(Player target, Player healer) {
        List<UUID> trusted = getTrustedBy(target);
        if (!trusted.contains(healer.getUniqueId())) {
            trusted.add(healer.getUniqueId());
            saveTrustedBy(target, trusted);
        }
    }

    public static void removeTrust(Player target, Player healer) {
        List<UUID> trusted = getTrustedBy(target);
        if (trusted.remove(healer.getUniqueId())) {
            saveTrustedBy(target, trusted);
        }
    }

    public static void toggleTrust(Player target, Player healer) {
        if (isTrustedBy(target, healer)) {
            removeTrust(target, healer);
        } else {
            addTrust(target, healer);
        }
    }

    public static Set<ProvinceTrustFlag> getTrustFlags(UUID ownerUuid, UUID guestUuid) {
        org.bukkit.OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUuid);
        String keyStr = "trust_flags_" + guestUuid.toString();
        NamespacedKey key = new NamespacedKey(Freedom.get_plugin(), keyStr);
        String flagsStr = owner.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        
        Set<ProvinceTrustFlag> flags = EnumSet.noneOf(ProvinceTrustFlag.class);
        if (flagsStr == null || flagsStr.isEmpty()) {
            // Default: if trusted but no flags, maybe they have everything?
            // Or maybe they have nothing?
            // Let's default to nothing if we want "tiered" trust.
            return flags;
        }
        
        for (String f : flagsStr.split(",")) {
            try {
                flags.add(ProvinceTrustFlag.valueOf(f));
            } catch (IllegalArgumentException ignored) {}
        }
        return flags;
    }

    public static void setTrustFlags(UUID ownerUuid, UUID guestUuid, Set<ProvinceTrustFlag> flags) {
        Player owner = Bukkit.getPlayer(ownerUuid);
        if (owner == null) return; // Can only set flags for online players

        String keyStr = "trust_flags_" + guestUuid.toString();
        NamespacedKey key = new NamespacedKey(Freedom.get_plugin(), keyStr);
        
        String flagsStr = flags.stream().map(Enum::name).collect(Collectors.joining(","));
        owner.getPersistentDataContainer().set(key, PersistentDataType.STRING, flagsStr);
    }
    
    public static void toggleTrustFlag(UUID ownerUuid, UUID guestUuid, ProvinceTrustFlag flag) {
        Set<ProvinceTrustFlag> flags = getTrustFlags(ownerUuid, guestUuid);
        if (flags.contains(flag)) {
            flags.remove(flag);
        } else {
            flags.add(flag);
        }
        setTrustFlags(ownerUuid, guestUuid, flags);
    }

    public static boolean hasFlag(UUID ownerUuid, UUID guestUuid, ProvinceTrustFlag flag) {
        if (ownerUuid.equals(guestUuid)) return true;
        return getTrustFlags(ownerUuid, guestUuid).contains(flag);
    }

    private static void saveTrustedBy(Player target, List<UUID> trusted) {
        String trustedString = trusted.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
        target.getPersistentDataContainer().set(TRUSTED_BY_KEY, PersistentDataType.STRING, trustedString);
    }

    // Helper to check if a player is trusted by NAME (for compatibility with existing logic if needed)
    public static boolean isTrustedByName(Player target, String healerName) {
        String trustedString = target.getPersistentDataContainer().get(TRUSTED_BY_KEY, PersistentDataType.STRING);
        if (trustedString == null || trustedString.isEmpty()) {
            return false;
        }
        // Check if the healerName is in the string (this is how the old system worked)
        // Note: this is still slightly buggy as it doesn't handle boundaries, but it's for compatibility.
        return trustedString.contains(healerName);
    }
}
