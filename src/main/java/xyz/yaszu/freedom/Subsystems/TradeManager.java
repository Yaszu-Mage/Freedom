package xyz.yaszu.freedom.Subsystems;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

public class TradeManager extends Util implements Listener {

    private static final Map<UUID, TradeRequest> pendingRequests = new HashMap<>();
    private static final Map<UUID, TradeSession> activeSessions = new HashMap<>();

    // GUI Constants (Hypixel SkyBlock Layout - 6 rows)
    // Left side: 0,1,2, 9,10,11, 18,19,20, 27,28,29
    // Right side: 6,7,8, 15,16,17, 24,25,26, 33,34,35
    // Status/Confirm slots:
    // Player 1: 48 (Confirm), 47 (Status)
    // Player 2: 50 (Confirm), 51 (Status)
    // Divider: 4, 13, 22, 31, 40, 49
    private static final int[] P1_SLOTS = {0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30};
    private static final int[] P2_SLOTS = {5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35};
    private static final int[] DIVIDER_SLOTS = {4, 13, 22, 31, 40, 49, 36, 37, 38, 39, 41, 42, 43, 44, 45, 46, 52, 53};
    private static final int P1_CONFIRM_SLOT = 47;
    private static final int P2_CONFIRM_SLOT = 51;
    private static final int P1_STATUS_SLOT = 48;
    private static final int P2_STATUS_SLOT = 50;

