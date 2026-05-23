package xyz.yaszu.freedom.Items.Swords;

import net.kyori.adventure.text.Component;
import xyz.yaszu.freedom.Items.Swords.Items.*;

import java.util.List;

public interface Sword {
    public List<Component> visions();
    public static Sword getSword(String name) {
        name = name.toLowerCase();
        switch (name) {
            case "darkheart": return new Darkheart();
            case "venomshank": return new Venomshank();
            case "icedagger": return new Icedagger();
            case "firebrand": return new Firebrand();
            case "windforce": return new Windforce();
            case "ghostwalker": return new Ghostwalker();
            case "illumina": return new Illumina();
        }
        return null;
    }
    public static Sword getSword(VisionHandler.SwordType type) {
        return getSword(type.name());
    }
}
