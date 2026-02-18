package xyz.yaszu.freedom.Util;

import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class player_util {

    public static Util util = new Util();

    public static boolean does_player_have_tag(Player player, String tag) {
        if (player.getPersistentDataContainer().has(util.keygen(tag), PersistentDataType.STRING)) {
            return true;
        } else {
            return false;
        }
    }
    public static String type_value(Player player,String tag) {
        if (player.getPersistentDataContainer().has(util.keygen(tag), PersistentDataType.STRING)) {
            return player.getPersistentDataContainer().get(util.keygen(tag), PersistentDataType.STRING);
        } else {
            return "";
        }
    }
    public static void set_type_value(Player player,String tag,Object value,PersistentDataType type) {
        player.getPersistentDataContainer().set(util.keygen(tag), type,value);
    }

    public static Object get_type_value(Player player, String tag, PersistentDataType type) {
        if (player.getPersistentDataContainer().has(util.keygen(tag), type)) {
            return player.getPersistentDataContainer().get(util.keygen(tag), type);
        } else {
            return null;
        }
    }

}