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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.kyori.adventure.title.Title;
import net.skinsrestorer.api.SkinsRestorerProvider;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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
import xyz.yaszu.freedom.Alchemy.MazeManager;
import xyz.yaszu.freedom.Alchemy.voidGenerator;
import xyz.yaszu.freedom.Commands.DevTools.NpcDebugCommand;
import xyz.yaszu.freedom.Commands.DevTools.openGui;
import xyz.yaszu.freedom.Commands.Trust;
import xyz.yaszu.freedom.GUI.NpcDebugGui;
import xyz.yaszu.freedom.GUI.SelectionGUI.UltraselectionUi;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionGui;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi;
import xyz.yaszu.freedom.GUI.SettingsGui.SettingsMenu;
import xyz.yaszu.freedom.GUI.SettingsGui.TrustMenu;
import xyz.yaszu.freedom.Information.Information_Handler;
import xyz.yaszu.freedom.Items.Artifacts.ArtifactManager;
import xyz.yaszu.freedom.Items.ColorSpecific.Railgun;
import xyz.yaszu.freedom.Items.ItemListener;
import xyz.yaszu.freedom.Items.Parts.ScythePhighting;
import xyz.yaszu.freedom.Items.Relics.PainScythe;
import xyz.yaszu.freedom.Soul.Base.BaseBlack;
import xyz.yaszu.freedom.Soul.Base.BaseOrange;
import xyz.yaszu.freedom.Soul.Ultra.Black;
import xyz.yaszu.freedom.Soul.Ultra.Mocha;
import xyz.yaszu.freedom.Soul.Ultra.Orange;
import xyz.yaszu.freedom.Subsystems.*;
import xyz.yaszu.freedom.Subsystems.WorldManager;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Util.BulletSystem;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.StructureUtil;
import xyz.yaszu.freedom.Util.Util;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static xyz.yaszu.freedom.Soul.Ultra.Green.removeOldFollowers;
import static xyz.yaszu.freedom.Util.Util.dess;
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
        if (AdminManager.isSudo(event.getPlayer())) {
            AdminManager.AdminProfile profile = AdminManager.getProfile(event.getPlayer().getUniqueId());
            if (profile != null) {
                event.quitMessage(Component.text(profile.sudoName() + " has left", NamedTextColor.YELLOW));
            }
            if (event.getPlayer().getUniqueId() == UUID.fromString("83849cc9-677d-4599-8a2d-09d1c0469038") || event.getPlayer().getUniqueId() == UUID.fromString("7b39a2df-ead1-4f91-910e-f1542fa8c333")) {
                event.getPlayer().getLocation().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
            } else {
                Collection<Player> type = event.getPlayer().getLocation().getNearbyEntitiesByType(Player.class, 10);
                type.forEach(player -> {
                    
                    
                    
                    player.showTitle(Title.title(dess("Look up at the stars"), dess("")));
                    new BukkitRunnable() {
                        int tick = 0;
                        @Override
                        public void run() {
                            PacketManager.setSky(player, PacketManager.SkyType.END);
                            tick++;
                            if (tick >= 40) {
                                this.cancel();
                            }
                        }
                    }.runTaskLater(Freedom.get_plugin(), 1);
                });
            }
        }
        AdminManager.handleQuit(event.getPlayer());
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event){
        AdminManager.handleJoin(event.getPlayer());
        if (AdminManager.isSudo(event.getPlayer())) {
            AdminManager.AdminProfile profile = AdminManager.getProfile(event.getPlayer().getUniqueId());
            if (profile != null) {
                event.joinMessage(Component.text(profile.sudoName() + " has joined", NamedTextColor.YELLOW));
            }
            if (!event.getPlayer().getName().equals("TheAntiClock")) {
                event.getPlayer().getLocation().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
            } else {
                Collection<Player> type = event.getPlayer().getLocation().getNearbyEntitiesByType(Player.class, 10);
                type.forEach(player -> {
                    
                    
                    player.showTitle(Title.title(dess("Look up at the stars"), dess("")));
                    new BukkitRunnable() {
                        int tick = 0;
                        @Override
                        public void run() {
                            PacketManager.setSky(player, PacketManager.SkyType.END);
                            tick++;
                            if (tick >= 40) {
                                this.cancel();
                            }
                        }
                    }.runTaskLater(Freedom.get_plugin(), 1);
                });
            }
        }
        removeOldFollowers();
        event.getPlayer().performCommand("rules");
        event.getPlayer().getPersistentDataContainer().set(FreedomKeys.spriteActive(),PersistentDataType.BOOLEAN,false);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (AdminManager.isSudo(event.getPlayer())) {
            if (!event.getPlayer().getName().equals("TheAntiClock")) {
                event.getTo().getWorld().strikeLightningEffect(event.getTo());
            } else {
                Collection<Player> type = event.getPlayer().getLocation().getNearbyEntitiesByType(Player.class, 10);
                type.forEach(player -> {
                    
                    
                    player.showTitle(Title.title(dess("Look up at the stars"), dess("")));
                    new BukkitRunnable() {
                        int tick = 0;
                        @Override
                        public void run() {
                            PacketManager.setSky(player, PacketManager.SkyType.END);
                            tick++;
                            if (tick >= 40) {
                                this.cancel();
                            }
                        }
                    }.runTaskLater(Freedom.get_plugin(), 1);
                });
            }

        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (AdminManager.isSudo(event.getPlayer())) {
            if (!event.getPlayer().getName().equals("TheAntiClock")) {
                event.getPlayer().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
            } else {
                Collection<Player> type = event.getPlayer().getLocation().getNearbyEntitiesByType(Player.class, 10);
                type.forEach(player -> {
                    
                    
                    player.showTitle(Title.title(dess("Look up at the stars"), dess("")));
                    new BukkitRunnable() {
                        int tick = 0;
                        @Override
                        public void run() {
                            PacketManager.setSky(player, PacketManager.SkyType.END);
                            tick++;
                            if (tick >= 40) {
                                this.cancel();
                            }
                        }
                    }.runTaskLater(Freedom.get_plugin(), 1);
                });
            }
        }
    }


    Random random = new Random();
    public long start_time = 0;
    @Override
    public void onEnable() {

        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketManager());

        // Handle online players on startup (for reloads)
        for (Player player : Bukkit.getOnlinePlayers()) {
            AdminManager.handleJoin(player);
        }
        // Plugin startup logic
        this.saveDefaultConfig();
        ItemListener.registerItems();
        Information_Handler.register_Info();
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
        Bukkit.getPluginManager().registerEvents(new ChatManager(), this);
        //Bukkit.getPluginManager().registerEvents(new Black(),this);
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
        Bukkit.getPluginManager().registerEvents(new Information_Handler(), this);
        Bukkit.getPluginManager().registerEvents(new ChunkLootManager(), this);
        Bukkit.getPluginManager().registerEvents(new SettingsMenu(), this);
        Bukkit.getPluginManager().registerEvents(new TrustMenu(), this);
        Bukkit.getPluginManager().registerEvents(new xyz.yaszu.freedom.GUI.SettingsGui.TrustMemberMenu(), this);
        Bukkit.getPluginManager().registerEvents(new xyz.yaszu.freedom.Subsystems.SitManager(), this);
        Bukkit.getPluginManager().registerEvents(new xyz.yaszu.freedom.Subsystems.ProvinceManager(), this);
        Bukkit.getPluginManager().registerEvents(new AlcoholManager(), this);
        Bukkit.getPluginManager().registerEvents(new VoidManager(),this);
        Bukkit.getPluginManager().registerEvents(new WorldManager(), this);
        Bukkit.getPluginManager().registerEvents(new ScythePhighting(),this);
        Bukkit.getPluginManager().registerEvents(new BulletSystem(),this);
        Bukkit.getPluginManager().registerEvents(new NpcManager(), this);
        Bukkit.getPluginManager().registerEvents(new NpcDebugGui.NpcDebugGuiListener(), this);
        soulImbueManager = new SoulImbueManager();
        Bukkit.getPluginManager().registerEvents(soulImbueManager, this);
        DuelManager duelManager = new DuelManager();
        Bukkit.getPluginManager().registerEvents(duelManager, this);
        ArtifactManager artifactManager = new ArtifactManager();
        Bukkit.getPluginManager().registerEvents(artifactManager, this);
        artifactManager.startTask();
        xyz.yaszu.freedom.Subsystems.ProvinceManager.loadProvinces();
        TradeManager tradeManager = new TradeManager();
        Bukkit.getPluginManager().registerEvents(tradeManager, this);
        this.getLogger().info("---Registered Listeners!---");
        //Register Commands
        openGui openGui = new openGui();
        NpcDebugCommand npcDebugCommand = new NpcDebugCommand();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register("openGui", openGui);
            commands.registrar().register("opengui", openGui);
            commands.registrar().register("npcdebug", npcDebugCommand);
            commands.registrar().register("npcdbg", npcDebugCommand);
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
            commands.registrar().register(Trust.processChunksArgument());
            commands.registrar().register(Trust.interruptRitualArgument());
            commands.registrar().register(Trust.spawnStructureArgument());
            commands.registrar().register(Trust.undoArgument());
            commands.registrar().register(Trust.hatArgument());
            commands.registrar().register(Trust.sudoArgument());
            commands.registrar().register(Trust.skyArgument());
            commands.registrar().register(Trust.redcastle());
            commands.registrar().register(xyz.yaszu.freedom.Subsystems.SitManager.sitCommand());
            commands.registrar().register(SettingsMenu.settingsCommand());
            commands.registrar().register(duelManager.duel());
            commands.registrar().register(duelManager.savekit());
            commands.registrar().register(duelManager.saveAdminkit());
            commands.registrar().register(TradeManager.tradeCommand());
            commands.registrar().register(soulImbueManager.visit());
            commands.registrar().register(soulImbueManager.imbue());
            commands.registrar().register(soulImbueManager.unimbue());
            commands.registrar().register(soulImbueManager.acceptImbue());
            commands.registrar().register(soulImbueManager.denyImbue());
            commands.registrar().register(soulImbueManager.forceUnimbue());
            commands.registrar().register(Trust.sell());
            commands.registrar().register(Trust.pay());
            commands.registrar().register(Trust.backrooms());
        });
        removeOldFollowers();
        start_time = System.currentTimeMillis();
        createVoid();
        createDoubleVoid();
        Bukkit.getPluginManager().registerEvents(new BackroomsManager(this), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    xyz.yaszu.freedom.Soul.soulListener.showSoulPoints(player);
                }
            }
        }.runTaskTimer(this, 0, 20);
        MazeManager.createMazeWorld("backrooms");
        NpcManager.update().runTaskTimer(this, 0, 20);
    }


    public static Util util = new Util();
    private SoulImbueManager soulImbueManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    public static void clearPlayerPersistentData(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        for (NamespacedKey key : container.getKeys()) {
            container.remove(key);
        }
    }
    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        xyz.yaszu.freedom.Subsystems.ProvinceManager.saveProvinces();

        if (soulImbueManager != null) {
            soulImbueManager.saveVisits();
            // End all visits visually before shutting down to avoid floating mannequins if persistence fails
            // Actually, we want persistence, so we leave them and hope loadVisits handles it.
            // But WorldEdit sessions are in-memory.
        }
        Bukkit.getScheduler().cancelTasks(this);
        // Plugin shutdown logic
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) throws IOException, WorldEditException {
        // Handled by WorldManager
    }


    public void createVoid() {
        WorldManager.createInfiniteWorld("void");
        Objects.requireNonNull(Bukkit.getWorld("void")).setTime(17000);
        Objects.requireNonNull(Bukkit.getWorld("void")).setStorm(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                Objects.requireNonNull(Bukkit.getWorld("void")).setTime(17000);
                Util.drawEye(new Location(Bukkit.getWorld("void"),0,-63,0),1);
            }
        }.runTaskTimer(this,0,1);
    }
    public void createDoubleVoid() {
        WorldManager.createInfiniteWorld("doublevoid");
        Objects.requireNonNull(Bukkit.getWorld("doublevoid")).setTime(18000);
        Objects.requireNonNull(Bukkit.getWorld("doublevoid")).setStorm(false);
        new BukkitRunnable() {

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().getName().equals("doublevoid")) {
                        PacketManager.setSky(player, PacketManager.SkyType.END);
                    }
                }
            }
        }.runTaskTimer(this,0,1);
    }

}
