package xyz.yaszu.freedom.Entities;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.platform.PlatformEntity;
import kr.toxicity.model.api.tracker.EntityTracker;
import kr.toxicity.model.api.tracker.TrackerModifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Util.Util;
import java.util.List;
import java.util.Objects;

public interface BaseEntity {
    public EntityType baseType();

    public int health();
    public int maxHealth();
    public String name();
    public List<GoalKey<Creature>> goals();


    public List<Biome> spawnBiomes();


    public default EntityTracker restore(Entity entity) {
        if (entity instanceof Mob mob) {
            Bukkit.getServer().getMobGoals().removeAllGoals(mob);
            goals().forEach(goal -> {
                Bukkit.getServer().getMobGoals().addGoal(mob,goals().indexOf(goal),goal);
            });
        }
        if (entity instanceof LivingEntity living) {
            Objects.requireNonNull(living.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(maxHealth());
            living.setHealth(health());
        }
        entity.getPersistentDataContainer().set(Util.keygen("customEntity"), PersistentDataType.STRING, name());
        PlatformEntity platEntity = BukkitAdapter.adapt(entity);
        // Simple Get or Create
        return BetterModel.model(name())
                .map(r -> r.getOrCreate(platEntity))
                .orElse(null);
    }


    public default EntityTracker initialize(Location location) {
        World world = location.getWorld();
        Entity entity = world.spawnEntity(location, baseType());
        return restore(entity);
    }



}
