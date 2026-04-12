package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class FeatherArtifact implements Base_Artifact {
    @Override
    public Component Name() {
        return util.dess("<white>Feather of Flight</white>");
    }

    @Override
    public Component Description() {
        return util.dess("<gray>Waking up with this item gives you agility like no other.</gray>");
    }

    @Override
    public List<PotionEffect> getBuffs() {
        return List.of(
            new PotionEffect(PotionEffectType.JUMP_BOOST, 12000, 1),
            new PotionEffect(PotionEffectType.SLOW_FALLING, 12000, 0)
        );
    }

    @Override
    public String getID() {
        return "feather";
    }

    @Override
    public Material getMaterial() {
        return Material.FEATHER;
    }
}
