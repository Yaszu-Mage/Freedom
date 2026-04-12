package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class OceanArtifact implements Base_Artifact {
    @Override
    public Component Name() {
        return util.dess("<blue>Ocean's Heart</blue>");
    }

    @Override
    public Component Description() {
        return util.dess("<gray>Breathe underwater and swim like a dolphin.</gray>");
    }

    @Override
    public List<PotionEffect> getBuffs() {
        return List.of(
            new PotionEffect(PotionEffectType.WATER_BREATHING, 12000, 0),
            new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 12000, 0)
        );
    }

    @Override
    public String getID() {
        return "ocean";
    }

    @Override
    public Material getMaterial() {
        return Material.HEART_OF_THE_SEA;
    }
}
