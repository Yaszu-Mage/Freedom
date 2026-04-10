package xyz.yaszu.freedom.Information;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.Map;
import xyz.yaszu.freedom.Information.Alchemy.Circle;

public class Information_Handler extends Util implements Listener {
    public static void register_Info() {
        register(new Circle(),"circle");
    }

    public static final Map<String, BaseInformation> ITEMS = new HashMap<>();

    private static void register(BaseInformation item, String id) {
        ITEMS.put(id, item);
    }
}
