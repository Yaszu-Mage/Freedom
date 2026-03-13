package xyz.yaszu.freedom;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.yaszu.freedom.Commands.DevTools.openGui;
import xyz.yaszu.freedom.Commands.Trust;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionGui;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi;
import xyz.yaszu.freedom.Soul.Black;
import xyz.yaszu.freedom.Subsystems.black_flash;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.Util;

import java.util.Random;

import static xyz.yaszu.freedom.Soul.Green.removeOldFollowers;
import static xyz.yaszu.freedom.Util.Util.keygen;

public final class Freedom extends JavaPlugin implements Listener {



    public static Plugin get_plugin() {
        return Bukkit.getPluginManager().getPlugin("Freedom");
    }
    public static int version = 0;
    @EventHandler
    public void WorldLoadEvent(WorldLoadEvent event) {
        if (System.currentTimeMillis() <= start_time + 10000) {
            this.getLogger().info("Baller");
            removeOldFollowers();
        }
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event){
        removeOldFollowers();
        event.getPlayer().getPersistentDataContainer().set(keygen("sprite_active"),PersistentDataType.BOOLEAN,false);
    }


    Random random = new Random();
    public long start_time = 0;
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
        Bukkit.getPluginManager().registerEvents(this,this);
        Bukkit.getPluginManager().registerEvents(new Black(),this);
        version = random.nextInt(0,9999);
        this.getLogger().info("---Registered Listeners!---");
        //Register Commands
        openGui openGui = new openGui();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register("openGui", openGui);
            commands.registrar().register("opengui", openGui);
            commands.registrar().register(Trust.reviveArgument());
            commands.registrar().register(Trust.trustArgument());
            commands.registrar().register(Trust.playerArgument());
            commands.registrar().register(Trust.toggleArgument());
            commands.registrar().register(Trust.Ability_One());
            commands.registrar().register(Trust.Ability_Two());
            commands.registrar().register(Trust.Active_Passive());
            commands.registrar().register(Trust.summonFriendly());
        });
        removeOldFollowers();
        start_time = System.currentTimeMillis();
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
        Bukkit.getScheduler().cancelTasks(this);
        // Plugin shutdown logic
    }
}
