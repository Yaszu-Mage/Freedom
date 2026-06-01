package xyz.yaszu.freedom.Entities.Passive;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
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
    public List<GoalKey<? extends Mob>> goals() {
        return List.of(
                VanillaGoal.FLOAT,              // priority 1 - don't drown
                VanillaGoal.PANIC,              // priority 2 - flee when hurt
                VanillaGoal.FOLLOW_PARENT,      // priority 3 - babies follow parents
                VanillaGoal.BREED,              // priority 4 - breed with wheat
                VanillaGoal.TEMPT,              // priority 5 - follow held food
                VanillaGoal.RANDOM_STROLL,      // priority 6 - wander around
                VanillaGoal.LOOK_AT_PLAYER,     // priority 7 - look at nearby players
                VanillaGoal.RANDOM_LOOK_AROUND  // priority 8 - idle look around
        );
    }

    @Override
    public List<Biome> spawnBiomes() {
        return List.of(
                Biome.PLAINS,
                Biome.FLOWER_FOREST,
                Biome.SUNFLOWER_PLAINS
        );
    }
}