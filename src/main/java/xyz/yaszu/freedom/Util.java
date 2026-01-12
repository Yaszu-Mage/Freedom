package xyz.yaszu.freedom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

public class Util {
    public NamespacedKey keygen(String key) {
        return new NamespacedKey(Bukkit.getPluginManager().getPlugin("Freedom"),key);
    }

    public static Component dess(String minimessage) {
        return MiniMessage.miniMessage().deserialize(minimessage);
    }
}
