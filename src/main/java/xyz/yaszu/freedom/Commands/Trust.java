package xyz.yaszu.freedom.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Toggleable;
import xyz.yaszu.freedom.Util;

public class Trust {
    public static Util util = new Util();
    public static LiteralCommandNode<CommandSourceStack> playerArgument() {
        return Commands.literal("fling")
                .then(Commands.argument("target", ArgumentTypes.player())
                        .executes(ctx -> {
                            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                            final Player target = targetResolver.resolve(ctx.getSource()).getFirst();

                            target.setVelocity(new Vector(0, 100, 0));
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
        return Commands.literal("AbilityOne").executes(
                ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        soulListener.AbilityOne(player);
                    }
                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }

    public static LiteralCommandNode<CommandSourceStack> Ability_Two() {
        return Commands.literal("AbilityTwo").executes(
                ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        soulListener.AbilityTwo(player);
                    }
                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }


    public static LiteralCommandNode<CommandSourceStack> Active_Passive() {
        return Commands.literal("ActivePassive").executes(
                ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        soulListener.ActivePassive(player);
                    }
                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }

    public static LiteralCommandNode<CommandSourceStack> toggleArgument() {
        return Commands.literal("ComorAction").executes(
                ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        if (player.getPersistentDataContainer().get(util.keygen("ComorAction"),PersistentDataType.BOOLEAN)) {
                            player.getPersistentDataContainer().set(util.keygen("ComorAction"), PersistentDataType.BOOLEAN, false);
                            player.sendRichMessage("You have selected to use Commands Instead of Actions");
                        } else {
                            player.getPersistentDataContainer().set(util.keygen("ComorAction"), PersistentDataType.BOOLEAN, true);
                            player.sendRichMessage("You have selected to use Actions Instead of Commands");
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }

    public static LiteralCommandNode<CommandSourceStack> trustArgument() {
        return Commands.literal("trust")
                .then(Commands.argument("target", ArgumentTypes.player())
                        .executes(ctx -> {
                            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                            final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                            final Player sender = (Player) ctx.getSource().getSender();
                            if (target.getPersistentDataContainer().has(util.keygen("trustedby"))) {
                                String trustedby = target.getPersistentDataContainer().get(util.keygen("trustedby"), PersistentDataType.STRING);
                                if (!trustedby.contains(sender.getName())) {
                                    trustedby = trustedby + sender.getName();
                                    ctx.getSource().getSender().sendRichMessage("You have trusted <target>.",
                                            Placeholder.component("target", target.name())
                                    );
                                } else {
                                    trustedby = trustedby.replace(sender.getName(), "");
                                    ctx.getSource().getSender().sendRichMessage("You have untrusted <target>.",
                                            Placeholder.component("target", target.name())
                                    );
                                }

                                target.getPersistentDataContainer().set(util.keygen("trustedby"), PersistentDataType.STRING, trustedby);
                            } else {
                                target.getPersistentDataContainer().set(util.keygen("trustedby"), PersistentDataType.STRING, sender.getName());
                                ctx.getSource().getSender().sendRichMessage("You have trusted <target>.",
                                        Placeholder.component("target", target.name())
                                );
                            }


                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }



}
