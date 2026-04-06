package xyz.yaszu.freedom.Commands.DevTools;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi;
import xyz.yaszu.freedom.Soul.Base.BaseRed;
import xyz.yaszu.freedom.Soul.Ultra.Purple;
import xyz.yaszu.freedom.Soul.Ultra.Red;
import org.bukkit.Location;
import org.bukkit.generator.structure.Structure;
import org.bukkit.Registry;
import org.bukkit.entity.Player;

public class StructTest implements BasicCommand {

    Structure Test = Registry.STRUCTURE.get(org.bukkit.NamespacedKey.minecraft("test"));
    public void execute(CommandSourceStack source, String[] args) {
        Location location = Player.class.cast(source.getSender()).getLocation();
        Structure structure = Structure.SWAMP_HUT;

        location.getWorld().canGenerateStructures();
        //wait wait wait can't you just use world edit or smtn or are you trying to just use nbt?

        //i have the nbt im just tring to get it to generate any structer first the

        //bro who wrote ts
    }

}
