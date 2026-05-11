package xyz.yaszu.freedom.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.yaszu.freedom.Alchemy.MazeConfig;
import xyz.yaszu.freedom.Alchemy.MazeManager;

/**
 * Command to create and manage maze worlds
 */
public class MazeCommand {

    public static LiteralCommandNode<CommandSourceStack> mazeCommand() {
        return Commands.literal("maze")
                .then(Commands.literal("create")
                        .then(Commands.argument("world", StringArgumentType.string())
                                .executes(ctx -> {
                                    String worldName = ctx.getArgument("world", String.class);

                                    if (Bukkit.getWorld(worldName) != null) {
                                        ctx.getSource().getSender().sendMessage(Component.text("World '" + worldName + "' already exists!", NamedTextColor.RED));
                                        return 0;
                                    }

                                    MazeManager.createMazeWorld(worldName);
                                    ctx.getSource().getSender().sendMessage(Component.text("Created maze world: ", NamedTextColor.GREEN)
                                            .append(Component.text(worldName, NamedTextColor.AQUA)));

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.argument("cellSize", IntegerArgumentType.integer(2, 16))
                                        .then(Commands.argument("wallHeight", IntegerArgumentType.integer(1, 20))
                                                .executes(ctx -> {
                                                    String worldName = ctx.getArgument("world", String.class);
                                                    int cellSize = ctx.getArgument("cellSize", int.class);
                                                    int wallHeight = ctx.getArgument("wallHeight", int.class);

                                                    if (Bukkit.getWorld(worldName) != null) {
                                                        ctx.getSource().getSender().sendMessage(Component.text("World '" + worldName + "' already exists!", NamedTextColor.RED));
                                                        return 0;
                                                    }

                                                    MazeConfig config = new MazeConfig();
                                                    config.setCellSize(cellSize);
                                                    config.setWallHeight(wallHeight);

                                                    MazeManager.createMazeWorld(worldName, config);
                                                    ctx.getSource().getSender().sendMessage(Component.text("Created maze world with cellSize=", NamedTextColor.GREEN)
                                                            .append(Component.text(String.valueOf(cellSize), NamedTextColor.AQUA))
                                                            .append(Component.text(", wallHeight=" + wallHeight, NamedTextColor.GREEN)));

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                )
                .then(Commands.literal("preset")
                        .then(Commands.argument("preset", StringArgumentType.string())
                                .then(Commands.argument("world", StringArgumentType.string())
                                        .executes(ctx -> {
                                            String presetName = ctx.getArgument("preset", String.class).toUpperCase();
                                            String worldName = ctx.getArgument("world", String.class);

                                            if (Bukkit.getWorld(worldName) != null) {
                                                ctx.getSource().getSender().sendMessage(Component.text("World '" + worldName + "' already exists!", NamedTextColor.RED));
                                                return 0;
                                            }

                                            try {
                                                MazeManager.MazePreset preset = MazeManager.MazePreset.valueOf(presetName);
                                                MazeManager.createPresetMaze(worldName, preset);
                                                ctx.getSource().getSender().sendMessage(Component.text("Created maze world with preset: ", NamedTextColor.GREEN)
                                                        .append(Component.text(presetName, NamedTextColor.AQUA)));
                                            } catch (IllegalArgumentException e) {
                                                ctx.getSource().getSender().sendMessage(Component.text("Unknown preset: " + presetName, NamedTextColor.RED));
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendMessage(Component.text("Available Maze Presets:", NamedTextColor.GOLD));
                            for (MazeManager.MazePreset preset : MazeManager.MazePreset.values()) {
                                ctx.getSource().getSender().sendMessage(Component.text("  - " + preset.name().toLowerCase(), NamedTextColor.YELLOW));
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("teleport")
                        .then(Commands.argument("world", StringArgumentType.string())
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getSender() instanceof Player)) {
                                        ctx.getSource().getSender().sendMessage(Component.text("Only players can teleport!", NamedTextColor.RED));
                                        return 0;
                                    }

                                    Player player = (Player) ctx.getSource().getSender();
                                    String worldName = ctx.getArgument("world", String.class);

                                    org.bukkit.World world = Bukkit.getWorld(worldName);
                                    if (world == null) {
                                        player.sendMessage(Component.text("World '" + worldName + "' not found!", NamedTextColor.RED));
                                        return 0;
                                    }

                                    player.teleport(world.getSpawnLocation());
                                    player.sendMessage(Component.text("Teleported to maze world: " + worldName, NamedTextColor.GREEN));

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("help")
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendMessage(Component.text("=== Maze Command Help ===", NamedTextColor.GOLD));
                            ctx.getSource().getSender().sendMessage(Component.text("/maze create <name> [cellSize] [wallHeight]", NamedTextColor.AQUA));
                            ctx.getSource().getSender().sendMessage(Component.text("  Create a custom maze world", NamedTextColor.GRAY));
                            ctx.getSource().getSender().sendMessage(Component.text("/maze preset <preset> <name>", NamedTextColor.AQUA));
                            ctx.getSource().getSender().sendMessage(Component.text("  Create a maze world from a preset", NamedTextColor.GRAY));
                            ctx.getSource().getSender().sendMessage(Component.text("/maze list", NamedTextColor.AQUA));
                            ctx.getSource().getSender().sendMessage(Component.text("  List available presets", NamedTextColor.GRAY));
                            ctx.getSource().getSender().sendMessage(Component.text("/maze teleport <world_name>", NamedTextColor.AQUA));
                            ctx.getSource().getSender().sendMessage(Component.text("  Teleport to a maze world", NamedTextColor.GRAY));

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(Component.text("Use /maze help for command information", NamedTextColor.YELLOW));
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}

