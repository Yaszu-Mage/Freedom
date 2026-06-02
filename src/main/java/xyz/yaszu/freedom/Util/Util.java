package xyz.yaszu.freedom.Util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Blocks.BlockHandler;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi;
import xyz.yaszu.freedom.Soul.Base.BaseRed;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Subsystems.CombatTimer;
import xyz.yaszu.freedom.Subsystems.TrustManager;

import java.util.*;

import static xyz.yaszu.freedom.Blocks.BlockHandler.restoreRotation;
import static xyz.yaszu.freedom.Soul.soulListener.getSoul;
import static xyz.yaszu.freedom.Subsystems.CurrencyManager.getCurrency;

public class Util {
    public Entity getTargetEntity(Player player) {
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

    public PotionEffectType randomPotionEffect(){
        PotionEffectType[] types = PotionEffectType.values();
        Random random = new Random();
        return types[random.nextInt(types.length)];
    }
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
    public static String itemToString(ItemStack item) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item);
        return config.saveToString();
    }
    public static ItemStack stringToItem(String data) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(data);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        return config.getItemStack("item");
    }


    public static String locationToString(Location loc) {
        return loc.getWorld().toString() + "," + loc.getX() + "," +loc.getY() + "," + loc.getZ() + "-";
    }
    public static ArrayList<Location> stringToLocations(String loc) {
        ArrayList<Location> locations = new ArrayList<>();
        String[] parts = loc.split("-");
        for (String location : parts) {
            parts = location.split(",");
            if (parts.length == 4) {
                locations.add(new Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])));
            }
        }
        return locations;
    }

    public PotionEffectType randomPositivePotionEffect(){
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

    public PotionEffectType randomNegativePotionEffect(){
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

    public static ItemStack emptyItem(ItemStack item) {
        ItemMeta workingMeta = item.getItemMeta();
        workingMeta.displayName(dess("\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33"));
        item.setItemMeta(workingMeta);
        return item;
    }
    public static NamespacedKey keygen(String key) {
        return FreedomKeys.key(key);
    }

    public static Vector directionTo(Location start, Location end) {
        return end.toVector().subtract(start.toVector()).normalize();
    }

    public static Component dess(String minimessage) {
        return MiniMessage.miniMessage().deserialize(minimessage);
    }

    public static final Set<UUID> hiddenEntities = new HashSet<>();
    public static ProtocolManager protocolManager;

    private static final Map<UUID, Long> lastPdcRead = new HashMap<>();
    private static final Map<UUID, Map<String, Object>> cachedPdcValues = new HashMap<>();
    private static final long PDC_CACHE_DURATION = 1000; // 1 second in milliseconds

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
    public static Location getMidpoint(Location loc1, Location loc2) {
        return loc1.add(loc2).multiply(0.5);
    }
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





    public void pulseCircle(Location center, double radius, int points, Particle particle, double smallestRadius, int pulsingTime, int pulses,Sound sound,Object options) {
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

    public static void drawEye(Location location, int scale) {
        drawElipse(location, 1 + scale, 128, Particle.DUST, -2.58, 6.6);
        drawElipse(location, 1 + scale, 128, Particle.DUST, -2.58, 3.3);
        drawCircle(location, 2 + 2 * scale, location.getWorld(), 128, Particle.DUST, new Particle.DustOptions(Color.RED, 2.0f));
    }

    public static double getGroundLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return 0d;
        return loc.getWorld().getHighestBlockYAt(
                loc.getBlockX(), loc.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES);
    }

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

    public static void clearPlayerCache(UUID playerUUID) {
        souls.remove(playerUUID);
        abilityOneCooldowns.remove(playerUUID);
        abilityTwoCooldowns.remove(playerUUID);
    }

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
    public static void updateValue(String name,Double value, Player player) {
        Objective objective = getUI(player);
        objective.getScore(name).setScore(value.intValue());
    }



    public static void updateValue(String name,Integer value, Player player) {

    }


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

    public static void multisquare(Location location, int tick, int size, Particle particle, int initialrot, Particle.DustOptions options) {
        Color optionscolor = options.getColor();
        optionscolor = optionscolor.mixColors(Color.RED);
        Particle.DustOptions options1 = new Particle.DustOptions(optionscolor, options.getSize());
        drawSquare(location, size, initialrot + tick, particle, options, 0, 8, 0);
        drawSquare(location, size, initialrot - 45 + tick, particle, options1, 0, 8, 0);
    }

    public static void Vertmultisquare(Location location, int tick, double size, Particle particle, int initialrot, Particle.DustOptions options, double yaw) {
        Color optionscolor = options.getColor();
        optionscolor = optionscolor.mixColors(Color.RED);
        Particle.DustOptions options1 = new Particle.DustOptions(optionscolor, options.getSize());
        drawVerticleSquare(location, size, initialrot + tick, particle, options, 0, 8, 0,true,yaw);
        drawVerticleSquare(location, size, initialrot - 45 + tick, particle, options1, 0, 8, 0,true,yaw);
    }

    // Example: Draw a 5x5 square on the ground around the player
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

    public static enum ParticlePortalState {
        Opening,
        Open,
        Closing
    }
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
    public static void drawverticleCircle(Location center, double radius, World world, int points, Particle particle, Particle.DustOptions options) {
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ(); // Calculate Z coordinate
            double y = center.getY() + (radius * Math.sin(angle));; // Y remains constant for a flat circle

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
    public static ItemStack getSkull(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
        item.setItemMeta(meta);
        return item;
    }
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
    public static SkinProperty getPlayerSkin(Player player) throws DataRequestException {
        PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
        Optional<SkinProperty> property = playerStorage.getSkinForPlayer(
                player.getUniqueId(),
                player.getName()
        );
        return property.orElse(null);
    }

    public static SoulTypes getSoulType(Player player) {
        if (!player.getPersistentDataContainer().has(FreedomKeys.soul(), PersistentDataType.STRING)) {
            selectionUi.open_UI(player,new BaseRed());
            return SoulTypes.Red;
        }
        return SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
    }

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

    public static void setSkinByUrl(Player player, String url) throws MineSkinException, DataRequestException {
        MineSkinAPI mineSkinAPI = skinsRestorerAPI.getMineSkinAPI();
        // Generate skin from URL (use CLASSIC as default)
        MineSkinResponse response = mineSkinAPI.genSkin(url, SkinVariant.SLIM);
        SkinProperty skinProperty = response.getProperty();
        // Apply directly to player
        skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player, skinProperty);
    }

    public static void setSkinByProperties(Player player, String value, String signature) {
        SkinProperty skinProperty = SkinProperty.of(value, signature);
        // Apply directly to player

        skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player, skinProperty);
    }

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



    public static void drawLine(Location start, Location end, World world, int points, Particle particle, Particle.DustOptions options) {
        double dx = (end.getX() - start.getX()) / points;
        double dy = (end.getY() - start.getY()) / points;
        double dz = (end.getZ() - start.getZ()) / points;

        for (int i = 0; i <= points; i++) {
            Location point = start.clone().add(dx * i, dy * i, dz * i);
            spawn(world, point, particle, options);
        }
    }

    public static void drawLine(Location start, Location end, World world, int points, Particle particle,int offsetX, int offsetY, int offsetZ, int extra, Color color) {
        double dx = (end.getX() - start.getX()) / points;
        double dy = (end.getY() - start.getY()) / points;
        double dz = (end.getZ() - start.getZ()) / points;

        for (int i = 0; i <= points; i++) {
            Location point = start.clone().add(dx * i, dy * i, dz * i);
            world.spawnParticle(particle, point, 1,offsetX, offsetY, offsetZ, extra, color);
        }
    }

    private static void spawn(World world, Location loc, Particle particle, Particle.DustOptions options) {
        if (particle == Particle.DUST && options != null) {
            world.spawnParticle(particle, loc, 1, 0, 0, 0, 1, options);
        } else {
            world.spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }


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


    public static void drawDangerSymbol(Location center, double size, int points, Particle particle, Particle.DustOptions outside, Particle.DustOptions inside) {
        drawExclamationPoint(center, size, points, particle, inside,new Location(center.getWorld(),0,0,1));
        drawIsoscelesTriangle(center, size * 1.5,center.getWorld(), points,particle,outside,new Location(center.getWorld(),0,0,-2.5));
    }

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

    public static List<Player> getNearbyTrusted(Player player,int radius) {
        List<Player> players = new ArrayList<>();
        player.getLocation().getNearbyEntitiesByType(Player.class,radius).forEach( iterated -> {
                if (TrustManager.isTrustedBy(player,iterated) && TrustManager.isTrustedBy(iterated,player) && !iterated.equals(player)) {
                    players.add(iterated);
                }
        });
        return players;
    }




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
