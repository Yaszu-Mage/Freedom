package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class WarriorArtifact implements Base_Artifact {
    @Override
    public Component Name() {
        return util.dess("<red>Warrior's Medallion</red>");
    }

    @Override
    public Component Description() {
        return util.dess("<gray>An emblem of great strength, forged in battle.</gray>");
    }

    @Override
    public List<PotionEffect> getBuffs() {
        return List.of(new PotionEffect(PotionEffectType.STRENGTH, 12000, 0));
    }

    @Override
    public String getID() {
        return "warrior";
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
    }
}
