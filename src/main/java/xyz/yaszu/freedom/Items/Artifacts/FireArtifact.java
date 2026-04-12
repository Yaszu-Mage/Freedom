package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class FireArtifact implements Base_Artifact {
    @Override
    public Component Name() {
        return util.dess("<gold>Firewalker's Boot</gold>");
    }

    @Override
    public Component Description() {
        return util.dess("<gray>Lava and fire are no longer your enemies.</gray>");
    }

    @Override
    public List<PotionEffect> getBuffs() {
        return List.of(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 12000, 0));
    }

    @Override
    public String getID() {
        return "fire";
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_BOOTS;
    }
}
