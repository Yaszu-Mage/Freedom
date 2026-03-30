package xyz.yaszu.freedom.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.BetterModelPlatform;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.keygen;

public class Trust {
    public static Util util = new Util();

    public static LiteralCommandNode<CommandSourceStack> test() {
        return Commands.literal("test").executes(
                ctx -> {

                    if ( ctx.getSource().getSender() instanceof Player target) {
//                        Location loc = target.getLocation().add(target.getLocation().getDirection().multiply(4));
//                        loc.setY(target.getLocation().getY());
                        Location loc = target.getLocation();
                        SoulTypes soulType = SoulTypes.valueOf(target.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
                       util.PortalParticleLifespan(loc.clone().add(0,2,0),loc.clone().add(0,50,4));
                        util.PortalParticleLifespan(loc.clone().add(0,50,0),loc.clone().add(0,2,4));
                    }

                    return Command.SINGLE_SUCCESS;
                }
        ).build();
    }



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
                        if (player.getPersistentDataContainer().get(keygen("ComorAction"),PersistentDataType.BOOLEAN)) {
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
            BetterModelPlatform platform = BetterModel.platform();
            Entity spawned = sender.getWorld().createEntity(sender.getLocation(), Wolf.class);

            return Command.SINGLE_SUCCESS;
        }).build();
    }


    public static LiteralCommandNode<CommandSourceStack> trustArgument() {
        return Commands.literal("trust")
                .then(Commands.argument("target", ArgumentTypes.player())
                        .executes(ctx -> {
                            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                            final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                            final Player sender = (Player) ctx.getSource().getSender();
                            if (target.getPersistentDataContainer().has(keygen("trustedby"))) {
                                String trustedby = target.getPersistentDataContainer().get(keygen("trustedby"), PersistentDataType.STRING);
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

                                target.getPersistentDataContainer().set(keygen("trustedby"), PersistentDataType.STRING, trustedby);
                                Freedom.get_plugin().getLogger().info(trustedby);
                            } else {
                                Freedom.get_plugin().getLogger().info(sender.getName());
                                target.getPersistentDataContainer().set(keygen("trustedby"), PersistentDataType.STRING, sender.getName());
                                ctx.getSource().getSender().sendRichMessage("You have trusted <target>.",
                                        Placeholder.component("target", target.name())
                                );
                            }


                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }
    public static LiteralCommandNode<CommandSourceStack> reviveArgument() {
        return Commands.literal("revive")
                .then(Commands.argument("target", ArgumentTypes.player())
                        .executes(ctx -> {
                            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                            final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                            final Player sender = (Player) ctx.getSource().getSender();
                            Life_and_Death.revive_player(target,sender.getLocation());
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }


}
