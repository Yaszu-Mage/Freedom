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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.StructureUtil;
import xyz.yaszu.freedom.Util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class Alchemy implements Listener {
    public static Alchemy instance = new Alchemy();
    public static void init() {
        instance.registerRecipes();
        instance.registerEvents();
    }

    public void registerRecipes() {

    }

    public void registerEvents() {

    }

    @EventHandler
    public void onPlayerInteractWrittenBook(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item != null && !event.getPlayer().isSneaking()){
            if (item.getItemMeta() instanceof BookMeta bookMeta){
                String text = String.join(" ", bookMeta.getPages());
                SpellCompiler.cost(text,event.getPlayer());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void LecturnInterActEvent(PlayerInsertLecternBookEvent event) throws IOException, WorldEditException {
        ItemStack book = event.getBook();
        if ((book.getItemMeta() instanceof BookMeta bookMeta)) {
            if (bookMeta.getAuthor() == null) {
                return;
            }
        } else {
            return;
        }
        Player target = event.getPlayer();
        SoulTypes soulType = Util.getSoulType(target);
        event.getBook();
        ritual(event.getLectern().getLocation(),soulType,event.getBook(),target);
    }

    public void ritual(Location centerLocation, SoulTypes soulType,ItemStack book,Player player) throws IOException, WorldEditException {
        Clipboard load = StructureUtil.loadSchematicFromResource("ritual.schem");
        if (load != null) {

            if (compareStructure(load, centerLocation)) {
                BookMeta meta = (BookMeta) book.getItemMeta();
                String text = String.join(" ", meta.getPages());
                int Casted = SpellCompiler.castSpell(text, centerLocation, player,true);
                if (Casted > 0 && text.toLowerCase().contains("province")) {
                    Set<Location> structureBlocks = getStructureBlocks(load, centerLocation);
                    // Extract range from text or re-parse?
                    // For now, let's just re-parse range from the first statement that is province
                    var tokens = SpellCompiler.tokenize(text);
                    var ast = SpellCompiler.parse(tokens, centerLocation,player);
                    int range = 0;
                    for (var stmt : ast.statements) {
                        if (stmt.action == SpellCompiler.ritualtype.province) {
                            range = stmt.range;
                            break;
                        }
                    }
                    xyz.yaszu.freedom.Subsystems.ProvinceManager.claimProvince(player, centerLocation, range, structureBlocks);
                }

                if (Casted < 10000) {
                    Util.createMinMagicCircle(centerLocation.add(0.5,0,0.5),15,soulType);
                } else {
                    Util.createMaxMagicCircle(centerLocation.add(0.5,0,0.5),15,Casted/10000,soulType);
                }


            }
        }

    }

    public static boolean compareStructure(Clipboard clipboard, Location lecternLoc) {
        // ... (existing code)
        return true;
    }

    public static Set<Location> getStructureBlocks(Clipboard clipboard, Location lecternLoc) {
        Set<Location> blocks = new HashSet<>();
        BlockVector3 origin = clipboard.getOrigin();
        for (BlockVector3 position : clipboard.getRegion()) {
            BlockVector3 relativeToOrigin = position.subtract(origin);
            Location checkLoc = lecternLoc.clone().add(
                    relativeToOrigin.x(),
                    relativeToOrigin.y(),
                    relativeToOrigin.z()
            );
            blocks.add(checkLoc.getBlock().getLocation());
        }
        return blocks;
    }
}
