package xyz.yaszu.freedom.GUI;

import net.kyori.adventure.text.Component;
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

    public Component loadingBar(double value, double max, double min) {
        int maxBars = 5;

        //
        if (max <= min) {
            return dess("<shadow:#000000FF><b>|<red>#####</red></b>");
        }

        //
        double progress = (value - min) / (max - min);

        //
        progress = Math.max(0, Math.min(1, progress));

        //
        int filledBars = (int) Math.round(progress * maxBars);

        StringBuilder filled = new StringBuilder();
        StringBuilder empty = new StringBuilder();

        for (int i = 0; i < filledBars; i++) {
            filled.append("#");
        }

        for (int i = filledBars; i < maxBars; i++) {
            empty.append("#");
        }

        return dess("<shadow:#000000FF><b>|<aqua>" + filled + "</aqua><gray>" + empty + "</gray></b>");
    }


    public ScoreboardLibrary getLibrary() {

        library = Freedom.scoreboardLibrary;
        return Freedom.scoreboardLibrary;
    }
}
