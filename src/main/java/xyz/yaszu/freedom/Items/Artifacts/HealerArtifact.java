package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class HealerArtifact implements Base_Artifact {
    @Override
    public Component Name() {
        return util.dess("<light_purple>Healer's Totem</light_purple>");
    }

    @Override
    public Component Description() {
        return util.dess("<gray>Waking up with this item gives you incredible recovery.</gray>");
    }

    @Override
    public List<PotionEffect> getBuffs() {
        return List.of(new PotionEffect(PotionEffectType.REGENERATION, 12000, 0));
    }

    @Override
    public String getID() {
        return "healer";
    }

    @Override
    public Material getMaterial() {
        return Material.GHAST_TEAR;
    }
}