    public static LiteralCommandNode<CommandSourceStack> tradeCommand() {
        return Commands.literal("trade")
                .then(Commands.argument("player", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            String remaining = builder.getRemaining().toLowerCase();
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (player.getName().toLowerCase().startsWith(remaining)) {
                                    builder.suggest(player.getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(TradeManager::executeTrade))
                .build();
    }

    private static int executeTrade(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getExecutor() instanceof Player sender)) {
            context.getSource().getSender().sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return 0;
        }

        String targetName = StringArgumentType.getString(context, "player");
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
            return 0;
        }

        if (target.equals(sender)) {
            sender.sendMessage(Component.text("You cannot trade with yourself!", NamedTextColor.RED));
            return 0;
        }

        // Check if target already has a request from sender
        if (pendingRequests.containsKey(target.getUniqueId()) && pendingRequests.get(target.getUniqueId()).sender.equals(sender.getUniqueId())) {
            sender.sendMessage(Component.text("You have already sent a trade request to " + target.getName() + ".", NamedTextColor.YELLOW));
            return 1;
        }

        // Check if sender has a request from target (Accept it)
        if (pendingRequests.containsKey(sender.getUniqueId()) && pendingRequests.get(sender.getUniqueId()).sender.equals(target.getUniqueId())) {
            TradeRequest request = pendingRequests.remove(sender.getUniqueId());
            if (!request.isExpired()) {
                startTrade(target, sender);
                return 1;
            }
        }

        // Send new request
        pendingRequests.put(target.getUniqueId(), new TradeRequest(sender.getUniqueId()));
        sender.sendMessage(Component.text("You sent a trade request to " + target.getName() + ".", NamedTextColor.GREEN));

        Component invite = Component.text()
                .append(Component.text(sender.getName(), NamedTextColor.GOLD))
                .append(Component.text(" has sent you a trade request! ", NamedTextColor.YELLOW))
                .append(Component.text("[ACCEPT]", NamedTextColor.GREEN, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/trade " + sender.getName()))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to accept trade request", NamedTextColor.GRAY))))
                .build();
        target.sendMessage(invite);

        return 1;
    }

    private static void startTrade(Player p1, Player p2) {
        TradeSession session = new TradeSession(p1, p2);
        activeSessions.put(p1.getUniqueId(), session);
        activeSessions.put(p2.getUniqueId(), session);

        p1.openInventory(session.getInventory());
        p2.openInventory(session.getInventory());

        p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
        p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TradeSession session)) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // Prevent clicking top inventory empty/divider slots
        if (slot >= 0 && slot < 54) {
            boolean isP1 = player.getUniqueId().equals(session.p1.getUniqueId());
            boolean isP2 = player.getUniqueId().equals(session.p2.getUniqueId());

            if (isP1) {
                if (slot == P1_CONFIRM_SLOT) {
                    event.setCancelled(true);
                    session.handleConfirmClick(player);
                    return;
                }
                if (!contains(P1_SLOTS, slot)) {
                    event.setCancelled(true);
                    return;
                }
            } else if (isP2) {
                if (slot == P2_CONFIRM_SLOT) {
                    event.setCancelled(true);
                    session.handleConfirmClick(player);
                    return;
                }
                if (!contains(P2_SLOTS, slot)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                event.setCancelled(true);
                return;
            }

            // If an item is added/removed, reset ready status and start countdown if needed
            session.onItemChange();
        } else if (event.isShiftClick()) {
            // Shift clicking from bottom inventory
            event.setCancelled(true); // Simplify for now, Hypixel often prevents shift-click in trade to avoid errors
            player.sendMessage(Component.text("Shift-clicking is disabled in trades.", NamedTextColor.RED));
        }
    }

    private boolean contains(int[] array, int value) {
        for (int i : array) if (i == value) return true;
        return false;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof TradeSession session)) return;
        session.cancelTrade((Player) event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        activeSessions.remove(event.getPlayer().getUniqueId());
        pendingRequests.remove(event.getPlayer().getUniqueId());
    }

    private static class TradeRequest {
        final UUID sender;
        final long timestamp;

        TradeRequest(UUID sender) {
            this.sender = sender;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 30000;
        }
    }

    private static class TradeSession implements InventoryHolder {
        final Player p1, p2;
        final Inventory inventory;
        boolean p1Ready = false, p2Ready = false;
        boolean p1Accepted = false, p2Accepted = false;
        int countdown = -1;
        BukkitRunnable countdownTask = null;

        TradeSession(Player p1, Player p2) {
            this.p1 = p1;
            this.p2 = p2;
            this.inventory = Bukkit.createInventory(this, 54, Component.text("Trade with " + p2.getName()));
            setupInventory();
        }

        void setupInventory() {
            ItemStack divider = emptyItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            for (int slot : DIVIDER_SLOTS) {
                inventory.setItem(slot, divider);
            }
            updateStatusItems();
        }

        void updateStatusItems() {
            inventory.setItem(P1_CONFIRM_SLOT, getConfirmItem(p1Ready, p1Accepted, p1));
            inventory.setItem(P2_CONFIRM_SLOT, getConfirmItem(p2Ready, p2Accepted, p2));
            inventory.setItem(P1_STATUS_SLOT, getStatusItem(p1Ready, p1Accepted, p1));
            inventory.setItem(P2_STATUS_SLOT, getStatusItem(p2Ready, p2Accepted, p2));
        }

        ItemStack getConfirmItem(boolean ready, boolean accepted, Player player) {
            Material mat = accepted ? Material.EMERALD_BLOCK : (ready ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (accepted) {
                meta.displayName(Component.text("Trade Accepted!", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            } else if (ready) {
                meta.displayName(Component.text("Ready!", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
                meta.lore(List.of(Component.text("Click to cancel", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
            } else {
                meta.displayName(Component.text("Click to Ready", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            }
            item.setItemMeta(meta);
            return item;
        }

        ItemStack getStatusItem(boolean ready, boolean accepted, Player player) {
            ItemStack head = getSkull(player);
            ItemMeta meta = head.getItemMeta();
            meta.displayName(Component.text(player.getName() + "'s Status", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Ready: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                    .append(ready ? Component.text("YES", NamedTextColor.GREEN) : Component.text("NO", NamedTextColor.RED)));
            lore.add(Component.text("Accepted: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                    .append(accepted ? Component.text("YES", NamedTextColor.GREEN) : Component.text("NO", NamedTextColor.RED)));
            meta.lore(lore);
            head.setItemMeta(meta);
            return head;
        }

        void toggleReady(Player player) {
            if (countdown > 0) return;

            if (player.equals(p1)) {
                if (p1Accepted) return;
                if (p1Ready && p2Accepted) { // If p1 was ready and p2 accepted, and p1 cancels, reset both accepted
                    p2Accepted = false;
                }
                p1Ready = !p1Ready;
            } else {
                if (p2Accepted) return;
                if (p2Ready && p1Accepted) {
                    p1Accepted = false;
                }
                p2Ready = !p2Ready;
            }

            if (p1Ready && p2Ready) {
                startAcceptPhase();
            } else {
                updateStatusItems();
            }
        }

        void startAcceptPhase() {
            countdown = 3;
            if (countdownTask != null) countdownTask.cancel();
            countdownTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (countdown > 0) {
                        Component msg = Component.text("Trade will be acceptable in " + countdown + "...", NamedTextColor.YELLOW);
                        p1.sendMessage(msg);
                        p2.sendMessage(msg);
                        p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                        p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                        countdown--;
                    } else {
                        countdown = 0;
                        p1Accepted = false;
                        p2Accepted = false;
                        updateStatusItems();
                        Component msg = Component.text("You can now accept the trade!", NamedTextColor.GREEN);
                        p1.sendMessage(msg);
                        p2.sendMessage(msg);
                        p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                        p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                        
                        // Change Ready buttons to Accept buttons
                        inventory.setItem(P1_CONFIRM_SLOT, getAcceptItem(p1));
                        inventory.setItem(P2_CONFIRM_SLOT, getAcceptItem(p2));
                        this.cancel();
                    }
                }
            };
            countdownTask.runTaskTimer(Freedom.get_plugin(), 0, 20);
        }

        ItemStack getAcceptItem(Player player) {
            ItemStack item = new ItemStack(Material.LIME_TERRACOTTA);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("CONFIRM TRADE", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text("Click to accept the trade!", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
            item.setItemMeta(meta);
            return item;
        }

        void onItemChange() {
            if (p1Ready || p2Ready || p1Accepted || p2Accepted) {
                p1Ready = false;
                p2Ready = false;
                p1Accepted = false;
                p2Accepted = false;
                if (countdownTask != null) {
                    countdownTask.cancel();
                    countdownTask = null;
                }
                countdown = -1;
                updateStatusItems();
                p1.sendMessage(Component.text("Trade modified! Ready status reset.", NamedTextColor.YELLOW));
                p2.sendMessage(Component.text("Trade modified! Ready status reset.", NamedTextColor.YELLOW));
            }
        }

        void acceptTrade(Player player) {
            if (countdown != 0) return;
            if (player.equals(p1)) p1Accepted = true;
            else p2Accepted = true;

            updateStatusItems();

            if (p1Accepted && p2Accepted) {
                completeTrade();
            }
        }

        // Override toggleReady to handle accept phase
        void handleConfirmClick(Player player) {
            if (countdown > 0) return;
            if (countdown == 0) {
                acceptTrade(player);
            } else {
                toggleReady(player);
            }
        }

        void completeTrade() {
            List<ItemStack> p1Items = new ArrayList<>();
            List<ItemStack> p2Items = new ArrayList<>();

            for (int slot : P1_SLOTS) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && item.getType() != Material.AIR) p1Items.add(item);
            }
            for (int slot : P2_SLOTS) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && item.getType() != Material.AIR) p2Items.add(item);
            }

            inventory.clear(); // Prevent items from being returned on close
            activeSessions.remove(p1.getUniqueId());
            activeSessions.remove(p2.getUniqueId());

            for (ItemStack item : p1Items) p2.getInventory().addItem(item).values().forEach(i -> p2.getWorld().dropItem(p2.getLocation(), i));
            for (ItemStack item : p2Items) p1.getInventory().addItem(item).values().forEach(i -> p1.getWorld().dropItem(p1.getLocation(), i));

            p1.closeInventory();
            p2.closeInventory();

            p1.sendMessage(Component.text("Trade completed!", NamedTextColor.GREEN));
            p2.sendMessage(Component.text("Trade completed!", NamedTextColor.GREEN));
            p1.playSound(p1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            p2.playSound(p2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }

        void cancelTrade(Player closer) {
            if (!activeSessions.containsKey(p1.getUniqueId())) return; // Already completed

            activeSessions.remove(p1.getUniqueId());
            activeSessions.remove(p2.getUniqueId());

            for (int slot : P1_SLOTS) {
                ItemStack item = inventory.getItem(slot);
                if (item != null) p1.getInventory().addItem(item).values().forEach(i -> p1.getWorld().dropItem(p1.getLocation(), i));
            }
            for (int slot : P2_SLOTS) {
                ItemStack item = inventory.getItem(slot);
                if (item != null) p2.getInventory().addItem(item).values().forEach(i -> p2.getWorld().dropItem(p2.getLocation(), i));
            }
            inventory.clear();

            p1.sendMessage(Component.text("Trade cancelled.", NamedTextColor.RED));
            p2.sendMessage(Component.text("Trade cancelled.", NamedTextColor.RED));

            if (p1.getOpenInventory().getTopInventory().getHolder() instanceof TradeSession) p1.closeInventory();
            if (p2.getOpenInventory().getTopInventory().getHolder() instanceof TradeSession) p2.closeInventory();
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }
}
