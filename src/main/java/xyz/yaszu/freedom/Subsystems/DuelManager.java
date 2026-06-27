package xyz.yaszu.freedom.Subsystems;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Commands.Arguments.DuelArguments;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.StructureUtil;
import xyz.yaszu.freedom.Util.Util;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static xyz.yaszu.freedom.Util.Util.dess;

public class DuelManager implements Listener {
    public static final Map<UUID, Location> playerInDuelArena = new HashMap<>();


    //TODO finish all arenas
    public static String LargeArena = "SmallArena.schem";
    public static String MediumArena = "SmallArena.schem";
    public static String SmallArena = "SmallArena.schem";


    public static HashMap<UUID, Kit> duelsKitsSlot0 = new HashMap<>();
    public static HashMap<UUID, Kit> duelsKitsSlot1 = new HashMap<>();
    public static HashMap<UUID, Kit> duelsKitsSlot2 = new HashMap<>();
    public static HashMap<UUID, Kit> duelsKitsSlot3 = new HashMap<>();
    public static HashMap<UUID, Kit> duelsKitsSlot4 = new HashMap<>();
    private static final Map<Location, EditSession> lastEditSessions = new HashMap<>();
    public static HashMap<Location, DuelArena> duelsArenas = new HashMap<>();

    private static final Map<UUID, DuelRequest> pendingRequests = new HashMap<>();

    public static Kit blankkit = new Kit(List.of(ItemStack.of(Material.AIR)).toArray(new ItemStack[0]),ItemStack.of(Material.IRON_HELMET),ItemStack.of(Material.IRON_CHESTPLATE),ItemStack.of(Material.IRON_LEGGINGS),ItemStack.of(Material.IRON_BOOTS));

    public static HashMap<String, Kit> adminKits = new HashMap<>();

    public DuelManager() {
        loadKits();
    }

    public void saveKits() {
        File file = new File(Freedom.get_plugin().getDataFolder(), "kits.yml");
        YamlConfiguration config = new YamlConfiguration();

        // Save Admin Kits
        for (Map.Entry<String, Kit> entry : adminKits.entrySet()) {
            String path = "adminKits." + entry.getKey();
            saveKitToConfig(config, path, entry.getValue());
        }

        // Save Player Kits
        savePlayerKitsToConfig(config, "slot0", duelsKitsSlot0);
        savePlayerKitsToConfig(config, "slot1", duelsKitsSlot1);
        savePlayerKitsToConfig(config, "slot2", duelsKitsSlot2);
        savePlayerKitsToConfig(config, "slot3", duelsKitsSlot3);
        savePlayerKitsToConfig(config, "slot4", duelsKitsSlot4);

        try {
            config.save(file);
        } catch (IOException e) {
            Freedom.get_plugin().getLogger().severe("Could not save kits.yml!");
            e.printStackTrace();
        }
    }

    private void savePlayerKitsToConfig(YamlConfiguration config, String slotName, Map<UUID, Kit> kits) {
        for (Map.Entry<UUID, Kit> entry : kits.entrySet()) {
            String path = "playerKits." + slotName + "." + entry.getKey().toString();
            saveKitToConfig(config, path, entry.getValue());
        }
    }

    private void saveKitToConfig(YamlConfiguration config, String path, Kit kit) {
        config.set(path + ".items", kit.items);
        config.set(path + ".helmet", kit.helmet);
        config.set(path + ".chestplate", kit.chestplate);
        config.set(path + ".leggings", kit.leggings);
        config.set(path + ".boots", kit.boots);
    }

