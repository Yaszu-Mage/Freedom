package xyz.yaszu.freedom.Util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.connections.MineSkinAPI;
import net.skinsrestorer.api.connections.model.MineSkinResponse;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.property.SkinVariant;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;
import org.bukkit.*;
import org.bukkit.block.Lectern;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Blocks.BlockHandler;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi;
import xyz.yaszu.freedom.Soul.Base.BaseRed;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Subsystems.CombatTimer;
import xyz.yaszu.freedom.Subsystems.TrustManager;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static xyz.yaszu.freedom.Blocks.BlockHandler.restoreRotation;
import static xyz.yaszu.freedom.Soul.soulListener.getSoul;
import static xyz.yaszu.freedom.Subsystems.CurrencyManager.getCurrency;
/**
 * class for containing misc methods for reference inside other classes.
 *
 */
/*
Dear Reviewers,
I apologize in advance, this file is hell. It's a bunch of methods I made,and used throughout the entire project
Welcome to my own personal hell, I suck at vector math. Good luck godspeed.
- Yaszu
 */
public class Util {
    /**
     * Get the target Entity a player is looking at
     *
     * @param player that is looking at the entity
     * @return Entity that the player is looking At
     * @deprecated
     * @since 1.0.0
     */
    @Deprecated
    public static Entity getTargetEntity(Player player) {
        int range = 10; // Detection range in blocks
        Entity target = null;
        double targetDistanceSq = range * range;

        Location eyeLocation = player.getEyeLocation();
        Vector lookDirection = eyeLocation.getDirection().normalize();

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity == player) continue; // Skip the player themselves

            Vector toEntity = entity.getLocation().subtract(eyeLocation).toVector();
            double dotProduct = lookDirection.dot(toEntity.normalize());

