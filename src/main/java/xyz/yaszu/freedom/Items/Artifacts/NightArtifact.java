package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class NightArtifact implements Base_Artifact {
    @Override
    public Component Name() {
        return util.dess("<dark_purple>Night Owl's Eye</dark_purple>");
    }

    @Override
    public Component Description() {
        return util.dess("<gray>See clearly through the darkest of nights.</gray>");
    }

    @Override
    public List<PotionEffect> getBuffs() {
        return List.of(new PotionEffect(PotionEffectType.NIGHT_VISION, 12000, 0));
    }

    @Override
    public String getID() {
        return "night";
    }

    @Override
    public Material getMaterial() {
        return Material.SPIDER_EYE;
    }
}
