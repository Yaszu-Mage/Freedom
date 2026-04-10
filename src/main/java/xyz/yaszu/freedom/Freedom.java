package xyz.yaszu.freedom;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import net.skinsrestorer.api.SkinsRestorerProvider;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Alchemy.Alchemy;
import xyz.yaszu.freedom.Alchemy.voidGenerator;
import xyz.yaszu.freedom.Commands.DevTools.openGui;
import xyz.yaszu.freedom.Commands.Trust;
import xyz.yaszu.freedom.GUI.SelectionGUI.UltraselectionUi;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionGui;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi;
import xyz.yaszu.freedom.Information.Information_Handler;
import xyz.yaszu.freedom.Items.ItemListener;
import xyz.yaszu.freedom.Items.Relics.PainScythe;
import xyz.yaszu.freedom.Soul.Base.BaseBlack;
import xyz.yaszu.freedom.Soul.Base.BaseOrange;
import xyz.yaszu.freedom.Soul.Ultra.Black;
import xyz.yaszu.freedom.Soul.Ultra.Mocha;
import xyz.yaszu.freedom.Soul.Ultra.Orange;
import xyz.yaszu.freedom.Subsystems.CombatTimer;
import xyz.yaszu.freedom.Subsystems.CurseManager;
import xyz.yaszu.freedom.Subsystems.TabDistance;
import xyz.yaszu.freedom.Subsystems.black_flash;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import static xyz.yaszu.freedom.Alchemy.Alchemy.loadSchematic;
import static xyz.yaszu.freedom.Soul.Ultra.Green.removeOldFollowers;
import static xyz.yaszu.freedom.Util.Util.keygen;

public final class Freedom extends JavaPlugin implements Listener {

    public static int version = 6942067;

