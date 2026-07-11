package xyz.yaszu.freedom.Subsystems;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;

import static xyz.yaszu.freedom.Util.Util.*;

public class SoulImbueManager  implements Listener {
    @EventHandler
    public void ApplyBuff(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (player.getInventory().getItemInMainHand() != null) {
                if (isImbued(player.getInventory().getItemInMainHand())) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    SoulTypes soulType;
                    if (item.getPersistentDataContainer().has(keygen("soultwo"), PersistentDataType.STRING)) {
                        soulType = SoulTypes.valueOf(item.getPersistentDataContainer().get(keygen("soultwo"), PersistentDataType.STRING));
                    } else {
                        soulType = SoulTypes.valueOf(item.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
                    }
                    if (soulType == null) {
                        return;
                    }
                    switch (soulType) {
                        case Red,BaseRed -> {
                            event.getEntity().setFireTicks(100);
                            player.getLocation().getWorld().playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1, 1);
                        }
                        case Green,BaseGreen -> {
                            player.setHealth(player.getHealth() + 1);
                        }
                        case Purple,BasePurple -> {
                            //more xp
                            int rand = random.nextInt(0,10);
                            if (rand > 9) {
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                player.teleport(event.getEntity().getLocation());
                                drawStar(player.getLocation(),8,player.getWorld(),32, Particle.DUST,new Particle.DustOptions(Color.YELLOW,1f));
                            }
                        }
                        case Yellow,BaseYellow,Blue,BaseBlue -> {
                            if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
                                // damage = 0 is full health, higher = more used
                                damageable.setDamage(Math.max(0,damageable.getDamage() - 2));
                                item.setItemMeta((ItemMeta) damageable);
                                player.getPersistentDataContainer().set(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE,player.getPersistentDataContainer().get(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE) - 5);
                            }
                        }
                        case Orange,BaseOrange -> {
                            //turn into a cat
                            Random random = new Random();
                            int rand = random.nextInt(0,10);
                            if (rand > 8) {
                                drawSpiral(player.getLocation(),8,3,player.getWorld(),32, Particle.DUST,new Particle.DustOptions(Color.ORANGE,1f));
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_PURR, 1, 1);
                                player.addPotionEffect(PotionEffectType.SATURATION.createEffect(40,0));
                            }
                        }
                        case Cafe,BaseCafe,Mocha,BaseMocha -> {
                            Random random = new Random();
                            int rand = random.nextInt(0,10);
                            if (rand > 8) {
                                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                                player.addPotionEffect(PotionEffectType.HASTE.createEffect(40, 0));
                                player.addPotionEffect(PotionEffectType.SPEED.createEffect(40, 0));
                            }
                        }
                        case Black,BaseBlack -> {
                            int rand = random.nextInt(0,11);
                            if (rand > 9) {
                                drawStar(player.getLocation(),8,player.getWorld(),32, Particle.DUST,new Particle.DustOptions(Color.YELLOW,1f));
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1, 1);
                                player.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(40,0));
                            }
                        }
                    }
                }
            }
        }
    }




    public static boolean isImbued(ItemStack item) {
        if (item.getItemMeta() == null) return false;
        return item.getItemMeta().getPersistentDataContainer().has(keygen("soul")) || item.getItemMeta().getPersistentDataContainer().has(keygen("soultwo"));
    }
    public static boolean isNotSelfImbued(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().has(keygen("soul"));
    }

    public SoulImbueManager() {
        startSyncTask();
        loadVisits();
    }

    private void startSyncTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID visitorUuid : activeVisitsByVisitor.keySet()) {
                    UUID targetUuid = activeVisitsByVisitor.get(visitorUuid);
                    Player visitor = Bukkit.getPlayer(visitorUuid);
                    Player target = Bukkit.getPlayer(targetUuid);
                    Mannequin mannequin = mannequins.get(visitorUuid);
                    Location location = returnLocations.get(visitorUuid);
                    location.getChunk().load();
                    boolean isindoublevoid = visitor.getWorld().getName().equals("doublevoid");
                    if (visitor == null || !visitor.isOnline() || target == null || !target.isOnline() || mannequin == null || !isindoublevoid) {
                        if (visitor != null && visitor.isOnline()) {
                            endVisit(visitor);
                            try {
                                mannequin.remove();
                            }catch (Exception ignored){}

                        }
                        endVisit(visitor);
                        continue;
                    }
                    // Sync target -> mannequin
                    syncMannequinHealth(mannequin, visitor);
                    mannequin.setFireTicks(visitor.getFireTicks());
                    mannequin.getEquipment().setHelmet(target.getInventory().getHelmet());
                    mannequin.getEquipment().setChestplate(visitor.getInventory().getChestplate());
                    mannequin.getEquipment().setLeggings(visitor.getInventory().getLeggings());
                    mannequin.getEquipment().setBoots(visitor.getInventory().getBoots());
                    mannequin.getEquipment().setItemInMainHand(visitor.getInventory().getItemInMainHand());
                    mannequin.getEquipment().setItemInOffHand(visitor.getInventory().getItemInOffHand());
                    mannequin.teleport(mannequin.getLocation().setDirection(visitor.getLocation().getDirection()));
                    // Clear and add potion effects
                    for (PotionEffect effect : mannequin.getActivePotionEffects()) {
                        mannequin.removePotionEffect(effect.getType());
                    }
                    mannequin.addPotionEffects(target.getActivePotionEffects());
                }
            }
        }.runTaskTimer(xyz.yaszu.freedom.Freedom.get_plugin(), 0, 5); // Sync every 5 ticks
    }




    public void saveVisits() {
        File file = new File(xyz.yaszu.freedom.Freedom.get_plugin().getDataFolder(), "visits.yml");
        YamlConfiguration config = new YamlConfiguration();

        int i = 0;
        for (UUID visitorUuid : activeVisitsByVisitor.keySet()) {
            UUID targetUuid = activeVisitsByVisitor.get(visitorUuid);
            config.set("visits." + i + ".visitor", visitorUuid.toString());
            config.set("visits." + i + ".target", targetUuid.toString());
            config.set("visits." + i + ".returnVisitor", returnLocations.get(visitorUuid));
            config.set("visits." + i + ".returnTarget", returnLocations.get(targetUuid));

            Location boxLoc = visitBoxLocations.get(targetUuid);
            if (boxLoc != null) {
                config.set("visits." + i + ".boxLoc", boxLoc);
            }

            // Mannequin belongs to the visitor's body left in the overworld.
            Mannequin m = mannequins.get(visitorUuid);
            if (m != null) {
                config.set("visits." + i + ".mannequinLoc", m.getLocation());
            } else {
                // If mannequin is not spawned (e.g. reload before players join), use the location from pendingVisits
                Location loc = pendingVisits.get(visitorUuid);
                if (loc != null) {
                    config.set("visits." + i + ".mannequinLoc", loc);
                }
            }
            i++;
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Undo all EditSessions on shutdown
        for (EditSession session : activeVisits.values()) {
            try (EditSession undoSession = WorldEdit.getInstance().newEditSession(session.getWorld())) {
                session.undo(undoSession);
            }
        }
        activeVisits.clear();
    }

    public void loadVisits() {
        File file = new File(xyz.yaszu.freedom.Freedom.get_plugin().getDataFolder(), "visits.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("visits") == null) return;

        for (String key : config.getConfigurationSection("visits").getKeys(false)) {
            UUID visitorUuid = UUID.fromString(config.getString("visits." + key + ".visitor"));
            UUID targetUuid = UUID.fromString(config.getString("visits." + key + ".target"));
            Location retVisitor = config.getLocation("visits." + key + ".returnVisitor");
            Location retTarget = config.getLocation("visits." + key + ".returnTarget");
            Location boxLoc = config.getLocation("visits." + key + ".boxLoc");
            Location mannequinLoc = config.getLocation("visits." + key + ".mannequinLoc");

            // Backward compatibility for older visits.yml that only stored mannequinLoc.
            if (boxLoc == null) {
                boxLoc = mannequinLoc;
            }

            if (boxLoc != null) {
                if (boxLoc.getWorld() == null || !"doublevoid".equals(boxLoc.getWorld().getName())) {
                    // Only restore visit arenas in doublevoid.
                    continue;
                }
                // Restore the cube first
                BlockVector3 center = BlockVector3.at(boxLoc.x(), boxLoc.y(), boxLoc.z());
                int radius = 5;
                World world = BukkitAdapter.adapt(boxLoc.getWorld());
                BlockVector3 pos1 = center.subtract(radius, radius, radius);
                BlockVector3 pos2 = center.add(radius, radius, radius);
                CuboidRegion region = new CuboidRegion(world, pos1, pos2);
                EditSession editSession = WorldEdit.getInstance().newEditSession(world);
        RandomPattern pattern = new RandomPattern();
        pattern.add(BlockTypes.BLACK_CONCRETE.getDefaultState(), 1.0);
        
        // Add light blocks inside the cube
        BlockVector3 lightPos1 = center.subtract(radius - 1, radius - 1, radius - 1);
        BlockVector3 lightPos2 = center.add(radius - 1, radius - 1, radius - 1);
        CuboidRegion lightRegion = new CuboidRegion(world, lightPos1, lightPos2);
        
        try {
            editSession.makeCuboidFaces(region, pattern);
            // Place some light blocks on the ceiling/corners
            editSession.setBlock(center.add(0, radius - 1, 0), BlockTypes.LIGHT.getDefaultState());
            editSession.setBlock(center.add(radius - 1, radius - 1, radius - 1), BlockTypes.LIGHT.getDefaultState());
            editSession.setBlock(center.add(-(radius - 1), radius - 1, -(radius - 1)), BlockTypes.LIGHT.getDefaultState());
            editSession.setBlock(center.add(radius - 1, radius - 1, -(radius - 1)), BlockTypes.LIGHT.getDefaultState());
            editSession.setBlock(center.add(-(radius - 1), radius - 1, radius - 1), BlockTypes.LIGHT.getDefaultState());
            editSession.close();
        } catch (MaxChangedBlocksException ignored) {}

                Location normalizedBoxLoc = boxLoc.getBlock().getLocation();
                activeVisits.put(normalizedBoxLoc, editSession);
                visitBoxLocations.put(targetUuid, normalizedBoxLoc);

                // Re-register metadata
                activeVisitsByVisitor.put(visitorUuid, targetUuid);
                returnLocations.put(visitorUuid, retVisitor);
                returnLocations.put(targetUuid, retTarget);

                // Attempt to spawn mannequin and link players if online
                Player visitor = Bukkit.getPlayer(visitorUuid);
                Player target = Bukkit.getPlayer(targetUuid);

                if (visitor != null && target != null) {
                    spawnMannequin(visitor, mannequinLoc != null ? mannequinLoc : normalizedBoxLoc);
                } else {
                    // They will be handled by join event or startSyncTask's online check if needed
                    // But currently join event is empty. Let's rely on join event.
                    pendingVisits.put(visitorUuid, mannequinLoc != null ? mannequinLoc : normalizedBoxLoc);
                }
            }
        }
        file.delete();
    }

    private void spawnMannequin(Player target, Location mannequinLoc) {
        Mannequin mannequin = mannequinLoc.getWorld().spawn(mannequinLoc, Mannequin.class);
        ResolvableProfile profile = ResolvableProfile.resolvableProfile(target.getPlayerProfile());
        mannequin.setProfile(profile);
        mannequin.getEquipment().setHelmet(target.getInventory().getHelmet());
        mannequin.getEquipment().setChestplate(target.getInventory().getChestplate());
        mannequin.getEquipment().setLeggings(target.getInventory().getLeggings());
        mannequin.getEquipment().setBoots(target.getInventory().getBoots());
        mannequin.getEquipment().setItemInMainHand(target.getInventory().getItemInMainHand());
        mannequin.getEquipment().setItemInOffHand(target.getInventory().getItemInOffHand());
        mannequin.setCustomName(target.getName());
        mannequin.setCustomNameVisible(true);
        
        // Fix for "ghost visible only when moving"
        mannequin.setAI(true);
        mannequin.setInvulnerable(false);
        
        // Ensure mannequin is not hidden by Life_and_Death
        mannequin.getPersistentDataContainer().set(keygen("life"), PersistentDataType.INTEGER, 9);
        syncMannequinHealth(mannequin, target);

        mannequins.put(target.getUniqueId(), mannequin);
    }

    private static void syncMannequinHealth(Mannequin mannequin, Player target) {
        AttributeInstance targetMaxAttr = target.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance mannequinMaxAttr = mannequin.getAttribute(Attribute.MAX_HEALTH);

        if (targetMaxAttr != null && mannequinMaxAttr != null) {
            double targetMax = Math.max(1.0D, targetMaxAttr.getValue());
            if (Math.abs(mannequinMaxAttr.getBaseValue() - targetMax) > 0.001D) {
                mannequinMaxAttr.setBaseValue(targetMax);
            }
        }

        double mannequinMax = mannequinMaxAttr != null ? mannequinMaxAttr.getValue() : 20.0D;
        double syncedHealth = Math.min(Math.max(0.0D, target.getHealth()), mannequinMax);
        mannequin.setHealth(syncedHealth <= 0.0D ? 0.1D : syncedHealth);
    }

    public ConcurrentHashMap<UUID, Location> pendingVisits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ImbueRequest> pendingImbueRequests = new ConcurrentHashMap<>();

    private record ImbueRequest(UUID requesterId, SoulTypes soulType, boolean selfImbue) {}

    private static boolean hasImbuedItem(Player player) {
        return player.getPersistentDataContainer().has(keygen("has_imbued_item"), PersistentDataType.BOOLEAN);
    }

    private static void setHasImbuedItem(Player player, boolean value) {
        if (value) {
            player.getPersistentDataContainer().set(keygen("has_imbued_item"), PersistentDataType.BOOLEAN, true);
        } else {
            player.getPersistentDataContainer().remove(keygen("has_imbued_item"));
        }
    }

    @EventHandler
    public void onMannequinDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Mannequin mannequin) {
            UUID targetUuid = null;
            for (UUID uuid : mannequins.keySet()) {
                if (mannequins.get(uuid).equals(mannequin)) {
                    targetUuid = uuid;
                    break;
                }
            }
            if (targetUuid != null) {
                Player target = Bukkit.getPlayer(targetUuid);
                if (target != null && target.isOnline()) {
                    event.setCancelled(true);
                    target.damage(event.getFinalDamage(), event.getDamager());
                    // Update mannequin health to match player health after damage
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            syncMannequinHealth(mannequin, target);
                        }
                    }.runTaskLater(xyz.yaszu.freedom.Freedom.get_plugin(), 1);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Visitor rejoin: respawn their mannequin when the target is online.
        if (pendingVisits.containsKey(uuid) && activeVisitsByVisitor.containsKey(uuid)) {
            UUID targetUuid = activeVisitsByVisitor.get(uuid);
            Player target = Bukkit.getPlayer(targetUuid);
            if (target != null && target.isOnline()) {
                Location mannequinLoc = pendingVisits.remove(uuid);
                if (mannequinLoc != null) {
                    spawnMannequin(player, mannequinLoc);
                }
            }
        }

        // Target rejoin: if visitor mannequin is pending, restore it when visitor is online.
        for (UUID visitorUuid : activeVisitsByVisitor.keySet()) {
            UUID targetUuid = activeVisitsByVisitor.get(visitorUuid);
            if (targetUuid != null && targetUuid.equals(uuid) && pendingVisits.containsKey(visitorUuid)) {
                Player visitor = Bukkit.getPlayer(visitorUuid);
                if (visitor != null && visitor.isOnline()) {
                    Location mannequinLoc = pendingVisits.remove(visitorUuid);
                    if (mannequinLoc != null) {
                        spawnMannequin(visitor, mannequinLoc);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // If a target quits, we remove their mannequin and put them in pending
        if (mannequins.containsKey(uuid)) {
            Mannequin m = mannequins.remove(uuid);
            pendingVisits.put(uuid, m.getLocation());
            m.remove();
        }

        // We don't necessarily want to end the visit if someone disconnects if we want it to persist
        // But the previous implementation ended it. Let's stick to making it persist.
        // So I will comment out or remove the endVisit on quit if I want it to persist across disconnects too.
        // Actually, the requirement just says server stop/start. 
        // If I want it to persist across server stop/start, it probably should also persist across player disconnects.
    }
    public ConcurrentHashMap<UUID, Location> returnLocations = new ConcurrentHashMap<>();
    public ConcurrentHashMap<UUID, UUID> activeVisitsByVisitor = new ConcurrentHashMap<>(); // Visitor UUID -> Target UUID
    public ConcurrentHashMap<UUID, Mannequin> mannequins = new ConcurrentHashMap<>(); // Target UUID -> Mannequin
    public ConcurrentHashMap<UUID, Location> visitBoxLocations = new ConcurrentHashMap<>(); // Target UUID -> doublevoid box center
    public ConcurrentHashMap<Location, EditSession> activeVisits = new ConcurrentHashMap<>();


    public LiteralCommandNode<CommandSourceStack> visit() {
        return Commands.literal("visit").executes(ctx -> {
            if (ctx.getSource().getSender() instanceof Player player) {
                if (activeVisitsByVisitor.containsKey(player.getUniqueId())) {
                    Freedom.get_plugin().getLogger().info("ENDING VISITING A");
                    endVisit(player);
                    return Command.SINGLE_SUCCESS;
                }

                if (player.getInventory().getItemInMainHand() != null) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (isImbued(item) && isNotSelfImbued(item)) {
                        String ownerUuidStr = item.getItemMeta().getPersistentDataContainer().get(keygen("soulowner"), PersistentDataType.STRING);
                        if (ownerUuidStr == null) {
                            player.sendMessage(dess("<red>Invalid soul owner data."));
                            return Command.SINGLE_SUCCESS;
                        }
                        UUID targetUuid = UUID.fromString(ownerUuidStr);
                        Player target = Bukkit.getPlayer(targetUuid);
                        if (target == null) {
                            player.sendMessage(dess("<red>The soul owner is not online."));
                            return Command.SINGLE_SUCCESS;
                        }

                        if (returnLocations.containsKey(player.getUniqueId())) {
                            player.sendMessage(dess("<red>You are already in a visit!"));
                            return Command.SINGLE_SUCCESS;
                        }
                        if (returnLocations.containsKey(target.getUniqueId())) {
                            player.sendMessage(dess("<red>The target is already being visited!"));
                            return Command.SINGLE_SUCCESS;
                        }

                        startVisit(player, target);

                    } else {
                        player.sendMessage(dess("<red>You must be holding an imbued item (not self-imbued) to visit."));
                    }
                }
            }
            return Command.SINGLE_SUCCESS;
        }).build();
    }

    private void startVisit(Player player, Player target) {
        Location location = new Location(Bukkit.getWorld("doublevoid"), 0, -60, 0);
        while (activeVisits.containsKey(location)) {
            location = location.clone().add(100, 0, 0);
        }

        // Save return points first so mannequin can spawn exactly where the target will return.
        Location returnVisitor = player.getLocation();
        Location returnTarget = target.getLocation();
        returnLocations.put(player.getUniqueId(), returnVisitor);
        returnLocations.put(target.getUniqueId(), returnTarget);

        if (returnTarget.getWorld() == null) {
            player.sendMessage(dess("<red>Visit failed: target return location is invalid."));
            target.sendMessage(dess("<red>Visit failed: your return location is invalid."));
            returnLocations.remove(player.getUniqueId());
            returnLocations.remove(target.getUniqueId());
            return;
        }

        spawnMannequin(player, returnVisitor.clone());
        BlockVector3 center = BlockVector3.at(location.x(), location.y(), location.z());
        int radius = 5;
        World world = BukkitAdapter.adapt(location.getWorld());
        BlockVector3 pos1 = center.subtract(radius, radius, radius);
        BlockVector3 pos2 = center.add(radius, radius, radius);

        CuboidRegion region = new CuboidRegion(world, pos1, pos2);

        EditSession editSession = WorldEdit.getInstance().newEditSession(world);
        RandomPattern pattern = new RandomPattern();
        pattern.add(BlockTypes.BLACK_CONCRETE.getDefaultState(), 1.0);
        
        try {
            editSession.makeCuboidFaces(region, pattern);
            // Place light blocks on the ceiling
            editSession.setBlock(center.add(0, radius - 1, 0), BlockTypes.LIGHT.getDefaultState());
            editSession.setBlock(center.add(radius - 1, radius - 1, radius - 1), BlockTypes.LIGHT.getDefaultState());
            editSession.setBlock(center.add(-(radius - 1), radius - 1, -(radius - 1)), BlockTypes.LIGHT.getDefaultState());
            editSession.setBlock(center.add(radius - 1, radius - 1, -(radius - 1)), BlockTypes.LIGHT.getDefaultState());
            editSession.setBlock(center.add(-(radius - 1), radius - 1, radius - 1), BlockTypes.LIGHT.getDefaultState());
            editSession.close();
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }

        activeVisits.put(location.getBlock().getLocation(), editSession);
        visitBoxLocations.put(target.getUniqueId(), location.getBlock().getLocation());
        activeVisitsByVisitor.put(player.getUniqueId(), target.getUniqueId());

        Location spawnLoc = location.clone().add(2, 1, 2);
        Location targetSpawnLoc = location.clone().add(-2, 1, -2);

        player.teleport(spawnLoc);
        target.teleport(targetSpawnLoc);
        player.sendMessage(dess("<green>You are now visiting " + target.getName() + ". Use /visit again to end."));
        target.sendMessage(dess("<green>" + player.getName() + " is visiting you."));
    }

    private void endVisit(Player player) {
        UUID visitorUuid = player.getUniqueId();
        UUID targetUuid = activeVisitsByVisitor.remove(visitorUuid);
        if (targetUuid == null) return;

        Player target = Bukkit.getPlayer(targetUuid);
        Location returnPlayer = returnLocations.remove(visitorUuid);
        if (returnPlayer != null && player.isOnline()) player.teleport(returnPlayer);

        Location returnTarget = returnLocations.remove(targetUuid);
        if (returnTarget != null && target != null && target.isOnline()) target.teleport(returnTarget);

        Location boxLoc = visitBoxLocations.remove(targetUuid);
        Mannequin mannequin = mannequins.remove(visitorUuid);
        pendingVisits.remove(visitorUuid);
        if (boxLoc == null && mannequin != null) {
            boxLoc = mannequin.getLocation().getBlock().getLocation();
        }
        if (boxLoc != null) {
            EditSession editSession = activeVisits.remove(boxLoc);
            if (editSession != null && boxLoc.getWorld() != null) {
                try (EditSession undoSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(boxLoc.getWorld()))) {
                    undoSession.undo(editSession);
                }
            }
        }
        if (mannequin != null) {
            mannequin.remove();
        }


        player.sendMessage(dess("<red>Visit ended."));
        if (target != null && target.isOnline()) {
            target.sendMessage(dess("<red>The visit has ended."));
        }
    }


    public LiteralCommandNode<CommandSourceStack> imbue() {
        return Commands.literal("imbue").then(Commands.argument("target", ArgumentTypes.player()).executes(ctx -> {
            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
            Player target = targetResolver.resolve(ctx.getSource()).getFirst();
            if (ctx.getSource().getSender() instanceof Player player) {
                if (player.getWorld() == Bukkit.getWorld("doublevoid") || target.getWorld() == Bukkit.getWorld("doublevoid")) {
                    player.sendMessage(dess("<red>You must be in a non-dev world to imbue items."));
                    return Command.SINGLE_SUCCESS;
                }
                if (hasImbuedItem(player)) {
                    player.sendMessage(dess("<red>You have already imbued an item. You can only imbue one item at a time."));
                    return Command.SINGLE_SUCCESS;
                }

                if (target == player) {
                    if (!player.getPersistentDataContainer().has(keygen("ghost"))) {
                        player.sendMessage(dess("<red>Only dead players can imbue items."));
                        return Command.SINGLE_SUCCESS;
                    }
                    if (player.getInventory().getItemInMainHand() != null && !player.getInventory().getItemInMainHand().getType().isAir()) {
                        ItemStack item = player.getInventory().getItemInMainHand();
                        if (isImbued(item)) {
                            player.sendMessage(dess("<red>This item is already imbued."));
                        } else {
                            SoulTypes soul = getSoulType(player);
                            ImbueItem(item, player, soul, true, player);
                            player.sendMessage(dess("<green>You have self-imbued your item with your " + soul.name() + " soul."));
                        }
                    } else {
                        player.sendMessage(dess("<red>You must be holding an item to imbue it."));
                    }
                } else {
                    if (!player.getPersistentDataContainer().has(keygen("ghost"))) {
                        player.sendMessage(dess("<red>Only dead players can imbue items."));
                        return Command.SINGLE_SUCCESS;
                    }
                    if (target.getInventory().getItemInMainHand() != null && !target.getInventory().getItemInMainHand().getType().isAir()) {
                        ItemStack targetItem = target.getInventory().getItemInMainHand();
                        if (isImbued(targetItem)) {
                            player.sendMessage(dess("<red>The target player's item is already imbued."));
                        } else {
                            SoulTypes soul = getSoulType(player);
                            pendingImbueRequests.put(target.getUniqueId(), new ImbueRequest(player.getUniqueId(), soul, false));
                            target.sendMessage(dess("<gold>" + player.getName() + " wants to imbue your held item with their " + soul.name() + " soul. Use /acceptimbue or /denyimbue."));
                            player.sendMessage(dess("<green>Imbue request sent to " + target.getName() + "."));
                        }
                    } else {
                        player.sendMessage(dess("<red>The target player must be holding an item to be imbued."));
                    }
                }
            }
            return Command.SINGLE_SUCCESS;
        })).build();
    }

    public LiteralCommandNode<CommandSourceStack> acceptImbue() {
        return Commands.literal("acceptimbue").executes(ctx -> {
            if (ctx.getSource().getSender() instanceof Player target) {
                ImbueRequest request = pendingImbueRequests.remove(target.getUniqueId());
                if (request == null) {
                    target.sendMessage(dess("<red>You have no pending imbue requests."));
                    return Command.SINGLE_SUCCESS;
                }

                Player requester = Bukkit.getPlayer(request.requesterId());
                if (requester == null || !requester.isOnline()) {
                    target.sendMessage(dess("<red>The requester is no longer online."));
                    return Command.SINGLE_SUCCESS;
                }

                if (hasImbuedItem(requester)) {
                    target.sendMessage(dess("<red>The requester has already imbued another item."));
                    requester.sendMessage(dess("<red>Your imbue request to " + target.getName() + " failed because you already have an imbued item."));
                    return Command.SINGLE_SUCCESS;
                }

                ItemStack item = target.getInventory().getItemInMainHand();
                if (item == null || item.getType().isAir()) {
                    target.sendMessage(dess("<red>You must be holding an item to accept the imbue."));
                    requester.sendMessage(dess("<red>" + target.getName() + " is no longer holding an item."));
                    return Command.SINGLE_SUCCESS;
                }

                if (isImbued(item)) {
                    target.sendMessage(dess("<red>This item is already imbued."));
                    requester.sendMessage(dess("<red>" + target.getName() + "'s item is already imbued."));
                    return Command.SINGLE_SUCCESS;
                }

                ImbueItem(item, requester, request.soulType(), false, requester);
                target.sendMessage(dess("<green>Your item has been imbued by " + requester.getName() + "."));
                requester.sendMessage(dess("<Red> DO NOT LOSE THE IMBUED ITEM"));
                target.sendMessage(dess("<red>DO NOT LOSE THE IMBUED ITEM"));
                requester.sendMessage(dess("<green>You have successfully imbued " + target.getName() + "'s item."));
            }
            return Command.SINGLE_SUCCESS;
        }).build();
    }

    public LiteralCommandNode<CommandSourceStack> denyImbue() {
        return Commands.literal("denyimbue").executes(ctx -> {
            if (ctx.getSource().getSender() instanceof Player target) {
                ImbueRequest request = pendingImbueRequests.remove(target.getUniqueId());
                if (request == null) {
                    target.sendMessage(dess("<red>You have no pending imbue requests."));
                    return Command.SINGLE_SUCCESS;
                }

                target.sendMessage(dess("<red>You denied the imbue request."));
                Player requester = Bukkit.getPlayer(request.requesterId());
                if (requester != null) {
                    requester.sendMessage(dess("<red>" + target.getName() + " denied your imbue request."));
                }
            }
            return Command.SINGLE_SUCCESS;
        }).build();
    }

    public LiteralCommandNode<CommandSourceStack> unimbue() {
        return Commands.literal("unimbue").then(Commands.argument("target",ArgumentTypes.player()).then(Commands.argument("slot", IntegerArgumentType.integer(0,1)).executes(ctx -> {
            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
            Player target = targetResolver.resolve(ctx.getSource()).getFirst();
            if (ctx.getSource().getSender() instanceof Player player) {
                if (target == player) {
                    ItemStack stack = player.getInventory().getItemInMainHand();
                    if (stack != null && !stack.getType().isAir()) {
                        UnImbueItem(stack, ctx.getArgument("slot", Integer.class));
                    }
                } else {
                    if (target.getInventory().getItemInMainHand() != null && !target.getInventory().getItemInMainHand().getType().isAir()) {
                        ItemStack stack = target.getInventory().getItemInMainHand();
                        List<Player> players = getWhoImbued(stack);
                        try {
                            if (players != null && !players.isEmpty()) {
                                if (players.contains(player)) {
                                    UnImbueItem(stack, 0);
                                } else {
                                    player.sendMessage(dess("You can't unimbue this item."));
                                }
                            }
                        } catch (Exception ignored) {}

                    }
                }
            }
            return Command.SINGLE_SUCCESS;
        }))).build();
    }

    public LiteralCommandNode<CommandSourceStack> forceUnimbue() {
        return Commands.literal("forceunimbue").then(Commands.argument("slot", IntegerArgumentType.integer(0, 1)).executes(ctx -> {
            if (ctx.getSource().getSender() instanceof Player player) {
                ItemStack stack = player.getInventory().getItemInMainHand();
                if (stack != null && !stack.getType().isAir()) {
                    int slot = ctx.getArgument("slot", Integer.class);
                    UnImbueItem(stack, slot);
                    player.sendMessage(dess("<green>Successfully unimbued slot " + slot + "."));
                } else {
                    player.sendMessage(dess("<red>You must be holding an item to unimbue it."));
                }
            }
            return Command.SINGLE_SUCCESS;
        })).build();
    }




    public static List<@Nullable Player> getWhoImbued(ItemStack stack) {
        if (isImbued(stack)) {
            if (stack.getItemMeta().getPersistentDataContainer().has(keygen("soulowner")) && stack.getItemMeta().getPersistentDataContainer().has(keygen("soulownertwo"))) {
                return List.of(Bukkit.getPlayer(UUID.fromString(stack.getItemMeta().getPersistentDataContainer().get(keygen("soulowner"), PersistentDataType.STRING))),Bukkit.getPlayer(UUID.fromString(stack.getItemMeta().getPersistentDataContainer().get(keygen("soulownertwo"), PersistentDataType.STRING))));
            }
            if (stack.getItemMeta().getPersistentDataContainer().has(keygen("soulowner"))) {
                return List.of(Bukkit.getPlayer(UUID.fromString(stack.getItemMeta().getPersistentDataContainer().get(keygen("soulowner"), PersistentDataType.STRING))));
            }
            if (stack.getItemMeta().getPersistentDataContainer().has(keygen("soulownertwo"))) {
                return List.of(Bukkit.getPlayer(UUID.fromString(stack.getItemMeta().getPersistentDataContainer().get(keygen("soulownertwo"), PersistentDataType.STRING))));
            }
        }
        return null;
    }

    /**
     * Imbue an item with a soul.
     *
     * @param item   Itemstack to imbue
     * @param player Player who's soul is being imbued.
     * @param soulType soultype to imbue
     * @param selfimbue if the soul is being imbued by the player itself
     */
    public static void ImbueItem(ItemStack item, Player player, SoulTypes soulType, boolean selfimbue, @Nullable Player imbueperson) {
        ItemMeta meta = item.getItemMeta();
        if (selfimbue) {
            meta.getPersistentDataContainer().set(keygen("soultwo"), PersistentDataType.STRING, soulType.name());
            meta.getPersistentDataContainer().set(keygen("soulownertwo"), PersistentDataType.STRING, player.getUniqueId().toString());
        } else {
            meta.getPersistentDataContainer().set(keygen("soul"), PersistentDataType.STRING, soulType.name());
            meta.getPersistentDataContainer().set(keygen("soulowner"), PersistentDataType.STRING, player.getUniqueId().toString());
            if (imbueperson != null) {
                player.getPersistentDataContainer().set(keygen("imbueperson"), PersistentDataType.STRING, imbueperson.getUniqueId().toString());
            }
        }

        List<Component> lore = meta.lore();
        if (lore == null) lore = new java.util.ArrayList<>();
        String imbuerName = (imbueperson != null) ? imbueperson.getName() : player.getName();
        lore.add(dess("<gray>Imbued by: <gold>" + imbuerName));
        meta.lore(lore);

        item.setItemMeta(meta);
        if (imbueperson != null) {
            setHasImbuedItem(imbueperson, true);
        } else {
            setHasImbuedItem(player, true);
        }
    }

    /**
     * Unimbue player item
     *
     * @param item   Itemstack to unimbue
     * @param slot   slot within item to unimbue
     */
    public static void UnImbueItem(ItemStack item, int slot) {
        ItemMeta meta = item.getItemMeta();
        UUID imbuerUuid = null;
        switch (slot) {
            case 0 -> {
                if (meta.getPersistentDataContainer().has(keygen("soulowner"), PersistentDataType.STRING)) {
                    imbuerUuid = UUID.fromString(meta.getPersistentDataContainer().get(keygen("soulowner"), PersistentDataType.STRING));
                }
                meta.getPersistentDataContainer().remove(keygen("soul"));
                meta.getPersistentDataContainer().remove(keygen("soulowner"));
            }
            case 1 -> {
                if (meta.getPersistentDataContainer().has(keygen("soulownertwo"), PersistentDataType.STRING)) {
                    imbuerUuid = UUID.fromString(meta.getPersistentDataContainer().get(keygen("soulownertwo"), PersistentDataType.STRING));
                }
                meta.getPersistentDataContainer().remove(keygen("soultwo"));
                meta.getPersistentDataContainer().remove(keygen("soulownertwo"));
            }
        }

        if (imbuerUuid != null) {
            Player imbuer = Bukkit.getPlayer(imbuerUuid);
            if (imbuer != null) {
                setHasImbuedItem(imbuer, false);
            } else {
                // If offline, we'd need another way to clear it, but for now let's hope they are online or use a more robust system later.
                // Actually, since it's in PDC, we can't easily clear it if they are offline without loading their data.
                // But the requirement says "player can only imbue one item", so if they unimbue it (or someone else does), they should be free.
            }
        }

        List<Component> lore = meta.lore();
        if (lore != null) {
            lore.removeIf(component -> MiniMessage.miniMessage().serialize(component).contains("Imbued by:"));
            meta.lore(lore);
        }

        item.setItemMeta(meta);
    }
    public static void unimbuePlayer(Player player) {
        setHasImbuedItem(player, false);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            for (ItemStack item : onlinePlayer.getInventory().getContents()) {
                if (item != null && isImbued(item)) {
                    ItemMeta meta = item.getItemMeta();
                    boolean modified = false;
                    if (meta.getPersistentDataContainer().has(keygen("soulowner"), PersistentDataType.STRING)) {
                        String ownerUuid = meta.getPersistentDataContainer().get(keygen("soulowner"), PersistentDataType.STRING);
                        if (player.getUniqueId().toString().equals(ownerUuid)) {
                            UnImbueItem(item, 0);
                            modified = true;
                        }
                    }
                    if (!modified && meta.getPersistentDataContainer().has(keygen("soulownertwo"), PersistentDataType.STRING)) {
                        String ownerUuid = meta.getPersistentDataContainer().get(keygen("soulownertwo"), PersistentDataType.STRING);
                        if (player.getUniqueId().toString().equals(ownerUuid)) {
                            UnImbueItem(item, 1);
                        }
                    }
                }
            }
        }
    }
}
