package xyz.yaszu.freedom.Subsystems;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Commands.Arguments.DuelArguments;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.StructureUtil;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DuelManager extends Util implements Listener {
    public static String LargeArena = "LargeArena.schem";
    public static String MediumArena = "MediumArena.schem";
    public static String SmallArena = "SmallArena.schem";


    public Location dueloffsetPlayerTwoLarge = new Location(Bukkit.getWorld("DoubleVoid"), 0, 0, -25);
    public Location dueloffsetPlayerTwoMedium = new Location(Bukkit.getWorld("DoubleVoid"), 0, 0, -15);
    public Location dueloffsetPlayerTwoSmall = new Location(Bukkit.getWorld("DoubleVoid"), 0, 0, -5);
    public Location dueloffsetPlayerOneLarge = new Location(Bukkit.getWorld("DoubleVoid"), 0, 0, 25);
    public Location dueloffsetPlayerOneMedium = new Location(Bukkit.getWorld("DoubleVoid"), 0, 0, 15);
    public Location dueloffsetPlayerOneSmall = new Location(Bukkit.getWorld("DoubleVoid"), 0, 0, 5);

    public static HashMap<UUID, Kit> duelsKitsSlot0 = new HashMap<>();
    public static HashMap<UUID, Kit> duelsKitsSlot1 = new HashMap<>();
    public static HashMap<UUID, Kit> duelsKitsSlot2 = new HashMap<>();
    public static HashMap<UUID, Kit> duelsKitsSlot3 = new HashMap<>();
    public static HashMap<UUID, Kit> duelsKitsSlot4 = new HashMap<>();
    private static final Map<Location, EditSession> lastEditSessions = new HashMap<>();
    public static HashMap<Location, DuelArena> duelsArenas = new HashMap<>();

    public static Kit blankkit = new Kit(List.of(ItemStack.of(Material.AIR)).toArray(new ItemStack[0]),ItemStack.of(Material.IRON_HELMET),ItemStack.of(Material.IRON_CHESTPLATE),ItemStack.of(Material.IRON_LEGGINGS),ItemStack.of(Material.IRON_BOOTS));

    public static HashMap<String, Kit> adminKits = new HashMap<>();

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
                    player.sendMessage("Kit saved");
                } catch (Exception e) {
                    player.sendMessage("Error saving kit");
                }

            }
            return Command.SINGLE_SUCCESS;
        })).build();
    };



    public LiteralArgumentBuilder<CommandSourceStack> adminKit() {
        LiteralArgumentBuilder<CommandSourceStack> admin = Commands.literal("adminkit").then(Commands.argument("kitslot", StringArgumentType.word()));
        return admin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> selfKit() {
        LiteralArgumentBuilder<CommandSourceStack> self = Commands.literal("adminkit").then(Commands.argument("kitslot", IntegerArgumentType.integer(1,5)));
        self.then(Commands.argument("duelsize", new DuelArguments()));
        self.executes(
                ctx -> {
                    if ( ctx.getSource().getSender() instanceof Player player) {
                        PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                        Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                        Kit kit = blankkit;
                        switch (ctx.getArgument("kitslot", Integer.class)) {
                            case 1 -> {
                                kit = duelsKitsSlot0.get(player.getUniqueId());
                            }
                            case 2 -> {
                                kit = duelsKitsSlot1.get(player.getUniqueId());
                            }
                            case 3 -> {
                                kit = duelsKitsSlot2.get(player.getUniqueId());
                            }
                            case 4 -> {
                                kit = duelsKitsSlot3.get(player.getUniqueId());
                            }
                            case 5 -> {
                                kit = duelsKitsSlot4.get(player.getUniqueId());
                            }
                        }
                        AdminManager.savePlayerState(target, FreedomKeys.originalState());
                        AdminManager.savePlayerState(player, FreedomKeys.originalState());
                        Location loc = new Location(Bukkit.getWorld("DoubleVoid"), 0, 0, 0);
                        while (duelsArenas.containsKey(loc)) {
                            loc = loc.add(1000,0,0);
                        }
                        switch (ctx.getArgument("duelsize", DuelArena.class)) {
                            case Large -> {
                                //load large arena
                                Clipboard clip = StructureUtil.loadSchematicFromResource(LargeArena);
                                EditSession struc = StructureUtil.spawnSchematic(clip,loc);
                                lastEditSessions.put(loc,struc);
                                duelsArenas.put(loc,DuelArena.Large);
                            }
                            case Medium -> {
                                //load medium arena
                                Clipboard clip = StructureUtil.loadSchematicFromResource(MediumArena);
                                EditSession struc = StructureUtil.spawnSchematic(clip,loc);
                                lastEditSessions.put(loc,struc);
                                duelsArenas.put(loc,DuelArena.Medium);
                            }
                            case Small -> {
                                //load small arena
                                Clipboard clip = StructureUtil.loadSchematicFromResource(SmallArena);
                                EditSession struc = StructureUtil.spawnSchematic(clip,loc);
                                lastEditSessions.put(loc, struc);
                                duelsArenas.put(loc,DuelArena.Small);
                            }
                        }
                        AdminManager.savePlayerState(target, FreedomKeys.originalState());
                        AdminManager.savePlayerState(player, FreedomKeys.originalState());
                        player.teleport(loc);
                        player.getInventory().clear();
                        player.getInventory().setHelmet(kit.helmet);
                        player.getInventory().setChestplate(kit.chestplate);
                        player.getInventory().setLeggings(kit.leggings);
                        player.getInventory().setBoots(kit.boots);
                        player.getInventory().setContents(kit.items);
                        target.getInventory().clear();
                        target.getInventory().setHelmet(kit.helmet);
                        target.getInventory().setChestplate(kit.chestplate);
                        target.getInventory().setLeggings(kit.leggings);
                        target.getInventory().setBoots(kit.boots);
                        target.getInventory().setContents(kit.items);

                    }

                    return Command.SINGLE_SUCCESS;
                }
        );
        return self;
    }

    public LiteralCommandNode<CommandSourceStack> duel() {
        LiteralArgumentBuilder<CommandSourceStack> duel = Commands.literal("duel").then(Commands.argument("target", ArgumentTypes.player()));

        duel.then(adminKit());
        duel.then(selfKit());
        return duel.build();
    }

    public void duel(Player player, Player target,Kit kit) {
        AdminManager.savePlayerState(player, FreedomKeys.originalState());
        AdminManager.savePlayerState(target, FreedomKeys.originalState());
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
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