    public void reapplyCurseWeakness(Player player) {
        if (player == null || !player.isOnline()) return;

        if (!player.getPersistentDataContainer().has(keygen("cursed"), PersistentDataType.STRING)) return;
        if (!"Frog".equals(player.getPersistentDataContainer().get(keygen("cursed"), PersistentDataType.STRING))) return;

        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 0, true, false));
    }

    @EventHandler
    public void onTotemPop(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                reapplyCurseWeakness(player);
            }
        }.runTaskLater(Freedom.get_plugin(), 30);

    }



    public static Plugin get_plugin() {
        return Bukkit.getPluginManager().getPlugin("Freedom");
    }
    @EventHandler
    public void WorldLoadEvent(WorldLoadEvent event) {
        if (System.currentTimeMillis() <= start_time + 10000) {
            this.getLogger().info("Baller");
            removeOldFollowers();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getName().equals("TheMoonLady")) {
            event.getPlayer().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
        }
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event){
        removeOldFollowers();
        event.getPlayer().performCommand("rules");
        event.getPlayer().getPersistentDataContainer().set(FreedomKeys.spriteActive(),PersistentDataType.BOOLEAN,false);
        if (event.getPlayer().getName().equals("TheMoonLady")) {
            event.getPlayer().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
        }
    }


    Random random = new Random();
    public long start_time = 0;
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        //Enable Listeners
        Util.skinsRestorerAPI = SkinsRestorerProvider.get();
        soulListener soulListener = new soulListener();
        soulListener.registerSouls();
        Bukkit.getPluginManager().registerEvents(this,this);
        Bukkit.getPluginManager().registerEvents(soulListener,this);
        Bukkit.getPluginManager().registerEvents(new selectionGui(), this);
        Bukkit.getPluginManager().registerEvents(new selectionUi(),this);
        Bukkit.getPluginManager().registerEvents(new black_flash(),this);
        Bukkit.getPluginManager().registerEvents(new Life_and_Death(), this);
        Bukkit.getPluginManager().registerEvents(this,this);
        Bukkit.getPluginManager().registerEvents(new Black(),this);
        Bukkit.getPluginManager().registerEvents(new TabDistance(), this);
        Bukkit.getPluginManager().registerEvents(new Mocha(), this);
        Bukkit.getPluginManager().registerEvents(new Orange(),this);
        Bukkit.getPluginManager().registerEvents(new BaseOrange(),this);
        Bukkit.getPluginManager().registerEvents(new BaseBlack(),this);
        Bukkit.getPluginManager().registerEvents(new ItemListener(), this);
        Bukkit.getPluginManager().registerEvents(new UltraselectionUi(), this);
        Bukkit.getPluginManager().registerEvents(new CombatTimer(),this);
        Bukkit.getPluginManager().registerEvents(new CurseManager(), this);
        Bukkit.getPluginManager().registerEvents(new Alchemy(), this);
        Bukkit.getPluginManager().registerEvents(new PainScythe(), this);
        this.getLogger().info("---Registered Listeners!---");
        //Register Commands
        openGui openGui = new openGui();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register("openGui", openGui);
            commands.registrar().register("opengui", openGui);
            commands.registrar().register(Trust.reviveArgument());
            commands.registrar().register(Trust.uncurseArgument());
            commands.registrar().register(Trust.trustArgument());
            commands.registrar().register(Trust.playerArgument());
            commands.registrar().register(Trust.toggleArgument());
            commands.registrar().register(Trust.Ability_One());
            commands.registrar().register(Trust.Ability_Two());
            commands.registrar().register(Trust.Active_Passive());
            commands.registrar().register(Trust.summonFriendly());
            commands.registrar().register(Trust.Passive());
            commands.registrar().register(Trust.test());
            commands.registrar().register(Trust.rules());
            commands.registrar().register(Trust.soulArgument());
            commands.registrar().register(Trust.customItemArgument());
        });
        removeOldFollowers();
        ItemListener.registerItems();
        Information_Handler.register_Info();
        start_time = System.currentTimeMillis();
        createVoid();


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

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) throws IOException, WorldEditException {
        if (event.getWorld() == Bukkit.getWorld("void") && !event.getChunk().getPersistentDataContainer().has(FreedomKeys.key("void"))) {
            event.getChunk().getPersistentDataContainer().set(FreedomKeys.key("void"),PersistentDataType.BOOLEAN,true);
            int currentrand = random.nextInt(1000);
            if (currentrand <= 50) {
                File ritualschem;
                int rand = random.nextInt(0,4);
                switch (rand) {
                    case 1 -> ritualschem = Freedom.get_plugin().getDataFolder().toPath().resolve("voidisland2.schem").toFile();
                    case 2 -> ritualschem = Freedom.get_plugin().getDataFolder().toPath().resolve("voidisland3.schem").toFile();
                    case 3 -> ritualschem = Freedom.get_plugin().getDataFolder().toPath().resolve("voidisland4.schem").toFile();
                    default -> ritualschem = Freedom.get_plugin().getDataFolder().toPath().resolve("voidisland.schem").toFile();
                }
                Freedom.get_plugin().getLogger().info(String.valueOf(Freedom.get_plugin().getDataFolder().toPath()));
                int x = (event.getChunk().getX() + random.nextInt(0,16)) * 16;
                int z = (event.getChunk().getZ() + random.nextInt(0,16)) * 16;
                int y = random.nextInt(0,128) * 16;
                Clipboard load = loadSchematic(ritualschem);
                World adapter = BukkitAdapter.adapt(Bukkit.getWorld("void"));
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(adapter)) {
                    Operation operation = new ClipboardHolder(load)
                            .createPaste(editSession)
                            .to(BlockVector3.at(x, y, z))
                            // configure here
                            .build();
                    Operations.complete(operation);
                }

            }
        }
    }


    public void createVoid() {
        WorldCreator worldCreator = new WorldCreator("void");
        worldCreator.generator(new voidGenerator());
        worldCreator.createWorld();
        Objects.requireNonNull(Bukkit.getWorld("void")).setTime(17000);
        Objects.requireNonNull(Bukkit.getWorld("void")).setStorm(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                Objects.requireNonNull(Bukkit.getWorld("void")).setTime(17000);
                Util.drawEye(new Location(Bukkit.getWorld("void"),0,-63,0),1);
            }
        }.runTaskTimer(this,0,20);
    }
}
