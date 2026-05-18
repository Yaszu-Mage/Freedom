package xyz.yaszu.freedom.GUI;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Subsystems.NpcManager;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NpcDebugGui extends Util implements InventoryHolder {
    private static final int GUI_SIZE = 54;
    private static final double SEARCH_RADIUS = 64.0;

    private final Inventory inventory;

    public NpcDebugGui(NpcManager.NPC npc, double distance) {
        this.inventory = Bukkit.createInventory(this, GUI_SIZE, dess("NPC Debug"));
        if (npc == null || npc.BaseEntity == null) {
            setNoNpc();
        } else {
            populateNpc(npc, distance);
        }
    }

    public static void open(Player player) {
        if (player == null) {
            return;
        }
        NpcManager.NPC nearest = NpcManager.getNearestNpc(player, SEARCH_RADIUS);
        double distance = nearest != null && nearest.BaseEntity != null
                ? nearest.BaseEntity.getLocation().distance(player.getLocation())
                : -1.0;
        NpcDebugGui gui = new NpcDebugGui(nearest, distance);
        player.openInventory(gui.getInventory());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private void setNoNpc() {
        inventory.setItem(22, buildItem(Material.BARRIER, "<red>No NPC nearby</red>",
                List.of(
                        "<gray>Move closer and try again.</gray>",
                        "<gray>Search radius: " + SEARCH_RADIUS + "</gray>"
                )));
    }

    private void populateNpc(NpcManager.NPC npc, double distance) {
        Location npcLocation = npc.BaseEntity.getLocation();

        inventory.setItem(10, buildItem(Material.NAME_TAG, "<green>Identity</green>", List.of(
                "<gray>Name: <white>" + safeString(getPdcString(npc.data, "name", PersistentDataType.STRING)) + "</white></gray>",
                "<gray>NPC ID: <white>" + safeString(getPdcString(npc.data, "NPCID", PersistentDataType.STRING)) + "</white></gray>",
                "<gray>Entity UUID: <white>" + npc.BaseEntity.getUniqueId() + "</white></gray>",
                "<gray>Type: <white>" + npc.BaseEntity.getType() + "</white></gray>"
        )));

        inventory.setItem(11, buildItem(Material.COMPASS, "<green>Location</green>", List.of(
                "<gray>World: <white>" + safeString(worldName(npcLocation.getWorld())) + "</white></gray>",
                "<gray>Pos: <white>" + formatLocation(npcLocation) + "</white></gray>",
                "<gray>Distance: <white>" + formatDistance(distance) + "</white></gray>"
        )));

        inventory.setItem(12, buildItem(Material.COOKED_BEEF, "<green>Stats</green>", List.of(
                "<gray>Goal: <white>" + npc.getState() + "</white></gray>",
                "<gray>Hunger: <white>" + formatNumber(npc.hunger) + "</white></gray>",
                "<gray>Health: <white>" + formatNumber(npc.BaseEntity.getHealth()) + "</white></gray>",
                "<gray>Max Health: <white>" + safeString(getMaxHealth(npc)) + "</white></gray>"
        )));

        inventory.setItem(13, buildItem(Material.PLAYER_HEAD, "<green>Skin</green>", List.of(
                "<gray>Skin: <white>" + safeString(getPdcString(npc.data, "skin", PersistentDataType.STRING)) + "</white></gray>",
                "<gray>Skin Value: <white>" + shorten(getPdcString(npc.data, "skinValue", PersistentDataType.STRING)) + "</white></gray>",
                "<gray>Skin Signature: <white>" + shorten(getPdcString(npc.data, "skinSignature", PersistentDataType.STRING)) + "</white></gray>",
                "<gray>Popular: <white>" + safeString(getPdcString(npc.data, "isPopular", PersistentDataType.BOOLEAN)) + "</white></gray>"
        )));

        inventory.setItem(14, buildItem(Material.OAK_DOOR, "<green>Home</green>", List.of(
                "<gray>Has Home: <white>" + safeString(getPdcString(npc.data, "home", PersistentDataType.BOOLEAN)) + "</white></gray>",
                "<gray>World: <white>" + safeString(getPdcString(npc.data, "homeworld", PersistentDataType.STRING)) + "</white></gray>",
                "<gray>X: <white>" + safeString(getPdcString(npc.data, "homeX", PersistentDataType.DOUBLE)) + "</white></gray>",
                "<gray>Y: <white>" + safeString(getPdcString(npc.data, "homeY", PersistentDataType.DOUBLE)) + "</white></gray>",
                "<gray>Z: <white>" + safeString(getPdcString(npc.data, "homeZ", PersistentDataType.DOUBLE)) + "</white></gray>"
        )));

        inventory.setItem(15, buildItem(Material.CRAFTING_TABLE, "<green>Build Target</green>", List.of(
                "<gray>World: <white>" + safeString(getPdcString(npc.data, "buildWorld", PersistentDataType.STRING)) + "</white></gray>",
                "<gray>X: <white>" + safeString(getPdcString(npc.data, "buildX", PersistentDataType.DOUBLE)) + "</white></gray>",
                "<gray>Y: <white>" + safeString(getPdcString(npc.data, "buildY", PersistentDataType.DOUBLE)) + "</white></gray>",
                "<gray>Z: <white>" + safeString(getPdcString(npc.data, "buildZ", PersistentDataType.DOUBLE)) + "</white></gray>"
        )));

        inventory.setItem(16, buildItem(Material.CHEST, "<green>Inventory</green>", List.of(
                "<gray>Slots: <white>" + npc.getInventory().getSize() + "</white></gray>",
                "<gray>Filled: <white>" + countFilledSlots(npc.getInventory()) + "</white></gray>"
        )));

        inventory.setItem(28, buildItem(Material.PAPER, "<green>PDC Flags</green>", List.of(
                "<gray>Is NPC: <white>" + safeString(getPdcString(npc.data, "isNpc", PersistentDataType.BOOLEAN)) + "</white></gray>",
                "<gray>Move Ticks: <white>" + safeString(getPdcString(npc.data, "moveTicks", PersistentDataType.INTEGER)) + "</white></gray>",
                "<gray>Allowed Move: <white>" + safeString(getPdcString(npc.data, "isallowedtomove", PersistentDataType.BOOLEAN)) + "</white></gray>",
                "<gray>Goal: <white>" + safeString(getPdcString(npc.data, "goal", PersistentDataType.STRING)) + "</white></gray>",
                "<gray>Hunger: <white>" + safeString(getPdcString(npc.data, "hunger", PersistentDataType.DOUBLE)) + "</white></gray>"
        )));

        inventory.setItem(31, buildItem(Material.ENDER_EYE, "<green>World Info</green>", List.of(
                "<gray>Weather: <white>" + safeString(weatherName(npcLocation.getWorld())) + "</white></gray>",
                "<gray>Time: <white>" + safeString(timeName(npcLocation.getWorld())) + "</white></gray>",
                "<gray>Biome: <white>" + npcLocation.getBlock().getBiome().name() + "</white></gray>"
        )));
    }

    private ItemStack buildItem(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(dess(name));
        if (loreLines != null && !loreLines.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(dess(line));
            }
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private String getPdcString(PersistentDataContainer data, String key, PersistentDataType<?, ?> type) {
        if (data == null || key == null || key.isEmpty() || type == null) {
            return "null";
        }
        if (!data.has(keygen(key), type)) {
            return "null";
        }
        Object value = data.get(keygen(key), type);
        return value == null ? "null" : value.toString();
    }

    private String formatLocation(Location location) {
        if (location == null) {
            return "null";
        }
        return String.format(Locale.US, "%.2f, %.2f, %.2f", location.getX(), location.getY(), location.getZ());
    }

    private String formatDistance(double distance) {
        if (distance < 0) {
            return "n/a";
        }
        return String.format(Locale.US, "%.2f", distance);
    }

    private String formatNumber(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String shorten(String value) {
        if (value == null || value.equals("null")) {
            return "null";
        }
        if (value.length() <= 24) {
            return value;
        }
        return value.substring(0, 24) + "...";
    }

    private String safeString(String value) {
        return value == null ? "null" : value;
    }

    private String worldName(World world) {
        return world == null ? "null" : world.getName();
    }

    private String weatherName(World world) {
        if (world == null) {
            return "null";
        }
        if (world.isClearWeather()) {
            return "clear";
        }
        return world.hasStorm() ? "storm" : "rain";
    }

    private String timeName(World world) {
        if (world == null) {
            return "null";
        }
        long time = world.getTime();
        return String.valueOf(time);
    }

    private int countFilledSlots(Inventory inventory) {
        int count = 0;
        if (inventory == null) {
            return count;
        }
        for (ItemStack item : inventory.getContents()) {
            if (item != null && !item.getType().isAir()) {
                count++;
            }
        }
        return count;
    }

    private String getMaxHealth(NpcManager.NPC npc) {
        if (npc == null || npc.BaseEntity == null || npc.BaseEntity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) == null) {
            return "null";
        }
        return formatNumber(npc.BaseEntity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
    }

    public static class NpcDebugGuiListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getInventory().getHolder() instanceof NpcDebugGui) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event) {
            if (event.getInventory().getHolder() instanceof NpcDebugGui) {
                event.setCancelled(true);
            }
        }
    }
}