            // The closer the dot product is to 1.0, the more directly the player is looking at it
            if (dotProduct > 0.99) {
                double distanceSq = eyeLocation.distanceSquared(entity.getLocation());
                if (distanceSq < targetDistanceSq) {
                    target = entity;
                    targetDistanceSq = distanceSq;
                }
            }
        }
        return target;
    }

    /**
     * Get's a Random Potion Effect Type
     * @return Random Potion Effect Type
     */
    public static PotionEffectType randomPotionEffect(){
        PotionEffectType[] types = PotionEffectType.values();
        Random random = new Random();
        return types[random.nextInt(types.length)];
    }

    /**
     * Retrieves an array of chunks near the specified location within the given range.
     *
     * @param location the central location from which nearby chunks are determined
     * @param range the distance in chunks to search for nearby chunks
     * @return an array of chunks located within the specified range of the given location
     */
    public static Chunk[] getNearbyChunks(Location location,int range) throws ExecutionException, InterruptedException, TimeoutException {
        World world = location.getWorld();
        if (world == null) return new Chunk[0];
        range = range * 16;
        int centerX = location.getChunk().getX();
        int centerZ = location.getChunk().getZ();
        List<Chunk> nearbyChunks = new ArrayList<>();
        for (int x = centerX - range; x <= centerX + range; x++) {
            for (int z = centerZ - range; z <= centerZ + range; z++) {
                nearbyChunks.add(world.getChunkAtAsync(x, z).get(1000, TimeUnit.MILLISECONDS));
            }
        }
        return nearbyChunks.toArray(new Chunk[0]);
    }


    /**
     * Get's the SoulPoints of a Currenly Selected Player
     * @param player who is currently selected
     * @return Integer of the SoulPoints of the player
     */
    public static int getSoulPoints(Player player) {
        Double SoulPoints;
        try {
            SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        } catch (Exception e) {
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, 0d);
            return 0;
        }
        return SoulPoints.intValue();
    }

    /**
     * Converts and Item Into a string
     * @param item that is being converted into a string
     * @return string of the item in Yaml format
     */
    public static String itemToString(ItemStack item) {
        if (item == null) return "";
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item);
        String yaml = config.saveToString();
        return Base64.getEncoder().encodeToString(yaml.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Converts a String into an Item
     * @param data that is being converted into an item, it's in standard yaml format
     * @return ItemStack of the item that was converted from the string
     */
    public static ItemStack stringToItem(String data) {
        if (data == null || data.isEmpty()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            String yaml = new String(bytes, StandardCharsets.UTF_8);
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(yaml);
            ItemStack item = config.getItemStack("item");
            if (item == null) {
                Freedom.get_plugin().getLogger().warning("[BlackInformation] stringToItem: deserialized null item from data: " + data.substring(0, Math.min(data.length(), 40)));
            }
            return item;
        } catch (IllegalArgumentException e) {
            // Not valid Base64 — try legacy plain-YAML path for data saved before this fix.
            try {
                YamlConfiguration config = new YamlConfiguration();
                config.loadFromString(data);
                return config.getItemStack("item");
            } catch (InvalidConfigurationException ex) {
                Freedom.get_plugin().getLogger().warning("[BlackInformation] stringToItem: failed to parse item data — " + ex.getMessage());
                return null;
            }
        } catch (InvalidConfigurationException e) {
            Freedom.get_plugin().getLogger().warning("[BlackInformation] stringToItem: invalid YAML — " + e.getMessage());
            return null;
        }
    }


    /**
     * Converts a location into a string, which is stored "x,y,z"
     * @param loc Location that is being converted into a string
     * @return String of the location in the format "world,x,y,z"
     */
    public static String locationToString(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    /**
     * Converts a string into a list of locations
     * @param loc String that is being converted into a list of locations
     * @return locations that were converted from the string
     */
    public static ArrayList<Location> stringToLocations(String loc) {
        ArrayList<Location> locations = new ArrayList<>();
        if (loc == null || loc.isEmpty()) return locations;
        String[] parts = loc.split("-");
        for (String entry : parts) {                          // FIX: iterate `parts`, not reassign it
            if (entry == null || entry.isEmpty()) continue;   // FIX: skip spurious empty entries
            String[] fields = entry.split(",");               // FIX: separate variable, not `parts`
            if (fields.length == 4) {
                World world = Bukkit.getWorld(fields[0]);     // getName()-compatible lookup
                if (world == null) continue;                  // FIX: skip unknown worlds gracefully
                try {
                    double x = Double.parseDouble(fields[1]);
                    double y = Double.parseDouble(fields[2]);
                    double z = Double.parseDouble(fields[3]);
                    locations.add(new Location(world, x, y, z));
                } catch (NumberFormatException e) {
                    Freedom.get_plugin().getLogger().warning("[BlackInformation] stringToLocations: bad coordinate in entry '" + entry + "'");
                }
            }
        }
        return locations;
    }

    /**
     * Get's a random Positive Potion Effect
     * @return Positive Potion Effect
     */
    public static PotionEffectType randomPositivePotionEffect(){
        PotionEffectType[] types = new PotionEffectType[] {
                PotionEffectType.ABSORPTION,
                PotionEffectType.REGENERATION,
                PotionEffectType.SPEED,
                PotionEffectType.CONDUIT_POWER,
                PotionEffectType.NIGHT_VISION,
                PotionEffectType.FIRE_RESISTANCE,
                PotionEffectType.WATER_BREATHING,
                PotionEffectType.INVISIBILITY,
                PotionEffectType.STRENGTH,
                PotionEffectType.SATURATION,
                PotionEffectType.JUMP_BOOST,
                PotionEffectType.LUCK,
                PotionEffectType.HASTE,
                PotionEffectType.INSTANT_HEALTH
        };
        Random random = new Random();
        return types[random.nextInt(types.length)];
    }

    /** Gets a random Negative Potion Effect
     *
     * @return Negative Potion Effect
     */
    public static PotionEffectType randomNegativePotionEffect(){
        PotionEffectType[] types = new PotionEffectType[] {
                PotionEffectType.POISON,
                PotionEffectType.BLINDNESS,
                PotionEffectType.WEAKNESS,
                PotionEffectType.SLOWNESS,
                PotionEffectType.HUNGER,
                PotionEffectType.WITHER,
                PotionEffectType.INFESTED,
                PotionEffectType.LEVITATION,
                PotionEffectType.GLOWING,
                PotionEffectType.INSTANT_DAMAGE,
                PotionEffectType.BAD_OMEN,
                PotionEffectType.SLOW_FALLING
        };
        Random random = new Random();
        return types[random.nextInt(types.length)];
    }

    /**
     * Plush Squish Animation
     * @param player player who is squishing the plush
     * @param event player interactevent that was triggered to squish the plush
     * @param baseBlock baseblock that was squished
     */
    public static void plush(Player player, PlayerInteractEvent event, BaseBlock baseBlock) {
        player.getWorld().playSound(player.getLocation(), (String) baseBlock.placeSound(), 10f, 1f);
        BlockHandler.BlockPos pos = BlockHandler.BlockPos.of(event.getClickedBlock().getLocation());

        UUID uuid = BlockHandler.currentCustomBlocks.get(pos);
        if (uuid != null) {
            ItemDisplay display = (ItemDisplay) Bukkit.getEntity(uuid);
            //lets do an animation
            float yaw;
            yaw = restoreRotation(Objects.requireNonNull(event.getInteractionPoint()));
            AxisAngle4f rotation = new AxisAngle4f((float) Math.toRadians(yaw), 0, -1, 0);
            new BukkitRunnable() {
                // single tick counter driving the whole animation (0..100)
                int tick = 0;
                @Override
                public void run() {
                    // when finished, ensure final transform and stop
                    if (tick > 10) {
                        display.setTransformation(new Transformation(
                                display.getTransformation().getTranslation(),
                                new Quaternionf(rotation),
                                new Vector3f(1f, 1f, 1f),
                                display.getTransformation().getRightRotation()
                        ));
                        this.cancel();
                        return;
                    }

                    // progress: 0..50 collapse from 1 -> 0, 51..100 expand from 0 -> 1
                    float yScale;
                    if (tick <= 5) {
                        yScale = 1f - (tick / 5f);
                    } else {
                        yScale = (tick - 5f) / 5f;
                    }

                    display.setTransformation(new Transformation(
                            display.getTransformation().getTranslation(),
                            new Quaternionf(rotation),
                            new Vector3f(1f, yScale, 1f),
                            display.getTransformation().getRightRotation()
                    ));

                    tick++;
                }
            }.runTaskTimer(Freedom.get_plugin(),0,0);
        } else {
            Freedom.get_plugin().getLogger().info("no uuid");
        }
    }
    public static SkinsRestorer skinsRestorerAPI;

    /**
     * Empty Item Constructor of a Base ItemStack
     * @param item inherits item type from this but set's display name
     * @return ItemStack of the item type but with the display name set to the empty item name
     */
    public static ItemStack emptyItem(ItemStack item) {
        ItemMeta workingMeta = item.getItemMeta();
        workingMeta.displayName(dess("\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33"));
        item.setItemMeta(workingMeta);
        return item;
    }

    /**
     * Creates a new Namespaced Key with a String "id"
     * @param key String Key ID to generate NamespacedKey
     * @return NamespacedKey of the key "id" from earlier
     */
    public static NamespacedKey keygen(String key) {
        return FreedomKeys.key(key);
    }

    /**
     * Gets the direction between two locations to create a vector line
     * @param start Start Location of the Vector Line
     * @param end End Location of the Vector Line
     * @return Vector of the direction from start to end
     */
    public static Vector directionTo(Location start, Location end) {
        return end.toVector().subtract(start.toVector()).normalize();
    }

    /**
     * Creates a component using Mini Message with a given string without the long
     * Minimessage.miniMessage().deserialize(String)
     * @param minimessage String of Minimessage you want to construct the component
     * @return Component Construction of the Minimessage String
     */
    public static Component dess(String minimessage) {
        return MiniMessage.miniMessage().deserialize(minimessage);
    }

    public static final Set<UUID> hiddenEntities = new HashSet<>();
    public static ProtocolManager protocolManager;

    private static final Map<UUID, Long> lastPdcRead = new HashMap<>();
    private static final Map<UUID, Map<String, Object>> cachedPdcValues = new HashMap<>();
    private static final long PDC_CACHE_DURATION = 1000; // 1 second in milliseconds

    /**
     * Retrieves a cached value associated with a player's Persistent Data Container (PDC).
     * If the value exists in the cache and is still valid, it is returned directly from the cache.
     * Otherwise, the value is fetched from the PDC, stored in the cache, and then returned.
     *
     * @param player The player whose PDC is being accessed.
     * @param key The key that identifies the specific value within the PDC.
     * @param type The data type of the value being fetched from the PDC.
     * @param <T> The primitive type the PersistentDataType maps from.
     * @param <Z> The object type the PersistentDataType maps to.
     * @return The value associated with the given key and type, or {@code null} if the key is not present in the PDC.
     */
    public static <T, Z> Z getCachedPdcValue(Player player, String key, PersistentDataType<T, Z> type) {
        UUID playerUUID = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (lastPdcRead.containsKey(playerUUID) && (now - lastPdcRead.get(playerUUID)) < PDC_CACHE_DURATION) {
            Map<String, Object> playerCache = cachedPdcValues.get(playerUUID);
            if (playerCache != null && playerCache.containsKey(key)) {
                try {
                    return (Z) playerCache.get(key);
                } catch (ClassCastException e) {
                    // Fall through to read from PDC if type is wrong
                }
            }
        }

        // If cache is invalid or missing, read from PDC
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (container.has(FreedomKeys.key(key), type)) {
            Z value = container.get(FreedomKeys.key(key), type);
            cachedPdcValues.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(key, value);
            lastPdcRead.put(playerUUID, now);
            return value;
        }

        return null;
    }

    /**
     * Gets midpoint between two locations
     * @param loc1 Location one to get midpoint
     * @param loc2 Location two to get midpoint
     * @return Midpoint Location between two locations
     */
    public static Location getMidpoint(Location loc1, Location loc2) {
        return loc1.add(loc2).multiply(0.5);
    }

    /**
     * Shows Entity to Player if that Entity was hidden using
     * @see #hideEntityFromPlayer(Player, Entity)
     * @param player player to show entity to
     * @param entity entity to show to player
     */
    public static void showEntityToPlayer(Player player, Entity entity) {
        if (entity == null || !entity.isValid() || player == null || !player.isOnline()) return;

        // remove from our hidden tracking
        hiddenEntities.remove(entity.getUniqueId());

        // Prefer Bukkit API showEntity if available
        try {
            // Paper / recent Bukkit has Player#showEntity(Plugin, Entity)
            player.showEntity(Freedom.get_plugin(), entity);
            return;
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
            // fallback to ProtocolLib approach
        }

        // Fallback: ask ProtocolLib to update/resend entity data to the player
        try {
            protocolManager = ProtocolLibrary.getProtocolManager();
            // try an update/refetch approach if supported
            protocolManager.updateEntity(entity, Collections.singletonList(player));
        } catch (Exception e) {
            Freedom.get_plugin().getLogger().warning("Failed to show entity via ProtocolLib: " + e);
        }
    }


    /**
     * Creates a Particle Pulse Circle, which is a circle that expands a couple times from a center point
     * @param center Location of the center of the circle
     * @param radius Radius of the circle
     * @param points Number of points to use for the circle
     * @param particle Particle to use for the circle
     * @param smallestRadius Smallest radius to use for the circle
     * @param pulsingTime Time to pulse the circle
     * @param pulses how many pulses to do
     * @param sound Sound to make while pulsing the circle
     * @param options Whatever freaking particle options you want to use
     */
    public static void pulseCircle(Location center, double radius, int points, Particle particle, double smallestRadius, int pulsingTime, int pulses,Sound sound,Object options) {
            new BukkitRunnable() {
                double ticks = 0;
                double currentRadius = smallestRadius;
                double step = radius / pulses;
                @Override
                public void run() {
                    if (ticks >= pulsingTime) {
                        this.cancel();
                        return;
                    }
                    if (options != null) {
                        if (options instanceof Particle.DustOptions) {
                            drawCircle(center, currentRadius, center.getWorld(), points, particle, (Particle.DustOptions) options);
                        }
                    } else {
                        drawCircle(center, currentRadius, center.getWorld(), points, particle);
                    }

                    if (ticks == 0) {
                        center.getWorld().playSound(center, sound, SoundCategory.MASTER, 1.0f, 1.0f);
                    }
                    currentRadius += step;
                    ticks += pulsingTime / pulses;
                }
            }.runTaskTimer(Freedom.get_plugin(),0,pulses/pulsingTime);
    }


    /**
     * Hiding an entity from the Player
     * @see #showEntityToPlayer(Player, Entity)
     * @param player player to hide entity from
     * @param entity entity to hide from player
     */
    public static void hideEntityFromPlayer(Player player, Entity entity) {
        if (entity == null || !entity.isValid() || player == null || !player.isOnline()) return;

        protocolManager = ProtocolLibrary.getProtocolManager();
        hiddenEntities.add(entity.getUniqueId());

        // Prefer Bukkit API hideEntity if available
        try {
            player.hideEntity(Freedom.get_plugin(), entity);
            return;
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
            // fallback to ProtocolLib approach
        }

        // Fallback: send ENTITY_DESTROY packet with the entity id
        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            // set payload to int array of entity ids (single id)
            packet.getIntegerArrays().write(0, new int[] { entity.getEntityId() });
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            Freedom.get_plugin().getLogger().warning("Failed to hide entity via ProtocolLib: " + e);
        }
    }

    /**
     * Drawing an Elipse using Particles
     * @param center Location of the center of the ellipse
     * @param radius Radius of the ellipse
     * @param points Number of points to use for the ellipse
     * @param particle Particle to use for the ellipse
     * @param linearscale Linear Scale of the ellipse
     * @param horizontalscale Horizontal Scale of the ellipse
     */
    public static void drawElipse(Location center, double radius, int points, Particle particle, double linearscale, double horizontalscale) {
        World world = center.getWorld();
        if (points <= 0) return;
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = (center.getX() + (horizontalscale * (radius * Math.cos(angle)))); // Calculate X coordinate
            double z = (center.getZ() + (linearscale * (radius * Math.sin(angle)))); // Calculate Z coordinate
            double y = center.getY(); // Y remains constant for a flat circle

            // Create a new location for the point
            Location pointLocation = new Location(world, x, y, z);
            // 2. Spawn particles
            if (particle == Particle.DUST) {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 2.0f));
            } else {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Drawing an Eye using Particles
     * @param location Center Location of the Eye
     * @param scale Scale of the Eye
     */
    public static void drawEye(Location location, int scale) {
        drawElipse(location, 1 + scale, 128, Particle.DUST, -2.58, 6.6);
        drawElipse(location, 1 + scale, 128, Particle.DUST, -2.58, 3.3);
        drawCircle(location, 2 + 2 * scale, location.getWorld(), 128, Particle.DUST, new Particle.DustOptions(Color.RED, 2.0f));
    }

    /**
     * Get the highest Location at a specific point
     * @param loc Point to get the highest location at
     * @return Location of the highest block at the point
     */
    public static double getGroundLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return 0d;
        return loc.getWorld().getHighestBlockYAt(
                loc.getBlockX(), loc.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES);
    }

    /**
     * Draw A circle using particles
     * @param center Center Location of the circle
     * @param radius Radius of the circle
     * @param world World to spawn the circle in
     * @param points Number of points to use for the circle
     * @param particle Particle to use for the circle
     * @param options Particle Options to use for the circle
     */
    public static void drawCircle(Location center, double radius, World world, int points, Particle particle, Particle.DustOptions options) {
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ() + (radius * Math.sin(angle)); // Calculate Z coordinate
            double y = center.getY(); // Y remains constant for a flat circle

            // Create a new location for the point
            Location pointLocation = new Location(world, x, y, z);

            // 2. Spawn particles
            if (particle == Particle.DUST) {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 1, options);
            } else {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Creates a new ItemStack of the Clover Signa
     * @return ItemStack of the Clover Signa
     */
    public static @NonNull ItemStack Clover() {
        ItemStack itemStack = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        ItemMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("cloversigna"));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Draws a tinted display around the player with animations and optional teleportation.
     * This method creates an `ItemDisplay` entity and applies transformations, tint coloring, scaling,
     * and rotational animations, while managing its lifecycle.
     *
     * @param isEye                  If true, the display originates from the player's eye location; otherwise, the player's main location is used.
     * @param time                   Duration (in seconds) for how long the display should persist.
     * @param player                 The player around whom the display is created.
     * @param teleportationDuration  Specifies the duration (in ticks) for item display teleportation.
     * @param itemStack              The item stack to be displayed. If null, a default leather armor item is used.
     * @param endingTransformation   The location where the display should move to at the end of its lifecycle.
     * @param tint                   The color tint to apply to the `ItemDisplay`.
     * @param scale                  The scale of the item display.
     * @param offset                 The offset value used for rotational animation.
     */
    public static void drawPlayerTintedDisplay(boolean isEye,double time,  Player player,int teleportationDuration, @Nullable ItemStack itemStack,  Location endingTransformation, Color tint,int scale,double offset) {
        Location beginningTransform = player.getLocation();
        if (isEye) {
            beginningTransform = player.getEyeLocation();
        }

        ItemDisplay display = (ItemDisplay) beginningTransform.getWorld().spawnEntity(beginningTransform, EntityType.ITEM_DISPLAY);
        display.setBrightness(new Display.Brightness(15,15));
        display.setGlowColorOverride(tint.setAlpha(0));
        display.setTransformation(
                new Transformation(
                        display.getTransformation().getTranslation(),
                        display.getTransformation().getLeftRotation(),
                        new Vector3f(scale, scale, scale),
                        display.getTransformation().getRightRotation()
                )
        );
        LeatherArmorMeta meta;
        if (itemStack != null) {
            meta = (LeatherArmorMeta) itemStack.getItemMeta();
        } else {
            itemStack = new ItemStack(Material.LEATHER_HORSE_ARMOR);
            meta = (LeatherArmorMeta) itemStack.getItemMeta();
            meta.setItemModel(NamespacedKey.minecraft("defaultsigna"));

        }
        meta.setColor(tint);
        itemStack.setItemMeta(meta);
        display.setItemStack(itemStack);
        display.setTeleportDuration(Math.clamp(teleportationDuration,0,59));
        new BukkitRunnable() {
            float rotation = (float) offset;
            @Override
            public void run() {
                if (display.isDead()) {
                    this.cancel();
                }
                display.setTransformation(
                        new Transformation(
                                display.getTransformation().getTranslation(),
                                display.getTransformation().getLeftRotation(),
                                new Vector3f(scale, scale, scale),
                                display.getTransformation().getRightRotation().rotateZ(rotation)
                        )
                );
                if (isEye) {
                    display.teleport(player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1 + scale/offset)));
                } else {
                    display.teleport(player);
                }
                rotation = (rotation + 0.01f);
            }
        }.runTaskTimer(Freedom.get_plugin(), 0,0);
        display.teleport(endingTransformation);
        new BukkitRunnable() {
            @Override
            public void run() {
                this.cancel();
                display.remove();
            }
        }.runTaskLater(Freedom.get_plugin(), (long) secondsToTicks(time));





    }

    /**
     * Creates and manages a tinted item display entity that transitions between two locations
     * over a configurable duration, with scaling and timing options. The display appears with
     * a specified color tint and is automatically removed after a set time.
     *
     * @param time The time in seconds after which the display entity will be removed.
     * @param beginningTransform The starting location where the display entity will spawn.
     * @param teleportationDuration The duration in ticks for the display to transition
     *                              between the beginning and ending transformations, clamped
     *                              between 0 and 59.
     * @param itemStack The desired {@link ItemStack} to be displayed. If null, a default
     *                  {@link Material#LEATHER_HORSE_ARMOR} with applied tint is used.
     * @param endingTransformation The location where the display entity will be teleported to.
     * @param tint The {@link Color} to apply as a tint to the displayed item.
     * @param scale The scaling factor to be applied to the displayed item during transformation.
     */
    public static void drawTintedDisplay(double time,  Location beginningTransform,int teleportationDuration, @Nullable ItemStack itemStack,  Location endingTransformation, Color tint,int scale) {
        ItemDisplay display = (ItemDisplay) beginningTransform.getWorld().spawnEntity(beginningTransform, EntityType.ITEM_DISPLAY);
        display.setTransformation(
                new Transformation(
                        display.getTransformation().getTranslation(),
                        display.getTransformation().getLeftRotation(),
                        new Vector3f(scale, scale, scale),
                        display.getTransformation().getRightRotation()
                )
        );
        LeatherArmorMeta meta;
        if (itemStack != null) {
            meta = (LeatherArmorMeta) itemStack.getItemMeta();
        } else {
            itemStack = new ItemStack(Material.LEATHER_HORSE_ARMOR);
            meta = (LeatherArmorMeta) itemStack.getItemMeta();
            meta.setItemModel(NamespacedKey.minecraft("defaultsigna"));

        }
        meta.setColor(tint);
        itemStack.setItemMeta(meta);
        display.setItemStack(itemStack);
        display.setTeleportDuration(Math.clamp(teleportationDuration,0,59));
        display.teleport(endingTransformation);
        new BukkitRunnable() {
            @Override
            public void run() {
                    this.cancel();
                    display.remove();
            }
        }.runTaskLater(Freedom.get_plugin(), (long) secondsToTicks(time));





    }

    /**
     * Drawing a Circle with any Particle Similar to
     * @see #drawCircle(Location, double, World, int, Particle, Particle.DustOptions)
     * Only difference is that it works with literally any particle while that is meant for dust
     * @param center Location of the center of the circle
     * @param radius Radius of the circle
     * @param world World to spawn the circle in
     * @param points Number of points to use for the circle
     * @param particle Particle to use for the circle
     */
    public static void drawCircle(Location center, double radius, World world, int points, Particle particle) {
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ() + (radius * Math.sin(angle)); // Calculate Z coordinate
            double y = center.getY(); // Y remains constant for a flat circle

            // Create a new location for the point
            Location pointLocation = new Location(world, x, y, z);

            // 2. Spawn particles
            if (particle == Particle.DUST) {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 1, new Particle.DustOptions(Color.PURPLE, 2.0f));
            } else {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Converts seconds to ticks
     * @param seconds to convert to ticks
     * @return Ticks equivalent to the seconds
     */
    public static double secondsToTicks(double seconds) {
        return seconds * 20;
    }

    /**
     * Retrieves or initializes a scoreboard objective named "UI" for the specified player.
     * The objective displays player-specific data such as "Lives" and "SoulPoints" in the sidebar slot.
     * If the objective does not already exist, it will be created with default settings and styled components.
     *
     * @param player the player whose scoreboard will be retrieved or modified
     * @return the initialized or existing objective named "UI" associated with the player's scoreboard
     */
    public static Objective getUI(Player player) {
        Scoreboard score = player.getScoreboard();
        if (score.getObjective("UI") == null) {
            Objective objective = score.registerNewObjective("UI", Criteria.DUMMY,dess(""));
            objective.getScore("Lives: ").setScore(player.getPersistentDataContainer().get(keygen("life"), PersistentDataType.INTEGER));
            objective.getScore("Lives: ").customName(dess("<shadow:#000000FF><b><green>Lives</green></b>:"));
            objective.getScore("SoulPoints:").customName(dess("<shadow:#000000FF><b><aqua>SoulPoints</aqua></b>:"));

            objective.numberFormat(NumberFormat.styled(Style.style(TextDecoration.BOLD)));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        return score.getObjective("UI");
    }

    public static HashMap<UUID, Scoreboard> souls = new HashMap<>();

    /**
     * Updates and displays a custom scoreboard for the given player based on various parameters
     * such as soul value, lives, and cooldowns of abilities. It also handles the player's combat status.
     *
     * @param player The player for whom the scoreboard is being updated.
     * @param soulvalue The current soul value of the player.
     * @param lives The current number of lives the player has.
     */
    public static void open(Player player, int soulvalue, int lives) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard score;
        if (souls.get(player.getUniqueId()) == null) {
            score = manager.getNewScoreboard();
            souls.put(player.getUniqueId(), score);
        } else {
            score = souls.get(player.getUniqueId());
        }


        String scoreboardName = player.getUniqueId().toString() + Freedom.version;

        for (Objective objective : score.getObjectives()) {
            if (!objective.getName().equals(scoreboardName)) {
                objective.unregister();
            }

        }

        if (score.getObjective(scoreboardName) != null) {
            int Money = getCurrency(player);
            Objective objective = score.getObjective(scoreboardName);
            objective.getScore("line1").customName(dess("<shadow:#000000FF><b><yellow>----------------</yellow></b>"));
            objective.getScore("line1").numberFormat(NumberFormat.blank());
            objective.getScore("ZLives").customName(dess("     <shadow:#000000FF><b><green>Lives</green>:       " + lives));
            objective.getScore("ZLives").numberFormat(NumberFormat.blank());
            objective.getScore("SoulPoints").customName(dess("<shadow:#000000FF><b><aqua>SoulPoints</aqua>: ").append(loadingBar(soulvalue,10,0)));
            objective.getScore("SoulPoints").numberFormat(NumberFormat.blank());
            objective.getScore("ZZMoney").numberFormat(NumberFormat.blank());
            objective.getScore("ZZMoney").customName(dess("  <shadow:#000000FF><b><gradient:gold:gold:#a64000>Groschen:   " + Money));
        } else {
            int Money = getCurrency(player);
            Objective objective = score.registerNewObjective(scoreboardName, "dummy",dess("<shadow:#000000FF><b>Details:"));
            objective.getScore("line1").customName(dess("<shadow:#000000FF><b><yellow>----------------</yellow></b>"));
            objective.getScore("line1").numberFormat(NumberFormat.blank());
            objective.getScore("ZLives").customName(dess("     <shadow:#000000FF><b><green>Lives</green>:       " + lives));
            objective.getScore("ZLives").numberFormat(NumberFormat.blank());
            objective.getScore("SoulPoints").customName(dess("<shadow:#000000FF><b><aqua>SoulPoints</aqua>: ").append(loadingBar(soulvalue,10,0)));
            objective.getScore("SoulPoints").numberFormat(NumberFormat.blank());
            objective.getScore("ZZMoney").customName(dess("  <shadow:#000000FF><b><gradient:gold:gold:#a64000>Groschen:   " + Money));
            objective.getScore("ZZMoney").numberFormat(NumberFormat.blank());
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        Objective objective = score.getObjective(scoreboardName);

        Base_Soul soul = getSoul(player);
        player.setScoreboard(score);
        if (soul == null) return;
        if (!abilityOneCooldowns.containsKey(player.getUniqueId())) {
            abilityOneCooldowns.put(player.getUniqueId(), 0L);
        }
        if (!abilityTwoCooldowns.containsKey(player.getUniqueId())) {
            abilityTwoCooldowns.put(player.getUniqueId(), 0L);
        }

        double seconds = (double) (soul.effective_cooldown(soul.AbilityOne_Cooldown(player), player.getUniqueId()) - (System.currentTimeMillis() - abilityOneCooldowns.get(player.getUniqueId()))) / 1000;
        if (soul.can_ability(soul.AbilityOne_Cooldown(player),abilityOneCooldowns,player.getUniqueId())) {
            objective.getScore("XAbility1").customName(
                    dess("   <shadow:#000000FF><b><aqua>Ability 1</aqua>: ").append(
                            dess("<shadow:#000000FF><b><green>READY!")
                    )
            );
        } else {
            objective.getScore("XAbility1").customName(
                    dess("   <shadow:#000000FF><b><aqua>Ability 1</aqua>: ").append(
                            dess(Math.round(seconds) + "s")
                    )
            );
        }

        objective.getScore("XAbility1").numberFormat(NumberFormat.blank());
        objective.getScore("XAbility2").numberFormat(NumberFormat.blank());
        double twoseconds = (double) (soul.effective_cooldown(soul.AbilityTwo_Cooldown(), player.getUniqueId()) - (System.currentTimeMillis() - abilityTwoCooldowns.get(player.getUniqueId()))) / 1000;
        if (soul.can_ability(soul.AbilityTwo_Cooldown(),abilityTwoCooldowns,player.getUniqueId())) {
            objective.getScore("XAbility2").customName(
                    dess("   <shadow:#000000FF><b><aqua>Ability 2</aqua>: ").append(
                            dess("<shadow:#000000FF><b><green>READY!")
                    )
            );
        } else {
            objective.getScore("XAbility2").customName(
                    dess("   <shadow:#000000FF><b><aqua>Ability 2</aqua>: ").append(
                            dess(Math.round(twoseconds) + "s")
                    )
            );
        }
        objective.getScore("XCombat").numberFormat(NumberFormat.blank());
        if (CombatTimer.combatTimer.containsKey(player.getUniqueId())) {
            int combatseconds = (int) (CombatTimer.combatTime + 2000 - (System.currentTimeMillis() - CombatTimer.combatTimer.get(player.getUniqueId()))) / 1000;
            objective.getScore("XCombat").customName(dess("   <shadow:#000000FF><b><gold>Combat</gold>:  ").append(dess("(<color:#ff4400>" + combatseconds + "s</color>)")));
        } else {
            objective.getScore("XCombat").customName(dess("<shadow:#000000FF><b><gold>Combat</gold>: ").append(dess("<shadow:#000000FF><b><red>INACTIVE!")));
        }
        objective.getScore("Zline2").customName(dess("<shadow:#000000FF><b><yellow>----------------</yellow></b>"));
        objective.getScore("Zline2").numberFormat(NumberFormat.blank());
        player.setScoreboard(score);
    }



    public static HashMap<UUID,Long> abilityOneCooldowns = new HashMap<>();

    public static HashMap<UUID,Long> abilityTwoCooldowns = new HashMap<>();

    /**
     * Clears the souls HashMap along with Ability Cooldowns for one Player
     * @param playerUUID uuid of player you are removing
     */
    public static void clearPlayerCache(UUID playerUUID) {
        souls.remove(playerUUID);
        abilityOneCooldowns.remove(playerUUID);
        abilityTwoCooldowns.remove(playerUUID);
    }

    //We are not done yet, you got this keep going

    /**
     * Generates a loading bar representation as a Component based on the provided value,
     * maximum, and minimum limits. The loading bar is displayed using a fixed number of
     * segments, where filled segments represent the progress proportionally, and unfilled
     * segments represent the remaining portion.
     *
     * @param value The current progress value to be represented by the loading bar.
     * @param max The maximum limit of the progress range. Must be greater than the minimum limit.
     * @param min The minimum limit of the progress range. Must be less than the maximum limit.
     * @return A Component object containing the styled loading bar representation,
     *         or a default fallback representation if the maximum limit is less than
     *         or equal to the minimum limit.
     */
    public static Component loadingBar(double value, double max, double min) {
        int maxBars = 5;

        //
        if (max <= min) {
            return dess("<shadow:#000000FF><b>|<red>#####</red></b>");
        }

        //
        double progress = (value - min) / (max - min);

        //
        progress = Math.max(0, Math.min(1, progress));

        //
        int filledBars = (int) Math.round(progress * maxBars);

        StringBuilder filled = new StringBuilder();
        StringBuilder empty = new StringBuilder();

        for (int i = 0; i < filledBars; i++) {
            filled.append("#");
        }

        for (int i = filledBars; i < maxBars; i++) {
            empty.append("#");
        }

        return dess("<shadow:#000000FF><b>|<aqua>" + filled + "</aqua><gray>" + empty + "</gray>|</b>");
    }


    /**
     * Creates a minimal magic circle effect around the specified player. The circle's color
     * is determined by the player's soul type, and it evolves over time with visual effects.
     *
     * @param target   The player around whom the magic circle will be generated.
     * @param tickRate The rate at which the magic circle effect updates, measured in ticks.
     */
    public static void createMinMagicCircleAroundPlayer(Player target, int tickRate) {
        SoulTypes soulType = SoulTypes.valueOf(target.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        Color color = Color.PURPLE;
        switch (soulType) {
            case Green, BaseGreen -> color = Color.GREEN;
            case Red, BaseRed -> color = Color.RED;
            case Cafe, BaseCafe -> color = Color.YELLOW;
            case Orange, BaseOrange -> color = Color.ORANGE;
            case BaseMocha, Mocha -> color = Color.BLUE;
            case Black, BaseBlack -> color = Color.BLACK;
        }
        Color finalColor = color;
        new BukkitRunnable() {
            int tick = 1;

            @Override
            public void run() {
                multisquare(target.getLocation(), tick, 4, Particle.DUST, 90, new Particle.DustOptions(finalColor.setBlue(0), 1.5f));
                multisquare(target.getLocation(), tick, 10, Particle.DUST, 30, new Particle.DustOptions(finalColor, 1.5f));
                drawCircle(target.getLocation(), 1, target.getWorld(), 30, Particle.DUST, new Particle.DustOptions(Color.PURPLE, 2.0f));
                tick = tick + tickRate;
                if (tick >= 1000) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 10);
    }


    /**
     * Creates a magic circle animation at a given location with specific particle effects and behavior based on the
     * soul type. This method spawns particle effects in patterns, which dynamically evolve over time.
     * The animation stops under certain conditions such as exceeding a threshold tick count or changes in the
     * block state at the specified location.
     *
     * @param center   The central {@code Location} where the magic circle will be created.
     * @param tickRate The rate at which the animation progresses, affecting how frequently particles are updated.
     * @param soulType The type of {@code SoulTypes} that determines the color of the particles used in the animation.
     */
    public static void createMinMagicCircle(Location center, int tickRate, SoulTypes soulType) {
        Color color = Color.PURPLE;

        switch (soulType) {
            case Green, BaseGreen -> color = Color.GREEN;
            case Red, BaseRed -> color = Color.RED;
            case Cafe, BaseCafe -> color = Color.YELLOW;
            case Orange, BaseOrange -> color = Color.ORANGE;
            case BaseMocha, Mocha -> color = Color.BLUE;
            case Black, BaseBlack -> color = Color.BLACK;
        }
        Color finalColor = color;
        if (center.clone().add(0, 0, 0).getBlock().getType() == Material.LECTERN) {
            Freedom.get_plugin().getLogger().info("LECTURN");
            new BukkitRunnable() {
                int tick = 1;

                @Override
                public void run() {
                    multisquare(center, tick, 4, Particle.DUST, 90, new Particle.DustOptions(finalColor.setBlue(0), 2f));
                    multisquare(center, tick, 10, Particle.DUST, 30, new Particle.DustOptions(finalColor, 2f));
                    if (tick % 3 == 0 || tick % 8 == 0) {
                        // multiple of 10
                        randompointoncircle(center, 30, 1, Particle.DUST);
                    }
                    drawCircle(center, 1, center.getWorld(), 30, Particle.DUST);
                    tick = tick + tickRate;

                    if (center.clone().add(0, 0, 0).getBlock().getType() != Material.LECTERN) {
                        this.cancel();
                    }
                    if (center.clone().add(0, 0, 0).getBlock().getType() == Material.LECTERN) {
                        Lectern lectern = (Lectern) center.clone().add(0, 0, 0).getBlock().getState();
                        if (lectern.getInventory().getItem(0) == null && tick > 4) {
                            this.cancel();
                        }

                    }

                }
            }.runTaskTimer(Freedom.get_plugin(), 5, 10);
        } else {
            new BukkitRunnable() {
                int tick = 1;

                @Override
                public void run() {
                    multisquare(center, tick, 4, Particle.DUST, 90, new Particle.DustOptions(finalColor.setBlue(0), 2f));
                    multisquare(center, tick, 10, Particle.DUST, 30, new Particle.DustOptions(finalColor, 2f));
                    if (tick % 3 == 0 || tick % 8 == 0) {
                        // multiple of 10
                        randompointoncircle(center, 30, 1, Particle.DUST);
                    }
                    drawCircle(center, 1, center.getWorld(), 30, Particle.DUST);
                    tick = tick + tickRate;
                    if (tick >= 1000) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Freedom.get_plugin(), 5, 10);
        }

    }

    /**
     * Creates a minimal magic circle visual effect at a specified location.
     * The effect's color and behavior are determined by the specified soul type and timing parameters.
     *
     * @param center   The {@link Location} where the magic circle will be created.
     * @param tickRate The rate at which the effects update, in game ticks.
     * @param soulType The type of soul determining the color of the magic circle.
     *                 Available soul types include: Green, BaseGreen, Red, BaseRed, Cafe, BaseCafe,
     *                 Orange, BaseOrange, BaseMocha, Mocha, Black, BaseBlack.
     * @param time     The duration (in game ticks) for which the effect should last
     *                 if the location is not a lectern or the item on a lectern becomes invalid.
     */
    public static void createMinMagicCircle(Location center, int tickRate, SoulTypes soulType,int time) {
        Color color = Color.PURPLE;
        switch (soulType) {
            case Green, BaseGreen -> color = Color.GREEN;
            case Red, BaseRed -> color = Color.RED;
            case Cafe, BaseCafe -> color = Color.YELLOW;
            case Orange, BaseOrange -> color = Color.ORANGE;
            case BaseMocha, Mocha -> color = Color.BLUE;
            case Black, BaseBlack -> color = Color.BLACK;
        }
        Color finalColor = color;
        if (center.clone().getBlock().getType() == Material.LECTERN) {
            Freedom.get_plugin().getLogger().info("LECTURN");
            new BukkitRunnable() {
                int tick = 1;

                @Override
                public void run() {
                    multisquare(center, tick, 4, Particle.DUST, 90, new Particle.DustOptions(finalColor.setBlue(0), 2f));
                    multisquare(center, tick, 10, Particle.DUST, 30, new Particle.DustOptions(finalColor, 2f));
                    if (tick % 3 == 0 || tick % 8 == 0) {
                        // multiple of 10
                        randompointoncircle(center, 30, 1, Particle.DUST);
                    }
                    drawCircle(center, 1, center.getWorld(), 30, Particle.DUST);
                    tick = tick + tickRate;

                    if (center.clone().add(0, 0, 0).getBlock().getType() != Material.LECTERN) {
                        this.cancel();
                    }
                    if (center.clone().add(0, 0, 0).getBlock().getType() == Material.LECTERN) {
                        Lectern lectern = (Lectern) center.clone().add(0, 0, 0).getBlock().getState();
                        if (lectern.getInventory().getItem(0) == null && tick > 4) {
                            this.cancel();
                        }

                    }

                }
            }.runTaskTimer(Freedom.get_plugin(), 5, 10);
        } else {
            new BukkitRunnable() {
                int tick = 1;

                @Override
                public void run() {
                    multisquare(center, tick, 4, Particle.DUST, 90, new Particle.DustOptions(finalColor.setBlue(0), 2f));
                    multisquare(center, tick, 10, Particle.DUST, 30, new Particle.DustOptions(finalColor, 2f));
                    if (tick % 3 == 0 || tick % 8 == 0) {
                        // multiple of 10
                        randompointoncircle(center, 30, 1, Particle.DUST);
                    }
                    drawCircle(center, 1, center.getWorld(), 30, Particle.DUST);
                    tick = tick + tickRate;
                    if (tick >= time) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Freedom.get_plugin(), 5, 10);
        }

    }

    /**
     * Creates a vertical magic circle animation with a specified color and properties.
     * The animation's appearance and behavior depend on the provided soul type, rotation, scale, and time duration.
     *
     * @param center The central location where the magic circle is created.
     * @param tickRate The rate at which the circle animation updates.
     * @param soulType The type of soul that determines the color of the magic circle.
     * @param yaw The yaw rotation angle to apply, influencing the orientation of the circle.
     * @param pivot The pivot point used for rotation calculations.
     * @param time The total duration (in ticks) for which the magic circle should animate.
     * @param scale The scaling factor to modify the size of the magic circle.
     */
    public static void createVerticleMinMagicCircle(Location center, int tickRate, SoulTypes soulType, double yaw,Location pivot, int time,double scale) {
        Color color = Color.PURPLE;
        switch (soulType) {
            case Green, BaseGreen -> color = Color.GREEN;
            case Red, BaseRed -> color = Color.RED;
            case Cafe, BaseCafe -> color = Color.YELLOW;
            case Orange, BaseOrange -> color = Color.ORANGE;
            case BaseMocha, Mocha -> color = Color.BLUE;
            case Black, BaseBlack -> color = Color.BLACK;
        }
        Color finalColor = color;
        final Location cent = rotpointZ(pivot, yaw, center);
            new BukkitRunnable() {
                int tick = 1;

                @Override
                public void run() {
                    Vertmultisquare(cent, tick, 4 * scale, Particle.DUST, 90, new Particle.DustOptions(finalColor.setBlue(0), 2f),yaw);
                    Vertmultisquare(cent, tick, 10 * scale, Particle.DUST, 30, new Particle.DustOptions(finalColor, 2f),yaw);
                    tick = tick + tickRate;
                    if (tick >= time) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Freedom.get_plugin(), 5, 10);
        }


    /**
     * Creates a "magic circle" effect at a given location using particles, tick rate, scaling, and soul type.
     * This method animates the circle with specific patterns and colors, dynamically determined based on the soul type.
     * The task automatically cancels based on certain conditions, such as the absence of a lectern or an empty inventory slot.
     *
     * @param center   The central location where the magic circle will be created.
     * @param tickRate The rate at which the animation progresses, controlling the speed of updates.
     * @param scale    The scale or size of the magic circle, influencing its dimensions and effect radius.
     * @param soulType The type of soul that determines the color of the magic circle and its thematic representation.
     */
    public static void createMaxMagicCircle(Location center, int tickRate, int scale, SoulTypes soulType) {
        Color color = Color.PURPLE;
        switch (soulType) {
            case Green, BaseGreen -> color = Color.GREEN;
            case Red, BaseRed -> color = Color.RED;
            case Cafe, BaseCafe -> color = Color.YELLOW;
            case Orange, BaseOrange -> color = Color.ORANGE;
            case BaseMocha, Mocha -> color = Color.BLUE;
            case Black, BaseBlack -> color = Color.BLACK;
        }
        Color finalColor = color;
        new BukkitRunnable() {
            int tick = 1;

            @Override
            public void run() {
                multisquare(center, tick, 4 + scale, Particle.DUST, 90, new Particle.DustOptions(finalColor, 1.5f));
                multisquare(center, tick, 10 + scale, Particle.DUST, 30, new Particle.DustOptions(finalColor, 1.5f));
                drawCircle(center, 1 + ((double) scale / 2), center.getWorld(), 30 + (scale * 10), Particle.DUST);
                multisquare(center, tick, 17 + scale, Particle.DUST, 90, new Particle.DustOptions(finalColor, 1.5f));
                tick = tick + tickRate;
                if (center.clone().add(0, 0, 0).getBlock().getType() != Material.LECTERN) {
                    this.cancel();
                }
                if (center.clone().add(0, 0, 0).getBlock().getType() == Material.LECTERN) {
                    Lectern lectern = (Lectern) center.clone().add(0, 0, 0).getBlock().getState();
                    if (lectern.getInventory().getItem(0) == null && tick > 4) {
                        this.cancel();
                    }

                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 12);
    }

    public static Random random = new Random();

    /**
     * Creates and animates remote explosion particles at a specified location. The animation
     * includes rotational effects, particle trails, sound effects, and ends with a simulated explosion.
     *
     * @param center The central {@code Location} where the explosion particles originate.
     * @param tickrate The rate at which the particle rotation and animation steps occur, measured in ticks.
     * @param size The power or scale of the explosion, determining the size and intensity of the particle effects.
     */
    public static void createRemoteExplosionParticles(Location center/* Center of Explosion*/, int tickrate/* rate of rotation*/, int size/* Power given from spell compiler*/) {
        new BukkitRunnable() {
            double size_modifier = 1;

            @Override
            public void run() {


                Location c1 = center.clone().add(-((size + random.nextFloat(0, 1)) * size_modifier), 0, -((size + random.nextFloat(0, 1)) * size_modifier));
                Location c2 = center.clone().add(((size + random.nextFloat(0, 1)) * size_modifier), 0, -(size * size_modifier));
                Location c3 = center.clone().add(((size + random.nextFloat(0, 1)) * size_modifier), 0, ((size + random.nextFloat(0, 1)) * size_modifier));
                Location c4 = center.clone().add(-((size + random.nextFloat(0, 1)) * size_modifier), 0, ((size + random.nextFloat(0, 1)) * size_modifier));
                //add rot
                int rot = tick;
                c1 = rotpointX(center, rot, c1);
                c2 = rotpointX(center, rot, c2);
                c3 = rotpointX(center, rot, c3);
                c4 = rotpointX(center, rot, c4);

                Location c1u = c1.clone().add(0, 30, 0);
                Location c2u = c2.clone().add(0, 30, 0);
                Location c3u = c3.clone().add(0, 30, 0);
                Location c4u = c4.clone().add(0, 30, 0);
                drawParticleLine(c1, c1u, c1.getWorld(), Particle.SOUL_FIRE_FLAME, new Particle.DustOptions(Color.AQUA, 2.0f), 0, 8, 0);
                drawParticleLine(c2, c2u, c2.getWorld(), Particle.SOUL_FIRE_FLAME, new Particle.DustOptions(Color.AQUA, 2.0f), 0, 8, 0);
                drawParticleLine(c3, c3u, c3.getWorld(), Particle.SOUL_FIRE_FLAME, new Particle.DustOptions(Color.AQUA, 2.0f), 0, 8, 0);
                drawParticleLine(c4, c4u, c4.getWorld(), Particle.SOUL_FIRE_FLAME, new Particle.DustOptions(Color.AQUA, 2.0f), 0, 8, 0);
                center.getWorld().playSound(center, Sound.ITEM_FLINTANDSTEEL_USE, 4, soundtick);
                drawDangerSymbol(center,5,16,Particle.DUST,new Particle.DustOptions(Color.YELLOW, 8.0f),new Particle.DustOptions(Color.BLACK,8.0f));
                tick = tick + tickrate;
                if (tick % 80 == 0) {
                    soundtick++;

                }
                size_modifier = size_modifier * 0.9;
                if (tick >= 550) {
                    for (int x = 0; x < 30; x = x + 5) {
                        drawSquare(center.clone().add(0, x, 0), ((float) size / 3) + ((float) x / 5), tick, Particle.DUST, new Particle.DustOptions(Color.WHITE, 2.0f + ((float) x / 5)), 0, 8, 0);
                        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 4, 1);

                    }

                    center.getWorld().spawnParticle(Particle.EXPLOSION, center, 30);
                    this.cancel();
                }
            }

            int soundtick = 0;
            int tick = 0;

        }.runTaskTimer(Freedom.get_plugin(), 0, 4);
    }

    /**
     * Creates a maximum magic circle effect around the specified player. The visual effect is dynamic
     * and involves particles, changing intention and color based on the player's soul
     * @param scale The scale of the magic circle, affecting its size and visual impact.
     * @param tickRate The rate at which the animation progresses, affecting the speed of the effect.
     *
     * */
    public void createMaxMagicCircleAroundPlayer(Player target, int tickRate, int scale) {
        SoulTypes soulType = SoulTypes.valueOf(target.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        Color color = Color.PURPLE;
        switch (soulType) {
            case Green, BaseGreen -> color = Color.GREEN;
            case Red, BaseRed -> color = Color.RED;
            case Cafe, BaseCafe -> color = Color.YELLOW;
            case Orange, BaseOrange -> color = Color.ORANGE;
            case BaseMocha, Mocha -> color = Color.BLUE;
            case Black, BaseBlack -> color = Color.BLACK;
        }
        Color finalColor = color;
        new BukkitRunnable() {
            int tick = 1;

            @Override
            public void run() {
                multisquare(target.getLocation(), tick, 4 + scale, Particle.DUST, 90, new Particle.DustOptions(finalColor, 8f));
                multisquare(target.getLocation(), tick, 10 + scale, Particle.DUST, 30, new Particle.DustOptions(finalColor, 8f));
                drawCircle(target.getLocation(), 1 + ((double) scale / 2), target.getWorld(), 30 + (scale * 10), Particle.DUST);
                multisquare(target.getLocation(), tick, 17 + scale, Particle.DUST, 90, new Particle.DustOptions(finalColor, 1.5f));
                tick = tick + tickRate;
                if (tick >= 1000) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 12);
    }

    /**
     * Gets a random point on a circle
     * @param center {@code Location} of the center of the circle
     * @param points How many points on the original Circle
     * @param radius The Radius of the Original Circle
     * @param particle Particle Type that you want to spawn as the little pillar thing
     */
    public static void randompointoncircle(Location center, int points, int radius, Particle particle) {
        World world = center.getWorld();
        Random random = new Random();
        int i = random.nextInt(points);
        double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
        double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
        double z = center.getZ() + (radius * Math.sin(angle)); // Calculate Z coordinate
        double y = center.getY(); // Y remains constant for a flat circle

        // Create a new location for the point
        Location pointLocation = new Location(world, x, y, z);

        // 2. Spawn particles
        if (particle == Particle.DUST) {
            Location location = pointLocation.clone();
            for (double iter = -1.0; iter <= 1.0; iter += 0.25) {

                world.spawnParticle(particle, pointLocation.clone().add(0, iter, 0), 1, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 2.0f));
            }
        } else {
            world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Broadcast a message to all online players
     * @param message The message you want to broadcast
     */
    public void broadcast(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.showTitle(Title.title(dess("<shadow:#000000FF><b><Red>ANNOUNCEMENT"),dess(message)));
        });
    }

    /**
     * Creates a multilayered square looking Just like a magic circle
     * @param location Center Location of the Magic Circle
     * @param tick Tick offset you want to start at
     * @param size size of the multisquare, acts as a scale
     * @param particle Particle Type that you want to spawn as the little pillar thing
     * @param initialrot Initial offset of the circle's rotations
     * @param options Any Dust Options you want to use
     */
    public static void multisquare(Location location, int tick, int size, Particle particle, int initialrot, Particle.DustOptions options) {
        Color optionscolor = options.getColor();
        optionscolor = optionscolor.mixColors(Color.RED);
        Particle.DustOptions options1 = new Particle.DustOptions(optionscolor, options.getSize());
        drawSquare(location, size, initialrot + tick, particle, options, 0, 8, 0);
        drawSquare(location, size, initialrot - 45 + tick, particle, options1, 0, 8, 0);
    }

    /**
     * Creates a verticle multisquare similar to multisquare
     * @param location Center Location of the Magic Circle
     * @param tick Tick offset you want to start at
     * @param size size of the multisquare, acts as a scale
     * @param particle Particle Type that you want to spawn as the little pillar thing
     * @param initialrot Initial offset of the circle's rotations
     * @param options Any Dust Options you want to use
     * @param yaw Yaw angle for the vertical square
     */
    public static void Vertmultisquare(Location location, int tick, double size, Particle particle, int initialrot, Particle.DustOptions options, double yaw) {
        Color optionscolor = options.getColor();
        optionscolor = optionscolor.mixColors(Color.RED);
        Particle.DustOptions options1 = new Particle.DustOptions(optionscolor, options.getSize());
        drawVerticleSquare(location, size, initialrot + tick, particle, options, 0, 8, 0,true,yaw);
        drawVerticleSquare(location, size, initialrot - 45 + tick, particle, options1, 0, 8, 0,true,yaw);
    }

    /**
     * Draws a square shape using particles at a specified location, with configurable size, rotation, particle type, and dust options.
     * @param center The center location of the square.
     * @param size The size of the square, affecting its width and height.
     * @param rot The initial rotation angle of the square.
     * @param particle Particle type to spawn for the square.
     * @param options Dust options to apply to the particles in the square.
     * @param xlimit A cutting variable that stops the drawing at a certain point, to allow for "loading animations" on the X axis
     * @param ylimit A cutting variable that stops the drawing at a certain point, to allow for "loading animations" on the Y axis
     * @param zlimit A cutting variable that stops the drawing at a certain point, to allow for "loading animations" on the Z axis
     */
    public static void drawSquare(Location center, double size, double rot, Particle particle, Particle.DustOptions options, double xlimit, double ylimit, double zlimit) {
        size = size / 2; // Half-size for a 5x5

        // Four corners
        Location c1 = center.clone().add(-size, 0, -size);
        Location c2 = center.clone().add(size, 0, -size);
        Location c3 = center.clone().add(size, 0, size);
        Location c4 = center.clone().add(-size, 0, size);
        //add rot
        c1 = rotpointX(center, rot, c1);
        c2 = rotpointX(center, rot, c2);
        c3 = rotpointX(center, rot, c3);
        c4 = rotpointX(center, rot, c4);
        // Draw lines between corners (simplified)
        drawParticleLine(c1, c2, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c2, c3, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c3, c4, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c4, c1, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
    }

    /**
     * Draws a verticle square shape using particles at a specified location, with configurable size, rotation, particle type, and dust options.
     * @param center The center location of the square.
     * @param size The size of the square, affecting its width and height.
     * @param rot The initial rotation angle of the square.
     * @param particle Particle type to spawn for the square.
     * @param options Dust options to apply to the particles in the square.
     * @param xlimit A cutting variable that stops the drawing at a certain point, to allow for "loading animations" on the X axis
     * @param ylimit A cutting variable that stops the drawing at a certain point, to allow for "loading animations" on the Y axis
     * @param zlimit A cutting variable that stops the drawing at a certain point, to allow for "loading animations" on the Z axis
     */
    public static void drawVerticleSquare(Location center, double size, double rot, Particle particle, Particle.DustOptions options, double xlimit, double ylimit, double zlimit) {
        size = size / 2; // Half-size for a 5x5

        // Four corners
        Location c1 = center.clone().add(-size, -size, 0);
        Location c2 = center.clone().add(size, -size, 0);
        Location c3 = center.clone().add(size, size, 0);
        Location c4 = center.clone().add(-size, size, 0);
        //add rot
        c1 = rotpointZ(center, rot, c1);
        c2 = rotpointZ(center, rot, c2);
        c3 = rotpointZ(center, rot, c3);
        c4 = rotpointZ(center, rot, c4);
        // Draw lines between corners (simplified)
        drawParticleLine(c1, c2, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c2, c3, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c3, c4, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c4, c1, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
    }

    /**
     * Draws a vertical square using particles in a 3D space, with options for size, rotation, and positional limits.
     *
     * @param center The central {@code Location} around which the square will be drawn.
     * @param size The length of the sides of the square. This value will be halved internally to determine the distance from the center.
     * @param rot The rotation angle in radians to apply to the square around the Y-axis.
     * @param particle The {@code Particle} type used to draw the square.
     * @param options Additional {@code DustOptions} for customizing particle appearance, such as color and size.
     * @param xlimit The limit on the X-axis beyond which particles will not be rendered.
     * @param ylimit The limit on the Y-axis beyond which particles will not be rendered.
     * @param zlimit The limit on the Z-axis beyond which particles will not be rendered.
     * @param baller A boolean flag determining whether specific rotational effects will be applied during rendering.
     * @param yaw The yaw angle in degrees to apply when rotating the square along its Z-axis.
     */
    public static void drawVerticleSquare(Location center, double size, double rot, Particle particle, Particle.DustOptions options, double xlimit, double ylimit, double zlimit,boolean baller, double yaw) {
        size = size / 2; // Half-size for a 5x5

        // Four corners
        Location c1 = center.clone().add(-size, -size, 0);
        Location c2 = center.clone().add(size, -size, 0);
        Location c3 = center.clone().add(size, size, 0);
        Location c4 = center.clone().add(-size, size, 0);
        //add rot
        Freedom.get_plugin().getLogger().info("Rotating:" + yaw);
        c1 = rotpointY(center, rot, c1);
        c2 = rotpointY(center, rot, c2);
        c3 = rotpointY(center, rot, c3);
        c4 = rotpointY(center, rot, c4);
        c1 = rotpointZ(center, yaw, c1);
        c2 = rotpointZ(center, yaw, c2);
        c3 = rotpointZ(center, yaw, c3);
        c4 = rotpointZ(center, yaw, c4);
        if (yaw <= 90) {
            c1 = rotpointX(center, yaw, c1);
            c2 = rotpointX(center, yaw, c2);
            c3 = rotpointX(center, yaw, c3);
            c4 = rotpointX(center, yaw, c4);
        }
        if (yaw >= -90 || yaw <= 0) {
            c1 = rotpointX(center, yaw + 90, c1);
            c2 = rotpointX(center, yaw+ 90, c2);
            c3 = rotpointX(center, yaw+ 90, c3);
            c4 = rotpointX(center, yaw+ 90, c4);
        }






        // Draw lines between corners (simplified)
        drawParticleLine(c1, c2, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c2, c3, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c3, c4, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
        drawParticleLine(c4, c1, center.getWorld(), particle, options, xlimit, ylimit, zlimit);
    }


    /**
     * Rotates a point around a pivot on the XZ-plane by a specified angle in degrees.
     *
     * @param pivot the location used as the center of rotation
     * @param angleDegrees the angle in degrees to rotate the point
     * @param toRotate the location of the point to be rotated
     * @return the new location of the point after rotation
     */
    public static Location rotpointX(Location pivot, double angleDegrees, Location toRotate) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        // 1. Get relative vector
        Vector v = toRotate.clone().subtract(pivot).toVector();

        // 2. Rotate Vector (X and Z)
        double x = v.getX() * cos - v.getZ() * sin;
        double z = v.getX() * sin + v.getZ() * cos;

        // 3. Add pivot back
        return pivot.clone().add(new Vector(x, v.getY(), z));
    }

    /**
     * Draws a clock visualization at the specified location using particle effects. The clock includes a face,
     * tick marks, an hour hand, and a minute hand. The appearance of the clock components is customizable
     * through various particle options.
     *
     * @param center the location where the clock will be drawn
     * @param radius the radius of the clock face
     * @param circlePoints the number of points used to render the circular clock face
     * @param handPoints the number of points used to render the clock hands
     * @param hours the hour value of the time to display on the clock (0-23)
     * @param minutes the minute value of the time to display on the clock (0-59)
     * @param faceParticle the type of particle to use for the clock face
     * @param faceOptions additional customization options for the clock face particle
     * @param tickParticle the type of particle to use for the tick marks
     * @param handParticle the type of particle to use for the hour and minute hands
     * @param minuteOptions additional customization options for the minute hand particle
     * @param hourOptions additional customization options for the hour hand particle
     */
    public static void drawClock(
            Location center,
            double radius,
            int circlePoints,
            int handPoints,
            int hours,
            int minutes,
            Particle faceParticle,
            Particle.DustOptions faceOptions,
            Particle tickParticle,
            Particle handParticle,
            Particle.DustOptions minuteOptions,
            Particle.DustOptions hourOptions
    ) {
        World world = center.getWorld();
        double y = center.getY();

    /* =========================
       1. DRAW CLOCK FACE
       ========================= */
        for (int i = 0; i < circlePoints; i++) {
            double angle = Math.toRadians(i * 360.0 / circlePoints);

            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));

            Location point = new Location(world, x, y, z);

            if (faceParticle == Particle.DUST) {
                world.spawnParticle(faceParticle, point, 1, 0, 0, 0, 1, faceOptions);
            } else {
                world.spawnParticle(faceParticle, point, 1, 0, 0, 0, 0);
            }
        }

    /* =========================
       2. DRAW TICKS (12 MARKS)
       ========================= */
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);

            double outerX = center.getX() + radius * Math.cos(angle);
            double outerZ = center.getZ() + radius * Math.sin(angle);

            double innerX = center.getX() + (radius * 0.85) * Math.cos(angle);
            double innerZ = center.getZ() + (radius * 0.85) * Math.sin(angle);

            for (double t = 0; t <= 1; t += 0.25) {
                double x = innerX + (outerX - innerX) * t;
                double z = innerZ + (outerZ - innerZ) * t;

                world.spawnParticle(
                        tickParticle,
                        new Location(world, x, y, z),
                        1, 0, 0, 0, 0,
                        minuteOptions
                );
            }
        }

    /* =========================
       3. CALCULATE HAND ANGLES
       ========================= */
        double minuteAngle = Math.toRadians((minutes / 60.0) * 360.0 - 90);
        double hourAngle = Math.toRadians(((hours % 12 + minutes / 60.0) / 12.0) * 360.0 - 90);

    /* =========================
       4. DRAW MINUTE HAND
       ========================= */
        for (int i = 0; i <= handPoints; i++) {
            double t = i / (double) handPoints;

            double x = center.getX() + (radius * 0.9 * t * Math.cos(minuteAngle));
            double z = center.getZ() + (radius * 0.9 * t * Math.sin(minuteAngle));

            Location point = new Location(world, x, y, z);

            if (handParticle == Particle.DUST) {
                world.spawnParticle(handParticle, point, 1, 0, 0, 0, 1, minuteOptions);
            } else {
                world.spawnParticle(handParticle, point, 1, 0, 0, 0, 0);
            }
        }

    /* =========================
       5. DRAW HOUR HAND
       ========================= */
        for (int i = 0; i <= handPoints; i++) {
            double t = i / (double) handPoints;

            double x = center.getX() + (radius * 0.6 * t * Math.cos(hourAngle));
            double z = center.getZ() + (radius * 0.6 * t * Math.sin(hourAngle));

            Location point = new Location(world, x, y, z);

            if (handParticle == Particle.DUST) {
                world.spawnParticle(handParticle, point, 1, 0, 0, 0, 1, hourOptions);
            } else {
                world.spawnParticle(handParticle, point, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Draws clock-like lines radiating outward from a central point on a circular plane.
     *
     * @param center   The central location around which the clocklines are drawn.
     * @param radius   The radius of the circle for the clocklines.
     * @param world    The world in which the particles will be displayed.
     * @param points   The total number of points (or divisions) around the circle.
     * @param particle The particle type to be displayed for the clocklines.
     * @param options  The particle dust options, if applicable for the given particle type.
     * @param rot      The rotational offset in radians for the starting angle of the clocklines.
     */
    public static void drawClocklines(Location center, double radius, World world, int points, Particle particle, Particle.DustOptions options, double rot) {
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ() + (radius * Math.sin(angle)); // Calculate Z coordinate
            double y = center.getY(); // Y remains constant for a flat circle

            // Create a new location for the point

            Location pointLocation = new Location(world, x, y, z);
            Location midpoint = new Location(world, (center.getX()+pointLocation.getX())/2, (center.getY()+pointLocation.getY())/2, (center.getZ()+pointLocation.getZ())/2);
            drawLine(pointLocation,midpoint,center.getWorld(),points/4,particle,options);
            // 2. Spawn particles
            if (particle == Particle.DUST) {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 1, options);
            } else {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
            }

        }
    }

    /**
     * Rotates a 3D point around the Z-axis by a specified angle relative to a pivot point.
     *
     * @param pivot        The pivot point around which the rotation is performed.
     * @param angleDegrees The rotation angle in degrees.
     * @param toRotate     The point to rotate.
     * @return A new Location representing the rotated point.
     */
    public static Location rotpointZ(Location pivot, double angleDegrees, Location toRotate) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        // 1. Get relative vector
        Vector v = toRotate.clone().subtract(pivot).toVector();

        // 2. Rotate Vector (X and Z)
        double x = v.getX() * cos - v.getZ() * sin;
        double Y = v.getX() * sin + v.getZ() * cos;

        // 3. Add pivot back
        return pivot.clone().add(new Vector(x, v.getY(), Y));
    }

    /**
     * Rotates a point around the Y-axis relative to a specified pivot point by a given angle.
     *
     * @param pivot the location of the pivot point around which the rotation is performed
     * @param angleDegrees the angle of rotation in degrees, measured clockwise
     * @param toRotate the location of the point to be rotated
     * @return the new location of the point after being rotated around the Y-axis
     */
    public static Location rotpointY(Location pivot, double angleDegrees, Location toRotate) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        // 1. Get relative vector
        Vector v = toRotate.clone().subtract(pivot).toVector();

        // 2. Rotate Vector (X and Z)
        double x = v.getX() * cos - v.getY() * sin;
        double Y = v.getX() * sin + v.getY() * cos;

        // 3. Add pivot back
        return pivot.clone().add(new Vector(x, Y, v.getZ()));
    }

    /**
     * Draws a line of particles between two locations in the specified world.
     * The particle line can have limitations in the x, y, and z directions, and supports custom particle options for dynamic effects.
     *
     * @param start The starting location of the particle line.
     * @param end The ending location of the particle line.
     * @param world The world in which the particles will be displayed.
     * @param particle The type of particle to be used for the line.
     * @param optionsdouble The additional options for the DUST particle effect, used when the particle type is {@code Particle.DUST}.
     * @param xlimit The distance along the x-axis after which particles will start being drawn.
     * @param ylimit The distance along the y-axis that affects particle display (currently unused in logic).
     * @param zlimit The distance along the z-axis that affects particle display (currently unused in logic).
     */
    public static void drawParticleLine(Location start, Location end, World world, Particle particle, Particle.DustOptions optionsdouble, double xlimit, double ylimit, double zlimit) {
        double space = 0.2; // Density of particles
        double distance = start.distance(end);
        Vector p1 = start.toVector();
        Vector p2 = end.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);

        for (double length = 0; length < distance; length += space) {

            p1.add(vector);
            if (length > xlimit) {
                if (particle == Particle.DUST) {
                    world.spawnParticle(particle, p1.toLocation(world), 1, 0, 0, 0, 1, optionsdouble);
                } else {
                    world.spawnParticle(particle, p1.toLocation(world), 1, 0, 0, 0, 0);
                }
            }
        }
    }

    /**
     * Represents the various states of a particle portal.
     * This enum is used to describe the lifecycle of a portal,
     * transitioning through its opening, open, and closing phases.
     */
    public static enum ParticlePortalState {
        Opening,
        Open,
        Closing
    }

    /**
     * Creates and manages the lifecycle of a particle-based portal effect at the specified location.
     * The portal progresses through three states: Opening, Open, and Closing, with particles displayed
     * to depict these transitions. Additionally, players near the portal during its "Open" state
     * are teleported to the specified target location.
     *
     * @param center The location where the portal is centered and particles are displayed.
     * @param targetLocation The location to which players will be teleported when near the portal
     *                       during its "Open" state.
     */
    public static void PortalParticleLifespan(Location center, Location targetLocation) {

        new BukkitRunnable() {
            int tick = 0;
            double lifespantick = 0;
            ParticlePortalState state = ParticlePortalState.Opening;
            @Override
            public void run() {
                switch (state) {
                    case Opening: {
                        if (lifespantick >= 2) {
                            state = ParticlePortalState.Open;
                        }
                        createPortalParticles(center,2-lifespantick,lifespantick * 4,0);
                        lifespantick = lifespantick + 0.5;
                        break;
                    }
                    case Open: {
                        lifespantick = 0;
                        createPortalParticles(center,0,4,0);
                        if (tick >= 100) {
                            state = ParticlePortalState.Closing;
                        }
                        for (Player player : center.getNearbyEntitiesByType(Player.class,2,2,2)) {
                            player.teleport(targetLocation);

                        }
                        break;
                    }
                    case Closing:
                        createPortalParticles(center,2 + lifespantick,lifespantick,0);
                        lifespantick = lifespantick + 1;
                        break;
                }
                if (tick >= 1200) {
                    this.cancel();
                }
                tick ++;
            }
        }.runTaskTimer(Freedom.get_plugin(),0,10);
    }

    /**
     * Creates a particle animation to simulate a portal with states: opening, open, and closing.
     * While the portal is open, nearby players are teleported to the target location.
     * The portal animation progresses over time and eventually stops after a set duration.
     *
     * @param center The location at which the portal particles are created. This is the center of the portal.
     * @param targetLocation The destination to which players will be teleported when they interact with the portal.
     * @param options The particle options used to define the appearance of the portal particles.
     */
    public static void PortalParticleLifespan(Location center, Location targetLocation, Particle.DustOptions options) {

        new BukkitRunnable() {
            int tick = 0;
            double lifespantick = 0;
            ParticlePortalState state = ParticlePortalState.Opening;
            @Override
            public void run() {
                switch (state) {
                    case Opening: {
                        if (lifespantick >= 2) {
                            state = ParticlePortalState.Open;
                        }
                        createPortalParticles(center,2-lifespantick,lifespantick * 4,0,options,new Particle.DustOptions(Color.BLACK,16f));
                        lifespantick = lifespantick + 0.5;
                        break;
                    }
                    case Open: {
                        lifespantick = 0;
                        createPortalParticles(center,0,4,0,options,new Particle.DustOptions(Color.BLACK,16f));
                        if (tick >= 100) {
                            state = ParticlePortalState.Closing;
                        }
                        for (Player player : center.getNearbyEntitiesByType(Player.class,2,2,2)) {
                            player.teleport(targetLocation);

                        }
                        break;
                    }
                    case Closing:
                        createPortalParticles(center,2 + lifespantick,lifespantick,0,options,new Particle.DustOptions(Color.BLACK,16f));
                        lifespantick = lifespantick + 1;
                        break;
                }
                if (tick >= 1200) {
                    this.cancel();
                }
                tick ++;
            }
        }.runTaskTimer(Freedom.get_plugin(),0,10);
    }

    /**
     * Creates portal particle effects at the specified location. The particle shapes can either form a circle
     * or multiple vertical squares depending on the configuration.
     *
     * @param center The central {@code Location} where the particles will be generated.
     * @param xlimit The x-axis limit controlling the particle effect spread in the x-direction.
     * @param ylimit The y-axis limit controlling the particle effect spread in the y-direction and affecting particle size.
     * @param zlimit The z-axis limit controlling the particle effect spread in the z-direction.
     */
    public static void createPortalParticles(Location center, double xlimit, double ylimit, double zlimit) {
        boolean circle = false;
        if (circle) {
            drawverticleCircle(center.add(0,2,0),2.1,center.getWorld(),32, Particle.DUST,new Particle.DustOptions(Color.GREEN,16f));
            for (double iteration = 0; iteration <= 2; iteration = iteration + 0.1) {
                drawverticleCircle(center,0 + iteration,center.getWorld(), (int) (16 + Math.round(iteration)),Particle.DUST,new Particle.DustOptions(Color.BLACK,128f));
            }
        } else {
            double rot = center.getRotation().yaw();
            drawVerticleSquare(center,4,rot, Particle.DUST,new Particle.DustOptions(Color.GREEN, (float) ylimit * 4),xlimit,0,0);
            drawVerticleSquare(center,3,rot, Particle.DUST,new Particle.DustOptions(Color.BLACK,(float) ylimit),xlimit,0,0);
            drawVerticleSquare(center,2,rot, Particle.DUST,new Particle.DustOptions(Color.BLACK,(float) ylimit),xlimit,0,0);
            drawVerticleSquare(center,1,rot, Particle.DUST,new Particle.DustOptions(Color.BLACK,(float) ylimit),xlimit,0,0);
        }
    }

    /**
     * Creates portal particle effects around a given location by generating various patterns such as circles or squares.
     * The type, color, and size of the particles can be customized using the provided parameters.
     *
     * @param center The center location where the portal particles will be created.
     * @param xlimit The horizontal limit defining the size or spread of the particle patterns along the X-axis.
     * @param ylimit The vertical limit defining the size or spread of the particle patterns along the Y-axis.
     * @param zlimit The depth limit defining the size or spread of the particle patterns along the Z-axis.
     * @param ring The `Particle.DustOptions` object specifying the properties (e.g., color, size) of the outer-ring particles.
     * @param inside The `Particle.DustOptions` object specifying the properties (e.g., color, size) of the inner particles.
     */
    public static void createPortalParticles(Location center, double xlimit, double ylimit, double zlimit, Particle.DustOptions ring, Particle.DustOptions inside) {
        boolean circle = false;
        if (circle) {
            drawverticleCircle(center.add(0,2,0),2.1,center.getWorld(),32, Particle.DUST,new Particle.DustOptions(Color.GREEN,16f));
            for (double iteration = 0; iteration <= 2; iteration = iteration + 0.1) {
                drawverticleCircle(center,0 + iteration,center.getWorld(), (int) (16 + Math.round(iteration)),Particle.DUST,new Particle.DustOptions(Color.BLACK,128f));
            }
        } else {
            double rot = center.getRotation().yaw();
            drawVerticleSquare(center,4,rot, Particle.DUST,ring,xlimit,0,0);
            drawVerticleSquare(center,3,rot, Particle.DUST,inside,xlimit,0,0);
            drawVerticleSquare(center,2,rot, Particle.DUST,inside,xlimit,0,0);
            drawVerticleSquare(center,1,rot, Particle.DUST,inside,xlimit,0,0);
        }
    }

    /**
     * Draws a vertical circle in the specified world using particles.
     *
     * @param center   The center location of the circle.
     * @param radius   The radius of the circle.
     * @param world    The world where the circle will be drawn.
     * @param points   The number of points (or particles) to use for forming the circle.
     * @param particle The type of particle to use for drawing the circle.
     * @param options  The particle options to be applied if the particle type is {@link Particle#DUST}.
     */
    public static void drawverticleCircle(Location center, double radius, World world, int points, Particle particle, Particle.DustOptions options) {
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ(); // Calculate Z coordinate
            double y = center.getY() + (radius * Math.sin(angle)); // Y remains constant for a flat circle
            // Create a new location for the point
            Location pointLocation = new Location(world, x, y, z);

            // 2. Spawn particles
            if (particle == Particle.DUST) {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 1, options);
            } else {
                world.spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Creates and returns an ItemStack representing a player head with the specified player's skin.
     *
     * @param player The player whose skin will be applied to the player head.
     * @return An ItemStack representing a player head with the specified player's skin.
     */
    public static ItemStack getSkull(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a custom player head item with a texture based on the provided Base64-encoded string.
     *
     * @param base64 the Base64-encoded texture string to apply to the player head.
     *               If the string is empty, a plain player head is returned.
     * @return an {@link ItemStack} representing the custom player head with the specified texture.
     */
    public static ItemStack getCustomSkull(String base64) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (base64.isEmpty()) return head;

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = skullMeta.getPlayerProfile();
        profile = Bukkit.createProfile(UUID.randomUUID(),null);
        profile.getProperties().add(new ProfileProperty("textures", base64));
        skullMeta.setPlayerProfile(profile);
        head.setItemMeta(skullMeta);
        return head;
    }

    /**
     * Retrieves the skin property of a specified player.
     *
     * @param player the player whose skin property is to be retrieved
     * @return the skin property of the player, or null if not found
     * @throws DataRequestException if there is an error during the skin data retrieval process
     */
    public static SkinProperty getPlayerSkin(Player player) throws DataRequestException {
        PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
        Optional<SkinProperty> property = playerStorage.getSkinForPlayer(
                player.getUniqueId(),
                player.getName()
        );
        return property.orElse(null);
    }

    /**
     * Determines the soul type of a given player.
     *
     * @param player The player whose soul type is to be determined. The player must have
     *               their soul type stored in the persistent data container.
     * @return The determined soul type of the player. If the player does not have a soul
     *         type stored, it defaults to {@code SoulTypes.Red} and opens a selection UI.
     */
    public static SoulTypes getSoulType(Player player) {
        if (!player.getPersistentDataContainer().has(FreedomKeys.soul(), PersistentDataType.STRING)) {
            selectionUi.open_UI(player,new BaseRed());
            return SoulTypes.Red;
        }
        return SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
    }

    /**
     * Sets the skin of a player based on the specified skin name.
     *
     * @param player   The player whose skin is to be updated.
     * @param skinName The name of the skin to apply to the player.
     * @throws MineSkinException      If an error occurs while processing the skin data.
     * @throws DataRequestException   If an error occurs while requesting skin data.
     */
    public static void setSkinByName(Player player, String skinName) throws MineSkinException, DataRequestException {
        SkinStorage skinStorage = skinsRestorerAPI.getSkinStorage();
        PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
        // Find or fetch the skin data
        Optional<InputDataResult> result = skinStorage.findOrCreateSkinData(skinName);
        if (result.isPresent()) {
            // Set the skin identifier for the player
            playerStorage.setSkinIdOfPlayer(
                    player.getUniqueId(),
                    result.get().getIdentifier()
            );
            // Apply the skin visually
            skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player);
        }
    }

    /**
     * Sets the skin of a given player using a skin image retrieved from a specified URL.
     *
     * @param player The player whose skin is to be set.
     * @param url The URL of the skin image to be applied. Must be a valid image link.
     * @throws MineSkinException If an error occurs while generating the skin using the MineSkin API.
     * @throws DataRequestException If there is an issue during the data request to fetch the skin information.
     */
    public static void setSkinByUrl(Player player, String url) throws MineSkinException, DataRequestException {
        MineSkinAPI mineSkinAPI = skinsRestorerAPI.getMineSkinAPI();
        // Generate skin from URL (use CLASSIC as default)
        MineSkinResponse response = mineSkinAPI.genSkin(url, SkinVariant.SLIM);
        SkinProperty skinProperty = response.getProperty();
        // Apply directly to player
        skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player, skinProperty);
    }

    /**
     * Sets the skin of the specified player using the provided skin properties.
     *
     * @param player the player whose skin is to be updated
     * @param value the value of the skin property
     * @param signature the signature of the skin property
     */
    public static void setSkinByProperties(Player player, String value, String signature) {
        SkinProperty skinProperty = SkinProperty.of(value, signature);
        // Apply directly to player

        skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player, skinProperty);
    }

    /**
     * Draws a sphere by spawning particles at locations calculated around the given center point.
     *
     * @param center   The central location of the sphere.
     * @param radius   The radius of the sphere.
     * @param points   The number of points to use for each dimension in rendering the sphere.
     * @param particle The type of particle to use for the sphere.
     * @param options  The options for the particle if the particle type is {@code Particle.DUST}, otherwise ignored.
     */
    public static void drawSphere(Location center, double radius, int points,Particle particle, Particle.DustOptions options) {
        //x coordinate = cos(a)*cos(b)
        //y coordinate = sin(a)*cos(b)
        //z coordinate = sin(b)
        for (int i = 0; i < points; i++) {
            double a = Math.toRadians(i * 360.0 / points);
            for (int j = 0; j < points; j++) {
                double b = Math.toRadians(j * 180.0 / points - 90);
                double x = center.getX() + radius * Math.cos(a) * Math.cos(b);
                double y = center.getY() + radius * Math.sin(a) * Math.cos(b);
                double z = center.getZ() + radius * Math.sin(b);
                Location pointLocation = new Location(center.getWorld(), x, y, z);
                if (particle == Particle.DUST) {
                    center.getWorld().spawnParticle(particle, pointLocation, 1, 0, 0, 0, 1, options);
                } else {
                    center.getWorld().spawnParticle(particle, pointLocation, 1, 0, 0, 0, 0);
                }
            }
        }

    }


    /**
     * Draws a line in the world by spawning particles between two given locations.
     *
     * @param start    the starting location of the line
     * @param end      the ending location of the line
     * @param world    the world in which the line will be drawn
     * @param points   the number of points (particles) to spawn along the line
     * @param particle the type of particle to use for drawing the line
     * @param options  additional options for customizing the appearance of the particles
     */
    public static void drawLine(Location start, Location end, World world, int points, Particle particle, Particle.DustOptions options) {
        double dx = (end.getX() - start.getX()) / points;
        double dy = (end.getY() - start.getY()) / points;
        double dz = (end.getZ() - start.getZ()) / points;

        for (int i = 0; i <= points; i++) {
            Location point = start.clone().add(dx * i, dy * i, dz * i);
            spawn(world, point, particle, options);
        }
    }

    /**
     * Draws a line between two locations in a specified world using particles.
     *  QUICK DISCLAIMER, THIS IS DIFFERENT THAN OTHER DRAW LINE BECAUSE THIS IS MEANT FOR GENERAL PARTICLES
     * @param start    The starting location of the line.
     * @param end      The ending location of the line.
     * @param world    The world in which the particles will be displayed.
     * @param points   The number of points (or segments) along the line where particles will be spawned.
     * @param particle The type of particle to display along the line.
     * @param offsetX  The offset for the particle's movement along the X-axis.
     * @param offsetY  The offset for the particle's movement along the Y-axis.
     * @param offsetZ  The offset for the particle's movement along the Z-axis.
     * @param extra    Additional data or modifiers for the particle, such as speed or size, depending on the particle type.
     * @param color    The color of the particle, if the particle type supports coloring.
     */
    public static void drawLine(Location start, Location end, World world, int points, Particle particle,int offsetX, int offsetY, int offsetZ, int extra, Color color) {
        double dx = (end.getX() - start.getX()) / points;
        double dy = (end.getY() - start.getY()) / points;
        double dz = (end.getZ() - start.getZ()) / points;

        for (int i = 0; i <= points; i++) {
            Location point = start.clone().add(dx * i, dy * i, dz * i);
            world.spawnParticle(particle, point, 1,offsetX, offsetY, offsetZ, extra, color);
        }
    }

    /**
     * Draws a line between two locations in the specified world using particles.
     *
     * @param start    The starting location of the line.
     * @param end      The ending location of the line.
     * @param world    The world in which the line will be drawn.
     * @param points   The number of points to distribute along the line, determining its density.
     * @param particle The type of particle to use for drawing the line.
     */
    public static void drawLine(Location start, Location end, World world, int points, Particle particle) {
        double dx = (end.getX() - start.getX()) / points;
        double dy = (end.getY() - start.getY()) / points;
        double dz = (end.getZ() - start.getZ()) / points;

        for (int i = 0; i <= points; i++) {
            Location point = start.clone().add(dx * i, dy * i, dz * i);
            world.spawnParticle(particle, point, 1);
        }
    }

    /**
     * Spawns a particle at the specified location in the given world.
     * If the particle type is Particle.DUST and the dust options are provided,
     * the particle will be spawned with the specified options. Otherwise,
     * the particle will be spawned using default values.
     *
     * @param world    the world in which the particle will be spawned
     * @param loc      the location where the particle will appear
     * @param particle the type of particle to spawn
     * @param options  the options for the particle if it is of type Particle.DUST,
     *                 otherwise null
     */
    private static void spawn(World world, Location loc, Particle particle, Particle.DustOptions options) {
        if (particle == Particle.DUST && options != null) {
            world.spawnParticle(particle, loc, 1, 0, 0, 0, 1, options);
        } else {
            world.spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Draws an isosceles triangle using particles in the specified world.
     *
     * @param center   The center location of the triangle's base.
     * @param size     The size of the triangle. This determines the distance from the center to the vertices.
     * @param world    The world in which the triangle will be drawn.
     * @param points   The number of points to use for drawing the edges of the triangle.
     * @param particle The type of particle to use for drawing the triangle.
     * @param options  Additional options for the particle appearance, such as color (if applicable).
     */
    public static void drawIsoscelesTriangle(Location center, double size, World world, int points, Particle particle, Particle.DustOptions options) {
        Location top = center.clone().add(0, 0, -size);
        Location left = center.clone().add(-size, 0, size);
        Location right = center.clone().add(size, 0, size);
        left = rotpointX(center, center.getYaw() + 180, left);
        top = rotpointX(center, center.getYaw() + 180, top);
        right = rotpointX(center, center.getYaw() + 180, right);
        Freedom.get_plugin().getLogger().info(String.valueOf(center.getYaw()) + " " + String.valueOf(Math.toDegrees(center.getYaw())));
        drawLine(top, left, world, points, particle, options);
        drawLine(left, right, world, points, particle, options);
        drawLine(right, top, world, points, particle, options);
    }

    /**
     * Draws an isosceles triangle in the given world using particles, based on the specified parameters.
     *
     * @param center the central location of the triangle, which determines its position
     * @param size the size of the triangle, affecting the distance from the center to each vertex
     * @param world the world in which the triangle will be drawn
     * @param points the number of points to be used for rendering the lines of the triangle
     * @param particle the type of particle to be used for drawing the triangle
     * @param options particle rendering options to be applied, such as color or size
     * @param offset an offset applied to the triangle's vertices to adjust its position
     */
    public static void drawIsoscelesTriangle(Location center, double size, World world, int points, Particle particle, Particle.DustOptions options,Location offset) {

        Location top = center.clone().add(0, 0, -size);
        Location left = center.clone().add(-size, 0, size).add(offset.getX(),offset.getY(),offset.getZ());
        Location right = center.clone().add(size, 0, size).add(offset.getX(),offset.getY(),offset.getZ());
        left = rotpointX(center, center.getYaw() + 180, left);
        top = rotpointX(center, center.getYaw() + 180, top);
        right = rotpointX(center, center.getYaw() + 180, right);
        Freedom.get_plugin().getLogger().info(String.valueOf(center.getYaw()) + " " + String.valueOf(Math.toDegrees(center.getYaw())));
        drawLine(top, left, world, points, particle, options);
        drawLine(left, right, world, points, particle, options);
        drawLine(right, top, world, points, particle, options);
    }

    /**
     * Draws a spiral originating from a specified center location in the given world.
     * The spiral expands outward based on the specified radius and completes a number of turns,
     * with a configurable number of points and visual appearance.
     *
     * @param center   The central location from which the spiral originates.
     * @param radius   The maximum radius of the spiral at its furthest point from the center.
     * @param turns    The number of complete turns the spiral will make.
     * @param world    The world in which the spiral will be drawn.
     * @param points   The number of particle points used to draw the spiral, affecting its smoothness.
     * @param particle The type of particle used to render the spiral.
     * @param options  Additional options for customizing the appearance of the particle.
     */
    public static void drawSpiral(Location center, double radius, int turns, World world, int points, Particle particle, Particle.DustOptions options) {
        for (int i = 0; i < points; i++) {
            double progress = (double) i / points;
            double angle = progress * turns * 2 * Math.PI;

            double currentRadius = radius * progress;

            double x = center.getX() + currentRadius * Math.cos(angle);
            double z = center.getZ() + currentRadius * Math.sin(angle);
            double y = center.getY(); // height increase

            spawn(world, new Location(world, x, y, z), particle, options);
        }
    }


    /**
     * Draws a star-shaped pattern in the world using particles.
     *
     * @param center The center location of the star.
     * @param radius The radius of the outer points of the star.
     * @param world The world in which the star will be drawn.
     * @param points The number of particles to draw between each point of the star.
     * @param particle The particle type to use for drawing the star.
     * @param options The dust options for the particle, used if the particle type supports it.
     */
    public static void drawStar(Location center, double radius, World world, int points, Particle particle, Particle.DustOptions options) {
        int vertices = 5;
        Location[] pts = new Location[vertices * 2];

        for (int i = 0; i < vertices * 2; i++) {
            double angle = Math.PI * i / vertices;
            double r = (i % 2 == 0) ? radius : radius / 2;

            double x = center.getX() + r * Math.cos(angle);
            double z = center.getZ() + r * Math.sin(angle);

            pts[i] = new Location(world, x, center.getY(), z);
        }

        for (int i = 0; i < pts.length; i++) {
            drawLine(rotpointX(center,center.getYaw() + 15, pts[i]), rotpointX(center,center.getYaw()  + 15, pts[(i + 1) % pts.length]), world, points, particle, options);
        }
    }

    /**
     * Elevates the target player and performs visual effects based on their soul type.
     * This method creates a star effect with alternating colors around the target player
     * for a duration of 10 ticks.
     *
     * @param target The player to be affected and elevated. This method uses the player's
     *               current location and persistent data to determine the behavior and effects.
     */
    public static void Lift(Player target) {
        Location loc = target.getLocation();
        SoulTypes soulType = SoulTypes.valueOf(target.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                Location loc = target.getLocation();

                switch (tick) {
                    case 1,3,5,7,9 -> drawStar(loc,8,loc.getWorld(),128, Particle.DUST, new Particle.DustOptions(Color.AQUA, 8.0f));
                    case 0,2,4,6,8,10 -> drawStar(loc,8,loc.getWorld(),128, Particle.DUST, new Particle.DustOptions(Color.WHITE, 8.0f));
                }
                tick++;
                if (tick == 10) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 5);
    }

    /**
     * Draws an exclamation point at the specified location using particles.
     *
     * @param center   The central location where the exclamation point will be drawn.
     * @param size     The size of the exclamation point. This determines the length of the shaft and the size of the circle.
     * @param points   The number of points used to draw the lines and circle, affecting the visual smoothness.
     * @param particle The particle type used to render the exclamation point.
     * @param options  Additional options for customizing the appearance of the particle (e.g., color or transition effects).
     * @param offset   The offset to apply to the particle positions during drawing.
     */
    public static void drawExclamationPoint(Location center, double size, int points, Particle particle, Particle.DustOptions options, Location offset) {
        Location tip = center.clone().add(0, 0, size / 8 - 3.5);
        Location back = center.clone().add(0, 0, size);
        double rot = center.getRotation().yaw();
        World world = center.getWorld();

        tip = rotpointX(center, rot, tip);
        back = rotpointX(center, rot, back);
        // Shaft
        drawLine(center, back, world, points, particle, options);
        //circle
        drawCircle(tip, size / 6, world, points, particle, options);
    }

    /**
     * Draws a danger symbol composed of an exclamation point and an enclosing isosceles triangle.
     *
     * @param center   The central location where the danger symbol will be drawn.
     * @param size     The size factor for scaling the danger symbol.
     * @param points   The resolution or number of points for rendering the shapes.
     * @param particle The type of particle to use for drawing the symbol.
     * @param outside  The visual options for the particles used in the outer triangle.
     * @param inside   The visual options for the particles used in the exclamation point.
     */
    public static void drawDangerSymbol(Location center, double size, int points, Particle particle, Particle.DustOptions outside, Particle.DustOptions inside) {
        drawExclamationPoint(center, size, points, particle, inside,new Location(center.getWorld(),0,0,1));
        drawIsoscelesTriangle(center, size * 1.5,center.getWorld(), points,particle,outside,new Location(center.getWorld(),0,0,-2.5));
    }

    /**
     * Draws a heart shape in the world using the specified particle effect.
     *
     * @param center The central location of the heart shape.
     * @param size The size multiplier for the heart shape.
     * @param world The world in which the particles will be displayed.
     * @param points The number of points used to define the heart's shape. A higher value will result in smoother curves.
     * @param particle The particle effect to use for drawing the heart shape.
     * @param options The dust options to customize the appearance of the particles, applicable if the particle type supports it.
     */
    public static void drawHeart(Location center, double size, World world, int points, Particle particle, Particle.DustOptions options) {
        for (int i = 0; i < points; i++) {
            double t = Math.PI * 2 * i / points;

            double x = size * 16 * Math.pow(Math.sin(t), 3);
            double z = size * (13 * Math.cos(t) - 5 * Math.cos(2 * t)
                    - 2 * Math.cos(3 * t) - Math.cos(4 * t));

            Location point = center.clone().add(x * 0.05, 0, z * 0.05);
            point = rotpointX(center, center.getYaw(), point);
            spawn(world, point, particle, options);
        }
    }

    /**
     * Retrieves a list of nearby trusted players within a specified radius of a given player.
     * A player is considered trusted if both players mutually trust each other.
     *
     * @param player the player for whom nearby trusted players are to be retrieved
     * @param radius the radius within which to search for other trusted players
     * @return a list of players who are within the specified radius and are mutually trusted by the given player
     */
    public static List<Player> getNearbyTrusted(Player player,int radius) {
        List<Player> players = new ArrayList<>();
        player.getLocation().getNearbyEntitiesByType(Player.class,radius).forEach( iterated -> {
                if (TrustManager.isTrustedBy(player,iterated) && TrustManager.isTrustedBy(iterated,player) && !iterated.equals(player)) {
                    players.add(iterated);
                }
        });
        return players;
    }

    /**
     * Retrieves the smelting result of the given item stack based on furnace recipes.
     *
     * @param item The {@code ItemStack} to check for a smelting result. Must not be null.
     * @return The {@code ItemStack} that represents the smelted result, with the same amount as the input {@code item}.
     *         Returns null if no matching furnace recipe is found.
     */
    public static ItemStack getSmeltingResult(ItemStack item) {
        ItemStack result = null;
        Iterator<Recipe> iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            if (!(recipe instanceof FurnaceRecipe)) continue;
            if (((FurnaceRecipe) recipe).getInput().getType() != item.getType()) continue;
            result = recipe.getResult();
            break;
        }
        result.setAmount(item.getAmount());
        return result;
    }

    /**
     * Checks if the given material can be smelted using a furnace recipe.
     *
     * @param material the material to check for smelting compatibility
     * @return {@code true} if the material can be smelted, {@code false} otherwise
     */
    public static boolean isSmeltable(Material material) {
        // Iterate through all server recipes
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();

            // Check if the recipe is a smelting recipe
            if (recipe instanceof FurnaceRecipe) {
                FurnaceRecipe furnaceRecipe = (FurnaceRecipe) recipe;

                // Check if our material matches the recipe's input
                if (furnaceRecipe.getInput().getType() == material) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Applies a temporary silence effect to a player for the specified number of seconds.
     * During the silence period, the silence state is tracked and removed automatically
     * after the duration expires.
     *
     * @param player The player to whom the silence effect will be applied.
     * @param seconds The duration of the silence effect in seconds.
     */
    public static void silenceFor(Player player, int seconds) {
        new BukkitRunnable() {
            int tick = 0;
            double tickGoal = secondsToTicks(seconds);
            @Override
            public void run() {
                tick += 1;
                if (tick <= 10) {
                    player.getPersistentDataContainer().set(FreedomKeys.silence(), PersistentDataType.BOOLEAN, false);
                    player.getPersistentDataContainer().set(FreedomKeys.silence(), PersistentDataType.BOOLEAN, true);
                }
                if (tick >= 10 && !player.getPersistentDataContainer().getOrDefault(keygen("silence"), PersistentDataType.BOOLEAN, false)) {
                    this.cancel();
                }
                if (tick >= tickGoal) {
                    player.getPersistentDataContainer().remove(FreedomKeys.silence());
                    this.cancel();
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0,0);
    }

    /**
     * Checks if the given ItemStack is null or represents air.
     *
     * @param stack the ItemStack to be checked
     * @return true if the ItemStack is null or its type is Material.AIR, false otherwise
     */
    public static boolean isItemNull(ItemStack stack) {
        try {
            return stack == null || stack.getType() == Material.AIR;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Retrieves an Enchantment object associated with the specified key.
     *
     * @param key the unique string key representing the enchantment within the Minecraft namespace.
     *            This parameter must not be null.
     * @return the Enchantment corresponding to the given key, never null.
     */
    @NotNull
    public static Enchantment getEnchantment(@NotNull @KeyPattern.Value String key) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(Key.key(Key.MINECRAFT_NAMESPACE, key));
    }

    /**
     * Constructs a colored potion bottle with the provided keys and values stored as persistent data.
     *
     * @param keys   A list of NamespacedKeys to store in the potion's persistent data container.
     * @param values A list of strings corresponding to the keys to be stored as persistent data.
     * @param color  The color of the potion.
     * @return An ItemStack representing the colored potion bottle with the specified persistent data.
     */
    public static ItemStack constructColoredBottle(List<NamespacedKey> keys,List<String> values, Color color) {
        ItemStack itemStack = ItemStack.of(Material.POTION);
        PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
        meta.setColor(color);
        keys.forEach(key -> {
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, values.get(keys.indexOf(key)));
        });
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Deals true damage to a living entity, bypassing armor and resistances. If the entity's health
     * drops to or below zero, it will be killed and the standard death event will be triggered.
     *
     * @param entity The living entity to deal true damage to. If the entity is already dead, no action is taken.
     * @param amount The amount of true damage to deal to the entity. This damage directly subtracts from the entity's health.
     */
    public static void dealTrueDamage(LivingEntity entity, double amount) {
        // Check if the entity is dead already
        if (entity.isDead()) return;

        // Get current health
        double currentHealth = entity.getHealth();
        double newHealth = currentHealth - amount;

        if (newHealth <= 0) {
            //Kill
            Freedom.get_plugin().getLogger().info("Killing " + entity.getName());
            entity.setHealth(0); // This triggers the standard death event
        } else {
            Freedom.get_plugin().getLogger().info("Dealing " + amount + " damage to " + entity.getName());
            entity.setHealth(newHealth);
        }
        entity.playHurtAnimation(45);
    }

    /**
     * Creates and returns an ItemStack representing a fireball.
     * The item uses the material FIRE_CHARGE and assigns a custom item model
     * associated with the "fireball" NamespacedKey.
     *
     * @return an ItemStack configured as a fireball with a specific item model.
     */
    public static ItemStack Fireball() {
        ItemStack Workingitem = ItemStack.of(Material.FIRE_CHARGE);
        ItemMeta meta = Workingitem.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("fireball"));
        Workingitem.setItemMeta(meta);
        return Workingitem;
    }

    /**
     * Initializes a fireball as an {@link ItemDisplay} entity, spawns it in the player's world
     * with the appropriate location and orientation, and sets its item stack to a fireball.
     *
     * @param fireball The entity to initialize. This is replaced with the newly spawned {@link ItemDisplay} entity.
     * @param player The player whose location and direction are used to position and orient the fireball.
     * @return The initialized {@link ItemDisplay} entity representing the fireball.
     */
    public static ItemDisplay initFireball(Entity fireball, Player player) {
        fireball = player.getWorld().spawnEntity(player.getLocation().clone().add(player.getLocation().getDirection().multiply(1.25)), EntityType.ITEM_DISPLAY);
        ItemDisplay itemDisplay = (ItemDisplay) fireball;
        itemDisplay.setItemStack(Fireball());
        itemDisplay.teleport(player.getLocation().add(player.getLocation().getDirection()));
        itemDisplay.setRotation(player.getYaw(),player.getPitch());
        return itemDisplay;
    }

    /**
     * Draws an arrow in the specified world using particles. The arrow is composed of an isosceles triangle
     * representing the arrowhead and a line representing the shaft.
     *
     * @param center   The location representing the center of the arrow.
     * @param size     The size of the arrow, determining the length and dimensions.
     * @param world    The world where the arrow will be drawn.
     * @param points   The number of points used to draw the arrow's shapes (affects smoothness).
     * @param particle The particle type used to render the arrow.
     * @param options  The particle options for customization, such as color or size.
     * @param rot      The rotation of the arrow in degrees, applied about the center.
     */
    public static void drawArrow(Location center, double size, World world, int points, Particle particle, Particle.DustOptions options,float rot) {
        Location tip = center.clone().add(0, 0, -size / 2);
        Location back = center.clone().add(0, 0, size);
        tip = rotpointX(center, rot + 180, tip);
        back = rotpointX(center, rot + 180, back);
        // Arrow head
        drawIsoscelesTriangle(tip, size / 2, world, points, particle, options);

        // Shaft
        drawLine(center, back, world, points, particle, options);
    }
}
/*
You finally finished it... good work... 
 */