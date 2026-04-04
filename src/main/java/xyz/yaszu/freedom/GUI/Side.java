package xyz.yaszu.freedom.GUI;

import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;

public class Side extends Util {
    public static ScoreboardLibrary library = Freedom.scoreboardLibrary;
    public static HashMap<Player, Sidebar> scoreboards = new HashMap<>();


    public void open(Player player) {
        library = getLibrary();
        Sidebar sidebar = library.createSidebar();
        //Create UI
        sidebar.line(0,dess("<shadow:#000000FF><b><green>Lives</green></b>: " + player.getPersistentDataContainer().get(keygen("life"), PersistentDataType.INTEGER) + "/9"));
        sidebar.line(1,dess("SoulPoints: " + player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE)));

        sidebar.addPlayer(player);
    }

    public ScoreboardLibrary getLibrary() {

        library = Freedom.scoreboardLibrary;
        return Freedom.scoreboardLibrary;
    }
}
