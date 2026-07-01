package xyz.yaszu.freedom.Subsystems;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.InventoryPersistentDataType;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

/**
 * a simple backpack system that stores contents in a persistent inventory.
 */
public class BackpackManager implements Listener {

    // -------------------------------------------------------------------------
    // InventoryClickEvent
    // -------------------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {

        boolean clickedInBackpackGui = event.getClickedInventory() != null
                && event.getClickedInventory().getHolder() instanceof BackpackGui;
        boolean topIsBackpackGui = event.getInventory().getHolder() instanceof BackpackGui;

        ItemStack cursor  = nullSafe(event.getCursor());
        ItemStack clicked = nullSafe(event.getCurrentItem());

        boolean cursorIsBackpack  = isBackpack(cursor);
        boolean clickedIsBackpack = isBackpack(clicked);

        // ── Rule 1: nothing backpack-shaped may be moved into a BackpackGui ──
        if (clickedInBackpackGui) {
            if (cursorIsBackpack || clickedIsBackpack) {
                event.setCancelled(true);
                return;
            }
        }

        // ── Rule 2: prevent shift-click carrying a backpack into a BackpackGui ──
        if (event.isShiftClick() && topIsBackpackGui && clickedIsBackpack) {
            event.setCancelled(true);
            return;
        }

        // ── Rule 3: prevent shift-click moving a backpack anywhere that would
        //            land it on top of another backpack (stacking) ──
        if (event.isShiftClick() && clickedIsBackpack) {
            event.setCancelled(true);
            return;
        }

        // ── Rule 4: prevent cursor-backpack being placed onto a slot that
        //            already holds a backpack (stacking in player inventory) ──
        if (cursorIsBackpack && clickedIsBackpack) {
            event.setCancelled(true);
            return;
        }

        // ── Rule 5: number-key (hotbar swap) would merge two backpacks ──
        //    getHotbarButton() returns -1 when not a hotbar-swap action.
        if (event.getHotbarButton() >= 0) {
            ItemStack hotbarItem = nullSafe(
                    event.getWhoClicked().getInventory().getItem(event.getHotbarButton()));
            if ((clickedIsBackpack && isBackpack(hotbarItem))
                    || (isBackpack(hotbarItem) && clickedIsBackpack)) {
                event.setCancelled(true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // InventoryDragEvent – prevent splitting a backpack across multiple slots
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (isBackpack(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }

    // -------------------------------------------------------------------------
    // InventoryCloseEvent – persist backpack contents back to the item
    // -------------------------------------------------------------------------

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        // We only care about BackpackGui windows
        if (!(event.getInventory().getHolder() instanceof BackpackGui)) {
            return;
        }

        // Find the backpack item in the player's hands (main first, then off)
        ItemStack backpackItem = findBackpackInHands(event);
        if (backpackItem == null) {
            Freedom.get_plugin().getLogger().warning(
                    "[BackpackManager] Closed a BackpackGui but could not locate the backpack item in "
                            + event.getPlayer().getName() + "'s hands. Inventory contents may be lost.");
            return;
        }

        ItemMeta meta = backpackItem.getItemMeta();
        if (meta == null) return;

        InventoryPersistentDataType.SaveContext ctx = new InventoryPersistentDataType.SaveContext(
                event.getPlayer().getUniqueId(),
                event.getView().title()
        );

        meta.getPersistentDataContainer().set(
                keygen("backpack"),
                InventoryPersistentDataType.get(ctx),
                event.getInventory()
        );
        backpackItem.setItemMeta(meta);
    }

    // -------------------------------------------------------------------------
    // BackpackGui (inner class)
    // -------------------------------------------------------------------------

    public static class BackpackGui implements InventoryHolder {

        /** Marker instance used as the InventoryHolder so we can identify our GUIs. */
        private static final BackpackGui HOLDER = new BackpackGui();

        private Inventory inventory;

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }

        /**
         * Open (or create) the backpack GUI for the given item.
         *
         * <p>If the item already has stored contents they are loaded; otherwise a
         * fresh 27-slot (3×9) inventory is created.</p>
         *
         * @param stack the backpack ItemStack
         * @return the inventory to open, or {@code null} if the item is not a backpack
         */
        public static Inventory open(ItemStack stack) {
            if (!stack.getPersistentDataContainer().has(keygen("backpack"))) {
                return null;
            }
            ItemMeta meta = stack.getItemMeta();
            // Attempt to load existing contents
            InventoryPersistentDataType.LoadResult result =
                    InventoryPersistentDataType.load(
                            meta.getPersistentDataContainer(), keygen("backpack"));

            if (result != null) {
                // Re-wrap under our holder so the GUI is identifiable
                Component name = result.inventoryName() != null
                        ? result.inventoryName()
                        : dess("Backpack");
                Inventory gui = Bukkit.createInventory(HOLDER, result.inventory().getSize(), name);
                gui.setContents(result.inventory().getContents());
                return gui;
            }

            // First open – create a fresh inventory
            return create(27);
        }

        /** Create a new, empty backpack inventory with the default size. */
        public static Inventory create(int size) {
            return Bukkit.createInventory(HOLDER, size, dess("Backpack"));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Returns a non-null air ItemStack instead of null, to simplify null checks. */
    private static ItemStack nullSafe(ItemStack item) {
        return (item != null) ? item : ItemStack.of(Material.AIR);
    }

    /** True iff the item has the {@code backpack} persistent-data key. */
    private static boolean isBackpack(ItemStack item) {
        return item != null
                && !item.getType().isAir()
                && item.getPersistentDataContainer().has(keygen("backpack"));
    }

    /**
     * Finds the backpack ItemStack in the closing player's main hand or off-hand.
     * Returns {@code null} if neither hand holds a backpack.
     */
    private static ItemStack findBackpackInHands(InventoryCloseEvent event) {
        ItemStack main = event.getPlayer().getInventory().getItemInMainHand();
        if (isBackpack(main)) return main;

        ItemStack off = event.getPlayer().getInventory().getItemInOffHand();
        if (isBackpack(off)) return off;

        return null;
    }
}