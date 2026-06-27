package xyz.yaszu.freedom;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.kyori.adventure.title.Title;
import net.skinsrestorer.api.SkinsRestorerProvider;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import xyz.yaszu.freedom.Alchemy.Alchemy;
import xyz.yaszu.freedom.Alchemy.MazeManager;
import xyz.yaszu.freedom.Blocks.BlockHandler;
import xyz.yaszu.freedom.Commands.DevTools.openGui;
import xyz.yaszu.freedom.Commands.Trust;
import xyz.yaszu.freedom.GUI.SelectionGUI.UltraselectionUi;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi;
import xyz.yaszu.freedom.GUI.SettingsGui.SettingsMenu;
import xyz.yaszu.freedom.GUI.SettingsGui.TrustMenu;
import xyz.yaszu.freedom.Information.Information_Handler;
import xyz.yaszu.freedom.Items.Artifacts.ArtifactManager;
import xyz.yaszu.freedom.Items.ItemListener;
import xyz.yaszu.freedom.Items.Parts.Grapple_Hook;
import xyz.yaszu.freedom.Items.Parts.ScythePhighting;
import xyz.yaszu.freedom.Items.Relics.PainScythe;
import xyz.yaszu.freedom.Items.Swords.Items.Firebrand;
import xyz.yaszu.freedom.Soul.Base.BaseBlack;
import xyz.yaszu.freedom.Soul.Base.BaseOrange;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.Ultra.Mocha;
import xyz.yaszu.freedom.Soul.Ultra.Orange;
import xyz.yaszu.freedom.Subsystems.*;
import xyz.yaszu.freedom.Subsystems.WorldManager;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Util.*;

import java.util.*;

import static xyz.yaszu.freedom.Items.Swords.VisionHandler.randomVisions;
import static xyz.yaszu.freedom.Soul.Ultra.Green.removeOldFollowers;
import static xyz.yaszu.freedom.Util.Util.*;

public final class Freedom extends JavaPlugin implements Listener {

    public static int version = 6942067;
    private BlockHandler blockHandler;

