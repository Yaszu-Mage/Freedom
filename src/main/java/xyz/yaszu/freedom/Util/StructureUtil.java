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
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;
import xyz.yaszu.freedom.Freedom;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class StructureUtil {

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
     * Loads and spawns a vanilla Minecraft structure from a resource bundled in the JAR.
     *
     * @param resourcePath Path to the .nbt structure file.
     * @param location     The location to spawn the structure at.
     */
    public static void spawnVanillaStructureFromResource(String resourcePath, Location location) {
        try (InputStream is = Freedom.get_plugin().getResource(resourcePath)) {
            if (is == null) {
                Freedom.get_plugin().getLogger().warning("Could not find vanilla structure resource: " + resourcePath);
                return;
            }

            Structure structure = Freedom.get_plugin().getServer().getStructureManager().loadStructure(is);
            structure.place(location, true, StructureRotation.NONE, Mirror.NONE, 0, 1.0f, new Random());
        } catch (IOException e) {
            Freedom.get_plugin().getLogger().severe("Error loading/spawning vanilla structure from resource: " + resourcePath);
            e.printStackTrace();
        }
    }
}
