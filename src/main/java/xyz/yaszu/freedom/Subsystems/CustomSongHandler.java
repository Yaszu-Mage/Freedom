package xyz.yaszu.freedom.Subsystems;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.Collection;
import java.util.HashMap;

public class CustomSongHandler extends Util implements Listener {

    private static final float SOUND_VOLUME = 4.0F; // Controls broadcast radius (~64 blocks at 4.0)
    private static final float SOUND_PITCH  = 1.0F;

    public enum CustomSong {
        sarajinae_gregor("Project Moon", "A song sung by gregor"),
        sarajinae_donquixote("Project Moon", "A song sung by Don Quixote"),
        sarajinae_heathcliff("Project Moon", "A song sung by Heathcliff"),
        sarajinae_ishmael("Project Moon", "A song sung by Ishmael"),
        sarajinae_rodion("Project Moon", "A song sung by Rodion"),
        sarajinae_sinclair("Project Moon", "A song sung by Sinclair"),
        sarajinae_hongLu("Project Moon", "A song sung by Hong Lu"),
        sarajinae_ryoshu("Project Moon", "A song sung by Ryoshu"),
        sarajinae_yisang("Project Moon", "A song sung by Yi Sang"),
        noli("Luca", "The world is bound to know Noli."),
        how_many_more_now("Luca", "Song sung by Postman for Nostalgic Hangout Game"),
        broken("Luca", "Deltarune Fan Song"),
        getting_pwned("Luca", "Going out's not a good decision."),
        koppen_as_fuck("Chris's chrisotodoulou", "--Find the TELEPORTER--"),
        the_rain_formerly_known_as_purple("Chris's chrisotodoulou", "--ESCAPE THE MOON--"),
        overworld_day("Terraria", "Is it day?"),
        third_sanctuary("Toby Fox", "Third Sanctuary; but Minecraft"),
        nine_thrumamind("Phighting", ""),
        to_you("Dreamcatcher", ""),
        the_housebuilding_song("David Ferguson", ""),
        the_most_wanted("Phighting", ""),
        final_duet("Omori", ""),
        bitter_and_blunt("Yonkagor", ""),
        kill_this_love("Blackpink", ""),
        raise_up_your_bat("Toby Fox", "Raise up your bat and start a fight!");

        private final String author;
        private final String description;

        CustomSong(String author, String description) {
            this.author = author;
            this.description = description;
        }
    }

    public static final HashMap<Location, CustomSong> jukes = new HashMap<>();

    @EventHandler
    public void onPlayerInputJukebox(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.JUKEBOX) return;

        Location loc = clickedBlock.getLocation();

        // =========================
        // EJECT CUSTOM DISC
        // =========================
        if (jukes.containsKey(loc)) {

            CustomSong disc = jukes.remove(loc);

            Key soundKey = Key.key("custom." + disc.name());
            Collection<? extends Player> nearbyPlayers =
                    loc.getWorld().getNearbyPlayers(loc, 64, 64, 64);

            SoundStop stop = SoundStop.namedOnSource(
                    Key.key("custom." + disc.name()),
                    Sound.Source.RECORD
            );

            // Send the stop to every player in range individually
            loc.getWorld().getNearbyPlayers(loc, 64, 64, 64)
                    .forEach(p -> p.stopSound(stop));

            // Give disc back
            clickedBlock.getWorld().dropItemNaturally(
                    loc.clone().add(0.5, 1, 0.5),
                    constructSong(disc)
            );

            event.setCancelled(true);
            return;
        }

        // =========================
        // INSERT CUSTOM DISC
        // =========================

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) return;
        if (item.getType() != Material.CARROT_ON_A_STICK) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(keygen("customsong"), PersistentDataType.STRING)) return;

        String songID = meta.getPersistentDataContainer().get(keygen("customsong"), PersistentDataType.STRING);
        if (songID == null) return;

        CustomSong song;
        try {
            song = CustomSong.valueOf(songID);
        } catch (IllegalArgumentException ex) {
            return;
        }

        // BUG FIX: Use a high volume value so the sound broadcasts to all players
        // in a reasonable radius (~64 blocks). Volume > 1.0 expands the radius.
        clickedBlock.getWorld().playSound(
                loc,
                "custom." + song.name(),
                SoundCategory.RECORDS,
                SOUND_VOLUME,
                SOUND_PITCH
        );

        jukes.put(loc, song);

        item.setAmount(item.getAmount() - 1);

        event.getPlayer().sendMessage(
                dess("<green>Now playing:</green> <white>"
                        + song.name().replace("_", " ")
                        + " by " + song.author)
        );

        event.setCancelled(true);
    }

    public static ItemStack constructSong(CustomSong song) {
        ItemStack stack = ItemStack.of(Material.CARROT_ON_A_STICK);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess(song.name().replace("_", " ").toUpperCase()));
        meta.getPersistentDataContainer().set(keygen("customsong"), PersistentDataType.STRING, song.name());
        meta.setItemModel(NamespacedKey.minecraft(song.name()));
        stack.setItemMeta(meta);
        return stack;
    }
}