    public void loadKits() {
        File file = new File(Freedom.get_plugin().getDataFolder(), "kits.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Load Admin Kits
        if (config.contains("adminKits")) {
            for (String key : config.getConfigurationSection("adminKits").getKeys(false)) {
                adminKits.put(key, loadKitFromConfig(config, "adminKits." + key));
            }
        }

        // Load Player Kits
        loadPlayerKitsFromConfig(config, "slot0", duelsKitsSlot0);
        loadPlayerKitsFromConfig(config, "slot1", duelsKitsSlot1);
        loadPlayerKitsFromConfig(config, "slot2", duelsKitsSlot2);
        loadPlayerKitsFromConfig(config, "slot3", duelsKitsSlot3);
        loadPlayerKitsFromConfig(config, "slot4", duelsKitsSlot4);
    }

    private void loadPlayerKitsFromConfig(YamlConfiguration config, String slotName, Map<UUID, Kit> kits) {
        String basePath = "playerKits." + slotName;
        if (config.contains(basePath)) {
            for (String uuidStr : config.getConfigurationSection(basePath).getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    kits.put(uuid, loadKitFromConfig(config, basePath + "." + uuidStr));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    private Kit loadKitFromConfig(YamlConfiguration config, String path) {
        ItemStack[] items;
        List<?> itemsList = config.getList(path + ".items");
        if (itemsList != null) {
            items = itemsList.toArray(new ItemStack[0]);
        } else {
            items = new ItemStack[0];
        }
        ItemStack helmet = config.getItemStack(path + ".helmet");
        ItemStack chestplate = config.getItemStack(path + ".chestplate");
        ItemStack leggings = config.getItemStack(path + ".leggings");
        ItemStack boots = config.getItemStack(path + ".boots");
        return new Kit(items, helmet, chestplate, leggings, boots);
    }

    public LiteralCommandNode<CommandSourceStack> saveAdminkit() {
        return Commands.literal("saveadminkit").then(Commands.argument("kitslot", StringArgumentType.word())
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        Inventory inventory = player.getInventory();
                        ItemStack[] items = inventory.getContents();
                        Kit kit = new Kit(items,player.getInventory().getHelmet(),player.getInventory().getChestplate(),player.getInventory().getLeggings(),player.getInventory().getBoots());
                        String slot = ctx.getArgument("kitslot", String.class);
                        try {
                            adminKits.put(slot, kit);
                            saveKits();
                            player.sendMessage("Kit saved");
                        } catch (Exception e) {
                            player.sendMessage("Error saving kit");
                        }

                    }
                    return Command.SINGLE_SUCCESS;
                })).build();
    };

    public LiteralCommandNode<CommandSourceStack> savekit() {
        return Commands.literal("savekit").then(Commands.argument("kitslot", IntegerArgumentType.integer(1,5))
                .executes(ctx -> {
            if (ctx.getSource().getSender() instanceof Player player) {
                Inventory inventory = player.getInventory();
                ItemStack[] items = inventory.getContents();
                Kit kit = blankkit;
                try {
                    kit = new Kit(items,player.getInventory().getHelmet(),player.getInventory().getChestplate(),player.getInventory().getLeggings(),player.getInventory().getBoots());
                } catch (Exception ignored) {}


                int slot = ctx.getArgument("kitslot", Integer.class);
                try {
                    switch (slot) {
                        case 1 -> {
                            duelsKitsSlot0.put(player.getUniqueId(), kit);
                        }
                        case 2 -> {
                            duelsKitsSlot1.put(player.getUniqueId(), kit);
                        }
                        case 3 -> {
                            duelsKitsSlot2.put(player.getUniqueId(), kit);
                        }
                        case 4 -> {
                            duelsKitsSlot3.put(player.getUniqueId(), kit);
                        }
                        case 5 -> {
                            duelsKitsSlot4.put(player.getUniqueId(), kit);
                        }
                    }
                    saveKits();
                    player.sendMessage("Kit saved");
                } catch (Exception e) {
                    player.sendMessage("Error saving kit");
                }

            }
            return Command.SINGLE_SUCCESS;
        })).build();
    };




    private void startDuel(Player player, Player target, Kit kit, DuelArena size) {
        Location arenaLoc = new Location(Bukkit.getWorld("DoubleVoid"), 0, 0, 0);
        while (duelsArenas.containsKey(arenaLoc)) {
            arenaLoc = arenaLoc.add(1000, 0, 0);
        }

        Clipboard clip = switch (size) {
            case Large -> StructureUtil.loadSchematicFromResource(LargeArena);
            case Medium -> StructureUtil.loadSchematicFromResource(MediumArena);
            case Small -> StructureUtil.loadSchematicFromResource(SmallArena);
        };

        if (clip == null) {
            player.sendRichMessage("<red>Failed to load arena schematic.</red>");
            return;
        }

        EditSession session = StructureUtil.spawnSchematic(clip, arenaLoc);
        lastEditSessions.put(arenaLoc, session);
        duelsArenas.put(arenaLoc, size);

        AdminManager.savePlayerState(player, FreedomKeys.originalState());
        AdminManager.savePlayerState(target, FreedomKeys.originalState());

        playerInDuelArena.put(player.getUniqueId(), arenaLoc);
        playerInDuelArena.put(target.getUniqueId(), arenaLoc);

        List<Location> spawnPoints = new java.util.ArrayList<>();
        BlockVector3 origin = clip.getOrigin();
        for (BlockVector3 position : clip.getRegion()) {
            if (clip.getFullBlock(position).getBlockType().equals(com.sk89q.worldedit.world.block.BlockTypes.REDSTONE_BLOCK)) {
                BlockVector3 relative = position.subtract(origin);
                spawnPoints.add(arenaLoc.clone().add(relative.x() + 0.5, relative.y() + 1, relative.z() + 0.5));
            }
        }

        if (spawnPoints.size() >= 2) {
            player.setHealth(20);
            target.setHealth(20);
            player.setFoodLevel(20);
            target.setFoodLevel(20);
            player.teleport(spawnPoints.get(0));
            target.teleport(spawnPoints.get(1));
        } else {
            player.sendRichMessage("<red>Failed to find sufficient spawn points (redstone blocks) in the arena.</red>");
            target.sendRichMessage("<red>Failed to find sufficient spawn points (redstone blocks) in the arena.</red>");
            endDuel(arenaLoc);
            return;
        }

        applyKit(player, kit);
        applyKit(target, kit);

        player.sendRichMessage("<green>Duel started against " + target.getName() + "!</green>");
        target.sendRichMessage("<green>Duel started against " + player.getName() + "!</green>");
    }

    private void applyKit(Player player, Kit kit) {
        player.getInventory().clear();
        player.getInventory().setHelmet(kit.helmet);
        player.getInventory().setChestplate(kit.chestplate);
        player.getInventory().setLeggings(kit.leggings);
        player.getInventory().setBoots(kit.boots);
        player.getInventory().setContents(kit.items);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        if (playerInDuelArena.containsKey(victim.getUniqueId())) {
            Location arenaLoc = playerInDuelArena.get(victim.getUniqueId());
            Player winner = victim.getKiller();

            if (winner != null && playerInDuelArena.containsKey(winner.getUniqueId())) {
                Bukkit.broadcast(dess("<gold>" + winner.getName() + " has won a duel against " + victim.getName() + "!</gold>"));
            } else {
                Bukkit.broadcast(dess("<gold>" + victim.getName() + " has lost a duel!</gold>"));
            }

            // End duel for both
            endDuel(arenaLoc);

            // Force respawn and restore for the victim
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (victim.isOnline()) {
                        victim.spigot().respawn();
                        AdminManager.loadPlayerState(victim, FreedomKeys.originalState());
                        victim.sendRichMessage("<yellow>Duel ended. Your state has been restored.</yellow>");
                    }
                }
            }.runTaskLater(Freedom.get_plugin(), 1L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerInDuelArena.containsKey(player.getUniqueId())) {
            Location arenaLoc = playerInDuelArena.get(player.getUniqueId());
            Bukkit.broadcast(dess("<gold>" + player.getName() + " has forfeited a duel by leaving!</gold>"));
            endDuel(arenaLoc);
        }
    }

