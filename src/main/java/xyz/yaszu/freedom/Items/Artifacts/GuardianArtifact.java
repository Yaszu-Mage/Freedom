package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class GuardianArtifact implements Base_Artifact {
    @Override
    public Component Name() {
        return util.dess("<gray>Guardian's Charm</gray>");
    }

    @Override
    public Component Description() {
        return util.dess("<gray>Waking up with this item gives you a shield against all harm.</gray>");
    }

    @Override
    public List<PotionEffect> getBuffs() {
        return List.of(new PotionEffect(PotionEffectType.RESISTANCE, 12000, 0));
    }

    @Override
    public String getID() {
        return "guardian";
    }

    @Override
    public Material getMaterial() {
        return Material.SHIELD;
    }
}
