package xyz.yaszu.freedom.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.BetterModelPlatform;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Commands.Arguments.ArtifactArguments;
import xyz.yaszu.freedom.Commands.Arguments.BookArguments;
import xyz.yaszu.freedom.Commands.Arguments.ItemArguments;
import xyz.yaszu.freedom.Commands.Arguments.SoulArguments;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Information.BaseEnumBook;
import xyz.yaszu.freedom.Information.Information_Handler;
import xyz.yaszu.freedom.Items.Artifacts.ArtifactTypes;
import xyz.yaszu.freedom.Items.BaseEnumItem;
import xyz.yaszu.freedom.Items.ItemListener;
import xyz.yaszu.freedom.Soul.Base.BaseYellow;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Subsystems.PacketManager;
import xyz.yaszu.freedom.Subsystems.TrustManager;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Subsystems.ChunkLootManager;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Subsystems.AdminManager;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.StructureUtil;
import xyz.yaszu.freedom.Util.Util;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static xyz.yaszu.freedom.Util.Util.*;

public class Trust {
    public static Util util = new Util();
    private static final Map<UUID, EditSession> lastEditSessions = new HashMap<>();

    public static LiteralCommandNode<CommandSourceStack> soulArgument() {
        return Commands.literal("setsoul")
                .then(Commands.argument("flavor", new SoulArguments()).then(Commands.argument("target", ArgumentTypes.player())
                        .executes(ctx -> {
                            final SoulTypes soultype = ctx.getArgument("flavor", SoulTypes.class);
                            if (ctx.getSource().getSender() instanceof Player sender) {
                                if (sender.isOp()) {
                                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                    final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                    target.getPersistentDataContainer().set(keygen("soul"), PersistentDataType.STRING, xyz.yaszu.freedom.Soul.soulListener.SOULS.get(soultype).Name_For_Container());
                                    target.sendRichMessage("Your soul has been set to <aqua>" + soultype.name() + "</aqua>");
                                }
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                ))
                .build();

    }

    public static LiteralCommandNode<CommandSourceStack> normalItemArgument() {
        return Commands.literal("item").then(Commands.argument("flavor", new ItemArguments()).then(Commands.argument("target", ArgumentTypes.player()).then(Commands.argument("amount", IntegerArgumentType.integer(0,64)).executes(ctx -> {

            if (ctx.getSource().getSender() instanceof Player sender) {
                final BaseEnumItem soultype = ctx.getArgument("flavor", BaseEnumItem.class);
                if (sender.isOp()) {
                    final int rsolve = ctx.getArgument("amount", int.class);
                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                    if (target == null) target = sender;
                    Freedom.get_plugin().getLogger().info(soultype.name());
                    Freedom.get_plugin().getLogger().info(target.getName());
                    Freedom.get_plugin().getLogger().info(ItemListener.ITEMS.toString());
                    ItemStack realitem = ItemListener.ITEMS.get(soultype.toString()).item();
                    realitem.setAmount(rsolve);
                    target.give(realitem);
                }

            }

            return Command.SINGLE_SUCCESS;
        })))).build();
    }

    public static LiteralCommandNode<CommandSourceStack> bookItemArgument() {
        return Commands.literal("book").then(Commands.argument("flavor", new BookArguments()).then(Commands.argument("target", ArgumentTypes.player()).then(Commands.argument("amount", IntegerArgumentType.integer(0,64)).executes(ctx -> {

            if (ctx.getSource().getSender() instanceof Player sender) {
                final BaseEnumBook soultype = ctx.getArgument("flavor", BaseEnumBook.class);
                if (sender.isOp()) {
                    final int rsolve = ctx.getArgument("amount", int.class);
                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                    if (target == null) target = sender;
                    Freedom.get_plugin().getLogger().info(soultype.name());
                    Freedom.get_plugin().getLogger().info(target.getName());
                    Freedom.get_plugin().getLogger().info(ItemListener.ITEMS.toString());
                    ItemStack realitem = Information_Handler.ITEMS.get(soultype.toString()).information();
                    realitem.setAmount(rsolve);
                    target.give(realitem);
                }

            }

            return Command.SINGLE_SUCCESS;
        })))).build();
    }

    public static LiteralCommandNode<CommandSourceStack> artifactArgument() {
        return Commands.literal("artifact").then(Commands.argument("flavor", new ArtifactArguments()).then(Commands.argument("target", ArgumentTypes.player()).then(Commands.argument("amount", IntegerArgumentType.integer(0, 64)).executes(ctx -> {

            if (ctx.getSource().getSender() instanceof Player sender) {
                final ArtifactTypes artifactType = ctx.getArgument("flavor", ArtifactTypes.class);
                if (sender.isOp()) {
                    final int rsolve = ctx.getArgument("amount", int.class);
                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                    if (target == null) target = sender;

                    ItemStack realitem = ItemListener.ITEMS.get(artifactType.toString()).item();
                    realitem.setAmount(rsolve);
                    target.give(realitem);
                }

            }

            return Command.SINGLE_SUCCESS;
        })))).build();
    }

    public static LiteralCommandNode<CommandSourceStack> processChunksArgument() {
        return Commands.literal("processchunks")
                .then(Commands.argument("radius", IntegerArgumentType.integer(0, 10))
                        .executes(ctx -> {
                            if (ctx.getSource().getSender() instanceof Player player) {
                                if (player.isOp()) {
                                    int radius = ctx.getArgument("radius", int.class);
                                    ChunkLootManager manager = new ChunkLootManager();
                                    int count = manager.processChunks(player.getLocation().getChunk(), radius);
                                    player.sendRichMessage("<green>Processed chunks in radius " + radius + ". Added items to " + count + " chests.</green>");
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        if (player.isOp()) {
                            ChunkLootManager manager = new ChunkLootManager();
                            int count = manager.processChunks(player.getLocation().getChunk(), 0);
                            player.sendRichMessage("<green>Processed current chunk. Added items to " + count + " chests.</green>");
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> customItemArgument() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("bestow");
        root.then(normalItemArgument());
        root.then(bookItemArgument());
        root.then(artifactArgument());
        return root.build();
    }

    public static LiteralCommandNode<CommandSourceStack> test() {
        return Commands.literal("test").executes(
                ctx -> {

                    if ( ctx.getSource().getSender() instanceof Player target) {
                        if (!target.isOp()) {
                            target.sendMessage(dess("<shadow:#000000FF><b><Red>Error</Red>:</b> YOU CANNOT USE THIS COMMAND ; YOU NEED TO BE OP"));
                            return Command.SINGLE_SUCCESS;
                        };
                        Location loc = target.getLocation();


//                        Location loc = target.getLocation().add(target.getLocation().getDirection().multiply(4));
//                        loc.setY(target.getLocation().getY());
//                        Double soulpoints = target.getPersistentDataContainer().get(keygen("SoulPoint"),PersistentDataType.DOUBLE);
//                        if (soulpoints == null) {
//                            soulpoints = 0d;
//                        }
//                        Integer life = target.getPersistentDataContainer().get(keygen("life"),PersistentDataType.INTEGER);
//                        open(target, (int) Math.round(soulpoints),life);
//                        drawDangerSymbol(target.getLocation(),5,16,Particle.DUST,new Particle.DustOptions(Color.YELLOW, 8.0f),new Particle.DustOptions(Color.BLACK,8.0f));
//                       drawSpiral(loc,8, 4, loc.getWorld(),128, Particle.DUST, new Particle.DustOptions(Color.PURPLE, 8.0f));

                    }

                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }




    public static LiteralCommandNode<CommandSourceStack> playerArgument() {
        return Commands.literal("fling")
                .then(Commands.argument("target", ArgumentTypes.player())
                        .executes(ctx -> {
                            final Player sender = (Player) ctx.getSource().getSender();
                            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                            final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                            if (!sender.isOp()) {
                                target.sendMessage(dess("<shadow:#000000FF><b><Red>Error</Red>:</b> YOU CANNOT USE THIS COMMAND ; YOU NEED TO BE OP"));
                                return Command.SINGLE_SUCCESS;
                            };
                            target.setVelocity(new Vector(0, 10, 0));
                            target.sendRichMessage("You will fly.");

                            ctx.getSource().getSender().sendRichMessage("Flung <target>!",
                                    Placeholder.component("target", target.name())
                            );
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }
    public static soulListener soulListener = new soulListener();


    public static LiteralCommandNode<CommandSourceStack> Ability_One() {
        return Commands.literal("abilityone").executes(
                ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        soulListener.AbilityOne(player);
                    }
                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }
    public static LiteralCommandNode<CommandSourceStack> Passive() {
        return Commands.literal("passive").executes(
                ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {

                        soulListener.Passive(player);
                    }
                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }

    public static LiteralCommandNode<CommandSourceStack> Ability_Two() {
        return Commands.literal("abilitytwo").executes(
                ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        try {
                            soulListener.AbilityTwo(player);
                        } catch (MineSkinException e) {
                            throw new RuntimeException(e);
                        } catch (DataRequestException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }
    public static LiteralCommandNode<CommandSourceStack> rules() {
        return Commands.literal("rules").executes(
                ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        player.sendMessage(dess("<shadow:#000000FF><b><green>Welcome to <Blue>Meowtin</Blue> </green>- <aqua>Catboy Central!"));
                        player.sendMessage(dess("<black><obf>RULESRULESRULESRULESRULESRULESRULESRULESR</obf></black><shadow:#000000FF><b>"));
                        player.sendMessage(dess("<shadow:#000000FF><b><u><Red>RULES</u>"));
                        player.sendMessage(dess("<shadow:#000000FF><b>1. No Discrimination based on race/sexuality"));
                        player.sendMessage(dess("<shadow:#000000FF><b>2. Do not put other people down"));
                        player.sendMessage(dess("<shadow:#000000FF><b>3. No Hacking/Cheating/Exploiting (etc.)"));
                        player.sendMessage(dess("<shadow:#000000FF><b>4. Be respectful to all players "));
                        player.sendMessage(dess("<shadow:#000000FF><b>5. No major griefing (ex. destroying entire towns) (yes that means if you nuke one person that's fine)"));
                        player.sendMessage(dess("<shadow:#000000FF><b>6. No Innapropiate Content (ex. Genitalia buildings, skins, paintings, usernames)"));
                        player.sendMessage(dess("<shadow:#000000FF><b>7. No account abuse (selling, trading, sharing accounts)"));
                        player.sendMessage(dess("<shadow:#000000FF><b>8. No abusive language, misleading information, or any kind of spam</shadow>"));
                        player.sendMessage(dess("<shadow:#000000FF><b>9. No combat logging</shadow>"));
                        player.sendMessage(dess("<black><obf>RULESRULESRULESRULESRULESRULESRULESRULESRULESR</black>"));
                    }
                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }

    public static LiteralCommandNode<CommandSourceStack> Active_Passive() {
        return Commands.literal("activepassive").executes(
                ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        soulListener.ActivePassive(player);
                    }
                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }

    public static LiteralCommandNode<CommandSourceStack> toggleArgument() {
        return Commands.literal("comoraction").executes(
                ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        Boolean current = player.getPersistentDataContainer().get(keygen("ComorAction"), PersistentDataType.BOOLEAN);
                        boolean actionsOnly = current != null && current;

                        if (actionsOnly) {
                            player.getPersistentDataContainer().set(keygen("ComorAction"), PersistentDataType.BOOLEAN, false);
                            player.sendRichMessage("You have selected to use Commands Instead of Actions");
                        } else {
                            player.getPersistentDataContainer().set(keygen("ComorAction"), PersistentDataType.BOOLEAN, true);
                            player.sendRichMessage("You have selected to use Actions Instead of Commands");
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }

    public static LiteralCommandNode<CommandSourceStack> summonFriendly(){
        return Commands.literal("ally").executes(ctx -> {

            final Player sender = (Player) ctx.getSource().getSender();
            if (!sender.isOp()) return Command.SINGLE_SUCCESS;
            BetterModelPlatform platform = BetterModel.platform();
            Entity spawned = sender.getWorld().createEntity(sender.getLocation(), Wolf.class);

            return Command.SINGLE_SUCCESS;
        }).build();
    }


    public static LiteralCommandNode<CommandSourceStack> trustArgument() {
        return Commands.literal("trust")
                .then(Commands.argument("target", ArgumentTypes.player())
                        .executes(ctx -> {
                            final Player sender = (Player) ctx.getSource().getSender();
                            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                            final Player target = targetResolver.resolve(ctx.getSource()).getFirst();

                            if (TrustManager.isTrustedBy(target, sender)) {
                                TrustManager.removeTrust(target, sender);
                                sender.sendRichMessage("You have untrusted <target>.",
                                        Placeholder.component("target", target.name())
                                );
                            } else {
                                TrustManager.addTrust(target, sender);
                                sender.sendRichMessage("You have trusted <target>.",
                                        Placeholder.component("target", target.name())
                                );
                            }


                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }
    public static LiteralCommandNode<CommandSourceStack> uncurseArgument() {
        return Commands.literal("uncurse")
                .then(Commands.argument("target", ArgumentTypes.player())
                        .executes(ctx -> {
                            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                            final Player target = targetResolver.resolve(ctx.getSource()).getFirst();

                            if (ctx.getSource().getSender() instanceof Player sender) {
                                if (!sender.isOp()) return Command.SINGLE_SUCCESS;
                            }

                            xyz.yaszu.freedom.Subsystems.CurseManager.uncurse(target);
                            ctx.getSource().getSender().sendRichMessage("<green>Uncursed <target>!</green>",
                                    Placeholder.component("target", target.name())
                            );
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> reviveArgument() {
        return Commands.literal("revive")
                .then(Commands.argument("target", ArgumentTypes.player())
                        .executes(ctx -> {
                            final Player sender = (Player) ctx.getSource().getSender();
                            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                            final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                                    if (!sender.isOp()) {
                            target.sendMessage(dess("<shadow:#000000FF><b><Red>Error</Red>:</b> YOU CANNOT USE THIS COMMAND ; YOU NEED TO BE OP"));
                            return Command.SINGLE_SUCCESS;
                        };

                            sender.sendMessage(dess("<shadow:#000000FF><b><green>Reviving <target>!</green></b>"));
                            Life_and_Death.revive_player(target,sender.getLocation());
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> interruptRitualArgument() {
        return Commands.literal("interruptritual")
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        xyz.yaszu.freedom.Subsystems.ProvinceManager.interruptRitual(player);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> spawnStructureArgument() {
        return Commands.literal("spawnstructure")
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ctx -> {
                            if (ctx.getSource().getSender() instanceof Player player) {
                                if (player.isOp()) {
                                    String name = ctx.getArgument("name", String.class);
                                    EditSession session = null;
                                    if (name.endsWith(".schem") || name.endsWith(".schematic")) {
                                        Clipboard clipboard = StructureUtil.loadSchematicFromResource(name);
                                        if (clipboard != null) {
                                            session = StructureUtil.spawnSchematic(clipboard, player.getLocation());
                                            player.sendRichMessage("<green>Spawned schematic: " + name + "</green>");
                                        } else {
                                            player.sendRichMessage("<red>Failed to load schematic: " + name + "</red>");
                                        }
                                    } else if (name.endsWith(".nbt")) {
                                        StructureUtil.spawnVanillaStructureFromResource(name, player.getLocation());
                                        player.sendRichMessage("<green>Spawned vanilla structure: " + name + "</green>");
                                        lastEditSessions.remove(player.getUniqueId());
                                        // Vanilla undo not yet supported
                                    } else {
                                        // Try both with .schem by default
                                        Clipboard clipboard = StructureUtil.loadSchematicFromResource(name + ".schem");
                                        if (clipboard != null) {
                                            session = StructureUtil.spawnSchematic(clipboard, player.getLocation());
                                            player.sendRichMessage("<green>Spawned schematic: " + name + ".schem</green>");
                                        } else {
                                            player.sendRichMessage("<red>Unknown structure format or file not found: " + name + "</red>");
                                        }
                                    }

                                    if (session != null) {
                                        lastEditSessions.put(player.getUniqueId(), session);
                                    }
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> undoArgument() {
        return Commands.literal("undo")
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        if (player.isOp()) {
                            EditSession session = lastEditSessions.remove(player.getUniqueId());
                            if (session != null) {
                                try (EditSession undoSession = WorldEdit.getInstance().newEditSession(session.getWorld())) {
                                    session.undo(undoSession);
                                }
                                session.close();
                                player.sendRichMessage("<green>Undid last structure spawn.</green>");
                            } else {
                                player.sendRichMessage("<red>Nothing to undo!</red>");
                            }
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> sudoArgument() {
        return Commands.literal("sudo")
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        AdminManager.toggleSudo(player);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> hatArgument() {
        return Commands.literal("hat")
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        ItemStack itemInHand = player.getInventory().getItemInMainHand();
                        if (itemInHand.getType().isAir()) {
                            player.sendRichMessage("<red>You are not holding anything!</red>");
                            return Command.SINGLE_SUCCESS;
                        }

                        ItemStack helmet = player.getInventory().getHelmet();
                        if (helmet != null && !helmet.getType().isAir()) {
                            player.sendRichMessage("<red>You already have a helmet on!</red>");
                            return Command.SINGLE_SUCCESS;
                        }

                        player.getInventory().setHelmet(itemInHand);
                        player.getInventory().setItemInMainHand(null);
                        player.sendRichMessage("<green>Enjoy your new hat!</green>");
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> skyArgument() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("sky");
        for (PacketManager.SkyType type : PacketManager.SkyType.values()) {
            root.then(Commands.literal(type.name().toLowerCase())
                    .executes(ctx -> {
                        if (ctx.getSource().getSender() instanceof Player player) {
                            if (player.isOp()) {
                                PacketManager.setSky(player, type);
                                player.sendRichMessage("<green>Sky set to " + type.name().toLowerCase() + ".</green>");
                            }
                        }
                        return Command.SINGLE_SUCCESS;
                    })
            );
        }
        return root.build();
    }


}
