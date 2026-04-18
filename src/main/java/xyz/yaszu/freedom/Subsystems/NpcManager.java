package xyz.yaszu.freedom.Subsystems;

import com.github.javafaker.Faker;
import com.destroystokyo.paper.profile.PlayerProfile;
import de.bsommerfeld.pathetic.api.factory.PathfinderFactory;
import de.bsommerfeld.pathetic.api.pathing.Pathfinder;
import de.bsommerfeld.pathetic.api.pathing.configuration.PathfinderConfiguration;
import de.bsommerfeld.pathetic.api.wrapper.PathPosition;
import de.bsommerfeld.pathetic.bukkit.context.BukkitEnvironmentContext;
import de.bsommerfeld.pathetic.bukkit.mapper.BukkitMapper;
import de.bsommerfeld.pathetic.bukkit.provider.LoadingNavigationPointProvider;
import de.bsommerfeld.pathetic.engine.factory.AStarPathfinderFactory;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.CopypartyClient;
import xyz.yaszu.freedom.Util.Util;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public class NpcManager extends Util implements Listener {

    public static Faker faker = new Faker();
    private static final CopypartyClient COPYPARTY_CLIENT = new CopypartyClient();
    private static final int MAX_PROFILE_NAME_LENGTH = 16;

    private static final List<String> SKINS = List.of(
            "https://s.namemc.com/i/7c2b160450e0839c.png",
            "https://s.namemc.com/i/f7998f18d571ff92.png",
            "https://s.namemc.com/i/a55fc6de61f6bf4a.png",
            "https://s.namemc.com/i/5587a028623bec8a.png"
    );

    public static String getRandomNpcSkin() {
        return getRandomNpcSkin(COPYPARTY_CLIENT);
    }

    static String getRandomNpcSkin(CopypartyClient copypartyClient) {
        return copypartyClient.fetchRandomSkinOrFallback(SKINS);
    }

    public void spawnNpc(Location location) {
        Mannequin mannequin = location.getWorld().spawn(location, Mannequin.class);
        Npc npc = new Npc();
        npc.npc = mannequin;

        npc.name = faker.funnyName().name();
        npc.skin = getRandomNpcSkin();

        applyNpcProfile(mannequin, npc.name, npc.skin);
    }

    private static void applyNpcProfile(Mannequin mannequin, String name, String skinUrl) {
        try {
            mannequin.setProfile(buildProfile(name, skinUrl));
        } catch (Exception ex) {
            // If skin URL parsing/profile build fails, keep a valid profile with just the generated name.
            mannequin.setProfile(buildProfile(name, null));
        }

        mannequin.setCustomName(name);
        mannequin.setCustomNameVisible(true);
    }

    private static ResolvableProfile buildProfile(String rawName, String skinUrl) {
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), sanitizeProfileName(rawName));
        if (skinUrl != null && !skinUrl.isBlank()) {
            try {
                profile.getTextures().setSkin(URI.create(skinUrl).toURL());
            } catch (IllegalArgumentException | MalformedURLException ex) {
                throw new IllegalArgumentException("Invalid skin URL for NPC profile: " + skinUrl, ex);
            }
        }

        return ResolvableProfile.resolvableProfile(profile);
    }

    private static String sanitizeProfileName(String rawName) {
        String normalized = rawName == null ? "FreedomNPC" : rawName.replaceAll("[^A-Za-z0-9_]", "_");
        if (normalized.isBlank()) {
            normalized = "FreedomNPC";
        }
        return normalized.length() > MAX_PROFILE_NAME_LENGTH
                ? normalized.substring(0, MAX_PROFILE_NAME_LENGTH)
                : normalized;
    }

    public void NpcThink(Mannequin npc) {
        new BukkitRunnable() {
            @Override
            public void run() {
                //Calculate Goals

            }
        }.runTaskLater(Freedom.get_plugin(),1);
    }

    public static enum goals {
        Eat,
        Rest,
        Farm,
        Hunt,
        Build,
        Socialize,
        Mine
    }
    public class Npc {
        public Mannequin npc;
        public String name;
        public String skin;
        public goals goal;


    }

    private void findPath(Location start, Location target,Mannequin mannequin) {

        PathPosition startPos = BukkitMapper.toPathPosition(start);
        PathPosition targetPos = BukkitMapper.toPathPosition(target);
        World world = start.getWorld();

        pathfinder.findPath(startPos, targetPos, new BukkitEnvironmentContext(world))
                .ifPresent(result -> {

                    // We have an usable result since it either found the path, or fallen back.
                    result.getPath().forEach(position -> {
                        Location location = BukkitMapper.toLocation(position, world);
                        // Do something with it.
                    });

                }).orElse(ignored -> {
                    // Handle no path found scenario
                    System.out.println("No path found between start and target positions.");

                }).exceptionally(ex -> System.err.println("An exception occurred -> " + ex));
    }

    PathfinderFactory factory = new AStarPathfinderFactory();

    // Configure the pathfinder
    PathfinderConfiguration configuration = PathfinderConfiguration.builder()
            .provider(new LoadingNavigationPointProvider())
            .async(true)
            .maxIterations(100_000_000)
            .build();

    // Create the pathfinder instance
    Pathfinder pathfinder = factory.createPathfinder(configuration);

}
