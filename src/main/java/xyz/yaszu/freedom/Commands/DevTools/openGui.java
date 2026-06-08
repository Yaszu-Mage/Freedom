package xyz.yaszu.freedom.Commands.DevTools;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi;
import xyz.yaszu.freedom.Soul.Base.BaseRed;

public class openGui implements BasicCommand {
    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        if (commandSourceStack.getSender() instanceof Player) {
            Player player = (Player) commandSourceStack.getSender();
            if (!player.isOp()) return;
            BaseRed red = new BaseRed();
            selectionUi.open_UI(player,red);
        }
    }
}
