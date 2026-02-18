package xyz.yaszu.freedom;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.yaszu.freedom.Commands.DevTools.openGui;
import xyz.yaszu.freedom.Commands.Trust;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionGui;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi;
import xyz.yaszu.freedom.Soul.black_flash;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.Util;

public final class Freedom extends JavaPlugin {



    public static Plugin get_plugin() {
        return Bukkit.getPluginManager().getPlugin("Freedom");
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        //Enable Listeners
        soulListener soulListener = new soulListener();
        Bukkit.getPluginManager().registerEvents(soulListener,this);
        Bukkit.getPluginManager().registerEvents(new selectionGui(), this);
        Bukkit.getPluginManager().registerEvents(new selectionUi(),this);
        Bukkit.getPluginManager().registerEvents(new black_flash(),this);
        Bukkit.getPluginManager().registerEvents(new Life_and_Death(), this);
        this.getLogger().info("---Registered Listeners!---");
        //Register Commands
        openGui openGui = new openGui();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register("openGui", openGui);
            commands.registrar().register(Trust.trustArgument());
            commands.registrar().register(Trust.playerArgument());
            commands.registrar().register(Trust.toggleArgument());
            commands.registrar().register(Trust.Ability_One());
            commands.registrar().register(Trust.Ability_Two());
            commands.registrar().register(Trust.Active_Passive());
            commands.registrar().register(Trust.summonFriendly());
        });

    }

    public static Util util = new Util();

    public static void clearPlayerPersistentData(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        for (NamespacedKey key : container.getKeys()) {
            container.remove(key);
        }
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
