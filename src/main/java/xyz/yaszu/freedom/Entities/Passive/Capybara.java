package xyz.yaszu.freedom.Entities.Passive;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import org.bukkit.block.Biome;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import xyz.yaszu.freedom.Entities.BaseEntity;
import xyz.yaszu.freedom.Util.Util;

import java.util.List;

public class Capybara extends Util implements BaseEntity {
    @Override
    public EntityType baseType() {
        return EntityType.PIG;
    }

    @Override
    public int health() {
        return 10;
    }

    @Override
    public int maxHealth() {
        return 10;
    }

    @Override
    public String name() {
        return "capybara";
    }

    @Override
    public List<GoalKey<Creature>> goals() {
        return List.of(VanillaGoal.HURT_BY,VanillaGoal.PANIC);
    }

    @Override
    public List<Biome> spawnBiomes() {
        return List.of();
    }
}
