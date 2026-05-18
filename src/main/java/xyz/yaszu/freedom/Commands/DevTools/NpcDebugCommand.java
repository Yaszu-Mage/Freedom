package xyz.yaszu.freedom.Commands.DevTools;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import xyz.yaszu.freedom.GUI.NpcDebugGui;

public class NpcDebugCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        if (!(commandSourceStack.getSender() instanceof Player player)) {
            return;
        }
        if (!player.isOp()) {
            return;
        }
        NpcDebugGui.open(player);
    }
}

