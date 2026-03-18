package xyz.yaszu.freedom.Commands.DevTools;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi;
import xyz.yaszu.freedom.Soul.Ultra.Purple;
import xyz.yaszu.freedom.Soul.Ultra.Red;

public class openGui implements BasicCommand {
    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        if (commandSourceStack.getSender() instanceof Player) {
            Player player = (Player) commandSourceStack.getSender();
            Purple purple = new Purple();
            Red red = new Red();
            selectionUi.open_UI(player,red);
            player.give(red.Related_Item());
            player.give(purple.Related_Item());

        }
    }
}
