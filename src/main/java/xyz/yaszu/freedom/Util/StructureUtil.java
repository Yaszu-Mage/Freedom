package xyz.yaszu.freedom.Util;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;
import xyz.yaszu.freedom.Freedom;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class StructureUtil {

    /**
     * Gets the dimensions (width, height, length) of a WorldEdit clipboard.
     *
     * @param clipboard The clipboard.
     * @return A BlockVector3 representing the size.
     */
    public static BlockVector3 getDimensions(Clipboard clipboard) {
        if (clipboard == null) return BlockVector3.ZERO;
        return clipboard.getDimensions();
    }

    /**
     * Gets the offset of the clipboard relative to its origin.
     *
     * @param clipboard The clipboard.
     * @return The offset.
     */
    public static BlockVector3 getOffset(Clipboard clipboard) {
        if (clipboard == null) return BlockVector3.ZERO;
        return clipboard.getRegion().getMinimumPoint().subtract(clipboard.getOrigin());
    }

    /**
     * Clears blocks above a certain height in a given area.
     *
     * @param world  The world.
     * @param minX   Minimum X.
     * @param minZ   Minimum Z.
     * @param maxX   Maximum X.
     * @param maxZ   Maximum Z.
     * @param startY Starting Y (inclusive).
     */
    public static void clearAbove(World world, int minX, int minZ, int maxX, int maxZ, int startY) {
        int maxY = world.getMaxHeight();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = startY; y < maxY; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }

    /**
     * Fills blocks below a certain height with a specified material.
     *
     * @param world    The world.
     * @param minX     Minimum X.
     * @param minZ     Minimum Z.
     * @param maxX     Maximum X.
     * @param maxZ     Maximum Z.
     * @param startY   Starting Y (exclusive, fills from startY - 1 downwards).
     * @param material The material to fill with.
     */
    public static void fillBelow(World world, int minX, int minZ, int maxX, int maxZ, int startY, Material material) {
        int minY = world.getMinHeight();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = startY - 1; y >= minY; y--) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR || block.getType() == Material.WATER || block.getType() == Material.LAVA) {
                        block.setType(material);
                    } else {
                        // Stop if we hit solid ground? The requirement says "all blocks under the structure match"
                        // Usually this means filling until we hit something non-air/non-liquid to ground it.
                        break;
                    }
                }
            }
        }
    }

    /**
     * Loads a WorldEdit clipboard from a resource bundled in the JAR.
     *
     * @param resourcePath Path to the resource (e.g., "ritual.schem")
     * @return The loaded clipboard, or null if loading failed.
     */
    public static Clipboard loadSchematicFromResource(String resourcePath) {
        try (InputStream is = Freedom.get_plugin().getResource(resourcePath)) {
            if (is == null) {
                Freedom.get_plugin().getLogger().warning("Could not find resource: " + resourcePath);
                return null;
            }

            // Find format by resource name (extension)
            ClipboardFormat format = ClipboardFormats.findByAlias(resourcePath.substring(resourcePath.lastIndexOf('.') + 1));
            if (format == null) {
                // Fallback for .schem
                if (resourcePath.endsWith(".schem")) {
                    format = ClipboardFormats.findByAlias("schem");
                } else if (resourcePath.endsWith(".schematic")) {
                    format = ClipboardFormats.findByAlias("schematic");
                }
            }

            if (format == null) {
                Freedom.get_plugin().getLogger().warning("Unknown schematic format for: " + resourcePath);
                return null;
            }

            try (ClipboardReader reader = format.getReader(is)) {
                return reader.read();
            }
        } catch (IOException e) {
            Freedom.get_plugin().getLogger().severe("Error loading schematic from resource: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Pastes a WorldEdit clipboard at the specified location.
     *
     * @param clipboard The clipboard to paste.
     * @param location  The location to paste at.
     * @return The EditSession used for pasting, for undo purposes.
     */
    public static EditSession spawnSchematic(Clipboard clipboard, Location location) {
        if (clipboard == null || location == null) return null;

        com.sk89q.worldedit.world.World adapter = BukkitAdapter.adapt(location.getWorld());
        EditSession editSession = WorldEdit.getInstance().newEditSession(adapter);
        try {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                    .build();
            Operations.complete(operation);
            return editSession;
        } catch (WorldEditException e) {
            Freedom.get_plugin().getLogger().severe("Error spawning schematic at " + location);
            e.printStackTrace();
            editSession.close();
            return null;
        } finally {
            editSession.flushSession();
        }
    }

    /**
     * Loads a vanilla Minecraft structure from a resource bundled in the JAR.
     *
     * @param resourcePath Path to the .nbt structure file.
     * @return The loaded structure, or null if loading failed.
     */
    public static Structure loadVanillaStructureFromResource(String resourcePath) {
        try (InputStream is = Freedom.get_plugin().getResource(resourcePath)) {
            if (is == null) {
                Freedom.get_plugin().getLogger().warning("Could not find vanilla structure resource: " + resourcePath);
                return null;
            }
            return Freedom.get_plugin().getServer().getStructureManager().loadStructure(is);
        } catch (IOException e) {
            Freedom.get_plugin().getLogger().severe("Error loading vanilla structure from resource: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads and spawns a vanilla Minecraft structure from a resource bundled in the JAR.
     *
     * @param resourcePath Path to the .nbt structure file.
     * @param location     The location to spawn the structure at.
     */
    public static void spawnVanillaStructureFromResource(String resourcePath, Location location) {
        Structure structure = loadVanillaStructureFromResource(resourcePath);
        if (structure != null) {
            structure.place(location, true, StructureRotation.NONE, Mirror.NONE, 0, 1.0f, new Random());
        }
    }
}
