package xyz.yaszu.freedom.Util;

import org.bukkit.NamespacedKey;
import xyz.yaszu.freedom.Freedom;

public class FreedomKeys {


    public static NamespacedKey itemId() {
        return key("item_id");
    }

    public static NamespacedKey soulPoint() {
        return key("soulpoints");
    }

    public static NamespacedKey spriteActive() {
        return key("sprite_active");
    }

    public static NamespacedKey comorAction() {
        return key("comoraction");
    }

    public static NamespacedKey trustedBy() {
        return key("trustedby");
    }

    public static NamespacedKey soul() {
        return key("soul");
    }

    public static NamespacedKey key(String key) {
        return new NamespacedKey(Freedom.get_plugin(),key);
    }
}
