package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class ScholarArtifact implements Base_Artifact {
    @Override
    public Component Name() {
        return util.dess("<aqua>Scholar's Lens</aqua>");
    }

    @Override
    public Component Description() {
        return util.dess("<gray>Increases luck and loot found in the world.</gray>");
    }

    @Override
    public List<PotionEffect> getBuffs() {
        return List.of(new PotionEffect(PotionEffectType.LUCK, 12000, 2));
    }

    @Override
    public String getID() {
        return "scholar";
    }

    @Override
    public Material getMaterial() {
        return Material.GLASS_PANE;
    }
}
