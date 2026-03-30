package xyz.yaszu.freedom.Alchemy;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import io.papermc.paper.event.player.PlayerInsertLecternBookEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;

public class Alchemy extends Util implements Listener {
    public static Alchemy instance = new Alchemy();
    public static void init() {
        instance.registerRecipes();
        instance.registerEvents();
    }

    public void registerRecipes() {

    }

    public void registerEvents() {

    }

    public enum ritualkeywords  {
        soul,
        amplification,
        destruction,
        location,
        teleport,
        area,
        effect,
        regeneration,
        haste,
        speed,
        jump,
        fire,
        water,
        lightning,
        poison,
        wither,
        strength,
        weakness,
        range,
    }

    public class ritualkeywordConstructor {
        String[] keywords = {};
        public PotionEffect[] effects;
        public ritualkeywordConstructor(String[] keywords) {
            this.keywords = keywords;
            applyKeywords();
        }
        public void applyKeywords() {
            for (String keyword : keywords) {
                keyword = keyword.toLowerCase();
                ritualkeywords active_keyword = ritualkeywords.valueOf(keyword);
                switch (active_keyword) {
                    case soul:
                        //do soul stuff IDK
                        break;
                    case amplification:
                        //amplify previous keyword
                        break;
                    case destruction:
                        //destroy location after keyword
                        break;
                    case teleport:
                        //create portal to location after keyword
                        break;
                    case area:
                        //create area that has the effect after keyword
                        break;
                    case effect:
                        //create effect for previous keyword
                        break;
                    case regeneration,fire,water,lightning,poison,wither,strength,weakness,haste,speed,jump:
                        //effect for the previous keyword
                        break;
                    case range:
                        //range of previous keyword
                        break;
                }
            }
        }
        public ritualkeywordConstructor(String keywords) {
            this.keywords = keywords.split(" ");
            applyKeywords();
        }
    }
    public enum ritualtype {
        soul,
        amplification,
        destruction,
        teleport,
        area,
        effect,
        range
    }
    public class ritualeffecttype {
        public ritualtype type = ritualtype.area;
        public int value = 0;
        public PotionEffect effect;
        public Location teleportlocation;
        public ritualeffecttype(ritualtype type, PotionEffect pot, int range) {
            this.effect = pot;
            this.value = range;
        }
        public ritualeffecttype(ritualtype type, Location location) {
            this.type = type;
            this.teleportlocation = location;
        }
    }

    @EventHandler
    public void LecturnInterActEvent(PlayerInsertLecternBookEvent event) throws IOException, WorldEditException {
        Player target = event.getPlayer();
        SoulTypes soulType = SoulTypes.valueOf(target.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        event.getBook();
        ritual(event.getLectern().getLocation(),soulType,event.getBook(),target);
    }

    public void ritual(Location centerLocation, SoulTypes soulType,ItemStack book,Player player) throws IOException, WorldEditException {
        File ritualschem = Freedom.get_plugin().getDataFolder().toPath().resolve("ritual.schem").toFile();
        Freedom.get_plugin().getLogger().info(String.valueOf(Freedom.get_plugin().getDataFolder().toPath()));
        Clipboard load = loadSchematic(ritualschem);
        if (load != null) {

            if (compareStructure(load, centerLocation)) {
                Freedom.get_plugin().getLogger().info("Ritual structure matches!");
                BookMeta meta = (BookMeta) book.getItemMeta();
                String text = String.join(" ", meta.getPages());
                if (!SpellCompiler.castSpell(text, centerLocation, player)) {
                    createMinMagicCircle(centerLocation.add(0.5,0,0.5),15,soulType);
                }


            } else {
                Freedom.get_plugin().getLogger().info("Ritual structure does not match!");
            }
        } else {
            Freedom.get_plugin().getLogger().info("CLIPBOARD IS NULL");
        }

    }

    public static Clipboard loadSchematic(File file) throws IOException {

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            Freedom.get_plugin().getLogger().info("UNSUPPORTED FORMAT");
            return null; // Unsupported format
        }
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        }
    }

    public static boolean compareStructure(Clipboard clipboard, Location lecternLoc) {
        // The 'Origin' is where the player was standing when they ran //copy
        BlockVector3 origin = clipboard.getOrigin();

        for (BlockVector3 position : clipboard.getRegion()) {
            // Calculate where this schematic block SHOULD be in the world
            // relative to the Lectern's position
            BlockVector3 relativeToOrigin = position.subtract(origin);

            Location checkLoc = lecternLoc.clone().add(
                    relativeToOrigin.x(),
                    relativeToOrigin.y(),
                    relativeToOrigin.z()
            );

            Block worldBlock = checkLoc.getBlock();
            org.bukkit.Material expectedMaterial = BukkitAdapter.adapt(clipboard.getBlock(position).getBlockType());

            if (worldBlock.getType() != expectedMaterial) {
                return false;
            }
        }
        return true;
    }
}
