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
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base.BaseYellow;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.time.LocalTime;

import static xyz.yaszu.freedom.Util.Util.*;

public class Trust {
    public static Util util = new Util();

    public static LiteralCommandNode<CommandSourceStack> test() {
        return Commands.literal("test").executes(
                ctx -> {

                    if ( ctx.getSource().getSender() instanceof Player target) {
                        if (!target.isOp()) {
                            target.sendMessage(dess("<shadow:#000000FF><b><Red>Error</Red>:</b> YOU CANNOT USE THIS COMMAND ; YOU NEED TO BE OP"));
                            return Command.SINGLE_SUCCESS;
                        };
                        Location loc = target.getLocation();
                        new BukkitRunnable() {
                            int tick = 0;
                            @Override
                            public void run() {

                                tick = tick - 12;
                                if (tick <= -1200) {
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(Freedom.get_plugin(), 0, 10);


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


}
