package xyz.yaszu.freedom.Alchemy;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.block.Block;
import xyz.yaszu.freedom.Freedom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class Alchemy {
    public static Alchemy instance = new Alchemy();
    public static void init() {
        instance.registerRecipes();
        instance.registerEvents();
    }

    public void registerRecipes() {

    }

    public void registerEvents() {

    }


    public void ritual(Location centerLocation) throws IOException {
        File ritualschem = Freedom.get_plugin().getDataFolder().toPath().resolve("ritual.schem").toFile();
        Clipboard load = loadSchematic(ritualschem);
        if (compareStructure(load, centerLocation)) {
            Freedom.get_plugin().getLogger().info("Ritual structure matches!");
        }
    }

    public Clipboard loadSchematic(File file) throws IOException {

        ClipboardFormat format = ClipboardFormats.findByPath(Path.of(file.getPath()));
        if (format == null) {
            return null; // Unsupported format
        }
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        }
    }

    public boolean compareStructure(Clipboard clipboard, Location pasteLoc) {
        // Iterate through all blocks in the clipboard's region
        for (BlockVector3 clipboardPos : clipboard.getRegion()) {
            // Calculate the corresponding world location
            BlockVector3 relativePos = clipboardPos.subtract(clipboard.getMinimumPoint());
            Location worldLoc = pasteLoc.clone().add(relativePos.x(), relativePos.y(), relativePos.z());
            Block worldBlock = worldLoc.getBlock();

            // Get the expected block type from the clipboard
            // Note: WorldEdit API handles block data/states internally
            org.bukkit.Material expectedMaterial = BukkitAdapter.adapt(clipboard.getBlock(clipboardPos).getBlockType());

            // Compare material types (and potentially block data/states if needed)
            if (worldBlock.getType() != expectedMaterial) {
                return false; // Structure does not match
            }
        }
        return true; // All blocks match
    }
}
