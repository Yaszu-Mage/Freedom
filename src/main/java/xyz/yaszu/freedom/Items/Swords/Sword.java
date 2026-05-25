package xyz.yaszu.freedom.Items.Swords;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.ItemListener;
import xyz.yaszu.freedom.Items.Swords.Items.*;

import java.util.HashMap;
import java.util.List;

public interface Sword {
    public List<Component> visions();
    public static Sword getSwordfromEnum(SwordType type) {
        return getSword(type.name());
    }
    public static Sword getSword(String name) {
        name = name.toLowerCase();
        switch (name) {
            case "darkheart": return new Darkheart();
            case "venomshank": return new Venomshank();
            case "icedagger": return new Icedagger();
            case "firebrand": return new Firebrand();
            case "windforce": return new Windforce();
            case "ghostwalker": return new Ghostwalker();
            case "illumina": return new Illumina();
        }
        return null;
    }
    public int Cooldown();
    public SwordType SwordType();

    public static boolean canUse(Player player, Sword sword) {
        if (ItemListener.SWORD_COOLDOWNS.getOrDefault(player.getUniqueId(),null) == null) {
            Freedom.get_plugin().getLogger().info("Cooldowns not initialized so true");
            return true;
        }
        HashMap<SwordType,Long> cooldowns = ItemListener.SWORD_COOLDOWNS.get(player.getUniqueId());
        if (cooldowns.getOrDefault(sword,null) != null) {
            Long lastUsed = cooldowns.get(sword);
            if (lastUsed + Sword.getSwordfromEnum(sword.SwordType()).Cooldown() <= System.currentTimeMillis()) {
                Freedom.get_plugin().getLogger().info("Cooldowns initialized and cooldown not null and cooldown correct so true");
                return true;
            } else {
                Freedom.get_plugin().getLogger().info("Cooldowns initialized and cooldown not null but cooldown incorrect so false");
                return false;
            }
        } else {
            return true;
        }
    }

    public static void StartCooldown(Player player, SwordType sword) {
        if (ItemListener.SWORD_COOLDOWNS.containsKey(player.getUniqueId())) {
            HashMap<SwordType,Long> cooldowns = ItemListener.SWORD_COOLDOWNS.get(player.getUniqueId());
            cooldowns.put(sword,System.currentTimeMillis());
            ItemListener.SWORD_COOLDOWNS.put(player.getUniqueId(),cooldowns);
        } else {
            HashMap<SwordType,Long> cooldowns = new HashMap<>();
            cooldowns.put(sword,System.currentTimeMillis());
            ItemListener.SWORD_COOLDOWNS.put(player.getUniqueId(),cooldowns);
        }
    }
    public enum SwordType {
        Darkheart,
        Firebrand,
        Windforce,
        Ghostwalker,
        Illumina,
        Venomshank,
        Icedagger,
    }
}