    private void endDuel(Location arenaLoc) {
        List<UUID> playersToRemove = playerInDuelArena.entrySet().stream()
                .filter(entry -> entry.getValue().equals(arenaLoc))
                .map(Map.Entry::getKey)
                .toList();

        for (UUID uuid : playersToRemove) {
            playerInDuelArena.remove(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && !player.isDead()) {
                AdminManager.loadPlayerState(player, FreedomKeys.originalState());
                player.sendRichMessage("<yellow>Duel ended. Your state has been restored.</yellow>");
            }
        }

        duelsArenas.remove(arenaLoc);
        EditSession session = lastEditSessions.remove(arenaLoc);
        if (session != null) {
            EditSession undoSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(arenaLoc.getWorld()));
            try {
                session.undo(undoSession);
            } catch (Exception e) {
                e.printStackTrace();
            }
            session.close();
            undoSession.flushSession();
            undoSession.close();
        }
    }

    public LiteralCommandNode<CommandSourceStack> duel() {
        LiteralArgumentBuilder<CommandSourceStack> duel = Commands.literal("duel");

        duel.then(Commands.literal("accept")
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player target) {
                        DuelRequest request = pendingRequests.remove(target.getUniqueId());
                        if (request == null || request.isExpired()) {
                            target.sendRichMessage("<red>No pending duel request found or it has expired.</red>");
                            return Command.SINGLE_SUCCESS;
                        }
                        Player challenger = Bukkit.getPlayer(request.challengerId());
                        if (challenger == null || !challenger.isOnline()) {
                            target.sendRichMessage("<red>Challenger is no longer online.</red>");
                            return Command.SINGLE_SUCCESS;
                        }
                        startDuel(challenger, target, request.kit(), request.size());
                    }
                    return Command.SINGLE_SUCCESS;
                }));

        duel.then(Commands.argument("target", ArgumentTypes.player())
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                        Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                        sendDuelRequest(player, target, blankkit, DuelArena.Medium);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("default")
                        .then(Commands.argument("duelsize", new DuelArguments())
                                .executes(ctx -> {
                                    if (ctx.getSource().getSender() instanceof Player player) {
                                        PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                        Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                        sendDuelRequest(player, target, blankkit, ctx.getArgument("duelsize", DuelArena.class));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(Commands.literal("selfkit")
                        .then(Commands.argument("kitslot", IntegerArgumentType.integer(1, 5))
                                .then(Commands.argument("duelsize", new DuelArguments())
                                        .executes(ctx -> {
                                            if (ctx.getSource().getSender() instanceof Player player) {
                                                PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                                Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                                int slot = ctx.getArgument("kitslot", Integer.class);
                                                Kit kit = switch (slot) {
                                                    case 1 -> duelsKitsSlot0.get(player.getUniqueId());
                                                    case 2 -> duelsKitsSlot1.get(player.getUniqueId());
                                                    case 3 -> duelsKitsSlot2.get(player.getUniqueId());
                                                    case 4 -> duelsKitsSlot3.get(player.getUniqueId());
                                                    case 5 -> duelsKitsSlot4.get(player.getUniqueId());
                                                    default -> null;
                                                };
                                                if (kit == null) kit = blankkit;
                                                sendDuelRequest(player, target, kit, ctx.getArgument("duelsize", DuelArena.class));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("adminkit")
                        .then(Commands.argument("kitslot", StringArgumentType.word())
                                .then(Commands.argument("duelsize", new DuelArguments())
                                        .executes(ctx -> {
                                            if (ctx.getSource().getSender() instanceof Player player) {
                                                PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                                Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                                String slot = ctx.getArgument("kitslot", String.class);
                                                Kit kit = adminKits.get(slot);
                                                if (kit == null) {
                                                    player.sendRichMessage("<red>Admin kit not found.</red>");
                                                    return Command.SINGLE_SUCCESS;
                                                }
                                                sendDuelRequest(player, target, kit, ctx.getArgument("duelsize", DuelArena.class));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))))
        );
        return duel.build();
    }

    private void sendDuelRequest(Player challenger, Player target, Kit kit, DuelArena size) {
        if (challenger.equals(target)) {
            challenger.sendRichMessage("<red>You cannot duel yourself!</red>");
            return;
        }
        if (playerInDuelArena.containsKey(target.getUniqueId())) {
            challenger.sendRichMessage("<red>" + target.getName() + " is already in a duel!</red>");
            return;
        }

        pendingRequests.put(target.getUniqueId(), new DuelRequest(challenger.getUniqueId(), kit, size, System.currentTimeMillis() + 60000));

        challenger.sendRichMessage("<yellow>Duel request sent to " + target.getName() + ".</yellow>");

        Component message = Component.text()
                .append(Component.text(challenger.getName(), NamedTextColor.GOLD))
                .append(Component.text(" has challenged you to a duel (", NamedTextColor.YELLOW))
                .append(Component.text(size.name(), NamedTextColor.AQUA))
                .append(Component.text(" arena)! ", NamedTextColor.YELLOW))
                .append(Component.text("[ACCEPT]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand("/duel accept")))
                .build();

        target.sendMessage(message);
    }

    public void duel(Player player, Player target, Kit kit) {
        sendDuelRequest(player, target, kit, DuelArena.Medium);
    }

    private record DuelRequest(UUID challengerId, Kit kit, DuelArena size, long expiry) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expiry;
        }
    }


    public static class Kit {

        public ItemStack[] items;
        public ItemStack helmet;
        public ItemStack chestplate;
        public ItemStack leggings;
        public ItemStack boots;
        public Kit(ItemStack[] stacks,ItemStack helmet,ItemStack chestplate,ItemStack leggings,ItemStack boots) {
            this.items = stacks;
            this.helmet = helmet;
            this.chestplate = chestplate;
            this.leggings = leggings;
            this.boots = boots;
        }
    }

}
