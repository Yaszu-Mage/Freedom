package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class AthleteArtifact implements Base_Artifact {
    @Override
    public Component Name() {
        return util.dess("<green>Athlete's Band</green>");
    }

    @Override
    public Component Description() {
        return util.dess("<gray>Run as fast as the wind.</gray>");
    }

    @Override
    public List<PotionEffect> getBuffs() {
        return List.of(new PotionEffect(PotionEffectType.SPEED, 12000, 0));
    }

    @Override
    public String getID() {
        return "athlete";
    }

    @Override
    public Material getMaterial() {
        return Material.RABBIT_FOOT;
    }
}
