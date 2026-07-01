package xyz.yaszu.freedom.Util;

import org.bukkit.NamespacedKey;
import xyz.yaszu.freedom.Freedom;

/**
 * holder for keys across the plugin
 */

public class FreedomKeys {


    public static NamespacedKey itemId() {
        return key("item_id");
    }

    /**
     * amount of soul points that a player has
     * (soul points are the currency used for using color based abilities)
     *
     * @return number of soulpoints held by a player
     */
    public static NamespacedKey soulPoint() {
        return key("SoulPoint");
    }

    /**
     * used to enable / disable sprite
     * (no longer used)
     *
     * @return boolean of if sprite is active
     */
    public static NamespacedKey spriteActive() {
        return key("sprite_active");
    }

    /**
     * a check for if a player has had ablitlies disabled
     *
     * @return boolean of if silenced
     */
    public static NamespacedKey silence() {
        return key("silence");
    }

    /**
     * check weather a player has set their abilities to be used on command or action
     *
     * @return boolean of command / action
     */
    public static NamespacedKey comorAction() {
        return key("comoraction");
    }

    /**
     * check for if a player has been added to trust
     *
     * @return boolean of if trusted
     */
    public static NamespacedKey trustedBy() {
        return key("trustedby");
    }

    /**
     * --unused--
     * @return --unused--
     */
    public static NamespacedKey soul() {
        return key("soul");
    }

    /**
     * the moveset of abilities a player has
     *
     * @return moveset of player
     */
    public static NamespacedKey moveset() {
        return key("moveset");
    }

    /**
     * weather player is allowed block breaking inside the province
     *
     * @return boolean of if player is allowed to break blocks
     */
    public static NamespacedKey provinceBlockBreak() {
        return key("province_block_break");
    }

    /**
     * weather player is allowed block placing inside the province
     *
     * @return boolean of if player is allowed to place blocks
     */
    public static NamespacedKey provinceBlockPlace() {
        return key("province_block_place");
    }

    /**
     * weather a province has fire spread enabled
     *
     * @return boolean of if fire spread is allowed
     */
    public static NamespacedKey provinceFireSpread() {
        return key("province_fire_spread");
    }

    /**
     * weather a province has  explosions enabled
     *
     * @return boolean of if explosions is allowed
     */
    public static NamespacedKey provinceExplosions() {
        return key("province_explosions");
    }

    /**
     * weather a player has an artifact enabled
     *
     * @return boolean of if active on player
     */
    public static NamespacedKey activeArtifact() {
        return key("active_artifact");
    }

    /**
     * weather a player is in sudo mode
     *
     * @return boolean of if in sudo or not
     */
    public static NamespacedKey isSudo() {
        return key("is_sudo");
    }

    /**
     * the saved state of a player
     *
     * used in both the sudo system and arena system
     *
     * @return saved state of a player
     */
    public static NamespacedKey originalState() {
        return key("original_state");
    }

    /**
     * used to set the alcohol status of a player
     *
     * @return alcohol status of a player
     */
    public static NamespacedKey alcohol() {return key("alcohol");}

    /**
     * the sudo state of an admin
     *
     * @return the state of the admin's sudo
     */
    public static NamespacedKey sudoState() {
        return key("sudo_state");
    }

    /**
     * used to show if an item is a focus
     *
     * @return if an item is a focus
     */
    public static NamespacedKey spellFocus() {
        return key("spell_focus");
    }


    public static NamespacedKey key(String key) {
        return new NamespacedKey(Freedom.get_plugin(),key);
    }
}
