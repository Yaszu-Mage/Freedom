package xyz.yaszu.freedom.Util;

import org.bukkit.NamespacedKey;
import xyz.yaszu.freedom.Freedom;

public final class FreedomKeys {
    private FreedomKeys() {}

    public static final String ITEM_ID = "item_id";
    public static final String SOUL = "soul";
    public static final String SOUL_POINT = "SoulPoint";
    public static final String TRUSTED_BY = "trustedby";
    public static final String SPRITE_ACTIVE = "sprite_active";
    public static final String COMOR_ACTION = "ComorAction";

    public static NamespacedKey key(String key) {
        return new NamespacedKey(Freedom.get_plugin(), key);
    }

    public static NamespacedKey itemId() { return key(ITEM_ID); }
    public static NamespacedKey soul() { return key(SOUL); }
    public static NamespacedKey soulPoint() { return key(SOUL_POINT); }
    public static NamespacedKey trustedBy() { return key(TRUSTED_BY); }
    public static NamespacedKey spriteActive() { return key(SPRITE_ACTIVE); }
    public static NamespacedKey comorAction() { return key(COMOR_ACTION); }
}