    /**
     * Main Initialization for Reapplying Curse Weakness
     * @param player
     */
    public void reapplyCurseWeakness(Player player) {
        if (player == null || !player.isOnline()) return;

        if (!player.getPersistentDataContainer().has(keygen("cursed"), PersistentDataType.STRING)) return;
        if (!"Frog".equals(player.getPersistentDataContainer().get(keygen("cursed"), PersistentDataType.STRING))) return;

        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 0, true, false));
    }



    @EventHandler
    public void onVillagerStockRunOut(EntityInteractEvent event) {
        Villager villager = (event.getEntity() instanceof Villager) ? (Villager) event.getEntity() : null;
        if (villager != null) {
            getLogger().info("RESTOCK");
            villager.restock();
        }
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
            AdminEffects(!event.getPlayer().getName().equals("TheAntiClock"), event.getPlayer().getLocation().getWorld(), event.getPlayer().getLocation(), event.getPlayer());
        }
        clearAura(event.getPlayer().getUniqueId());
        Util.clearPlayerCache(event.getPlayer().getUniqueId());
        cachedPdcValues.remove(event.getPlayer().getUniqueId());
        lastPdcRead.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event){
        AdminManager.handleJoin(event.getPlayer());
        if (AdminManager.isSudo(event.getPlayer())) {
            AdminManager.AdminProfile profile = AdminManager.getProfile(event.getPlayer().getUniqueId());
            if (profile != null) {
                event.joinMessage(Component.text(profile.sudoName() + " has joined", NamedTextColor.YELLOW));
            }
            AdminEffects(!event.getPlayer().getName().equals("TheAntiClock"), event.getPlayer().getLocation().getWorld(), event.getPlayer().getLocation(), event.getPlayer());
        }
        removeOldFollowers();
        event.getPlayer().performCommand("rules");
        event.getPlayer().getPersistentDataContainer().set(FreedomKeys.spriteActive(),PersistentDataType.BOOLEAN,false);
        ensureAura(event.getPlayer());
    }

    private static void AdminEffects(boolean event, org.bukkit.World event1, Location event2, Player event3) {
        if (event) {
            event1.strikeLightningEffect(event2);
        } else {
            Collection<Player> type = event3.getLocation().getNearbyEntitiesByType(Player.class, 10);
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

    public static ItemStack auraItem(SoulTypes type) {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("soul_aura-" + type.toBaseVariant().toString().toLowerCase()));
        item.setItemMeta(meta);
        return item;
    }

    public static HashMap<UUID, ItemDisplay> soulAuras = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> auraTasks = new HashMap<>();

    private static final Map<UUID, Long> lastPdcRead = new HashMap<>();
    private static final Map<UUID, Map<String, Object>> cachedPdcValues = new HashMap<>();

    private static void clearAura(UUID uuid) {
        BukkitRunnable task = auraTasks.remove(uuid);
        if (task != null) task.cancel();

        ItemDisplay display = soulAuras.remove(uuid);
        if (display != null && display.isValid()) {
            display.remove();
        }
    }

    private static void ensureAura(Player player) {
        UUID uuid = player.getUniqueId();
        ItemDisplay display = soulAuras.get(uuid);

        if (display == null || !display.isValid()) {
            clearAura(uuid);
            SoulTypes type = getSoulType(player);
            display = player.getLocation().getWorld().spawn(player.getLocation(), ItemDisplay.class);
            display.setItemStack(auraItem(type));
            display.setPersistent(false);
            soulAuras.put(uuid, display);

            Util.hideEntityFromPlayer(player, display);
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                Util.hideEntityFromPlayer(viewer, display);
            }
        }

        if (!auraTasks.containsKey(uuid)) {
            BukkitRunnable task = aura(player);
            auraTasks.put(uuid, task);
            task.runTaskTimer(Freedom.get_plugin(), 0, 4);
        }
    }




    public static BukkitRunnable aura(Player player) {
        ItemDisplay display = soulAuras.get(player.getUniqueId());
        if (display == null) {
            return new BukkitRunnable() {
                @Override
                public void run() {
                    this.cancel();
                }
            };
        }
        display.setTeleportDuration(2);
        display.setTransformation(new Transformation(
                new Vector3f(0,0,0),
                display.getTransformation().getLeftRotation(),
                new Vector3f(4,4,4),
                display.getTransformation().getRightRotation()
        ));
        return new BukkitRunnable() {
            @Override
            public void run() {
                ItemDisplay current = soulAuras.get(player.getUniqueId());
                if (current == null || !current.isValid() || !player.isOnline() || !player.isValid()) {
                    clearAura(player.getUniqueId());
                    this.cancel();
                    return;
                }
                current.teleport(player.getLocation().clone().add(0,2,0));
            }
        };

    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (AdminManager.isSudo(event.getPlayer())) {
            AdminEffects(!event.getPlayer().getName().equals("TheAntiClock"), event.getPlayer().getLocation().getWorld(), event.getPlayer().getLocation(), event.getPlayer());

        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (AdminManager.isSudo(event.getPlayer())) {
            AdminEffects(!event.getPlayer().getName().equals("TheAntiClock"), event.getPlayer().getLocation().getWorld(), event.getPlayer().getLocation(), event.getPlayer());
        }
        ensureAura(event.getPlayer());
        Life_and_Death.updateAllVisibility(event.getPlayer());
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
        Bukkit.getPluginManager().registerEvents(new selectionUi(),this);
        Bukkit.getPluginManager().registerEvents(new black_flash(),this);
        Bukkit.getPluginManager().registerEvents(new Life_and_Death(), this);
        Bukkit.getPluginManager().registerEvents(new ChatManager(), this);
        //Bukkit.getPluginManager().registerEvents(new Black(),this);
        Bukkit.getPluginManager().registerEvents(new TabDistance(), this);
        Bukkit.getPluginManager().registerEvents(new Mocha(), this);
        Bukkit.getPluginManager().registerEvents(new Firebrand(),this);
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
        Bukkit.getPluginManager().registerEvents(new RandomChestGenerator(), this);
        Bukkit.getPluginManager().registerEvents(new SettingsMenu(), this);
        Bukkit.getPluginManager().registerEvents(new TrustMenu(), this);
        blockHandler = new BlockHandler();
        Bukkit.getPluginManager().registerEvents(blockHandler,this);
        Bukkit.getPluginManager().registerEvents(new xyz.yaszu.freedom.GUI.SettingsGui.TrustMemberMenu(), this);
        Bukkit.getPluginManager().registerEvents(new xyz.yaszu.freedom.Subsystems.SitManager(), this);
        Bukkit.getPluginManager().registerEvents(new xyz.yaszu.freedom.Subsystems.ProvinceManager(), this);
        Bukkit.getPluginManager().registerEvents(new AlcoholManager(), this);
        Bukkit.getPluginManager().registerEvents(new VoidManager(),this);
        Bukkit.getPluginManager().registerEvents(new WorldManager(), this);
        Bukkit.getPluginManager().registerEvents(new ScythePhighting(),this);
        Bukkit.getPluginManager().registerEvents(new BulletSystem(),this);
        Bukkit.getPluginManager().registerEvents(new NpcManager(), this);
        Bukkit.getPluginManager().registerEvents(new CustomSongHandler(), this);
        Bukkit.getPluginManager().registerEvents(new Grapple_Hook(), this);
        Bukkit.getPluginManager().registerEvents(new GatewayListener(), this);
        Bukkit.getPluginManager().registerEvents(new RobloxCrossplay(), this);
        soulImbueManager = new SoulImbueManager();
        Bukkit.getPluginManager().registerEvents(soulImbueManager, this);
        DuelManager duelManager = new DuelManager();
        Bukkit.getPluginManager().registerEvents(duelManager, this);
        ArtifactManager artifactManager = new ArtifactManager();
        Bukkit.getPluginManager().registerEvents(artifactManager, this);
        artifactManager.startTask();
        Bukkit.getPluginManager().registerEvents(new BackpackManager(), this);
        xyz.yaszu.freedom.Subsystems.ProvinceManager.loadProvinces();
        TradeManager tradeManager = new TradeManager();
        Bukkit.getPluginManager().registerEvents(tradeManager, this);
        this.getLogger().info("---Registered Listeners!---");
        //Register Commands
        openGui openGui = new openGui();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
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
            commands.registrar().register(Trust.processChunksArgument());
            commands.registrar().register(Trust.interruptRitualArgument());
            commands.registrar().register(Trust.spawnStructureArgument());
            commands.registrar().register(Trust.undoArgument());
            commands.registrar().register(Trust.hatArgument());
            commands.registrar().register(Trust.sudoArgument());
            commands.registrar().register(Trust.skyArgument());
            commands.registrar().register(Trust.redcastle());
            commands.registrar().register(Trust.disableAll());
            commands.registrar().register(Trust.broadcast());
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
            commands.registrar().register(Trust.reset());
        });
        removeOldFollowers();
        start_time = System.currentTimeMillis();
        createVoid();
        createDoubleVoid();
        Bukkit.getPluginManager().registerEvents(new BackroomsManager(this), this);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    blockHandler.restore(world);
                }
            }
        }.runTaskLater(this, 20L);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    xyz.yaszu.freedom.Soul.soulListener.showSoulPoints(player);
                }
            }
        }.runTaskTimer(this, 0, 100);
        MazeManager.createMazeWorld("backrooms");
        randomVisions();
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL,
                PacketType.Play.Server.SPAWN_ENTITY,
                PacketType.Play.Server.NAMED_ENTITY_SPAWN,
                PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player receiver = event.getPlayer();
                Entity entity = event.getPacket().getEntityModifier(event).read(0);

                if (entity != null && Util.hiddenEntities.contains(entity.getUniqueId())) {
                    // Hide this entity from this specific player
                    event.setCancelled(true);
                }
            }
        });
        new BukkitRunnable() {
            @Override
            public void run() {
                new CustomEvents.TimeChangeEvent(Objects.requireNonNull(Bukkit.getWorld("world")).getTime()).callEvent();
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 20);
        new BukkitRunnable() {
            @Override
            public void run() {
                //get current time
                
            }
        }.runTaskTimer(Freedom.get_plugin(),10,0);
        crossplay.init();
    }
    public static RobloxCrossplay crossplay = new RobloxCrossplay();
    private ProtocolManager protocolManager;

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
        for (BukkitRunnable task : new ArrayList<>(auraTasks.values())) {
            task.cancel();
        }
        auraTasks.clear();
        for (ItemDisplay display : new ArrayList<>(soulAuras.values())) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        soulAuras.clear();
        Bukkit.getScheduler().cancelTasks(this);
        // Plugin shutdown logic
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
