package xyz.yaszu.freedom.Subsystems;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;

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
        Broken("Luca", "Deltarune Fan Song") /* FIXME UNABLE TO ACCESS ON SCHOOL WIFI*/,
        Getting_Pwned("Luca","Going out's not a good decision.")  /* FIXME UNABLE TO ACCESS ON SCHOOL WIFI*/,
        Koppen_as_fuck("Chris’s chrisotodoulou","--Find the TELEPORTER--")  /*FIXME UNABLE TO ACCESS ON SCHOOL WIFI*/,
        The_Rain_Formerly_Known_as_purple("Chris’s chrisotodoulou","--ESCAPE THE MOON--"),
        Overworld_Day("Terraria","Is it day?"),
        Third_Sanctuary("Toby Fox", "Third Sanctuary; but Minecraft"),
        Nine_Thrumamind("Phighting",""),
        To_You("Dreamcatcher",""),
        The_HouseBuilding_Song("David Ferguson",""),
        THE_MOST_WANTED("Phighting",""),
        Final_Duet("Omori",""),
        Bitter_And_Blunt("Yonkagor",""),
        Kill_This_Love("Blackpink",""),
        Raise_Up_Your_Bat("Toby Fox", "Raise up your bat and start a fight!");
        private final String author;
        private final String description;
        CustomSong(String author, String description) {
            this.author = author;
            this.description = description;
        }
    }
    public static HashMap<Location,CustomSong> jukes = new HashMap<>();
    @EventHandler
    public void onPlayerInputJukebox(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.JUKEBOX) return;
        Jukebox jukebox = (Jukebox) clickedBlock.getState();
        if (jukes.containsKey(clickedBlock.getLocation())) {
            //eject
            CustomSong disc = jukes.get(clickedBlock.getLocation());
            ItemStack discs = constructSong(disc);
            clickedBlock.getWorld().dropItemNaturally(clickedBlock.getLocation(), discs);
            //STOP MUSIC FROM PLAYING
            clickedBlock.getWorld().stopSound(Sound.sound(NamespacedKey.minecraft("custom." + disc.name()), SoundCategory.MASTER, 1.0F, 1.0F));
            event.setCancelled(true);
            return;
        }
        if (jukebox.isPlaying() || event.getPlayer().getInventory().getItemInMainHand() == null) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.CARROT_ON_A_STICK) return;
        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        if (!itemInHand.getItemMeta().getPersistentDataContainer().has(keygen("customsong"))) return;
        String songID = itemInHand.getItemMeta().getPersistentDataContainer().get(keygen("customsong"), PersistentDataType.STRING);
        CustomSong song = CustomSong.valueOf(songID);
        // Play the custom song on the jukebox using a resource pack disc
        // Send feedback to player
        event.getPlayer().sendMessage(dess("<green>Now playing:</green> <white>" + song.name().replace("_", " ") + " by " + song.author));
        clickedBlock.getWorld().playSound(clickedBlock.getLocation(),"custom." + song.name(), SoundCategory.MASTER, 1.0F, 1.0F);
        jukes.put(clickedBlock.getLocation(), song);
        // Cancel the event to prevent default behavior
        event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
        event.setCancelled(true);
    }


    public static ItemStack constructSong(CustomSong song){
        ItemStack stack = ItemStack.of(Material.CARROT_ON_A_STICK);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(keygen("customsong"), PersistentDataType.STRING, song.name());
        meta.setItemModel(NamespacedKey.minecraft(song.name()));
        stack.setItemMeta(meta);
        return stack;
    }
}
