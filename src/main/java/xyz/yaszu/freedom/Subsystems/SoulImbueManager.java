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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;

public class SoulImbueManager extends Util implements Listener {


    @EventHandler
    public void ApplyBuff(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (player.getInventory().getItemInMainHand() != null) {
                if (isImbued(player.getInventory().getItemInMainHand())) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    SoulTypes soulType = SoulTypes.valueOf(item.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
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
                        }
                        case Yellow,BaseYellow,Blue,BaseBlue -> {
                            if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
                                // damage = 0 is full health, higher = more used
                                damageable.setDamage(Math.max(0,damageable.getDamage() - 1));
                                item.setItemMeta((ItemMeta) damageable);
                                player.getPersistentDataContainer().set(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE,player.getPersistentDataContainer().get(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE) - 5);
                            }
                        }
                        case Orange,BaseOrange -> {
                            //turn into a cat
                        }
                        case Cafe,BaseCafe -> {

                        }
                        case Black,BaseBlack -> {
                            //Idek
                        }
                        case Mocha,BaseMocha -> {

                        }
                    }
                }
            }
        }
    }




    public static boolean isImbued(ItemStack item) {
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
                    Mannequin mannequin = mannequins.get(targetUuid);

                    if (visitor == null || !visitor.isOnline() || target == null || !target.isOnline() || mannequin == null || !mannequin.isValid()) {
                        if (visitor != null && visitor.isOnline()) {
                            endVisit(visitor);
                        }
                        continue;
                    }

                    // Sync target -> mannequin
                    mannequin.setHealth(target.getHealth());
                    mannequin.setFireTicks(target.getFireTicks());
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
            Mannequin m = mannequins.get(targetUuid);
            if (m != null) {
                config.set("visits." + i + ".mannequinLoc", m.getLocation());
            } else {
                // If mannequin is not spawned (e.g. reload before players join), use the location from pendingVisits
                Location loc = pendingVisits.get(targetUuid);
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
            Location mannequinLoc = config.getLocation("visits." + key + ".mannequinLoc");

            if (mannequinLoc != null) {
                // Restore the cube first
                BlockVector3 center = BlockVector3.at(mannequinLoc.x(), mannequinLoc.y(), mannequinLoc.z());
                int radius = 5;
                World world = BukkitAdapter.adapt(mannequinLoc.getWorld());
                BlockVector3 pos1 = center.subtract(radius, radius, radius);
                BlockVector3 pos2 = center.add(radius, radius, radius);
                CuboidRegion region = new CuboidRegion(world, pos1, pos2);
                EditSession editSession = WorldEdit.getInstance().newEditSession(world);
                RandomPattern pattern = new RandomPattern();
                pattern.add(BlockTypes.BLACK_CONCRETE.getDefaultState(), 1.0);
                try {
                    editSession.makeCuboidFaces(region, pattern);
                } catch (MaxChangedBlocksException ignored) {}

                activeVisits.put(mannequinLoc.getBlock().getLocation(), editSession);

                // Re-register metadata
                activeVisitsByVisitor.put(visitorUuid, targetUuid);
                returnLocations.put(visitorUuid, retVisitor);
                returnLocations.put(targetUuid, retTarget);

                // Attempt to spawn mannequin and link players if online
                Player visitor = Bukkit.getPlayer(visitorUuid);
                Player target = Bukkit.getPlayer(targetUuid);

                if (visitor != null && target != null) {
                    spawnMannequin(target, mannequinLoc);
                } else {
                    // They will be handled by join event or startSyncTask's online check if needed
                    // But currently join event is empty. Let's rely on join event.
                    pendingVisits.put(targetUuid, mannequinLoc);
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
        mannequin.setCustomName(target.getName());
        mannequin.setCustomNameVisible(true);
        mannequin.setHealth(target.getHealth());

        mannequins.put(target.getUniqueId(), mannequin);
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
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Check if the joined player is a target of a pending visit
        if (pendingVisits.containsKey(uuid)) {
            Location mannequinLoc = pendingVisits.get(uuid);
            UUID visitorUuid = null;
            for (UUID vUuid : activeVisitsByVisitor.keySet()) {
                if (activeVisitsByVisitor.get(vUuid).equals(uuid)) {
                    visitorUuid = vUuid;
                    break;
                }
            }

            if (visitorUuid != null) {
                Player visitor = Bukkit.getPlayer(visitorUuid);
                if (visitor != null && visitor.isOnline()) {
                    spawnMannequin(player, mannequinLoc);
                    pendingVisits.remove(uuid);
                }
            }
        }
        
        // Also check if the joined player is a visitor for a pending visit
        for (UUID vUuid : activeVisitsByVisitor.keySet()) {
            if (vUuid.equals(uuid)) {
                UUID targetUuid = activeVisitsByVisitor.get(vUuid);
                if (pendingVisits.containsKey(targetUuid)) {
                    Player target = Bukkit.getPlayer(targetUuid);
                    if (target != null && target.isOnline()) {
                        spawnMannequin(target, pendingVisits.get(targetUuid));
                        pendingVisits.remove(targetUuid);
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
    public ConcurrentHashMap<Location, EditSession> activeVisits = new ConcurrentHashMap<>();


    public LiteralCommandNode<CommandSourceStack> visit() {
        return Commands.literal("visit").executes(ctx -> {
            if (ctx.getSource().getSender() instanceof Player player) {
                if (activeVisitsByVisitor.containsKey(player.getUniqueId())) {
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

        Mannequin mannequin = location.getWorld().spawn(location, Mannequin.class);
        ResolvableProfile profile = ResolvableProfile.resolvableProfile(target.getPlayerProfile());
        mannequin.setProfile(profile);
        mannequin.getEquipment().setHelmet(target.getInventory().getHelmet());
        mannequin.getEquipment().setChestplate(target.getInventory().getChestplate());
        mannequin.getEquipment().setLeggings(target.getInventory().getLeggings());
        mannequin.getEquipment().setBoots(target.getInventory().getBoots());
        mannequin.setCustomName(target.getName());
        mannequin.setCustomNameVisible(true);
        mannequin.setHealth(target.getHealth());
        mannequin.setFireTicks(target.getFireTicks());
        mannequin.setRemainingAir(target.getRemainingAir());
        mannequin.setFallDistance(target.getFallDistance());

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
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }

        activeVisits.put(location, editSession);
        mannequins.put(target.getUniqueId(), mannequin);
        activeVisitsByVisitor.put(player.getUniqueId(), target.getUniqueId());

        Location spawnLoc = location.clone().add(0, 1, 0);
        returnLocations.put(player.getUniqueId(), player.getLocation());
        returnLocations.put(target.getUniqueId(), target.getLocation());

        player.teleport(spawnLoc);
        target.teleport(spawnLoc);

        player.sendMessage(dess("<green>You are now visiting " + target.getName() + ". Use /visit again to end."));
        target.sendMessage(dess("<green>" + player.getName() + " is visiting you."));
    }

    private void endVisit(Player player) {
        UUID targetUuid = activeVisitsByVisitor.remove(player.getUniqueId());
        if (targetUuid == null) return;

        Player target = Bukkit.getPlayer(targetUuid);
        Location returnPlayer = returnLocations.remove(player.getUniqueId());
        if (returnPlayer != null && player.isOnline()) player.teleport(returnPlayer);

        Location returnTarget = returnLocations.remove(targetUuid);
        if (returnTarget != null && target != null && target.isOnline()) target.teleport(returnTarget);

        Mannequin mannequin = mannequins.remove(targetUuid);
        if (mannequin != null) {
            Location loc = mannequin.getLocation().getBlock().getLocation();
            EditSession editSession = activeVisits.remove(loc);
            if (editSession != null) {
                try (EditSession undoSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(loc.getWorld()))) {
                    undoSession.undo(editSession);
                }
            }
            mannequin.remove();
        }

        player.sendMessage(dess("<red>Visit ended."));
        if (target != null && target.isOnline()) {
            target.sendMessage(dess("<red>The visit has ended."));
        }
    }


    public LiteralCommandNode<CommandSourceStack> imbue() {
        return Commands.literal("imbue").then(Commands.argument("target", ArgumentTypes.player())).executes(ctx -> {
            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
            Player target = targetResolver.resolve(ctx.getSource()).getFirst();
            if (ctx.getSource().getSender() instanceof Player player) {
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
        }).build();
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
