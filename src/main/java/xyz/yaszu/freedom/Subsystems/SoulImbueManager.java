package xyz.yaszu.freedom.Subsystems;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;
import java.util.UUID;

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
                                player.sendMessage(dess("Healed 25 durability."));
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




    public LiteralCommandNode<CommandSourceStack> visit() {
        return Commands.literal("visit").executes( ctx -> {
            if (ctx.getSource().getSender() instanceof Player player) {
                if (player.getInventory().getItemInMainHand() != null) {
                    if (isImbued(player.getInventory().getItemInMainHand())) {
                        ItemStack item = player.getInventory().getItemInMainHand();
                        SoulTypes soulType = SoulTypes.valueOf(item.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));

                    }
                }
            }
            return Command.SINGLE_SUCCESS;
        }).build();
    }


    public LiteralCommandNode<CommandSourceStack> imbue() {
        return Commands.literal("imbue").then(Commands.argument("target", ArgumentTypes.player())).executes(ctx -> {
                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                    if (ctx.getSource().getSender() instanceof Player player) {
                        if (target == player) {
                            if (player.getInventory().getItemInMainHand() != null) {
                                if (isImbued(player.getInventory().getItemInMainHand())) {
                                    UnImbueItem(player.getInventory().getItemInMainHand(), 0);
                                } else {
                                    SoulTypes soul = getSoulType(player);
                                    ImbueItem(player.getInventory().getItemInMainHand(), player, soul, true);
                                }
                            }
                        } else {
                            if (target.getInventory().getItemInMainHand() != null) {
                                if (isImbued(target.getInventory().getItemInMainHand())) {
                                    UnImbueItem(target.getInventory().getItemInMainHand(), 1);
                                } else {
                                    SoulTypes soul = getSoulType(target);
                                    ImbueItem(target.getInventory().getItemInMainHand(), target, soul, false);
                                }
                            }
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
                    UnImbueItem(stack, ctx.getArgument("slot", Integer.class));
                } else {
                    if (target.getInventory().getItemInMainHand() != null) {
                        ItemStack stack = target.getInventory().getItemInMainHand();
                        List<Player> players = getWhoImbued(stack);
                        try {
                            if (!players.isEmpty()) {
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
    public static void ImbueItem(ItemStack item, Player player, SoulTypes soulType, boolean selfimbue) {
        ItemMeta meta = item.getItemMeta();
        if (selfimbue) {
            meta.getPersistentDataContainer().set(keygen("soultwo"), PersistentDataType.STRING, soulType.name());
            meta.getPersistentDataContainer().set(keygen("soulownertwo"), PersistentDataType.STRING, player.getUniqueId().toString());
        } else {
            meta.getPersistentDataContainer().set(keygen("soul"), PersistentDataType.STRING, soulType.name());
            meta.getPersistentDataContainer().set(keygen("soulowner"), PersistentDataType.STRING, player.getUniqueId().toString());
        }
        item.setItemMeta(meta);
    }
    /**
     * Adds alcohol to a player.
     *
     * @param item   Itemstack to unimbue
     * @param slot   slot within item to unimbue
     */
    public static void UnImbueItem(ItemStack item, int slot) {
        switch (slot) {
            case 0 -> {
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().remove(keygen("soul"));
                meta.getPersistentDataContainer().remove(keygen("soulowner"));
                item.setItemMeta(meta);
            }
            case 1 -> {
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().remove(keygen("soultwo"));
                meta.getPersistentDataContainer().remove(keygen("soulownertwo"));
                item.setItemMeta(meta);
            }
        }
    }
}
