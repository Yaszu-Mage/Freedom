package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Util.Util;

public class CustomSongHandler extends Util implements Listener {

    public static enum CustomSong {
        Sarajinae_Gregor("Project Moon", "A song sung by gregor"),
        Sarajinae_DonQuixote("Project Moon", "A song sung by Don Quixote"),
        Sarajinae_Heathcliff("Project Moon", "A song sung by Heathcliff"),
        Sarajinae_Ishmael("Project Moon", "A song sung by Ishmael"),
        Sarajinae_Rodion("Project Moon", "A song sung by Rodion"),
        Sarajinae_Sinclair("Project Moon", "A song sung by Sinclair"),
        Sarajinae_HongLu("Project Moon", "A song sung by Hong Lu"),
        Sarajinae_Ryoshu("Project Moon", "A song sung by Ryoshu"),
        Sarajinae_YiSang("Project Moon", "A song sung by Yi Sang"),
        Noli("Luca", "The world is bound to know Noli."),
        How_Many_More_Now("Luca","Song sun by Postman for Nostalgic Hangout Game"),
        Broken("Luca", "Deltarune Fan Song"),
        Getting_Pwned("Luca","Going out's not a good decision."),
        Koppen_as_fuck("Chris’s chrisotodoulou",""),
        The_Rain_Formerly_Known_as_purple("Chris’s chrisotodoulou",""),
        Overworld_Day("Terraria","Is it day?"),
        Third_Sanctuary("Toby Fox", "Third Sanctuary; but Minecraft"),
        Raise_Up_Your_Bat("Toby Fox", "Raise up your bat and start a fight!");
        private final String author;
        private final String description;
        CustomSong(String author, String description) {
            this.author = author;
            this.description = description;
        }
    }

    @EventHandler
    public void onPlayerInputJukebox(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.JUKEBOX) return;
        Jukebox jukebox = (Jukebox) clickedBlock.getState();

        if (jukebox.isPlaying() || event.getPlayer().getInventory().getItemInMainHand() == null) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AMETHYST_SHARD) return;
        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        if (!itemInHand.getItemMeta().getPersistentDataContainer().has(keygen("customsong"))) return;
        String songID = itemInHand.getItemMeta().getPersistentDataContainer().get(keygen("customsong"), PersistentDataType.STRING);
        CustomSong song = CustomSong.valueOf(songID);

        // Play the custom song on the jukebox using a resource pack disc
        // Using MUSIC_DISC_OTHERSIDE as a placeholder that resource pack will override
        jukebox.setPlaying(Material.MUSIC_DISC_OTHERSIDE);
        jukebox.update();

        // Send feedback to player
        event.getPlayer().sendMessage("Now playing: " + song.name().replace("_", " "));

        // Cancel the event to prevent default behavior
        event.setCancelled(true);
    }
}
