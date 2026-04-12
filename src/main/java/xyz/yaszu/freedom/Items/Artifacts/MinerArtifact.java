package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class MinerArtifact implements Base_Artifact {
    @Override
    public Component Name() {
        return util.dess("<gold>Miner's Dream</gold>");
    }

    @Override
    public Component Description() {
        return util.dess("<gray>Waking up with this item gives you the mining speed of a thousand men.</gray>");
    }

    @Override
    public List<PotionEffect> getBuffs() {
        return List.of(new PotionEffect(PotionEffectType.HASTE, 12000, 1));
    }

    @Override
    public String getID() {
        return "miner";
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_PICKAXE;
    }
}
